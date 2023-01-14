//
//  ScanType.swift
//  Mobile Client
//
//  Created by Eric Ruck on 2/20/18.
//  Copyright © 2018 AuditPro. All rights reserved.
//

import Foundation

class ScanType {
	static let SCANNED = ScanType(id: 1, name: "Scanned")
	static let MANUAL  = ScanType(id: 2, name: "Manual")
	static let Types   = [SCANNED, MANUAL]

	init(id: Int, name: String) {
		self.id = id
		self.name = name
	}
	
	let id: Int
	let name: String
}
