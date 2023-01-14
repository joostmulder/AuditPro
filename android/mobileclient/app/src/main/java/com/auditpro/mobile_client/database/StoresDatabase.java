/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.auditpro.mobile_client.MobileClientException;
import com.auditpro.mobile_client.entities.Chain;
import com.auditpro.mobile_client.entities.Product;
import com.auditpro.mobile_client.entities.Store;

import java.util.ArrayList;
import java.util.List;


/**
 * Provides access to our cached stores.
 * @author Eric Ruck
 */
public class StoresDatabase extends BaseDatabase {

	/**
	 * Instantiates an object to access our stores database.
	 * @param context Application context
	 */
	public StoresDatabase(Context context) {
		super(context, "stores", DB_VERSION_CURRENT);
	}

	/**
	 * Handle database creation by creating our tables.
	 * @param db Connected database
	 */
	protected void onCreateDb(SQLiteDatabase db) {
		StoreRecord.createTable(db);
		ProductRecord.createTable(db);
	}

	/**
	 * Handle database version update by applying to our tables.
	 * @param db Connected database
	 * @param lastVersion Last version
	 */
	protected void onUpdateDb(SQLiteDatabase db, int lastVersion) {
		StoreRecord.updateTable(db, lastVersion);
		ProductRecord.updateTable(db, lastVersion);
	}

	/**
	 * Indicates if there are any stores in our cache.
	 * @return Empty flag
	 * @throws MobileClientException Readable database exception
	 */
	public boolean isEmpty() throws MobileClientException {
		return StoreRecord.isEmpty(getCon());
	}

	/**
	 * Replaces the local data with results from the web service.
	 * @param stores New stores from the web service
	 * @param products New products from the web service
	 */
	public void applyRefresh(List<Store> stores, List<Product> products) throws MobileClientException {
		SQLiteDatabase db = getCon();
		try {
			// Replace stores
			db.beginTransaction();
			StoreRecord.replaceWith(db, stores);
			ProductRecord.replaceWith(db, products);
			db.setTransactionSuccessful();
		} catch (SQLiteException sqlExc) {
			// Transform the exception
			db.endTransaction();
			throw new MobileClientException(
					"Failed to replace stores in local cache.",
					sqlExc);
		} finally {
			// Complete transaction
			db.endTransaction();
		}
	}

	/**
	 * Gets all of the stores in the database.
	 * @return The stores
	 * @throws MobileClientException Readable database exception
	 */
	public List<Store> getStores() throws MobileClientException {
		return StoreRecord.getStores(getCon());
	}

	/**
	 * Gets the identified store.
	 * @param storeId Identifies the stores to get
	 * @return Identified store or null if id invalid
	 * @throws MobileClientException Readable database exception
	 */
	public Store getStore(int storeId) throws MobileClientException {
		return StoreRecord.getStore(getCon(), storeId);
	}

	/**
	 * Gets all of the products for a store.
	 * @param storeId Identifies store whose products we want
	 * @return The products for store
	 * @throws MobileClientException Readable database exception
	 */
	public List<Product> getProductsForStore(int storeId) throws MobileClientException {
		// Get the store
		Store store = StoreRecord.getStore(getCon(), storeId);
		if (store == null) {
			// No such store
			return new ArrayList<>();
		}

		// Get the products
		return getProductsForStore(store);
	}

	/**
	 * Gets all of the products for a store.
	 * @param store Store whose products we want
	 * @return The products for store
	 */
	private List<Product> getProductsForStore(Store store) {
		return ProductRecord.getProducts(getCon(), store);
	}

	/**
	 * Gets the chains.
	 * @return The chains
	 * @throws MobileClientException User readable exception
	 */
	public List<Chain> getChains() throws MobileClientException {
		return StoreRecord.getChains(getCon());
	}

	/**
	 * Gets the current version of this database.
	 * @return Current version code
	 */
	static public int getVersion() {
		return DB_VERSION_CURRENT;
	}

	@SuppressWarnings("unused")
	public static final int DB_VERSION_INIT = 1; // Initial version
	public static final int DB_VERSION_11 = 2; // Build 11, Client SKU and In Store Min/Max for products
	public static final int DB_VERSION_15 = 3; // Build 15 Added store history
	private static final int DB_VERSION_CURRENT = DB_VERSION_15;
}
