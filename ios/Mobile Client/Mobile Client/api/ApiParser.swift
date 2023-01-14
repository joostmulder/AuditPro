//
//  ApiParser.swift
//  Mobile Client
//
//  Created by Eric Ruck on 3/21/18.
//  Copyright © 2018 AuditPro. All rights reserved.
//

import Foundation

class ApiParser {
	private let source: [String: Any]

	init(_ source: [String: Any]) {
		self.source = source
	}

	func int(_ name: String) -> Int? {
		if let val = source[name] {
			if (val is NSNull) {
				return nil
			}
			if (val is String) {
				return Int(val as! String)
			}
			if (val is NSNumber) {
				return (val as! NSNumber).intValue
			}
		}
		return nil
	}

	func string(_ name: String) -> String? {
		if let val = source[name] {
			if (val is NSNull) {
				return nil
			}
			if (val is NSNumber) {
				return String(describing: val as! NSNumber)
			}
			return val as? String
		}
		return nil
	}

	func double(_ name: String) -> Double? {
		if let val = source[name] {
			if (val is NSNull) {
				return nil
			}
			if (val is String) {
				return Double(val as! String)
			}
			if (val is NSNumber) {
				return (val as! NSNumber).doubleValue
			}
		}
		return nil
	}

	func bool(_ name: String, _ defaultValue: Bool? = nil) -> Bool? {
		if let val = source[name] {
			if (val is String) {
				return Bool(val as! String)
			}
			if (val is NSNumber) {
				return (val as! NSNumber).boolValue
			}
		}
		return (defaultValue == nil) ? nil : defaultValue!
	}
}
