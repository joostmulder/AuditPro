/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.auditpro.mobile_client.MobileClientException;
import com.auditpro.mobile_client.entities.Audit;
import com.auditpro.mobile_client.entities.Notes;
import com.auditpro.mobile_client.entities.Product;
import com.auditpro.mobile_client.entities.Receipt;
import com.auditpro.mobile_client.entities.ReorderStatus;
import com.auditpro.mobile_client.entities.Report;
import com.auditpro.mobile_client.entities.Scan;
import com.auditpro.mobile_client.security.Security;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;


/**
 * Provides access to our audits that have not yet been uploaded.
 * @author Eric Ruck
 */
public class AuditDatabase extends BaseDatabase {

	/**
	 * Instantiates an object to access our stores database.
	 * @param context Application context
	 */
	public AuditDatabase(Context context) {
		super(context, "audit", DB_VERSION_CURRENT);
	}

	/**
	 * Delegates table creation to the subclass.
	 * @param db Database in which table needs to be created
	 */
	@Override
	protected void onCreateDb(SQLiteDatabase db) {
		AuditRecord.createTable(db);
		ScanRecord.createTable(db);
		ReportRecord.createTable(db);
		NotesRecord.createTable(db);
		ConditionsRecord.createTable(db);
	}

	/**
	 * Handles table update event from the parent database.
	 * @param db Database in which table needs to be created
	 * @param lastVersion Last database version of our current tables
	 */
	@Override
	protected void onUpdateDb(SQLiteDatabase db, int lastVersion) {
		AuditRecord.updateTable(db, lastVersion);
		ScanRecord.updateTable(db, lastVersion);
		ReportRecord.updateTable(db, lastVersion);
		NotesRecord.updateTable(db, lastVersion);
		ConditionsRecord.updateTable(db, lastVersion);
	}

	/**
	 * Gets the number of completed audits waiting for synchronization.
	 * @param userId Current user
	 * @return Count of complete audits for user
	 * @throws MobileClientException Database error
	 */
	public int completeCount(int userId) throws MobileClientException {
		try {
			return AuditRecord.getCount(getCon(), userId, true);
		} catch (SQLiteException excSql) {
			// Unexpected database error
			throw new MobileClientException(
					"Failed to count completed audits in database",
					excSql);
		}
	}

	/**
	 * Gets all of the completed audits waiting for synchronization.
	 * @param userId User identifier
	 * @return List of complete audits
	 */
	public List<Audit> getCompleteAudits(int userId) {
		return AuditRecord.getCompleteAudits(getCon(), userId);
	}

	/**
	 * Begins a new audit. Fails if the user already has an audit in progress.
	 * @param userId Current user
	 * @param storeId Audited store
	 * @param storeDescr Store description for UI
	 * @param auditTypeId Type of audit
	 * @param latitude Starting latitude or null
	 * @param longitude Starting longitude or null
	 * @return The audit being started
	 * @throws MobileClientException Audit already in progress (or database error, unlikely)
	 */
	public Audit startAudit(int userId, int storeId, String storeDescr, int auditTypeId,
		Double latitude, Double longitude) throws MobileClientException {
		// Make sure there is no audit in progress
		if (AuditRecord.getCount(getCon(), userId, false) > 0) {
			throw new MobileClientException(
					"There is currently an audit in progress.");
		}
		try {
			// Create the new audit
			AuditRecord start = new AuditRecord(userId, storeId, storeDescr,
					auditTypeId, latitude, longitude);
			start.insertTo(getCon());
			return new Audit(start);
		} catch (SQLiteException excSql) {
			// Unexpected database error
			throw new MobileClientException(
					"Failed to create new audit in database",
					excSql);
		}
	}

	/**
	 * Resumes an audit in progress for the user. Returns null if no audit is in progress.
	 * @param userId Current user
	 * @return Audit in progress
	 * @throws MobileClientException Likely database error attempting to resume
	 */
	public Audit resumeAudit(int userId) throws MobileClientException {
		try {
			return AuditRecord.getAudit(getCon(), userId);
		} catch (SQLiteException excSql) {
			// Unexpected database error
			throw new MobileClientException(
					"Failed to locate open audit in database",
					excSql);
		}
	}

	/**
	 * Completes the audit in progress.
	 * @param audit Audit in progress
	 * @param latitute Final latitude or null
	 * @param longitude Final longitude or null
	 * @param endTime Optional end of audit time or null
	 * @throws MobileClientException Database error
	 */
	public void completeAudit(Audit audit, Double latitute, Double longitude, Date endTime)
		throws MobileClientException {
		try {
			// Update record
			AuditRecord complete = new AuditRecord(audit);
			complete.endAudit(latitute, longitude, endTime);
			complete.update(getCon());
		} catch (SQLiteException excSql) {
			// Unexpected database error
			throw new MobileClientException(
					"Failed to update completed audit in database",
					excSql);
		}
	}

