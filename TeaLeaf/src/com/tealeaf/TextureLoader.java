/* @license
 * This file is part of the Game Closure SDK.
 *
 * The Game Closure SDK is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * The Game Closure SDK is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with the Game Closure SDK.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.tealeaf;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.tealeaf.event.ImageLoadedEvent;
import com.tealeaf.event.ImageErrorEvent;
import com.tealeaf.event.LogEvent;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Base64;
import android.util.Pair;

// TODO: Better logger that has subsystems that can be turned on and off

// TODO: All of these side-loader classes should have a common base class that
// gets all the in-flight queue clearing and other tricky aspects right so that
// it doesn't need to be fixed in each case specially, for example.
public class TextureLoader implements Runnable {
	private TeaLeaf tealeaf;
	private boolean running = false;
	private int failedCount = 0;
	private boolean isClearing = false; // Indicate if in-flight loads are to be cleared
	// NOTE: This monitor is used for internal synchronization with the loader thread
	// And the class object is used for external synchronization with other threads
	private Object monitor = new Object();
	private HashSet<String> texturesToLoad = new HashSet<String>();
	private ResourceManager resourceManager;
	private TextManager textManager;
	private ContactList contactList;
	private ContactPicturesLoader contactPicturesLoader;
	private Thread contactPicturesLoaderThread, cameraPictureLoaderThread,
			galleryPictureLoaderThread;
	private PhotoLoader cameraPictureLoader, galleryPictureLoader;
	private PhotoPicker photoPicker;

	public TextureLoader(TeaLeaf tealeaf, ResourceManager resourceManager, TextManager textManager, ContactList contactList) {
		this.tealeaf = tealeaf;
		this.resourceManager = resourceManager;
		this.textManager = textManager;
		this.contactList = contactList;
		this.contactPicturesLoaderThread = null;
		this.photoPicker = new PhotoPicker(tealeaf, tealeaf.getSettings(), resourceManager);
	}

	public void run() {
		running = true;

		while (running) {
			Set<String> textures = null;

			try {
				synchronized (monitor) {
					// If there is no more data to process,
					if (texturesToLoad == null || texturesToLoad.size() <= 0) {
						monitor.wait();
					}

					// While locked, steal a set of textures to load
					textures = new HashSet<String>(texturesToLoad);
					texturesToLoad.clear();

					// Initialize the "is clearing in-flight" flag to false
					isClearing = false;
				}

				// Load the textures from the set
				if (textures != null && textures.size() > 0) {
					for (String texture : textures) {
						load(texture);
					}
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	private void loadingError(String url) {
		logger.log("{texture} WARNING: Unable to load image", url);
		ImageErrorEvent event = new ImageErrorEvent(url);
		EventQueue.pushEvent(event);
		//put the failed texture on the loaded image queue, it will then get cleared
		//from native
		TextureData td = new TextureData(url, false);
		synchronized (monitor) {
			if (!isClearing) {
				tealeaf.glView.pushLoadedImage(td);
			} else {
				logger.log("{texture} WARNING: Aborting pushing loaded image during clearing");
			}
		}
	}

	// Abort loading textures in the queue
	// NOTE: Clears the textures-to-load list, but not any in-flight that are
	// to be passed to texture_manager_on_texture_loaded() on the next GLThread
	// tick event.
	public synchronized void clearTextureLoadQueue() {
		synchronized(monitor) {
			isClearing = true;
			texturesToLoad.clear();
		}
	}

	// DANGER: This thread is called from threads other than GLThread!
	// In particular, it is called by the Android Native code from the texture loading thread
	public synchronized void loadTexture(String url) {
		// Handle @TEXT in GL thread specially to avoid flickering setText()
		// The trade-off is that memory management isn't applied on this frame but
		// text is typically essential for a game to look correct and small enough
		// to avoid causing problems.
		if (url.startsWith("@TEXT")) {
			if (tealeaf.glView.isGLThread()) {
				Bitmap bmp = null;
				try {
					bmp = textManager.getText(url);
					if (bmp == null) {
						loadingError(url);
					} else {
						TextureData td = getTextureData(url, bmp);
						if (td == null) {
							return;
						}
						finishLoadingTexture(td);
						EventQueue.pushEvent((new ImageLoadedEvent(td.url,
								td.width, td.height, td.originalWidth,
								td.originalHeight, td.name)));
						// Number of channels (last argument) is always 4 for now
						// (RGBA8888)
						NativeShim.onTextureLoaded(td.url, td.name, td.width,
								td.height, td.originalWidth, td.originalHeight, 4);
					}
				} catch (OutOfMemoryError e) {
					logger.log(e);
					loadTexture(url);
				}
				return;
			}
		}

		synchronized (monitor) {
			texturesToLoad.add(url);
			monitor.notify();
		}
	}

	private void loadContactPictures(String url) {
		if (contactPicturesLoaderThread == null) {
			contactPicturesLoader = new ContactPicturesLoader(url);
			contactPicturesLoaderThread = new Thread(contactPicturesLoader);
			contactPicturesLoaderThread.start();
		}
		contactPicturesLoader.addPicturesToLoad(url);
	}

	private Bitmap loadGalleryPicture(String id) {
		if (galleryPictureLoaderThread == null) {
			galleryPictureLoader = new PhotoLoader("GALLERYPHOTO");
			galleryPictureLoaderThread = new Thread(galleryPictureLoader);
			galleryPictureLoaderThread.start();
		}
		String[] parts = id.split("-");
		int intid = Integer.parseInt(parts[0]);
		int size = 64;
		if (parts.length > 1) {
			size = Integer.parseInt(parts[1]);
		}
		Bitmap bmp = photoPicker.getResult("galleryphoto", intid);
		if(bmp != null) {
			return scaleTo(size, bmp);
		} else {
			galleryPictureLoader.addPictureAndSize(intid, size);
			photoPicker.choose(intid);
		}
		return null;
	}

	public void finishGalleryPicture(int id) {
		if (galleryPictureLoader != null) {
			galleryPictureLoader.markFinishedPicture(id);
		}
	}

	public void saveGalleryPicture(int id, Bitmap bitmap) {
		photoPicker.save("galleryphoto", id, bitmap);
	}

	public void failedGalleryPicture(int id) {
		loadingError("@GALLERYPHOTO" + id);
	}

	private Bitmap loadCameraPicture(String id) {
		if (cameraPictureLoaderThread == null) {
			cameraPictureLoader = new PhotoLoader("CAMERA");
			cameraPictureLoaderThread = new Thread(cameraPictureLoader);
			cameraPictureLoaderThread.start();
		}
		String[] parts = id.split("-");
		int intid = Integer.parseInt(parts[0]);
		int size = 64;
		if (parts.length > 1)
			size = Integer.parseInt(parts[1]);
		Bitmap bmp = photoPicker.getResult("camera", intid);
		if (bmp != null) {
			return scaleTo(size, bmp);
		} else {
			cameraPictureLoader.addPictureAndSize(intid, size);
			photoPicker.take(intid);
		}
		return null;
	}

	public void finishCameraPicture(int id) {
		if (cameraPictureLoader != null) {
			cameraPictureLoader.markFinishedPicture(id);
		}
	}

	public void saveCameraPhoto(int id, Bitmap bitmap) {
		photoPicker.save("camera", id, bitmap);
	}

	public void failedCameraPicture(int id) {
		loadingError("@CAMERA" + id);
	}

	public int getNextCameraId() {
		return photoPicker.getNextCameraId();
	}

	public int getNextGalleryId() {
		return photoPicker.getNextGalleryId();
	}

	private Bitmap scaleTo(int size, Bitmap bmp) {
		Bitmap bitmap = getBitmap(size, size);
		Canvas c = new Canvas(bitmap);
		int originalWidth = bmp.getWidth();
		int originalHeight = bmp.getHeight();

		float centerW = originalWidth / 2, centerH = originalHeight / 2;
		Rect src = new Rect((int) (centerW - size), (int) (centerH - size),(int) (centerW + size), (int) (centerH + size));
		Rect dst = new Rect(0, 0, size, size);
		c.drawBitmap(bmp, src, dst, null);
		return bitmap;
	}

	class PhotoLoader implements Runnable {
		private Object monitor = new Object();
		private HashMap<Integer, Integer> ids = new HashMap<Integer, Integer>();
		private ArrayList<Pair<Integer, Integer>> finished = new ArrayList<Pair<Integer, Integer>>();
		private String tag;

		public PhotoLoader(String tag) {
			this.tag = tag;
		}

		public void run() {
			while (true) {
				// wait for a signal that the camera is done
				try {
					synchronized (monitor) {
						monitor.wait();
						logger.log("{texture} Camera finished. Loading the picture");
					}

					ArrayList<Pair<Integer, Integer>> ids = getLoadedIds();
					for (Pair<Integer, Integer> pair : ids) {
						int id = pair.first;
						int size = pair.second;
						Bitmap bmp = photoPicker.getResult(tag.toLowerCase(), id);
						if (bmp != null) {
							Bitmap bitmap = scaleTo(size, bmp);
							bmp.recycle();
							bmp = null;
							loadTexture("@" + tag.toUpperCase() + id + "-" + size, bitmap);
						} else {
							loadingError("@" + tag.toUpperCase() + id + "-" + size);
						}
					}
				} catch (InterruptedException e) {
					logger.log(e);
				}
			}
		}

		private void addPictureAndSize(int id, int size) {
			ids.put(id, size);
		}

		private ArrayList<Pair<Integer, Integer>> getLoadedIds() {
			ArrayList<Pair<Integer, Integer>> ret = new ArrayList<Pair<Integer, Integer>>(finished);
			ids.clear();
			return ret;
		}

		public void markFinishedPicture(int id) {
			synchronized (monitor) {
				finished.add(new Pair<Integer, Integer>(id, ids.get(id)));
				monitor.notify();
			}
		}
	}

	class ContactPicturesLoader implements Runnable {
		private Object monitor = new Object();
		private int failedCount = 0;
		private ArrayList<String> toLoad = new ArrayList<String>();

		public ContactPicturesLoader(String url) {
			toLoad.add(url);
		}

		public synchronized void addPicturesToLoad(String url) {
			synchronized (monitor) {
				toLoad.add(url);
				monitor.notify();
			}
		}

		private ArrayList<String> getPicturesToLoad() {
			ArrayList<String> ret = new ArrayList<String>(toLoad);
			toLoad.clear();
			return ret;
		}

		public void run() {
			while (true) {
				ArrayList<String> pictures = null;
				do {
					pictures = getPicturesToLoad();
					for (String picture : pictures) {
						loadPictures(picture);
					}
				} while (pictures != null && pictures.size() > 0);
				try {
					synchronized (monitor) {
						monitor.wait();
					}
				} catch (InterruptedException e) {
					logger.log(e);
				}
			}
		}

		private synchronized void loadPictures(String url) {
			String[] parts = url.split("\\|");
			String[] lookupKeys = new String[parts.length - 1];
			String[] sizeParts = parts[0].split("-");
			int size = 64;
			if (sizeParts.length > 1) {
				try {
					size = getNextHighestPO2(Integer.parseInt(sizeParts[1]));
				} catch (Exception e) {
					size = 64;
				}
			}
			for (int i = 1; i < parts.length; i++) {
				lookupKeys[i - 1] = parts[i];
			}
			//check for halfsized textures, scale if needed
			boolean halfsizedTextures = tealeaf.getSettings().getBoolean("@__use_halfsized_textures__", false);
			int scale = 1;
			if (halfsizedTextures) {
				scale = 2;
			}
			int sheetSize = 1024;
			if (halfsizedTextures && size > 64) {
				size /= scale;
				sheetSize /= scale;
			}
			int rows = sheetSize / size;
			int cols = sheetSize / size;
			int width = rows * size;
			int height = cols * size;
			Bitmap bitmap = null;
			try {
				bitmap = getBitmap(width, height);
			} catch (OutOfMemoryError e) {
				logger.log(e);
				failedCount++;
				try {
					Thread.sleep(1000 * failedCount);
				} catch (InterruptedException e1) {
					logger.log(e1);
				}
				addPicturesToLoad(url);
				return;
			}
			bitmap.eraseColor(Color.TRANSPARENT);
			Canvas c = new Canvas(bitmap);

			for (int i = 0; i < cols; i++) {
				for (int j = 0; j < rows; j++) {
					int index = i * rows + j;
					if (index < lookupKeys.length) {
						// we haven't reached the end of the list
						String lookupKey = lookupKeys[index];
						Bitmap bmp = contactList.getUnscaledPicture(lookupKey);
						if (bmp != null) {
							int originalWidth = bmp.getWidth();
							int originalHeight = bmp.getHeight();
							int scaledSize = size;
							if (originalWidth < scaledSize) {
								scaledSize = originalWidth;
							}
							if (originalHeight < scaledSize) {
								scaledSize = originalHeight;
							}
							int offsetX = (originalWidth - scaledSize) / 2;
							int offsetY = (originalHeight - scaledSize) / 2;
							Rect src = new Rect(offsetX, offsetY, scaledSize + offsetX, scaledSize + offsetY);

							int x = j * size;
							int y = i * size;
							Rect dst = new Rect(x, y, (j + 1) * size, (i + 1)
									* size);
							c.drawBitmap(bmp, src, dst, null);
							bmp.recycle();
							bmp = null;
						}
					} else {
						break;
					}
				}
			}
			TextureData td = new TextureData(url, -1, width, height, width,
					height, bitmap, true);
			tealeaf.glView.pushLoadedImage(td);
		}
	}

	private Bitmap getBitmap(int width, int height) {
		return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
	}

	private void load(String url) {
		logger.log("{texture} Loading", url);
		long then = System.currentTimeMillis();
		Bitmap bmp = null;
		if (url.startsWith("@TEXT")) {
			try {
				bmp = textManager.getText(url);
			} catch (OutOfMemoryError e) {
				logger.log(e);
				loadTexture(url);
				return;
			}
		} else if (url.startsWith("@CONTACTPICTURE")) {
			String[] parts = url.split("\\|");
			if (parts.length < 2) {
				loadingError(url);
				return;
			}
			String[] sizeParts = parts[0].split("-");
			int size = 64;
			if (sizeParts.length > 1) {
				try {
					size = Integer.parseInt(sizeParts[1]);
				} catch (NumberFormatException e) {
					logger.log(e);
					size = 64;
				}
			}
			try {
				bmp = contactList.getPicture(parts[1], size);
			} catch (Exception e) {
				loadingError(url);
				logger.log(e);
			}
		} else if (url.startsWith("@MULTICONTACTPICTURES")) {
			loadContactPictures(url);
			return;
		} else if (url.startsWith("@CAMERA")) {
			bmp = loadCameraPicture(url.substring(7));
			if (bmp == null) {
				// we need to wait for the picture to be taken
				return;
			}
		} else if (url.startsWith("@GALLERYPHOTO")) {
			bmp = loadGalleryPicture(url.substring(13));
			if (bmp == null) {
				return;
			}
		} else {
			bmp = getImage(url);
		}
		if (bmp == null) {
			loadingError(url);
		} else {
			loadTexture(url, bmp);
		}
		logger.log("{texture} Loading took", System.currentTimeMillis() - then, "ms");
	}

	public void loadTexture(String url, Bitmap bmp) {
		TextureData td = getTextureData(url, bmp);
		if (td == null) {
			return;
		}

		// If in-flight queue is not being cleared,
		synchronized (monitor) {
			if (!isClearing) {
				tealeaf.glView.pushLoadedImage(td);
			} else {
				logger.log("{texture} WARNING: Aborting pushing loaded image during clearing");
			}
		}
	}

	public TextureData getTextureData(String url, Bitmap bmp) {
		int originalWidth = bmp.getWidth(), originalHeight = bmp.getHeight(), width = getNextHighestPO2(originalWidth), height = getNextHighestPO2(originalHeight);
		if (width > 1024 || height > 1024) {
			EventQueue.pushEvent(new LogEvent("The image " + url + " has dimensions larger than 1024x1024, which won't work"));
		}

		boolean halfsizedTextures = width > 64 && height > 64 &&
				tealeaf.getSettings().getBoolean("@__use_halfsized_textures__", false);
		
		float ratio = 1.f;
		if (halfsizedTextures) {
			ratio = 2.f;
		}

		Bitmap bitmap = null;
		if (width != originalWidth || height != originalHeight || halfsizedTextures) {
			int scaledWidth = (int)(width / ratio);
			int scaledHeight = (int)(height / ratio);
			try {
				bitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888);
			} catch (OutOfMemoryError e) {
				logger.log(e);
				return null;
			} catch(Exception e) {
				logger.log(e);
				return null;
			}
			try {
				if(bitmap != null) {
					Canvas c = new Canvas(bitmap);
					c.scale(1.f / ratio, 1.f / ratio);
					c.drawBitmap(bmp, 0, 0, null);
					bmp.recycle();
					bmp = null;
				} else {
					// put the texture back on the queue--something went wrong
					loadTexture(url);
				}
			} catch(NullPointerException e) {
				// not sure what happened, but try to load it again anyway
				logger.log("{texture} WARNING: Failed to get texture data on", url, ", trying again");
				loadTexture(url);
			}
		}
		if (bmp != null) {
			bitmap = bmp;
		}
		return new TextureData(url, -1, width, height, originalWidth, originalHeight, bitmap, true);
	}

	public void finishLoadingTexture(TextureData td) {
		if (td == null || td.url == null || td.bitmap == null) {
			logger.log("{texture} WARNING: Loading texture failure", td,
					(td == null ? "" : td.url + " " + td.bitmap));
			return;
		}


		// Generate one texture pointer
		int[] textureIds = new int[1];
		GLES20.glGenTextures(1, textureIds, 0);

		// bind this texture
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[0]);

		// Create Nearest Filtered Texture
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
		GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

		// Use the Android GLUtils to specify a two-dimensional texture image
		// from our bitmap
		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, td.bitmap, 0);
		logger.debug("{texture} Done loading", td.url, "(", td.bitmap.getWidth(), ",", td.bitmap.getHeight(), ")");
		td.bitmap.recycle();
		td.bitmap = null;
		td.name = textureIds[0];

	}

	private static InputStream byteInputStream(final MappedByteBuffer mbb) {
		return new InputStream() {
			public int read() {
				if (!mbb.hasRemaining())
					return -1;
				return mbb.get();
			}

			public int read(byte[] bytes, int off, int len) throws IOException {
				len = Math.min(len, mbb.remaining());
				mbb.get(bytes, off, len);
				return len;
			}
		};
	}

	public Bitmap getImage(String addr) {
		addr = resourceManager.resolve(addr);
		if (addr.startsWith("data:image")) {
			return getImageFromBase64(addr);
		}

		URL url = null;
		InputStream is = null;
		Bitmap bmp = null;
		try {
			if (addr.startsWith("//")) {
				addr = "http:" + addr;
			} else if (addr.startsWith("/")) {
				addr = "file://" + addr;
			}
			url = new URL(addr);
			is = url.openStream();
			bmp = BitmapFactory.decodeStream(is);
			is.close();
			return bmp;
		} catch (OutOfMemoryError e) {
			logger.log("{texture} WARNING: Out of memory loading", addr);
			if (bmp != null) {
				bmp.recycle();
			}
			if (is != null) {
				try {
					is.close();
				} catch (Exception e2) {
					logger.log(e2);
				}
			}
			try {
				Thread.sleep(1000 * (failedCount++));
			} catch (InterruptedException ex) {
				logger.log(ex);
			} finally {
				loadTexture(addr);
			}
		} catch (FileNotFoundException e) {

			// don't bother logging 404s for images
			if (url != null && !url.getProtocol().equals("http")) {
				logger.log(e);
			}

		} catch (IOException e) {
			logger.log(e);
		} catch (Exception e) {
			logger.log(e);
		}
		return null;
	}

	private Bitmap getImageFromBase64(String data) {
		String[] parts = data.split(",");
		if (parts.length < 2) {
			logger.log("{texture} ERROR: Bad base64 format");
			return null;
		}
		String type = parts[0];
		byte[] imageData = Base64.decode(parts[1], Base64.DEFAULT);
		Bitmap bmp = BitmapFactory.decodeByteArray(imageData, 0,
				imageData.length);

		return bmp;
	}

	public static int getNextHighestPO2(int n) {
		n -= 1;
		n = n | (n >> 1);
		n = n | (n >> 2);
		n = n | (n >> 4);
		n = n | (n >> 8);
		n = n | (n >> 16);
		n = n | (n >> 32);
		return n + 1;
	}

}
