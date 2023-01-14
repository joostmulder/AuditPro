/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.auditpro.mobile_client.MobileClientException;
import com.auditpro.mobile_client.entities.Audit;


/**
 * Provides audit session object for DAL.
 * @author Eric Ruck
 */
public class AuditRecord {

	/**
	 * Initializes default instance with empty values.
	 */
	private AuditRecord() {
		// Initialize properties to default empty values
		setId(null);
		setUserId(-1);
		setStoreId(-1);
		setStoreDescr(null);
		setAuditStartedAt(null);
		setAuditEndedAt(null);
		setAuditTypeId(-1);
		setLatitudeAtStart(null);
		setLongitudeAtStart(null);
		setLatitudeAtEnd(null);
		setLongitudeAtEnd(null);
	}

	/**
	 * Initializes instance from entity source.
	 * @param source Entity source for initial values
	 */
	AuditRecord(Audit source) {
		this();
		setId(source.getId());
		setUserId(source.getUserId());
		setStoreId(source.getStoreId());
		setStoreDescr(source.getStoreDescr());
		setAuditStartedAt(source.getAuditStartedAt());
		setAuditEndedAt(source.getAuditEndedAt());
		setAuditTypeId(source.getAuditTypeId());
		setLatitudeAtStart(source.getLatitudeAtStart());
		setLongitudeAtStart(source.getLongitudeAtStart());
		setLatitudeAtEnd(source.getLatitudeAtEnd());
		setLongitudeAtEnd(source.getLongitudeAtEnd());
	}

	/**
	 * Initializes a record for a new audit.
	 * @param userId User identifier
	 * @param storeId Store identifier
	 * @param storeDescr Store description for display
	 * @param auditTypeId Audit type identifier
	 * @param latitude Latitude at start or null
	 * @param longitude Longitude at start or null
	 */
	AuditRecord(int userId, int storeId, String storeDescr,
					   int auditTypeId, Double latitude, Double longitude) {
		this();
		setId(UUID.randomUUID());
		setUserId(userId);
		setStoreId(storeId);
		setStoreDescr(storeDescr);
		setAuditStartedAt(new Date());
		setAuditTypeId(auditTypeId);
		setLatitudeAtStart(latitude);
		setLongitudeAtStart(longitude);
	}

	/**
	 * Initializes a record from a database cursor.
	 * @param cursor Cursor to source data
	 */
	private AuditRecord(Cursor cursor) {
		this();
		setId(UUID.fromString(cursor.getString(cursor.getColumnIndex(COL_ID))));
		setUserId(cursor.getInt(cursor.getColumnIndex(COL_USER_ID)));
		setStoreId(cursor.getInt(cursor.getColumnIndex(COL_STORE_ID)));
		setStoreDescr(cursor.getString(cursor.getColumnIndex(COL_STORE_DESCR)));
		setAuditStartedAt(BaseDatabase.parseDateTime(cursor.getString(cursor.getColumnIndex(COL_AUDIT_STARTED_AT))));
		setAuditEndedAt(BaseDatabase.getNullableDate(cursor, COL_AUDIT_ENDED_AT));
		setAuditTypeId(cursor.getInt(cursor.getColumnIndex(COL_AUDIT_TYPE_ID)));
		setLatitudeAtStart(BaseDatabase.getNullableDouble(cursor, COL_LATITUDE_AT_START));
		setLongitudeAtStart(BaseDatabase.getNullableDouble(cursor, COL_LONGITUDE_AT_END));
		setLatitudeAtEnd(BaseDatabase.getNullableDouble(cursor, COL_LATITUDE_AT_END));
		setLongitudeAtEnd(BaseDatabase.getNullableDouble(cursor, COL_LONGITUDE_AT_END));
	}

	/**
	 * Create our table in the passed database.
	 * @param db Database in which to create our table
	 */
	static void createTable(SQLiteDatabase db) {
		// Build a create table statement
		String st = "CREATE TABLE " + TABLE_NAME + " (" + COL_ID + " TEXT PRIMARY KEY, " +
				COL_USER_ID + " INTEGER, " +
				COL_STORE_ID + " INTEGER, " +
				COL_STORE_DESCR + " TEXT, " +
				COL_AUDIT_STARTED_AT + " TEXT, " +
				COL_AUDIT_ENDED_AT + " TEXT, " +
				COL_AUDIT_TYPE_ID + " INTEGER, " +
				COL_LATITUDE_AT_START + " DOUBLE, " +
				COL_LONGITUDE_AT_START + " DOUBLE, " +
				COL_LATITUDE_AT_END + " DOUBLE, " +
				COL_LONGITUDE_AT_END + " DOUBLE)";

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
		// No updates as of now
	}

