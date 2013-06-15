/* @license
 * This file is part of the Game Closure SDK.
 *
 * The Game Closure SDK is free software: you can redistribute it and/or modify
 * it under the terms of the Mozilla Public License v. 2.0 as published by Mozilla.

 * The Game Closure SDK is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Mozilla Public License v. 2.0 for more details.

 * You should have received a copy of the Mozilla Public License v. 2.0
 * along with the Game Closure SDK.  If not, see <http://mozilla.org/MPL/2.0/>.
 */
package com.tealeaf;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.*;
import android.provider.ContactsContract.*;
import android.util.Base64;

public class ContactList {
	private Activity activity;
	private ResourceManager resourceManager;

	public ContactList(Activity activity, ResourceManager resourceManager) {
		this.activity = activity;
		this.resourceManager = resourceManager;
	}

	public String getProfileLookup() {
		Account[] accounts = AccountManager.get(activity).getAccounts();
		if(accounts.length > 0) {
			String syncName = accounts[0].name;
			Cursor c = activity.getContentResolver().query(Email.CONTENT_URI,
					new String[] { Contacts.LOOKUP_KEY },
					Email.DATA + " = ?",
					new String[] { syncName }, null);
			String id = "";
			if (c.moveToFirst()) {
				id = c.getString(c.getColumnIndex(Contacts.LOOKUP_KEY));
			}
			c.close();
			return id;
		}
		return null;
	}

	public Bitmap getPicture(String lookupKey, int size) {
		String encoded = "unknown";
		try {
			encoded = URLEncoder.encode(lookupKey, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.log(e);
		}
		File f = new File(resourceManager.getCacheDirectory() + "/contacts/" + encoded + "x" + size + ".png");
		if (f.exists()) {
			return BitmapFactory.decodeFile(f.getAbsolutePath());
		}
		Bitmap img = getUnscaledPicture(lookupKey);
		if (img != null) {
			Bitmap result = null;

			try {
				result = Bitmap.createBitmap(size, size,
						Bitmap.Config.ARGB_8888);
			} catch (OutOfMemoryError e) {
				logger.log(e);
				return null;
			}
			Canvas c = new Canvas(result);
			int originalWidth = img.getWidth();
			int originalHeight = img.getHeight();
			Rect src = new Rect(0, 0, originalWidth, originalHeight);
			float scale = Math.min((float)size / originalWidth, (float)size / originalHeight);
			int scaledWidth = (int)(scale * originalWidth);
			int scaledHeight = (int)(scale * originalHeight);
			Rect dst = new Rect(0, 0, scaledWidth, scaledHeight);
			c.drawBitmap(img, src, dst, null);

			try {
				File parent = f.getParentFile();
				if (!parent.exists()) {
					parent.mkdirs();
				}
				f.createNewFile();
				result.compress(CompressFormat.PNG, 0, new FileOutputStream(f));
			} catch (Exception e) {
				logger.log(e);
				f.delete();
			}

			return result;
		}
		return null;
	}

	public Bitmap getUnscaledPicture(String lookupKey) {
		Uri lookupUri = Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI, lookupKey);
		Uri contact = Contacts.lookupContact(activity.getContentResolver(), lookupUri);
		Bitmap picture = null;
		if (contact != null) {
			InputStream photo = null;
			try {
				photo = Contacts.openContactPhotoInputStream(activity.getContentResolver(), contact);
			} catch (Exception e) {
				logger.log(e);
				picture = null;
			}
			if (photo != null) {
				picture = BitmapFactory.decodeStream(photo);
			}
		}
		//on 4.0 and above we can't do this sequel query, just skip it
		if (picture == null && android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			picture = loadFacebookAvatar(lookupKey);
		}
		return picture;
	}

	public String getProfilePicture(boolean base64) {
		return getPicture(getProfileLookup(), true);
	}

	public String getPicture(String lookupKey, boolean base64) {
		Bitmap img = getPicture(lookupKey, 128);
		if (img != null) {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			img.compress(CompressFormat.PNG, 0, stream);
			return Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT);
		}
		return "";
	}

	private Bitmap loadFacebookAvatar(String lookupKey) {
		String[] rawProjection = { ContactsContract.RawContacts._ID };
		String contactIdAssertion = ContactsContract.Contacts.LOOKUP_KEY + " = '" + lookupKey + "'";
		String rawWhere = new StringBuilder().append(contactIdAssertion)
				.append(") UNION ALL SELECT ")
				.append(ContactsContract.RawContacts._ID)
				.append(" FROM view_raw_contacts WHERE (")
				.append(contactIdAssertion).toString();

		Cursor query = null;
		try {
			query = activity.getContentResolver().query(ContactsContract.RawContacts.CONTENT_URI, rawProjection, rawWhere, null, null);
		} catch(Exception e) {
			// guess we can't get the facebook photo. :(
			if (query != null) {
				query.close();
				query = null;
			}
		}
		if (query != null && query.moveToFirst()) {
			do {
				long id = query.getLong(query.getColumnIndex(ContactsContract.RawContacts._ID));
				String[] projection = { ContactsContract.CommonDataKinds.Photo.PHOTO };
				Uri uri = ContactsContract.Data.CONTENT_URI;

				String mimeTypeAssertion = ContactsContract.Data.MIMETYPE + "='" + ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE + "'";
				String photoAssertion = ContactsContract.CommonDataKinds.Photo.PHOTO + " IS NOT NULL";
				String rawContactIdAssertion = ContactsContract.CommonDataKinds.Photo.RAW_CONTACT_ID + " = " + id;

				String where = new StringBuilder().append(mimeTypeAssertion)
						.append(" AND ").append(photoAssertion).append(" AND ")
						.append(rawContactIdAssertion)
						.append(") UNION ALL SELECT ")
						.append(ContactsContract.CommonDataKinds.Photo.PHOTO)
						.append(" FROM view_data WHERE (")
						.append(photoAssertion).append(" AND ")
						.append(rawContactIdAssertion).toString();

				Cursor photoQuery = activity.getContentResolver().query(uri, projection, where, null, null);
				if (photoQuery != null) {
					if (photoQuery.moveToFirst()) {
						do {
							byte[] photoData = photoQuery.getBlob(photoQuery.getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO));
							if (photoData != null) {
								query.close();
								photoQuery.close();
								return BitmapFactory.decodeByteArray(photoData, 0, photoData.length, null);
							}
						} while (photoQuery.moveToNext());

					}
					photoQuery.close();
				}
			} while (query.moveToNext());
			query.close();
		}
		return null;
	}
}
