/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.entities;

import com.auditpro.mobile_client.MobileClientException;
import com.auditpro.mobile_client.database.BaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Provides information about a prior audit event.
 * @author Eric Ruck
 */
@SuppressWarnings("ALL")
public class AuditHistory {

	// JSON attribute names
	private static final String ATTRIB_AUDIT_ID = "audit_id";
	private static final String ATTRIB_AUDIT_COUNTER = "audit_counter";
	private static final String ATTRIB_USER_EMAIL = "user_email";
	private static final String ATTRIB_AUDIT_NOTE = "audit_note";
	private static final String ATTRIB_STORE_NOTE = "audit_store_note";
	private static final String ATTRIB_PCT_IN_STOCK = "percent_in_stock";
	private static final String ATTRIB_PCT_VOID = "percent_void";
	private static final String ATTRIB_DURATION = "audit_duration_total";
	private static final String ATTRIB_DAYS_SINCE = "days_since_audit";
	private static final String ATTRIB_LAST_DATE = "last_audit_date";

	/**
	 * Constructs a default instance.
	 */
	private AuditHistory() { }

	/**
	 * Deserializes an instance from a JSON object.
	 * @param source Source JSON obnect
	 * @return Deserialized instance or null if source is invalid
	 */
	static AuditHistory fromJSON(JSONObject source) {
		// Validate source
		if (source == null) {
			// No source
			return null;
		}
		try {
			// Initialize the fields of a new object from the source attributes
			AuditHistory res = new AuditHistory();
			res.auditId = source.optString(ATTRIB_AUDIT_ID, "(none)");
			res.auditCounter = source.getInt(ATTRIB_AUDIT_COUNTER);
			res.userEmail = source.optString(ATTRIB_USER_EMAIL, "");
			res.auditNote = source.isNull(ATTRIB_AUDIT_NOTE) ? "" : source.optString(ATTRIB_AUDIT_NOTE, "");
			res.auditStoreNote = source.isNull(ATTRIB_STORE_NOTE) ? "" : source.optString(ATTRIB_STORE_NOTE, "");
			res.percentInStock = source.optInt(ATTRIB_PCT_IN_STOCK, 0);
			res.percentVoid = source.optInt(ATTRIB_PCT_VOID, 0);
			res.auditDurationTotal = source.optString(ATTRIB_DURATION, "");
			res.daysSinceAudit = source.optInt(ATTRIB_DAYS_SINCE, 0);
			res.lastAuditDate = BaseDatabase.parseDateTime(source.optString(ATTRIB_LAST_DATE));
			return res;
		} catch (JSONException exc) {
			// Unexpected parsing exception
			exc.printStackTrace();
			return null;
		}
	}

