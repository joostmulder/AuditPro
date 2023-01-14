//
//  AuditDatabase.swift
//  Mobile Client
//
//  Created by Eric Ruck on 2/21/18.
//  Copyright © 2018 AuditPro. All rights reserved.
//

import Foundation


/**
 * Provides access to our audits that have not yet been uploaded.
 * @author Eric Ruck
 */
class AuditDatabase : BaseDatabase {
	static let DB_NAME = "audit"
	static let DB_VERSION_2_1 = 2
	static let DB_VERSION_2_2 = 3
	static let DB_VERSION_CURRENT = DB_VERSION_2_2

	/**
	 * Instantiates an object to access our stores database.
	 */
	init?() {
		super.init(name: AuditDatabase.DB_NAME, version: AuditDatabase.DB_VERSION_CURRENT)
	}

	/**
	 * Delegates table creation to the subclass.
	 * @return Success flag
	 */
	override func onCreateDatabase() -> Bool {
		return
			AuditTable.createTable(db: self) &&
			ScanTable.createTable(db: self) &&
			ReportTable.createTable(db: self) &&
			NotesTable.createTable(db: self) &&
			ConditionsTable.createTable(db: self)
	}

	/**
	 * Handles table update event from the parent database.
	 * @param lastVersion Last database version of our current tables
	 * @return Success flag
	 */
	override func onUpdateDatabase(lastVersion: Int) -> Bool {
		return
			AuditTable.updateTable(db: self, lastVersion: lastVersion) &&
			ScanTable.updateTable(db: self, lastVersion: lastVersion) &&
			ReportTable.updateTable(db: self, lastVersion: lastVersion) &&
			NotesTable.updateTable(db: self, lastVersion: lastVersion) &&
			ConditionsTable.updateTable(db: self, lastVersion: lastVersion)
	}

	/**
	 * Gets the notes associated with an audit.
	 * @param forAudit Audit whose notes we want
	 * @return Audit notes
	 */
	func getNotes(forAudit: Audit) -> Notes? {
		return self.getNotes(auditId: forAudit.id)
	}

	/**
	 * Gets the notes associated with an audit.
	 * @param auditId Id of audit whose notes we want
	 * @return Audit notes
	 * @throws MobileClientException Database error
	 */
	private func getNotes(auditId: UUID) -> Notes? {
		// Find the record
		return NotesTable.getNotes(db: self, auditId: auditId)
	}

	/**
	 * Updates the notes for an audit.
	 * @param notes Notes to update
	 * @param contents New contents for internal notes
	 * @param store New contents for store notes
	 * @return Error message or nil on success
	 */
	@discardableResult
	func updateNotes(notes: Notes, contents: String, store: String) -> String? {
		return NotesTable.update(db: self, notes: notes, contents: contents, store: store)
	}

	/**
	 * Gets the selected SKU conditions for a product from the current audit.
	 * @param audit Current audit
	 * @param productId Identifies the product
	 * @return The selected conditions or nil if none or error
	 */
	func getSelectedSKUConditions(audit: Audit, productId: Int) -> Set<Int>? {
		return ConditionsTable.getConditions(db: self, audit: audit, productId: productId)
	}

	/**
	 * Updates the selected SKU conditions for a product in the current audit.
	 * @param audit Current audit
	 * @param productId Identifies the product
	 * @param selectedConditions Selected conditions or nil if none
	 * @return Success flag
	 */
	func updateSelectedSKUConditions(audit: Audit, productId: Int, selectedConditions: Set<Int>?) -> Bool {
		// Find the record
		return ConditionsTable.setConditions(db: self, audit: audit, productId: productId, conditions: selectedConditions)
	}

	/**
	 * Gets the number of completed audits waiting for synchronization.
	 * @param userId Current user
	 * @return Count of complete audits for user or nil on error
	 */
	func completeCount(userId: Int) -> Int? {
		return AuditTable.getCount(self, userId: userId, isCompleted: true)
	}

	/**
	 * Gets all of the completed audits waiting for synchronization.
	 * @param userId User identifier
	 * @return List of complete audits or nil on error
	 */
	func getCompleteAudits(userId: Int) -> [Audit]? {
		return AuditTable.getCompleteAudits(self, userId: userId)
	}

	/**
	 * Begins a new audit. Fails if the user already has an audit in progress.
	 * @param userId Current user
	 * @param storeId Audited store
	 * @param storeDescr Store description for UI
	 * @param auditTypeId Type of audit
	 * @param latitude Optional starting latitude
	 * @param longitude Optional starting longitude
	 * @return The audit being started or nil on error
	 */
	func startAudit(userId: Int, storeId: Int, storeDescr: String, auditTypeId: Int,
		latitude: Double?, longitude: Double?) -> Audit? {
		// Make sure there is no audit in progress
		let openCount = AuditTable.getCount(self, userId: userId, isCompleted: false)
		if ((openCount == nil) || (openCount! > 0)) {
			NSLog("There is currently an audit in progress.")
			return nil;
		}

		// Create the new audit
		let start = Audit(userId: userId, storeId: storeId,
			storeDescr: storeDescr, auditTypeId: auditTypeId,
			latitude: latitude, longitude: longitude)
		return AuditTable.insert(self, audit: start) ? start : nil
	}

