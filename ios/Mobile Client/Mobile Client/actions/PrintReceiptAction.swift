//
//  PrintReceiptAction.swift
//  Mobile Client
//
//  Created by Eric Ruck on 5/5/18.
//  Copyright 2018-2019 AuditPro. All rights reserved.
//

import Foundation


/**
 * Prints a receipt asynchronously to the Bluetooth printer.
 * @author Eric Ruck
 */
public class PrintReceiptAction : PrinterBaseAction {
	private var complete: (() -> Void)? = nil

	/**
	 * Override to provide more specific error messages and logging.
	 * @return Describes printer action
	 */
	override var actionType: String? {
		return "receipt"
	}

	/**
	 * Prints the receipt asynchronously.
	 */
	func printReceipt(_ receipt: Receipt, _ complete: @escaping () -> Void) {
		sendToPrinter(payload: receipt.formatZpl(), complete)
	}
}
