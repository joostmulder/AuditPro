/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.entities;

import com.auditpro.mobile_client.database.StoreRecord;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


/**
 * Provides information about a store that can be audited.
 * @author Eric Ruck
 */
public class Store {

	protected Store() {}

	public Store(StoreRecord source) {
		setClientId(source.getClientId());
		setChainId(source.getChainId());
		setChainName(source.getChainName());
		setChainCode(source.getChainCode());
		setStoreId(source.getStoreId());
		setStoreName(source.getStoreName());
		setStoreIdentifier(source.getStoreIdentifier());
		setStoreAddress(source.getStoreAddress());
		setStoreAddress2(source.getStoreAddress2());
		setStoreCity(source.getStoreCity());
		setStoreZip(source.getStoreZip());
		setStoreLat(source.getStoreLat());
		setStoreLon(source.getStoreLon());
		history = AuditHistory.fromString(source.getHistory());
	}

	public int getClientId() {
		return clientId;
	}

	protected void setClientId(int value) {
		clientId = value;
	}

	public int getChainId() {
		return chainId;
	}

	protected void setChainId(int value) {
		chainId = value;
	}

	public String getChainName() {
		return chainName;
	}

	protected void setChainName(String value) {
		chainName = value;
	}

	public String getChainCode() {
		return chainCode;
	}

	protected void setChainCode(String value) {
		chainCode = value;
	}

	public int getStoreId() {
		return storeId;
	}

	protected void setStoreId(int value) {
		storeId = value;
	}

	public String getStoreName() {
		return storeName;
	}

	protected void setStoreName(String value) {
		storeName = value;
	}

	public String getStoreIdentifier() {
		return storeIdentifier;
	}

	protected void setStoreIdentifier(String value) {
		storeIdentifier = value;
	}

	public String getStoreAddress() {
		return storeAddress;
	}

	protected void setStoreAddress(String value) {
		storeAddress = value;
	}

	public String getStoreAddress2() {
		return storeAddress2;
	}

	protected void setStoreAddress2(String value) {
		storeAddress2 = value;
	}

	public String getStoreCity() {
		return storeCity;
	}

	protected void setStoreCity(String value) {
		storeCity = value;
	}

	public String getStoreZip() {
		return storeZip;
	}

	protected void setStoreZip(String value) {
		storeZip = value;
	}

	public Double getStoreLat() {
		return storeLat;
	}

	protected void setStoreLat(Double value) {
		storeLat = value;
	}

	public Double getStoreLon() {
		return storeLon;
	}

	protected void setStoreLon(Double value) {
		storeLon = value;
	}

	public boolean hasLatLong() {
		return (storeLat != null) && (storeLon != null);
	}

	/**
	 * Gets the store description for display.
	 * @return Formatted description to display
	 */
	public String getDescription() {
		String res = (storeName == null) ? chainName : storeName;
		if (storeIdentifier != null) {
			if (res == null) {
				res = storeIdentifier;
			} else {
				res += String.format(" (%s)", storeIdentifier);
			}
		}
		return (res == null) ? String.format(Locale.getDefault(), "%d", storeId) : res;
	}

	@Override public String toString() {
		return getDescription();
	}

	/**
	 * Formats the city, state and zip as a single line.
	 * @return Formatted address
	 */
	public String getCityStateZip() {
		String res = storeCity;
		if ((storeZip != null) && !storeZip.matches("^\\s*$")) {
			res = (res.matches("^\\s*$") ? "" : res + " ") + storeZip;
		}
		return (res == null) ? "" : res;
	}

	/**
	 * Indicates if this store is geocoded.
	 * @return Geocoded flag
	 */
	public boolean isGeocoded() {
		return (getStoreLat() != null) && (getStoreLon() != null);
	}

	/**
	 * Gets the number of history entries for this store.
	 * @return Audit history entry count
	 */
	public int getHistoryCount() {
		return history.size();
	}

	/**
	 * Gets the story history at the indicated index in the list.
	 * @param index Audit history list index
	 * @return Audit history entry or null if index is invalid
	 */
	public AuditHistory getHistory(int index) {
		return ((index >= 0) && (index < history.size())) ? history.get(index) : null;
	}

	/**
	 * Gets an iterable view of all the history entries.
	 * @return Iterable histories
	 */
	public Iterable<AuditHistory> getHistory() {
		return history;
	}

	/**
	 * Add a history entry from the passed source.
	 * @param source JSON source contains audit history fields
	 * @return Success flag
	 */
	protected boolean addAuditHistory(JSONObject source) {
		// Parse the source
		AuditHistory parsed = AuditHistory.fromJSON(source);
		if (parsed == null) {
			// Failed to parse
			return false;
		}

		// Add the history
		history.add(parsed);
		return true;
	}

	private int clientId;
	private int chainId;
	private String chainName;
	private String chainCode;
	private int storeId;
	private String storeName;
	private String storeIdentifier;
	private String storeAddress;
	private String storeAddress2;
	private String storeCity;
	private String storeZip;
	private Double storeLat;
	private Double storeLon;
	private List<AuditHistory> history = new ArrayList<>();
}
