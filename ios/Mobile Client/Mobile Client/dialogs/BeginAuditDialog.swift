//
//  BeginAuditDialog.swift
//  Mobile Client
//
//  Created by Eric Ruck on 8/6/18.
//  Copyright 2018-2019 AuditPro. All rights reserved.
//

import UIKit

/**
 * Displays a dialog that shows details for a store about to be audited, and
 * accepts confirmation from the user.
 * @author Eric Ruck
 */
class BeginAuditDialog: UIViewController, UITableViewDelegate, UITableViewDataSource {
	// Referencing outlets
	@IBOutlet var chainLabel: UILabel?
	@IBOutlet var storeLabel: UILabel?
	@IBOutlet var streetLabel: UILabel?
	@IBOutlet var cityLabel: UILabel?
	@IBOutlet var cancelButton: UIButton?
	@IBOutlet var auditButton: UIButton?
	@IBOutlet var historyTable: UITableView?

	// Operating paramters
	private var store: Store? = nil
	private var review: Bool = false
	private var didDismiss: ((Store?) -> Void)? = nil


	/**
	 * Initializes to edit the passed filters.
	 */
	convenience init(store: Store, review: Bool,
		didDismiss: ((Store?) -> ())? = nil) {
		self.init()
		self.store = store
		self.review = review
		self.didDismiss = didDismiss
	}

	/**
	 * Initializes controls on load.
	 */
    override func viewDidLoad() {
        super.viewDidLoad()

		// Populate the store info
		chainLabel?.text = store?.chainName
		storeLabel?.text = store?.description
		var address = store?.storeAddress
		let address2 = store?.storeAddress2
		if (address?.count ?? 0) == 0 {
			address = address2
		} else if (address2?.count ?? 0) > 0 {
			address! += "\n" + address2!;
		}
		if (address?.count ?? 0) == 0 {
			streetLabel?.isHidden = true
		} else {
			streetLabel?.text = address
		}
		cityLabel?.text = store?.cityStateZip
		if (review) {
			// No audit option
			UIView.performWithoutAnimation {
				cancelButton?.setTitle("OK", for: .normal)
				auditButton?.isHidden = true
			}
		}
    }

	/**
	 * Handles tap on the cancel button.
	 */
    @IBAction func onCancel() {
    	// Remove ourself from the view hierarchy
    	view.removeFromSuperview()
		removeFromParent()

    	// Alert the delegate
    	self.didDismiss?(nil)
	}

	/**
	 * Handles tap on the audit button.
	 */
	@IBAction func onAudit() {
    	// Remove ourself from the view hierarchy
    	view.removeFromSuperview()
		removeFromParent()

    	// Alert the delegate
    	self.didDismiss?(store)
	}

	/**
	 * Populates a row view with the details of a history entry.
	 * @param rowView View to populate
	 * @param history History details
	 */
	private func populateRow(_ rowView: UIView, withHistory history: AuditHistory) {
		if let lastAuditLabel = rowView.viewWithTag(1) as? UILabel {
			// Update the last audit label
			if let lastAudit = history.formatLastAudit {
				lastAuditLabel.isHidden = false
				lastAuditLabel.text = lastAudit
			} else if (history.daysSinceAudit > 0) {
				lastAuditLabel.isHidden = false
				lastAuditLabel.text = String(format: "Last Audit %d Days Ago", history.daysSinceAudit)
			} else {
				lastAuditLabel.isHidden = true
			}
		}
		if let emailLabel = rowView.viewWithTag(2) as? UILabel {
			emailLabel.text = history.userEmail
		}
		if let timeLabel = rowView.viewWithTag(3) as? UILabel {
			timeLabel.text = String(format: "Audit Time: %@", history.auditDurationTotal)
		}
		if let noteLabel = rowView.viewWithTag(4) as? UILabel {
			// Update the note label
			if (history.auditNote.count > 0) {
				noteLabel.isHidden = false
				noteLabel.text = history.auditNote
			} else {
				noteLabel.isHidden = true
			}
		}
		if let storeLabel = rowView.viewWithTag(5) as? UILabel {
			// Update the store note label
			if (history.auditStoreNote.count > 0) {
				storeLabel.isHidden = false
				storeLabel.text = history.auditStoreNote
			} else {
				storeLabel.isHidden = true
			}
		}
		if let percentLabel = rowView.viewWithTag(6) as? UILabel {
			percentLabel.text = String(format: "%d%%", history.percentInStock)
		}
	}

	/** Cache the history row heights. */
	private var heightMap = [Int: CGFloat]()

	/**
	 * Gets the height of a row in the table.
	 * @param tableView Table whose row we're measuring
	 * @param indexPath Index path of height
	 * @return Height of row
	 */
	func tableView(_ tableView: UITableView, heightForRowAt indexPath: IndexPath) -> CGFloat {
		// Is the height cached?
		if let height = heightMap[indexPath.row] {
			// Return the cached height
			return height
		}

		// Populate view to determine height
		let view = Bundle.main.loadNibNamed("BeginAuditDialogRow", owner: nil, options: nil)![0] as! UIView
		view.bounds = tableView.bounds
		populateRow(view, withHistory: store!.getHistoryAt(index: indexPath.row)!)
		view.layoutIfNeeded()
		let height = view.viewWithTag(100)!.bounds.size.height + 22
		heightMap[indexPath.row] = height
		return height
	}

	/**
	 * Gets the number of rows to show in the history table.
	 * @param tableView Table whose rows we're counting
	 * @param section Section whose row count we want
	 * @return Number of rows in section
	 */
	func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
		return store!.historyCount
	}

	/**
	 * Gets the cell view for a row in a table.
	 * @param tableView Table whose row we're rendering
	 * @param indexPath Which row we want
	 * @return Hydrated row view
	 */
	func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
		// Get the cell view
		var cell = tableView.dequeueReusableCell(withIdentifier: "history")
		if cell == nil {
			cell = Bundle.main.loadNibNamed("BeginAuditDialogRow", owner: nil, options: nil)![0] as? UITableViewCell
		}

		// Populate and return the view
		populateRow(cell!, withHistory: store!.getHistoryAt(index: indexPath.row)!)
		return cell!
	}
}
