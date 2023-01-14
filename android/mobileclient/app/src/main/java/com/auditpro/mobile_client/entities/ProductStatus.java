/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.entities;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;


/**
 * Maintains the status of products being displayed in this audit.
 * @author Eric Ruck
 */
public class ProductStatus implements Parcelable {

	/**
	 * Instantiates for a product.
	 * @param product Product represented by this status
	 */
	public ProductStatus(Product product) {
		this.product = product;
		this.reorderStatus = ReorderStatus.NONE;
		this.displayPrice = null;
	}

	/**
	 * For deserialization only.
	 */
	private ProductStatus() { }

	/**
	 * Compares ourself to an object.  Per our business logic, only tests the product ID, not
	 * the other values.
	 * @param obj Object to compare
	 * @return Equals flag
	 */
	@SuppressWarnings("SimplifiableIfStatement")
	@Override
	public boolean equals(Object obj) {
		if ((obj == null) || !(obj instanceof ProductStatus)) {
			return false;
		}
		return product.getId() == ((ProductStatus) obj).product.getId();
	}

	/**
	 * Required for serialization.
	 * @return Placeholder zero value
	 */
	@Override
	public int describeContents() {
		return 0;
	}

	/**
	 * Writes our fields to the passed parcel for serialization
	 * @param parcel Parcel receives fields
	 * @param i Serialization flags
	 */
	@Override
	public void writeToParcel(Parcel parcel, int i) {
		parcel.writeParcelable(product, i);
		parcel.writeInt(reorderStatus.getId());
		parcel.writeValue(displayPrice);
	}

	/**
	 * Creates a new instance of ourself from serialized parcel.
	 */
	public static final Parcelable.Creator<ProductStatus> CREATOR = new Parcelable.Creator<ProductStatus>() {
		/**
		 * Creates the instance initialized by the passed parcel.
		 * @param parcel Parcel source for our fields
		 * @return New instance
		 */
		@SuppressLint("ParcelClassLoader")
		@Override
		public ProductStatus createFromParcel(Parcel parcel) {
			ProductStatus res = new ProductStatus();
			res.setProduct((Product) parcel.readParcelable(null));
			res.setReorderStatus(parcel.readInt());
			res.setDisplayPrice((Double) parcel.readValue(null));
			return res;
		}

		/**
		 * Instantiates an array of our instances.
		 * @param i Create parcel flags
		 * @return Array instantiated to null instances
		 */
		@Override
		public ProductStatus[] newArray(int i) {
			return new ProductStatus[i];
		}
	};

	/**
	 * Gets the product associated with this status.
	 * @return Product details
	 */
	public Product getProduct() {
		return product;
	}

	/**
	 * Sets the product associated with this status.
	 * @param value Product to set
	 */
	private void setProduct(Product value) {
		product = value;
	}

	/**
	 * Gets the reorder status for this product.
	 * @return Reorder status
	 */
	public ReorderStatus getReorderStatus() {
		return reorderStatus;
	}

	/**
	 * Sets the reorder status for this product.
	 * @param value New reorder status
	 */
	private void setReorderStatus(ReorderStatus value) {
		reorderStatus = value;
	}

	/**
	 * Sets the reorder status for this product.
	 * @param valueId Id for new reorder status
	 */
	private void setReorderStatus(int valueId) {
		 setReorderStatus(ReorderStatus.fromId(valueId));
	}

	/**
	 * Gets the display price for this product.
	 * @return Display price (null if none)
	 */
	public Double getDisplayPrice() {
		return displayPrice;
	}

	/**
	 * Indicates if we have a display price recorded (vs null).
	 * @return Has display price flag
	 */
	public boolean hasDisplayPrice() {
		return displayPrice != null;
	}

	/**
	 * Sets the display price for this product.
	 * @param value New display price
	 */
	private void setDisplayPrice(Double value) {
		displayPrice = value;
	}

	/**
	 * Sets the display price from the passed scan.  Does not update if the passed scan is null.
	 * @param scan Optional scan with prices
	 */
	private void setDisplayPrice(Scan scan) {
		if (scan != null) {
			// Update the display price from the scan
			displayPrice = scan.getSalePrice();
			if (displayPrice == null) {
				displayPrice = scan.getRetailPrice();
			}
		}
	}

	/**
	 * Gets the type of this product.
	 * @return Product type
	 */
	public String getProductType() {
		return product.getProductTypeName();
	}

	/**
	 * Clones the passed source instance.
	 * @param source Instance to clone
	 */
	@SuppressWarnings("unused")
	public ProductStatus(ProductStatus source) {
		setProduct(source.getProduct());
		setReorderStatus(source.getReorderStatus());
		setDisplayPrice(source.getDisplayPrice());
	}

	/**
	 * Provides a title for this product for rendering in a list.
	 * @return Product title
	 */
	@Override public String toString() {
		return (product == null) ? "(None)" : product.toString();
	}

	/**
	 * Sets the reorder status from a report record.  If the report is null, the reorder status is
	 * set to NONE.  If the scan is not null, the display price is updated, otherwise it is left
	 * unchanged.
	 * @param report Record with status to set, optional
	 * @param scan Scan with recent price to set, optional
	 */
	public void setReorderStatus(Report report, Scan scan) {
		reorderStatus = (report == null)
			? ReorderStatus.NONE
			: ReorderStatus.fromId(report.getReorderStatusId());
		setDisplayPrice(scan);
	}

	/**
	 * Provides the product name.
	 * @return The name of the product
	 */
	public String getProductName() {
		return product.getProductName();
	}

	/**
	 * Provides the reorder status short text.
	 * @return The short name of the reorder status
	 */
	@SuppressWarnings("unused")
	public String getReorderStatusShortName() {
		return reorderStatus.getCode();
	}

	/**
	 * Provides the product ID as a string that can be bound to a button.
	 * @return The stringized product ID
	 */
	public String getId() {
		return Integer.toString(product.getId());
	}

	/**
	 * Updates from the passed source.
	 * @param source Source of update
	 * @param scan Scan with recent price to set, optional
	 */
	public void updateFrom(ProductStatus source, Scan scan) {
		setReorderStatus(source.getReorderStatus());
		setDisplayPrice(scan);
	}

	/** Product details.*/
	private Product product;

	/** Current reorder status. */
	private ReorderStatus reorderStatus;

	/** Price to display in summary, can be null. */
	private Double displayPrice;
}
