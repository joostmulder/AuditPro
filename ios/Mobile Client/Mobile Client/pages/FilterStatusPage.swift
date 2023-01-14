//
//  FilterStatusPageViewController.swift
//  Mobile Client
//
//  Created by Eric Ruck on 12/31/17.
//  Copyright 2017-2019 AuditPro. All rights reserved.
//

import UIKit


/**
 * Manages the page to display and update the reorder status filter for limiting
 * the products on the audit page.
 * @author Eric Ruck
 */
class FilterStatusPage: BasePage, UITableViewDataSource, UITableViewDelegate {

	@IBOutlet var filterTable: UITableView?

	private var filterReorderStatus: [ReorderStatus]?
	private var filteredProductTypes: [String]?
	private var allProductTypes: [String]?
	private var didUpdateFilter: (([ReorderStatus]?, [String]?) -> Void)?

	/**
	 * Initializes to edit the passed filters.
	 */
	convenience init(filterReorderStatus: [ReorderStatus]?, allProductTypes: [String],
		filterProductTypes: [String]?, didUpdateFilter: @escaping ([ReorderStatus]?, [String]?) -> Void) {
		self.init()
		self.filterReorderStatus = filterReorderStatus
		self.filteredProductTypes = filterProductTypes
		self.allProductTypes = allProductTypes
		self.didUpdateFilter = didUpdateFilter
	}

    override func viewDidLoad() {
        super.viewDidLoad()
    }

	/**
	 * Provides our page name for display.
	 */
	override var pageName: String? {
		return "Select Filters"
	}

	/**
	 * Saves filters to delegate before exit.
	 */
	override func onBack() -> Bool {
		saveFilterStatus()
		return false
	}

