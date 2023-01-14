/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.auditpro.mobile_client.MobileClientException;
import com.auditpro.mobile_client.entities.AuditHistory;
import com.auditpro.mobile_client.entities.Chain;
import com.auditpro.mobile_client.entities.Store;

import java.util.ArrayList;
import java.util.List;


/**
 * Records a store in the local cache for the DAL.
 * It made sense when this was originally attributed for the SQL ORM in C#, but maybe not now.
 * Really same question for all the *Record classes.
 * @author Eric Ruck
 */
public class StoreRecord {

	/**
	 * Initializes default instance with empty values.
	 */
	private StoreRecord() {
		setClientId(-1);
		setChainId(-1);
		setChainName(null);
		setChainCode(null);
		setStoreId(-1);
		setStoreName(null);
		setStoreIdentifier(null);
		setStoreAddress(null);
		setStoreAddress2(null);
		setStoreCity(null);
		setStoreZip(null);
		setStoreLat(null);
		setStoreLon(null);
		setHistory(null);
	}

	/**
	 * Create our table in the passed database.
	 * @param db Database in which to create our table
	 */
	static void createTable(SQLiteDatabase db) {
		// Build a create table statement
		String st = "CREATE TABLE " + TABLE_NAME + " (" + COL_STORE_ID + " INTEGER PRIMARY KEY, " +
				COL_CLIENT_ID + " INTEGER, " +
				COL_CHAIN_ID + " INTEGER, " +
				COL_CHAIN_NAME + " TEXT, " +
				COL_CHAIN_CODE + " TEXT, " +
				COL_STORE_NAME + " TEXT, " +
				COL_STORE_IDENTIFIER + " TEXT, " +
				COL_STORE_ADDR + " TEXT, " +
				COL_STORE_ADDR2 + " TEXT, " +
				COL_STORE_CITY + " TEXT, " +
				COL_STORE_ZIP + " TEXT, " +
				COL_STORE_LAT + " DOUBLE, " +
				COL_STORE_LONG + " DOUBLE, " +
				COL_HISTORY + " TEXT" +
				")";
		// Execute it
		db.execSQL(st);
	}

	/**
	 * Updates our table to the current version if becessary
	 * @param db Connected database
	 * @param lastVersion Last version of our database
	 */
	static void updateTable(SQLiteDatabase db, int lastVersion) {
		// No updates as of now
		if (lastVersion < StoresDatabase.DB_VERSION_15) {
			// Add history field
			db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD " + COL_HISTORY +  " TEXT");
		}
	}

