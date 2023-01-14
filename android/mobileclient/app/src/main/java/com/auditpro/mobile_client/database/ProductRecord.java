/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.auditpro.mobile_client.entities.Product;
import com.auditpro.mobile_client.entities.Store;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Provides an interface between the products table in the database and our application.
 * @author Eric Ruck
 */
public class ProductRecord {

	/**
	 * Initializes default instance with empty values.
	 */
	private ProductRecord() {
		setId(-1);
		setClientId(-1);
		setChainId(-1);
		setGlobalProductId(-1);
		setBrandName(null);
		setBrandNameShort(null);
		setProductName(null);
		setUPC(null);
		setMSRP(null);
		setRandomWeight(false);
		setRetailPriceMin(null);
		setRetailPriceMax(null);
		setRetailPriceAverage(null);
		setCategoryName(null);
		setSubcategoryName(null);
		setProductTypeName(null);
		setCurrentReorderCode(null);
		setPreviousReorderCode(null);
		setBrandSKU(null);
		setLastScannedAt(null);
		setLastScannedPrice(null);
		setLastScanWasSale(false);
		setChainSKU(null);
		setInStockPriceMin(null);
		setInStockPriceMax(null);
	}

	/**
	 * Create our table in the passed database.
	 * @param db Database in which to create our table
	 */
	static void createTable(SQLiteDatabase db) {
		// Build a create table statement
		String st = "CREATE TABLE " + TABLE_NAME + " (" + COL_ID + " INTEGER PRIMARY KEY, " +
				COL_CLIENT_ID + " INTEGER, " +
				COL_CHAIN_ID + " INTEGER, " +
				COL_PRODUCT_ID + " INTEGER, " +
				COL_BRAND_NAME + " TEXT, " +
				COL_BRAND_NAME_SHORT + " TEXT, " +
				COL_PRODUCT_NAME + " TEXT, " +
				COL_UPC + " TEXT, " +
				COL_MSRP + " DOUBLE, " +
				COL_RANDOM_WEIGHT + " TINYINT, " +
				COL_RETAIL_PRICE_MIN + " DOUBLE, " +
				COL_RETAIL_PRICE_MAX + " DOUBLE, " +
				COL_RETAIL_PRICE_AVERAGE + " DOUBLE, " +
				COL_CATEGORY_NAME + " TEXT, " +
				COL_SUBCATEGORY_NAME + " TEXT, " +
				COL_PRODUCT_TYPE_NAME + " TEXT, " +
				COL_CURRENT_REORDER_CODE + " TEXT, " +
				COL_PREVIOUS_REORDER_CODE + " TEXT, " +
				COL_BRAND_SKU + " TEXT, " +
				COL_LAST_SCANNED_AT + " TEXT, " +
				COL_LAST_SCANNED_PRICE + " DOUBLE, " +
				COL_LAST_SCAN_WAS_SALE + " TINYINT, " +
				COL_CHAIN_SKU + " TEXT, " +
				COL_IN_STOCK_PRICE_MIN + " DOUBLE, " +
				COL_IN_STOCK_PRICE_MAX + " DOUBLE" +
				")";

		// Execute it
		db.execSQL(st);
	}

