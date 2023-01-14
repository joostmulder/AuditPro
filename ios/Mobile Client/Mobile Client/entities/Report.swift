//
//  Report.swift
//  Mobile Client
//
//  Created by Eric Ruck on 2/20/18.
//  Copyright © 2018 AuditPro. All rights reserved.
//

import Foundation


/**
 * Manages the report on one product in an audit.
 * @author Eric Ruck
 */
struct Report {
	let id: UUID?
	let createdAt: Date
	let updatedAt: Date
	let auditId: UUID
	let scanId: UUID?
	let productId: Int
	let reorderStatusId: Int
}

extension Report {
	/**
	 * Creates a new report that is ready to insert into the database.
	 * @param scan Scan with which this report is associated
	 * @param product Product with which this report is associated
	 * @param reorderStatusId Reported reorder status
	 */
	init(scan: Scan, product: Product, reorderStatusId: Int) {
		let now = Date()
		self.id = UUID()
		self.createdAt = now
		self.updatedAt = now
		self.auditId = scan.auditId
		self.scanId = scan.id
		self.productId = product.id
		self.reorderStatusId = reorderStatusId
	}

	/**
	 * Creates a new report to update an existing instance.
	 * @param source Existing instance to update
	 * @param scan Scan associated with this report
	 * @param reorderStatusId Updated reorder status
	 */
	init(source: Report, scan: Scan?, reorderStatusId: Int) {
		self.id = source.id
		self.createdAt = source.createdAt
		self.updatedAt = Date()
		self.auditId = source.auditId
		self.scanId = scan?.id
		self.productId = source.productId
		self.reorderStatusId = reorderStatusId
	}

	/**
	 * Initializes an implicit instance from a scan.
	 * @param source Scan source
	 */
	init(source: Scan) {
		self.id = nil
		self.createdAt = source.createdAt
		self.updatedAt = (source.updatedAt == nil) ? source.createdAt : source.updatedAt!
		self.auditId = source.auditId
		self.scanId = source.id
		self.productId = source.productId
		self.reorderStatusId = ReorderStatus.IN_STOCK.id
	}

	/**
	 * Initializes an implicit instance from a product.
	 * @param audit Parent audit
	 * @param product Product source
	 * @param reorderStatusId Initial status ID or nil for default
	 */
	init(audit: Audit, product: Product, reorderStatusId: Int? = nil) {
		let now = Date()
		self.id = nil
		self.createdAt = now
		self.updatedAt = now
		self.auditId = audit.id
		self.scanId = nil
		self.productId = product.id
		self.reorderStatusId = (reorderStatusId == nil)
			? ReorderStatus.OUT_OF_STOCK.id
			: reorderStatusId!
	}
}
