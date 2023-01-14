/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.pages;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.auditpro.mobile_client.entities.Product;
import com.auditpro.mobile_client.test.R;

import java.text.NumberFormat;


/**
 * Displays the product extended information.
 * @author Eric Ruck
 */
public class InfoProductPage extends BasePage {

	/**
	 * Required empty public constructor.
	 */
	public InfoProductPage() { }

	/**
	 * Creates a new page to display the passed product.
	 * @param product Product to display
	 * @return A new instance of fragment InfoProductPage.
	 */
	public static InfoProductPage newInstance(Product product) {
		InfoProductPage fragment = new InfoProductPage();
		Bundle args = new Bundle();
		args.putParcelable(ARG_PRODUCT, product);
		fragment.setArguments(args);
		return fragment;
	}

	/**
	 * Handles creation by unpacking our arguments.
	 * @param savedInstanceState Optional state
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			product = getArguments().getParcelable(ARG_PRODUCT);
		}
	}

	/**
	 * Creates our view.
	 * @param inflater Use to hydrate our layout
	 * @param container Parent view for page
	 * @param savedInstanceState Optional state
	 * @return Created view
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_info_product_page, container, false);
	}

	/**
	 * Initializes our view once created.
	 * @param view View to initialize
	 * @param savedInstanceState Optional state
	 */
	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Populate with passed product
		((TextView) view.findViewById(R.id.productNameText)).setText(product.getProductName());
		populate(view, R.id.brandNameText, product.getBrandName(), product.getBrandNameShort());
		populate(view, R.id.msrpText, product.getMSRP());
		populate(view, R.id.averageText, product.getRetailPriceAverage());
		populate(view, R.id.minText, product.getRetailPriceMin());
		populate(view, R.id.maxText, product.getRetailPriceMax());
		populate(view, R.id.weighedText, product.isRandomWeight());
		populate(view, R.id.upcText, product.getUPC(), null);
		populate(view, R.id.skuText, product.getBrandSKU(), null);
		populate(view, R.id.reorderCodeText, product.getCurrentReorderCode(), null);
		populate(view, R.id.categoryText, product.getCategoryName(), null);
		populate(view, R.id.subcategoryText, product.getSubcategoryName(), null);
		populate(view, R.id.productTypeText, product.getProductTypeName(), null);
	}

	/**
	 * Gets the title to display for this page.
	 * @param context Application context for string resource lookup
	 * @return Product info page title
	 */
	@Override
	public String getPageName(Context context) {
		return context.getString(R.string.message_info_title);
	}

	/**
	 * Populates the label with a product detail.
	 * @param view Parent view
	 * @param labelId Id of label to populate
	 * @param text Desired detail text
	 * @param altText Alternate detail text or null if none
	 */
	private void populate(View view, int labelId, String text, String altText) {
		TextView label = view.findViewById(labelId);
		if ((text != null) && !text.matches("^\\s*$")) {
			label.setText(text);
		} else if ((altText != null) && !altText.matches("^\\s*$")) {
			label.setText(altText);
		} else {
			label.setText(R.string.message_info_not_specified);
		}
	}

	/**
	 * Populates the label with a boolean value.
	 * @param view Parent view
	 * @param labelId Id of label to populate
	 * @param formatValue Value to format
	 */
	private void populate(View view, int labelId, boolean formatValue) {
		TextView label = view.findViewById(labelId);
		label.setText(formatValue ? R.string.button_yes : R.string.button_no);
	}

	/**
	 * Populates the label with a product detail in dollars.
	 * @param view Parent view
	 * @param labelId Id of label to populate
	 * @param dollarValue Dollars amount to display
	 */
	private void populate(View view, int labelId, Double dollarValue) {
		TextView label = view.findViewById(labelId);
		if (dollarValue == null) {
			label.setText(R.string.message_info_not_spec_short);
		} else {
			NumberFormat formatter = NumberFormat.getCurrencyInstance();
			label.setText(formatter.format(dollarValue));
		}
	}

	private static final String ARG_PRODUCT = "productArg";
	private Product product;
}
