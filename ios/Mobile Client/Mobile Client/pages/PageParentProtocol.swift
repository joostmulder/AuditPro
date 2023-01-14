//
//  PageParentProtocol.swift
//  Mobile Client
//
//  Created by Eric Ruck on 1/7/18.
//  Copyright 2018-2019 AuditPro. All rights reserved.
//

import Foundation
import UIKit


/**
 * Interface required by pages host.
 * @author Eric Ruck
 */
protocol PageParentProtocol : class {
	/**
	 * Gets the current application version as a display string.
	 */
	var version: String { get }

	/**
	 * Pushes a page into our stack.
	 * @param push Page to push
	 */
	func pushPage(_ push: BasePage)

	/**
	 * Swaps the top page with the passed page.
	 * @param swapIn Page to swap with the top page
	 */
	func swapPage(_ swapIn: BasePage)

	/**
	 * Pops the top page in the stack.
	 */
	func popPage()

	/**
	 * Pops pages down to the passed page type
	 * @param targetPageType Page type to leave on top
	 */
	func popTo(_ targetPageType: BasePage.Type)

	/**
	 * Tests if the passed page is on top of the display stack.
	 * @param testPage Page to test
	 * @return Page is top, displayed
	 */
	func isTopPage(_ testPage: BasePage) -> Bool

	/**
	 * Gets and sets the visibility state of the activity spinner in the user interface.
	 */
	var isActivityShowing: Bool { get set }

	/**
	 * Requests sync from another page.
	 */
	func requestSync()

	/**
	 * Gets the current sync requested state, and resets the state.
	 * @return Current sync requested state
	 */
	func isSyncRequested() -> Bool

	/**
	 * Shows a dialog over the entire application.
	 * @param dialog Controller for dialog to show
	 */
	func showDialog(_ dialog: UIViewController)
}
