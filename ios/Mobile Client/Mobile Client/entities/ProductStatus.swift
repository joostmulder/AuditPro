//
//  ProductStatus.swift
//  Mobile Client
//
//  Created by Eric Ruck on 2/20/18.
//  Copyright © 2018 AuditPro. All rights reserved.
//

import Foundation


/**
 * Maintains the status of products being displayed in this audit.
 * @author Eric Ruck
 */
class ProductStatus : Equatable {
	/**
	 * Instantiates for a product.
	 * @param product Product represented by this status
	 */
	init(product: Product) {
		self._product = product
		self._reorderStatus = ReorderStatus.NONE
		self._displayPrice = nil
	}

	/**
	 * Clones the passed source instance.
	 * @param source Instance to clone
	 */
	init(source: ProductStatus) {
		self._product = source.product
		self._reorderStatus = source.reorderStatus
	}

	/**
	 * Implements equatable interface.
	 * @param v0 Comparison item
	 * @param v1 Other comparison item
	 * @return Items equal flag
	 */
	static func == (_ v0: ProductStatus, _ v1: ProductStatus) -> Bool {
		return v0.product.id == v1.product.id
	}

	/**
	 * Gets the product associated with this status.
	 * @return Product details
	 */
	var product: Product {
		get { return self._product }
	}

	/**
	 * Gets the reorder status for this product.
	 * @return Reorder status
	 */
	var reorderStatus: ReorderStatus {
		get { return self._reorderStatus }
	}

	/**
	 * Sets the reorder status from the passed id code.
	 * @param fromId Reorder status id code
	 */
	func setReorderStatus(fromId: Int) {
		self._reorderStatus = ReorderStatus.from(id: fromId) ?? ReorderStatus.NONE
	}

	/**
	 * Gets the display price for this product.
	 * @return Display price (null if none)
	 */
	var displayPrice: Double? {
		get { return _displayPrice }
		set { _displayPrice = newValue }
	}

	/**
	 * Indicates if we have a display price recorded (vs null).
	 * @return Has display price flag
	 */
	var hasDisplayPrice: Bool {
		return _displayPrice != nil
	}

	/**
	 * Sets the display price from the passed scan.  Does not update if the passed scan is null.
	 * @param scan Optional scan with prices
	 */
	private func setDisplayPrice(fromScan source: Scan?) {
		if let scan = source {
			_displayPrice = scan.salePrice ?? scan.retailPrice
		}
	}

	/**
	 * Gets the type of this product.
	 * @return Product type
	 */
	var productType: String? {
		return _product.productTypeName
	}

	/**
	 * Provides a title for this product for rendering in a list.
	 * @return Product title
	 */
	var description: String {
		get {
			return self._product.description
		}
	}

	/**
	 * Sets the reorder status from a report record.  If the report is null, the reorder status is
	 * set to NONE.  If the scan is not null, the display price is updated, otherwise it is left
	 * unchanged.
	 * @param report Record with status to set, optional
	 * @param scan Scan with recent price to set, optional
	 */
	func setReorderStatus(fromReport report: Report?, scan: Scan?) {
		self._reorderStatus = (report == nil)
			? ReorderStatus.NONE
			: ReorderStatus.from(id: report!.reorderStatusId)!
		setDisplayPrice(fromScan: scan)
	}

	/**
	 * Provides the product name.
	 * @return The name of the product
	 */
	var getProductName: String {
		get { return self._product.productName }
	}

	/**
	 * Provides the reorder status short text.
	 * @return The short name of the reorder status
	 */
	var getReorderStatusShortName: String {
		get { return self._reorderStatus.code }
	}

	/**
	 * Provides the product ID as a string that can be bound to a button.
	 * @return The stringized product ID
	 */
	var getId: String {
		get { return String(self._product.id) }
	}

	/**
	 * Updates from the passed source.
	 * @param scan Scan with recent price to set
	 */
	func updateFrom(scan: Scan) {
		setDisplayPrice(fromScan: scan)
	}

	/** Product details. */
	private var _product: Product

	/** Current reorder status. */
	private var _reorderStatus: ReorderStatus

	/** Price to display in summary, can be null. */
	private var _displayPrice: Double?
}
