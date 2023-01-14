//
//  Audit.swift
//  Mobile Client
//
//  Created by Eric Ruck on 2/20/18.
//  Copyright © 2018 AuditPro. All rights reserved.
//

import Foundation

struct Audit : Equatable {
	let id: UUID
	let userId: Int
	let storeId: Int
	let storeDescr: String
	let auditStartedAt: Date
	let auditEndedAt: Date?
	let auditTypeId: Int
	let latitudeAtStart: Double?
	let longitudeAtStart: Double?
	let latitudeAtEnd: Double?
	let longitudeAtEnd: Double?

	/**
	 * Implements equatable interface.
	 * @param v0 Comparison item
	 * @param v1 Other comparison item
	 * @return Items equal flag
	 */
	static func == (_ v0: Audit, _ v1: Audit) -> Bool {
		return v0.id == v1.id
	}
}

extension Audit {
	/**
	 * Creates an instance for a new audit.
	 * @param userId Id of user creating audit
	 * @param storeId Id of store being audited
	 * @param storeDescr Description of store being audited
	 * @param auditTypeId Type of audit being performed
	 * @param latitude Optional latitude at end of audit
	 * @param longitude Optional longitude at end of audit
	 */
	init(userId: Int, storeId: Int, storeDescr: String, auditTypeId: Int,
		latitude: Double?, longitude: Double?) {
		self.id = UUID()
		self.userId = userId
		self.storeId = storeId
		self.storeDescr = storeDescr
		self.auditStartedAt = Date()
		self.auditEndedAt = nil
		self.auditTypeId = auditTypeId
		self.latitudeAtStart = latitude
		self.longitudeAtStart = longitude
		self.latitudeAtEnd = nil
		self.longitudeAtEnd = nil
	}

	/**
	 * Creates a reopened version of the passed source audit.
	 * @param reopen Source to reopen
	 */
	init(reopen source: Audit) {
		self.id = source.id
		self.userId = source.userId
		self.storeId = source.storeId
		self.storeDescr = source.storeDescr
		self.auditStartedAt = source.auditStartedAt
		self.auditEndedAt = nil
		self.auditTypeId = source.auditTypeId
		self.latitudeAtStart = source.latitudeAtStart
		self.longitudeAtStart = source.longitudeAtStart
		self.latitudeAtEnd = nil
		self.longitudeAtEnd = nil
	}

	/**
	 * Ends an audit now.
	 * @param source Audit to end
	 * @param latitude Latitude at end of audit
	 * @param longitude Longitude at end of audit
	 * @param endTime Optional end of audit time
	 * @return Ended audit or nil if invalid
	 */
	init?(source: Audit, latitude: Double?, longitude: Double?, endTime: Date? = Date()) {
		// Validate
		if (source.auditEndedAt != nil) {
			// Invalid, audit already ended
			NSLog("Attempted to end audit %@ that has already ended.",
				source.id.uuidString);
			return nil
		}

		// Initialize the new instance
		self.id = source.id
		self.userId = source.userId
		self.storeId = source.storeId
		self.storeDescr = source.storeDescr
		self.auditStartedAt = source.auditStartedAt
		self.auditEndedAt = (endTime == nil) ? Date() : endTime
		self.auditTypeId = source.auditTypeId
		self.latitudeAtStart = source.latitudeAtStart
		self.longitudeAtStart = source.longitudeAtStart
		self.latitudeAtEnd = latitude
		self.longitudeAtEnd = longitude
	}

	/**
	 * Provides a eadable description of this instance.
	 * @return Readable description string
	 */
	var description: String {
		let formatter = DateFormatter()
		formatter.locale = Locale.current
		formatter.dateStyle = .medium
		formatter.timeStyle = .none
		return String(format: "%@ @ %@", storeDescr, formatter.string(from: auditStartedAt))
	}
}
