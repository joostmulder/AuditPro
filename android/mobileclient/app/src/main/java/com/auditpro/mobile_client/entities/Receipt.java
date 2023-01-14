/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.entities;

import android.annotation.SuppressLint;
import android.util.SparseArray;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;


/**
 * Manages the data for a printed receipt.
 * @author Eric Ruck
 */
public class Receipt {

	/**
	 * Creates a new receipt to print.
	 * If the audit stamp is not provided, the current time will be formatted for the current locale
	 * and used.
	 * @param customerName Customer name to display
	 * @param storeName Store name to display
	 * @param auditStamp Formatted audit stamp to display or null for now
	 */
	public Receipt(String customerName, String storeName, String auditStamp) {
		// Keep passed params
		this.customerName = customerName;
		this.storeName = storeName;
		if (auditStamp == null) {
			// Format now
			DateFormat formatter = DateFormat.getDateInstance(DateFormat.MEDIUM);
			this.auditStamp = formatter.format(new Date());
		} else {
			// Keep passed time stamp
			this.auditStamp = auditStamp;
		}
	}

	/**
	 * Adds an out of stock item to the receipt.
	 * @param reorderNumber Reorder number for item
	 * @param productName Product name for item
	 */
	public void addOutOfStockItem(String reorderNumber, String productName) {
		outofStockItems.add(new LineItem(reorderNumber, productName));
	}

	/**
	 * Adds a void item to the receipt.
	 * @param reorderNumber Reorder number for item
	 * @param productName Product name for item
	 */
	public void addVoidItem(String reorderNumber, String productName) {
		voidItems.add(new LineItem(reorderNumber, productName));
	}

	/**
	 * Adds a SKU condition item to the receipt.
	 * @param allConditions Descriptions of all possible conditions
	 * @param conditionIds Identifies SKU conditions associated with a product
	 * @param reorderNumber Reorder number for item
	 * @param productName Product name for item
	 */
	public void addSKUConditions(SparseArray<SKUCondition> allConditions, Set<Integer> conditionIds,
 			String reorderNumber, String productName) {
		// Check trivial case, no conditions
		if ((conditionIds == null) || (conditionIds.size() == 0)) {
			// No conditions associated
			return;
		}

		// Keep the condition details
		this.allConditions = allConditions;

		// Cycle through the conditions
		for(Integer conditionId : conditionIds) {
			// Have we started a line item list for this condition?
			ArrayList<LineItem> items = skuConditionItems.get(conditionId);
			if (items == null) {
				// No, start a new list
				items = new ArrayList<>();
				skuConditionItems.put(conditionId, items);
			}

			// Add the item to the condition
			items.add(new LineItem(reorderNumber, productName));
		}
	}

	/**
	 * Sets the store notes to display.
	 * @param storeNotes Notes to display or null for none
	 */
	public void setStoreNotes(String storeNotes) {
		this.storeNotes = storeNotes;
	}

	/** Provides the maximum line width in pixels. */
	private static final int MAX_LINE_WIDTH = 516;

	/** Provides the average character width in pixels. */
	private static final double AVG_CHAR_WIDTH = 20;


	/**
	 * Formats receipt output in Zebra ZPL format.
	 * @return Formatted ZPL document
	 */
	@SuppressLint("DefaultLocale")
	public String formatZpl() {
		// First determine the longest reorder code
		double longestReorderNumberWidth = calculateLongestReorderNumberWidth();

		// Determine the formatting type
		boolean longCodes = longestReorderNumberWidth > MAX_LINE_WIDTH / 3;
		int reorderWidth = longCodes
			? 0
			: (int)(longestReorderNumberWidth + (2 * AVG_CHAR_WIDTH));
		int detailWidth = MAX_LINE_WIDTH - reorderWidth;
		int maxLineChars = (int)(detailWidth / AVG_CHAR_WIDTH);

		// Start by building the formatting for the out of stock items
		StringBuilder formatItems = new StringBuilder();
		String sectionName = ((voidItems.size() == 0) || (skuConditionItems.size() == 0))
				? null
				: ReorderStatus.OUT_OF_STOCK.getName().toUpperCase();
		int position = appendFormatItems(sectionName, outofStockItems, formatItems,
			250, longCodes, reorderWidth, detailWidth, maxLineChars);
		for (int index = 0; index < skuConditionItems.size(); ++index) {
			// Add the current SKU condition
			SKUCondition condition = allConditions.get(skuConditionItems.keyAt(index));
			if (condition != null) {
				sectionName = condition.getName().toUpperCase();
				position = appendFormatItems(sectionName, skuConditionItems.valueAt(index),
						formatItems, position, longCodes, reorderWidth, detailWidth, maxLineChars);
			}
		}
		if (voidItems.size() > 0) {
			// Add the void items
			position = appendFormatItems(ReorderStatus.VOID.getName().toUpperCase(), voidItems,
					formatItems, position, longCodes, reorderWidth, detailWidth, maxLineChars);
		}

		// Do we have store notes?
		String formatNotes = "";
		if (storeNotes != null) {
			// Format the notes heading
			formatNotes = String.format("^FO25,%d^FB%d,1,0,C,0^FDNOTES:^FS", position + 27, MAX_LINE_WIDTH);
			position += 54;

			// Format the notes body
			int maxNotesChars = (int)(MAX_LINE_WIDTH / AVG_CHAR_WIDTH);
			int notesLines = (storeNotes.length() + maxNotesChars - 1) / maxNotesChars;
			formatNotes += String.format(
					"^FO25,%d^FB%d,%d,0,L,0^FD%s^FS",
					position, MAX_LINE_WIDTH, notesLines, storeNotes);
			position += (27 * notesLines);
		}

		// Start the complete document
		String doc = "! U1 setvar \"device.languages\" \"zpl\"\r\n ^XA^CFA,25";

		// Add the heading
		doc += String.format("^LL%d", position + 100); // Leave space for footer
		doc += String.format("^FO25,110^FB516,2,0,C,0^FD%s Reorder List For^FS", customerName);
		doc += String.format("^FO25,160^FB516,3,0,C,0^FD%s %s^FS", storeName, auditStamp);

		// Add the items
		doc += formatItems;
		doc += formatNotes;

		// Complete the document incl footer
		doc += String.format("^FO25,%d^FB516,1,0,C,0^FD--- www.AuditPRO.io ---^FS", position + 70);
		doc += "^XZ";
		return doc;
	}

