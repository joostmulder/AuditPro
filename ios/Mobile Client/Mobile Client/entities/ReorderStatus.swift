//
//  ReorderStatus.swift
//  Mobile Client
//
//  Created by Eric Ruck on 2/20/18.
//  Copyright © 2018 AuditPro. All rights reserved.
//

import Foundation


class ReorderStatus : Equatable {
	static let NONE         = ReorderStatus(id: 0, name: "None", code: "None", isValid: false)
	static let IN_STOCK     = ReorderStatus(id: 1, name: "In Stock", code: "I", isValid: true)
	static let OUT_OF_STOCK = ReorderStatus(id: 2, name: "Out of Stock", code: "OOS", isValid: true)
	static let VOID         = ReorderStatus(id: 3, name: "Void", code: "V", isValid: true)
	static let Statuses = [NONE, IN_STOCK, OUT_OF_STOCK, VOID]

	static func from(id: Int) -> ReorderStatus? {
		for status in Statuses {
			if (status.id == id) {
				return status;
			}
		}
		return nil
	}

	static func from(name: String?) -> ReorderStatus? {
		if (name == nil) { return nil }
		for status in Statuses {
			if (status.name == name) {
				return status;
			}
		}
		return nil;
	}

	static func from(code: String) -> ReorderStatus? {
		for status in Statuses {
			if (status.code == code) {
				return status;
			}
		}
		return nil;
	}

	static func isStatus(_ status: ReorderStatus, inFilters filters: [ReorderStatus]?) -> Bool {
		if (filters == nil) {
			return false
		}
		for test in filters! {
			if status.id == test.id {
				return true
			}
		}
		return false
	}

	public var description: String { return name; }

	private init(id: Int, name: String, code: String, isValid: Bool) {
		self.id = id
		self.name = name
		self.code = code
		self.isValid = isValid
	}

	/**
	 * Implements equatable interface.
	 * @param v0 Comparison item
	 * @param v1 Other comparison item
	 * @return Items equal flag
	 */
	static func == (_ v0: ReorderStatus, _ v1: ReorderStatus) -> Bool {
		return v0.id == v1.id
	}

	let id: Int
	let name: String
	let code: String
	let isValid: Bool
}