	/**
	 * Inserts ourself into the database.
	 * @param db Database to insert
	 */
	void insertTo(SQLiteDatabase db) {
		ContentValues record = new ContentValues();
		record.put(COL_ID, getId().toString());
		record.put(COL_USER_ID, getUserId());
		record.put(COL_STORE_ID, getStoreId());
		record.put(COL_STORE_DESCR, getStoreDescr());
		record.put(COL_AUDIT_STARTED_AT, BaseDatabase.parseDateTime(getAuditStartedAt()));
		record.put(COL_AUDIT_ENDED_AT, BaseDatabase.parseDateTime(getAuditEndedAt()));
		record.put(COL_AUDIT_TYPE_ID, getAuditTypeId());
		record.put(COL_LATITUDE_AT_START, getLatitudeAtStart());
		record.put(COL_LONGITUDE_AT_START, getLongitudeAtStart());
		record.put(COL_LATITUDE_AT_END, getLatitudeAtEnd());
		record.put(COL_LONGITUDE_AT_END, getLongitudeAtEnd());
		db.insert(TABLE_NAME, null, record);
	}

	/**
	 * Gets the number of completed audits for the passed user in the passed database.
	 * @param db Database to query
	 * @param userId Identifies user
	 * @return Count of copmleted audits for user
	 */
	static int getCount(SQLiteDatabase db, int userId, boolean completed) {
		String query = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE user_id=? AND audit_ended_at IS " +
				(completed ? "NOT NULL" : "NULL");
		Cursor cursor = db.rawQuery(query, new String[] { Integer.toString(userId) });
		int count = cursor.moveToNext() ? cursor.getInt(0) : 0;
		cursor.close();
		return count;
	}

	/**
	 * Gets the completed audits for a user in the database.
	 * @param db Database to query
	 * @param userId User whose audits we want
	 * @return List of completed audits, may be empty
	 */
	static List<Audit> getCompleteAudits(SQLiteDatabase db, int userId) {
		// Query the database for the audits
		String query = "SELECT * FROM " + TABLE_NAME + " WHERE user_id=? AND audit_ended_at IS NOT NULL";
		Cursor cursor = db.rawQuery(query, new String[] { Integer.toString(userId) });

		// Accumulate the results
		ArrayList<Audit> res = new ArrayList<>();
		int idxId = -1;
		int idxUserId = -1;
		int idxStoreId = -1;
		int idxStoreDescr = -1;
		int idxAuditStartedAt = -1;
		int idxAuditEndedAt = -1;
		int idxAuditTypeId = -1;
		int idxLatitudeAtStart = -1;
		int idxLongitudeAtStart = -1;
		int idxLatitudeAtEnd = -1;
		int idxLongitudeAtEnd = -1;
		while (cursor.moveToNext()) {
			if (idxId < 0) {
				// Get the column indices
				idxId = cursor.getColumnIndex(COL_ID);
				idxUserId = cursor.getColumnIndex(COL_USER_ID);
				idxStoreId = cursor.getColumnIndex(COL_STORE_ID);
				idxStoreDescr = cursor.getColumnIndex(COL_STORE_DESCR);
				idxAuditStartedAt = cursor.getColumnIndex(COL_AUDIT_STARTED_AT);
				idxAuditEndedAt = cursor.getColumnIndex(COL_AUDIT_ENDED_AT);
				idxAuditTypeId = cursor.getColumnIndex(COL_AUDIT_TYPE_ID);
				idxLatitudeAtStart = cursor.getColumnIndex(COL_LATITUDE_AT_START);
				idxLongitudeAtStart = cursor.getColumnIndex(COL_LONGITUDE_AT_START);
				idxLatitudeAtEnd = cursor.getColumnIndex(COL_LATITUDE_AT_END);
				idxLongitudeAtEnd = cursor.getColumnIndex(COL_LONGITUDE_AT_END);
			}

			// Create the new record
			AuditRecord record = new AuditRecord();
			record.setId(UUID.fromString(cursor.getString(idxId)));
			record.setUserId(cursor.getInt(idxUserId));
			record.setStoreId(cursor.getInt(idxStoreId));
			record.setStoreDescr(cursor.getString(idxStoreDescr));
			record.setAuditStartedAt(BaseDatabase.parseDateTime(cursor.getString(idxAuditStartedAt)));
			record.setAuditEndedAt(BaseDatabase.getNullableDate(cursor, idxAuditEndedAt));
			record.setAuditTypeId(cursor.getInt(idxAuditTypeId));
			record.setLatitudeAtStart(BaseDatabase.getNullableDouble(cursor, idxLatitudeAtStart));
			record.setLongitudeAtStart(BaseDatabase.getNullableDouble(cursor, idxLongitudeAtStart));
			record.setLatitudeAtEnd(BaseDatabase.getNullableDouble(cursor, idxLatitudeAtEnd));
			record.setLongitudeAtEnd(BaseDatabase.getNullableDouble(cursor, idxLongitudeAtEnd));
			res.add(new Audit(record));
		}

		// Return the complete result
		cursor.close();
		return res;
	}

	/**
	 * Gets the audit in progress for the user or null if none.
	 * @param db Database where audits live
	 * @param userId User id whose open audit we want
	 * @return Open audit or null
	 */
	static Audit getAudit(SQLiteDatabase db, int userId) {
		// Query for the audit
		String query = "SELECT * FROM audits WHERE " + COL_USER_ID + "=? AND audit_ended_at IS NULL";
		Cursor cursor = db.rawQuery(query, new String[] { Integer.toString(userId )});
		Audit res = cursor.moveToNext()
				? new Audit(new AuditRecord(cursor))
				: null;

		// Return the audit result
		cursor.close();
		return res;
	}