	/**
	 * Resumes an audit in progress for the user. Returns null if no audit is in progress.
	 * @param userId Current user
	 * @return Audit in progress or nil if none or error
	 */
	func resumeAudit(userId: Int) -> Audit? {
		return AuditTable.getAudit(self, userId: userId)
	}

	/**
	 * Completes the audit in progress.
	 * @param audit Audit in progress
	 * @param latitude Final latitude or null
	 * @param longitude Final longitude or null
	 * @param endTime Optional end of audit time or null
	 * @return Success flag
	 */
	func completeAudit(audit: Audit, latitude: Double?, longitude: Double?, endTime: Date) -> Bool {
		// Update record
		let complete = Audit(source: audit, latitude: latitude, longitude: longitude, endTime: endTime)
		return (complete == nil) ? false : AuditTable.update(self, audit: complete!)
	}

	/**
	 * Reopens a previously closed audit.
	 * @param audit Audit to reopen
	 * @return Reopened audit or nil on error
	 */
	func reopenAudit(_ audit: Audit) -> Audit? {
		// Update record
		let reopen = Audit(reopen: audit)
		return AuditTable.update(self, audit: reopen) ? reopen : nil
	}

	/**
	 * Adds a scan to this audit.
	 * @param scan Scan to add
	 * @return Success flag
	 */
	func addScan(scan: Scan) -> Bool {
		return ScanTable.insert(self, scan: scan)
	}

	/**
	 * Updates a scan in this audit.
	 * @param scan Scan to update
	 * @return Success flag
	 */
	func updateScan(update: Scan) -> Bool {
		return ScanTable.update(self, scan: update)
	}

	/**
	 * Gets a scan from the current audit.
	 * @param audit Current audit
	 * @param productId Identifier of scanned product
	 * @return The scan or nil if none or error
	 */
	func getScan(audit: Audit, productId: Int) -> Scan? {
		return ScanTable.getScan(self, forProductId: productId, inAudit: audit)
	}

	/**
	 * Gets a report from the current audit.
	 * @param audit Current audit
	 * @param productId Identifier of scanned product
	 * @return The report or nil if none or error
	 */
	func getReport(audit: Audit, productId: Int) -> Report? {
		return ReportTable.getReport(self, forProductId: productId, inAudit: audit)
	}

	/**
	 * Adds a report to the audit in progress.
	 * @param report Report to add
	 * @return Success flag
	 */
	func addReport(report: Report) -> Bool {
		return ReportTable.insert(self, report: report)
	}

	/**
	 * Updates a report in the audit in progress.
	 * @param update Report to update
	 * @return Success flag
	 */
	func updateReport(update: Report) -> Bool {
		if (update.id == nil) {
			// The source report is implicit, we actually need to add
			return ReportTable.insert(self, report: update)
		}

		// Update the report
		return ReportTable.update(self, report: update)
	}

	/**
	 * Gets all of the reports for the current audit.
	 * <p>
	 * Some of the reports returned may be implicit, e.g. In Stock for scanned items.  Pass null for
	 * products to return only explicit records.
	 * @param audit Current audit
	 * @param products Products for store, optional, see remarks
	 * @return Audit reports or nil on error
	 */
	func getAllReports(audit: Audit, products: [Product]?) -> [Report]? {
		// Start with the actual reports
		var reports = ReportTable.getReports(self, forAudit: audit);
		if ((reports == nil) || (products == nil)) {
			// We only want the real records
			return reports;
		}

		// Merge with scans
		let scans = ScanTable.getScans(self, forAudit: audit)
		if (scans == nil) {
			return nil
		}
		for scan in scans! {
			var found: Report? = nil
			for seek in reports! {
				if (seek.productId == scan.productId) {
					found = seek
					break;
				}
			}
			if (found == nil) {
				reports!.append(Report(source: scan));
			}
		}

		// Merge with products
		for prod in products! {
			var found: Report? = nil;
			for seek in reports! {
				if (seek.productId == prod.id) {
					found = seek
					break;
				}
			}
			if (found == nil) {
				reports!.append(Report(audit: audit, product: prod));
			}
		}

		// Return all of the reports
		return reports;
	}

