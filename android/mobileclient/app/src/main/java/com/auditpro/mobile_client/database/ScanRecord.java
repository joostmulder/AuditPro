/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.auditpro.mobile_client.entities.Audit;
import com.auditpro.mobile_client.entities.Scan;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


/**
 * Records a scan within an audit session for DAL.
 * @author Eric Ruck
 */
public class ScanRecord {

	/**
	 * Initializes default instance with empty values.
	 */
	private ScanRecord() {
		setId(null);
		setAuditId(null);
		setCreatedAt(null);
		setUpdatedAt(null);
		setProductId(-1);
		setRetailPrice(null);
		setSalePrice(null);
		setScanData(null);
		setScanTypeId(-1);
		setProductName(null);
		setBrandName(null);
	}

	/**
	 * Initializes instance from entity source.
	 * @param source Initial data from entity
	 */
	ScanRecord(Scan source) {
		this();
		setId(source.getId());
		setAuditId(source.getAuditId());
		setCreatedAt(source.getCreatedAt());
		setUpdatedAt(source.getUpdatedAt());
		setProductId(source.getProductId());
		setRetailPrice(source.getRetailPrice());
		setSalePrice(source.getSalePrice());
		setScanData(source.getScanData());
		setScanTypeId(source.getScanTypeId());
		setProductName(source.getProductName());
		setBrandName(source.getBrandName());
	}

	/**
	 * Initializes a record from the database cursor.
	 * @param cursor Holds data for this instance
	 */
	private ScanRecord(Cursor cursor) {
		this();
		setId(UUID.fromString(cursor.getString(cursor.getColumnIndex(COL_ID))));
		setAuditId(UUID.fromString(cursor.getString(cursor.getColumnIndex(COL_AUDIT_ID))));
		setCreatedAt(BaseDatabase.getNullableDate(cursor, COL_CREATED_AT));
		setUpdatedAt(BaseDatabase.getNullableDate(cursor, COL_UPDATED_AT));
		setProductId(cursor.getInt(cursor.getColumnIndex(COL_PRODUCT_ID)));
		setRetailPrice(BaseDatabase.getNullableDouble(cursor, COL_RETAIL_PRICE));
		setSalePrice(BaseDatabase.getNullableDouble(cursor, COL_SALE_PRICE));
		setScanData(BaseDatabase.getNullableString(cursor, COL_SCAN_DATA));
		setScanTypeId(cursor.getInt(cursor.getColumnIndex(COL_SCAN_TYPE_ID)));
		setProductName(BaseDatabase.getNullableString(cursor, COL_PRODUCT_NAME));
		setBrandName(BaseDatabase.getNullableString(cursor, COL_BRAND_NAME));
	}

