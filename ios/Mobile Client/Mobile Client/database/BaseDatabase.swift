//
//  BaseDatabase.swift
//  Mobile Client
//
//  Created by Eric Ruck on 2/20/18.
//  Copyright © 2018 AuditPro. All rights reserved.
//

import Foundation


// TODO Consider logging errors to Crashlytics; add more error handling in table classes

/**
 * Provides common features for all of our local database stores.
 * @author Eric Ruck
 */
class BaseDatabase {
	let name: String
	var con: OpaquePointer?


	/**
	 * Initializes a connection to a database.
	 * @param name Database base name
	 * @param version Database version number
	 */
	init?(name: String, version: Int) {
		// Open the database
		self.name = name
		let fileURL = try! FileManager.default.url(for: .documentDirectory, in: .userDomainMask, appropriateFor: nil, create: false)
            .appendingPathComponent(name + ".sqlite")
        if sqlite3_open(fileURL.path, &con) != SQLITE_OK {
            print("error opening database " + name)
			return nil
        }

		// Query the current version
		var oldVersion: Int = -1
		var stmt: OpaquePointer?
		if (sqlite3_prepare_v2(con, "PRAGMA user_version", -1, &stmt, nil) == SQLITE_OK) {
			oldVersion = 0
			let rc = sqlite3_step(stmt)
			if (rc == SQLITE_ROW) {
				oldVersion = Int(sqlite3_column_int(stmt, 0))
			}
		}
		sqlite3_finalize(stmt);
		if (oldVersion < 0) {
			print("Error getting version for database: " + name)
			sqlite3_close(con)
			return nil
		}
		if (oldVersion == version) {
			// Verify the database
			if (!onVerifyDatabase()) {
				NSLog("Failed to verify database " + name)
				return nil
			}
		} else {
			// Start version update transaction
			if (sqlite3_exec(con, "BEGIN TRANSACTION", nil, nil, nil) != SQLITE_OK) {
				print("Error failed to start update transaction on " + name)
				sqlite3_close(con)
				return nil
			}

			// Are we creating or updating
			var updated: Bool
			if (oldVersion == 0) {
				updated = self.onCreateDatabase()
			} else {
				updated = self.onUpdateDatabase(lastVersion: oldVersion);
			}
			if (updated) {
				if (sqlite3_exec(con, "PRAGMA user_version=" + String(version), nil, nil, nil) != SQLITE_OK) {
					print("Error failed to set database version to " + String(version) + " in " + name)
					updated = false
				}
			}
			if (!updated) {
				print("Error failed to " + (oldVersion == 0 ? "create" : "update") + " database: " + name)
				sqlite3_exec(con, "ROLLBACK TRANSACTION", nil, nil, nil);
				sqlite3_close(con)
				return nil
			}
			if (sqlite3_exec(con, "COMMIT TRANSACTION", nil, nil, nil) != SQLITE_OK) {
				print("Error failed to commit " + (oldVersion == 0 ? "create" : "update") + " transaction on " + name)
				sqlite3_close(con)
				return nil
			}
		}
	}

	deinit {
		self.close()
	}

	func close() {
		if (con != nil) {
			sqlite3_close(con)
			con = nil
		}
	}

	func onCreateDatabase() -> Bool {
		return true;
	}

	func onUpdateDatabase(lastVersion: Int) -> Bool {
		return true;
	}

	func onVerifyDatabase() -> Bool {
		return true;
	}

	/**
	 * Convenience function for binding strings to prepared SQL.
	 * @param stmt Prepared SQL statement
	 * @param param Parameter index in SQL, 1-based
	 * @param toString String value to bind
	 */
	func bind(stmt: OpaquePointer?, param: Int, toString: String?) {
		if (toString != nil) {
			sqlite3_bind_text(stmt, Int32(param), NSString(string:toString!).utf8String, -1, nil)
		} else {
			sqlite3_bind_null(stmt, Int32(param))
		}
	}

	/**
	 * Convenience function for binding a UUID to a prepared query.
	 * @param stmt Prepared query
	 * @param param Parameter index in query, 1-based
	 * @param toUUID UUID value to bind
	 */
	func bind(stmt: OpaquePointer?, param: Int, toUUID: UUID?) {
		bind(stmt: stmt, param: param, toString: toUUID == nil ? nil : toUUID?.uuidString)
	}

