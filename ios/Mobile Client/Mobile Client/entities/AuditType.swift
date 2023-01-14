//
//  AuditType.swift
//  Mobile Client
//
//  Created by Eric Ruck on 2/20/18.
//  Copyright © 2018 AuditPro. All rights reserved.
//

import Foundation


/**
 * Provides the valid audit types.
 * @author Eric Ruck
 */
class AuditType {
	static let STANDARD = AuditType(id: 1, name: "Standard")
	static let DEMO     = AuditType(id: 2, name: "Demo")
	static let RESEARCH = AuditType(id: 3, name: "Research")
	static let Types = [STANDARD, DEMO, RESEARCH]

	/**
	 * Initializes a new audit type
	 * @param id Unique audit type identifier
	 * @param name Audit type display name
	 */
	private init(id: Int, name: String) {
		self.id = id;
		self.name = name;
	}

	/** Provides the unique identifier of this audit type. */
	let id: Int

	/** Provides the display name of this audit type. */
	let name: String
}
