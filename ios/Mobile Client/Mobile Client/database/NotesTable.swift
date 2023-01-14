//
//  NotesRecord.swift
//  Mobile Client
//
//  Created by Eric Ruck on 2/21/18.
//  Copyright © 2018 AuditPro. All rights reserved.
//

import Foundation


/**
 * Manages the notes table in the audit database.
 * @author Eric Ruck
 */
class NotesTable {
	/**
	 * Create our table in the passed database.
	 * @param db Database in which to create our table
	 */
	static func createTable(db: BaseDatabase) -> Bool {
		// Build a create table statement
		let st = "CREATE TABLE " + TABLE_NAME + " (" + COL_ID + " TEXT PRIMARY KEY, " +
			COL_AUDIT_ID + " TEXT, " +
			COL_CONTENTS + " TEXT, " +
			COL_STORE + " TEXT)"
		return sqlite3_exec(db.con, st, nil, nil, nil) == SQLITE_OK
	}

	/**
	 * Updates our table to the current version if necessary
	 * @param db Connected database
	 * @param lastVersion Last version of our database
	 */
	static func updateTable(db: BaseDatabase, lastVersion: Int) -> Bool {
		if (lastVersion < AuditDatabase.DB_VERSION_2_2) {
			// Upgrade for version 2.2
			if sqlite3_exec(db.con,
					"ALTER TABLE " + TABLE_NAME + " ADD " + COL_STORE + " TEXT DEFAULT ''",
					nil, nil, nil) != SQLITE_OK {
				return false
			}
		}
		return true
	}

	/**
	 * Gets the notes for the passed audit.
	 * @param db Database that holds the notes
	 * @param auditId Audit whose notes we want
	 * @return Notes for audit
	 */
	static func getNotes(db: BaseDatabase, auditId: UUID) -> Notes? {
		// Query for the notes
		var res: Notes?
		var stmt: OpaquePointer?
		let query = "SELECT " +
			COL_ID + ", " +
			COL_AUDIT_ID + ", " +
			COL_CONTENTS + ", " +
			COL_STORE + " " +
			"FROM notes WHERE audit_uuid=?"
		if (sqlite3_prepare_v2(db.con, query, -1, &stmt, nil) == SQLITE_OK) {
			db.bind(stmt: stmt, param: 1, toString: auditId.uuidString)
			if (sqlite3_step(stmt) == SQLITE_ROW) {
				let contents = String(cString: sqlite3_column_text(stmt, 2))
				let store = String(cString: sqlite3_column_text(stmt, 3))
				guard
					let id = UUID(uuidString: String(cString: sqlite3_column_text(stmt, 0))),
					let auditId = UUID(uuidString: String( cString:sqlite3_column_text(stmt, 1)))
				else {
					sqlite3_finalize(stmt)
					return nil
				}
				res = Notes(id: id, auditId: auditId, contents: contents, store: store)
			} else {
				res = Notes(id: nil, auditId: auditId, contents: "", store: "")
			}
			sqlite3_finalize(stmt)
		}
		return res;
	}

	/**
	 * Updates the passed note in the database. Inserts if this note has not yet
	 * been created.
	 * @param db Database to update
	 * @param notes Notes to update
	 * @param contents New contents for notes
	 * @param store New store notes
	 * @return Error message or nil on success
	 */
	static func update(db: BaseDatabase, notes: Notes, contents: String, store: String) -> String? {
		var stmt: OpaquePointer?
		if (notes.id == nil) {
			// Insert new note
			let query = "INSERT INTO " + TABLE_NAME + " (" +
				COL_ID + ", " + COL_AUDIT_ID + ", " + COL_CONTENTS + ", " + COL_STORE + ") VALUES (?,?,?,?)"
			if (sqlite3_prepare_v2(db.con, query, -1, &stmt, nil) == SQLITE_OK) {
				let noteId = UUID().uuidString
				let auditId = notes.auditId.uuidString
				db.bind(stmt: stmt, param: 1, toString: noteId)
				db.bind(stmt: stmt, param: 2, toString: auditId)
				db.bind(stmt: stmt, param: 3, toString: contents)
				db.bind(stmt: stmt, param: 4, toString: store)
				let rc = sqlite3_step(stmt)
				let success = rc == SQLITE_DONE
				sqlite3_finalize(stmt)
				return success ? nil : db.logLastError("insert new note")
			}
		} else {
			// Update existing note
			let query = "UPDATE " + TABLE_NAME + " SET " +
				COL_CONTENTS + "=?, " + COL_STORE + "=? WHERE " + COL_ID + "=?";
			if (sqlite3_prepare_v2(db.con, query, -1, &stmt, nil) == SQLITE_OK) {
				db.bind(stmt: stmt, param: 1, toString: contents)
				db.bind(stmt: stmt, param: 2, toString: store)
				db.bind(stmt: stmt, param: 3, toString: notes.id!.uuidString)
				let success = sqlite3_step(stmt) == SQLITE_DONE
				sqlite3_finalize(stmt)
				return success ? nil : db.logLastError("update existing note")
			}
		}
		let type = (notes.id == nil) ? "insert" : "update"
		return db.logLastError("prepare note " + type)
	}

	private static let TABLE_NAME = "notes"
	private static let COL_ID = "notes_uuid"
	private static let COL_AUDIT_ID = "audit_uuid"
	private static let COL_CONTENTS = "contents"
	private static let COL_STORE = "store_audit_note"
}
