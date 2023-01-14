//
//  Analytics.swift
//  Mobile Client
//
//  Created by Eric Ruck on 7/21/18.
//  Copyright © 2018 AuditPro. All rights reserved.
//

import Foundation
import Crashlytics


/**
 * Provides helper analytics methods for consistency and compactness.
 * @author Eric Ruck
 */
class Analytics {
	/**
	 * Logs an event with a single custom attribute.
	 * @param name Event name
	 * @param key Attribute name
	 * @param value Attribute value
	 */
	static func log(name: String, key: String, value: String) {
		Answers.logCustomEvent(withName: name, customAttributes: [
			key: value
		])
	}

	/**
	 * Logs an event with a single custom attribute.
	 * @param name Event name
	 * @param key Attribute name
	 * @param value Attribute value
	 */
	static func log(name: String, key: String, value: Bool) {
		Answers.logCustomEvent(withName: name, customAttributes: [
			key: value ? 1 : 0
		])
	}

	/**
	 * Logs an event for an audit with a single custom attribute.
	 * @param name Event name
	 * @param audit Audit to which event applies
	 * @param key Attribute name
	 * @param value Attribute value
	 */
	static func log(name: String, audit: Audit, key: String, value: String) {
		Answers.logCustomEvent(withName: name, customAttributes: [
			"Audit Id": audit.id.uuidString,
			key: value
		])
	}

	/**
	 * Logs an event for an audit.
	 * @param name Event name
	 * @param audit Audit to which event applies
	 */
	static func log(name: String, audit: Audit) {
		Answers.logCustomEvent(withName: name, customAttributes: [
			"Audit Id": audit.id.uuidString
		])
	}

	/**
	 * Logs a filter change.
	 * @param name Which filter
	 * @param options List of selected options, empty for all or nil if none
	 * @param text Text filter, empty for all or nil if none
	 */
	static func filter(name: String, options: [String]?, text: String?) {
		// Assemble the attributes
		var attrs = ["type": "name"]
		if (options != nil) {
			if options!.count == 0 {
				attrs["options"] = "all"
			} else {
				attrs["options"] = options!.joined(separator: ",")
			}
		}
		if (text != nil) {
			attrs["text"] = text!
		}

		// Log the event
		Answers.logCustomEvent(withName: name, customAttributes: attrs)
	}

	/**
	 * Logs an action menu event.
	 * @param type Action type
	 * @param details Details or nil
	 */
	static func menuAction(type: String, details: String? = nil) {
		// Assemble attributes
		var attrs = ["type": type]
		if (details != nil) {
			attrs["details"] = details
		}

		// Log the event
		Answers.logCustomEvent(withName: "Menu Action", customAttributes: attrs)
	}
}
