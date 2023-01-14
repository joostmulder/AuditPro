//
//  ConditionsTable.swift
//  Mobile Client
//
//  Created by Eric Ruck on 7/19/18.
//  Copyright © 2018 AuditPro. All rights reserved.
//

import Foundation


/**
 * Represents the table in which a record stores one set of SKU conditions for
 * a product in an audit.
 * @author Eric Ruck
 */
class ConditionsTable {
	/**
	 * Create our table in the passed database.
	 * @param db Database in which to create our table
	 * @return Success flag
	 */
	static func createTable(db: BaseDatabase) -> Bool {
		// Build and execute the create table statement
		let st = "CREATE TABLE " + TABLE_NAME + " (" + COL_ID + " TEXT PRIMARY KEY, " +
				COL_CREATED_AT + " TEXT, " +
				COL_UPDATED_AT + " TEXT, " +
				COL_AUDIT_ID + " TEXT, " +
				COL_PRODUCT_ID + " INTEGER, " +
				COL_CONDITIONS + " TEXT)";
		return sqlite3_exec(db.con, st, nil, nil, nil) == SQLITE_OK
	}

	/**
	 * Updates our table to the current version if necessary
	 * @param db Connected database
	 * @param lastVersion Last version of our database
	 * @return Success flag
	 */
	static func updateTable(db: BaseDatabase, lastVersion: Int) -> Bool {
		if (lastVersion < AuditDatabase.DB_VERSION_2_2) {
			// Create our table as of this version
			return createTable(db: db)
		}
		return true
	}

	/**
	 * Get the conditions set for the product in an audit.
	 * @param db Database hold conditions records
	 * @param audit Audit whose record we want
	 * @param productId Product in audit whose conditions we want
	 * @return Conditions set if found else null
	 */
	static func getConditions(db: BaseDatabase, audit: Audit, productId: Int) -> Set<Int>? {
		// Prepare the query for the conditinos
		let query = "SELECT " + COL_CONDITIONS + " FROM " + TABLE_NAME +
			" WHERE " + COL_AUDIT_ID + "=? and " + COL_PRODUCT_ID + "=?"
		var stmt: OpaquePointer?
		if (sqlite3_prepare_v2(db.con, query, -1, &stmt, nil) != SQLITE_OK) {
			return nil
		}

		// Bind and execute the query
		var res: Set<Int>? = nil
		db.bind(stmt: stmt, param: 1, toUUID: audit.id)
		db.bind(stmt: stmt, param: 2, toInt: productId)
		if (sqlite3_step(stmt) == SQLITE_ROW) {
			do {
				if
					let parse = db.column(stmt: stmt, stringParam: 0),
					let data = parse.data(using: .utf8),
					let array = try JSONSerialization.jsonObject(with: data, options: []) as? [Int] {
					res = Set<Int>(array)
				}
			} catch {
				// Failed to parse
			}
		}

		// Return the results
		sqlite3_finalize(stmt)
		return (res?.count ?? 0) > 0 ? res : nil
	}

	/**
	 * Serializes the conditions for every product in an audit to JSON.
	 * @param db Database contains reports
	 * @param audit Audit whose reports we want
	 * @return Serialized reports
	 * @throws JSONException Unexpected serialization error
	 */
	static func getJSON(db: BaseDatabase, audit: Audit) -> [Any]? {
		// Query for the conditions
		var stmt: OpaquePointer?
		let query = "SELECT " + COL_PRODUCT_ID + ", " + COL_CONDITIONS +
			" FROM " + TABLE_NAME + " WHERE " + COL_AUDIT_ID + "=?";
		if (sqlite3_prepare_v2(db.con, query, -1, &stmt, nil) != SQLITE_OK) {
			return nil
		}

		// Bind the filter
		db.bind(stmt: stmt, param: 1, toUUID: audit.id)

		// Cycle through the results
		var res = [Any]()
		while (sqlite3_step(stmt) == SQLITE_ROW) {
			do {
				guard
					// Parse the conditions in the current record
					let parse = db.column(stmt: stmt, stringParam: 1),
					let data = parse.data(using: .utf8),
					let array = try JSONSerialization.jsonObject(with: data, options: []) as? [Int] else {
						continue
					}

					// Add the object for this product to the results
					let productId = db.column(stmt: stmt, intParam: 0)
					res.append([
						"chainXProductId": productId!,
						"skuConditionIds": array
					])
			} catch {
				// Failed to parse
				continue
			}
		}

		// Return the results
		sqlite3_finalize(stmt)
		return res
	}