	/**
	 * Queries the stores in the database.
	 * @param db Database to query
	 * @param limit Maximum number of results
	 * @param storeId Specific store to fetch
	 * @return Stores from the database
	 * @throws MobileClientException Readable database exception
	 */
	@SuppressLint("DefaultLocale")
	private static List<Store> getStores(SQLiteDatabase db, int limit, int storeId) throws MobileClientException {
		// Build the query
		String query = "SELECT * FROM " + TABLE_NAME;
		if (storeId > 0) {
			query += String.format(" WHERE %s=%d", COL_STORE_ID, storeId);
		}
		if (limit > 0) {
			query += String.format(" LIMIT %d", limit);
		}

		// Execute the query
		try (Cursor cursor = db.rawQuery(query, null)) {

			// Extract the results
			int idxStoreId = -1;
			int idxClientId = -1;
			int idxChainId = -1;
			int idxChainName = -1;
			int idxChainCode = -1;
			int idxStoreName = -1;
			int idxStoreIdentifier = -1;
			int idxStoreAddr = -1;
			int idxStoreAddr2 = -1;
			int idxStoreCity = -1;
			int idxStoreZip = -1;
			int idxStoreLat = -1;
			int idxStoreLong = -1;
			int idxHistory = -1;
			ArrayList<Store> res = new ArrayList<>();
			while (cursor.moveToNext()) {
				if (idxStoreId < 0) {
					// Get the field indices from the cursor
					idxStoreId = cursor.getColumnIndex(COL_STORE_ID);
					idxClientId = cursor.getColumnIndex(COL_CLIENT_ID);
					idxChainId = cursor.getColumnIndex(COL_CHAIN_ID);
					idxChainName = cursor.getColumnIndex(COL_CHAIN_NAME);
					idxChainCode = cursor.getColumnIndex(COL_CHAIN_CODE);
					idxStoreName = cursor.getColumnIndex(COL_STORE_NAME);
					idxStoreIdentifier = cursor.getColumnIndex(COL_STORE_IDENTIFIER);
					idxStoreAddr = cursor.getColumnIndex(COL_STORE_ADDR);
					idxStoreAddr2 = cursor.getColumnIndex(COL_STORE_ADDR2);
					idxStoreCity = cursor.getColumnIndex(COL_STORE_CITY);
					idxStoreZip = cursor.getColumnIndex(COL_STORE_ZIP);
					idxStoreLat = cursor.getColumnIndex(COL_STORE_LAT);
					idxStoreLong = cursor.getColumnIndex(COL_STORE_LONG);
					idxHistory = cursor.getColumnIndex(COL_HISTORY);
				}

				// Populate a new record
				StoreRecord record = new StoreRecord();
				record.setStoreId(cursor.getInt(idxStoreId));
				record.setClientId(cursor.getInt(idxClientId));
				record.setChainId(cursor.getInt(idxChainId));
				record.setChainName(cursor.getString(idxChainName));
				record.setChainCode(cursor.getString(idxChainCode));
				record.setStoreName(cursor.getString(idxStoreName));
				record.setStoreIdentifier(cursor.getString(idxStoreIdentifier));
				record.setStoreAddress(cursor.getString(idxStoreAddr));
				record.setStoreAddress2(cursor.getString(idxStoreAddr2));
				record.setStoreCity(cursor.getString(idxStoreCity));
				record.setStoreZip(cursor.getString(idxStoreZip));
				record.setStoreLat(cursor.getDouble(idxStoreLat));
				record.setStoreLon(cursor.getDouble(idxStoreLong));
				if ((idxHistory >= 0) && !cursor.isNull(idxHistory)) {
					record.setHistory(cursor.getString(idxHistory));
				}

				// Add the store to the results
				res.add(new Store(record));
			}

			// Return the query results
			return res;
		} catch(SQLiteException exc) {
			// Failed to query stores
			throw new MobileClientException("Failed to query stores from the database", exc);
		}
	}

	/**
	 * Indicates if there are any stores in the database
	 * @param db Database to test
	 * @return Empty flag
	 * @throws MobileClientException Readable database exception
	 */
	static boolean isEmpty(SQLiteDatabase db) throws MobileClientException {
		return getStores(db, 1, -1).size() == 0;
	}

	/**
	 * Gets the identified store.  Returns null if not found.
	 * @param db Database that contains stores
	 * @param storeId Id of desired store
	 * @return Store record or null if not found
	 * @throws MobileClientException Readable database exception
	 */
	static Store getStore(SQLiteDatabase db, int storeId) throws MobileClientException {
		List<Store> res = getStores(db, 1, storeId);
		return res.size() > 0 ? res.get(0) : null;
	}

	/**
	 * Gets all of the stores in the database.
	 * @param db Database contains stores
	 * @return Stores from the database
	 * @throws MobileClientException Readable database exception
	 */
	static List<Store> getStores(SQLiteDatabase db) throws MobileClientException {
		return getStores(db, 0, -1);
	}

	/**
	 * Gets the unique chains for which we have stores in our database.
	 * @param db Database contains stores (and their chains)
	 * @return List of unique chains
	 * @throws MobileClientException Database access failed
	 */
	static List<Chain> getChains(SQLiteDatabase db) throws MobileClientException {
		// Execute the query
		String query = "SELECT DISTINCT " + COL_CHAIN_ID + ", " + COL_CHAIN_NAME + ", " +
				COL_CHAIN_CODE + " FROM " + TABLE_NAME;
		try (Cursor cursor = db.rawQuery(query, null)) {
			// Extract the results
			ArrayList<Chain> res = new ArrayList<>();
			while (cursor.moveToNext()) {
				// Add the chain to the results
				res.add(new Chain(cursor.getInt(0), cursor.getString(1), cursor.getString(2)));
			}

			// Return the query results
			return res;
		} catch (SQLiteException exc) {
			throw new MobileClientException("Failed to get chains from the database", exc);
		}
	}

