package com.tealeaf;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.view.Display;
import android.view.Window;
import android.widget.AbsoluteLayout;
import android.os.Bundle;

import com.tealeaf.logger;
import com.tealeaf.TeaLeaf;
import com.tealeaf.TeaLeafOptions;
import com.tealeaf.NativeShim;

import android.opengl.GLSurfaceView;
import android.opengl.GLES20;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.egl.EGLConfig;

import android.view.MotionEvent;

public class TeaLeafActivity extends Activity
{
	NativeShim shim;

	static class InputEvents {
		public static final int MAX_EVENTS = 32;
		public static int[] ids = new int[MAX_EVENTS];
		public static int[] types = new int[MAX_EVENTS];
		public static int[] xs = new int[MAX_EVENTS];
		public static int[] ys = new int[MAX_EVENTS];

		private static int index = 0;

		public static Object lock = new Object();

		public static void push(int id, int type, int x, int y) {
			if (index < MAX_EVENTS) {
				ids[index] = id;
				types[index] = type;
				xs[index] = x;
				ys[index] = y;
				index++;
			}
		}

		public static void clear() {
			index = 0;
		}
	}

	private void handleInputEvents() {
		synchronized (InputEvents.lock) {
			if (InputEvents.index > 0 && shim != null) {
				shim.dispatchInputEvents(InputEvents.ids,
						InputEvents.types, InputEvents.xs, InputEvents.ys,
						InputEvents.index);
				InputEvents.clear();
			}
		}
	}

	class MyGLSurfaceView extends GLSurfaceView {

		int width;
		int height;

		public MyGLSurfaceView(Context context){
			super(context);

			setEGLContextClientVersion(2);
			// Set the Renderer for drawing on the GLSurfaceView
			setRenderer(new MyGLRenderer());
		}

		@Override
		public boolean onTouchEvent(MotionEvent event) {
			int activePointer = event.getActionIndex();
			int id = event.getPointerId(activePointer);
			float x = Math.min(Math.max(0, event.getX(activePointer)), width);
			float y = Math.min(Math.max(0, event.getY(activePointer)), height);
			int type;
			int eventType = event.getAction();

			switch (eventType & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_POINTER_1_DOWN:
					type = 1;
					break;
				case MotionEvent.ACTION_MOVE:
					type = 2;
					break;
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_POINTER_1_UP:
				case MotionEvent.ACTION_CANCEL:
					type = 3;
					break;
				default:
					type = -1;
			}

			synchronized (InputEvents.lock) {
				InputEvents.push(id, type, (int) x, (int) y);
			}

			for (int i = 0; i < event.getPointerCount(); i++) {
				if (i == activePointer) {
					continue;
				}
				id = event.getPointerId(i);
				x = Math.min(Math.max(0, event.getX(i)), width);
				y = Math.min(Math.max(0, event.getY(i)), height);
				type = 2;
				synchronized (InputEvents.lock) {
					InputEvents.push(id, type, (int) x, (int) y);
				}
			}
			return true;
		}

		public class MyGLRenderer implements GLSurfaceView.Renderer {

			long lastMS = 0;
			public void onSurfaceCreated(GL10 unused, EGLConfig config) {
			}

			public void onDrawFrame(GL10 unused) {
				// Redraw background color
				if (shim != null) {
					handleInputEvents();
					long now = System.currentTimeMillis();
					shim.step((int) (now - lastMS));
					lastMS = now;
				}
			}

			public void onSurfaceChanged(GL10 unused, int width, int height) {
				MyGLSurfaceView.this.width = width;
				MyGLSurfaceView.this.height = height;
				shim = TeaLeaf.init((Activity)TeaLeafActivity.this);
				TeaLeafOptions opts = new TeaLeafOptions(TeaLeafActivity.this);

				String sourceDir = opts.getSourceDir();
				shim.coreInit(
						"http://localhost/",
						"http://localhost/",
						9200,
						9200,
						"devkit.native.launchClient",
						sourceDir,
						width,
						height,
						false,
						null, // Disable OpenGL splash
						"");
				lastMS = System.currentTimeMillis();
			}
		}
	}


	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(new MyGLSurfaceView(this));
	}

	static {
		System.load("libtealeaf.so");
	}
}