	/**
	 * Calculates the width of the longest reorder number in any line.
	 * @return Longest reorder number width, pixels
	 */
	private double calculateLongestReorderNumberWidth() {
		double res = 0;
		for (LineItem item : outofStockItems) {
			double currentReorderCode = item.calculateReorderNumberWidth();
			if (currentReorderCode > res) {
				res = currentReorderCode;
			}
		}
		for (LineItem item : voidItems) {
			double currentReorderCode = item.calculateReorderNumberWidth();
			if (currentReorderCode > res) {
				res = currentReorderCode;
			}
		}
		for (int index = 0; index < skuConditionItems.size(); ++index) {
			for(LineItem item : skuConditionItems.valueAt(index)) {
				double currentReorderCode = item.calculateReorderNumberWidth();
				if (currentReorderCode > res) {
					res = currentReorderCode;
				}
			}
		}
		return res;
	}

	/**
	 * Appends the items in a list to the formetted document.
	 * @param items Items to append
	 * @param formatItems Formatted document in progress
	 * @param position Top position for items in document
	 * @param longCodes Long reorder codes flag
	 * @param reorderWidth Width for reorder codes
	 * @param detailWidth Width for details (product name)
	 * @param maxLineChars Maximum detail characters on a line
	 * @return Position on document after last item
	 */
	@SuppressLint("DefaultLocale")
	private int appendFormatItems(String sectionName, List<LineItem> items, StringBuilder formatItems,
  			int position, boolean longCodes, int reorderWidth, int detailWidth, int maxLineChars) {
		// Is there a setion name?
		if (sectionName != null) {
			// Yes, add it to the document
			formatItems.append(String.format("^FO25,%d^FB%d,1,0,C,0^FD%s:^FS",
					position + 27, MAX_LINE_WIDTH, sectionName));
			position += 54;
		}

		// Cycle through the items
		int lines;
		for (LineItem item : items) {
			if (longCodes) {
				// Format as a single line
				String longFormat = String.format("%s - %s", item.getReorderNumber(), item.getProductName());
				lines = (longFormat.length() + maxLineChars - 1) / maxLineChars;
				formatItems.append(String.format(
						"^FO25,%d^FB%d,%d,0,L,0^FD%s^FS",
						position, detailWidth, lines, longFormat));
			} else {
				// Two column format
				lines = (item.getProductName().length() + maxLineChars - 1) / maxLineChars;
				if (lines == 1) {
					formatItems.append(String.format(
							"^FO25,%d^FD%s^FS^FO%d,%d^FD%s^FS",
							position, item.getReorderNumber(),
							reorderWidth, position, item.getProductName()));
				} else {
					formatItems.append(String.format(
							"^FO25,%d^FD%s^FS^FO%d,%d^FB%d,%d,0,L,0^FD%s^FS",
							position, item.getReorderNumber(),
							reorderWidth, position, detailWidth, lines, item.getProductName()));
				}
			}

			// Update the position of the next line
			position += (27 * lines);
		}
		return position;
	}

	/**
	 * Stores one line item on the receipt.
	 */
	public class LineItem {
		/**
		 * Instantiates a new product line.
		 * @param reorderNumber Product reorder number
		 * @param productName Product name
		 */
		LineItem(String reorderNumber, String productName) {
			this.reorderNumber = reorderNumber;
			this.productName = productName;
		}

		/**
		 * Gets the reorder number for this product.
		 * @return Product reorder number
		 */
		String getReorderNumber() {
			return reorderNumber;
		}

		/**
		 * Gets the name of this product.
		 * @return Product name
		 */
		String getProductName() {
			return productName;
		}

		/**
		 * Gets an estimate of the printed with of the reorder number.
		 * @return Estimated width, pixels
		 */
		double calculateReorderNumberWidth() {
			return getReorderNumber().length() * AVG_CHAR_WIDTH;
		}

		/** Provides the reorder number for this product. */
		private String reorderNumber;

		/** Provides the name for this product. */
		private String productName;
	}

	/**
	 * Backs the out of stock items in the list.
	 */
	private ArrayList<LineItem> outofStockItems = new ArrayList<>();

	/**
	 * Backs the void items in the list.
	 */
	private ArrayList<LineItem> voidItems = new ArrayList<>();

	/**
	 * References all possible conditions, if we are displaying any.
	 */
	private SparseArray<SKUCondition> allConditions;

	/**
	 * Backs the SKU condition items.
	 */
	private SparseArray<ArrayList<LineItem>> skuConditionItems = new SparseArray<>();

	/**
	 * Provides the customer name for the receipt heading.
	 */
	private String customerName;

	/**
	 * Provides the store name for the receipt heading.
	 */
	private String storeName;

	/**
	 * Provides the audit time stamp for the receipt heading.
	 */
	private String auditStamp;

	/**
	 * References optional store notes or null if none.
	 */
	private String storeNotes;
}
