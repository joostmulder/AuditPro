//
//  Receipt.swift
//  Mobile Client
//
//  Created by Eric Ruck on 2/24/18.
//  Copyright 2018-2019 AuditPro. All rights reserved.
//

import Foundation


/**
 * Manages the data for a printed receipt.
 * @author Eric Ruck
 */
class Receipt {
	/**
	 * Creates a new receipt to print.
	 * If the audit stamp is not provided, the current time will be formatted for the current locale
	 * and used.
	 * @param customerName Customer name to display
	 * @param storeName Store name to display
	 * @param auditStamp Formatted audit stamp to display or null for now
	 */
	init(customerName: String, storeName: String, auditStamp: String?) {
		// Keep passed params
		self._customerName = customerName;
		self._storeName = storeName;
		if (auditStamp == nil) {
			// Format now
			let dateFormatter = DateFormatter()
			dateFormatter.dateFormat = "MMM dd, yyyy"
			self._auditStamp = dateFormatter.string(from: Date())
		} else {
			// Keep passed time stamp
			self._auditStamp = auditStamp!
		}
	}

	/**
	 * Adds an out of stock item to the receipt.
	 * @param reorderNumber Reorder number for item
	 * @param productName Product name for item
	 */
	func addOutOfStockItem(reorderNumber: String, productName: String) {
		_outOfStockItems.append(LineItem(reorderNumber: reorderNumber, productName: productName))
	}

	/**
	 * Adds a void item to the receipt.
	 * @param reorderNumber Reorder number for item
	 * @param productName Product name for item
	 */
	func addVoidItem(reorderNumber: String, productName: String) {
		_voidItems.append(LineItem(reorderNumber: reorderNumber, productName: productName))
	}

	/**
	 * Adds a SKU condition item to the receipt.
	 * @param all Descriptions of all possible conditions
	 * @param conditionIds Identifies SKU conditions associated with a product
	 * @param reorderNumber Reorder number for item
	 * @param productName Product name for item
	 */
	func addSKUConditions(all: [Int: SKUCondition]?, from ids: Set<Int>?,
 			reorderNumber: String, productName: String) {
		// Check trivial case, no conditions
		if all == nil || (ids?.count ?? 0) == 0 {
			// No conditions associated
			return
		}

		// Keep the condition details
		self._allConditions = all;

		// Cycle through the conditions
		for conditionId in ids! {
			// Add the product to its conditions
			let line = LineItem(reorderNumber: reorderNumber, productName: productName)
			// Have we started a line item list for this condition?
			if _skuConditionItems.keys.contains(conditionId) {
				// Append product with this condition
				_skuConditionItems[conditionId]?.append(line)
			} else {
				// First product with this condition
				_skuConditionItems[conditionId] = [line]
			}
		}
	}

	/**
	 * Formats receipt output in Zebra ZPL format.
	 * @return Formatted ZPL document
	 */
	func formatZpl() -> String {
		// First determine the longest reorder code
		let longestReorderNumberWidth = calculateLongestReorderNumberWidth();

		// Determine the formatting type
		let longCodes = longestReorderNumberWidth > Double(Receipt.MAX_LINE_WIDTH / 3)
		let reorderWidth = longCodes
			? 0
			: Int(longestReorderNumberWidth + (2.0 * Receipt.AVG_CHAR_WIDTH))
		let detailWidth = Receipt.MAX_LINE_WIDTH - reorderWidth
		let maxLineChars = Int(Double(detailWidth) / Receipt.AVG_CHAR_WIDTH)

		// Start by building the formatting for the out of stock items
		var formatItems = ""
		let sectionName: String? = ((_voidItems.count == 0) || (_skuConditionItems.count == 0))
				? nil
				: ReorderStatus.OUT_OF_STOCK.name.uppercased()
		var position = appendFormatItems(name: sectionName, items: _outOfStockItems, format: &formatItems, position: 250,
			longCodes: longCodes, reorderWidth: reorderWidth, detailWidth: detailWidth, maxLineChars: maxLineChars)
		for itemPair in _skuConditionItems {
			// Add the current SKU condition
			if let condition = _allConditions?[itemPair.key] {
				position = appendFormatItems(name: condition.name.uppercased(), items: itemPair.value, format: &formatItems, position: position,
					longCodes: longCodes, reorderWidth: reorderWidth, detailWidth: detailWidth, maxLineChars: maxLineChars)
			}
		}
		if (_voidItems.count > 0) {
			// Add the void items
			position = appendFormatItems(name: ReorderStatus.VOID.name.uppercased(), items: _voidItems, format: &formatItems, position: position,
					longCodes: longCodes, reorderWidth: reorderWidth, detailWidth: detailWidth, maxLineChars: maxLineChars)
		}

		// Do we have store notes?
		var formatNotes = ""
		if (storeNotes != nil) {
			// Format the notes heading
			formatNotes = String(format: "^FO25,%d^FB%d,1,0,C,0^FDNOTES:^FS", position + 27, Receipt.MAX_LINE_WIDTH)
			position += 54;

			// Format the notes body
			let maxNotesChars = Int(Double(Receipt.MAX_LINE_WIDTH) / Receipt.AVG_CHAR_WIDTH)
			let notesLines = (storeNotes!.count + maxNotesChars - 1) / maxNotesChars
			formatNotes += String(format: "^FO25,%d^FB%d,%d,0,L,0^FD%@^FS",
					position, Receipt.MAX_LINE_WIDTH, notesLines, storeNotes!)
			position += 27 * notesLines
		}

		// Start the complete document
		var doc = "! U1 setvar \"device.languages\" \"zpl\"\r\n ^XA^CFA,25"

		// Add the heading
		doc += String(format: "^LL%d", position + 100)
		doc += String(format: "^FO25,110^FB516,2,0,C,0^FD%@ Reorder List For^FS", _customerName)
		doc += String(format: "^FO25,160^FB516,3,0,C,0^FD%@ %@^FS", _storeName, _auditStamp)

		// Add the items
		doc += formatItems
		doc += formatNotes

		// Complete the document
		doc += String(format: "^FO25,%d^FB516,1,0,C,0^FD--- www.AuditPRO.io ---^FS", position + 70)
		doc += "^XZ"
		return doc
	}

