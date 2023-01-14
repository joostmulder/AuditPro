//
//  ProductTable.swift
//  Mobile Client
//
//  Created by Eric Ruck on 2/22/18.
//  Copyright © 2018 AuditPro. All rights reserved.
//

import Foundation


/**
 * Provides an interface between the products table in the database and our application.
 * @author Eric Ruck
 */
class ProductTable {
	/**
	 * Create our table in the passed database.
	 * @param db Database in which to create our table
	 */
	static func createTable(db: BaseDatabase) -> Bool {
		// Build a create table statement
		let query = "CREATE TABLE " + TABLE_NAME + " (" + COL_ID + " INTEGER PRIMARY KEY, " +
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
				")"
		let res = sqlite3_exec(db.con, query, nil, nil, nil) == SQLITE_OK
		if (!res) {
			db.logLastError("create table " + TABLE_NAME)
		}
		return res
	}

	/**
	 * Updates our table to the current version if necessary
	 * @param db Connected database
	 * @param lastVersion Last version of our database
	 */
	static func updateTable(db: BaseDatabase, lastVersion: Int) -> Bool {
		return true
	}

	/**
	 * Gets all of the products for a store.
	 * @param db Database contains products
	 * @param store Store whose products we want
	 * @return The products for store or nil
	 */
	static func getProducts(db: BaseDatabase, store: Store) -> [Product]? {
		// Execute the query
		let query = String(format: "SELECT %@ FROM %@ WHERE %@=%d AND %@=%d",
			db.formatColumns(TABLE_COLS), TABLE_NAME, COL_CLIENT_ID,
			store.clientId, COL_CHAIN_ID, store.chainId)

		// Execute the query
		var stmt: OpaquePointer?
		if (sqlite3_prepare_v2(db.con, query, -1, &stmt, nil) != SQLITE_OK) {
			db.logLastError("get products")
			return nil
		}

		// Get the results
		var res = [Product]()
		while (sqlite3_step(stmt) == SQLITE_ROW) {
			res.append(Product(
				id: Int(sqlite3_column_int(stmt, 0)),
				clientId: Int(sqlite3_column_int(stmt, 1)),
				chainId: Int(sqlite3_column_int(stmt, 2)),
				globalProductId: Int(sqlite3_column_int(stmt, 3)),
				brandName: db.column(stmt: stmt, stringParam: 4)!,
				productName: db.column(stmt: stmt, stringParam: 6)!,
				upc: db.column(stmt: stmt, stringParam: 7)!,
				brandNameShort: db.column(stmt: stmt, stringParam: 5),
				msrp: db.column(stmt: stmt, doubleParam: 8),
				randomWeight: db.column(stmt: stmt, boolParam: 9)!,
				retailPriceMin: db.column(stmt: stmt, doubleParam: 10),
				retailPriceMax: db.column(stmt: stmt, doubleParam: 11),
				retailPriceAverage: db.column(stmt: stmt, doubleParam: 12),
				categoryName: db.column(stmt: stmt, stringParam: 13),
				subcategoryName: db.column(stmt: stmt, stringParam: 14),
				productTypeName: db.column(stmt: stmt, stringParam: 15),
				currentReorderCode: db.column(stmt: stmt, stringParam: 16),
				previousReorderCode: db.column(stmt: stmt, stringParam: 17),
				brandSku: db.column(stmt: stmt, stringParam: 18),
				lastScannedAt: db.column(stmt: stmt, dateParam: 19),
				lastScannedPrice: db.column(stmt: stmt, doubleParam: 20),
				lastScanWasSale: db.column(stmt: stmt, boolParam: 21)!,
				chainSku: db.column(stmt: stmt, stringParam: 22),
				inStockPriceMin: db.column(stmt: stmt, doubleParam: 23),
				inStockPriceMax: db.column(stmt: stmt, doubleParam: 24)
			))
		}

		// Return the query results
		sqlite3_finalize(stmt)
		return res
	}

