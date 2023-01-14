//
//  Store.swift
//  Mobile Client
//
//  Created by Eric Ruck on 2/19/18.
//  Copyright © 2018 AuditPro. All rights reserved.
//

import Foundation


/**
 * Provides information about a store that can be audited.
 * @author Eric Ruck
 */
class Store {
	private let _clientId: Int
	private let _chainId: Int
	private let _storeId: Int
	private let _chainName: String
	private let _chainCode: String
	private let _storeName: String
	private let _storeIdentifier: String?
	private let _storeAddress: String?
	private let _storeAddress2: String?
	private let _storeCity: String?
	private let _storeZip: String?
	private let _storeLat: Double?
	private let _storeLon: Double?
	private var _history = [AuditHistory]()

	init(clientId: Int, chainId: Int, storeId: Int, chainName: String, chainCode: String,
		storeName: String, storeIdentifier: String?, storeAddress: String?, storeAddress2: String?,
		storeCity: String?, storeZip: String?, storeLat: Double?, storeLon: Double?, history: String?) {
		self._clientId = clientId
		self._chainId = chainId
		self._storeId = storeId
		self._chainName = chainName
		self._chainCode = chainCode
		self._storeName = storeName
		self._storeIdentifier = storeIdentifier
		self._storeAddress = storeAddress
		self._storeAddress2 = storeAddress2
		self._storeCity = storeCity
		self._storeZip = storeZip
		self._storeLat = storeLat
		self._storeLon = storeLon
		self._history = AuditHistory.fromString(history) ?? [AuditHistory]()
	}

	// Getters
	var clientId: Int { return _clientId }
	var chainId: Int { return _chainId }
	var storeId: Int { return _storeId }
	var chainName: String { return _chainName }
	var chainCode: String { return _chainCode }
	var storeName: String { return _storeName }
	var storeIdentifier: String? { return _storeIdentifier }
	var storeAddress: String? { return _storeAddress }
	var storeAddress2: String? { return _storeAddress2 }
	var storeCity: String? { return _storeCity }
	var storeZip: String? { return _storeZip }
	var storeLat: Double? { return _storeLat }
	var storeLon: Double? { return _storeLon }
	var history: String? { return AuditHistory.toString(_history) }

	/**
	 * Formats the city, state and zip as a single line.
	 */
	var cityStateZip: String {
		var res = _storeCity
		if !(_storeZip?.trimmingCharacters(in: .whitespaces).isEmpty ?? true) {
			res = ((res?.trimmingCharacters(in: .whitespaces).isEmpty ?? true) ? "" : res! + " ") + _storeZip!
		}
		return res ?? ""
	}

	/**
	 * Gets the store description for display.
	 */
	var description: String {
		return (_storeIdentifier == nil)
			? _storeName
			: String(format: "%@ (%@)", _storeName, _storeIdentifier!)
	}

	/**
	 * Indicates if this store is geocoded.
	 */
	var isGeocoded: Bool {
		return (_storeLat != nil) && (_storeLon != nil)
	}

	/**
	 * Gets the number of history entries for this store.
	 * @return Audit history entry count
	 */
	var historyCount: Int {
		return _history.count
	}

	/**
	 * Gets the story history at the indicated index in the list.
	 * @param index Audit history list index
	 * @return Audit history entry or null if index is invalid
	 */
	func getHistoryAt(index: Int) -> AuditHistory? {
		return index >= 0 && index < _history.count ? _history[index] : nil
	}

	/**
	 * Add a history entry from the passed source.
	 * @param source JSON source contains audit history fields
	 * @return Success flag
	 */
	func addAuditHistory(json source: [String: Any?]?) -> Bool {
		// Parse the source
		if let parsed = AuditHistory(source) {
			// Apply valid history
			_history.append(parsed)
			return true
		}

		// Failed to parse history
		return false
	}
}
