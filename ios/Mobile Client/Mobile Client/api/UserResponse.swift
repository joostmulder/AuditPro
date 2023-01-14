//
//  UserResponse.swift
//  Mobile Client
//
//  Created by Eric Ruck on 2/10/18.
//  Copyright © 2018 AuditPro. All rights reserved.
//

import Foundation

struct UserResponse {
	let userId: Int
	let firstName: String
	let lastName: String
	let email: String
	let roleId: Int
	let roleName: String
	let roleRank: Int?
	let clientId: Int
	let clientName: String
	let clientSettings: [String: String]
	let skuConditions: [Int: SKUCondition]?
}

extension UserResponse {
	init?(json: [String: Any]) {
		// Parse the JSON
		guard
			let userId     = json["user_id"] as? Int,
			let firstName  = json["user_first_name"] as? String,
			let lastName   = json["user_last_name"] as? String,
			let email      = json["user_email"] as? String,
			let roleId     = json["role_id"] as? Int,
			let roleName   = json["role_name"] as? String,
			let roleRank   = json["role_rank"] as! Int?,
			let clientId   = json["client_id"] as? Int,
			let clientName = json["client_name"] as? String,
			let settingsList = json["client_settings"] as? [[String: Any]]
		else {
			// Invalid JSON
			return nil
		}

		// Validate parsed properties
		if ((userId <= 0) || (clientId <= 0)) {
			// IDs cannot be nil
			return nil
		}
		if (UserResponse.isEmpty(test: firstName) ||
			UserResponse.isEmpty(test: lastName) ||
			UserResponse.isEmpty(test: email)) {
			// First, lane name and e-mail cannot be empty
			return nil
		}

		// Convert the settings into a dictionary
		var clientSettings = [String: String]()
		for setting in settingsList {
			guard
				let name = setting["setting_name"] as? String,
				let value = setting["setting_value"] as? String
			else {
				continue
			}
			clientSettings[name] = value
		}

		// Accept the fields
		self.userId     = userId
		self.firstName  = firstName
		self.lastName   = lastName
		self.email      = email
		self.roleId     = roleId
		self.roleName   = roleName
		self.roleRank   = roleRank
		self.clientId   = clientId
		self.clientName = clientName
		self.clientSettings = clientSettings

		// Optional field
		self.skuConditions = SKUCondition.from(json: json["sku_conditions"] as? [[String: Any?]])
	}

	private static func isEmpty(test: String) -> Bool {
		let rxSpace = "^\\s*$"
		return test.range(of: rxSpace, options: .regularExpression, range: nil, locale: nil) != nil
	}
}
