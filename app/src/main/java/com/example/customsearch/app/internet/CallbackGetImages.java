package com.example.customsearch.app.internet;

import android.database.sqlite.SQLiteDatabase;

public interface CallbackGetImages {
	void onTaskCompleted(SQLiteDatabase database);
}
