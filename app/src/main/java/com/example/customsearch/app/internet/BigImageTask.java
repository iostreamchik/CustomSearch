package com.example.customsearch.app.internet;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import com.example.customsearch.app.ImagePagerActivity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class BigImageTask extends AsyncTask<String, Void, Bitmap> {

	private Context context;
	private CallbackGetBigImage callbackGetBigImage;
	private static ProgressBar progressBar;

	public BigImageTask(Context context, CallbackGetBigImage callbackGetBigImage) {
		this.context = context;
		this.callbackGetBigImage = callbackGetBigImage;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	@Override
	protected void onPreExecute() {
		progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
		prgBarEnable((ImagePagerActivity) context);
		super.onPreExecute();
	}

	@Override
	protected Bitmap doInBackground(String... params) {
		byte[] img;
		try {
			img = fetchImage(params[0]);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		InputStream is = new ByteArrayInputStream(img);
		Bitmap bmp = BitmapFactory.decodeStream(is);
		return bmp;
	}

	private byte[] fetchImage(final String s) throws IOException {
		URL url = new URL(s);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			assert connection != null;
			InputStream in = connection.getInputStream();
			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				return null;
			} else {
				int bytesRead = 0;
				byte[] buffer = new byte[1024];
				while ((bytesRead = in.read(buffer)) > 0) {
					out.write(buffer, 0, bytesRead);
				}
				out.close();
				return out.toByteArray();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			assert connection != null;
			connection.disconnect();
		}
		return null;
	}

	@Override
	protected void onPostExecute(Bitmap img) {
		prgBarDisable();
		callbackGetBigImage.onImageLoad(img);
		super.onPostExecute(img);
	}

	private void prgBarEnable(Activity activity) {
		// create new ProgressBar and style it
		progressBar.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 24));
		progressBar.setIndeterminate(true);

		// retrieve the top view of application
		final FrameLayout decorView = (FrameLayout) activity.getWindow().getDecorView();
		decorView.addView(progressBar);

		// Here we try to position the ProgressBar to the correct position by looking
		// at the position where content area starts. But during creating time, sizes
		// of the components are not set yet, so we have to wait until the components
		// has been laid out
		// Also note that doing progressBar.setY(136) will not work, because of different
		// screen densities and different sizes of actionBar
		ViewTreeObserver observer = progressBar.getViewTreeObserver();
		observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				View contentView = decorView.findViewById(android.R.id.content);
				progressBar.setY(contentView.getY() - 10);
				ViewTreeObserver observer = progressBar.getViewTreeObserver();
				observer.removeGlobalOnLayoutListener(this);
			}
		});
	}

	private void prgBarDisable() {
		if (progressBar != null) {
			progressBar.setIndeterminate(false);
			progressBar.setVisibility(View.INVISIBLE);
			progressBar = null;
		} else {
			progressBar = null;
		}
	}
}
