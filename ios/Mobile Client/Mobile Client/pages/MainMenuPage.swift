//
//  MainMenuPage.swift
//  Mobile Client
//
//  Created by Eric Ruck on 1/6/18.
//  Copyright 2018-2019 AuditPro. All rights reserved.
//

import UIKit


/**
 * Manages the main menu page controller of the application.
 * @author Eric Ruck
 */
class MainMenuPage: BasePage {

	// Control outlets
	@IBOutlet weak var syncButton: UIButton!
	@IBOutlet weak var auditButton: UIButton!
	@IBOutlet weak var versionLabel: UILabel!
	@IBOutlet weak var syncRequiredLabel: UILabel!

	/** Options to sync when this page displays, forward or back navigation. */
	private enum InitSyncOption { case
		none, // Don't sync
		ask, // Ask before sync
		sync, // Sync without asking
		login, // Only sync if we're logged in
		page // Handle sync request from another page
	}

	/** Current sync on page display option. */
	private var initSync = InitSyncOption.none;

	// Audit options
	private enum AuditStatus: String { case request = "Request", continueCurrent = "ContinueCurrent", endNew = "EndNew" }

	// Current web API client
	private var client: ApiClient? = nil


	/**
	 * Populates the view when it initially loads.
	 */
    override func viewDidLoad() {
        super.viewDidLoad()

		// Determine auto sync based on current state
		let autoSync = Security.optSetting(name: Security.SETTING_AUTOSYNC_WIFI, defaultValue: false)
		initSync = autoSync && AppDelegate.isWifiConnected ? .sync : .none
    }

	/**
	 * Updates when we attach to the parent.
	 * @param parent Parent view controller
	 */
	override func didMove(toParent parent: UIViewController?) {
		if (parent != nil) {
			// Initialize the version
			versionLabel.text = String.init(format: versionLabel.text!, host!.version)
		}
	}

	/**
	 * Updates UI when the page appears.
	 */
	override func onPageAppearing() {
		// Is there an audit in progress?
		let auditTitle = getOpenAudit() == nil
			? "AUDIT STORE"
			: "CONTINUE AUDIT"
		UIView.performWithoutAnimation {
			auditButton.setTitle(auditTitle, for: .normal)
			auditButton.layoutIfNeeded()
		}

		// Display audit counts to send in store button
		updateAuditCount();

		// Should we sync now?
		var syncNow = false
		if (initSync == .none) && (host?.isSyncRequested() ?? false) {
			initSync = .page
		}
		switch (initSync) {
			case .ask:
				// Ask the user if they want to sync now
				let alert = UIAlertController(title: "Sync to Server",
					message: "Would you like to sync now?  You may be using your personal data plan.",
					preferredStyle: .alert)
				alert.addAction(UIAlertAction(title: "Cancel", style: .cancel, handler: nil))
				alert.addAction(UIAlertAction(title: "Sync", style: .default, handler: { action in
					self.performSync()
				}))
				present(alert, animated: true)
				break
			case .login:
				// Sync now if we're logged in
				syncNow = AppDelegate.isInSession
				break
			case .sync:
				// Always sync now
				syncNow = true
				break
			case .page:
				// Show regular interface as necessary
				onSync()
				syncNow = false
				break
			default:
				// Don't sync
				syncNow = false
		}

		// Reset sync
		initSync = .none;
		if (syncNow) {
			// Perform sync
			performSync();
		}
	}

	/**
	 * Suspends API requests when we're disappearing.
	 */
	override func onPageDisappearing() {
		// Make sure no API is running
		client?.cancel()
	}

	/**
	 * Turns on location to end audit in progress, prime the pump for
	 * the some of the next child pages.
	 */
	override var isLocationNeeded: BasePage.LocationNeed {
		return .on
	}

	/**
	 * Handles tap on the sync button.
	 */
	@IBAction func onSync() {
		// Do we have a data connection?
		if (!AppDelegate.isNetworkConnected) {
			// No connection
			view.makeToast(message: "Please connect to the Internet before syncing.")
			return
		}

		// Have we authenticated?
		if (!AppDelegate.isInSession) {
			// We need to log in before we can sync
			initSync = InitSyncOption.login;
			host!.pushPage(LoginPage(sessionFlag: true))
			return
		}

		// Synchronize now
		performSync()
	}

