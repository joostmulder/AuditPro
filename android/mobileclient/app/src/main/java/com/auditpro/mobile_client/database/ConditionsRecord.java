/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.auditpro.mobile_client.entities.Audit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.auditpro.mobile_client.database.AuditDatabase.DB_VERSION_18;


/**
 * Represents the table in which a record stores one set of SKU conditions for a product in an audit.
 * @author Eric Ruck
 */
public class ConditionsRecord {

	/**
	 * Initializes default instance with empty values.
	 */
	private ConditionsRecord() {
		setId(null);
		setCreatedAt(null);
		setUpdatedAt(null);
		setAuditId(null);
		setProductId(-1);
		setConditions((String) null);
	}

	/**
	 * Initializes instance from database cursor.
	 * @param cursor Instance source
	 */
	private ConditionsRecord(Cursor cursor) {
		this();
		setId(UUID.fromString(cursor.getString(cursor.getColumnIndex(COL_ID))));
		setCreatedAt(BaseDatabase.getNullableDate(cursor, COL_CREATED_AT));
		setUpdatedAt(BaseDatabase.getNullableDate(cursor, COL_UPDATED_AT));
		setAuditId(UUID.fromString(cursor.getString(cursor.getColumnIndex(COL_AUDIT_ID))));
		setProductId(cursor.getInt(cursor.getColumnIndex(COL_PRODUCT_ID)));
		setConditions(BaseDatabase.getNullableString(cursor, COL_CONDITIONS));
	}

	/**
	 * Initializes a new record from passed properties
	 * @param audit Audit with which the product is associated
	 * @param productId Product whose conditions we want to set
	 * @param conditions Conditions to set
	 */
	private ConditionsRecord(Audit audit, int productId, Set<Integer> conditions) {
		this();
		setCreatedAt(new Date());
		setUpdatedAt(new Date());
		setAuditId(audit.getId());
		setProductId(productId);
		setConditions(conditions);
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
				COL_PRODUCT_ID + " INTEGER, " +
				COL_CONDITIONS + " TEXT)";

		// Execute it
		db.execSQL(st);
	}

	/**
	 * Updates our table to the current version if becessary
	 * @param db Connected database
	 * @param lastVersion Last version of our database
	 */
	static void updateTable(SQLiteDatabase db, int lastVersion) {
		if (lastVersion < DB_VERSION_18) {
			// Create our table as of this version
			createTable(db);
		}
	}

	/**
	 * Get the conditions record for the product in an audit.
	 * @param db Database hold conditions records
	 * @param audit Audit whose record we want
	 * @param productId Product in audit whose conditions we want
	 * @return Conditions record if found else null
	 */
	public static ConditionsRecord getConditions(SQLiteDatabase db, Audit audit, int productId) {
		// Query for the report
		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COL_AUDIT_ID + "=? and " + COL_PRODUCT_ID + "=?";
		Cursor cursor = db.rawQuery(query,
				new String[] { audit.getId().toString(), Integer.toString(productId) });
		ConditionsRecord res = cursor.moveToNext()
				? new ConditionsRecord(cursor)
				: null;

		// Return the result
		cursor.close();
		return res;
	}

	/**
	 * Sets the conditions associated with the product in an audit.
	 * @param db Database holds conditions records
	 * @param audit Audit whose associated with product
	 * @param productId Identifies product whose conditions we want to set
	 * @param conditions Conditions to set
	 */
	public static void setConditions(SQLiteDatabase db, Audit audit, int productId, Set<Integer> conditions) {
		if (conditions == null) {
			// No conditions
			deleteFor(db, audit, productId);
		} else {
			// Is there an existing record?
			ConditionsRecord rec = getConditions(db, audit, productId);
			if (rec == null) {
				// Insert new record
				rec = new ConditionsRecord(audit, productId, conditions);
			} else {
				// Update existing record
				rec.setUpdatedAt(new Date());
				rec.setConditions(conditions);
			}

			// Persist the record
			rec.save(db);
		}
	}

	/**
	 * Deletes the conditions associated with the passed audit.
	 * @param db Database from which to delete conditions
	 * @param audit Audit whose conditions we want to delete
	 */
	static void deleteFor(SQLiteDatabase db, Audit audit) {
		db.delete(TABLE_NAME, COL_AUDIT_ID + "=?", new String[] { audit.getId().toString() });
	}

	/**
	 * Deletes the conditions associated with the passed audit.
	 * @param db Database from which to delete conditions
	 * @param audit Audit whose conditions we want to delete
	 * @param productId Product whose conditions we want to delete
	 */
	private static void deleteFor(SQLiteDatabase db, Audit audit, int productId) {
		db.delete(TABLE_NAME, COL_AUDIT_ID + "=? AND " + COL_PRODUCT_ID + "=?",
				new String[] { audit.getId().toString(), Integer.toString(productId) });
	}

