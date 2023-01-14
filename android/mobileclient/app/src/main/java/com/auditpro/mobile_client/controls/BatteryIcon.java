package com.auditpro.mobile_client.controls;


import android.content.Context;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.auditpro.mobile_client.test.R;

import java.util.Date;

/**
 * Manages a battery icon display.
 * @author Eric Ruck
 */
public class BatteryIcon {

	/** Identifies our messages in the log. */
	private static final String LOG_TAG = "BatteryIcon";

	/** References UI item. */
	private MenuItem item;

	/** References UI image view. */
	private ImageView image;

	/** Identifies the device. */
	private String device;

	/** References current battery state. */
	private int pct;

	/** Desired visible state of the indicator. */
	private boolean visible;

	/** Provides the time of the last update received. */
	private Date lastUpdate;

	/**
	 * Initializes a new battery display manager for the passed menu item.
	 * @param item Menu item to update
	 * @param device Identifies the device
	 */
	public BatteryIcon(MenuItem item, String device) {
		this.item = item;
		this.device = device;
		setVisible(false);
	}

	/**
	 * Initializes a new battery display manager for the passed menu item.
	 * @param image Image view to update
	 * @param device Identifies the device
	 */
	public BatteryIcon(ImageView image, String device) {
		this.image = image;
		this.device = device;
		visible = image.getVisibility() == View.VISIBLE;
	}

	/**
	 * Adjust the visibility of the battery indicator.
	 * @param visible Desired visible state
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
		if (item != null) {
			item.setVisible(visible);
		}
		if (image != null) {
			image.setVisibility(visible ? View.VISIBLE : View.GONE);
		}
		if (!visible) {
			lastUpdate = null;
		}
	}

	/**
	 * Sets the current battery state.
	 * @param pct Current battery percentage 0..100 or invalid number shows '?'
	 */
	public void setValue(int pct) {
		// Update state
		this.pct = pct;
		this.lastUpdate = new Date();

		// Update the UI based on the percent
		int pctIcon;
		String logRound;
		if ((pct <= 0) || (pct > 100)) {
			// Invalid, set unknown value
			pctIcon = R.drawable.baseline_battery_unknown_white_24dp;
			logRound = "unknown";
		} else if (pct < 15) {
			// Low battery alert
			pctIcon = R.drawable.baseline_battery_alert_white_24dp;
			logRound = "lowbatt";
		} else if (pct < 25) {
			// Round to 20%
			pctIcon = R.drawable.baseline_battery_20_white_24dp;
			logRound = "20%";
		} else if (pct < 40) {
			// Round to 30%
			pctIcon = R.drawable.baseline_battery_30_white_24dp;
			logRound = "30%";
		} else if (pct < 55) {
			// Round to 50%
			pctIcon = R.drawable.baseline_battery_50_white_24dp;
			logRound = "50%";
		} else if (pct < 70) {
			// Round to 60%
			pctIcon = R.drawable.baseline_battery_60_white_24dp;
			logRound = "60%";
		} else if (pct < 85) {
			// Round to 80%
			pctIcon = R.drawable.baseline_battery_80_white_24dp;
			logRound = "80%";
		} else if (pct < 95) {
			// Round to 90%
			pctIcon = R.drawable.baseline_battery_90_white_24dp;
			logRound = "90%";
		} else {
			// Must be full
			pctIcon = R.drawable.baseline_battery_full_white_24dp;
			logRound = "full";
		}
		if (item != null) {
			item.setIcon(pctIcon);
		}
		if (image != null) {
			image.setImageDrawable(image.getContext().getDrawable(pctIcon));
		}

		// Log current battery setting
		Log.i(LOG_TAG, String.format("%s battery level at %d displaying %s", device, pct, logRound));
	}

	/**
	 * Gets a localized description of the current battery state.
	 * @param ctx Application context
	 * @return Description of current battery state
	 */
	public String describeState(Context ctx) {
		int formatStringId;
		if (((item != null) && !item.isVisible()) || ((image != null) && (image.getVisibility() != View.VISIBLE))) {
			formatStringId = R.string.battery_state_none;
		} else if ((pct <= 0) || (pct > 100)) {
			formatStringId = R.string.battery_state_unknown;
		} else if (pct >= 95) {
			formatStringId = R.string.battery_state_full;
		} else {
			formatStringId = R.string.battery_state_pct;
		}
		return ctx.getString(formatStringId, device, pct);
	}

	/**
	 * Indicates if we need a battery update.
	 * @param updateSeconds Time to update battery
	 * @return Update needed flag
	 */
	public boolean isUpdateNeeded(int updateSeconds) {
		return lastUpdate == null || (int) (new Date().getTime() - lastUpdate.getTime()) >= (updateSeconds * 1000);
	}

	/**
	 * Attaches a menu item resource to this indicator, replacing any previous resource.
	 * @param item Menu item resource to attach
	 */
	public void attach(MenuItem item) {
		// Attaches to a new menu item resource
		this.item = item;
		item.setVisible(visible);
		setValue(pct);
	}
}
