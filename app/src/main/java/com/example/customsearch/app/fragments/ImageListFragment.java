package com.example.customsearch.app.fragments;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.example.customsearch.app.MainActivity;
import com.example.customsearch.app.R;
import com.example.customsearch.app.database.DatabaseHandler;
import com.example.customsearch.app.database.SQLiteCursorLoader;
import com.example.customsearch.app.internet.CSETask;
import com.example.customsearch.app.internet.CallbackGetImages;

import java.util.ArrayList;

public class ImageListFragment extends SherlockListFragment implements CallbackGetImages, AbsListView.OnScrollListener, LoaderManager.LoaderCallbacks<Cursor> {

	private int count = 1;
	private CSETask cseTask;

	public static DatabaseHandler databaseHandler;
	private String query;
	public static boolean endOfImagesOnServer;



	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		databaseHandler = new DatabaseHandler(getSherlockActivity(), "mainDB", null, 1);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		getLoaderManager().initLoader(0, null, this);
		View view = inflater.inflate(R.layout.image_list_layout, container, false);
		ImageView imageView = (ImageView) view.findViewById(R.id.thumbnail);
		imageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d("FRAGMENT on Image click", String.valueOf(getListView().getId()));
			}
		});
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();
		getListView().setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
		getListView().setOnScrollListener(this);
		getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				getSherlockActivity().startActionMode(new MyActionModeCallback(getSherlockActivity(), databaseHandler, getListView()));
				CheckBox box = (CheckBox) view.findViewById(R.id.check);
				box.toggle();
			}
		});
	}

	@Override
	public void onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu, com.actionbarsherlock.view.MenuInflater inflater) {
		inflater.inflate(R.menu.search, menu);

		SearchManager searchManager = (SearchManager) getSherlockActivity().getSystemService(Context.SEARCH_SERVICE);
		final SearchView searchView = (SearchView) menu.findItem(R.id.find).getActionView();
		if (null != searchView) {
			searchView.setSearchableInfo(searchManager
					.getSearchableInfo(getSherlockActivity().getComponentName()));
			searchView.setIconifiedByDefault(false);
		}

		SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
			public boolean onQueryTextChange(String newText) {
				return true;
			}

			@Override
			public boolean onQueryTextSubmit(String s) {
				if (networkState()) {
					assert searchView != null;
					searchView.clearFocus();
					query = s;
					dropImageTable();
					searchImages(s);
				} else {
					Toast.makeText(getSherlockActivity(), "Lost network connection!", Toast.LENGTH_SHORT).show();
				}
				return false;
			}
		};
		assert searchView != null;
		searchView.setOnQueryTextListener(queryTextListener);

		super.onCreateOptionsMenu(menu, inflater);
	}


	/**
	 * Create database and drop table images if last exist
	 * @param query search query from search view
	 */
	private void searchImages(String query) {
		if (cseTask == null) {
			cseTask = new CSETask(getSherlockActivity(), databaseHandler, count, this);
			Log.i("FRAGMENT", "Create task: " + cseTask.hashCode());
			cseTask.execute(query);
		} else {
			Log.i("FRAGMENT", "Set context to task " + cseTask.hashCode());
			if (cseTask.getStatus() != AsyncTask.Status.RUNNING) {
				cseTask = new CSETask(getSherlockActivity(), databaseHandler, count, this);
				cseTask.setContext(getSherlockActivity());
				cseTask.execute(query);
			}
		}
	}

	private boolean networkState() {
		ConnectivityManager connMgr = (ConnectivityManager) getSherlockActivity().getSystemService(Activity.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		return networkInfo != null && networkInfo.isConnected();
	}

	@Override
	public void onTaskCompleted(SQLiteDatabase database) {
		getLoaderManager().restartLoader(0, null, this);
		count += 10;
	}

	private void dropImageTable() {
		SQLiteDatabase database = databaseHandler.getWritableDatabase();
		database.execSQL("DROP TABLE IF EXISTS " + getString(R.string.table));
		databaseHandler.onCreate(database);
	}

	/**
	 * Just for self-control
	 *
	 * @param db
	 */
	private void readDatabase(SQLiteDatabase db) {
		Cursor c = db.query(getString(R.string.table), null, null, null, null, null, null);
		if (c.moveToFirst()) {
			int blob = c.getColumnIndex(getString(R.string.img));
			int name = c.getColumnIndex(getString(R.string.name));
			int link = c.getColumnIndex(getString(R.string.link));
			do {
				Log.d("Database item", c.getString(name));
			} while (c.moveToNext());
		}
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		final int lastItem = firstVisibleItem + visibleItemCount;
		if (networkState() &&
				lastItem == totalItemCount &&
				!endOfImagesOnServer) {
			if (query != null) {
				Log.e("searchImages", String.valueOf(count));
				searchImages(query);
			}
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
		return new MyListCursorLoader(getSherlockActivity());
	}

	@Override
	public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
		MyCursorAdapter cursorAdapter = new MyCursorAdapter(getSherlockActivity(), (DatabaseHandler.MyCursor) cursor, 0);
		setListAdapter(cursorAdapter);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> cursorLoader) {
		setListAdapter(null);
	}

	private static class MyListCursorLoader extends SQLiteCursorLoader {

		public MyListCursorLoader(Context context) {
			super(context);
		}

		@Override
		protected Cursor loadCursor() {
			return databaseHandler.itemsRequest();
		}

	}

	private class MyActionModeCallback implements ActionMode.Callback {

		private Context context;
		private DatabaseHandler databaseHandler;
		private ListView listView;

		public MyActionModeCallback(Context context, DatabaseHandler databaseHandler, ListView listView) {
			this.context = context;
			this.databaseHandler = databaseHandler;
			this.listView = listView;
		}

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.image_list_context, menu);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
			return false;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch (item.getItemId()) {
				case R.id.addToFavorites:
					SparseBooleanArray sbArray = listView.getCheckedItemPositions();
					ArrayList<Integer> list = new ArrayList<>();
					for (int i = 0; i < sbArray.size(); i++) {
						int key = sbArray.keyAt(i);
						if (sbArray.get(key)) {
							list.add(key);
						}
					}
					if (list.size() > 0) {
						addToFavorites(list);
					}
					return true;
				default:
					mode.finish();
					return false;
			}
		}

		private void addToFavorites(final ArrayList<Integer> list) {
			SQLiteDatabase database = databaseHandler.getReadableDatabase();
			SQLiteDatabase favorites = FavoritesImagesFragment.databaseHandler.getWritableDatabase();
			Cursor cursor = database.query(context.getString(R.string.table), null, null, null, null, null, null);
			if (cursor.moveToFirst()) {
				do {
					int id = cursor.getColumnIndex(context.getString(R.string.id));
					int name = cursor.getColumnIndex(context.getString(R.string.name));
					Log.d("db", String.valueOf(cursor.getInt(id)) + ": " + cursor.getString(name));
				} while (cursor.moveToNext());
			}

			ContentValues cv = new ContentValues(cursor.getColumnCount());
			for (Integer integer : list) {
				if (cursor.moveToPosition(integer)) {
					int blob = cursor.getColumnIndex(context.getString(R.string.img));
					int name = cursor.getColumnIndex(context.getString(R.string.name));
					int link = cursor.getColumnIndex(context.getString(R.string.link));
					cv.put(context.getString(R.string.img), cursor.getBlob(blob));
					cv.put(context.getString(R.string.name), cursor.getString(name));
					cv.put(context.getString(R.string.link), cursor.getString(link));
					favorites.insert(context.getString(R.string.table), null, cv);
					cv.clear();
				}
			}
			FavoritesImagesFragment ref = (FavoritesImagesFragment) ((MainActivity) getSherlockActivity()).getFragmentByPosition(1);
			ref.refresh();
			databaseHandler.close();
		}
	}
}