	/**
	 * Gets the number of rows to show in the filter table
	 */
	func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
		// Default 4 status and label, plus the product types and their label if there are any
		return 5 + (hasProductTypeFilter ? (allProductTypes!.count + 1) : 0)
	}

	// Cached row heights
	private var statusTitleHeight: CGFloat = 0
	private var typeTitleHeight: CGFloat = 0
	private var optHeight: CGFloat = 0

	/**
	 * Return actual height for requested row.
	 * @param tableView Table whose row height we want
	 * @param indexPath Row whose height we want
	 * @return Height of requested row
	 */
	func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
		if indexPath.row == 0 {
			if statusTitleHeight == 0 {
				let cell = (Bundle.main.loadNibNamed("FilterStatusReorderStatusTableCell", owner: nil, options: nil)![0] as! UITableViewCell)
				statusTitleHeight = cell.bounds.height
			}
			return statusTitleHeight
		}
		if indexPath.row == 5 {
			if typeTitleHeight == 0 {
				let cell = (Bundle.main.loadNibNamed("FilterStatusProductTypesTableCell", owner: nil, options: nil)![0] as! UITableViewCell)
				typeTitleHeight = cell.bounds.height
			}
			return typeTitleHeight
		}
		if optHeight == 0 {
			let cell = (Bundle.main.loadNibNamed("FilterStatusOptionTableCell", owner: nil, options: nil)![0] as! UITableViewCell)
			optHeight = cell.bounds.height
		}
		return optHeight
	}

	/**
	 * Gets the requested populated cell for the filter table.
	 */
	func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
		switch (indexPath.row) {
			case 0:
				return self.tableView(tableView, reuseId: "reorderstatus", inNib: "FilterStatusReorderStatusTableCell")
			case 1:
				return self.tableView(tableView, forReorderStatus: ReorderStatus.NONE)
			case 2:
				return self.tableView(tableView, forReorderStatus: ReorderStatus.IN_STOCK)
			case 3:
				return self.tableView(tableView, forReorderStatus: ReorderStatus.OUT_OF_STOCK)
			case 4:
				return self.tableView(tableView, forReorderStatus: ReorderStatus.VOID)
			case 5:
				return self.tableView(tableView, reuseId: "producttypes", inNib: "FilterStatusProductTypesTableCell")
			default:
				return self.tableView(tableView, forProductStatusAt: indexPath.row - 6)
		}
	}

	/**
	 * Gets a "static" row that is not tied to the data.
	 * @param reuseId Identifier to dequeue row
	 * @param inNib Name of NIB where row lives
	 * @return Requested cell
	 */
	func tableView(_ tableView: UITableView, reuseId: String, inNib: String) -> UITableViewCell {
		return tableView.dequeueReusableCell(withIdentifier: reuseId) ??
			Bundle.main.loadNibNamed(inNib, owner: nil, options: nil)![0] as! UITableViewCell
	}

	/**
	 * Gets an option cell to use in the passed table
	 */
	func optionCellForTableView(_ tableView: UITableView) -> UITableViewCell {
		// Get the reuse cell
		var cell = tableView.dequeueReusableCell(withIdentifier: "option")
		if (cell == nil) {
			// Create new cell
			cell = (Bundle.main.loadNibNamed("FilterStatusOptionTableCell", owner: nil, options: nil)![0] as! UITableViewCell)
			(cell!.viewWithTag(1) as! UISwitch).addTarget(self, action: #selector(switchValueDidChange(_:)), for: .valueChanged)
		}

		// Return the cell
		return cell!
	}

	/**
	 * Gets a table view cell initialized for the reorder status filter.
	 * @param tableView View for which we want a cell
	 * @param forReorderStatus Status whose cell we want
	 * @return Table cell for reorder status
	 */
	func tableView(_ tableView: UITableView, forReorderStatus: ReorderStatus) -> UITableViewCell {
		// Get the option cell
		let cell = optionCellForTableView(tableView)

		// Populate the status name
		(cell.viewWithTag(2) as! UILabel).text = forReorderStatus.name

		// Set the status value
		(cell.viewWithTag(1) as! UISwitch).isOn =
			ReorderStatus.isStatus(forReorderStatus, inFilters: filterReorderStatus)

		// Return the cell
		return cell
	}

	/**
	 * Gets a table view cell initialized for the product type filter.
	 * @param  tableView View for which we want a cell
	 * @param  forProductStatusAt Index of status in all products list
	 * @return Requested cell
	 */
	func tableView(_ tableView: UITableView, forProductStatusAt: Int) -> UITableViewCell {
		// Get the option cell
		let cell = optionCellForTableView(tableView)

		// Populate the product type name
		let productType = allProductTypes?[forProductStatusAt] ?? "(Missing)"
		(cell.viewWithTag(2) as! UILabel).text = productType

		// Set the status value
		(cell.viewWithTag(1) as! UISwitch).isOn =
			filteredProductTypes?.contains(productType) ?? false

		// Return the cell
		return cell
	}

	/**
	 * Handles change in any filter switch value.
	 * @param sender Which switch changed
	 */
	@objc func switchValueDidChange(_ sender: UISwitch) {
		// Can we determine which switch was changed?
		if let indexPath = filterTable?.indexPath(for: (sender.superview?.superview as? UITableViewCell)!) {
			switch (indexPath.row) {
				case 1:
					setReorderStatusSwitch(ReorderStatus.NONE, isOn: sender.isOn)
					break
				case 2:
					setReorderStatusSwitch(ReorderStatus.IN_STOCK, isOn: sender.isOn)
					break
				case 3:
					setReorderStatusSwitch(ReorderStatus.OUT_OF_STOCK, isOn: sender.isOn)
					break
				case 4:
					setReorderStatusSwitch(ReorderStatus.VOID, isOn: sender.isOn)
					break
				default:
					if (indexPath.row > 5) {
						setProductTypeSwitchAtIndex(indexPath.row - 6, isOn: sender.isOn)
					}
			}
		}
	}

	/**
	 * Sets the filter switch state for a reorder status.
	 * @param status Reorder status to set
	 * @param isOn New filter switch state
	 */
	func setReorderStatusSwitch(_ status: ReorderStatus, isOn: Bool) {
		if (isOn) {
			if (filterReorderStatus == nil) {
				filterReorderStatus = [status]
			} else if (filterReorderStatus!.firstIndex(of: status) == nil) {
				filterReorderStatus!.append(status)
			}
		} else if (filterReorderStatus != nil) {
			if let index = filterReorderStatus!.firstIndex(of: status) {
				if (filterReorderStatus!.count == 1) {
					filterReorderStatus = nil
				} else {
					filterReorderStatus?.remove(at: index)
				}
			}
		}
	}

	/**
	 * Sets the filter switch state for a product type.
	 * @param index Index of product in all list to set
	 * @param isOn New filter switch state
	 */
	func setProductTypeSwitchAtIndex(_ index: Int, isOn: Bool) {
		if ((index < 0) || (index >= allProductTypes!.count)) {
			return
		}
		let productType = allProductTypes![index]
		if (isOn) {
			if (filteredProductTypes == nil) {
				filteredProductTypes = [productType]
			} else if (filteredProductTypes!.firstIndex(of: productType) == nil) {
				filteredProductTypes!.append(productType)
			}
		} else if (filteredProductTypes != nil) {
			if let index = filteredProductTypes!.firstIndex(of: productType) {
				if (filteredProductTypes!.count == 1) {
					filteredProductTypes = nil
				} else {
					filteredProductTypes?.remove(at: index)
				}
			}
		}
	}
	/**
	 * Saves the updated filter status back to the provider.
	 */
	private func saveFilterStatus() {
		// Build the list of selected reorder filters
		filteredProductTypes?.sort()
		didUpdateFilter!(filterReorderStatus, filteredProductTypes)
	}

	/**
	 * Should we show a product type filter?
	 * @return Show product type filter flag
	 */
	private var hasProductTypeFilter: Bool {
		return (allProductTypes?.count ?? 0) > 1
	}
}
