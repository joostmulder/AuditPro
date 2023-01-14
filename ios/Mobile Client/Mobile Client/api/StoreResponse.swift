//
//  StoreResponse.swift
//  Mobile Client
//
//  Created by Eric Ruck on 2/19/18.
//  Copyright © 2018 AuditPro. All rights reserved.
//

import Foundation


/**
 * Parses a response from the API into the store entity.
 * @author Eric Ruck
 */
extension Store {
	convenience init?(json: [String: Any]) {
		// Parse the incoming values
		let parser = ApiParser(json)
		guard
			// These can't be nil
			let clientId   = parser.int("client_id"),
			let chainId    = parser.int("chain_id"),
			let storeId    = parser.int("store_id"),
			let chainName  = parser.string("chain_name"),
			let chainCode  = parser.string("chain_code"),
			let storeName  = parser.string("store_name")
		else {
			return nil
		}

		// These are optional
		let storeIdent = parser.string("store_identifier")
		let storeAddr  = parser.string("store_street_address_1")
		let storeAddr2 = parser.string("store_street_address_2")
		let storeCity  = parser.string("store_city")
		let storeZip   = parser.string("store_zip")
		var storeLat   = parser.double("store_lat")
		var storeLon   = parser.double("store_lon")
		if storeLat == nil || storeLon == nil {
			// Make sure they're both nil
			storeLat = nil
			storeLon = nil
		}

		// Do we have a history
		var history: String?
		do {
			if let jh = json["audit_history"] as? [Any] {
				history = String(data: try JSONSerialization.data(withJSONObject: jh, options: []), encoding: .utf8)
			} else {
				history = nil
			}
		} catch {
			history = nil
		}

		// Validate the parsed values
		if ((clientId <= 0) || (chainId <= 0) || (storeId <= 0)) {
			return nil;
		}

		// Keep the parsed values
		self.init(
			clientId: clientId,
			chainId: chainId,
			storeId: storeId,
			chainName: chainName,
			chainCode: chainCode,
			storeName: storeName,
			storeIdentifier: storeIdent,
			storeAddress: storeAddr,
			storeAddress2: storeAddr2,
			storeCity: storeCity,
			storeZip: storeZip,
			storeLat: storeLat,
			storeLon: storeLon,
			history: history
		)
	}
}
