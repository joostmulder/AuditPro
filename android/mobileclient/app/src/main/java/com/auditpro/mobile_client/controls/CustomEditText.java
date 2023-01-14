/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.controls;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;


/**
 * Provides custom styling for edit text.
 * https://stackoverflow.com/questions/16648190/how-to-set-a-particular-font-for-a-button-text-in-android
 * TODO: Add option for clear button to handle update product page with auto decimal
 * @author Eric Ruck
 */
public class CustomEditText extends android.support.v7.widget.AppCompatEditText {
	public CustomEditText(Context context) {
		super(context);
	}

	public CustomEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		CustomFontHelper.setCustomFont(this, context, attrs);
	}

	public CustomEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		CustomFontHelper.setCustomFont(this, context, attrs);
	}
}
