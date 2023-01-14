//
//  ScanRecord.swift
//  Mobile Client
//
//  Created by Eric Ruck on 2/21/18.
//  Copyright © 2018 AuditPro. All rights reserved.
//

import Foundation


/**
 * Records a scan within an audit session for DAL.
 * @author Eric Ruck
 */
class ScanTable {
	/**
	 * Create our table in the passed database.
	 * @param db Database in which to create our table
	 */
	static func createTable(db: BaseDatabase) -> Bool {
		// Build a create table statement
		let query = "CREATE TABLE " + TABLE_NAME + " (" + COL_ID + " TEXT PRIMARY KEY, " +
				COL_AUDIT_ID + " TEXT, " +
				COL_CREATED_AT + " TEXT, " +
				COL_UPDATED_AT + " TEXT, " +
				COL_PRODUCT_ID + " INTEGER, " +
				COL_RETAIL_PRICE + " DOUBLE, " +
				COL_SALE_PRICE + " DOUBLE, " +
				COL_SCAN_DATA + " TEXT, " +
				COL_SCAN_TYPE_ID + " INTEGER, " +
				COL_PRODUCT_NAME + " TEXT, " +
				COL_BRAND_NAME + " TEXT)"
		return sqlite3_exec(db.con, query, nil, nil, nil) == SQLITE_OK
	}

	/**
	 * Updates our table to the current version if necessary
	 * @param db Connected database
	 * @param lastVersion Last version of our database
	 */
	static func updateTable(db: BaseDatabase, lastVersion: Int) -> Bool {
		return true
	}

	/**
	 * Instantiates a scan from a database cursor.
	 * @param db Database
	 * @param fromCursor Cursor
	 * @return Instantiated scan
	 */
	private static func createScan(_ db: BaseDatabase, fromCursor: OpaquePointer?) -> Scan {
		return Scan(
			id: db.column(stmt: fromCursor, uuidParam: 0)!,
			auditId: db.column(stmt: fromCursor, uuidParam: 1)!,
			createdAt: db.column(stmt: fromCursor, dateParam: 2)!,
			updatedAt: db.column(stmt: fromCursor, dateParam: 3),
			productId: db.column(stmt: fromCursor, intParam: 4)!,
			retailPrice: db.column(stmt: fromCursor, doubleParam: 5),
			salePrice: db.column(stmt: fromCursor, doubleParam: 6),
			scanData: db.column(stmt: fromCursor, stringParam: 7),
			scanTypeId: db.column(stmt: fromCursor, intParam: 8)!,
			productName: db.column(stmt: fromCursor, stringParam: 9)!,
			brandName: db.column(stmt: fromCursor, stringParam: 10)!
		)
	}

	/**
	 * Gets the scan for the product in an audit.
	 * @param db Database contains scans
	 * @param forProductId Id of desired product
	 * @param inAudit Audit contains scans
	 * @return Requested scan record or nil if not found or error
	 */
	static func getScan(_ db: BaseDatabase, forProductId: Int, inAudit: Audit) -> Scan? {
		// Prepare the query for the product scan in the audit
		let query = "SELECT " + db.formatColumns(TABLE_COLS) + " FROM " + TABLE_NAME +
			" WHERE " + COL_AUDIT_ID + "=? AND " + COL_PRODUCT_ID + "=?"
		var stmt: OpaquePointer?
		if (sqlite3_prepare_v2(db.con, query, -1, &stmt, nil) != SQLITE_OK) {
			return nil
		}

		// Bind and execute the query
		db.bind(stmt: stmt, param: 1, toString: inAudit.id.uuidString)
		db.bind(stmt: stmt, param: 2, toInt: forProductId)
		let res = (sqlite3_step(stmt) == SQLITE_ROW)
			? createScan(db, fromCursor: stmt)
			: nil
		sqlite3_finalize(stmt)
		return res
	}

	/**
	 * Gets all the scans for an audit.
	 * @param db Database contains scans
	 * @param forAudit Audit whose scans we want
	 * @return List of scans in audit or nil on error
	 */
	static func getScans(_ db: BaseDatabase, forAudit: Audit) -> [Scan]? {
		// Prepare the query for the product scan in the audit
		let query = "SELECT " + db.formatColumns(TABLE_COLS) + " FROM " + TABLE_NAME +
			" WHERE " + COL_AUDIT_ID + "=?"
		var stmt: OpaquePointer?
		if (sqlite3_prepare_v2(db.con, query, -1, &stmt, nil) != SQLITE_OK) {
			return nil
		}

		// Bind and execute the query
		var res = [Scan]()
		db.bind(stmt: stmt, param: 1, toString: forAudit.id.uuidString)
		while (sqlite3_step(stmt) == SQLITE_ROW) {
			res.append(createScan(db, fromCursor: stmt))
		}
		sqlite3_finalize(stmt)
		return res
	}

	/**
	 * Serializes the scans for an audit to an array of dictionary objects,
	 * for eventual conversion to JSON.
	 * @param db Database contains scans
	 * @param forAudit Audit whose scans we want
	 * @return Serialized scans or nil on error
	 */
	static func getJSON(_ db: BaseDatabase, forAudit: Audit) -> [[String: Any?]]? {
		// Get the scans from the database
		let scans = getScans(db, forAudit: forAudit)
		if (scans == nil) {
			// Failed to get scans to serialize
			return nil;
		}

		// Serialize the scans
		var res = [[String: Any?]]()
		for scan in scans! {
			res.append([
				"scanId": scan.id.uuidString,
				"createdAt": BaseDatabase.formatDate(scan.createdAt),
				"updatedAt": BaseDatabase.formatDate(scan.updatedAt),
				"chainXProductId": scan.productId,
				"retailPrice": scan.retailPrice,
				"salePrice": scan.salePrice,
				"scanData": scan.scanData,
				"scanTypeId": scan.scanTypeId,
				"productName": scan.productName,
				"brandName": scan.brandName
			])
		}

		// Return the serialized result
		return res;
	}

