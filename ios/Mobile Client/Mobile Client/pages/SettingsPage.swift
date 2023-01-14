//
//  SettingsPage.swift
//  Mobile Client
//
//  Created by Eric Ruck on 5/11/18.
//  Copyright 2018-2019 AuditPro. All rights reserved.
//

import UIKit


/**
 * Manages the page to allow the user to view and edit settings.
 * @author Eric Ruck
 */
class SettingsPage : BasePage {
	@IBOutlet var autoSyncSwitch: UISwitch? = nil
	@IBOutlet var autoDecimalSwitch: UISwitch? = nil

	/**
	 * Returns our page display name.
	 * @return Page display name
	 */
	override var pageName: String? {
		return "Settings"
	}

	/**
	 * Initializes controls when the view loads.
	 */
	override func viewDidLoad() {
		super.viewDidLoad()

		// Initialize option switches
		autoSyncSwitch?.setOn(Security.optSetting(name: Security.SETTING_AUTOSYNC_WIFI, defaultValue: false), animated: false)
		autoDecimalSwitch?.setOn(Security.optSetting(name: Security.SETTING_AUTO_DECIMAL, defaultValue: true), animated: false)
	}

	/**
	 * Preserves options on leaving.
	 * @return False to allow default navigation
	 */
	override func onBack() -> Bool {
		// Keep the settings
		Security.setSetting(name: Security.SETTING_AUTOSYNC_WIFI, value: autoSyncSwitch?.isOn ?? false)
		Security.setSetting(name: Security.SETTING_AUTO_DECIMAL, value: autoDecimalSwitch?.isOn ?? true)

		// Normal navigation
		return false
	}

	/**
	 * Keeps location turned on for the main menu while we're displayed
	 */
	override var isLocationNeeded: BasePage.LocationNeed {
		return  .keep
	}
}
