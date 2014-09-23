package com.example.customsearch.app.internet;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import com.example.customsearch.app.MainActivity;
import com.example.customsearch.app.R;
import com.example.customsearch.app.database.DatabaseHandler;
import com.example.customsearch.app.entitys.MyImage;
import com.example.customsearch.app.fragments.ImageListFragment;
import com.google.gson.Gson;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CSETask extends AsyncTask<String, Void, SQLiteDatabase> {

	private Context context;
	private DatabaseHandler databaseHandler;
	private int count;

	private SQLiteDatabase db;

	private Set<MyImage> myImages = new HashSet<>(10);

	private static ProgressBar progressBar;

	private CallbackGetImages callbackGetImages;

	public CSETask(Context context, DatabaseHandler databaseHandler, int count, CallbackGetImages callbackGetImages) {
		this.context = context;
		this.databaseHandler = databaseHandler;
		this.count = count;
		this.callbackGetImages = callbackGetImages;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	@Override
	protected void onPreExecute() {
		db = databaseHandler.getWritableDatabase();
		prgBarEnable((MainActivity) context);
		super.onPreExecute();
	}

	@Override
	protected SQLiteDatabase doInBackground(String... params) {
		String request = makeSearchString(params[0], count);
		InputStream inputStream;
		HttpClient httpClient = new DefaultHttpClient();
		HttpGet httpPost = new HttpGet(request);
		try {
			HttpResponse httpResponse = httpClient.execute(httpPost);
			inputStream = httpResponse.getEntity().getContent();
			if (inputStream != null) {
				String result = convertInputStreamToString(inputStream);
				jsonParse(result);
				fillDatabase();
			} else {
				return db;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return db;
	}

	@Override
	protected void onPostExecute(SQLiteDatabase database) {
		prgBarDisable();
		callbackGetImages.onTaskCompleted(database);
		super.onPostExecute(database);
	}

	private String makeSearchString(String qSearch, int start) {
		StringBuilder toSearch = new StringBuilder(context.getString(R.string.searchURL) +
				"key=" + context.getString(R.string.apiKey) + "&cx=" +
				context.getString(R.string.cseKey) + "&q=");

		//replace spaces in the search query with +
		String keys[] = qSearch.split("[ ]+");
		for (String key : keys) {
			toSearch.append(key).append("+"); //append the keywords to the url
		}

		//specify response format as json
		toSearch.append("&alt=json");

		//specify starting result number
		toSearch.append("&start=").append(start);

		//image search
		toSearch.append("&searchType=").append("image");

		//specify the number of results you need from the starting position
		toSearch.append("&num=").append(10);
		return toSearch.toString();
	}

	private String convertInputStreamToString(InputStream stream) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		StringBuilder sb = new StringBuilder();

		String line;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	private void jsonParse(String in) {
		try {
			JSONObject jsonObject = new JSONObject(in);
			String jsonString = jsonObject.optString("items");
			Gson gson = new Gson();
			MyImage[] arr = gson.fromJson(jsonString, MyImage[].class);
			if (arr != null) {
				Set<MyImage> tmp = new HashSet<>(Arrays.asList(arr));
				myImages.addAll(tmp);
				ImageListFragment.endOfImagesOnServer = false;
			} else {
				ImageListFragment.endOfImagesOnServer = true;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void fillDatabase() throws IOException {
		ContentValues cv = new ContentValues(myImages.size());
		long all = 0;
		for (MyImage myImage : myImages) {
			byte[] img = fetchImage(myImage.getThumbnail().getThumbnailLink());
			if (img != null) {
				cv.put(context.getString(R.string.img), img);
				cv.put(context.getString(R.string.name), myImage.getSnippet());
				cv.put(context.getString(R.string.link), myImage.getLink());
				all = db.insert(context.getString(R.string.table), null, cv);
				cv.clear();
			}
		}
		Log.d("DB", "Success insert to database: " + String.valueOf(all) + " elements");
	}

	private byte[] fetchImage(final String s) throws IOException {
		URL url = new URL(s);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			InputStream in = connection.getInputStream();
			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				return null;
			} else {
				int bytesRead;
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
			connection.disconnect();
		}
		return null;
	}

	private void prgBarEnable(Activity activity) {
		// create new ProgressBar and style it
		progressBar = new ProgressBar(activity, null, android.R.attr.progressBarStyleHorizontal);
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
		progressBar.setIndeterminate(false);
		progressBar.setVisibility(View.INVISIBLE);
		progressBar = null;
	}
}