	/**
	 * Deletes the scans associated with the passed audit.
	 * @param db Database from which to delete scans
	 * @param forAudit Audit whose scans we want to delete
	 * @return Success flag
	 */
	static func delete(_ db: BaseDatabase, forAudit: Audit) -> Bool {
		let query = "DELETE FROM " + TABLE_NAME + " WHERE " + COL_AUDIT_ID + "=?"
		var stmt: OpaquePointer?
		if (sqlite3_prepare_v2(db.con, query, -1, &stmt, nil) != SQLITE_OK) {
			return false
		}
		db.bind(stmt: stmt, param: 1, toString: forAudit.id.uuidString)
		let success = sqlite3_step(stmt) == SQLITE_DONE
		sqlite3_finalize(stmt)
		return success
	}

	/**
	 * Inserts ourself into the passed database.
	 * @param db Database to receive record
	 */
	static func insert(_ db: BaseDatabase, scan: Scan) -> Bool {
		// Prepare the insert query
		let query = "INSERT INTO " + TABLE_NAME + "(" +
			db.formatColumns(TABLE_COLS) + ") VALUES (" +
			db.formatColParams(TABLE_COLS) + ")"
		var stmt: OpaquePointer?
		if (sqlite3_prepare_v2(db.con, query, -1, &stmt, nil) != SQLITE_OK) {
			return false
		}

		// Bind and execute the query
		db.bind(stmt: stmt, param: 1, toString: scan.id.uuidString)
		db.bind(stmt: stmt, param: 2, toString: scan.auditId.uuidString)
		db.bind(stmt: stmt, param: 3, toDate: scan.createdAt)
		db.bind(stmt: stmt, param: 4, toDate: scan.updatedAt)
		db.bind(stmt: stmt, param: 5, toInt: scan.productId)
		db.bind(stmt: stmt, param: 6, toDouble: scan.retailPrice)
		db.bind(stmt: stmt, param: 7, toDouble:scan.salePrice)
		db.bind(stmt: stmt, param: 8, toString:scan.scanData)
		db.bind(stmt: stmt, param: 9, toInt: scan.scanTypeId)
		db.bind(stmt: stmt, param: 10, toString: scan.productName)
		db.bind(stmt: stmt, param: 11, toString: scan.brandName)
		let success = sqlite3_step(stmt) == SQLITE_DONE
		sqlite3_finalize(stmt)
		return success
	}

	/**
	 * Updates our record in the passed database.
	 * @param db Database to update
	 */
	static func update(_ db: BaseDatabase, scan: Scan) -> Bool {
		let query = "UPDATE " + TABLE_NAME + " SET " +
			COL_AUDIT_ID + "=?, " +
			COL_CREATED_AT + "=?, " +
			COL_UPDATED_AT + "=?, " +
			COL_PRODUCT_ID + "=?, " +
			COL_RETAIL_PRICE + "=?, " +
			COL_SALE_PRICE + "=?, " +
			COL_SCAN_DATA + "=?, " +
			COL_SCAN_TYPE_ID + "=?, " +
			COL_PRODUCT_NAME + "=?, " +
			COL_BRAND_NAME + "=? " +
			" WHERE " + COL_ID + "=?"
		var stmt: OpaquePointer?
		if (sqlite3_prepare_v2(db.con, query, -1, &stmt, nil) != SQLITE_OK) {
			return false
		}

		// Bind and execute update query
		db.bind(stmt: stmt, param: 1, toString: scan.auditId.uuidString)
		db.bind(stmt: stmt, param: 2, toDate: scan.createdAt)
		db.bind(stmt: stmt, param: 3, toDate: scan.updatedAt)
		db.bind(stmt: stmt, param: 4, toInt: scan.productId)
		db.bind(stmt: stmt, param: 5, toDouble: scan.retailPrice)
		db.bind(stmt: stmt, param: 6, toDouble: scan.salePrice)
		db.bind(stmt: stmt, param: 7, toString: scan.scanData)
		db.bind(stmt: stmt, param: 8, toInt: scan.scanTypeId)
		db.bind(stmt: stmt, param: 9, toString: scan.productName)
		db.bind(stmt: stmt, param: 10, toString: scan.brandName)
		db.bind(stmt: stmt, param: 11, toString: scan.id.uuidString)
		let success = sqlite3_step(stmt) == SQLITE_DONE
		sqlite3_finalize(stmt)
		return success
	}

	private static let TABLE_NAME = "scans"
	private static let COL_ID = "audit_scan_uuid"
	private static let COL_AUDIT_ID = "audit_uuid"
	private static let COL_CREATED_AT = "created_at"
	private static let COL_UPDATED_AT = "updated_at"
	private static let COL_PRODUCT_ID = "chain_x_product_id"
	private static let COL_RETAIL_PRICE = "retail_price"
	private static let COL_SALE_PRICE = "sale_price"
	private static let COL_SCAN_DATA = "scan_data"
	private static let COL_SCAN_TYPE_ID = "scan_type_id"
	private static let COL_PRODUCT_NAME = "product_name"
	private static let COL_BRAND_NAME = "brand_name"
	private static let TABLE_COLS: [String] = [
		COL_ID,
		COL_AUDIT_ID,
		COL_CREATED_AT,
		COL_UPDATED_AT,
		COL_PRODUCT_ID,
		COL_RETAIL_PRICE,
		COL_SALE_PRICE,
		COL_SCAN_DATA,
		COL_SCAN_TYPE_ID,
		COL_PRODUCT_NAME,
		COL_BRAND_NAME
	]
}
