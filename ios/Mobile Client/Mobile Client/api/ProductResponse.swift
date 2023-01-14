//
//  ProductResponse.swift
//  Mobile Client
//
//  Created by Eric Ruck on 2/19/18.
//  Copyright © 2018 AuditPro. All rights reserved.
//

import Foundation

extension Product {
	init?(json: [String: Any]) {
		let parser = ApiParser(json)
		guard
			// Parse the non-optional fields
			let id = parser.int("chain_x_product_id"),
			let clientId = parser.int("client_id"),
			let chainId = parser.int("chain_id"),
			let globalProductId = parser.int("product_id"),
			let brandName = parser.string("brand_name"),
			let productName = parser.string("product_name"),
			let upc = parser.string("upc")
		else {
			// Non-optional fail
			return nil
		}

		// Parse the optionals
		let brandNameShort = parser.string("brand_name_short")
		let msrp = parser.double("msrp")
		let randomWeight = parser.bool("is_random_weight", false)!
		let retailPriceMin = parser.double("retail_price_min")
		let retailPriceMax = parser.double("retail_price_max")
		let retailPriceAverage = parser.double("retail_price_average")
		let categoryName = parser.string("category_name")
		let subcategoryName = parser.string("subcategory_name")
		let productTypeName = parser.string("product_type_name")
		let currentReorderCode = parser.string("current_reorder_code")
		let previousReorderCode = parser.string("previous_reorder_code")
		let brandSKU = parser.string("brand_sku")
		let lastScannedAt = parser.string("last_scanned_at")
		let lastScannedPrice = parser.double("last_scanned_price")
		let lastScanWasSale = parser.bool("last_scan_was_sale", false)!
		let chainSKU = parser.string("chain_sku")
		let inStockPriceMin = parser.double("in_stock_price_min")
		let inStockPriceMax = parser.double("in_stock_price_max")

		// Final validation
		if ((id <= 0) || (clientId <= 0) || (chainId <= 0) || (globalProductId <= 0)) {
			return nil
		}

		// Complete field instantiation
		self.id = id
		self.clientId = clientId
		self.chainId = chainId
		self.globalProductId = globalProductId
		self.brandName = brandName
		self.productName = productName
		self.upc = upc
		self.brandNameShort = brandNameShort
		self.msrp = msrp
		self.randomWeight = randomWeight
		self.retailPriceMin = retailPriceMin
		self.retailPriceMax = retailPriceMax
		self.retailPriceAverage = retailPriceAverage
		self.categoryName = categoryName
		self.subcategoryName = subcategoryName
		self.productTypeName = productTypeName
		self.currentReorderCode = currentReorderCode
		self.previousReorderCode = previousReorderCode
		self.brandSku = brandSKU
		self.lastScannedPrice = lastScannedPrice
		self.lastScanWasSale = lastScanWasSale
		self.chainSku = chainSKU
		self.inStockPriceMin = inStockPriceMin
		self.inStockPriceMax = inStockPriceMax
		if (lastScannedAt == nil) {
			self.lastScannedAt = nil
		} else {
			let df = DateFormatter()
			df.dateFormat = "yyyy-MM-dd'T'HH:mm:ssZ"
			self.lastScannedAt = df.date(from:lastScannedAt!)
		}
	}
}
