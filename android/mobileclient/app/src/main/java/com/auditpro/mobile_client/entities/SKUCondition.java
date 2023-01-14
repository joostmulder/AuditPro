/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.entities;


import android.util.Log;
import android.util.SparseArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Holds a configured SKU response option.
 * @author Eric Ruck
 */
public class SKUCondition {

	private static final String LOG_TAG = "SKUCondition";

	private static final String ATTRIB_ID = "sku_condition_id";
	private static final String ATTRIB_NAME = "sku_condition_name";
	private static final String ATTRIB_DESCR = "sku_condition_description";

	private int conditionId;
	private String name;
	private String description;


	/**
	 * Parses an array of SKU conditions from a JSON source.  Returns null if the source is empty,
	 * null or invalid.  If some (but not all) of the individual conditions are invalid, they
	 * will be logged and skipped.
	 * @param source JSON source array
	 * @return Parsed SKU conditions or null
	 */
	public static SparseArray<SKUCondition> fromJSON(JSONArray source) {
		// Validate
		if (source == null) {
			// Invalid source
			return null;
		}

		// Cycle through the objects
		SparseArray<SKUCondition> res = new SparseArray<>();
		for (int index = 0; index < source.length(); ++index) {
			try {
				// Attempt to parse the current condition
				SKUCondition condition = new SKUCondition(source.getJSONObject(index));
				res.put(condition.getConditionId(), condition);
			} catch (JSONException exc) {
				// Invalid condition
				Log.w(LOG_TAG, String.format("Invalid SKU Condition at index %d", index), exc);
			}
		}

		// Are there any conditions?
		return (res.size() == 0) ? null : res;
	}

	/**
	 * Parses an array of SKU conditions from a JSON string.  Returns null if the source is empty,
	 * null or invalid.  If some (but not all) of the individual conditions are invalid, they
	 * will be logged and skipped.
	 * @param source JSON source string
	 * @return Parsed SKU conditions or null
	 */
	public static SparseArray<SKUCondition> fromJSON(String source) {
		// Validate
		if (source == null) {
			return null;
		}
		try {
			// Parse
			return fromJSON(new JSONArray(source));
		} catch (JSONException exc) {
			// Trap
			Log.w(LOG_TAG, "Invalid SKU Conditions source", exc);
			return null;
		}
	}

	/**
	 * Serializes the source container to a JSON encoded string.  Returns null if the source
	 * parameter is null.
	 * @param source Source conditions
	 * @return Serialized representation or null
	 */
	public static String toJSONString(SparseArray<SKUCondition> source) {
		// Validate input
		if (source == null) {
			// Invalid input
			return null;
		}
		try {
			// Convert to JSON
			JSONArray res = new JSONArray();
			for (int index = 0; index < source.size(); ++index) {
				res.put(source.valueAt(index).toJSON());
			}
			return res.toString();
		} catch (JSONException exc) {
			// Unexpected encoding fail
			Log.w(LOG_TAG, "Unexpected encoding fail converting to JSON", exc);
			return null;
		}
	}

	/**
	 * Instantiates from a JSON object source.
	 * @param source Source of SKU condition attributes
	 * @throws JSONException Invalid source
	 */
	private SKUCondition(JSONObject source) throws JSONException {
		conditionId = source.getInt(ATTRIB_ID);
		name = source.getString(ATTRIB_NAME);
		description = source.getString(ATTRIB_DESCR);
	}

	/**
	 * Serializes this to a JSON object
	 * @return Serialized JSON object
	 * @throws JSONException Serialization error
	 */
	private JSONObject toJSON() throws JSONException {
		JSONObject res = new JSONObject();
		res.put(ATTRIB_ID, conditionId);
		res.put(ATTRIB_NAME, name);
		res.put(ATTRIB_DESCR, description);
		return res;
	}

	/**
	 * Gets the unique identifier for this SKU condition.
	 * @return Unique identifier for SKU condition
	 */
	public int getConditionId() {
		return conditionId;
	}

	/**
	 * Gets the display name of this SKU condition.
	 * @return Display name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the detailed description of this SKU condition.
	 * @return Detailed description
	 */
	public String getDescription() {
		return description;
	}
}