	/**
	 * Handles tap on the audit button.
	 */
	@IBAction func onAudit() {
		performAudit(.request)
	}

	/**
	 * Handles tap on the review button.
	 */
	@IBAction func onReview() {
		host?.pushPage(ReviewPage())
	}

	/**
	 * Handles tap on the logout button.
	 */
	@IBAction func onLogout() {
		AppDelegate.sessionToken = nil
		host?.popPage()
	}

	/**
	 * Handles tap on the help button.
	 */
	@IBAction func onHelp() {
		host?.pushPage(HelpPage(helpFile: "MainHelp.html"))
	}

	/**
	 * Handles tap on the settings button.
	 */
	@IBAction func onSettings() {
		host?.pushPage(SettingsPage())
	}

	/**
	 * Gets the current audit in process, if there is one.
	 * @return The open audit or nil if none
	 */
	private func getOpenAudit() -> Audit? {
		let db = AuditDatabase()!
		return db.resumeAudit(userId: Security.userId)
	}

	/**
	 * Begins a new audit, or optionally resumes an audit in progress.
	 * @param status Reqiested audit action option
	 */
	private func performAudit(_ status: AuditStatus) {
		// Make sure we have stores
		let db = StoresDatabase()!
		if (db.isEmpty()) {
			view.makeToast(message: "Please synchronize now to load stores that can be audited.")
			return
		}

		// Is there an audit to continue?
		let openAudit = getOpenAudit()

		// Do we need to confirm the request?
		var statusDescr = status.rawValue
		if ((status == .request) && (openAudit != nil)) {
			// Display audit; offer continue, close and cancel options
			statusDescr = "AlreadyOpen"
			let message = String(format: "An audit of %@ was started on %@.",
				openAudit!.storeDescr,
				BaseDatabase.readDateTime(openAudit?.auditStartedAt))
			let alert = UIAlertController(title: nil, message: message, preferredStyle: .actionSheet)
			alert.addAction(UIAlertAction(title: "Continue This Audit", style: .default) { action in
				self.performAudit(.continueCurrent)
			})
			alert.addAction(UIAlertAction(title: "End This Audit And Start A New One", style: .default) { action in
				self.performAudit(.endNew)
			})
			alert.addAction(UIAlertAction(title: "Cancel", style: .cancel))
			present(alert, animated: true)
		} else if (status == .request) {
			// Show the stores selection
			host?.pushPage(SelectStorePage())
		} else if (status == .continueCurrent) {
			// Go straight to the products page
			host?.pushPage(SelectProductPage(audit: openAudit!))
		} else if (status == .endNew) {
			// Process request to complete open audit
			if Security.optSetting(name: Security.SETTING_NO_NOTES_WARNING, defaultValue: false) {
				// Does the audit have notes?
				let adb = AuditDatabase()
				if (adb?.getNotes(forAudit: openAudit!)?.contents ?? "").count == 0 {
					// Verify that the user wants to continue with no notes
					let alert = UIAlertController(title: "No Notes",
						message: "You have not entered any notes for this audit. Are you sure you want to end the audit without notes?", preferredStyle: .alert)
					alert.addAction(UIAlertAction(title: "Continue Audit To Add Notes", style: .default) {
						action in self.performAudit(.continueCurrent)
					})
					alert.addAction(UIAlertAction(title: "Close Without Notes", style: .default){
						action in self.completeOpenAudit(openAudit!)
					})
					alert.addAction(UIAlertAction(title: "Cancel", style: .cancel))
					present(alert, animated: true)
					return
				}
			}

			// Passed validation, ok to complete
			completeOpenAudit(openAudit!)
		}

		// Provide analytics
		Analytics.log(name: "On Audit", key: "Action", value: statusDescr)
	}

	/**
	 * Completes the open audit, and starts a new one.  Assumes that we've already
	 * checked for the "no notes" situation.
	 * @param openAudit Audit being closed
	 */
	private func completeOpenAudit(_ openAudit: Audit) {
		let db = AuditDatabase()
		let lastLocation = AppDelegate.lastLocation
		_ = db!.completeAudit(audit: openAudit,
			latitude: lastLocation?.coordinate.latitude,
			longitude: lastLocation?.coordinate.longitude,
			endTime: Date())

		// Now request a new audit
		performAudit(.request);
	}

