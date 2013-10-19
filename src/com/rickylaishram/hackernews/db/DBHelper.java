package com.rickylaishram.hackernews.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHelper extends SQLiteOpenHelper{
	
	public static final String DATABASE_NAME		= "hn.db";
	public static final int DATABASE_VERSION		= 4;
	
	public static final String TABLE_LINKS			= "read_links";
	public static final String COLUMN_ID			= "_id";
	public static final String LINKS				= "link";
	
	public static final String TABLE_BM				= "bookmarks";
	public static final String NAME					= "name";
	public static final String ARTICLE_URL			= "link";
	public static final String COMMENT_ID			= "comment";
	public static final String ADDED_ON				= "timestamp";
	public static final String DATE					= "date";
	public static final String MONTH				= "month";
	public static final String YEAR					= "year";
	
	//DATABASE CREATION
	private static final String DB_1_CREATE		= "create table " + TABLE_LINKS + "(" + 
													COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
													LINKS + " TEXT UNIQUE ON CONFLICT REPLACE " + 
													");";
	private static final String DB_2_CREATE		= "create table " + TABLE_BM + "(" + 
													COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
													ARTICLE_URL + " TEXT, " + 
													NAME + " TEXT, " + 
													DATE + " TEXT, " + 
													MONTH + " TEXT, " + 
													YEAR + " TEXT, " + 
													COMMENT_ID + " TEXT UNIQUE ON CONFLICT REPLACE, " +
													ADDED_ON + " INTEGER " +
													");";
	
	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DB_1_CREATE);
		db.execSQL(DB_2_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(DBHelper.class.getName(), "Upgrading database from version "
		        + oldVersion + " to " + newVersion
		        + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_LINKS);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_BM);
		onCreate(db);
	}

}
