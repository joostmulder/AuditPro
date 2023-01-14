/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.controls;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Provides custom styling for buttons
 * https://stackoverflow.com/questions/16648190/how-to-set-a-particular-font-for-a-button-text-in-android
 * @author Eric Ruck
 */
public class CustomButton extends android.support.v7.widget.AppCompatButton {

	public CustomButton(Context context) {
		super(context);
	}

	public CustomButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		CustomFontHelper.setCustomFont(this, context, attrs);
	}

	public CustomButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		CustomFontHelper.setCustomFont(this, context, attrs);
	}
}