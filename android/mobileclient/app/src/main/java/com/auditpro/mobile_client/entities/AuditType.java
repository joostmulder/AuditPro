/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.entities;


/**
 * Provides the valid audit types.
 * @author Eric Ruck
 */
public class AuditType {
	public static final AuditType STANDARD = new AuditType(1, "Standard");
	public static final AuditType DEMO = new AuditType(2, "Demo");
	public static final AuditType RESEARCH = new AuditType(3, "Research");
	public static final AuditType[] Types = { STANDARD, DEMO, RESEARCH };

	/**
	 * Initializes a new audit type
	 * @param id Unique audit type identifier
	 * @param name Audit type display name
	 */
	private AuditType(int id, String name) {
		this.id = id;
		this.name = name;
	}

	/**
	 * Gets the unique identifier of this audit type.
	 * @return Audit type unique identifier
	 */
	public int getId() {
		return id;
	}

	/**
	 * Gets the display name of this audit type.
	 * @return Audit type display name
	 */
	public String getName() {
		return name;
	}

	/** Provides the unique identifier of this audit type. */
	private int id;

	/** Provides the display name of this audit type. */
	private String name;
}
