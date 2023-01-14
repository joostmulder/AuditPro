/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.entities;

import java.util.UUID;


/**
 * Manages the notes for an audit.
 * @author Eric Ruck
 */
public class Notes {

	/**
	 * Initializes a new instance with specific field values.
	 * @param id Unique notes identifier
	 * @param auditId Associates notes with an audit
	 * @param contents Internal notes contents
	 * @param store Store notes contents
	 */
	public Notes(UUID id, UUID auditId, String contents, String store) {
		setId(id);
		setAuditId(auditId);
		setContents(contents);
		setStore(store);
	}

	/**
	 * Gets the unique identifier for these notes.
	 * @return Our unique identifier
	 */
	public UUID getId() {
		return id;
	}

	/**
	 * Sets the unique identifier for these notes.
	 * @param value New notes unique identifier
	 */
	private void setId(UUID value) {
		id = value;
	}

	/**
	 * Gets the unique identifier for the audit associated with our notes.
	 * @return Associated audit id
	 */
	public UUID getAuditId() {
		return auditId;
	}

	/**
	 * Sets the audit with which our notes are associated.
	 * @param value New audit ID for our notes
	 */
	private void setAuditId(UUID value) {
		auditId = value;
	}

	/**
	 * Gets the confidential notes for our audit.
	 * @return Current private notes
	 */
	public String getContents() {
		return contents;
	}

	/**
	 * Sets the confidential notes value.
	 * @param value New private notes value
	 */
	private void setContents(String value) {
		contents = value;
	}

	/**
	 * Gets the store notes.
	 * @return The store notes for our audit
	 */
	public String getStore() {
		return store;
	}

	/**
	 * Sets the store notes.
	 * @param value New store notes value
	 */
	private void setStore(String value) {
		store = value;
	}

	/**
	 * Indicates if the store notes are empty (null, empty string or whitespace).
	 * @return Store notes empty flag
	 */
	public boolean isStoreEmpty() {
		return ((store == null) || store.matches("^\\s*$"));
	}

	/** Provides our unique identifier. */
	private UUID id;

	/** Provides the identifier of the audit with which these notes are associated. */
	private UUID auditId;

	/** Provides the confidential auditor notes. */
	private String contents;

	/** Provides the shared store notes. */
	private String store;
}
