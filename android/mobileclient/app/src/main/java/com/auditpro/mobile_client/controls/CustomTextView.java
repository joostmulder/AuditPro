/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.controls;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;


/**
 * Provides custom styling for the static text control.
 * https://stackoverflow.com/questions/16648190/how-to-set-a-particular-font-for-a-button-text-in-android
 * @author Eric Ruck
 */
public class CustomTextView extends AppCompatTextView {
	public CustomTextView(Context context) {
		super(context);
	}

	public CustomTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		CustomFontHelper.setCustomFont(this, context, attrs);
	}

	public CustomTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		CustomFontHelper.setCustomFont(this, context, attrs);
	}
}
