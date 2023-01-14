/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.api;

import android.util.Log;

import com.auditpro.mobile_client.database.BaseDatabase;
import com.auditpro.mobile_client.entities.Product;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * Holds a response from the product query.
 * @author Eric Ruck
 */
class ProductResponse extends Product {

	/**
	 * Initialize store from a JSON source.
	 * @param source Initial data
	 */
	private ProductResponse(JSONObject source) {
		super();
		setId(source.optInt("chain_x_product_id"));
		setClientId(source.optInt("client_id"));
		setChainId(source.optInt("chain_id"));
		setGlobalProductId(source.optInt("product_id"));
		setBrandName(ApiClient.jsonString(source,"brand_name"));
		setBrandNameShort(ApiClient.jsonString(source,"brand_name_short"));
		setProductName(ApiClient.jsonString(source,"product_name"));
		setUPC(ApiClient.jsonString(source,"upc"));
		setMSRP(source.isNull("msrp") ? null : source.optDouble("msrp"));
		setRandomWeight(source.optBoolean("is_random_weight"));
		setRetailPriceMin(source.isNull("retail_price_min") ? null : source.optDouble("retail_price_min"));
		setRetailPriceMax(source.isNull("retail_price_max") ? null : source.optDouble("retail_price_max"));
		setRetailPriceAverage(source.isNull("retail_price_average") ? null : source.optDouble("retail_price_average"));
		setCategoryName(ApiClient.jsonString(source,"category_name"));
		setSubcategoryName(ApiClient.jsonString(source,"subcategory_name"));
		setProductTypeName(ApiClient.jsonString(source,"product_type_name"));
		setCurrentReorderCode(ApiClient.jsonString(source,"current_reorder_code"));
		setPreviousReorderCode(ApiClient.jsonString(source,"previous_reorder_code"));
		setBrandSKU(ApiClient.jsonString(source,"brand_sku"));
		setLastScannedAt(BaseDatabase.parseDateTime(ApiClient.jsonString(source,"last_scanned_at")));
		setLastScannedPrice(source.isNull("last_scanned_price") ? null : source.optDouble("last_scanned_price"));
		setLastScanWasSale(source.optBoolean("last_scan_was_sale"));
		setChainSKU(ApiClient.jsonString(source, "chain_sku"));
		setInStockPriceMin(source.isNull("in_stock_price_min") ? null : source.optDouble("in_stock_price_min"));
		setInStockPriceMax(source.isNull("in_stock_price_max") ? null : source.optDouble("in_stock_price_max"));
	}

	/**
	 * Validates the state of this store.
	 * @return Valid flag
	 */
	private boolean isValid() {
		return !((getId() <= 0) || (getClientId() <= 0) || (getChainId() <= 0) ||
				(getGlobalProductId() <= 0) || (getBrandName() == null) ||
				(getProductName() == null) || (getUPC() == null));
	}

	/**
	 * Parses the array response from the web service.
	 * @param source Array from the web service
	 * @return Parsed results
	 */
	static List<Product> fromJSON(JSONArray source) {
		// Parse the individual objects out of the store
		List<Product> res = new ArrayList<>();
		ProductResponse product = null;
		for (int index = 0; index < source.length(); ++index) {
			try {
				// Parse the current index
				product = new ProductResponse(source.getJSONObject(index));
				if (!product.isValid()) {
					// Invalid data in the product
					String name = product.getProductName();
					Log.w(LOG_TAG, String.format("Invalid product %d %s at %d",
							product.getId(), (name == null) ? "(null)" : name, index));
				} else {
					// Add the store to the result
					res.add(product);
				}
			} catch (JSONException excJSON) {
				// Failed to parse the current store index
				String id = (product == null) ? "(null)" : Integer.toString(product.getId());
				String name = (product == null) ? null : product.getProductName();
				Log.w(LOG_TAG, String.format("Failed to parse product %s %s at %d",
						id, (name == null) ? "(null)" : name, index));
			}
		}

		// Return the stores
		return res;
	}

	private static final String LOG_TAG = "StoreResponse";
}
