/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.auditpro.mobile_client.entities.Audit;
import com.auditpro.mobile_client.entities.Report;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


/**
 * Interfaces one reported row in an audit with the database.
 * @author Eric Ruck
 */
public class ReportRecord {

	/**
	 * Initializes default instance with empty values.
	 */
	private ReportRecord() {
		setId(null);
		setCreatedAt(null);
		setUpdatedAt(null);
		setAuditId(null);
		setScanId(null);
		setProductId(-1);
		setReorderStatusId(-1);
	}

	/**
	 * Initializes instance from entity source.  Generates a new Guid if the source does not
	 * already have one.
	 * @param source Provides initial field values
	 */
	ReportRecord(Report source) {
		this();
		setId((source.getId() == null) ? UUID.randomUUID() : source.getId());
		setCreatedAt(source.getCreatedAt());
		setUpdatedAt(source.getUpdatedAt());
		setAuditId(source.getAuditId());
		setScanId(source.getScanId());
		setProductId(source.getProductId());
		setReorderStatusId(source.getReorderStatusId());
	}

	/**
	 * Initializes instance from database cursor.
	 * @param cursor Instance source
	 */
	private ReportRecord(Cursor cursor) {
		this();
		setId(UUID.fromString(cursor.getString(cursor.getColumnIndex(COL_ID))));
		setCreatedAt(BaseDatabase.getNullableDate(cursor, COL_CREATED_AT));
		setUpdatedAt(BaseDatabase.getNullableDate(cursor, COL_UPDATED_AT));
		setAuditId(UUID.fromString(cursor.getString(cursor.getColumnIndex(COL_AUDIT_ID))));
		setScanId(BaseDatabase.getNullableId(cursor, cursor.getColumnIndex(COL_SCAN_ID)));
		setProductId(cursor.getInt(cursor.getColumnIndex(COL_PRODUCT_ID)));
		setReorderStatusId(cursor.getInt(cursor.getColumnIndex(COL_REORDER_STATUS_ID)));
	}

