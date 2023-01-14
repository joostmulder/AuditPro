/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.entities;


/**
 * Represents valid reorder status states.
 * @author Eric Ruck
 */
public class ReorderStatus {
	public static final ReorderStatus NONE = new ReorderStatus(0, "None", "None", false);
	public static final ReorderStatus IN_STOCK = new ReorderStatus(1, "In Stock", "I", true);
	public static final ReorderStatus OUT_OF_STOCK = new ReorderStatus(2, "Out of Stock", "OOS", true);
	public static final ReorderStatus VOID = new ReorderStatus(3, "Void", "V", true);
	public static final ReorderStatus[] Statuses = { NONE, IN_STOCK, OUT_OF_STOCK, VOID };

	public static ReorderStatus fromId(int id) {
		for (ReorderStatus status : Statuses) {
			if (status.getId() == id) {
				return status;
			}
		}
		return null;
	}

	public static ReorderStatus fromName(String name) {
		for (ReorderStatus status : Statuses) {
			if (status.getName().equals(name)) {
				return status;
			}
		}
		return null;
	}

	public static ReorderStatus fromCode(String code) {
		for (ReorderStatus status : Statuses) {
			if (status.getCode().equals(code)) {
				return status;
			}
		}
		return null;
	}

	private ReorderStatus(int id, String name, String code, boolean valid) {
		this.id = id;
		this.name = name;
		this.code = code;
		this.valid = valid;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getCode() {
		return code;
	}

	public boolean isValid() {
		return valid;
	}

	@Override
	public String toString() {
		return name;
	}

	private int id;
	private String name;
	private String code;
	private boolean valid;
}
