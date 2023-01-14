/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.entities;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


/**
 * Manages the data for a receipt. This was the original receipt experiment, we're not currently
 * using it in the code but there are some interesting notes so I'm keeping it around for now.
 * Created by ericruck on 9/24/17.
 */

@SuppressWarnings("unused")
public class ReceiptTest {
	private String customerName;
	private String storeName;
	private String auditStamp;
	private ArrayList<LineItem> items = new ArrayList<>();

	public ReceiptTest(String customerName, String storeName, String auditStamp) {
		// Keep passed params
		this.customerName = customerName;
		this.storeName = storeName;
		if (auditStamp == null) {
			// Format now
			@SuppressLint("SimpleDateFormat")
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/YYYY HH:mm:ss");
			sdf.setTimeZone(TimeZone.getDefault());
			this.auditStamp = sdf.format(new Date());
		} else {
			// Keep passed time stamp
			this.auditStamp = auditStamp;
		}
	}

	public void addItem(String reorderNumber, String productName) {
		items.add(new LineItem(reorderNumber, productName));
	}

	public String formatZpl() {
		// Start by building the formatting for the items
		StringBuilder formatItems = new StringBuilder();
		int position = 100;
		for (LineItem item : items) {
			formatItems.append(String.format(
					Locale.getDefault(),
					"^FO25,%d^FD%s^FS^FO160,%d^FD%s^FS",
					position, item.getReorderNumber(),
					position, item.getProductName()));
			position += 22;
		}

		// Start the complete document
		@SuppressWarnings("StringBufferReplaceableByString")
		StringBuilder doc = new StringBuilder("! U1 setvar \"device.languages\" \"zpl\"\r\n ^XA^CFA,20");

		// Add the heading
		doc.append(String.format(Locale.getDefault(), "^LL%d", position + 100));
		doc.append(String.format(Locale.getDefault(), "^FO25,10^FB516,2,0,C,0^FD%s Reorder List For^FS", customerName));
		doc.append(String.format(Locale.getDefault(), "^FO25,50^FB516,2,0,C,0^FD%s %s^FS", storeName, auditStamp));

		// Add the items
		doc.append(formatItems);

		// Complete the document
		doc.append("^XZ");
		return doc.toString();
	}

// Specimin of output that more or less works...
//			String test = "! U1 setvar \"device.languages\" \"zpl\"\r\n ^XA^LL300^FO25,0^FB516,3,0,C,0^CFA,20" +
//					"^FDThis Should Be Centered Hopefully Across Two Lines If This Text Is Long Enough^FS" +
//					"^FO25,60^FD12345^FS" +
//					"^FO160,60^FDProduct Name^FS" +
//					"^XZ";

	private class LineItem {
		private String reorderNumber;
		private String productName;

		LineItem(String reorderNumber, String productName) {
			this.reorderNumber = reorderNumber;
			this.productName = productName;
		}

		String getReorderNumber() {
			return reorderNumber;
		}

		String getProductName() {
			return productName;
		}
	}
}
