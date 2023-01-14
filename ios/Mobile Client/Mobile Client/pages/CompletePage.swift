//
//  CompletePage.swift
//  Mobile Client
//
//  Created by Eric Ruck on 12/31/17.
//  Copyright 2017-2019 AuditPro. All rights reserved.
//

import UIKit


/**
 * Manages the page to complete an audit.
 * @author Eric Ruck
 */
class CompletePage: BasePage {

	@IBOutlet var auditNameLabel: UILabel?
	@IBOutlet var notesButton: UIButton?
	@IBOutlet var printBattery: UIButton?
	@IBOutlet var pbWidth: NSLayoutConstraint?
	@IBOutlet var pbSpace: NSLayoutConstraint?

	private let completeTime = Date()
	private var audit: Audit? = nil
	private var batteryIcon: BatteryIcon? = nil


	/**
	 * Associates our page with an audit to complete.
	 * @param audit Audit to complete
	 */
	convenience init(audit: Audit) {
		self.init()
		self.audit = audit
	}

	/**
	 * Initializes page views on load from resources.
	 */
    override func viewDidLoad() {
        super.viewDidLoad()

		// Initialize the audit name
		auditNameLabel?.text = audit?.description

		// Setup the printer battery level
		batteryIcon = BatteryIcon(button: printBattery!, for: "Printer")
		batteryIcon?.updatePercent(-1)

		// Begin query for printer battery status
		PrinterBatteryAction().readBattery() { batteryStatus in
			// Update UI for returned status
			DispatchQueue.main.async {
				self.batteryIcon?.updatePercent(batteryStatus)
			}
		}
	}

	/**
	 * Gets the title to display for this page.
	 * @return Complete page title
	 */
	override var pageName: String? {
		return "Complete Audit"
	}

	/**
	 * Ensure we're displaying the correct labels returning to this page.
	 */
	override func onPageAppearing() {
		// Make sure we're receiving location updates
		super.onPageAppearing()

		// Make sure notes button is displaying the correct title
		let db = AuditDatabase()
		let title = (db?.getNotes(forAudit: audit!)?.contents ?? "").count == 0
			? "ADD NOTES"
			: "EDIT NOTES"
		UIView.performWithoutAnimation {
			notesButton?.setTitle(title, for: .normal)
			notesButton?.layoutIfNeeded()
		}
	}

	/**
	 * Turns on location to annotate closing audit.
	 */
	override var isLocationNeeded: BasePage.LocationNeed {
		return .on
	}

	/**
	 * Handles the request to print the reorder form.
	 */
	@IBAction func onPrint() {
		// Create the receipt to print
		let receipt = Receipt(customerName: Security.clientName,
			storeName: audit!.storeDescr,
			auditStamp: BaseDatabase.readDateTime(completeTime))
		var products: [Product]? = nil
		if let db = StoresDatabase() {
			products = db.getProductsForStore(storeId: audit!.storeId)
		}
		if let db = AuditDatabase() {
			_ = db.populateReceipt(receipt: receipt, audit: audit!, products: products ?? [])
		}

		// Start printing
		Analytics.log(name: "Print", audit: audit!)
		let action = PrintReceiptAction()
		host?.isActivityShowing = true
		action.printReceipt(receipt) {
			self.host?.isActivityShowing = false
			if let error = action.errorMessage {
				self.view.makeToast(message: error)
			}
		}
	}

	/**
	 * Edits the notes associated with this audit.
	 */
	@IBAction func onNotes() {
		host?.pushPage(NotesPage(audit: audit!))
	}

	/**
	 * Closes the audit and returns to the main menu (or displays a toast if there's an error).
	 * @param confirmGps Flags that we should check with the user to wait on the GPS
	 */
	@IBAction func onClose() {
		// Process request to complete open audit
		if !Security.optSetting(name: Security.SETTING_NO_NOTES_WARNING, defaultValue: false) {
			// No need to check notes, skip to GPS check
			onCloseCheckGPS()
			return
		}
		let db = AuditDatabase()
		if (db?.getNotes(forAudit: audit!)?.contents ?? "").count > 0 {
			// Notes checks, skip to GPS check
			onCloseCheckGPS()
			return
		}

		// Verify that the user wants to continue with no notes
		let alert = UIAlertController(title: "No Notes",
			message: "You have not entered any notes for this audit. Are you sure you want to end the audit without notes?", preferredStyle: .alert)
		alert.addAction(UIAlertAction(title: "Add Notes", style: .default) {
			action in self.onNotes()
		})
		alert.addAction(UIAlertAction(title: "Close Without Notes", style: .default){
			action in self.onCloseCheckGPS()
		})
		alert.addAction(UIAlertAction(title: "Cancel", style: .cancel))
		present(alert, animated: true)
	}

	/**
	 * Displays printer battery details toast.
	 */
	@IBAction func onPrintBattery() {
		if let message = batteryIcon?.describeState {
			view.makeToast(message: message)
		}
	}

	/**
	 * Completes close process by verifying that we either have a GPS location,
	 * or the user decided to close without one.
	 */
	private func onCloseCheckGPS() {
		// Do we have a GPS fix?
		let lastLocation = AppDelegate.lastLocation
		if (lastLocation != nil) {
			// Close with the current location
			performClose(latitude: lastLocation?.coordinate.latitude, longitude: lastLocation?.coordinate.longitude)
			return
		}

		// Check with the user to close without GPS
		let alert = UIAlertController(title: "No GPS Position",
			message: "We have not received a GPS position for the audit completion.  Would you like to wait, or send without GPS?",
			preferredStyle: .alert)
		alert.addAction(UIAlertAction(title: "Wait", style: .cancel))
		alert.addAction(UIAlertAction(title: "Close Without GPS", style: .default) { action in
			self.performClose(latitude: nil, longitude: nil)
		})
		present(alert, animated: true)
	}

	/**
	 * Closes this audit out in the database.
	 */
	private func performClose(latitude: Double?, longitude: Double?) {
		// Complete the audit
		guard
			let db = AuditDatabase(),
			db.completeAudit(audit: audit!, latitude: latitude, longitude: longitude, endTime: completeTime)
		else {
			// Show error
			view.makeToast(message: "Failed to update audit to completed in database, please contact customer support.")
			return
		}

		// Leave the audit user interface
		host?.popTo(MainMenuPage.self)
	}
}
