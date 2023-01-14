//
//  AuditHistory.swift
//  Mobile Client
//
//  Created by Eric Ruck on 7/8/18.
//  Copyright © 2018 AuditPro. All rights reserved.
//

import Foundation


/**
 * Provides information about a prior audit event.
 * @author Eric Ruck
 */
struct AuditHistory {
	let auditId: String
	let auditCounter: Int
	let userEmail: String
	let auditNote: String
	let auditStoreNote: String
	let percentInStock: Int
	let percentVoid: Int
	let auditDurationTotal: String
	let daysSinceAudit: Int
	let lastAuditDate: Date?
}

/**
 * Extends the history object with useful functionality.
 * @author Eric Ruck
 */
extension AuditHistory {
	// JSON attribute names
	private static let ATTRIB_AUDIT_ID = "audit_id"
	private static let ATTRIB_AUDIT_COUNTER = "audit_counter"
	private static let ATTRIB_USER_EMAIL = "user_email"
	private static let ATTRIB_AUDIT_NOTE = "audit_note"
	private static let ATTRIB_STORE_NOTE = "audit_store_note"
	private static let ATTRIB_PCT_IN_STOCK = "percent_in_stock"
	private static let ATTRIB_PCT_VOID = "percent_void"
	private static let ATTRIB_DURATION = "audit_duration_total"
	private static let ATTRIB_DAYS_SINCE = "days_since_audit"
	private static let ATTRIB_LAST_DATE = "last_audit_date"

	/**
	 * Deserializes an instance from a JSON object.
	 * @param source Source JSON obnect
	 * @return Deserialized instance or null if source is invalid
	 */
	init?(_ source: [String: Any?]?) {
		// Validate source
		if (source == nil) {
			// No source
			return nil
		}
		guard
			// Parse the record
			let parse = source,
			let auditId = parse[AuditHistory.ATTRIB_AUDIT_ID]!,
			let auditCounter = parse[AuditHistory.ATTRIB_AUDIT_COUNTER] as? Int
		else {
			// One or more invalid fields
			return nil
		}

		// Keep the validated parsed values
		self.auditId = String(describing: auditId)
		self.auditCounter = auditCounter

		// Pull unvalidated fields
		let lastAuditDate = parse[AuditHistory.ATTRIB_LAST_DATE] as? String ?? ""
		self.userEmail = parse[AuditHistory.ATTRIB_USER_EMAIL] as? String ?? ""
		self.auditNote = parse[AuditHistory.ATTRIB_AUDIT_NOTE] as? String ?? ""
		self.auditStoreNote = parse[AuditHistory.ATTRIB_STORE_NOTE] as? String ?? ""
		self.percentInStock = parse[AuditHistory.ATTRIB_PCT_IN_STOCK] as? Int ?? 0
		self.percentVoid = parse[AuditHistory.ATTRIB_PCT_VOID] as? Int ?? 0
		self.daysSinceAudit = parse[AuditHistory.ATTRIB_DAYS_SINCE] as? Int ?? 0
		self.auditDurationTotal = parse[AuditHistory.ATTRIB_DURATION] as? String ?? ""
		self.lastAuditDate = BaseDatabase.iso8601Formatter.date(from: lastAuditDate)
	}

	/**
	 * Parses an array of audit history entries from JSON.
	 * @param source JSON array of audit history entries
	 * @return Parsed history entries or null if source is invalid
	 */
	static func fromJSON(_ source: [[String: Any?]]?) -> [AuditHistory]? {
		// Is there a source?
		guard let parse = source else {
			// No source
			return nil;
		}

		// Cycle through the objects in the source array
		var res: [AuditHistory] = []
		for item in parse {
			if let entry = AuditHistory(item) {
				res.append(entry)
			}
		}
		return res
	}

	/**
	 * Parses an array of audit history entries from a JSON encoded string.
	 * @param source JSON encoded string array of audit history entries
	 * @return Parsed history entries or null if source is invalid
	 */
	static func fromString(_ source: String?) -> [AuditHistory]? {
		do {
			// Parse the source
			if
				let data = source?.data(using: .utf8),
				let json = try JSONSerialization.jsonObject(with: data, options: []) as? [[String: Any?]] {
				return fromJSON(json)
			}
		} catch {
			// Invalid source
			NSLog("Failed to parse audit history JSON: %@", error.localizedDescription)
		}

		// Unable to parse
		return nil
	}

	/**
	 * Renders this instance to a JSON encoded string.
	 * @return JSON encoded string
	 */
	var description: String {
		return self.toJSON().description
	}

	/**
	 * Formats the last audit date for readability, return nil if no date
	 */
	var formatLastAudit: String? {
		// Make sure we have a last audit date
		guard let useDate = lastAuditDate else {
			return nil
		}

		// Format readable date
		let formatter = DateFormatter()
		formatter.locale = Locale(identifier: "en_US_POSIX")
		formatter.dateFormat = "dd/MM/yyyy"
		return formatter.string(from: useDate)
	}

	/**
	 * Serializes this history entry to a JSON object.
	 * @return Serialized history entry
	 */
	func toJSON() -> [String: Any?]  {
		return [
			AuditHistory.ATTRIB_AUDIT_ID: auditId,
			AuditHistory.ATTRIB_AUDIT_COUNTER: auditCounter,
			AuditHistory.ATTRIB_USER_EMAIL: userEmail,
			AuditHistory.ATTRIB_AUDIT_NOTE: auditNote,
			AuditHistory.ATTRIB_STORE_NOTE: auditStoreNote,
			AuditHistory.ATTRIB_PCT_IN_STOCK: percentInStock,
			AuditHistory.ATTRIB_PCT_VOID: percentVoid,
			AuditHistory.ATTRIB_DURATION: auditDurationTotal,
			AuditHistory.ATTRIB_DAYS_SINCE: daysSinceAudit,
			AuditHistory.ATTRIB_LAST_DATE: BaseDatabase.formatDate(lastAuditDate)
		]
	}

	/**
	 * Serializes a list of audit history entries to a JSON array.
	 * @param entries Entries to serialize
	 * @return Serialized array or nil if entries are invalid
	 */
	static func toJSON(_ entries: [AuditHistory]?) -> [[String: Any?]] {
		var res: [[String: Any?]] = []
		if (entries != nil) {
			for entry in entries! {
				res.append(entry.toJSON())
			}
		}
		return res
	}

	/**
	 * Serializes a list of audit history entries to a JSON string.
	 * @param entries Entries to serialize
	 * @return Serialized string
	 */
	static func toString(_ entries: [AuditHistory]?) -> String {
		guard
			let data = try? JSONSerialization.data(withJSONObject: toJSON(entries), options: []),
			let res = String(data: data, encoding: .utf8)
		else {
			NSLog("Failed to serialize audit history to JSON: %@")
			return ""
		}
		return res
	}
}