	/**
	 * Reopens a previously closed audit
	 * @param audit Audit to reopen
	 * @return Reopened audit
	 * @throws MobileClientException Database error
	 */
	public Audit reopenAudit(Audit audit)
			throws MobileClientException {
		try {
			// Update record
			AuditRecord reopen = new AuditRecord(audit);
			reopen.reopenAudit();
			reopen.update(getCon());
			return new Audit(reopen);
		} catch (SQLiteException excSql) {
			// Unexpected database error
			throw new MobileClientException(
					"Failed to update completed audit in database",
					excSql);
		}
	}

	/**
	 * Adds a scan to this audit.
	 * @param scan Scan to add
	 * @throws MobileClientException Database error
	 */
	public void addScan(Scan scan) throws MobileClientException {
		try {
			// Create the new record
			new ScanRecord(scan).insert(getCon());
		} catch (SQLiteException excSql) {
			// Unexpected database error
			throw new MobileClientException(
					"Failed to add scan to database",
					excSql);
		}
	}

	/**
	 * Updates a scan in this audit.
	 * @param scan Scan to update
	 * @throws MobileClientException Database error
	 */
	public void updateScan(Scan scan) throws MobileClientException {
		try {
			// Apply the update to the database
			new ScanRecord(scan).update(getCon());
		} catch (SQLiteException excSql) {
			// Unexpected database error
			throw new MobileClientException(
					"Failed to update scan in database",
					excSql);
		}
	}

	/**
	 * Gets a scan from the current audit.
	 * @param audit Current audit
	 * @param productId Identifier of scanned product
	 * @return The scan or null if none
	 * @throws MobileClientException Database error
	 */
	public Scan getScan(Audit audit, int productId) throws MobileClientException {
		try {
			// Find the record
			ScanRecord scan = ScanRecord.getScan(getCon(), audit, productId);
			return (scan == null) ? null : new Scan(scan);
		} catch (SQLiteException excSql) {
			// Unexpected database error
			throw new MobileClientException(
					"Failed to lookup scan in database",
					excSql);
		}
	}

	/**
	 * Gets a report from the current audit.
	 * @param audit Current audit
	 * @param productId Identifier of scanned product
	 * @return The report or null if none
	 * @throws MobileClientException Database or state error
	 */
	public Report getReport(Audit audit, int productId) throws MobileClientException {
		try {
			// Find the record
			ReportRecord report = ReportRecord.getReport(getCon(), audit, productId);
			return (report == null) ? null : new Report(report);
		} catch (SQLiteException excSql) {
			// Unexpected database error
			throw new MobileClientException(
					"Failed to lookup report in database",
					excSql);
		}
	}

	/**
	 * Adds a report to the audit in progress.
	 * @param report Report to add
	 * @throws MobileClientException Database or state error
	 */
	public void addReport(Report report) throws MobileClientException {
		try {
			// Create the new report
			ReportRecord rec = new ReportRecord(report);
			rec.insert(getCon());
		} catch (SQLiteException excSql) {
			// Unexpected database error
			throw new MobileClientException(
					"Failed to add report in database",
					excSql);
		}
	}

	/**
	 * Updates a report in the audit in progress.
	 * @param report Report to update
	 * @throws MobileClientException Database or state error
	 */
	public void updateReport(Report report) throws MobileClientException {
		if (report.getId() == null) {
			// The source report is implicit, we actually need to add
			addReport(report);
		}
		try {
			// Create the new report
			ReportRecord rec = new ReportRecord(report);
			rec.update(getCon());
		} catch (SQLiteException excSql) {
			// Unexpected database error
			throw new MobileClientException(
					"Failed to update report in database",
					excSql);
		}
	}

	/**
	 * Gets all of the reports for the current audit.
	 * <p>
	 * Some of the reports returned may be implicit, e.g. In Stock for scanned items.  Pass null for
	 * products to return only explicit records.
	 * @param audit Current audit
	 * @param products Products for store, optional, see remarks
	 * @return Audit reports
	 * @throws MobileClientException Database or state error
	 */
	public List<Report> getAllReports(Audit audit, List<Product> products) throws MobileClientException {
		try {
			// Start with the actual reports
			List<Report> reports = ReportRecord.getReports(getCon(), audit);
			if (products == null) {
				// We only want the real records
				return reports;
			}

			// Merge with scans
			List<ScanRecord> scans = ScanRecord.getScans(getCon(), audit);
			List<Report> append = new ArrayList<>();
			for(ScanRecord scan : scans) {
				Report found = null;
				for(Report seek : reports) {
					if (seek.getProductId() == scan.getProductId()) {
						found = seek;
						break;
					}
				}
				if (found == null) {
					append.add(new Report(scan));
				}
			}
			reports.addAll(append);

			// Merge with products
			append = new ArrayList<>();
			for (Product prod : products) {
				Report found = null;
				for (Report seek : reports) {
					if (seek.getProductId() == prod.getId()) {
						found = seek;
						break;
					}
				}
				if (found == null) {
					append.add(new Report(audit, prod, null));
				}
			}
			reports.addAll(append);
			return reports;
		} catch (SQLiteException excSql) {
			// Unexpected database error
			throw new MobileClientException(
					"Failed to get reports for audit in database",
					excSql);
		}
	}

