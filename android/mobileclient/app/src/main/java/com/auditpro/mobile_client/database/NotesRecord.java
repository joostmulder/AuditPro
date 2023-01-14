/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.auditpro.mobile_client.entities.Notes;

import java.util.UUID;


/**
 * Records the notes for an audit session in the DAL.
 * @author Eric Ruck
 */
public class NotesRecord {

	/**
	 * Initializes default instance with empty values.
	 */
	private NotesRecord() {
		setId(null);
		setAuditId(null);
		setContents(null);
		setStore(null);
	}

	/**
	 * Initializes instance from entity source.
	 * @param source Entity source
	 * @param contents New contents for record or null if none
	 * @param store New store note contents or null if none
	 */
	NotesRecord(Notes source, String contents, String store) {
		this();
		setId(source.getId());
		setAuditId(source.getAuditId());
		setContents((contents == null) ? source.getContents() : contents);
		setStore((store == null) ? source.getStore() : store);
	}

	/**
	 * Create our table in the passed database.
	 * @param db Database in which to create our table
	 */
	static void createTable(SQLiteDatabase db) {
		// Build a create table statement
		String st = "CREATE TABLE " + TABLE_NAME + " (" + COL_ID + " TEXT PRIMARY KEY, " +
				COL_AUDIT_ID + " TEXT, " +
				COL_CONTENTS + " TEXT, " +
				COL_STORE + " TEXT" +
				")";

		// Execute it
		db.execSQL(st);
	}

	/**
	 * Updates our table to the current version if becessary
	 * @param db Connected database
	 * @param lastVersion Last version of our database
	 */
	@SuppressWarnings("unused")
	static void updateTable(SQLiteDatabase db, int lastVersion) {
		if (lastVersion < AuditDatabase.DB_VERSION_16) {
			db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD " + COL_STORE + " TEXT");
		}
	}

	/**
	 * Gets the note for the passed audit.
	 * @param db Database that holds the notes
	 * @param auditId Audit whose notes we want
	 * @return Notes for audit
	 */
	static Notes getNote(SQLiteDatabase db, UUID auditId) {
		// Query for the notes
		String query = "SELECT * FROM notes WHERE audit_uuid=?";
		Cursor cursor = db.rawQuery(query, new String[] { auditId.toString() });
		Notes res = cursor.moveToNext()
				? new Notes(
					UUID.fromString(cursor.getString(cursor.getColumnIndex(COL_ID))),
					auditId,
					BaseDatabase.getNullableString(cursor, COL_CONTENTS, ""),
					BaseDatabase.getNullableString(cursor, COL_STORE, "")
				  )
				: new Notes(null, auditId, "", "");

		// Return the result
		cursor.close();
		return res;
	}

	/**
	 * Updates the passed note in the database. Inserts if this note has not yet been created.
	 * @param db Database to update
	 */
	void update(SQLiteDatabase db) {
		if (getId() == null) {
			// Insert a new record
			ContentValues insert = new ContentValues();
			insert.put(COL_ID, UUID.randomUUID().toString());
			insert.put(COL_AUDIT_ID, getAuditId().toString());
			insert.put(COL_CONTENTS, getContents());
			insert.put(COL_STORE, getStore());
			db.insert(TABLE_NAME, null, insert);
		} else {
			// Update existing record
			ContentValues update = new ContentValues();
			update.put(COL_CONTENTS, getContents());
			update.put(COL_STORE, getStore());
			db.update(TABLE_NAME, update, COL_ID + "=?", new String[] { getId().toString() });
		}
	}

	private static final String TABLE_NAME = "notes";
	private static final String COL_ID = "notes_uuid";
	private static final String COL_AUDIT_ID = "audit_uuid";
	private static final String COL_CONTENTS = "contents";
	private static final String COL_STORE = "store_audit_note";

	private UUID id;
	private UUID auditId;
	private String contents;
	private String store;

	public UUID getId() {
		return id;
	}

	public void setId(UUID value) {
		id = value;
	}

	private UUID getAuditId() {
		return auditId;
	}

	private void setAuditId(UUID value) {
		auditId = value;
	}

	private String getContents() {
		return contents;
	}

	private void setContents(String value) {
		contents = ((value == null) || (value.length() == 0)) ? null : value;
	}

	private String getStore() {
		return store;
	}

	private void setStore(String value) {
		store = ((value == null) || (value.length() == 0)) ? null : value;
	}
}
