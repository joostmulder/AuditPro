//
//  PrintReceiptAction.swift
//  Mobile Client
//
//  Created by Eric Ruck on 5/5/18.
//  Copyright 2018-2019 AuditPro. All rights reserved.
//

import Foundation


/**
 * Fetches the battery status of the Bluetooth printer asynchronously.
 * @author Eric Ruck
 */
public class PrinterBatteryAction : PrinterBaseAction {
	private var complete: ((_ batteryStatus: Int) -> Void)? = nil

	/** Defines the no printer battery result code. */
	static let BATTERY_STATUS_NO_PRINTER = 0

	/** Defines inability to communicate with printer code. */
	static let BATTERY_STATUS_PRINTER_ERROR = -1;

	/** Unexpected or invalid battery charge response from printer. */
	static let BATTERY_STATUS_PARSE_ERROR = -2;

	/**
	 * Override to provide more specific error messages and logging.
	 * @return Describes printer action
	 */
	override var actionType: String? {
		return "battery request"
	}

	/**
	 * Reads the battery state asynchronously. The completion delegate receives
	 * the battery percentage 1..100 or a BATTERY_STATUS code
	 */
	func readBattery(_ complete: @escaping (_ batteryStatus: Int) -> Void) {
		transactWithPrinter(payload: "! U1 getvar \"power.percent_full\"\r\n") { (res) in
			// Interpret the result
			if (res == nil) {
				// Error result
				complete(self.isPrinterFound
					? PrinterBatteryAction.BATTERY_STATUS_PRINTER_ERROR
					: PrinterBatteryAction.BATTERY_STATUS_NO_PRINTER)
			} else {
				// Parse the result
				// Range<String.Index)
				if let range = res?.range(of: "\\d+", options: .regularExpression, range: nil, locale: nil) {
					// Parse the found result
					let parsed = Int(res![range])
					complete(parsed ?? PrinterBatteryAction.BATTERY_STATUS_PARSE_ERROR)
				} else {
					// Failed to find the result
					complete(PrinterBatteryAction.BATTERY_STATUS_PARSE_ERROR)
				}
			}

			// Always complete after read
			return true
		}
	}
}