	/**
	 * Replaces all of the store records with the passed data.
	 * @param db Database to replace
	 * @param stores Stores to save to database
	 */
	static void replaceWith(SQLiteDatabase db, List<Store> stores) {
		// Wipe all the records
		db.delete(TABLE_NAME, null, null);
		for(Store store : stores) {
			String history;
			try {
				// Convert the history for the database
				history = AuditHistory.toString(store.getHistory());
			} catch (MobileClientException e) {
				history = "";
			}

			// Insert the current record
			ContentValues record = new ContentValues();
			record.put(COL_STORE_ID, store.getStoreId());
			record.put(COL_CLIENT_ID, store.getClientId());
			record.put(COL_CHAIN_ID, store.getChainId());
			record.put(COL_CHAIN_NAME, store.getChainName());
			record.put(COL_CHAIN_CODE, store.getChainCode());
			record.put(COL_STORE_NAME, store.getStoreName());
			record.put(COL_STORE_IDENTIFIER, store.getStoreIdentifier());
			record.put(COL_STORE_ADDR, store.getStoreAddress());
			record.put(COL_STORE_ADDR2, store.getStoreAddress2());
			record.put(COL_STORE_CITY, store.getStoreCity());
			record.put(COL_STORE_ZIP, store.getStoreZip());
			record.put(COL_STORE_LAT, store.getStoreLat());
			record.put(COL_STORE_LONG, store.getStoreLon());
			record.put(COL_HISTORY, history);
			db.insert(TABLE_NAME, null, record);
		}
	}

	public int getStoreId() {
		return storeId;
	}

	private void setStoreId(int value) {
		storeId = value;
	}

	public int getClientId() {
		return clientId;
	}

	private void setClientId(int value) {
		clientId = value;
	}

	public int getChainId() {
		return chainId;
	}

	private void setChainId(int value) {
		chainId = value;
	}

	public String getChainName() {
		return chainName;
	}

	private void setChainName(String value) {
		chainName = value;
	}

	public String getChainCode() {
		return chainCode;
	}

	private void setChainCode(String value) {
		chainCode = value;
	}

	public String getStoreName() {
		return storeName;
	}

	private void setStoreName(String value) {
		storeName = value;
	}

	public String getStoreIdentifier() {
		return storeIdentifier;
	}

	private void setStoreIdentifier(String value) {
		storeIdentifier = value;
	}

	public String getStoreAddress() {
		return storeAddress;
	}

	private void setStoreAddress(String value) {
		storeAddress = value;
	}

	public String getStoreAddress2() {
		return storeAddress2;
	}

	private void setStoreAddress2(String value) {
		storeAddress2 = value;
	}

	public String getStoreCity() {
		return storeCity;
	}

	private void setStoreCity(String value) {
		storeCity = value;
	}

	public String getStoreZip() {
		return storeZip;
	}

	private void setStoreZip(String value) {
		storeZip = value;
	}

	public Double getStoreLat() {
		return storeLat;
	}

	private void setStoreLat(Double value){
		storeLat = value;
	}

	public Double getStoreLon() {
		return storeLon;
	}

	private void setStoreLon(Double value) {
		storeLon = value;
	}

	public String getHistory() {
		return history;
	}

	private void setHistory(String value) {
		history = value;
	}

	private int storeId;
	private int clientId;
	private int chainId;
	private String chainName;
	private String chainCode;
	private String storeName;
	private String storeIdentifier;
	private String storeAddress;
	private String storeAddress2;
	private String storeCity;
	private String storeZip;
	private Double storeLat;
	private Double storeLon;
	private String history;

	private static final String TABLE_NAME = "stores";
	private static final String COL_STORE_ID ="store_id";
	private static final String COL_CLIENT_ID = "client_id";
	private static final String COL_CHAIN_ID = "chain_id";
	private static final String COL_CHAIN_NAME = "chain_name";
	private static final String COL_CHAIN_CODE = "chain_code";
	private static final String COL_STORE_NAME = "store_name";
	private static final String COL_STORE_IDENTIFIER = "store_identifier";
	private static final String COL_STORE_ADDR = "store_addr";
	private static final String COL_STORE_ADDR2 = "store_addr2";
	private static final String COL_STORE_CITY = "store_city";
	private static final String COL_STORE_ZIP = "store_zip";
	private static final String COL_STORE_LAT = "store_lat";
	private static final String COL_STORE_LONG = "store_lon";
	private static final String COL_HISTORY = "history";
}