	/**
	 * Calculates the width of the longest reorder number in any line.
	 * @return Longest reorder number width, pixels
	 */
	private func calculateLongestReorderNumberWidth() -> Double {
		var res: Double = 0
		for item in _outOfStockItems {
			let currentReorderCode = item.calculateReorderNumberWidth
			if (currentReorderCode > res) {
				res = currentReorderCode;
			}
		}
		for item in _voidItems {
			let currentReorderCode = item.calculateReorderNumberWidth
			if (currentReorderCode > res) {
				res = currentReorderCode;
			}
		}
		for itemPair in _skuConditionItems {
			for item in itemPair.value {
				let currentReorderCode = item.calculateReorderNumberWidth
				if (currentReorderCode > res) {
					res = currentReorderCode;
				}
			}
		}
		return res;
	}

	/**
	 * Appends the items in a list to the formetted document.
	 * @param items Items to append
	 * @param formatItems Formatted document in progress
	 * @param position Top position for items in document
	 * @param longCodes Long reorder codes flag
	 * @param reorderWidth Width for reorder codes
	 * @param detailWidth Width for details (product name)
	 * @param maxLineChars Maximum detail characters on a line
	 * @return Position on document after last item
	 */
	private func appendFormatItems(name sectionName: String?, items: [LineItem], format formatItems: inout String,
  			position startPosition: Int, longCodes: Bool, reorderWidth: Int, detailWidth: Int, maxLineChars: Int) -> Int {
		// Is there a setion name?
		var position = startPosition
		if (sectionName != nil) {
			// Yes, add it to the document
			formatItems += String(format:"^FO25,%d^FB%d,1,0,C,0^FD%@:^FS",
					position + 27, Receipt.MAX_LINE_WIDTH, sectionName!)
			position += 54
		}

		// Cycle through the items
		var lines: Int
		for item in items {
			if (longCodes) {
				// Format as a single line
				let longFormat = String(format: "%@ - %@", item.reorderNumber, item.productName)
				lines = (longFormat.count + maxLineChars - 1) / maxLineChars
				formatItems += String(format: "^FO25,%d^FB%d,%d,0,L,0^FD%@^FS",
						position, detailWidth, lines, longFormat)
			} else {
				// Two column format
				lines = (item.productName.count + maxLineChars - 1) / maxLineChars
				if (lines == 1) {
					formatItems += String(format: "^FO25,%d^FD%@^FS^FO%d,%d^FD%@^FS",
							position, item.reorderNumber,
							reorderWidth, position, item.productName)
				} else {
					formatItems += String(format: "^FO25,%d^FD%@^FS^FO%d,%d^FB%d,%d,0,L,0^FD%@^FS",
							position, item.reorderNumber,
							reorderWidth, position, detailWidth, lines, item.productName)
				}
			}

			// Update the position of the next line
			position += (27 * lines);
		}

		// Return the position after the formatted lines
		return position;
	}

	/** Provides the maximum line width in pixels. */
	private static let MAX_LINE_WIDTH: Int = 516

	/** Provides the average character width in pixels. */
	private static let AVG_CHAR_WIDTH: Double = 20

	/** Stores one line item on the receipt. */
	struct LineItem {
		let reorderNumber: String
		let productName: String
		var calculateReorderNumberWidth: Double {
			return Double(reorderNumber.count) * AVG_CHAR_WIDTH
		}
	}

	/** Backs the out of stock items in the list. */
	private var _outOfStockItems = [LineItem]()

	/** Backs the void items in the list. */
	private var _voidItems = [LineItem]()

	/** Backs the SKU condition items. */
	private var _skuConditionItems = [Int: [LineItem]]()

	/** References all possible conditions, if we are displaying any. */
	private var _allConditions: [Int: SKUCondition]?

	/** Provides the customer name for the receipt heading. */
	private let _customerName: String

	/** Provides the store name for the receipt heading. */
	private let _storeName: String

	/** Provides the audit time stamp for the receipt heading. */
	private let _auditStamp: String

	/** References optional store notes or nil if none. */
	public var storeNotes: String?
}
