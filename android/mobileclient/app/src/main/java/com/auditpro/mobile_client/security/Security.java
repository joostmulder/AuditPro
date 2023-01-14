/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.security;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.Pair;
import android.util.SparseArray;

import com.auditpro.mobile_client.api.UserResponse;
import com.auditpro.mobile_client.entities.SKUCondition;

import java.security.MessageDigest;
import java.util.Map;
import java.util.Set;


/**
 * Manages user state and offline access to the application.
 * @author Eric Ruck
 */
@SuppressWarnings("SimplifiableIfStatement")
public class Security {

	/**
	 * Initializes a security instance.
	 * @param context Application context
	 */
	public Security(Context context) {
		this.context = context;
	}

	/**
	 * Gets the user identifier. The returned value is cached if there is no current session.  If
	 * there is no cached user, returns -1.
	 * @return The current user identifier
	 */
	public int getUserId() {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		return prefs.getInt(USERID_KEY, -1);
	}

	/**
	 * Gets the user name formatted as "last, first".  The return value os cached if there is no
	 * current session.  If there is no cached user, returns an empty string.
	 * @return User name
	 */
	public String getUserName() {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		String lastName = prefs.getString(LASTNAME_KEY, "");
		String firstName = prefs.getString(FIRSTNAME_KEY, "");
		if (lastName.equals("")) {
			return firstName;
		}
		if (firstName.equals("")) {
			return lastName;
		}
		return String.format("%s, %s", lastName, firstName);
	}

	/**
	 * Gets the unique client identifier. The returned value is cached if there is no current
	 * session.  If there is no cached user, returns -1.
	 * @return The current client identifier
	 */
	public int getClientId() {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		return prefs.getInt(CLIENTID_KEY, -1);
	}

	/**
	 * Gets the last authenticated e-mail.
	 * @return Email or empty string if none
	 */
	public String getLastEmail() {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		return prefs.getString(EMAIL_KEY, "");
	}

	/**
	 * Gets the shared password if there is one, otherwise returns an empty string.
	 * @return Saved password or empty string if none
	 */
	public String getSavedPassword() {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		return prefs.getString(PASSWORD_KEY, "");
	}

	/**
	 * Gets the client display name. The returned value is cached if there is no current session.
	 * If there is no cached user, returns an empty string.
	 * @return The current client display name
	 */
	public String getClientName() {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		return prefs.getString(CLIENTNAME_KEY, "");
	}

	/**
	 * Gets the client account SKU conditions, if any, otherwise null.
	 * @return SKU conditions or null
	 */
	public SparseArray<SKUCondition> getSKUConditions() {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		return SKUCondition.fromJSON(prefs.getString(SKUCONDITIONS_KEY, null));
	}

	// Known setting names, from web service
	public static final String SETTING_IN_STOCK_REQUIRES_SCAN = "in_stock_requires_scan"; // Bool
	public static final String SETTING_IN_STOCK_PRICE_MAX = "in_stock_price_max"; // Double
	public static final String SETTING_IN_STOCK_PRICE_MIN = "in_stock_price_min"; // Double
	public static final String SETTING_SCAN_FORCES_IN_STOCK = "scan_forces_in_stock"; // Bool
	public static final String SETTING_NO_NOTES_WARNING = "no_notes_warning";
	public static final String SETTING_AUDIT_STORE_NOTES = "allow_store_notes"; // Bool default false
	public static final String SETTING_ALLOW_SMART_SCAN = "allow_smart_scan"; // Bool
	public static final String SETTING_PRINT_VOIDS = "print_voids"; // Bool default false
	public static final String SETTING_PRINT_CONDITIONS = "print_conditions"; // Bool default false
	public static final String SETTING_PRINT_STORE_NOTES = "print_store_notes"; // Bool default false
	@SuppressWarnings("unused") public static final String SETTING_ALLOW_CHAIN_SKU = "allow_chain_sku"; // Bool
	@SuppressWarnings("unused") public static final String SETTING_AUDIT_DISTANCE_MAX_MILES = "audit_distance_max_miles"; // Double