	/**
	 * Convenience function for binding doubles to prepared SQL.
	 * @param stmt Prepared SQL statement
	 * @param param Parameter index in SQL, 1-based
	 * @param toDouble Double value to bind
	 */
	func bind(stmt: OpaquePointer?, param: Int, toDouble: Double?) {
		if (toDouble != nil) {
			sqlite3_bind_double(stmt, Int32(param), toDouble!)
		} else {
			sqlite3_bind_null(stmt, Int32(param))
		}
	}

	/**
	 * Convenience function for binding doubles to prepared SQL.
	 * @param stmt Prepared SQL statement
	 * @param param Parameter index in SQL, 1-based
	 * @param toInt Integer value to bind
	 */
	func bind(stmt: OpaquePointer?, param: Int, toInt: Int?) {
		if (toInt != nil) {
			sqlite3_bind_int(stmt, Int32(param), Int32(toInt!))
		} else {
			sqlite3_bind_null(stmt, Int32(param))
		}
	}

	/**
	 * Convenience function for binding boolean to prepared SQL.
	 * @param stmt Prepared SQL statement
	 * @param param Parameter index in SQL, 1-based
	 * @param toBool Boolean value to bind
	 */
	func bind(stmt: OpaquePointer?, param: Int, toBool: Bool?) {
		if (toBool != nil) {
			sqlite3_bind_int(stmt, Int32(param), toBool! ? 1 : 0)
		} else {
			sqlite3_bind_null(stmt, Int32(param))
		}
	}

	/**
	 * Convenience function for binding a Date to prepared SQL.
	 * @param stmt Prepared SQL statement
	 * @param param Parameter index in SQL, 1-based
	 * @param toBool Date value to bind
	 */
	func bind(stmt: OpaquePointer?, param: Int, toDate: Date?) {
		if (toDate != nil) {
			let value = NSString(string:BaseDatabase.iso8601Formatter.string(from: toDate!)).utf8String
 			sqlite3_bind_text(stmt, Int32(param), value, -1, nil)
		} else {
			sqlite3_bind_null(stmt, Int32(param))
		}
	}

	/**
	 * Determines if a table exists in our database.
	 * @param tableName Table to check
	 * @return Exists flag
	 */
	func tableExists(_ tableName: String) -> Bool {
		let query = String(format: "SELECT name FROM sqlite_master WHERE type='table' AND name='%@'", tableName)
		var stmt: OpaquePointer?
		if (sqlite3_prepare_v2(con, query, -1, &stmt, nil) != SQLITE_OK) {
			logLastError("testing for table " + tableName)
			return false
		}
		let res = (sqlite3_step(stmt) == SQLITE_ROW)
		sqlite3_finalize(stmt)
		return res
	}

	/**
	 * Gets a formatter to convert ISO8601 strings to Date.
	 * @return Formatter
	 */
	static var iso8601Formatter: DateFormatter {
		let formatter = DateFormatter()
		formatter.calendar = Calendar(identifier: .iso8601)
		formatter.locale = Locale(identifier: "en_US_POSIX")
		formatter.timeZone = TimeZone(secondsFromGMT: 0)
		formatter.dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSXXXXX"
		return formatter
	}

	/**
	 * Gets the nullable string value of a column.
	 * @param Prepared SQL statement
	 * @param stringParam 0-based string column number
	 * @return Nullable string value of column
	 */
	func column(stmt: OpaquePointer?, stringParam: Int) -> String? {
		let param = Int32(stringParam)
		return (sqlite3_column_type(stmt, param) == SQLITE_NULL)
			? nil
			: String(cString: sqlite3_column_text(stmt, param))
	}

	/**
	 * Gets the nullable double value of a column.
	 * @param Prepared SQL statement
	 * @param doubleParam 0-based double column number
	 * @return Nullable double value of column
	 */
	func column(stmt: OpaquePointer?, doubleParam: Int) -> Double? {
		let param = Int32(doubleParam)
		return (sqlite3_column_type(stmt, param) == SQLITE_NULL)
			? nil
			: sqlite3_column_double(stmt, param)
	}

	/**
	 * Gets the nullable boolean value of a column.
	 * @param Prepared SQL statement
	 * @param boolParam 0-based boolean column number
	 * @return Nullable boolean value of column
	 */
	func column(stmt: OpaquePointer?, boolParam: Int) -> Bool? {
		let param = Int32(boolParam)
		return (sqlite3_column_type(stmt, param) == SQLITE_NULL)
			? nil
			: (sqlite3_column_int(stmt, param) == 0 ? false : true)
	}

