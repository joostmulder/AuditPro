/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.controls;

import android.content.Context;
import android.graphics.Typeface;

import java.util.Hashtable;


/**
 * Caches font for low memory devices, older OS versions.
 * https://stackoverflow.com/questions/16648190/how-to-set-a-particular-font-for-a-button-text-in-android
 * @author Eric Ruck
 */
class FontCache {

	private static Hashtable<String, Typeface> fontCache = new Hashtable<>();

	public static Typeface get(String name, Context context) {
		Typeface tf = fontCache.get(name);
		if(tf == null) {
			try {
				tf = Typeface.createFromAsset(context.getAssets(), name);
			}
			catch (Exception e) {
				return null;
			}
			fontCache.put(name, tf);
		}
		return tf;
	}
}