	/**
	 * Create our table in the passed database.
	 * @param db Database in which to create our table
	 */
	static void createTable(SQLiteDatabase db) {
		// Build a create table statement
		String st = "CREATE TABLE " + TABLE_NAME + " (" + COL_ID + " TEXT PRIMARY KEY, " +
				COL_AUDIT_ID + " TEXT, " +
				COL_CREATED_AT + " TEXT, " +
				COL_UPDATED_AT + " TEXT, " +
				COL_PRODUCT_ID + " INTEGER, " +
				COL_RETAIL_PRICE + " DOUBLE, " +
				COL_SALE_PRICE + " DOUBLE, " +
				COL_SCAN_DATA + " TEXT, " +
				COL_SCAN_TYPE_ID + " INTEGER, " +
				COL_PRODUCT_NAME + " TEXT, " +
				COL_BRAND_NAME + " TEXT)";

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
	 * Gets the scan for the product in an audit.
	 * @param db Database contains scans
	 * @param audit Audit contains scans
	 * @param productId Id of desired product
	 * @return Requested scan record or null if not found
	 */
	static ScanRecord getScan(SQLiteDatabase db, Audit audit, int productId) {
		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " +
				COL_AUDIT_ID + "=? and " + COL_PRODUCT_ID + "=?";
		Cursor cursor = db.rawQuery(query,
				new String[] { audit.getId().toString(), Integer.toString(productId) });
		ScanRecord res = cursor.moveToNext()
				? new ScanRecord(cursor)
				: null;
		cursor.close();
		return res;
	}

	/**
	 * Gets all the scans for an audit.
	 * @param db Database contains scans
	 * @param audit Audit whose scans we want
	 * @return List of scans in audit
	 */
	static List<ScanRecord> getScans(SQLiteDatabase db, Audit audit) {
		// Query for the reports
		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COL_AUDIT_ID + "=?";
		Cursor cursor = db.rawQuery(query, new String[] { audit.getId().toString() });

		// Build the results
		ArrayList<ScanRecord> res = new ArrayList<>();
		while (cursor.moveToNext()) {
			res.add((new ScanRecord(cursor)));
		}

		// Return results
		cursor.close();
		return res;
	}

	/**
	 * Serializes the scans for an audit to JSON.
	 * @param db Database contains scans
	 * @param audit Audit whose scans we want
	 * @return Serialized scans
	 * @throws JSONException Unexpected serialization error
	 */
	static JSONArray getJSON(SQLiteDatabase db, Audit audit) throws JSONException {
		// Get the scans from the database
		List<ScanRecord> scans = getScans(db, audit);

		// Serialize the scans
		JSONArray res = new JSONArray();
		for(ScanRecord record : scans) {
			JSONObject json = new JSONObject();
			json.put("scanId", record.getId().toString());
			json.put("createdAt", BaseDatabase.parseDateTime(record.getCreatedAt()));
			json.put("updatedAt", BaseDatabase.parseDateTime(record.getUpdatedAt()));
			json.put("chainXProductId", record.getProductId());
			json.put("retailPrice", record.getRetailPrice());
			json.put("salePrice", record.getSalePrice());
			json.put("scanData", record.getScanData());
			json.put("scanTypeId", record.getScanTypeId());
			json.put("productName", record.getProductName());
			json.put("brandName", record.getBrandName());
			res.put(json);
		}

		// Return the serialized result
		return res;
	}

	/**
	 * Deletes the scans associated with the passed audit.
	 * @param db Database from which to delete scans
	 * @param audit Audit whose scans we want to delete
	 */
	static void deleteFor(SQLiteDatabase db, Audit audit) {
		db.delete(TABLE_NAME, COL_AUDIT_ID + "=?", new String[] { audit.getId().toString() });
	}

	/**
	 * Inserts ourself into the passed database.
	 * @param db Database to receive record
	 */
	void insert(SQLiteDatabase db) {
		ContentValues insert = new ContentValues();
		insert.put(COL_ID, getId().toString());
		insert.put(COL_AUDIT_ID, getAuditId().toString());
		insert.put(COL_CREATED_AT, BaseDatabase.parseDateTime(getCreatedAt()));
		insert.put(COL_UPDATED_AT, BaseDatabase.parseDateTime(getUpdatedAt()));
		insert.put(COL_PRODUCT_ID, getProductId());
		insert.put(COL_RETAIL_PRICE, getRetailPrice());
		insert.put(COL_SALE_PRICE, getSalePrice());
		insert.put(COL_SCAN_DATA, getScanData());
		insert.put(COL_SCAN_TYPE_ID, getScanTypeId());
		insert.put(COL_PRODUCT_NAME, getProductName());
		insert.put(COL_BRAND_NAME, getBrandName());
		db.insert(TABLE_NAME, null, insert);
	}

	/**
	 * Updates our record in the passed database.
	 * @param db Database to update
	 */
	void update(SQLiteDatabase db) {
		ContentValues update = new ContentValues();
		update.put(COL_AUDIT_ID, getAuditId().toString());
		update.put(COL_CREATED_AT, BaseDatabase.parseDateTime(getCreatedAt()));
		update.put(COL_UPDATED_AT, BaseDatabase.parseDateTime(getUpdatedAt()));
		update.put(COL_PRODUCT_ID, getProductId());
		update.put(COL_RETAIL_PRICE, getRetailPrice());
		update.put(COL_SALE_PRICE, getSalePrice());
		update.put(COL_SCAN_DATA, getScanData());
		update.put(COL_SCAN_TYPE_ID, getScanTypeId());
		update.put(COL_PRODUCT_NAME, getProductName());
		update.put(COL_BRAND_NAME, getBrandName());
		db.update(TABLE_NAME, update, COL_ID + "=?", new String[] { getId().toString() });
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID value) {
		id = value;
	}

	public UUID getAuditId() {
		return auditId;
	}

	private void setAuditId(UUID value) {
		auditId = value;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	private void setCreatedAt(Date value) {
		createdAt = value;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	private void setUpdatedAt(Date value) {
		updatedAt = value;
	}

	public int getProductId() {
		return productId;
	}

	private void setProductId(int value) {
		productId = value;
	}

	public Double getRetailPrice() {
		return retailPrice;
	}

	private void setRetailPrice(Double value) {
		retailPrice = value;
	}

	public Double getSalePrice() {
		return salePrice;
	}

	private void setSalePrice(Double value) {
		salePrice = value;
	}

	public String getScanData() {
		return scanData;
	}

	private void setScanData(String value) {
		scanData = value;
	}

	public int getScanTypeId() {
		return scanTypeId;
	}

	private void setScanTypeId(int value) {
		scanTypeId = value;
	}

	public String getProductName() {
		return productName;
	}

	private void setProductName(String value) {
		productName = value;
	}

	public String getBrandName() {
		return brandName;
	}

	private void setBrandName(String value) {
		brandName = value;
	}

	private static final String TABLE_NAME = "scans";
	private static final String COL_ID = "audit_scan_uuid";
	private static final String COL_AUDIT_ID = "audit_uuid";
	private static final String COL_CREATED_AT = "created_at";
	private static final String COL_UPDATED_AT = "updated_at";
	private static final String COL_PRODUCT_ID = "chain_x_product_id";
	private static final String COL_RETAIL_PRICE = "retail_price";
	private static final String COL_SALE_PRICE = "sale_price";
	private static final String COL_SCAN_DATA = "scan_data";
	private static final String COL_SCAN_TYPE_ID = "scan_type_id";
	private static final String COL_PRODUCT_NAME = "product_name";
	private static final String COL_BRAND_NAME = "brand_name";

	private UUID id;
	private UUID auditId;
	private Date createdAt;
	private Date updatedAt;
	private int productId;
	private Double retailPrice;
	private Double salePrice;
	private String scanData;
	private int scanTypeId;
	private String productName;
	private String brandName;
}