	/**
	 * Parses an array of audit history entries from JSON.
	 * @param source JSON array of audit history entries
	 * @return Parsed history entries or null if source is invalid
	 */
	static List<AuditHistory> fromJSON(JSONArray source) {
		// Is there a source?
		if (source == null) {
			// No source
			return null;
		}
		try {
			// Cycle through the objects in the source array
			List<AuditHistory> res = new ArrayList<>();
			for (int index = 0; index < source.length(); ++index) {
				AuditHistory entry = AuditHistory.fromJSON(source.getJSONObject(index));
				if(entry == null) {
					// Invalid entry
					return null;
				}
				res.add(entry);
			}
			return res;
		} catch (JSONException e) {
			// Invalid source
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Parses an array of audit history entries from a JSON encoded string.
	 * @param source JSON encoded string array of audit history entries
	 * @return Parsed history entries or null if source is invalid
	 */
	static List<AuditHistory> fromString(String source) {
		// Validate source
		if (source == null) {
			// No source
			return null;
		}
		try {
			return fromJSON(new JSONArray(source));
		} catch (JSONException e) {
			// Invalid source
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Renders this instance to a JSON encoded string.
	 * @return JSON encoded string
	 */
	@Override
	public String toString() {
		try {
			return toJSON().toString();
		} catch (MobileClientException e) {
			return auditId;
		}
	}

	/**
	 * Serializes this history entry to a JSON object.
	 * @return Serialized history entry
	 * @throws MobileClientException Unexpected coding error
	 */
	public JSONObject toJSON() throws MobileClientException {
		try {
			JSONObject res = new JSONObject();
			res.put(ATTRIB_AUDIT_ID, auditId);
			res.put(ATTRIB_AUDIT_COUNTER, auditCounter);
			res.put(ATTRIB_USER_EMAIL, userEmail);
			res.put(ATTRIB_AUDIT_NOTE, auditNote);
			res.put(ATTRIB_STORE_NOTE,auditStoreNote);
			res.put(ATTRIB_PCT_IN_STOCK, percentInStock);
			res.put(ATTRIB_PCT_VOID, percentVoid);
			res.put(ATTRIB_DURATION, auditDurationTotal);
			res.put(ATTRIB_DAYS_SINCE, daysSinceAudit);
			res.put(ATTRIB_LAST_DATE, BaseDatabase.parseDateTime(lastAuditDate));
			return res;
		} catch (JSONException e) {
			throw new MobileClientException("Failed to serialize audit history", e);
		}
	}

	/**
	 * Serializes a list of audit history entries to a JSON array.
	 * @param entries Entries to serialized
	 * @return Serialized array or null if entries are invalid
	 * @throws MobileClientException Unexpected encoding error
	 */
	static public JSONArray toJSON(Iterable<AuditHistory> entries) throws MobileClientException {
		JSONArray res = new JSONArray();
		if (entries != null) {
			for (AuditHistory entry : entries) {
				res.put(entry.toJSON());
			}
		}
		return res;
	}

	/**
	 * Serializes a list of audit history entries to a JSON string.
	 * @param entries Entries to serialized
	 * @return Serialized string or null if entries are invalid
	 * @throws MobileClientException Unexpected encoding error
	 */
	static public String toString(Iterable<AuditHistory> entries) throws MobileClientException {
		return toJSON(entries).toString();
	}

	/**
	 * Gets the unique identified for this audit.
	 * @return Audit identifier
	 */
	public String getAuditId() {
		return auditId;
	}

	/**
	 * Sets the unqiue identified for this audit.
	 * @param value Audit identifier
	 */
	protected void setAuditId(String value) {
		auditId = value;
	}

	public int getAuditCounter() {
		return auditCounter;
	}

	protected void setAuditCounter(int value) {
		auditCounter = value;
	}

	public String getUserEmail() {
		return userEmail;
	}

	protected void setUserEmail(String value) {
		userEmail = value;
	}

	public String getAuditNote() {
		return auditNote;
	}

	protected void setAuditNote(String value) {
		auditNote = value;
	}

	public String getAuditStoreNote() {
		return auditStoreNote;
	}

	protected void setAuditStoreNote(String value) {
		auditStoreNote = value;
	}

	public int getPercentInStock() {
		return percentInStock;
	}

	protected void setPercentInStock(int value) {
		percentInStock = value;
	}

	public int getPercentVoid() {
		return percentVoid;
	}

	protected void setPercentVoid(int value) {
		percentVoid = value;
	}

	public String getAuditDurationTotal() {
		return auditDurationTotal;
	}

	protected void setAuditDurationTotal(String value) {
		auditDurationTotal = value;
	}

	public int getDaysSinceAudit() {
		return daysSinceAudit;
	}

	protected void setDaysSinceAudit(int value) {
		daysSinceAudit = value;
	}

	public Date getLastAuditDate() {
		return lastAuditDate;
	}

	protected void setLastAuditDate(Date value) {
		lastAuditDate = value;
	}

	private String auditId;
	private int auditCounter;
	private String userEmail;
	private String auditNote;
	private String auditStoreNote;
	private int percentInStock;
	private int percentVoid;
	private String auditDurationTotal;
	private int daysSinceAudit;
	private Date lastAuditDate;
}
