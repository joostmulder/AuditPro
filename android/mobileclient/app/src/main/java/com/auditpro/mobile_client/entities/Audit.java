/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.entities;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

import com.auditpro.mobile_client.database.AuditRecord;
import com.auditpro.mobile_client.database.BaseDatabase;

import java.text.DateFormat;
import java.util.Date;
import java.util.UUID;


/**
 * Records an audit session at a store.
 * @author Eric Ruck
 */
public class Audit implements Parcelable {

	/**
	 * Default constructor for internal use.
	 */
	private Audit() { }

	/**
	 * Initializes a new instance with DAL source.
	 * @param source Database source
	 */
	public Audit(AuditRecord source) {
		this();
		setId(source.getId());
		setUserId(source.getUserId());
		setStoreId(source.getStoreId());
		setStoreDescr(source.getStoreDescr());
		setAuditStartedAt(source.getAuditStartedAt());
		setAuditEndedAt(source.getAuditEndedAt());
		setAuditTypeId(source.getAuditTypeId());
		setLatitudeAtStart(source.getLatitudeAtStart());
		setLongitudeAtStart(source.getLongitudeAtStart());
		setLatitudeAtEnd(source.getLatitudeAtEnd());
		setLongitudeAtEnd(source.getLongitudeAtEnd());
	}

	public UUID getId() {
		return id;
	}

	protected void setId(UUID value) {
		id = value;
	}

	public int getUserId() {
		return userId;
	}

	private void setUserId(int value) {
		userId = value;
	}

	public int getStoreId() {
		return storeId;
	}

	private void setStoreId(int value) {
		storeId = value;
	}

	public String getStoreDescr() {
		return storeDescr;
	}

	private void setStoreDescr(String value) {
		storeDescr = value;
	}

	public Date getAuditStartedAt() {
		return auditStartedAt;
	}

	private void setAuditStartedAt(Date value) {
		auditStartedAt = value;
	}

	public Date getAuditEndedAt() {
		return auditEndedAt;
	}

	private void setAuditEndedAt(Date value) {
		auditEndedAt = value;
	}

	public int getAuditTypeId() {
		return auditTypeId;
	}

	private void setAuditTypeId(int value) {
		auditTypeId = value;
	}

	public Double getLatitudeAtStart() {
		return latitudeAtStart;
	}

	private void setLatitudeAtStart(Double value) {
		latitudeAtStart = value;
	}

	public Double getLongitudeAtStart() {
		return longitudeAtStart;
	}

	private void setLongitudeAtStart(Double value) {
		longitudeAtStart = value;
	}

	public Double getLatitudeAtEnd() {
		return latitudeAtEnd;
	}

	private void setLatitudeAtEnd(Double value) {
		latitudeAtEnd = value;
	}

	public Double getLongitudeAtEnd() {
		return longitudeAtEnd;
	}

	private void setLongitudeAtEnd(Double value) {
		longitudeAtEnd = value;
	}

	/**
	 * Formats entity for display.
	 * @return Formatted audit entity
	 */
	@Override
	public String toString() {
		DateFormat formatter = DateFormat.getDateInstance(DateFormat.SHORT);
		return String.format("%s @ %s", getStoreDescr(), formatter.format(getAuditStartedAt()));
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel parcel, int i) {
		parcel.writeString(id.toString());
		parcel.writeInt(userId);
		parcel.writeInt(storeId);
		parcel.writeString(storeDescr);
		parcel.writeString(BaseDatabase.parseDateTime(auditStartedAt));
		parcel.writeString(BaseDatabase.parseDateTime(auditEndedAt));
		parcel.writeInt(auditTypeId);
		parcel.writeValue(latitudeAtStart);
		parcel.writeValue(longitudeAtStart);
		parcel.writeValue(latitudeAtEnd);
		parcel.writeValue(longitudeAtEnd);
	}

	public static final Parcelable.Creator<Audit> CREATOR = new Parcelable.Creator<Audit>() {
		@SuppressLint("ParcelClassLoader")
		@Override
		public Audit createFromParcel(Parcel parcel) {
			Audit audit = new Audit();
			audit.setId(UUID.fromString(parcel.readString()));
			audit.setUserId(parcel.readInt());
			audit.setStoreId(parcel.readInt());
			audit.setStoreDescr(parcel.readString());
			audit.setAuditStartedAt(BaseDatabase.parseDateTime(parcel.readString()));
			audit.setAuditEndedAt(BaseDatabase.parseDateTime(parcel.readString()));
			audit.setAuditTypeId(parcel.readInt());
			audit.setLatitudeAtStart((Double) parcel.readValue(null));
			audit.setLongitudeAtStart((Double) parcel.readValue(null));
			audit.setLatitudeAtEnd((Double) parcel.readValue(null));
			audit.setLongitudeAtEnd((Double) parcel.readValue(null));
			return audit;
		}

		@Override
		public Audit[] newArray(int i) {
			return new Audit[i];
		}
	};

	private UUID id;
	private int userId;
	private int storeId;
	private String storeDescr;
	private Date auditStartedAt;
	private Date auditEndedAt;
	private int auditTypeId;
	private Double latitudeAtStart;
	private Double longitudeAtStart;
	private Double latitudeAtEnd;
	private Double longitudeAtEnd;
}
