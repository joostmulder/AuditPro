/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;


/**
 * Provides common features for all of our local database stores.
 * @author Eric Ruck
 */
abstract public class BaseDatabase implements AutoCloseable {

	/**
	 * Initializes a connection to a database.
	 * @param context Application context
	 * @param name Database base name
	 * @param version Database version number
	 */
	BaseDatabase(Context context, String name, int version) {
		BaseHelper helper = new BaseHelper(context, name, version);
		this.con = helper.getWritableDatabase();
		this.ctx = context.getApplicationContext();
	}

	/**
	 * Gets the database connection.
	 * @return Database connection
	 */
	SQLiteDatabase getCon() {
		return con;
	}

	/**
	 * Releases the database resource associated with this instance.
	 */
	public void close() {
		con.close();
	}

	/**
	 * Gets a nullable date from the database.
	 * @param cursor Database cursor
	 * @param columnName Column name to get
	 * @return Date or null
	 */
	static Date getNullableDate(Cursor cursor, String columnName) {
		return getNullableDate(cursor, cursor.getColumnIndex(columnName));
	}

	/**
	 * Gets a nullable date from the database
	 * @param cursor Database cursor
	 * @param idxColumn Column index to get
	 * @return Date or null
	 */
	static Date getNullableDate(Cursor cursor, int idxColumn) {
		// Check for null
		if (cursor.isNull(idxColumn)) {
			return null;
		}

		// Convert non null field
		return parseDateTime(cursor.getString(idxColumn));
	}

	/**
	 * Gets a nullable UUID from the database.
	 * @param cursor Database cursor
	 * @param idxColumn Column index to get
	 * @return UUID or null;
	 */
	static UUID getNullableId(Cursor cursor, int idxColumn) {
		// Check for null
		if (cursor.isNull(idxColumn)) {
			return null;
		}

		String value = cursor.getString(idxColumn);
		try {
			// Convert non null field
			return UUID.fromString(value);
		} catch (IllegalArgumentException exc) {
			// Failed to convert
			Log.w(LOG_TAG, "Failed to convert UUID value " + value, exc);
			return null;
		}
	}

	/**
	 * Gets a nullable double from the database.
	 * @param cursor Database cursor
	 * @param columnName Column name to get
	 * @return Double or null
	 */
	static Double getNullableDouble(Cursor cursor, String columnName) {
		return getNullableDouble(cursor, cursor.getColumnIndex(columnName));
	}

	/**
	 * Gets a nullable double from the database.
	 * @param cursor Database cursor
	 * @param idxColumn Column index to get
	 * @return Double or null
	 */
	static Double getNullableDouble(Cursor cursor, int idxColumn) {
		// Check for null
		if (cursor.isNull(idxColumn)) {
			return null;
		}

		// Convert non null field
		return cursor.getDouble(idxColumn);
	}

	/**
	 * Gets a nullable string from the database.
	 * @param cursor Database cursor
	 * @param columnName Column name to get
	 * @return Fetched string
	 */
	static String getNullableString(Cursor cursor, String columnName) {
		return getNullableString(cursor, cursor.getColumnIndex(columnName), null);
	}

	/**
	 * Gets a nullable string from the database.
	 * @param cursor Database cursor
	 * @param columnName Column name to get
	 * @param nullValue Value to use in place of null
	 * @return Fetched string
	 */
	static String getNullableString(Cursor cursor, String columnName, @SuppressWarnings("SameParameterValue") String nullValue) {
		return getNullableString(cursor, cursor.getColumnIndex(columnName), nullValue);
	}

	/**
	 * Gets a nullable string from the database.
	 * @param cursor Database cursor
	 * @param idxColumn Column index to get
	 * @param nullValue Value to use in place of null
	 * @return Fetched string
	 */
	private static String getNullableString(Cursor cursor, int idxColumn, String nullValue) {
		// Validate
		if (idxColumn < 0) {
			// Invalid column
			return nullValue;
		}
		if (cursor.isNull(idxColumn)) {
			// Null value in column
			return nullValue;
		}

		// Return non null field
		return cursor.getString(idxColumn);
	}

	private static final String DATE_ISO8601 = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

	/**
	 * Parses the ISO 8601 timestamp encoded as a string to a system object.
	 * Logs a warning if the input source is invalid.
	 * @param source Source timestamp string
	 * @return Date object or null
	 */
	public static Date parseDateTime(String source) {
		// Trivial case
		if ((source == null) || source.matches("^\\s*$")) {
			return null;
		}

		// Parse to the structure
		SimpleDateFormat df = new SimpleDateFormat(DATE_ISO8601, Locale.US);
		ParsePosition pos = new ParsePosition(0);
		String parse = source.
				replaceAll("Z$", "+0000").
				replaceAll("(?<=T\\d\\d:\\d\\d:\\d\\d)\\+", ".000+");
		Date res = df.parse(parse, pos);
		if (res == null) {
			Log.w(LOG_TAG, String.format("Attempted to parse invalid time stamp \"%s\"", source));
		}

		// Returned the parsed structure
		return res;
	}

	/**
	 * Common utility to convert a time stamp to ISO 8601.
	 * @param source Stamp to convert
	 * @return 8601 time string or null
	 */
	public static String parseDateTime(Date source) {
		// Trivial case
		if (source == null) {
			// No date/time
			return null;
		}

		// Format in ISO 8601
		SimpleDateFormat df = new SimpleDateFormat(DATE_ISO8601, Locale.US);
		return df.format(source);
	}

	/**
	 * Common utility to convert a time stamp to a readable format.
	 * @param source Stamp to convert
	 * @return Readable time and date
	 */
	public static String readDateTime(Date source) {
		// Trivial case
		if (source == null) {
			// No date time
			return "N/A";
		}

		// Format short readable for current locale
		DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
		return df.format(source);
	}

	/**
	 * Delegate table creation to the subclass.
	 * @param db Database in which tables need to be created
	 */
	abstract protected void onCreateDb(SQLiteDatabase db);

	/**
	 * Delegate table update to the subclass.
	 * @param db Database in which tables need to be updated
	 * @param lastVersion Last version of current database
	 */
	abstract protected void onUpdateDb(SQLiteDatabase db, int lastVersion);

	/**
	 * Implements a database helper to make accessing SQLite easier.
	 */
	class BaseHelper extends SQLiteOpenHelper {

		BaseHelper(Context context, String name, int version) {
			super(context, name + ".db", null, version);
		}

		@Override
		public void onCreate(SQLiteDatabase sqLiteDatabase) {
			onCreateDb(sqLiteDatabase);
		}

		@Override
		public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
			// Nothing to do for now
			onUpdateDb(sqLiteDatabase, i);
		}
	}

	/** Provides the connection to the database. */
	private SQLiteDatabase con;

	/** Provides the application context. */
	Context ctx;

	/**
	 * Identifies log message source.
	 */
	private static final String LOG_TAG = "BaseDatabase";
}
