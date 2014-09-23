package com.example.customsearch.app.fragments;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.example.customsearch.app.R;
import com.example.customsearch.app.database.DatabaseHandler;
import com.example.customsearch.app.database.SQLiteCursorLoader;

import java.util.ArrayList;

public class FavoritesImagesFragment extends SherlockListFragment implements AbsListView.OnScrollListener, LoaderManager.LoaderCallbacks<Cursor> {

	public static DatabaseHandler databaseHandler;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		databaseHandler = new DatabaseHandler(getSherlockActivity(), "favDB", null, 1);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		getLoaderManager().initLoader(0, null, this);
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();
		getListView().setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
		getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				getSherlockActivity().startActionMode(new MyActionModeCallback(getSherlockActivity(), databaseHandler, getListView()));
				CheckBox box = (CheckBox) view.findViewById(R.id.check);
				box.toggle();
				Log.i("ListItem", String.valueOf(position));
			}
		});
	}

	public void refresh() {
		getLoaderManager().restartLoader(0, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
		return new MyListCursorLoader(getSherlockActivity());

	}

	@Override
	public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
		MyCursorAdapter cursorAdapter = new MyCursorAdapter(getSherlockActivity(), (DatabaseHandler.MyCursor) cursor, 1);
		setListAdapter(cursorAdapter);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		setListAdapter(null);
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {

	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

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

	public class MyActionModeCallback implements ActionMode.Callback {

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
			inflater.inflate(R.menu.favorites_list_context, menu);
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
				case R.id.removeFromFavorites:
					SparseBooleanArray sbArray = listView.getCheckedItemPositions();

					ArrayList<Integer> list = new ArrayList<>();
					for (int i = 0; i < sbArray.size(); i++) {
						int key = sbArray.keyAt(i);
						if (sbArray.get(key)) {
							list.add(key);
						}
					}
					if (list.size() > 0) {
						removeFromFavorites(list);
					}
					return true;
				default:
					mode.finish();
					return false;
			}
		}

		private void removeFromFavorites(final ArrayList<Integer> list) {
			SQLiteDatabase favorites = databaseHandler.getWritableDatabase();
			Cursor cursor = favorites.query(context.getString(R.string.table), null, null, null, null, null, null);
			Log.e("LIST", list.toString());
			for (int i = list.size() - 1; i >= 0; i--) {
				cursor.moveToPosition(list.get(i));
				int link = cursor.getColumnIndex(getString(R.string.link));
				favorites.delete(context.getString(R.string.table), "link = ?", new String[]{cursor.getString(link)});
			}
			getLoaderManager().restartLoader(0, null, FavoritesImagesFragment.this);
			databaseHandler.close();
		}
	}
}
