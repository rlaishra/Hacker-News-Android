package com.rickylaishram.hackernews.db;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Vector;

import com.rickylaishram.hackernews.BookmarkItems;
import static com.rickylaishram.util.CommonUitls.getMonthNameShort;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class BookMarksDataSource {
	private SQLiteDatabase db;
	private DBHelper dbHelper;
	private String[] allColumns = { dbHelper.COLUMN_ID, dbHelper.ARTICLE_URL, dbHelper.COMMENT_ID, dbHelper.ADDED_ON,
			dbHelper.NAME, dbHelper.DATE, dbHelper.MONTH, dbHelper.YEAR };
	
	public BookMarksDataSource(Context context) {
		dbHelper	= new DBHelper(context);
	}
	
	public void open() throws SQLException {
		db = dbHelper.getWritableDatabase();
	}
	
	public void close() {
		dbHelper.close();
	}
	
	public void add(String url, String comment, String name) {
		Integer time			= (int) (System.currentTimeMillis());
		Calendar cal 			= Calendar.getInstance();
		
		ContentValues values	= new ContentValues();
		values.put(DBHelper.NAME, name);
		values.put(DBHelper.ARTICLE_URL, url);
		values.put(DBHelper.COMMENT_ID, comment);
		values.put(DBHelper.ADDED_ON, time);
		values.put(DBHelper.DATE, cal.get(Calendar.DATE));
		values.put(DBHelper.MONTH, getMonthNameShort(cal.get(Calendar.MONTH)));
		values.put(DBHelper.YEAR, cal.get(Calendar.YEAR));
		
		long insertId	= db.insert(DBHelper.TABLE_BM, null, values);
	}
	
	public void remove(String comment) {
		db.delete(DBHelper.TABLE_BM, DBHelper.COMMENT_ID + " = '" + comment + "'", null);
	}
	
	public void clearAll() {
		db.delete(DBHelper.TABLE_BM, null, null);
	}
	
	public Boolean isBookmarked(String comment) {
		Cursor cursor = db.query(DBHelper.TABLE_BM, allColumns, DBHelper.COMMENT_ID + " = '" + 
				comment + "'", null, null, null, null, null);
		cursor.moveToFirst();
		
		Boolean result = false;
		
		if(cursor.getCount() > 0) {
			result = true;
		}
		
		return result;
	}
	
	public Vector<BookmarkItems> fetchAll() {
		Cursor cursor = db.query(DBHelper.TABLE_BM, allColumns, null, null, null, null, DBHelper.ADDED_ON + " DESC", null);
		cursor.moveToFirst();
		
		Vector<BookmarkItems> result 	= new Vector<BookmarkItems>();
		
		while (!cursor.isAfterLast()) {
			BookmarkItems item	= new BookmarkItems();
			item.setItems(
							cursor.getString(cursor.getColumnIndex(dbHelper.ARTICLE_URL)), 
							cursor.getString(cursor.getColumnIndex(dbHelper.COMMENT_ID)),
							cursor.getString(cursor.getColumnIndex(dbHelper.DATE)),
							cursor.getString(cursor.getColumnIndex(dbHelper.MONTH)),
							cursor.getString(cursor.getColumnIndex(dbHelper.YEAR)),
							cursor.getString(cursor.getColumnIndex(dbHelper.NAME)));
			result.add(item);
			
			cursor.moveToNext();
		}
		cursor.close();
		
		return result;
	}
}
