//
//  StoresDatabase.swift
//  Mobile Client
//
//  Created by Eric Ruck on 2/22/18.
//  Copyright © 2018 AuditPro. All rights reserved.
//

import Foundation


/**
 * Provides access to our cached stores.
 * @author Eric Ruck
 */
class StoresDatabase : BaseDatabase {
	static let DB_NAME = "stores"
	static let DB_VERSION_INIT = 1; // Initial version
	static let DB_VERSION_11 = 2; // Build 11, Client SKU and In Store Min/Max for products
	static let DB_VERSION_2_2 = 3;
	static let DB_VERSION_CURRENT = DB_VERSION_2_2;

	/**
	 * Convenience accessor for our current version.
	 */
	var version: Int {
		return StoresDatabase.DB_VERSION_CURRENT;
	}

	/**
	 * Instantiates an object to access our stores database.
	 */
	init?() {
		// TODO CRAP this isn't good...well, it's not horrible but...we should do an update fix
		//    We need our own DB_NAME and DB_VERSION_CURRENT constants...
		super.init(name: AuditDatabase.DB_NAME, version: AuditDatabase.DB_VERSION_CURRENT)
	}

	/**
	 * Delegates table creation to the subclass.
	 * @return Success flag
	 */
	override func onCreateDatabase() -> Bool {
		var success = StoreTable.createTable(db: self)
		if (success) {
			success = ProductTable.createTable(db: self)
		}
		return success
	}

	/**
	 * Handles table update event from the parent database.
	 * @param lastVersion Last database version of our current tables
	 * @return Success flag
	 */
	override func onUpdateDatabase(lastVersion: Int) -> Bool {
		var success = StoreTable.updateTable(db: self, lastVersion: lastVersion)
		if (success) {
			success = ProductTable.updateTable(db: self, lastVersion: lastVersion)
		}
		return success
	}

	/**
	 * Verifies the database is in the current state.  Note that there appears
	 * to be a bug, where the database version is remembered even after the app
	 * is uninstalled.  Might be just a simulator thing but either way I need
	 * a workaround.
	 */
	override func onVerifyDatabase() -> Bool {
		var success = true
		if (!tableExists(StoreTable.TABLE_NAME)) {
			success = StoreTable.createTable(db: self)
		}
		if (success && !tableExists(ProductTable.TABLE_NAME)) {
			success = ProductTable.createTable(db: self)
		}
		return success
	}

	/**
	 * Indicates if there are any stores in our cache.
	 * @return Empty flag
	 */
	func isEmpty() -> Bool {
		return StoreTable.isEmpty(db: self)
	}

	/**
	 * Replaces the local data with results from the web service.
	 * @param stores New stores from the web service
	 * @param products New products from the web service
	 * @return Success flag
	 */
	func applyRefresh(stores: [Store], products: [Product]) -> Bool {
		// Start an update transaction
		if (sqlite3_exec(con, "BEGIN TRANSACTION", nil, nil, nil) != SQLITE_OK) {
			logLastError("apply refresh begin")
			return false
		}

		// Execute the updates within the transaction
		var success = StoreTable.replaceWith(db: self, stores: stores)
		if (success) {
			success = ProductTable.replaceWith(db: self, products: products)
		}

		// Did we succeed?
		if (success) {
			// Yes, commit
			success = sqlite3_exec(con, "COMMIT TRANSACTION", nil, nil, nil) == SQLITE_OK
		}
		if (!success) {
			// Failed, roll back
			logLastError("apply refresh commit")
			sqlite3_exec(con, "ROLLBACK TRANSACTION", nil, nil, nil)
		}
		return success;
	}

	/**
	 * Gets all of the stores in the database.
	 * @return The stores or nil on failure
	 */
	func getStores() -> [Store]? {
		return StoreTable.getStores(db: self)
	}

	/**
	 * Gets the identified store.
	 * @param storeId Identifies the stores to get
	 * @return Identified store or nil if id invalid or other failure
	 */
	func getStore(_ storeId: Int) -> Store? {
		return StoreTable.getStore(db: self, storeId: storeId)
	}

	/**
	 * Gets all of the products for a store.
	 * @param storeId Identifies store whose products we want
	 * @return The products for store or nil on error or none
	 */
	func getProductsForStore(storeId: Int) -> [Product]? {
		// Get the store
		let store = StoreTable.getStore(db: self, storeId: storeId)
		if (store == nil) {
			// No such store
			return [Product]()
		}

		// Get the products
		return getProductsForStore(store: store!)
	}

	/**
	 * Gets all of the products for a store.
	 * @param store Store whose products we want
	 * @return The products for store or nil on error
	 */
	private func getProductsForStore(store: Store) -> [Product]? {
		return ProductTable.getProducts(db: self, store: store)
	}

	/**
	 * Gets the chains.
	 * @return The chains or nil on error
	 */
	func getChains() -> [Chain]? {
		return StoreTable.getChains(db: self)
	}
}
