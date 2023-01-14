//
//  BasePage.swift
//  Mobile Client
//
//  Created by Eric Ruck on 1/6/18.
//  Copyright 2018-2019 AuditPro. All rights reserved.
//

import UIKit

/**
 * Provides a base class with common functionality for all page controllers.
 * @author Eric Ruck
 */
class BasePage: UIViewController {

	/**
	 * Identifies possible location requirements for a page.
	 */
	enum LocationNeed { case
		on,   // Needs location on
		off,  // Location not required
		keep  // Leave location in current state
		static let initial = LocationNeed.off
	}

	/**
	 * Gets the host to our page.
	 */
	var host: PageParentProtocol? {
		return self.parent as? PageParentProtocol
	}

	/**
	 * Gets the display name for this page, nil if no name should be displayed.
	 */
	var pageName: String? {
		return nil
	}

	/**
	 * Gets a flag indicating that the back navigation icon should be shown next to the page
	 * name.  Does nothing if there is no page name.
	 */
	var showNavigation: Bool {
		return true
	}

	/**
	 * Gets a flag indicating that the application window should shrink when the keyboard appears
	 * (vs the keyboard overlapping the bottom of the window).
	 */
	var shrinkForKeyboard: Bool {
		return false
	}

	/**
	 * Default implementation of page appearing event handler, override to do something useful.
	 * These aren't quite like the default fragment lifecycle events, as they are called not only
	 * when the pages are actually appearing on creation and disappearing on destruction, but also
	 * when the page is uncovered (and covered) by actions against the parent's page stack.
	 */
	func onPageAppearing() {
		// Subclasses overload for functionality
	}

	/**
	 * Default implementation of page disappearing event handler, override to do something useful.
	 * See #onPageAppearing for more information.
	 */
	func onPageDisappearing() {
		// Subclasses overload for functionality
	}

	/**
	 * Handles application backgrounding active event.
	 */
	func onDidEnterBackground() {
		// Subclasses overload for functionality
	}

	/**
	 * Handles application becoming active event.
	 */
	func onDidBecomeActive() {
		// Subclasses overload for functionality
	}

	/**
	 * Optionally handles back navigation.  Return true to prevent default navigation behavior.
	 * @return Handled flag
	 */
	func onBack() -> Bool {
		// Subclass overload for functionality
		return false
	}


	/**
	 * Populates the view when it loads.
	 */
    override func viewDidLoad() {
        super.viewDidLoad()
		onPageAppearing()
    }

	/**
	 * Optionally provides a menu for when the page is displaying.  By default,
	 * no menu shows, override to provide a view for the menu area top right
	 * in the status bar.  Superclass responsible for wiring up and handling
	 * events on controls in the view.
	 * @return Menu view or nil if none
	 */
	func onCreateMenu() -> UIView? {
		return nil
	}

	/**
	 * Returns the location needs of this page.  By default turned off, as
	 * location services consume battery life.  Override to turn on location
	 * services for a page, or use the .keep value if the location state should
	 * be left unchanged from the prior page.
	 */
	var isLocationNeeded: LocationNeed {
		return .off
	}
}
