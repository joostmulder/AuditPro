/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.api;

import android.util.Log;

import com.auditpro.mobile_client.MobileClientException;
import com.auditpro.mobile_client.entities.AuditHistory;
import com.auditpro.mobile_client.entities.Store;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * Holds a response from the store query.
 * @author Eric Ruck
 */
public class StoreResponse extends Store {

	private static final String ATTRIB_CLIENT_ID = "client_id";
	private static final String ATTRIB_CHAIN_ID = "chain_id";
	private static final String ATTRIB_CHAIN_NAME = "chain_name";
	private static final String ATTRIB_CHAIN_CODE = "chain_code";
	private static final String ATTRIB_ID = "store_id";
	private static final String ATTRIB_NAME = "store_name";
	private static final String ATTRIB_STORE_IDENTIFIER = "store_identifier";
	private static final String ATTRIB_ADDR = "store_street_address_1";
	private static final String ATTRIB_ADDR2 = "store_street_address_2";
	private static final String ATTRIB_CITY = "store_city";
	private static final String ATTRIB_POSTAL = "store_zip";
	private static final String ATTRIB_LATITUDE = "store_lat";
	private static final String ATTRIB_LONGITUDE = "store_lon";
	private static final String ATTRIB_AUDIT_HISTORY = "audit_history";

	/**
	 * Initialize store from a JSON source.
	 * @param source Initial data
	 */
	private StoreResponse(JSONObject source) {
		super();

		// Pick out the fields from the source object
		setClientId(source.optInt(ATTRIB_CLIENT_ID));
		setChainId(source.optInt(ATTRIB_CHAIN_ID));
		setChainName(ApiClient.jsonString(source,ATTRIB_CHAIN_NAME));
		setChainCode(ApiClient.jsonString(source,ATTRIB_CHAIN_CODE));
		setStoreId(source.optInt(ATTRIB_ID));
		setStoreName(ApiClient.jsonString(source,ATTRIB_NAME));
		setStoreIdentifier(ApiClient.jsonString(source,ATTRIB_STORE_IDENTIFIER));
		setStoreAddress(ApiClient.jsonString(source,ATTRIB_ADDR));
		setStoreAddress2(ApiClient.jsonString(source,ATTRIB_ADDR2));
		setStoreCity(ApiClient.jsonString(source,ATTRIB_CITY));
		setStoreZip(ApiClient.jsonString(source,ATTRIB_POSTAL));
		try {
			// Parse out the location
			setStoreLat(source.isNull(ATTRIB_LATITUDE) ? null : source.getDouble(ATTRIB_LATITUDE));
			setStoreLon(source.isNull(ATTRIB_LONGITUDE) ? null : source.getDouble(ATTRIB_LONGITUDE));
		} catch (JSONException excJSON) {
			// Invalid location
			Log.w(LOG_TAG, String.format("Invalid lat/long received for store %d", getStoreId()));
			setStoreLat(null);
			setStoreLon(null);
		}
		JSONArray history = source.optJSONArray(ATTRIB_AUDIT_HISTORY);
		if (history != null) {
			// Parse out the history
			for (int index = 0; index < history.length(); ++index) {
				// Parse the current history
				if (!addAuditHistory(history.optJSONObject(index))) {
					Log.w(LOG_TAG, "Unexpected invalid store history");
				}
			}
		}
	}

	/**
	 * Validates the state of this store.
	 * @return Valid flag
	 */
	private boolean isValid() {
		if ((getClientId() <= 0) || (getChainId() <= 0) || (getStoreId() <= 0) ||
				(getChainName() == null) || (getChainCode() == null) ||
				(getStoreName() == null)) {
			return false;
		}

		// Let's call it valid
		return true;
	}

	/**
	 * Parses the array response from the web service.
	 * @param source Array from the web service
	 * @return Parsed results
	 */
	static List<Store> fromJSON(JSONArray source) {
		// Parse the individual objects out of the store
		List<Store> res = new ArrayList<>();
		StoreResponse store = null;
		for (int index = 0; index < source.length(); ++index) {
			try {
				// Parse the current index
				store = new StoreResponse(source.getJSONObject(index));
				if (!store.isValid()) {
					// Invalid data in store
					String storeName = store.getStoreName();
					Log.w(LOG_TAG, String.format("Invalid store %d %s at %d",
							store.getStoreId(), (storeName == null) ? "(null)" : storeName, index));
				} else {
					// Add the store to the result
					res.add(store);
				}
			} catch (JSONException excJSON) {
				// Failed to parse the current store index
				String storeId = (store == null) ? "(null)" : Integer.toString(store.getStoreId());
				String storeName = (store == null) ? null : store.getStoreName();
				Log.w(LOG_TAG, String.format("Failed to parse store %s %s at %d",
						storeId, (storeName == null) ? "(null)" : storeName, index));
			}
		}

		// Return the stores
		return res;
	}

	public static Store fromJSON(String source) {
		try {
			JSONObject parse = new JSONObject(source);
			StoreResponse res = new StoreResponse(parse);
			return res.isValid() ? res : null;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static JSONObject toJSON(Store store) {
		try {
			JSONObject res = new JSONObject();
			res.put(ATTRIB_CLIENT_ID, store.getClientId());
			res.put(ATTRIB_CHAIN_ID, store.getChainId());
			res.put(ATTRIB_CHAIN_NAME, store.getChainName());
			res.put(ATTRIB_CHAIN_CODE, store.getChainCode());
			res.put(ATTRIB_ID, store.getStoreId());
			res.put(ATTRIB_NAME, store.getStoreName());
			res.put(ATTRIB_STORE_IDENTIFIER, store.getStoreIdentifier());
			res.put(ATTRIB_ADDR, store.getStoreAddress());
			res.put(ATTRIB_ADDR2, store.getStoreAddress2());
			res.put(ATTRIB_CITY, store.getStoreCity());
			res.put(ATTRIB_POSTAL, store.getStoreZip());
			res.put(ATTRIB_LATITUDE, store.getStoreLat());
			res.put(ATTRIB_LONGITUDE, store.getStoreLon());
			res.put(ATTRIB_AUDIT_HISTORY, AuditHistory.toJSON(store.getHistory()));
			return res;
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		} catch (MobileClientException e) {
			return null;
		}
	}

	private static final String LOG_TAG = "StoreResponse";
}