	/**
	 * Create our table in the passed database.
	 * @param db Database in which to create our table
	 */
	static void createTable(SQLiteDatabase db) {
		// Build a create table statement
		String st = "CREATE TABLE " + TABLE_NAME + " (" + COL_ID + " TEXT PRIMARY KEY, " +
				COL_CREATED_AT + " TEXT, " +
				COL_UPDATED_AT + " TEXT, " +
				COL_AUDIT_ID + " TEXT, " +
				COL_SCAN_ID + " TEXT, " +
				COL_PRODUCT_ID + " INTEGER, " +
				COL_REORDER_STATUS_ID + " INTEGER)";

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
	 * Get the report record for the product in an audit.
	 * @param db Database hold report records
	 * @param audit Audit whose record we want
	 * @param productId Product in audit whose report we want
	 * @return Report record if found else null
	 */
	static ReportRecord getReport(SQLiteDatabase db, Audit audit, int productId) {
		// Query for the report
		String query = "SELECT * FROM reports WHERE " + COL_AUDIT_ID + "=? and " + COL_PRODUCT_ID + "=?";
		Cursor cursor = db.rawQuery(query,
				new String[] { audit.getId().toString(), Integer.toString(productId) });
		ReportRecord res = cursor.moveToNext()
				? new ReportRecord(cursor)
				: null;

		// Return the result
		cursor.close();
		return res;
	}

	/**
	 * Gets all the reports for an audit.
	 * @param db Database contains reports
	 * @param audit Audit whose reports we want
	 * @return List of reports in audit
	 */
	static List<Report> getReports(SQLiteDatabase db, Audit audit) {
		// Query for the reports
		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COL_AUDIT_ID + "=?";
		Cursor cursor = db.rawQuery(query, new String[] { audit.getId().toString() });

		// Build the results
		ArrayList<Report> res = new ArrayList<>();
		while (cursor.moveToNext()) {
			res.add(new Report(new ReportRecord(cursor)));
		}

		// Return results
		cursor.close();
		return res;
	}

	/**
	 * Serializes the reports for an audit to JSON.
	 * @param db Database contains reports
	 * @param audit Audit whose reports we want
	 * @return Serialized reports
	 * @throws JSONException Unexpected serialization error
	 */
	static JSONArray getJSON(SQLiteDatabase db, Audit audit) throws JSONException {
		// Get the scans from the database
		List<Report> reports = getReports(db, audit);

		// Serialize the reports
		JSONArray res = new JSONArray();
		for (Report record : reports) {
			// Serialize the current report
			JSONObject json = new JSONObject();
			UUID scanId = record.getScanId();
			json.put("reportId", record.getId().toString());
			json.put("createdAt", BaseDatabase.parseDateTime(record.getCreatedAt()));
			json.put("updatedAt", BaseDatabase.parseDateTime(record.getUpdatedAt()));
			json.put("scanId", (scanId == null) ? null : scanId.toString());
			json.put("chainXProductId", record.getProductId());
			json.put("reorderStatusId", record.getReorderStatusId());
			res.put(json);
		}

		// Return the serialized reports
		return res;
	}

	/**
	 * Deletes the reports associated with the passed audit.
	 * @param db Database from which to delete reports
	 * @param audit Audit whose reports we want to delete
	 */
	static void deleteFor(SQLiteDatabase db, Audit audit) {
		db.delete(TABLE_NAME, COL_AUDIT_ID + "=?", new String[] { audit.getId().toString() });
	}

	/**
	 * Inserts this record into the passed database.
	 * @param db Database to receive record
	 */
	void insert(SQLiteDatabase db) {
		ContentValues insert = new ContentValues();
		insert.put(COL_ID, getId().toString());
		insert.put(COL_CREATED_AT, BaseDatabase.parseDateTime(getCreatedAt()));
		insert.put(COL_UPDATED_AT, BaseDatabase.parseDateTime(getUpdatedAt()));
		insert.put(COL_AUDIT_ID, getAuditId().toString());
		insert.put(COL_SCAN_ID, getScanIdString());
		insert.put(COL_PRODUCT_ID, getProductId());
		insert.put(COL_REORDER_STATUS_ID, getReorderStatusId());
		db.insert(TABLE_NAME, null, insert);
	}

	/**
	 * Updates this record into the passed database.
	 * @param db Database to receive record
	 */
	void update(SQLiteDatabase db) {
		ContentValues update = new ContentValues();
		update.put(COL_UPDATED_AT, BaseDatabase.parseDateTime(getUpdatedAt()));
		update.put(COL_SCAN_ID, getScanIdString());
		update.put(COL_PRODUCT_ID, getProductId());
		update.put(COL_REORDER_STATUS_ID, getReorderStatusId());
		db.update(TABLE_NAME, update, COL_ID + "=?", new String[] { getId().toString() });
	}

	/**
	 * Gets the unique identifier for this row.
	 * @return Unique identifier
	 */
	public UUID getId() {
		return id;
	}

	/**
	 * Sets the unique identifier for this row.
	 * @param value New unique identifier
	 */
	public void setId(UUID value) {
		id = value;
	}

	/**
	 * Gets the time at which this record was created.
	 * @return Creation time
	 */
	public Date getCreatedAt() {
		return createdAt;
	}

	/**
	 * Sets the time at which this record was created.
	 * @param value New creation time
	 */
	private void setCreatedAt(Date value) {
		createdAt = value;
	}

	/**
	 * Gets the time at which this record was most recently updated.
	 * @return Most recent update time
	 */
	public Date getUpdatedAt() {
		return updatedAt;
	}

	/**
	 * Sets the time at which this record was most recently updated.
	 * @param value New most recent update time
	 */
	private void setUpdatedAt(Date value) {
		updatedAt = value;
	}

	/**
	 * Gets the id of the audit with which this record is associated.
	 * @return Associated audit id
	 */
	public UUID getAuditId() {
		return auditId;
	}

	/**
	 * Sets the id of the audit with which this record is associated.
	 * @param value New associated audit id
	 */
	private void setAuditId(UUID value) {
		auditId = value;
	}

	/**
	 * Gets the scan with which this record is associated
	 * @return Associated scan id
	 */
	public UUID getScanId() {
		return scanId;
	}

	/**
	 * Gets the scan with which this record is associated
	 * @return Scan id rendered as a string or null if none
	 */
	private String getScanIdString() {
		return (scanId == null) ? null : scanId.toString();
	}

	/**
	 * Sets the scan with which this record is associated
	 * @param value Associated scan id
	 */
	private void setScanId(UUID value) {
		scanId = value;
	}

	/**
	 * Gets the id of the product with which this record is associated
	 * @return Associated product id
	 */
	public int getProductId() {
		return productId;
	}

	/**
	 * Sets the id of the product with which this record is associated
	 * @param value Associated product id
	 */
	private void setProductId(int value) {
		productId = value;
	}

	/**
	 * Gets the reorder status for this combination of audit and product.
	 * @return Reorder status id for product in audit
	 */
	public int getReorderStatusId() {
		return reorderStatusId;
	}

	/**
	 * Sets the reorder status for this combination of audit and product.
	 * @param value New reorder status id for product in audit
	 */
	private void setReorderStatusId(int value) {
		reorderStatusId = value;
	}

	private static final String TABLE_NAME = "reports";
	private static final String COL_ID = "audit_report_uuid";
	private static final String COL_CREATED_AT = "created_at";
	private static final String COL_UPDATED_AT = "updated_at";
	private static final String COL_AUDIT_ID = "audit_uuid";
	private static final String COL_SCAN_ID = "audit_scan_uuid";
	private static final String COL_PRODUCT_ID = "chain_x_product_id";
	private static final String COL_REORDER_STATUS_ID = "reorder_status_id";

	private UUID id;
	private Date createdAt;
	private Date updatedAt;
	private UUID auditId;
	private UUID scanId;
	private int productId;
	private int reorderStatusId;
}
