/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.pages;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
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
import com.auditpro.mobile_client.entities.ReorderStatus;
import com.auditpro.mobile_client.test.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;


/**
 * Manages the page to display and update the reorder status filter for limiting the products
 * on the audit page.
 * @author Eric Ruck
 */
public class FilterStatusPage extends BasePage {

	/**
	 * Required empty public constructor.
	 */
	public FilterStatusPage() { }

	/**
	 * Creates a new page to adjust the product filter by status.
	 * @return A new instance of fragment FilterStatusPage
	 */
	public static FilterStatusPage newInstance() {
		return new FilterStatusPage();
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
		return inflater.inflate(R.layout.fragment_filter_status_page, container, false);
	}

	/**
	 * Initialize the controls in the created view.
	 * @param view Created view
	 * @param savedInstanceState Optional state
	 */
	@SuppressWarnings("ConstantConditions")
	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Initialize reorder status controls
		map = new Hashtable<>();
		Switch noneSwitch = view.findViewById(R.id.noneSwitch);
		TextView noneText = view.findViewById(R.id.noneText);
		map.put(ReorderStatus.NONE, noneSwitch);
		map.put(ReorderStatus.IN_STOCK, (Switch) view.findViewById(R.id.inStockSwitch));
		map.put(ReorderStatus.OUT_OF_STOCK, (Switch) view.findViewById(R.id.outOfStockSwitch));
		map.put(ReorderStatus.VOID, (Switch) view.findViewById(R.id.voidSwitch));
		if (savedInstanceState == null) {
			// Initialize from provider
			ReorderStatus[] current = provider.getFilterReorderStatus();
			if (current == null) {
				// Everything is in filter
				for (Enumeration<Switch> e = map.elements(); e.hasMoreElements(); ) {
					e.nextElement().setChecked(true);
				}
			} else {
				// Only select statuses in filter
				for (ReorderStatus status : provider.getFilterReorderStatus()) {
					map.get(status).setChecked(true);
				}
			}
		} else {
			// Initialize from state
			for (Enumeration<ReorderStatus> e = map.keys(); e.hasMoreElements(); ) {
				ReorderStatus status = e.nextElement();
				map.get(status).setChecked(savedInstanceState.getBoolean(status.getCode(), false));
			}
		}

		// Do we have a product type filter?
		allProductTypes = provider.getAllProductTypes();
		if (!hasProductTypeFilter()) {
			// No, hide the option
			view.findViewById(R.id.productTypeText).setVisibility(View.GONE);
		} else {
			// Initialize the options
			DisplayMetrics metrics = new DisplayMetrics();
			getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
			float logicalDensity = metrics.density;
			int parentLeft = (int) Math.ceil(30 * logicalDensity);
			int parentTop = (int) Math.ceil(10 * logicalDensity);
			ViewGroup filterLayout = view.findViewById(R.id.filterLayout);
			filteredProductTypes = null;
			if ((savedInstanceState != null) && savedInstanceState.containsKey(STATE_FILTERED_PRODUCT_TYPES)) {
				filteredProductTypes = savedInstanceState.getStringArrayList(STATE_FILTERED_PRODUCT_TYPES);
			} else {
				ArrayList<String> initProductTypes = provider.getFilterProductTypes();
				if (initProductTypes != null) {
					filteredProductTypes = new ArrayList<>(initProductTypes);
				}
			}
			if (filteredProductTypes == null) {
				filteredProductTypes = new ArrayList<>(allProductTypes);
			}
			for (String type : allProductTypes) {
				// Create group for type
				LinearLayout.LayoutParams parentParams = new LinearLayout.LayoutParams(
						ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
				parentParams.setMargins(parentLeft, parentTop, 0, 0);
				LinearLayout parentLayout = new LinearLayout(getContext());
				parentLayout.setOrientation(LinearLayout.HORIZONTAL);
				parentLayout.setLayoutParams(parentParams);

				// Create switch for type
				final String viewType = type;
				Switch switchView = new Switch(getContext());
				switchView.setLayoutParams(new ViewGroup.LayoutParams(
						ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
				switchView.setChecked(filteredProductTypes.contains(viewType));
				switchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton view, boolean isChecked) {
						if (isChecked && !filteredProductTypes.contains(viewType)) {
							filteredProductTypes.add(viewType);
						} else {
							filteredProductTypes.remove(viewType);
						}
					}
				});
				parentLayout.addView(switchView);

				// Create label for type
				CustomTextView labelView = new CustomTextView(getContext());
				labelView.setLayoutParams(new ViewGroup.LayoutParams(
						ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
				labelView.setText(viewType);
				labelView.setPadding(noneText.getPaddingStart(), 0, 0, 0);
				CustomFontHelper.setCustomFont(labelView, "hero.otf", getContext());
				labelView.setTextColor(noneText.getCurrentTextColor());
				labelView.setTextSize(TypedValue.COMPLEX_UNIT_PX, noneText.getTextSize());
				parentLayout.addView(labelView);

				// Add the parent to the filter layout
				filterLayout.addView(parentLayout);
			}
		}
	}

	/**
	 * Saves our current state on demand.
	 * @param outState Receives state
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		for (Enumeration<ReorderStatus> e = map.keys(); e.hasMoreElements(); ) {
			ReorderStatus status = e.nextElement();
			outState.putBoolean(status.getCode(), map.get(status).isChecked());
		}
		if (hasProductTypeFilter()) {
			outState.putStringArrayList(STATE_FILTERED_PRODUCT_TYPES, filteredProductTypes);
		}
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
		return context.getString(R.string.message_filter_title);
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
		// Build the list of selected reorder filters
		List<ReorderStatus> save = new ArrayList<>();
		for (Enumeration<ReorderStatus> e = map.keys(); e.hasMoreElements(); ) {
			ReorderStatus status = e.nextElement();
			if (map.get(status).isChecked()) {
				save.add(status);
			}
		}

		// Is there a list of product type filters?
		ArrayList<String> updateProductTypes = null;
		if (hasProductTypeFilter() && (filteredProductTypes.size() != 0) && (allProductTypes.size() != filteredProductTypes.size())) {
			// Yes, sort for easier processing by provider
			String[] sortedProductTypes = filteredProductTypes.toArray(new String[0]);
			Arrays.sort(sortedProductTypes);
			updateProductTypes = new ArrayList<>(Arrays.asList(sortedProductTypes));
		}

		// Update the provider
		provider.setFilterStatus(save.toArray(new ReorderStatus[] {}), updateProductTypes);
	}

	/**
	 * Should we show a product type filter?
	 * @return Show product type filter flag
	 */
	private boolean hasProductTypeFilter() {
		return (allProductTypes != null) && (allProductTypes.size() > 1);
	}

	/** Identifies our messages in the log. */
	private static final String STATE_FILTERED_PRODUCT_TYPES = "filteredProductTypes";

	/** Provides the filter for the user interface. */
	private FilterStatusProvider provider;

	/** Maps the switch controls to their reorder status. */
	private Dictionary<ReorderStatus, Switch> map;

	/** Provides all of the product types available in this audit. */
	private List<String> allProductTypes;

	/** Provides the current product types in the filter. */
	private ArrayList<String> filteredProductTypes;
}
