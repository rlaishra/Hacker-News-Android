package com.rickylaishram.hackernews.db;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class LinksDataSource {
	
	private SQLiteDatabase db;
	private DBHelper dbHelper;
	private String[] allColumns = { dbHelper.COLUMN_ID, dbHelper.LINKS };
	
	public LinksDataSource(Context context) {
		dbHelper	= new DBHelper(context);
	}
	
	public void open() throws SQLException {
		db = dbHelper.getWritableDatabase();
	}
	
	public void close() {
		dbHelper.close();
	}
	
	public void addLink(String link) {
		ContentValues values	= new ContentValues();
		values.put(DBHelper.LINKS, link);
		long insertId	= db.insert(DBHelper.TABLE_LINKS, null, values);
	}
	
	public Boolean isLinkSeen(String link) {
		Cursor cursor = db.query(DBHelper.TABLE_LINKS, allColumns, DBHelper.LINKS + " = '" + 
				link + "'", null, null, null, null, null);
		cursor.moveToFirst();
		
		Boolean result = false;
		
		if(cursor.getCount() > 0) {
			result = true;
		}
		
		return result;
	}
}