	/**
	 * Replaces all of the store records with the passed data.
	 * @param db Database to replace
	 * @param products Products to save to database
	 * @return Success flag
	 */
	static func replaceWith(db: BaseDatabase, products: [Product]) -> Bool {
		// Delete the current records
		let delQuery = "DELETE FROM " + TABLE_NAME
		if (sqlite3_exec(db.con, delQuery, nil, nil, nil) != SQLITE_OK) {
			db.logLastError("replace products delete")
			return false
		}

		// Insert the new records
		var stmt: OpaquePointer?
		let insQuery = "INSERT INTO " + TABLE_NAME + "(" +
			db.formatColumns(TABLE_COLS) +
			") VALUES (" + db.formatColParams(TABLE_COLS) + ")"
		for product in products {
			// Insert the current store
			if (sqlite3_prepare_v2(db.con, insQuery, -1, &stmt, nil) != SQLITE_OK) {
				db.logLastError("replace products insert prepare")
				return false
			}
			db.bind(stmt:stmt, param:1, toInt:product.id)
			db.bind(stmt:stmt, param:2, toInt:product.clientId)
			db.bind(stmt:stmt, param:3, toInt:product.chainId)
			db.bind(stmt:stmt, param:4, toInt:product.globalProductId)
			db.bind(stmt:stmt, param:5, toString:product.brandName)
			db.bind(stmt:stmt, param:6, toString:product.brandNameShort)
			db.bind(stmt:stmt, param:7, toString:product.productName)
			db.bind(stmt:stmt, param:8, toString:product.upc)
			db.bind(stmt:stmt, param:9, toDouble:product.msrp)
			db.bind(stmt:stmt, param:10, toBool:product.randomWeight)
			db.bind(stmt:stmt, param:11, toDouble:product.retailPriceMin)
			db.bind(stmt:stmt, param:12, toDouble:product.retailPriceMax)
			db.bind(stmt:stmt, param:13, toDouble:product.retailPriceAverage)
			db.bind(stmt:stmt, param:14, toString:product.categoryName)
			db.bind(stmt:stmt, param:15, toString:product.subcategoryName)
			db.bind(stmt:stmt, param:16, toString:product.productTypeName)
			db.bind(stmt:stmt, param:17, toString:product.currentReorderCode)
			db.bind(stmt:stmt, param:18, toString:product.previousReorderCode)
			db.bind(stmt:stmt, param:19, toString:product.brandSku)
			db.bind(stmt:stmt, param:20, toDate:product.lastScannedAt)
			db.bind(stmt:stmt, param:21, toDouble:product.lastScannedPrice)
			db.bind(stmt:stmt, param:22, toBool:product.lastScanWasSale)
			db.bind(stmt:stmt, param:23, toString:product.chainSku)
			db.bind(stmt:stmt, param:24, toDouble:product.inStockPriceMin)
			db.bind(stmt:stmt, param:25, toDouble:product.inStockPriceMax)

			let success = sqlite3_step(stmt) == SQLITE_DONE
			sqlite3_finalize(stmt)
			if (!success) {
				db.logLastError("replace products insert complete")
				return false
			}
		}

		// All stores saved
		return true
	}

	static let TABLE_NAME = "products"
	private static let COL_ID = "chain_x_product_id"
	private static let COL_CLIENT_ID = "client_id"
	private static let COL_CHAIN_ID = "chain_id"
	private static let COL_PRODUCT_ID = "product_id"
	private static let COL_BRAND_NAME = "brand_name"
	private static let COL_BRAND_NAME_SHORT = "brand_name_short"
	private static let COL_PRODUCT_NAME = "product_name"
	private static let COL_UPC = "upc"
	private static let COL_MSRP = "msrp"
	private static let COL_RANDOM_WEIGHT = "is_random_weight"
	private static let COL_RETAIL_PRICE_MIN = "retail_price_min"
	private static let COL_RETAIL_PRICE_MAX = "retail_price_max"
	private static let COL_RETAIL_PRICE_AVERAGE = "retail_price_average"
	private static let COL_CATEGORY_NAME = "category_name"
	private static let COL_SUBCATEGORY_NAME = "subcategory_name"
	private static let COL_PRODUCT_TYPE_NAME = "product_type_name"
	private static let COL_CURRENT_REORDER_CODE = "current_reorder_code"
	private static let COL_PREVIOUS_REORDER_CODE = "previous_reorder_code"
	private static let COL_BRAND_SKU = "brand_sku"
	private static let COL_LAST_SCANNED_AT = "last_scanned_at"
	private static let COL_LAST_SCANNED_PRICE = "last_scanned_price"
	private static let COL_LAST_SCAN_WAS_SALE = "last_scan_was_sale"
	private static let COL_CHAIN_SKU = "chain_sku"
	private static let COL_IN_STOCK_PRICE_MIN = "in_stock_price_min"
	private static let COL_IN_STOCK_PRICE_MAX = "in_stock_price_max"
	private static let TABLE_COLS: [String] = [
		COL_ID,
		COL_CLIENT_ID,
		COL_CHAIN_ID,
		COL_PRODUCT_ID,
		COL_BRAND_NAME,
		COL_BRAND_NAME_SHORT,
		COL_PRODUCT_NAME,
		COL_UPC,
		COL_MSRP,
		COL_RANDOM_WEIGHT,
		COL_RETAIL_PRICE_MIN,
		COL_RETAIL_PRICE_MAX,
		COL_RETAIL_PRICE_AVERAGE,
		COL_CATEGORY_NAME,
		COL_SUBCATEGORY_NAME,
		COL_PRODUCT_TYPE_NAME,
		COL_CURRENT_REORDER_CODE,
		COL_PREVIOUS_REORDER_CODE,
		COL_BRAND_SKU,
		COL_LAST_SCANNED_AT,
		COL_LAST_SCANNED_PRICE,
		COL_LAST_SCAN_WAS_SALE,
		COL_CHAIN_SKU,
		COL_IN_STOCK_PRICE_MIN,
		COL_IN_STOCK_PRICE_MAX]
}
