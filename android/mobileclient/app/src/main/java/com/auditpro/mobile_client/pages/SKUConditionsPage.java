/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.pages;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.auditpro.mobile_client.MobileClientException;
import com.auditpro.mobile_client.controls.CustomFontHelper;
import com.auditpro.mobile_client.controls.CustomTextView;
import com.auditpro.mobile_client.database.AuditDatabase;
import com.auditpro.mobile_client.entities.Audit;
import com.auditpro.mobile_client.entities.SKUCondition;
import com.auditpro.mobile_client.security.Security;
import com.auditpro.mobile_client.test.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


/**
 * Manages the page to display and update the SKU conditions for a product in an audit.
 * @author Eric Ruck
 */
public class SKUConditionsPage extends BasePage {

	/** Identifies the audit id in the parameters bundle. */
	private static final String ARG_AUDIT = "audit";

	/** Identifies the product id in the parameters bundle. */
	private static final String ARG_PRODUCTID = "productId";

	/** Identifies our messages in the log. */
	private static final String STATE_SELECTED_CONDITIONS = "selectedConditions";

	/** Provides the ID of the audit to which these conditions apply. */
	private Audit audit;

	/** Provides the ID of the product to which these conditions apply. */
	private int productId;

	/** Provides the current selected conditions. */
	private Set<Integer> selectedConditions;


	/**
	 * Provides the required empty public constructor.
	 */
	public SKUConditionsPage() { }

	/**
	 * Creates a new instance to edit the SKU conditions for the passed product in the passed audit.
	 * @param audit Current audit in progress
	 * @param productId Identifies the product
	 * @return New instance
	 */
	public static SKUConditionsPage newInstance(Audit audit, int productId) {
		SKUConditionsPage fragment = new SKUConditionsPage();
		Bundle args = new Bundle();
		args.putParcelable(ARG_AUDIT, audit);
		args.putInt(ARG_PRODUCTID, productId);
		fragment.setArguments(args);
		return fragment;
	}

	/**
	 * Deserializes our arguments.
	 * @param savedInstanceState Saved state or null
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle args = getArguments();
		if (args != null) {
			audit = args.getParcelable(ARG_AUDIT);
			productId = args.getInt(ARG_PRODUCTID);
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
		return inflater.inflate(R.layout.fragment_skuconditions_page, container, false);
	}

	/**
	 * Initialize the controls in the created view.
	 * @param view Created view
	 * @param savedInstanceState Optional state
	 */
	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Get specimin metrics
		ViewGroup speciminGroup = view.findViewById(R.id.specimen_group);
		TextView speciminText = view.findViewById(R.id.specimen_text);
		int marginTop = ((ViewGroup.MarginLayoutParams) speciminGroup.getLayoutParams()).topMargin;
		int paddingStart = speciminText.getPaddingStart();
		int textColor = speciminText.getCurrentTextColor();
		float textSize = speciminText.getTextSize();

		// Get all the SKU options
		Security sec = new Security(getContext().getApplicationContext());
		SparseArray<SKUCondition> allConditions = sec.getSKUConditions();

		// Get the current settings
		if ((savedInstanceState != null) && (savedInstanceState.containsKey(STATE_SELECTED_CONDITIONS))) {
			//noinspection ConstantConditions
			selectedConditions = new HashSet<>(savedInstanceState.getIntegerArrayList(STATE_SELECTED_CONDITIONS));
		} else {
			try (AuditDatabase db = new AuditDatabase(getContext())) {
				selectedConditions = db.getSelectedSKUConditions(audit, productId);
			} catch (MobileClientException exc) {
				// Already logged
			}
		}
		if (selectedConditions == null) {
			// No selected conditions, use empty set
			selectedConditions = new HashSet<>();
		}

		// Initialize the brand options
		ViewGroup skuConditionsLayout = view.findViewById(R.id.skuConditionsLayout);
		skuConditionsLayout.removeAllViews();
		for (int index = 0; index < allConditions.size(); ++index) {
			// Process the current condition
			final SKUCondition condition = allConditions.valueAt(index);

			// Create the group for the condition
			LinearLayout.LayoutParams parentParams = new LinearLayout.LayoutParams(
					ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			parentParams.setMargins(0, marginTop, 0, 0);
			LinearLayout parentLayout = new LinearLayout(getContext());
			parentLayout.setOrientation(LinearLayout.HORIZONTAL);
			parentLayout.setLayoutParams(parentParams);

			// Create the switch for the condition
			final Integer conditionId = condition.getConditionId();
			Switch switchView = new Switch(getContext());
			switchView.setLayoutParams(new ViewGroup.LayoutParams(
					ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
			switchView.setChecked(selectedConditions.contains(conditionId));
			switchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton view, boolean isChecked) {
					if (isChecked) {
						selectedConditions.add(conditionId);
					} else {
						selectedConditions.remove(conditionId);
					}
				}
			});
			parentLayout.addView(switchView);

			// Create the label for the condition
			CustomTextView labelView = new CustomTextView(getContext());
			labelView.setLayoutParams(new ViewGroup.LayoutParams(
					ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
			labelView.setText(condition.getName());
			labelView.setPadding(paddingStart, 0, 0, 0);
			CustomFontHelper.setCustomFont(labelView, "hero.otf", getContext());
			labelView.setTextColor(textColor);
			labelView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
			parentLayout.addView(labelView);

			// Add the parent to the filter layout
			skuConditionsLayout.addView(parentLayout);
		}
	}

	/**
	 * Saves our current state on demand.
	 * @param outState Receives state
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putIntegerArrayList(STATE_SELECTED_CONDITIONS,
				new ArrayList<>(selectedConditions));
	}

	/**
	 * Gets the filter page name to display.
	 * @param context Application context for string resource lookup
	 * @return Filter page name
	 */
	@Override
	public String getPageName(Context context) {
		return context.getString(R.string.message_skuconditions_title);
	}

	/**
	 * Saves on back event.
	 * @return Allow default navigation
	 */
	@Override
	public boolean onBack() {
		saveSelectedConditions();
		return false;
	}

	/**
	 * Saves the selected conditions for the product in the audit to the database.
	 */
	private void saveSelectedConditions() {
		try (AuditDatabase db = new AuditDatabase(getContext())) {
			// Save the selections
			db.updateSelectedSKUConditions(audit, productId,
					(selectedConditions.size() == 0) ? null : selectedConditions);
		} catch (MobileClientException exc) {
			// Toast the error
			Toast.makeText(getContext(), exc.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}
}
