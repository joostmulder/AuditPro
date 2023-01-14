/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.pages;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.auditpro.mobile_client.Analytics;
import com.auditpro.mobile_client.security.Security;
import com.auditpro.mobile_client.test.R;


/**
 * Manages the settings page of the application.
 * @author Eric Ruck
 */
public class SettingsPage extends BasePage  {

	/**
	 * Required default constructor for fragment.
	 */
	public SettingsPage() { }


	/**
	 * Creates fragment view.
	 * @param inflater Inflater to use on layout
	 * @param container Container receives inflated view
	 * @param savedInstanceState Optional instance state
	 * @return View for fragment
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_settings_page, container, false);
	}

	/**
	 * Initializes created view.
	 * @param view Created view parent
	 * @param savedInstanceState Optional instance state
	 */
	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Wire up settings controls
		final Security sec = new Security(getContext().getApplicationContext());

		// Initialize auto sync
		Switch autoSync = view.findViewById(R.id.autoSyncSwitch);
		autoSync.setChecked(sec.optSettingBool(Security.SETTING_AUTOSYNC_WIFI, false));
		autoSync.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Analytics.log("Settings", Security.SETTING_AUTOSYNC_WIFI, isChecked);
				sec.setSetting(Security.SETTING_AUTOSYNC_WIFI, isChecked);
			}
		});

		// Initialize auto decimal
		Switch autoDecimal = view.findViewById(R.id.autoDecimalSwitch);
		autoDecimal.setChecked(sec.optSettingBool(Security.SETTING_AUTO_DECIMAL, true));
		autoDecimal.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Analytics.log("Settings", Security.SETTING_AUTO_DECIMAL, isChecked);
				sec.setSetting(Security.SETTING_AUTO_DECIMAL, isChecked);
			}
		});
	}

	/**
	 * Shows the settings title for the page.
	 * @param context Application context for string resource lookup
	 * @return Settings title
	 */
	@Override
	public String getPageName(Context context) {
		return context.getString(R.string.page_name_settings);
	}
}
