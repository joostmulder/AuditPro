/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.controls;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.auditpro.mobile_client.test.R;


/**
 * Provides common control font functionality
 * https://stackoverflow.com/questions/16648190/how-to-set-a-particular-font-for-a-button-text-in-android
 * @author Eric Ruck
 */
public class CustomFontHelper {

	/**
	 * Sets a font on a textview based on the custom com.my.package:font attribute
	 * If the custom font attribute isn't found in the attributes nothing happens
	 * @param textview View to which property applies
	 * @param context Application context
	 * @param attrs Attributes receives property
	 */
	public static void setCustomFont(TextView textview, Context context, AttributeSet attrs) {
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomFont);
		String font = a.getString(R.styleable.CustomFont_customfont);
		setCustomFont(textview, font, context);
		a.recycle();
	}

	/**
	 * Sets a font on a textview
	 * @param textview View to which property applies
	 * @param font Font to set
	 * @param context Application context
	 */
	public static void setCustomFont(TextView textview, String font, Context context) {
		if(font == null) {
			return;
		}
		Typeface tf = FontCache.get(font, context);
		if(tf != null) {
			textview.setTypeface(tf);
		}
	}
}
