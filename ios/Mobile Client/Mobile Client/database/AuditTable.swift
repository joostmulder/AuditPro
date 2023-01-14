//
//  AuditTable.swift
//  Mobile Client
//
//  Created by Eric Ruck on 2/21/18.
//  Copyright © 2018 AuditPro. All rights reserved.
//

import Foundation


/**
 * Provides audit session object for DAL.
 * @author Eric Ruck
 */
class AuditTable {
	/**
	 * Create our table in the passed database.
	 * @param db Database in which to create our table
	 * @return Success flag
	 */
	static func createTable(db: BaseDatabase) -> Bool {
		// Build a create table statement
		let query = "CREATE TABLE " + TABLE_NAME + " (" + COL_ID + " TEXT PRIMARY KEY, " +
				COL_USER_ID + " INTEGER, " +
				COL_STORE_ID + " INTEGER, " +
				COL_STORE_DESCR + " TEXT, " +
				COL_AUDIT_STARTED_AT + " TEXT, " +
				COL_AUDIT_ENDED_AT + " TEXT, " +
				COL_AUDIT_TYPE_ID + " INTEGER, " +
				COL_LATITUDE_AT_START + " DOUBLE, " +
				COL_LONGITUDE_AT_START + " DOUBLE, " +
				COL_LATITUDE_AT_END + " DOUBLE, " +
				COL_LONGITUDE_AT_END + " DOUBLE)"
		return sqlite3_exec(db.con, query, nil, nil, nil) == SQLITE_OK
	}

	/**
	 * Updates our table to the current version if necessary
	 * @param db Connected database
	 * @param lastVersion Last version of our database
	 * @return Success flag
	 */
	static func updateTable(db: BaseDatabase, lastVersion: Int) -> Bool {
		return true
	}

	/**
	 * Inserts ourself into the database.
	 * @param db Database to insert
	 * @param audit Audit to insert
	 * @return Success flag
	 */
	static func insert(_ db: BaseDatabase, audit: Audit) -> Bool {
		// Prepare the insert query
		let query = "INSERT INTO " + TABLE_NAME + " (" +
			db.formatColumns(TABLE_COLS) + ") VALUES (" +
			db.formatColParams(TABLE_COLS) + ")"
		var stmt: OpaquePointer?
		if (sqlite3_prepare_v2(db.con, query, -1, &stmt, nil) != SQLITE_OK) {
			return false
		}

		// Bind the fields and execute
		db.bind(stmt: stmt, param: 1, toString: audit.id.uuidString)
		db.bind(stmt: stmt, param: 2, toInt: audit.userId)
		db.bind(stmt: stmt, param: 3, toInt: audit.storeId)
		db.bind(stmt: stmt, param: 4, toString: audit.storeDescr)
		db.bind(stmt: stmt, param: 5, toDate: audit.auditStartedAt)
		db.bind(stmt: stmt, param: 6, toDate: audit.auditEndedAt)
		db.bind(stmt: stmt, param: 7, toInt: audit.auditTypeId)
		db.bind(stmt: stmt, param: 8, toDouble: audit.latitudeAtStart)
		db.bind(stmt: stmt, param: 9, toDouble: audit.longitudeAtStart)
		db.bind(stmt: stmt, param: 10, toDouble: audit.latitudeAtEnd)
		db.bind(stmt: stmt, param: 11, toDouble: audit.longitudeAtEnd)
		let success = sqlite3_step(stmt) == SQLITE_DONE
		sqlite3_finalize(stmt)
		if (!success) {
			db.logLastError("insert new audit")
		}
		return success
	}

	/**
	 * Gets the number of completed audits for the passed user in the passed database.
	 * @param db Database to query
	 * @param userId Identifies user
	 * @return Count of completed audits for user or nil on error
	 */
	static func getCount(_ db: BaseDatabase, userId: Int, isCompleted: Bool) -> Int? {
		// Query the database for the count
		let query = "SELECT COUNT(*) FROM " + TABLE_NAME +
			" WHERE " + COL_USER_ID + "=? AND " + COL_AUDIT_ENDED_AT + " IS " +
			(isCompleted ? "NOT NULL" : "NULL")
		var stmt: OpaquePointer?
		if (sqlite3_prepare_v2(db.con, query, -1, &stmt, nil) != SQLITE_OK) {
			return nil
		}

		// Bind and execute the query
		db.bind(stmt: stmt, param: 1, toInt: userId)
		let count = (sqlite3_step(stmt) != SQLITE_ROW)
			? nil
			: Int(sqlite3_column_int(stmt, 0))
		sqlite3_finalize(stmt)
		return count;
	}

	/**
	 * Initializes a new Audit instance from a cursor in which all of the fields
	 * are selected in the expected default order.
	 * @param db Working database
	 * @param fromCursor Cursor on record to instantiate
	 * @return New audit instance
	 */
	private static func audit(_ db: BaseDatabase, fromCursor: OpaquePointer?) -> Audit {
		return Audit(
			id: db.column(stmt: fromCursor, uuidParam: 0)!,
			userId: db.column(stmt: fromCursor, intParam: 1)!,
			storeId: db.column(stmt: fromCursor, intParam: 2)!,
			storeDescr: db.column(stmt: fromCursor, stringParam: 3)!,
			auditStartedAt: db.column(stmt: fromCursor, dateParam: 4)!,
			auditEndedAt: db.column(stmt: fromCursor, dateParam: 5),
			auditTypeId: db.column(stmt: fromCursor, intParam: 6)!,
			latitudeAtStart: db.column(stmt: fromCursor, doubleParam:7),
			longitudeAtStart: db.column(stmt: fromCursor, doubleParam: 8),
			latitudeAtEnd: db.column(stmt: fromCursor, doubleParam: 9),
			longitudeAtEnd: db.column(stmt: fromCursor, doubleParam: 10)
		)
	}

