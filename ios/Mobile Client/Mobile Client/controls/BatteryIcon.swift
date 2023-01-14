//
//  BatteryIcon.swift
//  Mobile Client
//
//  Created by Eric Ruck on 7/18/18.
//  Copyright 2018-2019 AuditPro. All rights reserved.
//

import Foundation
import UIKit


/**
 * Manages a battery icon display.
 * @author Eric Ruck
 */
class BatteryIcon {
	/**
	 * Initializes a new battery display manager for the passed button.
	 * @param button Button to update
	 * @param device Describes the device
	 */
	init(button: UIButton, for device: String) {
		self.button = button
		self.device = device
	}

	/**
	 * Indicates the current visibility state of the icon.
	 */
	var isVisible: Bool {
		get {
			return !button.isHidden
		}
		set {
			button.isHidden = !newValue
			if !newValue {
				lastUpdate = nil
			}
		}
	}

	/**
	 * Sets the current battery state.
	 * @param pct Current battery percentage 0..100 or invalid number shows '?'
	 */
	func updatePercent(_ pct: Int) {
		// Update state
		self.pct = pct
		self.lastUpdate = Date()

		// Determine the battery display
		var pctIcon: String = "baseline_battery_unknown_black_24pt"
		var logRound: String = "unknown"
		if pct > 0 && pct <= 100 {
			// Update the UI based on the valid percent
			if pct < 15 {
				// Low battery alert
				pctIcon = "baseline_battery_alert_black_24pt"
				logRound = "lowbatt"
			} else if pct < 25 {
				// Round to 20%
				pctIcon = "baseline_battery_20_black_24pt"
				logRound = "20%"
			} else if pct < 40 {
				// Round to 30%
				pctIcon = "baseline_battery_30_black_24pt"
				logRound = "30%"
			} else if (pct < 55) {
				// Round to 50%
				pctIcon = "baseline_battery_50_black_24pt"
				logRound = "50%"
			} else if (pct < 70) {
				// Round to 60%
				pctIcon = "baseline_battery_60_black_24pt"
				logRound = "60%"
			} else if (pct < 85) {
				// Round to 80%
				pctIcon = "baseline_battery_80_black_24pt"
				logRound = "80%"
			} else if (pct < 95) {
				// Round to 90%
				pctIcon = "baseline_battery_90_black_24pt"
				logRound = "90%"
			} else {
				// Must be full
				pctIcon = "baseline_battery_full_black_24pt"
				logRound = "full"
			}
		}
		if let image = UIImage(named: pctIcon) {
			// Update the button image
			let tintable = image.withRenderingMode(.alwaysTemplate)
			button.setBackgroundImage(tintable, for: .normal)
			button.tintColor = UIColor.init(red: 0.61, green: 0.80, blue: 0.33, alpha: 1.0)

			// Log current battery setting
			NSLog("%@ battery level at %d displaying %@", device, pct, logRound)
		} else {
			// Failed to load battery icon
			NSLog("Failed to load %@ battery icon %@ for %d percent", device, pctIcon, pct)
		}
	}

	/**
	 * Gets a localized description of the current battery state.
	 */
	var describeState: String {
		var format: String = "%@ Battery at %d%%"
		if !isVisible {
			format = "No %@ Battery Information"
		} else if (pct <= 0) || (pct > 100) {
			format = "Unknown %@ Battery State"
		} else if (pct >= 95) {
			format = "Full Charge on %@ Battery"
		}
		return String(format: format, device, pct)
	}

	/**
	 * Indicates if we need a battery update.
	 * @param updateSeconds Time to update battery
	 * @return Update needed flag
	 */
	func isUpdateNeeded(afterSeconds updateSeconds: Int) -> Bool {
		return
			lastUpdate == nil ||
			(Int)(lastUpdate!.timeIntervalSinceNow * -1) >= updateSeconds
	}

	/** References button with battery image. */
	private let button: UIButton

	/** Describes the device. */
	private let device: String

	/** Provides the time of the last update received. */
	private var lastUpdate: Date?

	/** Provides the current battery state. */
	private var pct: Int = 0
}