	/**
	 * Populates the passed receipt with the out of stock items in the passed audit.
	 * @param receipt Receipt to populate
	 * @param audit Current audit
	 * @param products Products for store
	 * @throws MobileClientException Database error
	 */
	public void populateReceipt(Receipt receipt, Audit audit, List<Product> products)
			throws MobileClientException {
		try {
			// Start with the actual reports
			List<Report> reports = ReportRecord.getReports(getCon(), audit);

			// Sort products for output
			ArrayList<Product> sortedProducts = new ArrayList<>(products);
			Collections.sort(sortedProducts, new Comparator<Product>() {
				@Override
				public int compare(Product product, Product t1) {
					return product.getProductName().compareTo(t1.getProductName());
				}
			});

			// Look for all products that will go on the receipt
			Security sec = new Security(ctx);
			boolean printVoids = sec.optSettingBool(Security.SETTING_PRINT_VOIDS, false);
			boolean printConditions = sec.optSettingBool(Security.SETTING_PRINT_CONDITIONS, false);
			boolean printNotes =
					sec.optSettingBool(Security.SETTING_AUDIT_STORE_NOTES, false) &&
					sec.optSettingBool(Security.SETTING_PRINT_STORE_NOTES, false);
			for(Product product : sortedProducts) {
				// Should we include the current product on the receipt?
				for (Report report : reports) {
					if (report.getProductId() == product.getId()) {
						if (report.getReorderStatusId() == ReorderStatus.OUT_OF_STOCK.getId()) {
							receipt.addOutOfStockItem(product.getDisplayReorderCode(), product.getProductName());
						} else if (printVoids && (report.getReorderStatusId() == ReorderStatus.VOID.getId())) {
							receipt.addVoidItem(product.getDisplayReorderCode(), product.getProductName());
						}
						break;
					}
				}
				if (printConditions) {
					// Check for SKU conditions
					receipt.addSKUConditions(sec.getSKUConditions(), getSelectedSKUConditions(audit, product.getId()),
							product.getDisplayReorderCode(), product.getProductName());

				}
			}

			// Should we check store notes?
			if (printNotes) {
				// Check store notes
				Notes notes = getNotes(audit);
				if ((notes != null) && !notes.isStoreEmpty()) {
					// Add store notes
					receipt.setStoreNotes(notes.getStore());
				}
			}
		} catch (SQLiteException excSql) {
			// Unexpected database error
			throw new MobileClientException(
					"Failed to get reports for audit in database",
					excSql);
		}
	}

	/**
	 * Gets the notes associated with an audit.
	 * @param audit Audit whose notes we want
	 * @return Audit notes
	 * @throws MobileClientException Database error
	 */
	public Notes getNotes(Audit audit) throws MobileClientException {
		return getNotes(audit.getId());
	}

	/**
	 * Gets the notes associated with an audit.
	 * @param auditId Id of audit whose notes we want
	 * @return Audit notes
	 * @throws MobileClientException Database error
	 */
	private Notes getNotes(UUID auditId) throws MobileClientException {
		try {
			// Find the record
			return NotesRecord.getNote(getCon(), auditId);
		} catch (SQLiteException excSql) {
			// Unexpected database error
			throw new MobileClientException(
					"Failed to lookup notes in database",
					excSql);
		}
	}

	/**
	 * Updates the notes for an audit.
	 * @param notes Notes to update
	 * @param contents New contents for internal notes
	 * @param store New contents for store notes
	 * @throws MobileClientException Database error
	 */
	public void updateNotes(Notes notes, String contents, String store) throws MobileClientException {
		try {
			// Is this a new notes record?
			new NotesRecord(notes, contents, store).update(getCon());
		} catch (SQLiteException excSql) {
			// Unexpected database error
			String method = (notes.getId() == null) ? "create" : "update";
			throw new MobileClientException(
					String.format("Failed to %s notes for audit %s in database",
							method, notes.getAuditId().toString()),
					excSql);
		}
	}