	/**
	 * Gets the completed audits for a user in the database.
	 * @param db Database to query
	 * @param userId User whose audits we want
	 * @return List of completed audits, may be empty
	 */
	static func getCompleteAudits(_ db: BaseDatabase, userId: Int) -> [Audit]? {
		// Prepare the query for the audits
		let query = "SELECT " + db.formatColumns(TABLE_COLS) + " FROM " + TABLE_NAME +
			" WHERE " + COL_USER_ID + "=? AND " + COL_AUDIT_ENDED_AT + " IS NOT NULL"
		var stmt: OpaquePointer?
		if (sqlite3_prepare_v2(db.con, query, -1, &stmt, nil) != SQLITE_OK) {
			return nil
		}

		// Bind and execute the query
		var res = [Audit]()
		db.bind(stmt: stmt, param: 1, toInt: userId)
		while (sqlite3_step(stmt) == SQLITE_ROW) {
			res.append(audit(db, fromCursor:stmt))
		}

		// Return the results
		sqlite3_finalize(stmt)
		return res
	}

	/**
	 * Gets the audit in progress for the user or null if none.
	 * @param db Database where audits live
	 * @param userId User id whose open audit we want
	 * @return Open audit or null
	 */
	static func getAudit(_ db: BaseDatabase, userId: Int) -> Audit? {
		// Prepare the query for the audits
		let query = "SELECT " + db.formatColumns(TABLE_COLS) + " FROM " + TABLE_NAME +
			" WHERE " + COL_USER_ID + "=? AND " + COL_AUDIT_ENDED_AT + " IS NULL"
		var stmt: OpaquePointer?
		if (sqlite3_prepare_v2(db.con, query, -1, &stmt, nil) != SQLITE_OK) {
			return nil
		}

		// Query for the audit
		db.bind(stmt: stmt, param: 1, toInt: userId)
		let res = (sqlite3_step(stmt) == SQLITE_ROW)
			? audit(db, fromCursor: stmt)
			: nil
		sqlite3_finalize(stmt)
		return res
	}

	/**
	 * Deletes the passed audit.
	 * @param db Database from which to delete audit
	 * @param forAudit Audit to delete
	 * @return Success flag
	 */
	static func delete(_ db: BaseDatabase, forAudit: Audit) -> Bool {
		// Prepare the query
		let query = "DELETE FROM " + TABLE_NAME + " WHERE " + COL_ID + "=?"
		var stmt: OpaquePointer?
		if (sqlite3_prepare_v2(db.con, query, -1, &stmt, nil) != SQLITE_OK) {
			return false
		}

		// Bind and execute the query
		db.bind(stmt: stmt, param: 1, toUUID: forAudit.id)
		let success = sqlite3_step(stmt) == SQLITE_DONE
		sqlite3_finalize(stmt)
		return success;
	}

	/**
	 * Applies updates in this record to the database.
	 * @param db Database to which the updates will be applied
	 * @param audit Updated audit to save
	 * @return Success flag
	 */
	static func update(_ db: BaseDatabase, audit: Audit) -> Bool {
		// Prepare the update query
		let query = "UPDATE " + TABLE_NAME + " SET " +
			COL_AUDIT_ENDED_AT + "=?, " +
			COL_LATITUDE_AT_END + "=?, " +
			COL_LONGITUDE_AT_END + "=? " +
			"WHERE " + COL_ID + "=?"
		var stmt: OpaquePointer?
		if (sqlite3_prepare_v2(db.con, query, -1, &stmt, nil) != SQLITE_OK) {
			return false
		}

		// Bind and execute the query
		db.bind(stmt: stmt, param: 1, toDate: audit.auditEndedAt)
		db.bind(stmt: stmt, param: 2, toDouble: audit.latitudeAtEnd)
		db.bind(stmt: stmt, param: 3, toDouble: audit.longitudeAtEnd)
		db.bind(stmt: stmt, param: 4, toString: audit.id.uuidString)
		let success = sqlite3_step(stmt) == SQLITE_DONE
		sqlite3_finalize(stmt)
		return success
	}

	private static let TABLE_NAME = "audits"
	private static let COL_ID = "audit_uuid"
	private static let COL_USER_ID = "user_id"
	private static let COL_STORE_ID = "store_id"
	private static let COL_STORE_DESCR = "store_descr"
	private static let COL_AUDIT_STARTED_AT = "audit_started_at"
	private static let COL_AUDIT_ENDED_AT = "audit_ended_at"
	private static let COL_AUDIT_TYPE_ID = "audit_type_id"
	private static let COL_LATITUDE_AT_START = "latitude_at_start"
	private static let COL_LONGITUDE_AT_START = "longitude_at_start"
	private static let COL_LATITUDE_AT_END = "latitude_at_end"
	private static let COL_LONGITUDE_AT_END = "longitude_at_end"
	private static let TABLE_COLS: [String] = [
		COL_ID,
		COL_USER_ID,
		COL_STORE_ID,
		COL_STORE_DESCR,
		COL_AUDIT_STARTED_AT,
		COL_AUDIT_ENDED_AT,
		COL_AUDIT_TYPE_ID,
		COL_LATITUDE_AT_START,
		COL_LONGITUDE_AT_START,
		COL_LATITUDE_AT_END,
		COL_LONGITUDE_AT_END]
}
