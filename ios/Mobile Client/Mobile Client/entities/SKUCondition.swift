//
//  SKUCondition.swift
//  Mobile Client
//
//  Created by Eric Ruck on 7/15/18.
//  Copyright © 2018 AuditPro. All rights reserved.
//

import Foundation


/**
 * Holds a configured SKU response option.
 * @author Eric Ruck
 */
struct SKUCondition {
	let conditionId: Int
	let name: String
	let description: String
}


/**
 * Extends the SKUCondition object with useful functionality.
 * @author Eric Ruck
 */
extension SKUCondition {
	/**
	 * Parses an array of SKU conditions from a JSON source.  Returns null if the source is empty,
	 * null or invalid.  If some (but not all) of the individual conditions are invalid, they
	 * will be logged and skipped.
	 * @param source JSON source array
	 * @return Parsed SKU conditions or null
	 */
	static func from(json source: [[String: Any?]]?) -> [Int: SKUCondition]? {
		// Validate
		guard let parse = source else {
			// Invalid source
			return nil
		}

		// Cycle through the objects
		var res = [Int: SKUCondition]()
		for item in parse {
			if let entry = SKUCondition(json: item) {
				res[entry.conditionId] = entry
			} else {
				NSLog("Invalid SKU Condition %@", item.description)
			}
		}

		// Are there any conditions?
		return res.count == 0 ? nil : res
	}

	/**
	 * Parses an array of SKU conditions from a JSON string.  Returns null if the source is empty,
	 * null or invalid.  If some (but not all) of the individual conditions are invalid, they
	 * will be logged and skipped.
	 * @param source JSON source string
	 * @return Parsed SKU conditions or null
	 */
	static func from(string source: String?) -> [Int: SKUCondition]? {
		// Validate
		guard let data = source?.data(using: .utf8) else {
			return nil
		}
		do {
			// Parse the string
			return from(json: try JSONSerialization.jsonObject(with: data, options: []) as? [[String: Any?]])
		} catch {
			// Trap
			NSLog("Invalid SKU Conditions source: %@", error.localizedDescription)
			return nil
		}
	}

	/**
	 * Serializes the source container to a JSON encoded string.  Returns null if the source
	 * parameter is null.
	 * @param source Source conditions
	 * @return Serialized representation or null
	 */
	static func toJSONString(_ source: [Int: SKUCondition]?) -> String? {
		// Validate input
		guard let conditions = source else {
			// Invalid input
			return nil
		}

		// Convert to JSON
		var res = [[String: Any?]]()
		for condition in conditions {
			res.append(condition.value.json)
		}
		do {
			// Stringize the JSON
			return String(data: try JSONSerialization.data(withJSONObject: res, options: []), encoding: .utf8)
		} catch {
			// Failed to stringize
			NSLog("Unexpected encoding fail converting SKU conditions to JSON: %@", error.localizedDescription)
			return nil
		}
	}

	// JSON attribute names
	private static let ATTRIB_ID = "sku_condition_id"
	private static let ATTRIB_NAME = "sku_condition_name"
	private static let ATTRIB_DESCR = "sku_condition_description"

	/**
	 * Instantiates from a JSON object source.
	 * @param source Source of SKU condition attributes
	 * @throws JSONException Invalid source
	 */
	private init?(json: [String: Any?]) {
		if
			let conditionId = json[SKUCondition.ATTRIB_ID] as? Int,
			let name = json[SKUCondition.ATTRIB_NAME] as? String,
			let description = json[SKUCondition.ATTRIB_DESCR] as? String {
			self.conditionId = conditionId
			self.name = name
			self.description = description
		} else {
			return nil
		}
	}

	/**
	 * Serializes this to a JSON object
	 * @return Serialized JSON object
	 * @throws JSONException Serialization error
	 */
	private var json: [String: Any?] {
		return [
			SKUCondition.ATTRIB_ID: conditionId,
			SKUCondition.ATTRIB_NAME: name,
			SKUCondition.ATTRIB_DESCR: description
		]
	}
}
