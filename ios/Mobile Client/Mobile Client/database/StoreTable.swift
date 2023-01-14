//
//  StoreTable.swift
//  Mobile Client
//
//  Created by Eric Ruck on 2/22/18.
//  Copyright © 2018 AuditPro. All rights reserved.
//

import Foundation


/**
 * Records a store in the local cache for the DAL.
 * @author Eric Ruck
 */
class StoreTable {
	/**
	 * Create our table in the passed database.
	 * @param db Database in which to create our table
	 */
	static func createTable(db: BaseDatabase) -> Bool {
		// Build a create table statement
		let query = "CREATE TABLE " + TABLE_NAME + " (" + COL_STORE_ID + " INTEGER PRIMARY KEY, " +
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
				")"
		let res = sqlite3_exec(db.con, query, nil, nil, nil)
		if (res != SQLITE_OK) {
			db.logLastError("creating table " + TABLE_NAME)
		}
		return res == SQLITE_OK
	}

	/**
	 * Updates our table to the current version if necessary
	 * @param db Connected database
	 * @param lastVersion Last version of our database
	 */
	static func updateTable(db: BaseDatabase, lastVersion: Int) -> Bool {
		if (lastVersion < StoresDatabase.DB_VERSION_2_2) {
			// Upgrade for version 2.2
			if sqlite3_exec(db.con,
					"ALTER TABLE " + TABLE_NAME + " ADD " + COL_HISTORY + " TEXT",
					nil, nil, nil) != SQLITE_OK {
				return false
			}
		}
		return true
	}

	/**
	 * Queries the stores in the database.
	 * @param db Database to query
	 * @param limit Maximum number of results
	 * @param storeId Specific store to fetch
	 * @return Stores from the database
	 */
	private static func getStores(db: BaseDatabase, limit: Int, storeId: Int) -> [Store]? {
		// Build the query
		var query = "SELECT " + db.formatColumns(TABLE_COLS) + " FROM " + TABLE_NAME;
		if (storeId > 0) {
			query += String(format:" WHERE %@=%d", COL_STORE_ID, storeId)
		}
		if (limit > 0) {
			query += String(format: " LIMIT %d", limit)
		}

		// Execute the query
		var stmt: OpaquePointer?
		if (sqlite3_prepare_v2(db.con, query, -1, &stmt, nil) != SQLITE_OK) {
			db.logLastError("get stores")
			return nil
		}

		// Get the results
		var res = [Store]()
		while (sqlite3_step(stmt) == SQLITE_ROW) {
			res.append(Store(
				clientId: Int(sqlite3_column_int(stmt, 1)),
				chainId: Int(sqlite3_column_int(stmt, 2)),
				storeId: Int(sqlite3_column_int(stmt, 0)),
				chainName: String(cString: sqlite3_column_text(stmt, 3)),
				chainCode: String(cString: sqlite3_column_text(stmt, 4)),
				storeName: String(cString: sqlite3_column_text(stmt, 5)),
				storeIdentifier: db.column(stmt: stmt, stringParam: 6),
				storeAddress: db.column(stmt: stmt, stringParam: 7),
				storeAddress2: db.column(stmt: stmt, stringParam: 8),
				storeCity: db.column(stmt: stmt, stringParam: 9),
				storeZip: db.column(stmt: stmt, stringParam: 10),
				storeLat: db.column(stmt: stmt, doubleParam: 11),
				storeLon: db.column(stmt: stmt, doubleParam: 12),
				history: db.column(stmt: stmt, stringParam: 13)
			))
		}

		// Return the query results
		sqlite3_finalize(stmt)
		return res;
	}

	/**
	 * Indicates if there are any stores in the database.
	 * @param db Database to test
	 * @return Empty flag
	 */
	static func isEmpty(db: BaseDatabase) -> Bool {
		let res = getStores(db:db, limit: 1, storeId: -1)
		return (res == nil) ? true : (res!.count == 0)
	}

	/**
	 * Gets the identified store.  Returns nil if not found.
	 * @param db Database that contains stores
	 * @param storeId Id of desired store
	 * @return Store record or nil if not found
	 */
	static func getStore(db: BaseDatabase, storeId: Int) -> Store? {
		let res = getStores(db: db, limit: 1, storeId: storeId)
		return (res == nil) ? nil : (res!.count == 0 ? nil : res![0])
	}

	/**
	 * Gets all of the stores in the database.
	 * @param db Database contains stores
	 * @return Stores from the database or nil on error
	 */
	static func getStores(db: BaseDatabase) -> [Store]? {
		return getStores(db: db, limit: 0, storeId: -1)
	}