	/**
	 * Deletes the conditions associated with the passed audit.
	 * @param db Database from which to delete conditions
	 * @param audit Audit whose conditions we want to delete
	 * @return Success flag
	 */
	static func delete(_ db: BaseDatabase, forAudit audit: Audit) -> Bool {
		// Prepare the query
		var stmt: OpaquePointer?
		let query = "DELETE FROM " + TABLE_NAME + " WHERE " + COL_AUDIT_ID + "=?"
		if (sqlite3_prepare_v2(db.con, query, -1, &stmt, nil) != SQLITE_OK) {
			return false
		}

		// Bind and execute the query
		db.bind(stmt: stmt, param: 1, toUUID: audit.id)
		let success = sqlite3_step(stmt) == SQLITE_DONE
		sqlite3_finalize(stmt)
		return success
	}

	/**
	 * Sets the conditions associated with the product in an audit.
	 * @param db Database holds conditions records
	 * @param audit Audit whose associated with product
	 * @param productId Identifies product whose conditions we want to set
	 * @param conditions Conditions to set
	 * @return Success flag
	 */
	static func setConditions(db: BaseDatabase, audit: Audit, productId: Int, conditions: Set<Int>?) -> Bool {
		// Does the record exist?
		var conditionsId: UUID?
		var stmt: OpaquePointer?
		var query = "SELECT " + COL_ID + " FROM " + TABLE_NAME +
			" WHERE " + COL_AUDIT_ID + "=? and " + COL_PRODUCT_ID + "=?"
		if (sqlite3_prepare_v2(db.con, query, -1, &stmt, nil) != SQLITE_OK) {
			// Failed to query for the record
			return false
		} else {
			// Bind and execute the query
			db.bind(stmt: stmt, param: 1, toUUID: audit.id)
			db.bind(stmt: stmt, param: 2, toInt: productId)
			if (sqlite3_step(stmt) == SQLITE_ROW) {
				conditionsId = db.column(stmt: stmt, uuidParam: 0)
			}
			sqlite3_finalize(stmt)
		}

		// Are there any conditions to set?
		if (conditions?.count ?? 0 == 0) {
			// No conditions
			if (conditionsId == nil) {
				// Nothing to do
				return true
			}

			// Delete the conditions
			query = "DELETE FROM " + TABLE_NAME + " WHERE " + COL_ID + "=?"
			if (sqlite3_prepare_v2(db.con, query, -1, &stmt, nil) != SQLITE_OK) {
				return false
			} else {
				db.bind(stmt: stmt, param: 1, toUUID: conditionsId)
				let success = sqlite3_step(stmt) == SQLITE_DONE
				sqlite3_finalize(stmt)
				return success
			}
		}

		// Stringize the conditions
		let json: String?
		do {
			let data = try JSONSerialization.data(withJSONObject: [Int](conditions!), options: [])
			json = String(data: data, encoding: .utf8)
			if (json == nil) {
				return false
			}
		} catch {
			// Failed to stringize conditions
			return false
		}

		// What kind of query do we need to set?
		if (conditionsId == nil) {
			// Prepare the insert query
			query = "INSERT INTO " + TABLE_NAME + " (" +
				db.formatColumns(TABLE_COLS) + ") VALUES (" +
				db.formatColParams(TABLE_COLS) + ")"
			if (sqlite3_prepare_v2(db.con, query, -1, &stmt, nil) != SQLITE_OK) {
				return false
			}

			// Bind the inserted fields
			let now = Date()
			db.bind(stmt: stmt, param: 1, toUUID: UUID())
			db.bind(stmt: stmt, param: 2, toDate: now)
			db.bind(stmt: stmt, param: 3, toDate: now)
			db.bind(stmt: stmt, param: 4, toUUID: audit.id)
			db.bind(stmt: stmt, param: 5, toInt: productId)
			db.bind(stmt: stmt, param: 6, toString: json)
		} else {
			// Prepare update query
			query = "UPDATE " + TABLE_NAME + " SET " + COL_CONDITIONS + "=? WHERE " +
				COL_ID + "=?"
			if (sqlite3_prepare_v2(db.con, query, -1, &stmt, nil) != SQLITE_OK) {
				return false
			}

			// Bind the update fields
			db.bind(stmt: stmt, param: 1, toString: json)
			db.bind(stmt: stmt, param: 2, toUUID: conditionsId)
		}

		// Execute the query
		let success = sqlite3_step(stmt) == SQLITE_DONE
		sqlite3_finalize(stmt)
		if (!success) {
			db.logLastError("set conditions for audit")
		}
		return success
	}

	private static let TABLE_NAME = "conditions"
	private static let COL_ID = "conditions_uuid"
	private static let COL_CREATED_AT = "created_at"
	private static let COL_UPDATED_AT = "updated_at"
	private static let COL_AUDIT_ID = "audit_uuid"
	private static let COL_PRODUCT_ID = "chain_x_product_id"
	private static let COL_CONDITIONS = "conditions_array"
	private static let TABLE_COLS: [String] = [
		COL_ID,
		COL_CREATED_AT,
		COL_UPDATED_AT,
		COL_AUDIT_ID,
		COL_PRODUCT_ID,
		COL_CONDITIONS]
}
