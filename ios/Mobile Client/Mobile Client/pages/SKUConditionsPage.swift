//
//  SKUConditionsPage.swift
//  Mobile Client
//
//  Created by Eric Ruck on 8/5/18.
//  Copyright 2018-2019 AuditPro. All rights reserved.
//

import UIKit


/**
 * Manages the page to display and update the SKU conditions for a product
 * in an audit.
 * @author Eric Ruck
 */
class SKUConditionsPage: BasePage, UITableViewDataSource, UITableViewDelegate {

	/** References the conditions table. */
	@IBOutlet var conditionsTable: UITableView?

	/** Provides the ID of the audit to which these conditions apply. */
	private var audit: Audit? = nil

	/** Provides the ID of the product to which these conditions apply. */
	private var productId: Int = 0

	/** Provides the current selected conditions. */
	private var selectedConditions: Set<Int>? = nil

	/** Provides all of the possible SKU conditions for this client. */
	private var allConditions: [Int: SKUCondition]?

	/**
	 * Initializes to edit the SKU conditions for the passed product in the audit.
	 * @param audit Audit being conducted
	 * @param productId Product whose SKU conditions are being edited
	 */
	convenience init(audit: Audit, productId: Int) {
		// Default init
		self.init()

		// Keep the passed parameters
		self.audit = audit
		self.productId = productId

		// Get the current settings
		let db = AuditDatabase()
		selectedConditions = db?.getSelectedSKUConditions(audit: audit, productId: productId) ?? Set<Int>()
		allConditions = Security.skuConditions
	}

	/**
	 * Gets the SKU Conditions page name to display.
	 */
	override var pageName: String? {
		return "Select SKU Conditions"
	}

	/**
	 * Saves on back event.
	 * @return Allow default navigation
	 */
	override func onBack() -> Bool {
		saveSelectedConditions()
		return false
	}

	/**
	 * Saves the selected conditions for the product in the audit to the database.
	 */
	private func saveSelectedConditions() {
		let saveConditions = (selectedConditions?.count ?? 0) == 0 ? nil : selectedConditions
		_ = AuditDatabase()?.updateSelectedSKUConditions(audit: audit!, productId: productId, selectedConditions: saveConditions)
	}

	/**
	 * Gets the number of rows to show in the filter table
	 */
	func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
		return allConditions?.count ?? 0
	}

	// Cached row height
	private var rowHeight: CGFloat = 0

	/**
	 * Return actual height for requested row.
	 * @param tableView Table whose row height we want
	 * @param indexPath Row whose height we want
	 * @return Height of requested row
	 */
	func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
		if rowHeight == 0 {
			let cell = (Bundle.main.loadNibNamed("FilterStatusOptionTableCell", owner: nil, options: nil)![0] as! UITableViewCell)
			rowHeight = cell.bounds.height
		}
		return rowHeight
	}

	/**
	 * Gets the requested populated cell for the filter table.
	 */
	func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
		// Get the reuse cell
		var cell = tableView.dequeueReusableCell(withIdentifier: "option")
		if (cell == nil) {
			// Create new cell
			cell = (Bundle.main.loadNibNamed("FilterStatusOptionTableCell", owner: nil, options: nil)![0] as! UITableViewCell)
			(cell!.viewWithTag(1) as! UISwitch).addTarget(self, action: #selector(switchValueDidChange(_:)), for: .valueChanged)
		}

		// Update the cell for the row and return it
		let conditionId = Array(allConditions!.keys)[indexPath.row]
		(cell!.viewWithTag(2) as! UILabel).text = allConditions![conditionId]?.description
		(cell!.viewWithTag(1) as! UISwitch).isOn = selectedConditions!.contains(conditionId)
		return cell!
	}

	/**
	 * Handles change in any filter switch value.
	 * @param sender Which switch changed
	 */
	@objc func switchValueDidChange(_ sender: UISwitch) {
		// Can we determine which switch was changed?
		if let indexPath = conditionsTable?.indexPath(for: (sender.superview?.superview as? UITableViewCell)!) {
			let conditionId = Array(allConditions!.keys)[indexPath.row]
			if sender.isOn {
				selectedConditions?.insert(conditionId)
			} else {
				selectedConditions?.remove(conditionId)
			}
		}
	}
}
