/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.api;

import android.util.SparseArray;

import com.auditpro.mobile_client.entities.SKUCondition;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * Holds response from user query.
 * @author Eric Ruck
 */
@SuppressWarnings("unused")
public class UserResponse {
	private static final String ATTRIB_USERID = "user_id";
	private static final String ATTRIB_FIRSTNAME = "user_first_name";
	private static final String ATTRIB_LASTNAME = "user_last_name";
	private static final String ATTRIB_EMAIL = "user_email";
	private static final String ATTRIB_ROLEID = "role_id";
	private static final String ATTRIB_ROLENAME = "role_name";
	private static final String ATTRIB_ROLERANK = "role_rank";
	private static final String ATTRIB_CLIENTID = "client_id";
	private static final String ATTRIB_CLIENTNAME = "client_name";
	private static final String ATTRIB_SETTINGS = "client_settings";
	private static final String ATTRIB_SETNAME = "setting_name";
	private static final String ATTRIB_SETVALUE = "setting_value";
	private static final String ATTRIB_SKUCOND = "sku_conditions";

	/**
	 * Initializes from JSON source.
	 * @param source JSON with initial user data
	 */
	UserResponse(JSONObject source) {
		clientSettings = new HashMap<>();
		if (source != null) {
			userId = source.optInt(ATTRIB_USERID);
			firstName = ApiClient.jsonString(source,ATTRIB_FIRSTNAME);
			lastName = ApiClient.jsonString(source,ATTRIB_LASTNAME);
			email = ApiClient.jsonString(source,ATTRIB_EMAIL);
			roleId = source.optInt(ATTRIB_ROLEID);
			roleName = ApiClient.jsonString(source,ATTRIB_ROLENAME);
			roleRank = ApiClient.jsonString(source,ATTRIB_ROLERANK);
			clientId = source.optInt(ATTRIB_CLIENTID);
			clientName = ApiClient.jsonString(source,ATTRIB_CLIENTNAME);

			// Get the settings
			JSONArray settings = source.optJSONArray(ATTRIB_SETTINGS);
			if (settings != null) {
				for (int i = 0; i < settings.length(); ++i) {
					JSONObject setting = settings.optJSONObject(i);
					if (setting != null) {
						String name = setting.optString(ATTRIB_SETNAME);
						String value = setting.optString(ATTRIB_SETVALUE);
						if (name != null) {
							clientSettings.put(name, value);
						}
					}
				}
			}

			// Get the SKU conditions
			skuConditions = SKUCondition.fromJSON(source.optJSONArray(ATTRIB_SKUCOND));
		}
	}

	/**
	 * Determines if the user response is valid.
	 * @return Valid flag
	 */
	boolean isValid() {
		// Validate  the basic properties
		if ((userId <= 0) || (firstName == null) || (lastName == null) || (email == null) ||
				(clientId <= 0) || (clientName == null)) {
			// Easy case invalid
			return false;
		}

		// Complete test for empty strings
		final String rxSpace = "^\\s*$";
		return !firstName.matches(rxSpace) && !lastName.matches(rxSpace) && !email.matches(rxSpace) &&
				!clientName.matches(rxSpace);
	}

	/**
	 * Gets the unique identifier of this user.
	 * @return User id
	 */
	public int getUserId() {
		return userId;
	}

	/**
	 * Gets the first name of this user.
	 * @return User first name
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * Gets the last name of this user.
	 * @return User last name
	 */
	public String getLastName() {
		return lastName;
	}

	/**
	 * Gets the e-mail address for this user, also used as the login name.
	 * @return User e-mail
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Gets the unique idenetifier of this user's role.
	 * @return Role id
	 */
	public int getRoleId() {
		return roleId;
	}

	/**
	 * Gets the role name of this user.
	 * @return Role name
	 */
	public String getRoleName() {
		return roleName;
	}

	/**
	 * Gets the role ranking of this user.
	 * @return Role rank
	 */
	public String getRoleRank() {
		return roleRank;
	}

	/**
	 * Gets the unique identifier of this client.
	 * @return Client id
	 */
	public int getClientId() {
		return clientId;
	}

	/**
	 * Gets the name of this client.
	 * @return Client name
	 */
	public String getClientName() {
		return clientName;
	}

	/**
	 * Gets a set of our attribute value settings pairs.
	 * @return Client account settings
	 */
	public Set<Map.Entry<String, String>> getClientSettings() { return clientSettings.entrySet(); }

	/**
	 * Gets the SKU conditions for this user's account, or null if none.
	 * @return Account SKU conditions or null
	 */
	public SparseArray<SKUCondition> getSkuConditions() {
		return skuConditions;
	}

	private int userId;
	private String firstName;
	private String lastName;
	private String email;
	private int roleId;
	private String roleName;
	private String roleRank;
	private int clientId;
	private String clientName;
	private HashMap<String, String> clientSettings;
	private SparseArray<SKUCondition> skuConditions;
}
