package com.example.customsearch.app.database;

import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.customsearch.app.R;
import com.example.customsearch.app.entitys.MyImage;

public class DatabaseHandler extends SQLiteOpenHelper {

	private final Context context;

	public DatabaseHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
		super(context, name, factory, version);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("create table images ("
				+ "_id integer primary key autoincrement,"
				+ "img BLOB not null,"
				+ "name text,"
				+ "link text not null" + ");");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	public MyCursor itemsRequest() {
		Cursor wrapped = getWritableDatabase().query(context.getString(R.string.table), null, null, null, null, null, null, null);
		return new MyCursor(wrapped);
	}

	public class MyCursor extends CursorWrapper {

		public MyCursor(Cursor c) {
			super(c);
		}

		public MyImage getImage() {
			if (isBeforeFirst() || isAfterLast())
				return null;
			MyImage image = new MyImage();
			int id = getInt(getColumnIndex(DatabaseHandler.this.context.getString(R.string.id)));
			image.setId(id);
			byte[] BLOB = getBlob(getColumnIndex(DatabaseHandler.this.context.getString(R.string.img)));
			image.setThumbnailBLOB(BLOB);
			String name = getString(getColumnIndex(DatabaseHandler.this.context.getString(R.string.name)));
			image.setSnippet(name);
			String link = getString(getColumnIndex(DatabaseHandler.this.context.getString(R.string.link)));
			image.setLink(link);
			return image;
		}
	}

}
