/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.entities;

import com.auditpro.mobile_client.database.ScanRecord;

import java.util.Date;
import java.util.Objects;
import java.util.UUID;


/**
 * Records a scan within an audit session.
 * @author Eric Ruck
 */
public class Scan {

	/**
	 * Initializes a new instance with DAL source.
	 * @param source Values for instance
	 */
	public Scan(ScanRecord source) {
		setId(source.getId());
		setAuditId(source.getAuditId());
		setCreatedAt(source.getCreatedAt());
		setUpdatedAt(source.getUpdatedAt());
		setProductId(source.getProductId());
		setRetailPrice(source.getRetailPrice());
		setSalePrice(source.getSalePrice());
		setScanData(source.getScanData());
		setScanTypeId(source.getScanTypeId());
		setProductName(source.getProductName());
		setBrandName(source.getBrandName());
	}

	/**
	 * Provides copy constructor.
	 * @param source Source scan to copy
	 */
	private Scan(Scan source) {
		setId(source.getId());
		setAuditId(source.getAuditId());
		setCreatedAt(source.getCreatedAt());
		setUpdatedAt(source.getUpdatedAt());
		setProductId(source.getProductId());
		setRetailPrice(source.getRetailPrice());
		setSalePrice(source.getSalePrice());
		setScanData(source.getScanData());
		setScanTypeId(source.getScanTypeId());
		setProductName(source.getProductName());
		setBrandName(source.getBrandName());
	}

	/**
	 * Initializes for a newly selected product.
	 * @param audit The audit of which this scan is a part
	 * @param product Details of the scanned product
	 * @param scanData Raw scanner data or null for manual
	 * @param scanRetail Scanned retail value
	 * @param scanSale Scanned sale value
	 */
	public Scan(Audit audit, Product product, String scanData,
				Double scanRetail, Double scanSale) {
		setId(UUID.randomUUID());
		setAuditId(audit.getId());
		setCreatedAt(new Date());
		setUpdatedAt(getCreatedAt());
		setProductId(product.getId());
		setProductName(product.getProductName());
		setBrandName(product.getBrandName());
		setRetailPrice(scanRetail);
		setSalePrice(scanSale);
		setScanData(scanData);
		setScanTypeId((scanData == null) ? ScanType.MANUAL.getId() : ScanType.SCANNED.getId());
	}

	/**
	 * Creates a new rescan instance of this scan.
	 * @param scanData Raw scanner data or null for manual
	 * @param scanRetail Updated retail price
	 * @param scanSale Updated sale price
	 * @return Updated scan record or null if no update
	 */
	@SuppressWarnings("ConstantConditions")
	public Scan createRescan(String scanData, Double scanRetail, Double scanSale) {
		// Check for differences
		if (Objects.equals(scanData, getScanData()) && Objects.equals(scanRetail, getRetailPrice()) && Objects.equals(scanSale, getSalePrice())) {
			// No update
			return null;
		}

		// Create rescan record
		Scan rescan = new Scan(this);
		rescan.setUpdatedAt(new Date());
		rescan.setScanData(scanData);
		rescan.setScanTypeId((scanData == null) ? ScanType.MANUAL.getId() : ScanType.SCANNED.getId());
		rescan.setRetailPrice(scanRetail);
		rescan.setSalePrice(scanSale);
		return rescan;
	}

	/**
	 * Gets the unique idenifier for this scan.
	 * @return Scan unique identifier
	 */
	public UUID getId() {
		return id;
	}

	/**
	 * Sets the unique identifier for this scan.
	 * @param value New scan unique identifier
	 */
	private void setId(UUID value) {
		id = value;
	}

	/**
	 * Gets the identifier of the audit with which this scan is associated.
	 * @return Audit identifier
	 */
	public UUID getAuditId() {
		return auditId;
	}

	/**
	 * Sets the identifier of the audit with which this scan is associated.
	 * @param value New audit identifier
	 */
	private void setAuditId(UUID value) {
		auditId = value;
	}

	/**
	 * Gets the time stamp at which this scan was created.
	 * @return Scan time stamp
	 */
	public Date getCreatedAt() {
		return createdAt;
	}

	/**
	 * Sets the time stamp at which this scan was created.
	 * @param value New time stamp
	 */
	private void setCreatedAt(Date value) {
		createdAt = value;
	}

	/**
	 * Gets the time stamp at which this scan was last updated.
	 * @return Time stamp
	 */
	public Date getUpdatedAt() {
		return updatedAt;
	}

	/**
	 * Sets the time stamp at which this scan was last updated.
	 * @param value New time stamp
	 */
	private void setUpdatedAt(Date value) {
		updatedAt = value;
	}

	/**
	 * Gets the id of the product with which this scan is associated.
	 * @return Product id
	 */
	public int getProductId() {
		return productId;
	}

	/**
	 * Sets the id of the product which which this scan is associated.
	 * @param value New product id
	 */
	private void setProductId(int value) {
		productId = value;
	}

	/**
	 * Gets the retail price entered for the scanned product.  May be null if no price entered.
	 * @return Retail price or null for none
	 */
	public Double getRetailPrice() {
		return retailPrice;
	}

	/**
	 * Sets the retail price for the scanned product, or null for none.
	 * @param value New retail price or null
	 */
	private void setRetailPrice(Double value) {
		retailPrice = value;
	}

	/**
	 * Gets the sale price entered for the scanned product.  May be null if no price entered.
	 * @return Sale price or null for none
	 */
	public Double getSalePrice() {
		return salePrice;
	}

	/**
	 * Sets the sale price for the scanned product, or null for none.
	 * @param value New sale price or null
	 */
	private void setSalePrice(Double value) {
		salePrice = value;
	}

	/**
	 * Gets the raw data from the scanner for this product, or null if manual.
	 * @return Raw scan data or null
	 */
	public String getScanData() {
		return scanData;
	}

	/**
	 * Sets the raw data from the scanner for this product, or null for manual.
	 * @param value New scan data or null
	 */
	private void setScanData(String value) {
		scanData = value;
	}

	/**
	 * Gets the identifier for the type of scan recorded.
	 * @see ScanType
	 * @return Scan type identifier
	 */
	public int getScanTypeId() {
		return scanTypeId;
	}

	/**
	 * Sets the identifier for the type of scan recorded.
	 * @param value Scan type identifier from {@link ScanType}
	 */
	private void setScanTypeId(int value) {
		scanTypeId = value;
	}

	/**
	 * Gets the display name of the scanned product.
	 * @return Product display name
	 */
	public String getProductName() {
		return productName;
	}

	/**
	 * Sets the display name of the scanned product.
	 * @param value New display name
	 */
	private void setProductName(String value) {
		productName = value;
	}

	/**
	 * Gets the brand name of the scanned product.
	 * @return Product brand name
	 */
	public String getBrandName() {
		return brandName;
	}

	/**
	 * Sets the brand name of the scanned product.
	 * @param value New product brand name
	 */
	private void setBrandName(String value) {
		brandName = value;
	}

	private UUID id;
	private UUID auditId;
	private Date createdAt;
	private Date updatedAt;
	private int productId;
	private Double retailPrice;
	private Double salePrice;
	private String scanData;
	private int scanTypeId;
	private String productName;
	private String brandName;
}
