//
//  MainViewController.swift
//  Mobile Client
//
//  Created by Eric Ruck on 12/31/17.
//  Copyright 2017-2019 AuditPro. All rights reserved.
//

import UIKit


/**
 * Provides a common controller with shared services, into which all other
 * page controllers are inserted.
 * @author Eric Ruck
 */
class MainViewController: UIViewController, PageParentProtocol {

	@IBOutlet var pageView: UIView!
	@IBOutlet var pageViewBottom: NSLayoutConstraint!

	@IBOutlet var statusBackgroundView: UIView!
	@IBOutlet var activityView: UIView!
	@IBOutlet var actionBarView: UIView!
	@IBOutlet var navigationButton: UIButton!

	@IBOutlet var menuContainer: UIView!
	@IBOutlet var menuContainerH: NSLayoutConstraint!
	@IBOutlet var menuContainerW: NSLayoutConstraint!

	/** Provides our ordered list of attached pages. */
	private var attachedPages: [BasePage] = []

	/** Indicates if a sync has been requested by a child page. */
	private var syncRequested: Bool = false


	/**
	 * Initializes our view on load.
	 */
    override func viewDidLoad() {
        super.viewDidLoad()

		// Show the login page
		isActivityShowing = false
		pushPage(LoginPage())
    }

	/**
	 * Makes sure we can respond to keyboard events.
	 * @param animated Event animated flag
	 */
	override func viewWillAppear(_ animated: Bool) {
		super.viewWillAppear(animated)

		// Register for keyboard notifications
		NotificationCenter.default.addObserver(self,
			selector: #selector(MainViewController.keyboardWasShown(notification:)),
			name: UIResponder.keyboardWillShowNotification, object: nil)
		NotificationCenter.default.addObserver(self,
			selector: #selector(MainViewController.keyboardWillBeHidden(notification:)),
			name: UIResponder.keyboardWillHideNotification, object: nil)
	}

	/**
	 * Unregisters keyboard event observation.
	 */
	override func viewWillDisappear(_ animated: Bool) {
		super.viewWillDisappear(animated)
		NotificationCenter.default.removeObserver(self,
	  		name: UIResponder.keyboardWillShowNotification, object: nil)
		NotificationCenter.default.removeObserver(self,
	  		name: UIResponder.keyboardWillHideNotification, object: nil)
	}

	/**
	 * Gets the application version string.
	 * @return Application version string from resource bundle
	 */
	var version: String {
		return Bundle.main.infoDictionary!["CFBundleShortVersionString"] as! String
	}

	/** Margin from page view to the bottom of the display area, pixels. */
	private let PAGE_VIEW_BOTTOM_MARGIN: CGFloat = 8

