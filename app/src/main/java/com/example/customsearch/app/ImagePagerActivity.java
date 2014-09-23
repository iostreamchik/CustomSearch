package com.example.customsearch.app;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.example.customsearch.app.animation.DepthPageTransformer;
import com.example.customsearch.app.database.DatabaseHandler;
import com.example.customsearch.app.entitys.MyImage;
import com.example.customsearch.app.fragments.BigImageFragment;

public class ImagePagerActivity extends SherlockFragmentActivity {

	public static final String EXTRA_DB_NAME = "DBName";
	public static final String EXTRA_IMAGE_ID = "ImageID";
	public static DatabaseHandler databaseHandler;
	private int mPage = -1;
	private String title;
	int id;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		String dbName = (String) getIntent().getSerializableExtra(EXTRA_DB_NAME);
		id = (int) getIntent().getSerializableExtra(EXTRA_IMAGE_ID);

		ViewPager mViewPager = new ViewPager(this);
		mViewPager.setId(R.id.viewPager);
		setContentView(mViewPager);

		databaseHandler = new DatabaseHandler(getApplicationContext(), dbName, null, 1);

		FragmentManager fm = getSupportFragmentManager();
		mViewPager.setAdapter(new FragmentPagerAdapter(fm) {

			@Override
			public int getCount() {
				SQLiteDatabase database = databaseHandler.getReadableDatabase();
				Cursor cursor = database.query(getString(R.string.table), null, null, null, null, null, null);
				int count = cursor.getCount();
				database.close();
				return count;
			}

			@Override
			public Fragment getItem(int i) {
				Log.d("getItem", "Cursor item: " + String.valueOf(i + id));
				SQLiteDatabase database = databaseHandler.getReadableDatabase();
				Cursor cursor = database.query(getString(R.string.table), null, "_id = " + String.valueOf(i + id), null, null, null, null);
				String link = "";
				if (cursor.moveToFirst()) {
					int nameColumn = cursor.getColumnIndex(getString(R.string.name));
					int linkColumn = cursor.getColumnIndex(getString(R.string.link));
					link = cursor.getString(linkColumn);
					String name = cursor.getString(nameColumn);
				}
				database.close();
//
//					/**
//					 * I`m really sorry. This try block is monster crutch :(
//					 * But i haven`t time to fix setTitle(), seeing, that this method
//					 * call twice, and set Title from neighboring Fragment
//					 */
//					Log.e("NAME", name);
//					try {
//						if (i + id >= getCount()) {
//							MyImage myImage = getImage(i + id - 1);
//							setTitle(myImage.getSnippet());
//							Log.e("setTitle 1", myImage.getSnippet());
//						} else {
//							MyImage myImage = getImage(i + id - 2);
//							Log.e("setTitle 2", myImage.getSnippet());
//							setTitle(myImage.getSnippet());
//						}
//					} catch (CursorIndexOutOfBoundsException e) {
//						Log.e("EXCEPTION", e.toString());
//					}
				return BigImageFragment.newInstance(link);
			}
		});

		mViewPager.setPageTransformer(true, new DepthPageTransformer());

		mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageSelected(int i) {
				MyImage myImage = getImage(i + id);
				setTitle(myImage.getSnippet());
				mPage = i;
			}

			@Override
			public void onPageScrolled(int i, float v, int i2) {
			}

			@Override
			public void onPageScrollStateChanged(int i) {
			}
		});

	}

	private MyImage getImage(int item) {
		Log.i("getImage", String.valueOf(item));
		MyImage myImage = new MyImage();
		SQLiteDatabase database = databaseHandler.getReadableDatabase();
		Cursor cursor = database.query(getString(R.string.table), null, "_id = " + String.valueOf(item), null, null, null, null);
		if (cursor.moveToFirst()) {
			int linkColumn = cursor.getColumnIndex(getString(R.string.link));
			myImage.setLink(cursor.getString(linkColumn));
			int nameColumn = cursor.getColumnIndex(getString(R.string.name));
			myImage.setSnippet(cursor.getString(nameColumn));
			title = cursor.getString(nameColumn);
		}
		database.close();
		return myImage;
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent();
		intent.putExtra("index", mPage);
		setResult(RESULT_OK, intent);
		super.onBackPressed();
	}
}
