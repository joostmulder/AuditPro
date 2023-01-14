//
//  ReportRecord.swift
//  Mobile Client
//
//  Created by Eric Ruck on 2/21/18.
//  Copyright © 2018 AuditPro. All rights reserved.
//

import Foundation


/**
 * Interfaces one reported row in an audit with the database.
 * @author Eric Ruck
 */
class ReportTable {
	/**
	 * Create our table in the passed database.
	 * @param db Database in which to create our table
	 */
	static func createTable(db: BaseDatabase) -> Bool {
		// Build a create table statement
		let query = "CREATE TABLE " + TABLE_NAME + " (" + COL_ID + " TEXT PRIMARY KEY, " +
				COL_CREATED_AT + " TEXT, " +
				COL_UPDATED_AT + " TEXT, " +
				COL_AUDIT_ID + " TEXT, " +
				COL_SCAN_ID + " TEXT, " +
				COL_PRODUCT_ID + " INTEGER, " +
				COL_REORDER_STATUS_ID + " INTEGER)"
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
	 * Initializes instance from database cursor.
	 * @param database Database from which the report is coming
	 * @param fromCursor Cursor on a current report record
	 * @return Created record or nil if invalid
	 */
	private static func createRecord(_ db: BaseDatabase, fromCursor: OpaquePointer?) -> Report? {
		// Validate fields
		guard
			let id = db.column(stmt: fromCursor, uuidParam: 0),
			let createdAt = db.column(stmt: fromCursor, dateParam: 1),
			let updatedAt = db.column(stmt: fromCursor, dateParam: 2),
			let auditId = db.column(stmt: fromCursor, uuidParam: 3),
			let productId = db.column(stmt: fromCursor, intParam: 5),
			let reorderStatusId = db.column(stmt: fromCursor, intParam: 6)
		else {
			return nil
		}

		// Complete report instance
		let scanId = db.column(stmt: fromCursor, uuidParam: 4)
		return Report(
			id: id,
			createdAt: createdAt,
			updatedAt: updatedAt,
			auditId: auditId,
			scanId: scanId,
			productId: productId,
			reorderStatusId: reorderStatusId
		)
	}

	/**
	 * Get the report record for the product in an audit.
	 * @param db Database hold report records
	 * @param forProductId Product in audit whose report we want
	 * @param inAudit Audit whose record we want
	 * @return Report record if found else nil or not found or error
	 */
	static func getReport(_ db: BaseDatabase, forProductId: Int, inAudit: Audit) -> Report? {
		// Prepare the query for the report
		let query = "SELECT " + db.formatColumns(TABLE_COLS) +
			" FROM " + TABLE_NAME +
			" WHERE " + COL_AUDIT_ID +
			"=? AND " + COL_PRODUCT_ID + "=?"
		var stmt: OpaquePointer?
		if (sqlite3_prepare_v2(db.con, query, -1, &stmt, nil) != SQLITE_OK) {
			return nil
		}

		// Bind and execute the query
		db.bind(stmt: stmt, param: 1, toUUID: inAudit.id)
		db.bind(stmt: stmt, param: 2, toInt: forProductId)
		let res = (sqlite3_step(stmt) == SQLITE_ROW)
			? createRecord(db, fromCursor: stmt)
			: nil
		sqlite3_finalize(stmt)
		return res
	}

	/**
	 * Gets all the reports for an audit.
	 * @param db Database contains reports
	 * @param forAudit Audit whose reports we want
	 * @return List of reports in audit or nil on error
	 */
	static func getReports(_ db: BaseDatabase, forAudit: Audit) -> [Report]? {
		// Prepare the query for the reports
		let query = "SELECT " + db.formatColumns(TABLE_COLS) +
			" FROM " + TABLE_NAME +
			" WHERE " + COL_AUDIT_ID + "=?"
		var stmt: OpaquePointer?
		if (sqlite3_prepare_v2(db.con, query, -1, &stmt, nil) != SQLITE_OK) {
			return nil
		}

		// Bind and execute the query
		var res = [Report]()
		db.bind(stmt: stmt, param: 1, toUUID: forAudit.id)
		while (sqlite3_step(stmt) == SQLITE_ROW) {
			if let record = createRecord(db, fromCursor: stmt) {
				res.append(record)
			}
		}
		sqlite3_finalize(stmt)
		return res;
	}

	/**
	 * Serializes the reports for an audit to JSON.
	 * @param db Database contains reports
	 * @param forAudit Audit whose reports we want
	 * @return Serialized reports or nil on failure
	 */
	static func getJSON(_ db: BaseDatabase, forAudit: Audit) -> [[String: Any?]]? {
		// Get the reports from the database
		let reports = getReports(db, forAudit: forAudit)
		if (reports == nil) {
			// Failed to get reports
			return nil
		}

		// Serialize the reports
		var res = [[String: Any?]]()
		for report in reports! {
			// Serialize the current report
			res.append([
				"reportId": report.id?.uuidString,
				"createdAt": BaseDatabase.formatDate(report.createdAt),
				"updatedAt": BaseDatabase.formatDate(report.updatedAt),
				"scanId": (report.scanId == nil) ? nil : report.scanId!.uuidString,
				"chainXProductId": report.productId,
				"reorderStatusId": report.reorderStatusId
			])
		}

		// Return the serialized reports
		return res;
	}

	/**
	 * Deletes the reports associated with the passed audit.
	 * @param db Database from which to delete reports
	 * @param forAudit Audit whose reports we want to delete
	 * @return Success flag
	 */
	static func delete(_ db: BaseDatabase, forAudit: Audit) -> Bool {
		// Prepare the query for the reports
		let query = "DELETE FROM " + TABLE_NAME +
			" WHERE " + COL_AUDIT_ID + "=?"
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
	 * Inserts this record into the passed database.
	 * @param db Database to receive record
	 * @param report Report to insert
	 * @return Success flag
	 */
	static func insert(_ db: BaseDatabase, report: Report) -> Bool {
		// Prepare the insert query
		let query = "INSERT INTO " + TABLE_NAME + " (" +
			db.formatColumns(TABLE_COLS) + ") VALUES (" +
			db.formatColParams(TABLE_COLS) + ")"
		var stmt: OpaquePointer?
		if (sqlite3_prepare_v2(db.con, query, -1, &stmt, nil) != SQLITE_OK) {
			db.logLastError("insert report")
			return false
		}

		// Bind and execute the query
		db.bind(stmt: stmt, param: 1, toUUID: report.id ?? UUID())
		db.bind(stmt: stmt, param: 2, toDate: report.createdAt)
		db.bind(stmt: stmt, param: 3, toDate: report.updatedAt)
		db.bind(stmt: stmt, param: 4, toUUID: report.auditId)
		db.bind(stmt: stmt, param: 5, toUUID: report.scanId)
		db.bind(stmt: stmt, param: 6, toInt: report.productId)
		db.bind(stmt: stmt, param: 7, toInt: report.reorderStatusId)
		let success = sqlite3_step(stmt) == SQLITE_DONE
		sqlite3_finalize(stmt)
		return success
	}

	/**
	 * Updates this record into the passed database.
	 * @param db Database to receive record
	 * @pararm report Report to update
	 * @return Success flag
	 */
	static func update(_ db: BaseDatabase, report: Report) -> Bool {
		// Prepare the update query
		let query = "UPDATE " + TABLE_NAME + " SET " +
			COL_UPDATED_AT + "=?, " +
			COL_SCAN_ID + "=?, " +
			COL_PRODUCT_ID + "=?, " +
			COL_REORDER_STATUS_ID + "=? WHERE " +
			COL_ID + "=?"
		var stmt: OpaquePointer?
		if (sqlite3_prepare_v2(db.con, query, -1, &stmt, nil) != SQLITE_OK) {
			return false
		}

		// Bind and execute the update query
		db.bind(stmt: stmt, param: 1, toDate: report.updatedAt)
		db.bind(stmt: stmt, param: 2, toUUID: report.scanId)
		db.bind(stmt: stmt, param: 3, toInt: report.productId)
		db.bind(stmt: stmt, param: 4, toInt: report.reorderStatusId)
		db.bind(stmt: stmt, param: 5, toUUID: report.id)
		let success = sqlite3_step(stmt) == SQLITE_DONE
		sqlite3_finalize(stmt)
		return success
	}

	private static let TABLE_NAME = "reports"
	private static let COL_ID = "audit_report_uuid"
	private static let COL_CREATED_AT = "created_at"
	private static let COL_UPDATED_AT = "updated_at"
	private static let COL_AUDIT_ID = "audit_uuid"
	private static let COL_SCAN_ID = "audit_scan_uuid"
	private static let COL_PRODUCT_ID = "chain_x_product_id"
	private static let COL_REORDER_STATUS_ID = "reorder_status_id"
	private static let TABLE_COLS: [String] = [
		COL_ID,
		COL_CREATED_AT,
		COL_UPDATED_AT,
		COL_AUDIT_ID,
		COL_SCAN_ID,
		COL_PRODUCT_ID,
		COL_REORDER_STATUS_ID
	]
}

/* TODO Port Android
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
*/
