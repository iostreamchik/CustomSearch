package com.example.customsearch.app.internet;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import java.io.*;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Vector;

public class ImageLoader {

	public ImageLoader(CallbackGetBigImage callbackGetBigImage) {
		this.callbackGetBigImage = callbackGetBigImage;
	}

	private CallbackGetBigImage callbackGetBigImage;

	private Vector<ImageView> downloaded = new Vector<ImageView>();
	public boolean findObject(ImageView object) {
		for (int i = 0; i < downloaded.size(); i++) {
			if (downloaded.elementAt(i).equals(object)) {
				return true;
			}
		}
		return false;
	}
	public String md5(String s) {
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();
			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++) {
				hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
			}
			return hexString.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}
	public static void fileSave(InputStream is, FileOutputStream outputStream) {
		int i;
		try {
			while ((i = is.read()) != -1) {
				outputStream.write(i);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private Bitmap downloadImage(Context context, int cacheTime, String iUrl, ImageView iView) {
		Bitmap bitmap = null;
		if (cacheTime != 0) {
			File file = new File(context.getExternalCacheDir(), md5(iUrl)
					+ ".cache");
			long time = new Date().getTime() / 1000;
			long timeLastModified = file.lastModified() / 1000;
			try {
				if (file.exists()) {
					if (timeLastModified + cacheTime < time) {
						file.delete();
						file.createNewFile();
						fileSave(new URL(iUrl).openStream(),
								new FileOutputStream(file));
					}
				} else {
					file.createNewFile();
					fileSave(new URL(iUrl).openStream(), new FileOutputStream(
							file));
				}
				bitmap = BitmapFactory.decodeStream(new FileInputStream(file));
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (bitmap == null) {
				file.delete();
			}
		} else {
			try {
				bitmap = BitmapFactory.decodeStream(new URL(iUrl).openStream());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (iView != null) {
			downloaded.remove(iView);
		}
		return bitmap;
	}
	public void fetchImage(final Context context, final int cacheTime, final String url, final ImageView iView) {
		if (iView != null) {
			if (findObject(iView)) {
				return;
			}
			downloaded.add(iView);
		}
		new AsyncTask<String, Void, Bitmap>() {
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
			}

			protected Bitmap doInBackground(String... iUrl) {
				return downloadImage(context, cacheTime, iUrl[0], iView);
			}
			protected void onPostExecute(Bitmap result) {
				super.onPostExecute(result);
				if (iView != null) {
					iView.setImageBitmap(result);
				}
				callbackGetBigImage.onImageLoad(result);
			}
		}.execute(url);
	}
}
