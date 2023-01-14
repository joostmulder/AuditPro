/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.pages;


/**
 * Interface required by pages host.
 * @author Eric Ruck
 */
public interface IPageParent {

	/**
	 * Gets the current application version as a display string.
	 * @return Display version
	 */
	String getVersion();

	/**
	 * Pushes a page into our stack.
	 * @param push Page to push
	 */
	void pushPage(BasePage push);

	/**
	 * Swaps the top page with the passed page.
	 * @param swapIn Page to swap with the top page
	 */
	void swapPage(BasePage swapIn);

	/**
	 * Pops the top page in the stack.
	 */
	void popPage();

	/**
	 * Pops pages down to the passed page type
	 * @param targetPage Page type to leave on top
	 */
	void popTo(Class<? extends BasePage> targetPage);

	/**
	 * Tests if the passed page is on top of the display stack.
	 * @param testPage Page to test
	 * @return Page is top, displayed
	 */
	boolean isTopPage(BasePage testPage);

	/**
	 * Handles page attaching to the view stack.
	 * @param attach Attaching page
	 */
	void attachPage(BasePage attach);

	/**
	 * Handles page detaching from the view stack.
	 * @param detach Detaching page
	 */
	void detachPage(BasePage detach);

	/**
	 * Shows or hides the activity spinner in the user interface.
	 * @param isShowing Show activity flag
	 */
	void setActivity(boolean isShowing);

	/**
	 * Requests sync from another page.
	 */
	void requestSync();

	/**
	 * Gets the current sync requested state, and resets the state.
	 * @return Current sync requested state
	 */
	boolean isSyncRequested();
}
