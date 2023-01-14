//
//  Scan.swift
//  Mobile Client
//
//  Created by Eric Ruck on 2/20/18.
//  Copyright © 2018 AuditPro. All rights reserved.
//

import Foundation


/**
 * Records a scan within an audit session.
 * @author Eric Ruck
 */
struct Scan {
	let id: UUID
	let auditId: UUID
	let createdAt: Date
	let updatedAt: Date?
	let productId: Int
	let retailPrice: Double?
	let salePrice: Double?
	let scanData: String?
	let scanTypeId: Int
	let productName: String
	let brandName: String
}


/**
 * Extends the scan object with useful functionality.
 * @author Eric Ruck
 */
extension Scan {

	/**
	 * Provides copy constructor.
	 * @param source Source scan to copy
	 */
	init?(source: Scan) {
		self.id = source.id
		self.auditId = source.auditId
		self.createdAt = source.createdAt
		self.updatedAt = source.updatedAt
		self.productId = source.productId
		self.retailPrice = source.retailPrice
		self.salePrice = source.salePrice
		self.scanData = source.scanData
		self.scanTypeId = source.scanTypeId
		self.productName = source.productName
		self.brandName = source.brandName
	}

	/**
	 * Initializes for a newly selected product.
	 * @param audit The audit of which this scan is a part
	 * @param product Details of the scanned product
	 * @param scanData Raw scanner data or null for manual
	 * @param scanRetail Scanned retail value
	 * @param scanSale Scanned sale value
	 */
	init(audit: Audit, product: Product, scanData: String?,
			scanRetail: Double?, scanSale: Double?) {
		let now = Date()
		self.id = UUID()
		self.auditId = audit.id
		self.createdAt = now
		self.updatedAt = now
		self.productId = product.id
		self.productName = product.productName
		self.brandName = product.brandName
		self.retailPrice = scanRetail
		self.salePrice = scanSale
		self.scanData = scanData
		self.scanTypeId = (scanData == nil)
			? ScanType.MANUAL.id
			: ScanType.SCANNED.id
	}

	/**
	 * Creates a new rescan instance of this source scan.  Returns nil if there
	 * is no update.
	 * @param source Scan being rescanned
	 * @param scanData Raw scanner data or null for manual
	 * @param scanRetail Updated retail price
	 * @param scanSale Updated sale price
	 * @return Updated scan record or null if no update
	 */
	init?(source: Scan, scanData: String?, scanRetail: Double?, scanSale: Double?) {
		// Check for differences
		if ((source.scanData == source.scanData) &&
			(scanRetail == source.retailPrice) &&
			(scanSale == source.salePrice)) {
			// No update
			return nil
		}

		// Create rescan record
		self.id = source.id
		self.auditId = source.auditId
		self.createdAt = source.createdAt
		self.updatedAt = Date()
		self.productId = source.productId
		self.productName = source.productName
		self.brandName = source.brandName
		self.retailPrice = scanRetail
		self.salePrice = scanSale
		self.scanData = scanData
		self.scanTypeId = (scanData == nil)
			? ScanType.MANUAL.id
			: ScanType.SCANNED.id
	}
}