	/**
	 * Gets the nullable Date value of a column.
	 * @param Prepared SQL statement
	 * @param dateParam 0-based Date column number
	 * @return Nullable Date value of column
	 */
	func column(stmt: OpaquePointer?, dateParam: Int) -> Date? {
		let param = Int32(dateParam)
		if (sqlite3_column_type(stmt, param) == SQLITE_NULL) {
			return nil
		}
		let parse = String(cString:sqlite3_column_text(stmt, param))
		return BaseDatabase.iso8601Formatter.date(from: parse)
	}

	/**
	 * Gets the nullable UUID value of a column.
	 * @param Prepared SQL statement
	 * @param uuidParam 0-based UUID column number
	 * @return Nullable UUID value of column
	 */
	func column(stmt: OpaquePointer?, uuidParam: Int) -> UUID? {
		let parse = column(stmt: stmt, stringParam: uuidParam)
		return (parse == nil) ? nil : UUID(uuidString: parse!)
	}

	/**
	 * Gets the nullable Int value of a column.
	 * @param Prepared SQL statement
	 * @param intParam 0-based Int column number
	 * @return Nullable Int value of column
	 */
	func column(stmt: OpaquePointer?, intParam: Int) -> Int? {
		let param = Int32(intParam)
		if (sqlite3_column_type(stmt, param) == SQLITE_NULL) {
			return nil
		}
		return Int(sqlite3_column_int(stmt, param))
	}

	/**
	 * Convenience method returns formatted column list from column nam array.
	 * @param columns Array of column names
	 * @return Formatted column name list
	 */
	func formatColumns(_ columns:[String]) -> String {
		return columns.joined(separator: ", ")
	}

	/**
	 * Convenience method returns formatted parameter list from column nam array.
	 * @param columns Array of column names
	 * @return Formatted parameter list
	 */
	func formatColParams(_ columns:[String]) -> String {
		var params = [String]()
		for _ in 1...columns.count {
			params.append("?")
		}
		return params.joined(separator: ", ")
	}

	/**
	 * Format an ISO 8601 date from a date instance.
	 * @param date Date to format
	 * @return Formatted date or nil if input is nil
	 */
	static func formatDate(_ date: Date?) -> String? {
		if (date == nil) {
			return nil;
		}
		return iso8601Formatter.string(from: date!)
	}

	/**
	 * Logs the last database error for debugging.
	 * @param description Text for context
	 */
	@discardableResult
	func logLastError(_ description: String) -> String {
		let err = sqlite3_errcode(con)
		var log = String(format: "Database error %d in %@", err, name)
		let xer = sqlite3_extended_errcode(con)
		if (xer != err) {
			log += String(format: " (ext %d)", xer)
		}
		let msg = String(cString:sqlite3_errmsg(con))
		let format = "\(log): \(description) (\(msg))"
		NSLog(format)
		return format
	}

	/**
	 * Common utility to convert a time stamp to a readable format.
	 * @param source Stamp to convert
	 * @return Readable time and date
	 */
	static func readDateTime(_ source: Date?) -> String {
		// Trivial case
		if (source == nil) {
			// No date time
			return "N/A";
		}

		// Format short readable for current locale
		let df = DateFormatter()
		df.dateFormat = "MMM dd, yyyy"
		return df.string(from: source!)
	}
}

/* TODO Android Port
abstract public class BaseDatabase implements AutoCloseable {

	private static final String DATE_ISO8601 = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

	/**
	 * Parses the ISO 8601 timestamp encoded as a string to a system object.
	 * Logs a warning if the input source is invalid.
	 * @param source Source timestamp string
	 * @return Date object or null
	 */
	public static Date parseDateTime(String source) {
		// Trivial case
		if ((source == null) || source.matches("^\\s*$")) {
			return null;
		}

		// Parse to the structure
		SimpleDateFormat df = new SimpleDateFormat(DATE_ISO8601, Locale.US);
		ParsePosition pos = new ParsePosition(0);
		String parse = source.
				replaceAll("Z$", "+0000").
				replaceAll("(?<=T\\d\\d:\\d\\d:\\d\\d)\\+", ".000+");
		Date res = df.parse(parse, pos);
		if (res == null) {
			Log.w(LOG_TAG, String.format("Attempted to parse invalid time stamp \"%s\"", source));
		}

		// Returned the parsed structure
		return res;
	}

	/**
	 * Common utility to convert a time stamp to ISO 8601.
	 * @param source Stamp to convert
	 * @return 8601 time string or null
	 */
	public static String parseDateTime(Date source) {
		// Trivial case
		if (source == null) {
			// No date/time
			return null;
		}

		// Format in ISO 8601
		SimpleDateFormat df = new SimpleDateFormat(DATE_ISO8601, Locale.US);
		return df.format(source);
	}
}
*/