	/**
	 * Updates our table to the current version if becessary
	 * @param db Connected database
	 * @param lastVersion Last version of our database
	 */
	static void updateTable(SQLiteDatabase db, int lastVersion) {
		if (lastVersion < StoresDatabase.DB_VERSION_11) {
			// Add fields
			db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COL_CHAIN_SKU + " TEXT;");
			db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COL_IN_STOCK_PRICE_MIN + " DOUBLE;");
			db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + COL_IN_STOCK_PRICE_MAX + " DOUBLE;");
		}
	}

	/**
	 * Gets all of the products for a store.
	 * @param db Database contains products
	 * @param store Store whose products we want
	 * @return The products for store
	 */
	static List<Product> getProducts(SQLiteDatabase db, Store store) {
		// Execute the query
		@SuppressLint("DefaultLocale")
		String query = String.format("SELECT * FROM %s WHERE %s=%d AND %s=%d",
				TABLE_NAME, COL_CLIENT_ID, store.getClientId(), COL_CHAIN_ID, store.getChainId());
		Cursor cursor = db.rawQuery(query, null);

		// Extract the results
		int idxId = -1;
		int idxClientId = -1;
		int idxChainId = -1;
		int idxProductId = -1;
		int idxBrandName = -1;
		int idxBrandNameShort = -1;
		int idxProductName = -1;
		int idxUPC = -1;
		int idxMSRP = -1;
		int idxRandomWeight = -1;
		int idxRetailPriceMin = -1;
		int idxRetailPriceMax = -1;
		int idxRetailPriceAvg = -1;
		int idxCategoryName = -1;
		int idxSubcategoryName = -1;
		int idxProductTypeName = -1;
		int idxCurrentReorderCode = -1;
		int idxPreviousReorderCode = -1;
		int idxBrandSKU = -1;
		int idxLastScannedAt = -1;
		int idxLastScannedPrice = -1;
		int idxLastScanWasSale = -1;
		int idxChainSKU = -1;
		int idxInStockPriceMin = -1;
		int idxInStockPriceMax = -1;
		ArrayList<Product> res = new ArrayList<>();
		while (cursor.moveToNext()) {
			if (idxId < 0) {
				// Get the field indices from the cursor
				idxId = cursor.getColumnIndex(COL_ID);
				idxClientId = cursor.getColumnIndex(COL_CLIENT_ID);
				idxChainId = cursor.getColumnIndex(COL_CHAIN_ID);
				idxProductId = cursor.getColumnIndex(COL_PRODUCT_ID);
				idxBrandName = cursor.getColumnIndex(COL_BRAND_NAME);
				idxBrandNameShort = cursor.getColumnIndex(COL_BRAND_NAME_SHORT);
				idxProductName = cursor.getColumnIndex(COL_PRODUCT_NAME);
				idxUPC = cursor.getColumnIndex(COL_UPC);
				idxMSRP = cursor.getColumnIndex(COL_MSRP);
				idxRandomWeight = cursor.getColumnIndex(COL_RANDOM_WEIGHT);
				idxRetailPriceMin = cursor.getColumnIndex(COL_RETAIL_PRICE_MIN);
				idxRetailPriceMax = cursor.getColumnIndex(COL_RETAIL_PRICE_MAX);
				idxRetailPriceAvg = cursor.getColumnIndex(COL_RETAIL_PRICE_AVERAGE);
				idxCategoryName = cursor.getColumnIndex(COL_CATEGORY_NAME);
				idxSubcategoryName = cursor.getColumnIndex(COL_SUBCATEGORY_NAME);
				idxProductTypeName = cursor.getColumnIndex(COL_PRODUCT_TYPE_NAME);
				idxCurrentReorderCode = cursor.getColumnIndex(COL_CURRENT_REORDER_CODE);
				idxPreviousReorderCode = cursor.getColumnIndex(COL_PREVIOUS_REORDER_CODE);
				idxBrandSKU = cursor.getColumnIndex(COL_BRAND_SKU);
				idxLastScannedAt = cursor.getColumnIndex(COL_LAST_SCANNED_AT);
				idxLastScannedPrice = cursor.getColumnIndex(COL_LAST_SCANNED_PRICE);
				idxLastScanWasSale = cursor.getColumnIndex(COL_LAST_SCAN_WAS_SALE);
				idxChainSKU = cursor.getColumnIndex(COL_CHAIN_SKU);
				idxInStockPriceMin = cursor.getColumnIndex(COL_IN_STOCK_PRICE_MIN);
				idxInStockPriceMax = cursor.getColumnIndex(COL_IN_STOCK_PRICE_MAX);
			}

			// Populate a new record
			ProductRecord record = new ProductRecord();
			record.setId(cursor.getInt(idxId));
			record.setClientId(cursor.getInt(idxClientId));
			record.setChainId(cursor.getInt(idxChainId));
			record.setGlobalProductId(cursor.getInt(idxProductId));
			record.setBrandName(cursor.getString(idxBrandName));
			record.setBrandNameShort(cursor.getString(idxBrandNameShort));
			record.setProductName(cursor.getString(idxProductName));
			record.setUPC(cursor.getString(idxUPC));
			record.setMSRP(cursor.getDouble(idxMSRP));
			record.setRandomWeight(cursor.getInt(idxRandomWeight) != 0);
			record.setRetailPriceMin(cursor.getDouble(idxRetailPriceMin));
			record.setRetailPriceMax(cursor.getDouble(idxRetailPriceMax));
			record.setRetailPriceAverage(cursor.getDouble(idxRetailPriceAvg));
			record.setCategoryName(cursor.getString(idxCategoryName));
			record.setSubcategoryName(cursor.getString(idxSubcategoryName));
			record.setProductTypeName(cursor.getString(idxProductTypeName));
			record.setCurrentReorderCode(cursor.getString(idxCurrentReorderCode));
			record.setPreviousReorderCode(cursor.getString(idxPreviousReorderCode));
			record.setBrandSKU(cursor.getString(idxBrandSKU));
			record.setLastScannedAt(BaseDatabase.parseDateTime(cursor.getString(idxLastScannedAt)));
			record.setLastScannedPrice(cursor.getDouble(idxLastScannedPrice));
			record.setLastScanWasSale(cursor.getInt(idxLastScanWasSale) != 0);
			if ((idxChainSKU >= 0) && !cursor.isNull(idxChainSKU)) {
				record.setChainSKU(cursor.getString(idxChainSKU));
			}
			if ((idxInStockPriceMin >= 0) && !cursor.isNull(idxInStockPriceMin)) {
				record.setInStockPriceMin(cursor.getDouble(idxInStockPriceMin));
			}
			if ((idxInStockPriceMax >= 0) && !cursor.isNull(idxInStockPriceMax)) {
				record.setInStockPriceMax(cursor.getDouble(idxInStockPriceMax));
			}

			// Add the store to the results
			res.add(new Product(record));
		}

		// Return the query results
		cursor.close();
		return res;
	}

	/**
	 * Replaces all of the store records with the passed data.
	 * @param db Database to replace
	 * @param products Products to save to database
	 */
	static void replaceWith(SQLiteDatabase db, List<Product> products) {
		// Wipe all the records
		db.delete(TABLE_NAME, null, null);
		for(Product product : products) {
			// Insert the current record
			ContentValues record = new ContentValues();
			record.put(COL_ID, product.getId());
			record.put(COL_CLIENT_ID, product.getClientId());
			record.put(COL_CHAIN_ID, product.getChainId());
			record.put(COL_PRODUCT_ID, product.getGlobalProductId());
			record.put(COL_BRAND_NAME, product.getBrandName());
			record.put(COL_BRAND_NAME_SHORT, product.getBrandNameShort());
			record.put(COL_PRODUCT_NAME, product.getProductName());
			record.put(COL_UPC, product.getUPC());
			record.put(COL_MSRP, product.getMSRP());
			record.put(COL_RANDOM_WEIGHT, product.isRandomWeight());
			record.put(COL_RETAIL_PRICE_MIN, product.getRetailPriceMin());
			record.put(COL_RETAIL_PRICE_MAX, product.getRetailPriceMax());
			record.put(COL_RETAIL_PRICE_AVERAGE, product.getRetailPriceAverage());
			record.put(COL_CATEGORY_NAME, product.getCategoryName());
			record.put(COL_SUBCATEGORY_NAME, product.getSubcategoryName());
			record.put(COL_PRODUCT_TYPE_NAME, product.getProductTypeName());
			record.put(COL_CURRENT_REORDER_CODE, product.getCurrentReorderCode());
			record.put(COL_PREVIOUS_REORDER_CODE, product.getPreviousReorderCode());
			record.put(COL_BRAND_SKU, product.getBrandSKU());
			record.put(COL_LAST_SCANNED_AT, BaseDatabase.parseDateTime(product.getLastScannedAt()));
			record.put(COL_LAST_SCANNED_PRICE, product.getLastScannedPrice());
			record.put(COL_LAST_SCAN_WAS_SALE, product.isLastScanWasSale());
			record.put(COL_CHAIN_SKU, product.getChainSKU());
			record.put(COL_IN_STOCK_PRICE_MIN, product.getInStockPriceMin());
			record.put(COL_IN_STOCK_PRICE_MAX, product.getInStockPriceMax());
			db.insert(TABLE_NAME, null, record);
		}
	}

	public int getId() {
		return id;
	}

	public void setId(int value) {
		id = value;
	}

	public int getClientId() {
		return clientId;
	}

	private void setClientId(int value) {
		clientId = value;
	}

	public int getChainId() {
		return chainId;
	}

	private void setChainId(int value) {
		chainId = value;
	}

	public int getGlobalProductId() {
		return globalProductId;
	}

	private void setGlobalProductId(int value) {
		globalProductId = value;
	}

	public String getBrandName() {
		return brandName;
	}

	private void setBrandName(String value) {
		brandName = value;
	}

	public String getBrandNameShort() {
		return brandNameShort;
	}

	private void setBrandNameShort(String value) {
		brandNameShort = value;
	}

	public String getProductName() {
		return productName;
	}

	private void setProductName(String value) {
		productName = value;
	}

	public String getUPC() {
		return upc;
	}

	private void setUPC(String value) {
		upc = value;
	}

	public Double getMSRP() {
		return msrp;
	}

	private void setMSRP(Double value) {
		msrp = value;
	}

	public boolean isRandomWeight() {
		return randomWeight;
	}

	private void setRandomWeight(boolean value) {
		randomWeight = value;
	}

	public Double getRetailPriceMin() {
		return retailPriceMin;
	}

	private void setRetailPriceMin(Double value) {
		retailPriceMin = value;
	}

	public Double getRetailPriceMax() {
		return retailPriceMax;
	}

	private void setRetailPriceMax(Double value) {
		retailPriceMax = value;
	}

	public Double getRetailPriceAverage() {
		return retailPriceAverage;
	}

	private void setRetailPriceAverage(Double value) {
		retailPriceAverage = value;
	}

	public String getCategoryName() {
		return categoryName;
	}

	private void setCategoryName(String value) {
		categoryName = value;
	}

	public String getSubcategoryName() {
		return subcategoryName;
	}

	private void setSubcategoryName(String value) {
		subcategoryName = value;
	}

	public String getProductTypeName() {
		return productTypeName;
	}

	private void setProductTypeName(String value) {
		productTypeName = value;
	}

	public String getCurrentReorderCode() {
		return currentReorderCode;
	}

	private void setCurrentReorderCode(String value) {
		currentReorderCode = value;
	}

	public String getPreviousReorderCode() {
		return previousReorderCode;
	}

	private void setPreviousReorderCode(String value) {
		previousReorderCode = value;
	}

	public String getBrandSKU() {
		return brandSku;
	}

	private void setBrandSKU(String value) {
		brandSku = value;
	}

	public Date getLastScannedAt() {
		return lastScannedAt;
	}

	private void setLastScannedAt(Date value) {
		lastScannedAt = value;
	}

	public Double getLastScannedPrice() {
		return lastScannedPrice;
	}

	private void setLastScannedPrice(Double value) {
		lastScannedPrice = value;
	}

	public boolean isLastScanWasSale() {
		return lastScanWasSale;
	}

	private void setLastScanWasSale(boolean value) {
		lastScanWasSale = value;
	}

	public String getChainSKU() { return chainSku; }

	private void setChainSKU(String value) { chainSku = value; }

	public Double getInStockPriceMin() { return inStockPriceMin; }

	private void setInStockPriceMin(Double value) { inStockPriceMin = value; }

	public Double getInStockPriceMax() { return inStockPriceMax; }

	private void setInStockPriceMax(Double value) { inStockPriceMax = value; }

	private static final String TABLE_NAME = "products";
	private static final String COL_ID = "chain_x_product_id";
	private static final String COL_CLIENT_ID = "client_id";
	private static final String COL_CHAIN_ID = "chain_id";
	private static final String COL_PRODUCT_ID = "product_id";
	private static final String COL_BRAND_NAME = "brand_name";
	private static final String COL_BRAND_NAME_SHORT = "brand_name_short";
	private static final String COL_PRODUCT_NAME = "product_name";
	private static final String COL_UPC = "upc";
	private static final String COL_MSRP = "msrp";
	private static final String COL_RANDOM_WEIGHT = "is_random_weight";
	private static final String COL_RETAIL_PRICE_MIN = "retail_price_min";
	private static final String COL_RETAIL_PRICE_MAX = "retail_price_max";
	private static final String COL_RETAIL_PRICE_AVERAGE = "retail_price_average";
	private static final String COL_CATEGORY_NAME = "category_name";
	private static final String COL_SUBCATEGORY_NAME = "subcategory_name";
	private static final String COL_PRODUCT_TYPE_NAME = "product_type_name";
	private static final String COL_CURRENT_REORDER_CODE = "current_reorder_code";
	private static final String COL_PREVIOUS_REORDER_CODE = "previous_reorder_code";
	private static final String COL_BRAND_SKU = "brand_sku";
	private static final String COL_LAST_SCANNED_AT = "last_scanned_at";
	private static final String COL_LAST_SCANNED_PRICE = "last_scanned_price";
	private static final String COL_LAST_SCAN_WAS_SALE = "last_scan_was_sale";
	private static final String COL_CHAIN_SKU = "chain_sku";
	private static final String COL_IN_STOCK_PRICE_MIN = "in_stock_price_min";
	private static final String COL_IN_STOCK_PRICE_MAX = "in_stock_price_max";

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