	/**
	 * Gets the unique chains for which we have stores in our database.
	 * @param db Database contains stores (and their chains)
	 * @return List of unique chains or nil on error
	 */
	static func getChains(db: BaseDatabase) -> [Chain]? {
		// Execute the query
		var stmt: OpaquePointer?
		let query = "SELECT DISTINCT " + COL_CHAIN_ID + ", " + COL_CHAIN_NAME + ", " +
			COL_CHAIN_CODE + " FROM " + TABLE_NAME;
		if (sqlite3_prepare_v2(db.con, query, -1, &stmt, nil) != SQLITE_OK) {
			// Failed to prepare query
			db.logLastError("get chains")
			return nil
		}

		// Get the results
		var res = [Chain]()
		while(sqlite3_step(stmt) == SQLITE_ROW) {
			res.append(Chain(
				chainId: Int(sqlite3_column_int(stmt, 0)),
				chainName: String(cString:sqlite3_column_text(stmt, 1)),
				chainCode: String(cString:sqlite3_column_text(stmt, 2))
			))
		}

		// Return the results
		sqlite3_finalize(stmt);
		return res;
	}

	/**
	 * Replaces all of the store records with the passed data.
	 * @param db Database to replace
	 * @param stores Stores to save to database
	 */
	static func replaceWith(db: BaseDatabase, stores: [Store]) -> Bool {
		// Delete the current records
		let delQuery = "DELETE FROM " + TABLE_NAME + " WHERE " + COL_CLIENT_ID + " > 0"; // TODO DEBUGGING disallows trunc optimization within transaction
		if (sqlite3_exec(db.con, delQuery, nil, nil, nil) != SQLITE_OK) {
			db.logLastError("replace stores delete")
			return false
		}

		// Insert the new records
		var stmt: OpaquePointer?
		let insQuery = "INSERT INTO " + TABLE_NAME + "(" +
			db.formatColumns(TABLE_COLS) +
			") VALUES (" + db.formatColParams(TABLE_COLS) + ")"
		for store in stores {
			// Insert the current store
			if (sqlite3_prepare_v2(db.con, insQuery, -1, &stmt, nil) != SQLITE_OK) {
				db.logLastError("replace stores insert prepare")
				return false
			}
			db.bind(stmt:stmt, param:1, toInt:store.storeId)
			db.bind(stmt:stmt, param:2, toInt:store.clientId)
			db.bind(stmt:stmt, param:3, toInt:store.chainId)
			db.bind(stmt:stmt, param:4, toString:store.chainName)
			db.bind(stmt:stmt, param:5, toString:store.chainCode)
			db.bind(stmt:stmt, param:6, toString:store.storeName)
			db.bind(stmt:stmt, param:7, toString:store.storeIdentifier)
			db.bind(stmt:stmt, param:8, toString:store.storeAddress)
			db.bind(stmt:stmt, param:9, toString:store.storeAddress2)
			db.bind(stmt:stmt, param:10, toString:store.storeCity)
			db.bind(stmt:stmt, param:11, toString:store.storeZip)
			db.bind(stmt:stmt, param:12, toDouble:store.storeLat)
			db.bind(stmt:stmt, param:13, toDouble:store.storeLon)
			db.bind(stmt:stmt, param:14, toString:store.history)
			let success = sqlite3_step(stmt) == SQLITE_DONE
			sqlite3_finalize(stmt)
			if (!success) {
				db.logLastError("replace stores insert complete")
				return false
			}
		}

		// All stores saved
		return true
	}

	static let TABLE_NAME = "stores"
	private static let COL_STORE_ID = "store_id"
	private static let COL_CLIENT_ID = "client_id"
	private static let COL_CHAIN_ID = "chain_id"
	private static let COL_CHAIN_NAME = "chain_name"
	private static let COL_CHAIN_CODE = "chain_code"
	private static let COL_STORE_NAME = "store_name"
	private static let COL_STORE_IDENTIFIER = "store_identifier"
	private static let COL_STORE_ADDR = "store_addr"
	private static let COL_STORE_ADDR2 = "store_addr2"
	private static let COL_STORE_CITY = "store_city"
	private static let COL_STORE_ZIP = "store_zip"
	private static let COL_STORE_LAT = "store_lat"
	private static let COL_STORE_LONG = "store_lon"
	private static let COL_HISTORY = "history"

	private static let TABLE_COLS: [String] = [
		COL_STORE_ID, COL_CLIENT_ID, COL_CHAIN_ID, COL_CHAIN_NAME,
		COL_CHAIN_CODE, COL_STORE_NAME, COL_STORE_IDENTIFIER,
		COL_STORE_ADDR, COL_STORE_ADDR2, COL_STORE_CITY, COL_STORE_ZIP,
		COL_STORE_LAT, COL_STORE_LONG, COL_HISTORY]
}
