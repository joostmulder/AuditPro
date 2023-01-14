/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.entities;


/**
 * Provides legal scan types.
 * @author Eric Ruck
 */
public class ScanType {
	public static final ScanType SCANNED = new ScanType(1, "Scanned");
	public static final ScanType MANUAL = new ScanType(2, "Manual");
	public static final ScanType[] Types = { SCANNED, MANUAL };

	private ScanType(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	private int id;
	private String name;
}
