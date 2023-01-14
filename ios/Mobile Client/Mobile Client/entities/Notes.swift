//
//  Notes.swift
//  Mobile Client
//
//  Created by Eric Ruck on 2/20/18.
//  Copyright © 2018 AuditPro. All rights reserved.
//

import Foundation

/**
 * Manages the notes for an audit.
 * @author Eric Ruck
 */
struct Notes {
	let id: UUID?
	let auditId: UUID
	let contents: String?
	let store: String?
	var isStoreEmpty: Bool {
		return store?.count ?? 0 == 0
	}
}