	/**
	 * Executes a synchronization with the remote web services now.
	 */
	private func performSync() {
		// Get the audits to send
		let db = AuditDatabase()
		let audits = db?.getCompleteAudits(userId: Security.userId)

		// Kick off the sync process
		host?.isActivityShowing = true
		client = ApiClient(token: AppDelegate.sessionToken)
		syncNextAudit(db!, audits)
	}

	/**
	 * Synchronizes the next completed audit in the list, if any.
	 * @param audits List of audits to synchronize
	 */
	private func syncNextAudit(_ db: AuditDatabase, _ audits: [Audit]?) {
		// Is there an audit to sync?
		if (client?.isCanceled)! {
			// Sync process canceled
			client = nil
			return
		}
		if ((audits == nil) || (audits!.count == 0)) {
			// No, move on to the stores
			syncStores()
			return
		}

		// Post the next audit
		let syncAudit = audits![0]
		let json = db.serialize(audit: syncAudit)
		client!.postPayload(auditJson: json!, completion: { client in
			if client.message != nil {
				// Failed to sync audit
				self.syncComplete(errorMessage: client.message)
			} else {
				// Remove synced audit
				db.delete(audit: syncAudit)
				if (audits!.count > 1) {
					// Remove synced audit and move on
					self.syncNextAudit(db, Array(audits![1..<audits!.count]))
				} else {
					// Move on to the stores
					self.syncStores()
				}
			}
		})
	}

	/**
	 * Synchronizes the stores.
	 */
	private func syncStores() {
		if (client?.isCanceled)! {
			// Sync process canceled
			client = nil
			return
		}

		// Get new stores for this user
		client?.getStores(completion: { client in
			if (client.message != nil) {
				// Failed to get stores
				self.syncComplete(errorMessage: client.message)
			} else {
				// Move on to the products
				self.syncProducts(stores: client.response as! [Store])
			}
		})
	}

	/**
	 * Syncronizes the products.
	 * @param Stores that go with the products we're about to sync
	 */
	private func syncProducts(stores: [Store]) {
		if (client?.isCanceled)! {
			// Sync process canceled
			client = nil
			return
		}

		// Get new products
		client?.getProducts(completion: { client in
			if (client.message != nil) {
				// Failed to get products
				self.syncComplete(errorMessage: client.message)
			} else {
				// Update the database
				let db = StoresDatabase()
				if (!db!.applyRefresh(stores: stores, products: client.response as! [Product])) {
					// Failed to update the database
					self.syncComplete(errorMessage: "Failed to save the stores and products to the database")
				} else {
					// Sync completed
					Security.setStoreVersionOnSync(db!.version)
					self.syncComplete()
				}
			}
		})
	}

	/**
	 * Completes the synchronization process.
	 * @param errorMessage Message on failure or nil on success
	 */
	private func syncComplete(errorMessage: String? = nil) {
		client = nil
		if host == nil {
			// Nothing to do
			return
		}
		DispatchQueue.main.async {
			// Update the user interface
			self.host!.isActivityShowing = false
			self.updateAuditCount()
			if (errorMessage != nil) {
				// Show the error
				self.view.makeToast(message: errorMessage!)
			}
		}
	}

	/**
	 * Updates the user interface with the current completed audit count,
	 * indicating how many audits are ready to sync. Also updates visibility of
	 * version sync message.
	 */
	private func updateAuditCount() {
		// Get the initial button label text
		let db = AuditDatabase()
		let auditCount = db?.completeCount(userId: Security.userId)
		let title = ((auditCount != nil) && (auditCount! > 0))
			? String(format: "SYNC TO SERVER (%d AUDIT%@)",
				auditCount!, (auditCount! == 1) ? "" : "S")
			: "SYNC TO SERVER"
		UIView.performWithoutAnimation {
			syncButton.setTitle(title, for: .normal)
			syncButton.layoutIfNeeded()
		}

		// Adjust visibility of the version sync message
		if let version = StoresDatabase()?.version {
			syncRequiredLabel.isHidden = !Security.isSyncNeeded(forVersion: version)
		} else {
			syncRequiredLabel.isHidden = true
		}
	}
}
