/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client;

import android.text.TextUtils;

import com.auditpro.mobile_client.entities.Audit;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

import java.util.ArrayList;


/**
 * Provides helper analytics methods for consistency and compactness.
 * @author Eric Ruck
 */
public final class Analytics {
	/**
	 * Prevents construction.
	 */
	private Analytics() { }

	/**
	 * Logs an event with a single custom attribute.
	 * @param name Event name
	 * @param key Attribute name
	 * @param value Attribute value
	 */
	public static void log(String name, String key, String value) {
		Answers.getInstance().logCustom(new CustomEvent(name).putCustomAttribute(key, value));
	}

	/**
	 * Logs an event with a single custom attribute.
	 * @param name Event name
	 * @param key Attribute name
	 * @param value Attribute value
	 */
	public static void log(String name, String key, boolean value) {
		Answers.getInstance().logCustom(new CustomEvent(name).putCustomAttribute(key, value ? 1 : 0));
	}

	/**
	 * Logs an event for an audit with a single custom attribute.
	 * @param name Event name
	 * @param audit Audit to which event applies
	 * @param key Attribute name
	 * @param value Attribute value
	 */
	public static void log(String name, Audit audit, String key, String value) {
		Answers.getInstance().logCustom(new CustomEvent(name).
				putCustomAttribute("Audit Id", audit.getId().toString()).
				putCustomAttribute(key, value));
	}

	/**
	 * Logs an event for an audit.
	 * @param name Event name
	 * @param audit Audit to which event applies
	 */
	public static void log(String name, Audit audit) {
		Answers.getInstance().logCustom(new CustomEvent(name).
				putCustomAttribute("Audit Id", audit.getId().toString()));
	}

	/**
	 * Logs a filter change.
	 * @param name Which filter
	 * @param options List of selected options, empty for all or null if none
	 * @param text Text filter, empty for all or null if none
	 */
	public static void filter(String name, ArrayList<String> options, String text) {
		CustomEvent event = new CustomEvent("Filter").putCustomAttribute("type", "name");
		if (options != null) {
			if (options.size() == 0) {
				event.putCustomAttribute("options", "all");
			} else {
				event.putCustomAttribute("options", TextUtils.join(",", options));
			}
		}
		if (text != null) {
			event.putCustomAttribute("text", text.isEmpty() ? "{none}" : text);
		}
		Answers.getInstance().logCustom(event);
	}

	/**
	 * Logs an action menu event.
	 * @param type Action type
	 */
	public static void menuAction(String type) {
		menuAction(type, null);
	}

	/**
	 * Logs an action menu event.
	 * @param type Action type
	 * @param details Details or null
	 */
	public static void menuAction(String type, String details) {
		CustomEvent event =
				new CustomEvent("Menu Action").
						putCustomAttribute("type", type);
		if (details != null) {
			event.putCustomAttribute("details", details);
		}
		Answers.getInstance().logCustom(event);
	}
}