	/**
	 * Adjusts the screen for the keyboard as necessary.
	 */
	@objc func keyboardWasShown(notification: NSNotification) {
		// Handling keyboard depends on current page
		let pageCount = attachedPages.count;
		if (pageCount == 0) {
			// Nothing to do
			return;
		}
		if (attachedPages[pageCount - 1].shrinkForKeyboard) {
			// Need to calculate keyboard exact size due to Apple suggestions
			let info: NSDictionary = notification.userInfo! as NSDictionary
			let keyboardSize = (info[UIResponder.keyboardFrameEndUserInfoKey] as? NSValue)?.cgRectValue.size
			let keyboardHeight = keyboardSize!.height
			if #available(iOS 11.0, *) {
				let newBottom = keyboardHeight - self.view.safeAreaInsets.bottom
				pageViewBottom.constant = newBottom + PAGE_VIEW_BOTTOM_MARGIN
			} else {
				// Fallback on earlier versions
				let newBottom = keyboardHeight + PAGE_VIEW_BOTTOM_MARGIN
				pageViewBottom.constant = newBottom
			}
		}
	}

	/**
	 * Unadjusts screen for dismissed keyboard
	 */
	@objc func keyboardWillBeHidden(notification: NSNotification) {
		pageViewBottom.constant = PAGE_VIEW_BOTTOM_MARGIN
	}

	/**
	 * Provides the activity indicator showing state.
	 */
	var isActivityShowing: Bool {
		get {
			return !activityView.isHidden
		}
		set {
			activityView.isHidden = !newValue
		}
	}

	/**
	 * Determines of location notifications are on (or should be on) based on
	 * the current page stack.
	 * @return Location notifications should be on flag
	 */
	var isLocationEnabled: Bool {
		if let locPage = attachedPages.reversed().first(where: { $0.isLocationNeeded != .keep }) {
			return locPage.isLocationNeeded == .on
		}
		return false
	}

	/**
	 * Pushes a page onto our display stack.
	 * @param push Page to push
	 */
	func pushPage(_ pushPage: BasePage) {
		// Are we covering an active page?
		let size = attachedPages.count
		var lastLocationEnabled = BasePage.LocationNeed.initial
		if (size > 0) {
			// Yes, alert it
			let lastPage = attachedPages[size - 1]
			lastLocationEnabled = lastPage.isLocationNeeded
			lastPage.onPageDisappearing();
		}

		// Add the page to our stack
        pushPage.view.translatesAutoresizingMaskIntoConstraints = false
		addChild(pushPage)
		pushPage.view.frame = pageView.frame
		pageView.addSubview(pushPage.view)
		let pinTop = NSLayoutConstraint(item:pushPage.view!, attribute:.top, relatedBy:.equal, toItem: pageView, attribute:.top, multiplier:1.0, constant:0.0)
		let pinHgt = NSLayoutConstraint(item:pushPage.view!, attribute:.height, relatedBy:.equal, toItem:pageView, attribute:.height, multiplier:1.0, constant:0.0)
		let pinLft = NSLayoutConstraint(item:pushPage.view!, attribute:.left, relatedBy:.equal, toItem:pageView, attribute:.left, multiplier:1.0, constant:0.0)
		let pinWid = NSLayoutConstraint(item:pushPage.view!, attribute:.width, relatedBy:.equal, toItem:pageView, attribute:.width, multiplier:1.0, constant:0.0)
		view.addConstraints([pinTop, pinHgt, pinLft,  pinWid])
		pushPage.didMove(toParent:self)
		setToolbarFor(pushPage)
		attachedPages.append(pushPage)
		updateLocationEnabled(from: lastLocationEnabled, to: pushPage.isLocationNeeded)
	}

	/**
	 * Swaps the current top page with the passed page.
	 * @param swapIn Page to swap with the top
	 */
	func swapPage(_ swapIn: BasePage) {
		popPage()
		pushPage(swapIn)
	}

	/**
	 * Pops a page from our display stack.
	 */
	func popPage() {
		// Validate
		var size = attachedPages.count
		if (size == 0) {
			// Page stack empty
			NSLog("WARN: Attempt to pop empty page stack")
			return
		}

		// Clear activity in case the last page left it set
		isActivityShowing = false

		// Pop it
		size -= 1
		let popped = attachedPages[size]
		let lastLocationEnabled = popped.isLocationNeeded
		popped.onPageDisappearing()
		popped.view.removeFromSuperview()
		popped.removeFromParent()
		attachedPages.remove(at: size)
		if (size > 0) {
			// Setup for the uncovered page
			let show = attachedPages[size - 1]
			setToolbarFor(show)
			updateLocationEnabled(from: lastLocationEnabled, to: show.isLocationNeeded)
			show.onPageAppearing()
		}
	}

	/**
	 * Pop pages off the top of the stack until we get to the target page.
	 * @param targetPageType Target page class type
	 */
	func popTo(_ targetPageType: BasePage.Type) {
		// Search down the stack for the page
		var index = attachedPages.count
		let lastLocationEnabled = index == 0
			? BasePage.LocationNeed.initial
			: attachedPages[index - 1].isLocationNeeded
		while (index >= 0) {
			// Should we stop on this page?
			index -= 1
			let checkPage = attachedPages[index]
			if (checkPage.isKind(of: targetPageType)) {
				// Stop here
				setToolbarFor(checkPage)
				updateLocationEnabled(from: lastLocationEnabled, to: checkPage.isLocationNeeded)
				checkPage.onPageAppearing()
				break
			}

			// Pop this class
			checkPage.onPageDisappearing()
			checkPage.view.removeFromSuperview()
			checkPage.removeFromParent()
			attachedPages.remove(at: index);
		}
	}

	/**
	 * Updates the location enabled status depending on the needs of the prior
	 * and current page.
	 * @param from Location need of outgoing page
	 * @param to Location need of incoming page
	 */
	private func updateLocationEnabled(from: BasePage.LocationNeed, to: BasePage.LocationNeed) {
		if from != .keep && to != .keep && to != from {
			// Update the location enabled statue
			if (to == .on) {
				AppDelegate.startLocationUpdates()
			} else {
				AppDelegate.endLocationUpdates()
			}
		}
	}

	/**
	 * Propagates application becoming active event.
	 */
	func onDidBecomeActive() {
		for page in attachedPages {
			page.onDidBecomeActive()
		}
	}

	/**
	 * Propagates application backgrounding active event.
	 */
	func onDidEnterBackground() {
		for page in attachedPages {
			page.onDidEnterBackground()
		}
	}

	/**
	 * Determines if the passed page is at the top of the stack (showing).
	 * @param testPage Page to test
	 * @return Is top flag
	 */
	func isTopPage(_ testPage: BasePage) -> Bool {
		let index = attachedPages.count
		return (index > 0) && (attachedPages[index - 1] == testPage)
	}

	/**
	 * Requests sync from the main menu.
	 */
	func requestSync() {
		syncRequested = true
		popTo(MainMenuPage.self)
	}

	/**
	 * Indicates if sync has been requested, and resets the request.
	 * @return Sync requested flag
	 */
	func isSyncRequested() -> Bool {
		let res = syncRequested
		syncRequested = false
		return res
	}

	/**
	 * Shows a dialog over the entire application.
	 * @param dialog Controller for dialog to show
	 */
	func showDialog(_ dialog: UIViewController) {
		dialog.view.frame = self.view.bounds
		self.addChild(dialog)
		self.view.addSubview(dialog.view)
	}

	/**
	 * Updates the toolbar state for the passed page
	 * @param page Page to sync with toolbar state
	 */
	private func setToolbarFor(_ page: BasePage?) {
		// Does the page have a title?
		let title = page?.pageName
		if (title == nil) {
			// No title means no action bar
			actionBarView.isHidden = true
			statusBackgroundView.isHidden = true
		} else {
			// Show toolbar
			actionBarView.isHidden = false
			statusBackgroundView.isHidden = false
			navigationButton.setTitle(title, for: .normal)

			// Is there a current menu showing?
			while (menuContainer.subviews.count > 0) {
				// Remove old menu view
				menuContainer.subviews[0].removeFromSuperview()
			}

			// Is there a menu?
			if let menuView = page?.onCreateMenu() {
				// Adjust the container for the menu
				var menuFrame = menuView.frame
				menuContainer.isHidden = false
				menuContainerW.constant = menuFrame.size.width
				menuContainerH.constant = menuFrame.size.height

				// Add the menu to the container
				menuFrame.origin.x = 0
				menuFrame.origin.y = 0
				menuView.frame = menuFrame
				menuContainer.addSubview(menuView)
			} else {
				// Hide container
				menuContainer.isHidden = true
				menuContainerW.constant = 0
				menuContainerH.constant = 0
			}
		}

		// On page transitions make sure the keyboard is hidden
		view.endEditing(true)
	}

	/**
	 * Handles navigation request from the toolbar.
	 */
	@IBAction func onNavigation() {
		let index = attachedPages.count - 1
		if (index < 1) {
			NSLog("Back navigation disallowed, already at top")
		} else if (!attachedPages[index].onBack()) {
			popPage()
		}
	}
}