	/**
	 * Populates the passed receipt with the out of stock items in the passed audit.
	 * @param receipt Receipt to populate
	 * @param audit Current audit
	 * @param products Products for store
	 * @return Success flag
	 */
	func populateReceipt(receipt: Receipt, audit: Audit, products: [Product]) -> Bool {
		// Start with the actual reports
		guard let reports = ReportTable.getReports(self, forAudit: audit) else {
			return false
		}

		// Sort products for output
		var sortedProducts = [Product](products)
		sortedProducts.sort(by: { $0.productName < $1.productName })

		// Look for all products that will go on the receipt
		let printVoids = Security.optSetting(name: Security.SETTING_PRINT_VOIDS, defaultValue: false)
		let printConditions = Security.optSetting(name: Security.SETTING_PRINT_CONDITIONS, defaultValue: false)
		let printNotes =
			Security.optSetting(name: Security.SETTING_AUDIT_STORE_NOTES, defaultValue: false) &&
			Security.optSetting(name: Security.SETTING_AUDIT_STORE_NOTES, defaultValue: false)
		for product in sortedProducts {
			// Should we include the current product on the receipt?
			for report in reports {
				if (report.productId == product.id) {
					if (report.reorderStatusId == ReorderStatus.OUT_OF_STOCK.id) {
						receipt.addOutOfStockItem(reorderNumber: product.displayReorderCode, productName: product.productName)
					} else if (printVoids && (report.reorderStatusId == ReorderStatus.VOID.id)) {
						receipt.addVoidItem(reorderNumber: product.displayReorderCode, productName: product.productName)
					}
					break;
				}
			}
			if printConditions {
				// Check for SKU conditions
				receipt.addSKUConditions(all: Security.skuConditions,
					from: self.getSelectedSKUConditions(audit: audit, productId: product.id),
					reorderNumber: product.displayReorderCode, productName: product.productName)
			}
		}

		// Should we check store notes?
		if printNotes {
			// Check store notes
			if
				let notes = self.getNotes(forAudit: audit),
				!notes.isStoreEmpty {
					// Add store notes
					receipt.storeNotes = notes.store;
			}
		}
		return true
	}

	/**
	 * Serializes a completed audit to a JSON formatted string.
	 * @param audit Audit to serialize
	 * @return Serialized audit or nil on error
	 */
	func serialize(audit: Audit) -> String? {
		// Get the related scans and reports
		let scans = ScanTable.getJSON(self, forAudit: audit)
		let reports = ReportTable.getJSON(self, forAudit: audit)
		let skuConditions = ConditionsTable.getJSON(db: self, audit: audit)

		// Get our notes
		let notes = getNotes(forAudit: audit)

		// Get the user info
		let user = ([
			"userId": Security.userId,
			"clientId": Security.clientId
		])

		// Generate the result
		let res: [String: Any?] = ([
			"id": audit.id.uuidString,
			"storeId": audit.storeId,
			"auditStartedAt": BaseDatabase.formatDate(audit.auditStartedAt),
			"auditEndedAt": BaseDatabase.formatDate(audit.auditEndedAt),
			"latitudeAtStart": audit.latitudeAtStart,
			"longitudeAtStart": audit.longitudeAtStart,
			"latitudeAtEnd": audit.latitudeAtEnd,
			"longitudeAtEnd": audit.longitudeAtEnd,
			"user": user,
			"scans": scans,
			"reports": reports,
			"notes": notes?.contents,
			"skuConditions": skuConditions,
			"audit_store_note": notes?.store
		])
		let jsonData = try? JSONSerialization.data(withJSONObject: res, options: [])
		return (jsonData == nil) ? nil : String(data: jsonData!, encoding: .utf8)
	}

	/**
	 * Deletes an audit from the local cache.
	 * @param audit Audit to delete
	 * @return Success flag
	 */
	@discardableResult
	func delete(audit: Audit) -> Bool {
		// Start an update transaction
		if (sqlite3_exec(con, "BEGIN TRANSACTION", nil, nil, nil) != SQLITE_OK) {
			logLastError(String(format: "Begin transaction for delete audit %@", audit.id.description))
			return false
		}

		// Execute the delete steps within the transaction
		var operation = String(format: "Delete audit %@ from scan table", audit.id.description)
		var success = ScanTable.delete(self, forAudit: audit)
		if (success) {
			operation = String(format: "Delete audit %@ from report table", audit.id.description)
			success = ReportTable.delete(self, forAudit: audit)
		}
		if (success) {
			operation = String(format: "Delete audit %@ from audit table", audit.id.description)
			success = AuditTable.delete(self, forAudit: audit)
		}
		if (success) {
			operation = String(format: "Delete audit %@ from conditions table", audit.id.description)
			success = ConditionsTable.delete(self, forAudit: audit)
		}

		// Did we succeed?
		if (success) {
			// Yes, commit
			operation = String(format: "Commit transaction for delete audit %@", audit.id.description)
			success = sqlite3_exec(con, "COMMIT TRANSACTION", nil, nil, nil) == SQLITE_OK
		}
		if (!success) {
			// Failed, roll back
			logLastError(operation)
			sqlite3_exec(con, "ROLLBACK TRANSACTION", nil, nil, nil);
		}
		return success;
	}
}