	/**
	 * Serializes the conditions for every product in an audit to JSON.
	 * @param db Database contains reports
	 * @param audit Audit whose reports we want
	 * @return Serialized reports
	 * @throws JSONException Unexpected serialization error
	 */
	static JSONArray getJSON(SQLiteDatabase db, Audit audit) throws JSONException {
		// Query for the conditions
		String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COL_AUDIT_ID + "=?";
		Cursor cursor = db.rawQuery(query, new String[] { audit.getId().toString() });

		// Build the results
		JSONArray res = new JSONArray();
		while (cursor.moveToNext()) {
			// Build a new selected SKU conditions object
			ConditionsRecord current = new ConditionsRecord(cursor);
			JSONArray conditionIds = new JSONArray();
			for (Integer conditionId : current.getConditions()) {
				conditionIds.put(conditionId);
			}
			JSONObject forProduct = new JSONObject();
			forProduct.put("chainXProductId", current.getProductId());
			forProduct.put("skuConditionIds", conditionIds);
			res.put(forProduct);
		}

		// Return results
		cursor.close();
		return res;
	}

	/**
	 * Saves this record to the database, inserting or updating as necessary
	 * @param db Access our database
	 */
	private void save(SQLiteDatabase db) {
		// Prepare to save
		ContentValues cols = new ContentValues();
		if (getId() == null) {
			// Insert a new record
			setId(UUID.randomUUID());
			cols.put(COL_ID, getId().toString());
			cols.put(COL_CREATED_AT, BaseDatabase.parseDateTime(getCreatedAt()));
			cols.put(COL_UPDATED_AT, BaseDatabase.parseDateTime(getUpdatedAt()));
			cols.put(COL_AUDIT_ID, getAuditId().toString());
			cols.put(COL_PRODUCT_ID, getProductId());
			cols.put(COL_CONDITIONS, conditions);
			db.insert(TABLE_NAME, null, cols);
		} else {
			// Update the existing record
			cols.put(COL_UPDATED_AT, BaseDatabase.parseDateTime(getUpdatedAt()));
			cols.put(COL_CONDITIONS, conditions);
			db.update(TABLE_NAME, cols, COL_ID + "=?", new String[] { getId().toString() });
		}
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
	private void setId(UUID value) {
		id = value;
	}

	/**
	 * Gets the time at which this record was created.
	 * @return Creation time
	 */
	private Date getCreatedAt() {
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
	private Date getUpdatedAt() {
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
	private UUID getAuditId() {
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
	 * Gets the id of the product with which this record is associated
	 * @return Associated product id
	 */
	private int getProductId() {
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
	 * Gets the conditions for this combination of audit and product.
	 * @return Conditions IDs for product in audit
	 */
	public Set<Integer> getConditions() {
		// Do we have any conditions?
		if (conditions == null) {
			// No conditions
			return null;
		}
		try {
			// Convert conditions to set
			Set<Integer> res = new HashSet<>();
			JSONArray parsed = new JSONArray(conditions);
			//noinspection StatementWithEmptyBody
			for (int index = 0; index < parsed.length();
					res.add(parsed.getInt(index++)));
			return res;
		} catch (JSONException exc) {
			// Failed to parse conditions
			Log.w(LOG_TAG, "Failed to parse stored conditions", exc);
			return null;
		}
	}

	/**
	 * Sets the conditions for this combination of audit and product.
	 * @param value Condition IDs for product in audit
	 */
	private void setConditions(Set<Integer> value) {
		// Trivial case
		if (value == null) {
			conditions = null;
		} else {
			// Convert to JSON
			JSONArray conv = new JSONArray();
			for (Integer conditionId : value) {
				conv.put(conditionId);
			}
			conditions = conv.toString();
		}
	}

	/**
	 * Sets the conditions for this combination of audit and product, serialized to a JSON string.
	 * @param value Condition IDs serialized to JSON
	 */
	private void setConditions(String value) {
		conditions = value;
	}

	private static final String LOG_TAG = "ConditionsRecord";

	private static final String TABLE_NAME = "conditions";
	private static final String COL_ID = "conditions_uuid";
	private static final String COL_CREATED_AT = "created_at";
	private static final String COL_UPDATED_AT = "updated_at";
	private static final String COL_AUDIT_ID = "audit_uuid";
	private static final String COL_PRODUCT_ID = "chain_x_product_id";
	private static final String COL_CONDITIONS = "reorder_status_id"; // TODO Rename to conditions_array, update script

	private UUID id;
	private Date createdAt;
	private Date updatedAt;
	private UUID auditId;
	private int productId;
	private String conditions;
}
