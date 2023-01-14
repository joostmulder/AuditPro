/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.entities;

import com.auditpro.mobile_client.database.ReportRecord;
import com.auditpro.mobile_client.database.ScanRecord;

import java.util.Date;
import java.util.UUID;


/**
 * Manages the report on one product in an audit.
 * @author Eric Ruck
 */
public class Report {

	/**
	 * Initializes a new instance with DAL source.
	 * @param source Values for instance
	 */
	public Report(ReportRecord source) {
		setId(source.getId());
		setCreatedAt(source.getCreatedAt());
		setUpdatedAt(source.getUpdatedAt());
		setAuditId(source.getAuditId());
		setScanId(source.getScanId());
		setProductId(source.getProductId());
		setReorderStatusId(source.getReorderStatusId());
	}

	/**
	 * Initializes an implicit instance from a scan.
	 * @param source Scan source
	 */
	public Report(ScanRecord source) {
		setId(null);
		setCreatedAt(source.getCreatedAt());
		setUpdatedAt(source.getUpdatedAt());
		setAuditId(source.getAuditId());
		setScanId(source.getId());
		setProductId(source.getProductId());
		setReorderStatusId(ReorderStatus.IN_STOCK.getId());
	}

	/**
	 * Initializes an implicit instance from a product.
	 * @param audit Parent audit
	 * @param product Product source
	 * @param reorderStatusId Initial status ID or null for default
	 */
	public Report(Audit audit, Product product, Integer reorderStatusId) {
		setId(null);
		setCreatedAt(new Date());
		setUpdatedAt(new Date());
		setAuditId(audit.getId());
		setScanId(null);
		setProductId(product.getId());
		setReorderStatusId((reorderStatusId == null) ? ReorderStatus.OUT_OF_STOCK.getId() : reorderStatusId);
	}

	/**
	 * Creates a new report that is ready to insert into the database.
	 * @param scan Scan with which this report is associated
	 * @param product Product with which this report is associated
	 * @param reorderStatusId Reported reorder status
	 */
	public Report(Scan scan, Product product, int reorderStatusId) {
		setId(UUID.randomUUID());
		setCreatedAt(new Date());
		setUpdatedAt(new Date());
		setAuditId(scan.getAuditId());
		setScanId(scan.getId());
		setProductId(product.getId());
		setReorderStatusId(reorderStatusId);
	}

	/**
	 * Creates a new report to update an existing instance.
	 * @param source Existing instance to update
	 * @param scan Scan associated with this report
	 * @param reorderStatusId Updated reorder status
	 */
	public Report(Report source, Scan scan, int reorderStatusId) {
		setId(source.getId());
		setCreatedAt(source.getCreatedAt());
		setUpdatedAt(new Date());
		setAuditId(source.getAuditId());
		setScanId((scan == null) ? null : scan.getId());
		setProductId(source.getProductId());
		setReorderStatusId(reorderStatusId);
	}

	public UUID getId() {
		return id;
	}

	private void setId(UUID value) {
		id = value;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	private void setCreatedAt(Date value) {
		createdAt = value;
	}

	public Date getUpdatedAt() {
		return updatedAt;
	}

	private void setUpdatedAt(Date value) {
		updatedAt = value;
	}

	public UUID getAuditId() {
		return auditId;
	}

	private void setAuditId(UUID value) {
		auditId = value;
	}

	public UUID getScanId() {
		return scanId;
	}

	private void setScanId(UUID value) {
		scanId = value;
	}

	public int getProductId() {
		return productId;
	}

	private void setProductId(int value) {
		productId = value;
	}

	public int getReorderStatusId() {
		return reorderStatusId;
	}

	private void setReorderStatusId(int value) {
		reorderStatusId = value;
	}

	private UUID id;
	private Date createdAt;
	private Date updatedAt;
	private UUID auditId;
	private UUID scanId;
	private int productId;
	private int reorderStatusId;
}
