//
//  FilterBrandPage.swift
//  Mobile Client
//
//  Created by Eric Ruck on 8/5/18.
//  Copyright 2018-2019 AuditPro. All rights reserved.
//

import UIKit


/**
 * Manages the page to display and update the brands filter for limiting the
 * products on the audit page.
 * @author Eric Ruck
 */
class FilterBrandPage: BasePage, UITableViewDataSource, UITableViewDelegate {

	@IBOutlet var brandsTable: UITableView?

	private var allBrands: [String]?
	private var filteredBrands: Set<String>?
	private var didUpdateBrands: ((Set<String>?) -> Void)?


	/**
	 * Initializes to edit the passed filters.
	 */
	convenience init(allBrands: [String], filteredBrands: Set<String>?,
		didUpdateBrands: @escaping (Set<String>?) -> Void) {
		self.init()
		self.allBrands = allBrands
		self.filteredBrands = filteredBrands ?? Set<String>()
		self.didUpdateBrands = didUpdateBrands
	}

	/**
	 * Gets the filter page name to display.
	 */
	override var pageName: String? {
		return "Select Brands"
	}

	/**
	 * Saves on back event.
	 * @return Allow default navigation
	 */
	override func onBack() -> Bool {
		saveFilterStatus()
		return false
	}

	/**
	 * Saves the updated filter status back to the provider.
	 */
	private func saveFilterStatus() {
		// Update the provider
		if (filteredBrands?.count == allBrands?.count) || (filteredBrands?.count == 0) {
			didUpdateBrands!(nil)
		} else {
			didUpdateBrands!(filteredBrands)
		}
	}

	/**
	 * Handles tap on the toggle button.
	 */
	@IBAction func onToggle() {
		// Update the filtered brands
		let selectAll = filteredBrands!.count < allBrands!.count
		filteredBrands?.removeAll()
		if selectAll {
			allBrands?.forEach { brand in
				filteredBrands?.insert(brand)
			}
		}

		// Update the table
		brandsTable?.reloadData()
	}
	
	/**
	 * Gets the number of rows to show in the filter table
	 */
	func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
		return allBrands?.count ?? 0
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
		let brand = allBrands![indexPath.row]
		(cell!.viewWithTag(2) as! UILabel).text = brand
		(cell!.viewWithTag(1) as! UISwitch).isOn = filteredBrands?.contains(brand) ?? false
		return cell!
	}

	/**
	 * Handles change in any filter switch value.
	 * @param sender Which switch changed
	 */
	@objc func switchValueDidChange(_ sender: UISwitch) {
		// Can we determine which switch was changed?
		if let indexPath = brandsTable?.indexPath(for: (sender.superview?.superview as? UITableViewCell)!) {
			let brand = allBrands![indexPath.row]
			if sender.isOn {
				filteredBrands?.insert(brand)
			} else {
				filteredBrands?.remove(brand)
			}
		}
	}
}
