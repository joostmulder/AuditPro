//
//  Product.swift
//  Mobile Client
//
//  Created by Eric Ruck on 2/19/18.
//  Copyright © 2018 AuditPro. All rights reserved.
//

import Foundation

struct Product {
	let id: Int
	let clientId: Int
	let chainId: Int
	let globalProductId: Int
	let brandName: String
	let productName: String
	let upc: String

	let brandNameShort: String?
	let msrp: Double?
	let randomWeight: Bool
	let retailPriceMin: Double?
	let retailPriceMax: Double?
	let retailPriceAverage: Double?
	let categoryName: String?
	let subcategoryName: String?
	let productTypeName: String?
	let currentReorderCode: String?
	let previousReorderCode: String?
	let brandSku: String?
	let lastScannedAt: Date?
	let lastScannedPrice: Double?
	let lastScanWasSale: Bool
	let chainSku: String?
	let inStockPriceMin: Double?
	let inStockPriceMax: Double?
}

extension Product {
	/**
	 * Describes the product using its display name.
	 * @return Product description
	 */
	var description: String {
		get { return self.productName }
	}

	/**
	 * Determines if the token is in one of our searchable fields, case insensitive.
	 * @param token Token to find
	 * @return Found flag
	 */
	func hasToken(_ token: String?) -> Bool {
		// Trivial check
		if (token?.isEmpty ?? true) {
			// No token
			return false
		}

		// Case insensitive search
		let caseToken = token!.lowercased()
		return
			upc.lowercased().contains(caseToken) ||
			productName.lowercased().contains(caseToken) ||
			currentReorderCode?.lowercased().contains(caseToken) ?? false ||
			brandSku?.lowercased().contains(caseToken) ?? false ||
			chainSku?.lowercased().contains(caseToken) ?? false
		;
	}

	/**
	 * Determines the display reorder code based on our field values.
	 * @return Reorder code to display
 	 */
	var displayBrandName: String {
		return coalesce(brandName, brandNameShort, "")
	}

	/**
	 * Determines the display reorder code based on our field values.
	 * @return Reorder code to display
	 */
	var displayReorderCode: String {
		return coalesce(currentReorderCode, previousReorderCode, "--")
	}

	/**
	 * Coalesces a tuple.
	 * @param prim Primary value
	 * @param sec Secondary value
	 * @param def Default fallback value
	 * @return Coalesced value
	 */
	private func coalesce(_ prim: String?, _ sec: String?, _ def: String) -> String {
		if (prim ?? "").range(of: "^\\s*$", options: .regularExpression) == nil {
			return prim!
		}
		if (sec ?? "").range(of: "^\\s*$", options: .regularExpression) == nil {
			return sec!
		}
		return def
	}
}