	// Known setting names, local
	public static final String SETTING_AUTOSYNC_WIFI = "autosync_wifi"; // Bool
	public static final String SETTING_AUTO_DECIMAL = "auto_decimal"; // Bool

	/**
	 * Gets a boolean setting.
	 * @param name Setting name
	 * @param defaultValue Default value to use if setting not found
	 * @return Setting value or default
	 */
	public boolean optSettingBool(String name, @SuppressWarnings("SameParameterValue") boolean defaultValue) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		String value = prefs.getString(CLIENT_SETTING_KEY_PREFIX + name, null);
		if (value == null) {
			return defaultValue;
		}
		if (value.compareToIgnoreCase("true") == 0) {
			return true;
		}
		if (value.compareToIgnoreCase("false") == 0) {
			return false;
		}
		return defaultValue;
	}

	/**
	 * Gets a double setting.
	 * @param name Setting name
	 * @return Double setting or null if not set
	 */
	public Double optSettingDouble(String name) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		String value = prefs.getString(CLIENT_SETTING_KEY_PREFIX + name, null);
		if (value == null) {
			return null;
		}
		try {
			return Double.valueOf(value);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * Saves a boolean setting.
	 * @param name Setting name
	 * @param value New value for setting
	 */
	public void setSetting(String name, boolean value) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(CLIENT_SETTING_KEY_PREFIX + name, value ? "true" : "false");
		editor.apply();
	}

	/**
	 * Keeps the last audit position.  Pass null for either or both parameters to clear this position.
	 * @param latitude Optional latitude
	 * @param longitude Optional longitude
	 */
	public void setLastAuditPos(Double latitude, Double longitude) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		if ((latitude == null) || (longitude == null)) {
			editor.remove(LASTAUDITLAT_KEY);
			editor.remove(LASTAUDITLON_KEY);
		} else {
			editor.putString(LASTAUDITLAT_KEY, latitude.toString());
			editor.putString(LASTAUDITLON_KEY, longitude.toString());
		}
		editor.apply();
	}

	/// <summary>
	/// Gets the last audit position.
	/// </summary>
	/// <returns>The last audit position as a dynamic with latitude and
	/// longitude double properties, or null if none.</returns>
	public Pair<Double, Double> getLastAuditPos() {
		// Get the raw values
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		String rawLat = prefs.getString(LASTAUDITLAT_KEY, null);
		String rawLon = prefs.getString(LASTAUDITLON_KEY, null);
		if ((rawLat == null) || (rawLon == null)) {
			// No last value
			return null;
		}
		try {
			// Parse the raw values
			Double latitude = Double.valueOf(rawLat);
			Double longitude = Double.valueOf(rawLon);
			return new Pair<>(latitude, longitude);
		} catch (NumberFormatException exc) {
			// Failed to parse
			return null;
		}
	}

	/**
	 * Verifies that the passed password matches the last authentication, for offline access to the
	 * application.
	 * @param password Password to verify
	 * @param isPasswordSaved Should the password be saved in the store?
	 * @return Password verified offline flag
	 */
	public boolean verifyLastPassword(String password, boolean isPasswordSaved) {
		// Verify the password
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		boolean isVerified = hashPassword(password).equals(prefs.getString(HASH_KEY, ""));
		if (isVerified && isPasswordSaved) {
			// Update saved password state
			SharedPreferences.Editor editor = prefs.edit();
			//noinspection ConstantConditions
			editor.putString(PASSWORD_KEY, isPasswordSaved ? password : "");
			editor.apply();
		}

		// Indicate verified state
		return isVerified;
	}

	/**
	 * Preserves our successful login.
	 * @param email Validate user e-mail
	 * @param password Password entered to authenticate user
	 * @param isPasswordSaved Should the password be saved in the store?
	 * @param userResponse Response from user query to API
	 */
	public void setLogin(String email, String password, boolean isPasswordSaved, UserResponse userResponse) {
		// Prepare to update stored preferences
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();

		// Keep the input preferences
		editor.putString(EMAIL_KEY, email);
		editor.putString(PASSWORD_KEY, isPasswordSaved ? password : "");
		editor.putString(HASH_KEY, hashPassword(password));

		// Keep the queried user information
		editor.putInt(USERID_KEY, userResponse.getUserId());
		editor.putString(FIRSTNAME_KEY, userResponse.getFirstName());
		editor.putString(LASTNAME_KEY, userResponse.getLastName());
		editor.putInt(CLIENTID_KEY, userResponse.getClientId());
		editor.putString(CLIENTNAME_KEY, userResponse.getClientName());
		editor.putString(SKUCONDITIONS_KEY, SKUCondition.toJSONString(userResponse.getSkuConditions()));

		// Transfer the settings
		Set<Map.Entry<String, String>> settings = userResponse.getClientSettings();
		if (settings != null) {
			for (Map.Entry<String, String> setting : settings) {
				editor.putString(CLIENT_SETTING_KEY_PREFIX + setting.getKey(), setting.getValue());
			}
		}

		// Save the settings
		editor.apply();
	}

	/**
	 * Saves the store database version following sync.
	 * @param storeVersion Current store database version code at sync
	 */
	public void setStoreVersionOnSync(int storeVersion) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(SYNCSTORESVER_KEY, storeVersion);
		editor.apply();
	}

	/**
	 * Determines if we need a full sync.
	 * @param storeVersion Current store database version.
	 * @return Needs sync flag
	 */
	public boolean isSyncNeeded(int storeVersion) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		return prefs.getInt(SYNCSTORESVER_KEY, 0) < storeVersion;
	}

	/**
	 * Applies a secure password hash.
	 * @param password Password to hash
	 * @return Hashed password
	 */
	private String hashPassword(String password) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			byte[] textBytes = password.getBytes("utf-8");
			md.update(textBytes, 0, textBytes.length);
			byte[] sha1hash = md.digest();
			return convertToHex(sha1hash);
		} catch (Exception excAny) {
			// Yeah this is sketchy I should watch the logs in testing
			Log.e(LOG_TAG, "Failed to hash password", excAny);
			return password;
		}
	}

	/**
	 * Converts raw bytes to hex string.
	 * @param data Raw bytes input
	 * @return Converted text string output
	 */
	private static String convertToHex(byte[] data) {
		StringBuilder buf = new StringBuilder();
		for (byte b : data) {
			int halfbyte = (b >>> 4) & 0x0F;
			int two_halfs = 0;
			do {
				buf.append((0 <= halfbyte) && (halfbyte <= 9) ? (char) ('0' + halfbyte) : (char) ('a' + (halfbyte - 10)));
				halfbyte = b & 0x0F;
			} while (two_halfs++ < 1);
		}
		return buf.toString();
	}

	private Context context;

	private static final String PREFS_NAME = "com.auditpro.mobile_client.prefs";
	private static final String LOG_TAG = "Security";

	private static final String EMAIL_KEY = "email";
	private static final String PASSWORD_KEY = "password";
	private static final String HASH_KEY = "hash";
	private static final String USERID_KEY = "userid";
	private static final String FIRSTNAME_KEY = "firstname";
	private static final String LASTNAME_KEY = "lastname";
	private static final String CLIENTID_KEY = "clientid";
	private static final String CLIENTNAME_KEY = "clientname";
	private static final String SKUCONDITIONS_KEY = "skuConditions";
	private static final String LASTAUDITLAT_KEY = "lastauditlat";
	private static final String LASTAUDITLON_KEY = "lastauditlon";
	private static final String SYNCSTORESVER_KEY = "syncstoresver";
	private static final String CLIENT_SETTING_KEY_PREFIX = "clientsetting_";
}