	/**
	 * Deletes the passed audit.
	 * @param db Database from which to delete audit
	 * @param audit Audit to delete
	 */
	static void deleteFor(SQLiteDatabase db, Audit audit) {
		db.delete(TABLE_NAME, COL_ID + "=?", new String[] { audit.getId().toString() });
	}

	/**
	 * Ends this audit now.
	 * @param latitude Latitude at end of audit
	 * @param longitude Longitude at end of audit
	 * @param endTime Optional end of audit time
	 * @throws MobileClientException Business logic error
	 */
	void endAudit(Double latitude, Double longitude, Date endTime)
			throws MobileClientException {
		// Validate
		if (auditEndedAt != null) {
			// Invalid, audit already ended
			throw new MobileClientException(String.format(
					"Attempted to end audit %s that has already ended.",
					getId().toString()));
		}

		// End the audit now
		setAuditEndedAt((endTime == null) ? new Date() : endTime);
		setLatitudeAtEnd(latitude);
		setLongitudeAtEnd(longitude);
	}

	/**
	 * Reopens this audit now.
	 * @throws MobileClientException Business logic error
	 */
	void reopenAudit() throws MobileClientException {
		// Validate
		if (auditEndedAt == null) {
			// Invalid, audit currently open
			throw new MobileClientException((String.format(
					"Attempted to reopen audit %s that is already open.",
					getId().toString())));
		}

		// Reopen the audit
		setAuditEndedAt(null);
		setLatitudeAtEnd(null);
		setLongitudeAtEnd(null);
	}

	/**
	 * Applies updates in this record to the database.
	 * @param db Database to which the updates will be applied
	 */
	void update(SQLiteDatabase db) {
		ContentValues update = new ContentValues();
		update.put(COL_AUDIT_ENDED_AT, BaseDatabase.parseDateTime(getAuditEndedAt()));
		update.put(COL_LATITUDE_AT_END, getLatitudeAtEnd());
		update.put(COL_LONGITUDE_AT_END, getLongitudeAtEnd());
		db.update(TABLE_NAME, update, COL_ID + "=?", new String[] { getId().toString() });
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID value) {
		id = value;
	}

	public int getUserId() {
		return userId;
	}

	private void setUserId(int value) {
		userId = value;
	}

	public int getStoreId() {
		return storeId;
	}

	private void setStoreId(int value) {
		storeId = value;
	}

	public String getStoreDescr() {
		return storeDescr;
	}

	private void setStoreDescr(String value) {
		storeDescr = value;
	}

	public Date getAuditStartedAt() {
		return auditStartedAt;
	}

	private void setAuditStartedAt(Date value) {
		auditStartedAt = value;
	}

	public Date getAuditEndedAt() {
		return auditEndedAt;
	}

	private void setAuditEndedAt(Date value) {
		auditEndedAt = value;
	}

	public int getAuditTypeId() {
		return auditTypeId;
	}

	private void setAuditTypeId(int value) {
		auditTypeId = value;
	}

	public Double getLatitudeAtStart() {
		return latitudeAtStart;
	}

	private void setLatitudeAtStart(Double value) {
		latitudeAtStart = value;
	}

	public Double getLongitudeAtStart() {
		return longitudeAtStart;
	}

	private void setLongitudeAtStart(Double value) {
		longitudeAtStart = value;
	}

	public Double getLatitudeAtEnd() {
		return latitudeAtEnd;
	}

	private void setLatitudeAtEnd(Double value) {
		latitudeAtEnd = value;
	}

	public Double getLongitudeAtEnd() {
		return longitudeAtEnd;
	}

	private void setLongitudeAtEnd(Double value) {
		longitudeAtEnd = value;
	}

	private static final String TABLE_NAME = "audits";
	private static final String COL_ID = "audit_uuid";
	private static final String COL_USER_ID ="user_id";
	private static final String COL_STORE_ID = "store_id";
	private static final String COL_STORE_DESCR = "store_descr";
	private static final String COL_AUDIT_STARTED_AT = "audit_started_at";
	private static final String COL_AUDIT_ENDED_AT = "audit_ended_at";
	private static final String COL_AUDIT_TYPE_ID = "audit_type_id";
	private static final String COL_LATITUDE_AT_START = "latitude_at_start";
	private static final String COL_LONGITUDE_AT_START = "longitude_at_start";
	private static final String COL_LATITUDE_AT_END = "latitude_at_end";
	private static final String COL_LONGITUDE_AT_END = "longitude_at_end";

	private UUID id;
	private int userId;
	private int storeId;
	private String storeDescr;
	private Date auditStartedAt;
	private Date auditEndedAt;
	private int auditTypeId;
	private Double latitudeAtStart;
	private Double longitudeAtStart;
	private Double latitudeAtEnd;
	private Double longitudeAtEnd;
}
