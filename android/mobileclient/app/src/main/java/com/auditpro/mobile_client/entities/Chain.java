/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.entities;


/**
 * Manages one chain for the application.
 * @author Eric Ruck
 */
@SuppressWarnings("unused")
public class Chain {
	public Chain(int id, String name, String code) {
		this.chainId = id;
		this.chainName = name;
		this.chainCode = code;
	}

	public int getChainId() {
		return chainId;
	}

	public String getChainName() {
		return chainName;
	}

	public String getChainCode() {
		return chainCode;
	}

	private int chainId;
	private String chainName;
	private String chainCode;
}