	/**
	 * Gets the selected SKU conditions for a product from the current audit.
	 * @param audit Current audit
	 * @param productId Identifies the product
	 * @return The selected conditions or null if none
	 * @throws MobileClientException Database error
	 */
	public Set<Integer> getSelectedSKUConditions(Audit audit, int productId) throws MobileClientException {
		try {
			// Find the record
			ConditionsRecord conditions = ConditionsRecord.getConditions(getCon(), audit, productId);
			return (conditions == null) ? null : conditions.getConditions();
		} catch (SQLiteException excSql) {
			// Unexpected database error
			throw new MobileClientException(
					"Failed to lookup conditions in database",
					excSql);
		}
	}

	/**
	 * Updates the selected SKU conditions for a product in the current audit.
	 * @param audit Current audit
	 * @param productId Identifies the product
	 * @param selectedConditions Selected conditions or null if none
	 * @throws MobileClientException Database error
	 */
	public void updateSelectedSKUConditions(Audit audit, int productId, Set<Integer> selectedConditions)
			throws MobileClientException {
		try {
			// Find the record
			ConditionsRecord.setConditions(getCon(), audit, productId, selectedConditions);
		} catch (SQLiteException excSql) {
			// Unexpected database error
			throw new MobileClientException(
					"Failed to lookup conditions in database",
					excSql);
		}
	}

	/**
	 * Serialize a completed audit to a JSON formatted string.
	 * @param audit Audit to serialize
	 * @return Serialized audit
	 * @throws MobileClientException Database or serialization fault
	 */
	public String serializeAudit(Audit audit) throws MobileClientException {
		try {
			// Get the related scans and reports
			JSONArray scans = ScanRecord.getJSON(getCon(), audit);
			JSONArray reports = ReportRecord.getJSON(getCon(), audit);
			JSONArray skuConditions = ConditionsRecord.getJSON(getCon(), audit);

			// Get our notes
			Notes notes = getNotes(audit.getId());

			// Get the user info
			JSONObject user = new JSONObject();
			Security sec = new Security(ctx);
			user.put("userId", sec.getUserId());
			user.put("clientId", sec.getClientId());

			// Generate the result
			JSONObject res = new JSONObject();
			res.put("id", audit.getId().toString());
			res.put("storeId", audit.getStoreId());
			res.put("auditStartedAt", BaseDatabase.parseDateTime(audit.getAuditStartedAt()));
			res.put("auditEndedAt", BaseDatabase.parseDateTime(audit.getAuditEndedAt()));
			res.put("latitudeAtStart", audit.getLatitudeAtStart());
			res.put("longitudeAtStart", audit.getLongitudeAtStart());
			res.put("latitudeAtEnd", audit.getLatitudeAtEnd());
			res.put("longitudeAtEnd", audit.getLongitudeAtEnd());
			res.put("user", user);
			res.put("scans", scans);
			res.put("reports", reports);
			res.put("skuConditions", skuConditions);
			res.put("notes", notes.getContents());
			res.put("audit_store_note", notes.getStore());
			return res.toString();
		} catch (JSONException excJSON) {
			// Unexpected sieralization error
			throw new MobileClientException(
					"Failed to serialized details for requested audit",
					excJSON);
		} catch (SQLiteException excSql) {
			// Unexpected database error
			throw new MobileClientException(
					"Failed to get details for requested audit",
					excSql);
		}
	}

	/**
	 * Deletes an audit from the local cache.
	 * @param audit Audit to delete
	 * @throws MobileClientException Database error
	 */
	public void deleteAudit(Audit audit) throws MobileClientException {
		SQLiteDatabase db = getCon();
		try {
			// Delete records related to audit atomically
			db.beginTransaction();
			ScanRecord.deleteFor(getCon(), audit);
			ReportRecord.deleteFor(getCon(), audit);
			AuditRecord.deleteFor(getCon(), audit);
			ConditionsRecord.deleteFor(getCon(), audit);
			db.setTransactionSuccessful();
		} catch (SQLiteException excSql) {
			// Unexpected database error
			db.endTransaction();
			throw new MobileClientException(
					"Failed to remove audit from local cache",
					excSql
			);
		} finally {
			// Complete transaction
			db.endTransaction();
		}
	}

	/**
	 * Gets the current version of this database.
	 * @return Current version code
	 */
	static public int getVersion() {
		return DB_VERSION_CURRENT;
	}

	@SuppressWarnings("unused")
	public static final int DB_VERSION_INIT = 1;
	public static final int DB_VERSION_16 = 2; // Build 16 store notes
	static final int DB_VERSION_18 = 3; // Build 18 SKU conditions
	private static final int DB_VERSION_CURRENT = DB_VERSION_18;
}
