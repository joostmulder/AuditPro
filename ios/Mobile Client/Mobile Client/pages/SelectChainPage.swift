//
//  SelectChainPage.swift
//  Mobile Client
//
//  Created by Eric Ruck on 3/25/18.
//  Copyright 2018-2019 AuditPro. All rights reserved.
//

import UIKit

class SelectChainPage : BasePage, UITableViewDelegate, UITableViewDataSource {

	/** Delegates cell selection action. */
	private var onChain: ((Chain?) -> Void)?

	/** Provides all of the chains to offer. */
	private var allChains: [Chain]?


	/**
	 * Initializes for passed chain delegate.
	 * @param onChain Handles chain selection from this page
	 */
	convenience init(onChain: @escaping (Chain?) -> Void) {
		self.init()
		self.onChain = onChain
	}

	/**
	 * Provides the page display name (and implies back navigation).
	 * @return Page display name
	 */
	override var pageName: String? {
		return "Select Chain To Filter";
	}

	/**
	 * Keep location turned on for audit store selection.
	 */
	override var isLocationNeeded: BasePage.LocationNeed {
		return  .keep
	}

	/**
	 * Initializes the chain selector.
	 */
	override func viewDidLoad() {
		super.viewDidLoad()

		// Get the chains to display
		let db = StoresDatabase()
		if let allChains = db?.getChains() {
			self.allChains = allChains
			self.allChains?.sort(by: { chain1, chain2 in
				return chain1.chainName < chain2.chainName
			})
		}
	}

	/**
	 * Gets the number of rows to show in our table.
	 * @param tableView References our table
	 * @param numberofRowsInSection Indicates section (we only have 1)
	 */
	func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
		guard let count = allChains?.count else {
			return 1;
		}
		return count + 1
	}

	/**
	 * Provides the cell to display for a row in the table.
	 * @param tableView references our table
	 * @param indexPath Identifies requested cell
	 */
	func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
		// Get a cell view
		var cell = tableView.dequeueReusableCell(withIdentifier: "chain")
		if (cell == nil) {
			cell = (Bundle.main.loadNibNamed("SelectChainTableCell", owner: nil, options: nil)![0] as! UITableViewCell)
		}

		// Populate the cell
		let label = cell!.contentView.subviews[0] as! UILabel
		let chain = chainFor(indexPath: indexPath)
		label.text = chain?.chainName ?? "ALL CHAINS"
		return cell!
	}

	/**
	 * Get the chain for an index path.
	 * @param indexPath Path whose chain we want
	 * @return Chain or nil if first row (all) or invalid
	 */
	private func chainFor(indexPath: IndexPath) -> Chain? {
		let row = indexPath.row - 1
		if ((allChains == nil) || (row < 0) || (row >= allChains!.count)) {
			return nil
		}
		return allChains![row]
	}

	/**
	 * Handles chain selection.
	 * @param tableView references our table
	 * @param indexPath Identifies selected cell
	 */
	func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
		onChain!(chainFor(indexPath: indexPath))
		host!.popPage()
	}
}
