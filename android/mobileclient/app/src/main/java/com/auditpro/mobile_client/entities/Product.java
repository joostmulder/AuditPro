/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.entities;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

import com.auditpro.mobile_client.database.BaseDatabase;
import com.auditpro.mobile_client.database.ProductRecord;

import java.util.Date;


/**
 * Manages a product that can be associated with an audit for a store
 * @author Eric Ruck
 */
public class Product implements Parcelable {

	protected Product() {}

	public Product(ProductRecord source) {
		this();
		setId(source.getId());
		setClientId(source.getClientId());
		setChainId(source.getChainId());
		setGlobalProductId(source.getGlobalProductId());
		setBrandName(source.getBrandName());
		setBrandNameShort(source.getBrandNameShort());
		setProductName(source.getProductName());
		setUPC(source.getUPC());
		setMSRP(source.getMSRP());
		setRandomWeight(source.isRandomWeight());
		setRetailPriceMin(source.getRetailPriceMin());
		setRetailPriceMax(source.getRetailPriceMax());
		setRetailPriceAverage(source.getRetailPriceAverage());
		setCategoryName(source.getCategoryName());
		setSubcategoryName(source.getSubcategoryName());
		setProductTypeName(source.getProductTypeName());
		setCurrentReorderCode(source.getCurrentReorderCode());
		setPreviousReorderCode(source.getPreviousReorderCode());
		setBrandSKU(source.getBrandSKU());
		setLastScannedAt(source.getLastScannedAt());
		setLastScannedPrice(source.getLastScannedPrice());
		setLastScanWasSale(source.isLastScanWasSale());
		setChainSKU(source.getChainSKU());
		setInStockPriceMin(source.getInStockPriceMin());
		setInStockPriceMax(source.getInStockPriceMax());
	}


	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int i) {
		parcel.writeInt(id);
		parcel.writeInt(clientId);
		parcel.writeInt(chainId);
		parcel.writeInt(globalProductId);
		parcel.writeString(brandName);
		parcel.writeString(brandNameShort);
		parcel.writeString(productName);
		parcel.writeString(upc);
		parcel.writeValue(msrp);
		parcel.writeInt(randomWeight ? 1 : 0);
		parcel.writeValue(retailPriceMin);
		parcel.writeValue(retailPriceMax);
		parcel.writeValue(retailPriceAverage);
		parcel.writeString(categoryName);
		parcel.writeString(subcategoryName);
		parcel.writeString(productTypeName);
		parcel.writeString(currentReorderCode);
		parcel.writeString(previousReorderCode);
		parcel.writeString(brandSku);
		parcel.writeString(BaseDatabase.parseDateTime(lastScannedAt));
		parcel.writeValue(lastScannedPrice);
		parcel.writeInt(lastScanWasSale ? 1 : 0);
		parcel.writeString(chainSku);
		parcel.writeValue(inStockPriceMin);
		parcel.writeValue(inStockPriceMax);
	}

	public static final Parcelable.Creator<Product> CREATOR = new Parcelable.Creator<Product>() {
		@SuppressLint("ParcelClassLoader")
		@Override
		public Product createFromParcel(Parcel parcel) {
			Product product = new Product();
			product.id = parcel.readInt();
			product.clientId = parcel.readInt();
			product.chainId = parcel.readInt();
			product.globalProductId = parcel.readInt();
			product.brandName = parcel.readString();
			product.brandNameShort = parcel.readString();
			product.productName = parcel.readString();
			product.upc = parcel.readString();
			product.msrp = (Double) parcel.readValue(null);
			product.randomWeight = parcel.readInt() != 0;
			product.retailPriceMin = (Double) parcel.readValue(null);
			product.retailPriceMax = (Double) parcel.readValue(null);
			product.retailPriceAverage = (Double) parcel.readValue(null);
			product.categoryName = parcel.readString();
			product.subcategoryName = parcel.readString();
			product.productTypeName = parcel.readString();
			product.currentReorderCode = parcel.readString();
			product.previousReorderCode = parcel.readString();
			product.brandSku = parcel.readString();
			product.lastScannedAt = BaseDatabase.parseDateTime(parcel.readString());
			product.lastScannedPrice = (Double) parcel.readValue(null);
			product.lastScanWasSale = parcel.readInt() != 0;
			product.chainSku = parcel.readString();
			product.inStockPriceMin = (Double) parcel.readValue(null);
			product.inStockPriceMax = (Double) parcel.readValue(null);
			return product;
		}

		@Override
		public Product[] newArray(int i) {
			return new Product[i];
		}
	};

	/**
	 * Determines if the token is in one of our searchable fields, case insensitive.
	 * @param token Token to find
	 * @return Found flag
	 */
	public boolean hasToken(String token) {
		// Trivial check
		if ((token == null) || (token.length() == 0)) {
			// No token
			return false;
		}

		// Case insensitive search
		String caseToken = token.toLowerCase();
		return
			((upc != null) && upc.toLowerCase().contains(caseToken)) ||
			((currentReorderCode != null) && currentReorderCode.toLowerCase().contains(caseToken)) ||
			((productName != null) && productName.toLowerCase().contains(caseToken)) ||
			((brandSku != null) && brandSku.toLowerCase().contains(caseToken)) ||
			((chainSku != null) && chainSku.toLowerCase().contains(caseToken))
		;
	}

	public int getId() {
		return id;
	}

	protected void setId(int value) {
		id = value;
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

	public int getGlobalProductId() {
		return globalProductId;
	}

	protected void setGlobalProductId(int value) {
		globalProductId = value;
	}

	public String getBrandName() {
		return brandName;
	}

	protected void setBrandName(String value) {
		brandName = value;
	}

	public String getBrandNameShort() {
		return brandNameShort;
	}

	protected void setBrandNameShort(String value) {
		brandNameShort = value;
	}

	public String getDisplayBrandName() {
		return (brandName == null) ? brandNameShort : brandName;
	}

	public String getProductName() {
		return productName;
	}

	protected void setProductName(String value) {
		productName = value;
	}

	public String getUPC() {
		return upc;
	}

	protected void setUPC(String value) {
		upc = value;
	}

	public Double getMSRP() {
		return msrp;
	}

	protected void setMSRP(Double value) {
		msrp = value;
	}

	public boolean isRandomWeight() {
		return randomWeight;
	}

	protected void setRandomWeight(boolean value) {
		randomWeight = value;
	}

	public Double getRetailPriceMin() {
		return retailPriceMin;
	}

	protected void setRetailPriceMin(Double value) {
		retailPriceMin = value;
	}

	public Double getRetailPriceMax() {
		return retailPriceMax;
	}

	protected void setRetailPriceMax(Double value) {
		retailPriceMax = value;
	}

	public Double getRetailPriceAverage() {
		return retailPriceAverage;
	}

	protected void setRetailPriceAverage(Double value) {
		retailPriceAverage = value;
	}

	public String getCategoryName() {
		return categoryName;
	}

	protected void setCategoryName(String value) {
		categoryName = value;
	}

	public String getSubcategoryName() {
		return subcategoryName;
	}

	protected void setSubcategoryName(String value) {
		subcategoryName = value;
	}

	public String getProductTypeName() {
		return productTypeName;
	}

	protected void setProductTypeName(String value) {
		productTypeName = value;
	}

	public String getCurrentReorderCode() {
		return currentReorderCode;
	}

	protected void setCurrentReorderCode(String value) {
		currentReorderCode = value;
	}

	public String getPreviousReorderCode() {
		return previousReorderCode;
	}

	protected void setPreviousReorderCode(String value) {
		previousReorderCode = value;
	}

	public String getBrandSKU() {
		return brandSku;
	}

	protected void setBrandSKU(String value) {
		brandSku = value;
	}

	public Date getLastScannedAt() {
		return lastScannedAt;
	}

	protected void setLastScannedAt(Date value) {
		lastScannedAt = value;
	}

	public Double getLastScannedPrice() {
		return lastScannedPrice;
	}

	protected void setLastScannedPrice(Double value) {
		lastScannedPrice = value;
	}

	public boolean isLastScanWasSale() {
		return lastScanWasSale;
	}

	protected void setLastScanWasSale(boolean value) {
		lastScanWasSale = value;
	}

	public String getChainSKU() { return chainSku; }

	protected void setChainSKU(String value) { chainSku = value; }

	public Double getInStockPriceMin() { return inStockPriceMin; }

	protected void setInStockPriceMin(Double value) { inStockPriceMin = value; }

	public Double getInStockPriceMax() { return inStockPriceMax; }

	protected void setInStockPriceMax(Double value) { inStockPriceMax = value; }


	/**
	 * Renders object as just its product name.
	 * @return Rendered product to string
	 */
	@Override
	public String toString() {
		return productName;
	}

	/**
	 * Determines the display reorder code based on our field values.
	 * @return Reorder code to display
	 */
	public String getDisplayReorderCode() {
		String res = getCurrentReorderCode();
		if ((res == null) || res.matches("^\\s*$")) {
			res = getPreviousReorderCode();
			if ((res == null) || res.matches("^\\s*$")) {
				res = "--";
			}
		}
		return res;
	}

	private int id;
	private int clientId;
	private int chainId;
	private int globalProductId;
	private String brandName;
	private String brandNameShort;
	private String productName;
	private String upc;
	private Double msrp;
	private boolean randomWeight;
	private Double retailPriceMin;
	private Double retailPriceMax;
	private Double retailPriceAverage;
	private String categoryName;
	private String subcategoryName;
	private String productTypeName;
	private String currentReorderCode;
	private String previousReorderCode;
	private String brandSku;
	private Date lastScannedAt;
	private Double lastScannedPrice;
	private boolean lastScanWasSale;
	private String chainSku;
	private Double inStockPriceMin;
	private Double inStockPriceMax;
}
