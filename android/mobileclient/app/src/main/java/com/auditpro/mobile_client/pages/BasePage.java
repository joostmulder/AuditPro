/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.pages;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;


/**
 * Provides a base class with common functionality for all page fragments.
 * @author Eric Ruck
 */
public class BasePage extends Fragment {
	/**
	 * Provides default constructor.
	 */
	public BasePage() { }

	/**
	 * Gets our parent page.
	 * @return Parent page
	 */
	public IPageParent getParent() {
		return pageParent;
	}

	/**
	 * Safely shows page, checking for state race condition.
	 * @param page Page to show
	 */
	public void showPage(BasePage page) {
		IPageParent parent = getParent();
		if (parent != null) {
			parent.pushPage(page);
		}
	}

	/**
	 * Gets the display name for this page.  Returns null if no name should be displayed.
	 * @param context Application context for string resource lookup
	 * @return Page name or null
	 */
	public String getPageName(Context context) {
		return null;
	}

	/**
	 * Gets a flag indicating that the back navigation icon should be shown next to the page
	 * name.  Does nothing if there is no page name.
	 * @return Show navigation flag
	 */
	public boolean showNavigation() {
		return true;
	}

	/**
	 * Gets a flag indicating that the application window should shrink when the keyboard appears
	 * (vs the keyboard overlapping the bottom of the window).
	 * @return Shrink keyboard flag
	 */
	public boolean shrinkForKeyboard() {
		return false;
	}

	/**
	 * Default implementation of page appearing event handler, override to do something useful.
	 * These aren't quite like the default fragment lifecycle events, as they are called not only
	 * when the pages are actually appearing on creation and disappearing on destruction, but also
	 * when the page is uncovered (and covered) by actions against the parent's page stack.
	 */
	public void onPageAppearing() {
		// Subclasses overload for functionality
	}

	/**
	 * Default implementation of page disappearing event handler, override to do something useful.
	 * See #onPageAppearing for more information.
	 */
	public void onPageDisappearing() {
		// Subclasses overload for functionality
	}

	/**
	 * Optionally handles back navigation.  Return true to prevent default navigation behavior.
	 * @return Handled flag
	 */
	public boolean onBack() {
		// Subclass overload for functionality
		return false;
	}

	/**
	 * Optionally provides a menu for when tge page is displaying.  By default, no menu shows,
	 * override to inflate a menu resource into the passed control, and return true.
	 * @param inflater Inflater to use on menu resource
	 * @param menu Parent receives menu items
	 * @return Display menu flag
	 */
	public boolean onCreateMenu(MenuInflater inflater, Menu menu) {
		return false;
	}

	/**
	 * Delegates handling of a menu item selection.
	 * @param item Item selected
	 * @return Handled flag
	 */
	public boolean onMenuItem(MenuItem item) {
		return false;
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		view.setClickable(true);
		view.setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.white));
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		onPageAppearing();
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		if (context instanceof IPageParent) {
			pageParent = (IPageParent) context;
			pageParent.attachPage(this);
		} else {
			throw new RuntimeException(context.toString()
					+ " must implement IPageParent");
		}
	}

	@Override
	public void onDetach() {
		super.onDetach();
		pageParent.detachPage(this);
		pageParent = null;
	}

	private IPageParent pageParent;
}
