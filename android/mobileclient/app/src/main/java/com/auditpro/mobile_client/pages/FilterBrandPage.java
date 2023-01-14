/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.pages;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.auditpro.mobile_client.controls.CustomFontHelper;
import com.auditpro.mobile_client.controls.CustomTextView;
import com.auditpro.mobile_client.test.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


/**
 * Manages the page to display and update the brands filter for limiting the products on the
 * audit page.
 * @author Eric Ruck
 */
public class FilterBrandPage extends BasePage {

	/**
	 * Required empty public constructor.
	 */
	public FilterBrandPage() { }

	/**
	 * Creates a new instance of the brands filter page.
	 * @return New instance
	 */
	public static FilterBrandPage newInstance() {
		return new FilterBrandPage();
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
		return inflater.inflate(R.layout.fragment_filter_brand_page, container, false);
	}

	/**
	 * Initialize the controls in the created view.
	 * @param view Created view
	 * @param savedInstanceState Optional state
	 */
	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Hookup toggle
		view.findViewById(R.id.toggleAll).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onToggle();
			}
		});

		// Get specimin metrics
		ViewGroup speciminGroup = view.findViewById(R.id.specimen_group);
		TextView speciminText = view.findViewById(R.id.specimen_text);
		int marginTop = ((ViewGroup.MarginLayoutParams) speciminGroup.getLayoutParams()).topMargin;
		int paddingStart = speciminText.getPaddingStart();
		int textColor = speciminText.getCurrentTextColor();
		float textSize = speciminText.getTextSize();

		// Initialize the brand options
		allBrands = provider.getAllBrands();
		ViewGroup filterLayout = view.findViewById(R.id.filterLayout);
		filterLayout.removeAllViews();
		filteredBrands = null;
		if ((savedInstanceState != null) && savedInstanceState.containsKey(STATE_FILTERED_BRANDS)) {
			//noinspection ConstantConditions
			filteredBrands = new HashSet<>(savedInstanceState.getStringArrayList(STATE_FILTERED_BRANDS));
		} else {
			Set<String> initBrands = provider.getFilterBrands();
			if (initBrands != null) {
				filteredBrands = new HashSet<>(initBrands);
			}
		}
		if (filteredBrands == null) {
			filteredBrands = new HashSet<>(allBrands);
		}
		for (String brand : allBrands) {
			// Create the group for brand
			LinearLayout.LayoutParams parentParams = new LinearLayout.LayoutParams(
					ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			parentParams.setMargins(0, marginTop, 0, 0);
			LinearLayout parentLayout = new LinearLayout(getContext());
			parentLayout.setOrientation(LinearLayout.HORIZONTAL);
			parentLayout.setLayoutParams(parentParams);

			// Create the switch for the brand
			final String viewBrand = brand;
			Switch switchView = new Switch(getContext());
			switchView.setLayoutParams(new ViewGroup.LayoutParams(
					ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
			switchView.setChecked(filteredBrands.contains(viewBrand));
			switchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton view, boolean isChecked) {
					if (isChecked && !filteredBrands.contains(viewBrand)) {
						filteredBrands.add(viewBrand);
					} else {
						filteredBrands.remove(viewBrand);
					}
				}
			});
			parentLayout.addView(switchView);
			allSwitches.add(switchView);

			// Create the label for the brand
			CustomTextView labelView = new CustomTextView(getContext());
			labelView.setLayoutParams(new ViewGroup.LayoutParams(
					ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
			labelView.setText(viewBrand);
			labelView.setPadding(paddingStart, 0, 0, 0);
			CustomFontHelper.setCustomFont(labelView, "hero.otf", getContext());
			labelView.setTextColor(textColor);
			labelView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
				parentLayout.addView(labelView);

			// Add the parent to the filter layout
			filterLayout.addView(parentLayout);
		}
	}

	/**
	 * Saves our current state on demand.
	 * @param outState Receives state
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putStringArrayList(STATE_FILTERED_BRANDS, new ArrayList<>(filteredBrands));
	}

	/**
	 * Attaches the parent context to our page fragment.
	 * Parent context must implement our UpdateProductListener interface.
	 * @param context Parent context.
	 */
	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		if (context instanceof FilterStatusProvider) {
			provider = (FilterStatusProvider) context;
		} else {
			throw new RuntimeException(context.toString()
					+ " must implement FilterStatusProvider");
		}
	}

	/**
	 * Dereferences the filter provider on detach.
	 */
	@Override
	public void onDetach() {
		super.onDetach();
		provider = null;
	}

	/**
	 * Gets the filter page name to display.
	 * @param context Application context for string resource lookup
	 * @return Filter page name
	 */
	@Override
	public String getPageName(Context context) {
		return context.getString(R.string.message_brand_title);
	}

	/**
	 * Saves on back event.
	 * @return Allow default navigation
	 */
	@Override
	public boolean onBack() {
		saveFilterStatus();
		return false;
	}

	/**
	 * Saves the updated filter status back to the provider.
	 */
	private void saveFilterStatus() {
		// Update the provider
		if ((filteredBrands.size() == allBrands.size()) || (filteredBrands.size() == 0)) {
			provider.setFilterBrands(null);
		} else {
			provider.setFilterBrands(filteredBrands);
		}
	}

	/**
	 * Handles tap on the toggle button.
	 */
	private void onToggle() {
		boolean isChecked = filteredBrands.size() == 0;
		for (Switch current : allSwitches) {
			current.setChecked(isChecked);
		}
	}

	/** Identifies selected brands in saved state. */
	private static final String STATE_FILTERED_BRANDS = "filteredBrands";

	/** Provides the actual visual filter that we're editing. */
	private FilterStatusProvider provider;

	/** Provides all of the brands in the current audit. */
	private Set<String> allBrands;

	/** Provides the brands that are currently in the filter. */
	private Set<String> filteredBrands;

	/** References all of our switches for the toggle implementation. */
	private ArrayList<Switch> allSwitches = new ArrayList<>();
}
