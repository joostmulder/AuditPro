/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.pages;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.auditpro.mobile_client.MobileClientException;
import com.auditpro.mobile_client.database.AuditDatabase;
import com.auditpro.mobile_client.entities.Audit;
import com.auditpro.mobile_client.entities.Product;
import com.auditpro.mobile_client.entities.ProductStatus;
import com.auditpro.mobile_client.entities.ReorderStatus;
import com.auditpro.mobile_client.entities.Report;
import com.auditpro.mobile_client.entities.Scan;
import com.auditpro.mobile_client.security.Security;
import com.auditpro.mobile_client.test.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;


/**
 * Displays the product details for updating.
 * @author Eric Ruck
 */
public class UpdateProductPage extends BasePage implements TextWatcher {

	/**
	 * Required empty public constructor.
	 */
	public UpdateProductPage() { }

	/**
	 * Creates a page to update a product in an audit.
	 * this fragment using the provided parameters.
	 *
	 * @param audit The audit being conducted
	 * @param productStatus The product being updated
	 * @param rawScan Raw scan data for product or null if manual
	 * @return A new instance of fragment UpdateProductPage.
	 */
	public static UpdateProductPage newInstance(Audit audit, ProductStatus productStatus, String rawScan) {
		return newInstance(audit, productStatus, rawScan, null);
	}

	/**
	 * Creates a page to update a product in an audit.
	 * this fragment using the provided parameters.
	 *
	 * @param audit The audit being conducted
	 * @param productStatus The product being updated
	 * @param rawScan Raw scan data for product or null if manual
	 * @param initReorderStatus initial reorder status for UI if different than product status or null
	 * @return A new instance of fragment UpdateProductPage.
	 */
	public static UpdateProductPage newInstance(Audit audit, ProductStatus productStatus, String rawScan, ReorderStatus initReorderStatus) {
		Bundle args = new Bundle();
		args.putParcelable(ARG_AUDIT, audit);
		args.putParcelable(ARG_PRODUCT_STATUS, productStatus);
		args.putString(ARG_RAW_SCAN, rawScan);
		if (initReorderStatus != null) {
			args.putInt(ARG_INIT_REORDER_STATUS, initReorderStatus.getId());
		}

		// Create requested fragment with bundled arguments
		UpdateProductPage fragment = new UpdateProductPage();
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
		Bundle args = getArguments();
		if (args != null) {
			audit = args.getParcelable(ARG_AUDIT);
			productStatus = args.getParcelable(ARG_PRODUCT_STATUS);
			rawScan = args.getString(ARG_RAW_SCAN);
			if (args.containsKey(ARG_INIT_REORDER_STATUS)) {
				initReorderStatus = ReorderStatus.fromId(args.getInt(ARG_INIT_REORDER_STATUS));
			}
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
		return inflater.inflate(R.layout.fragment_update_product_page, container, false);
	}

	/**
	 * Initializes our view once created.
	 * @param view View to initialize
	 * @param savedInstanceState Optional state
	 */
	@SuppressLint("DefaultLocale")
	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Set the product name
		((TextView) view.findViewById(R.id.productNameText)).setText(productStatus.getProductName());

		// Set the product details
		TextView detailText = view.findViewById(R.id.detailText);
		String reorderCode = productStatus.getProduct().getCurrentReorderCode();
		String details = ((reorderCode == null) || reorderCode.matches("^\\s*$"))
			? null
			: getString(R.string.message_updateprod_label_reorder_code) + " " + reorderCode;
		String sku = productStatus.getProduct().getBrandSKU();
		if ((sku != null) && !sku.matches("^\\s*$")) {
			// Format brand SKU
			details = (details == null ? "" : details + ", ") +
				getString(R.string.message_updateprod_label_sku) +  " " + sku;
		}
		if (details == null) {
			// Don't show the details label
			detailText.setVisibility(View.GONE);
		} else {
			// Update the details text
			detailText.setText(details);
		}

		// Attach buttons
		reorderStatusButton = view.findViewById(R.id.reorderStatusButton);
		view.findViewById(R.id.infoButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onInfo();
			}
		});
		reorderStatusButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onReorderStatus();
			}
		});
		view.findViewById(R.id.backButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onBack();
			}
		});
		view.findViewById(R.id.skuConditionsButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onSKUConditions();
			}
		});


		// Figure out initial values for the price edits
		Double retailPrice = null;
		Double salePrice = null;
		Integer reorderStatusId = null;
		try (AuditDatabase db = new AuditDatabase(getContext())){
			// Get the existing scan, if any
			updateScan = db.getScan(audit, productStatus.getProduct().getId());
			updateReport = db.getReport(audit, productStatus.getProduct().getId());
		} catch (MobileClientException exc) {
			// Failed to get existing scan
			Toast.makeText(getContext(), exc.getMessage(), Toast.LENGTH_SHORT).show();
		}
		if (savedInstanceState != null) {
			// Restore state
			retailPrice = (Double) savedInstanceState.get(STATE_RETAIL_PRICE);
			salePrice = (Double) savedInstanceState.get(STATE_SALE_PRICE);
			reorderStatusId = (Integer) savedInstanceState.get(STATE_REORDER_STATUS);
		} else {
			// Initialize from the database
			if (updateScan != null) {
				retailPrice = updateScan.getRetailPrice();
				salePrice = updateScan.getSalePrice();
			}
			if (updateReport != null) {
				reorderStatusId = updateReport.getReorderStatusId();
			}
		}

		// Get security options for product
		Security sec = new Security(getContext().getApplicationContext());
		isSKUConditionsEnabled = sec.getSKUConditions() != null;
		isAutoDecimalEnabled = sec.optSettingBool(Security.SETTING_ALLOW_SMART_SCAN, false);
		isAllowedToSetInStock = (rawScan != null) ||
				productStatus.getProduct().isRandomWeight() ||
				(productStatus.getReorderStatus() == ReorderStatus.IN_STOCK);
		if (!isAllowedToSetInStock) {
			// Check settings
			if (!sec.optSettingBool(Security.SETTING_IN_STOCK_REQUIRES_SCAN, false)) {
				isAllowedToSetInStock = true;
			}
		}
		updateSKUConditions();

		// Initialize the reorder status
		if (reorderStatusId == null) {
			oldReorderStatus = ReorderStatus.NONE;
		} else {
			oldReorderStatus = ReorderStatus.fromId(reorderStatusId);
			if (oldReorderStatus == null) {
				oldReorderStatus = ReorderStatus.NONE;
			}
		}
		ReorderStatus reorderStatus = oldReorderStatus;
		if (initReorderStatus != null) {
			// Use the passed initial reorder status
			reorderStatus = initReorderStatus;
		} else if ((rawScan != null) && sec.optSettingBool(Security.SETTING_SCAN_FORCES_IN_STOCK, false)) {
			// Scan sets status to in stock
			reorderStatus = ReorderStatus.IN_STOCK;
		} else if ((oldReorderStatus == ReorderStatus.NONE) && isAllowedToSetInStock) {
			// Default display to in stock
			oldReorderStatus = ReorderStatus.IN_STOCK;
			reorderStatus = ReorderStatus.IN_STOCK;
		}

		// Set the input controls
		retailPriceEdit = view.findViewById(R.id.retailPriceEdit);
		salePriceEdit = view.findViewById(R.id.salePriceEdit);
		retailPriceEdit.requestFocus();
		retailPriceEdit.setText((retailPrice == null) ? "" : String.format("%.2f", retailPrice));
		salePriceEdit.setText((salePrice == null) ? "" : String.format("%.2f", salePrice));
		//noinspection ConstantConditions
		reorderStatusButton.setText((reorderStatus == null)
				? ReorderStatus.IN_STOCK.getName()
				: reorderStatus.getName()
		);
		retailPriceEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
				if ((i == EditorInfo.IME_ACTION_UNSPECIFIED) && (keyEvent != null) &&
						(keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
					if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
						return true;
					}
					i = EditorInfo.IME_ACTION_NEXT;
				}
				if (i == EditorInfo.IME_ACTION_NEXT) {
					salePriceEdit.requestFocus();
					return true;
				}
				return false;
			}
		});
		salePriceEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
				if ((i == EditorInfo.IME_ACTION_UNSPECIFIED) && (keyEvent != null) &&
						(keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
					if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
						return true;
					}
					i = EditorInfo.IME_ACTION_NEXT;
				}
				if (i == EditorInfo.IME_ACTION_NEXT) {
					InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
					if (imm != null) {
						imm.hideSoftInputFromWindow(salePriceEdit.getWindowToken(), 0);
					}
					onReorderStatus();
					return true;
				}
				return false;
			}
		});
		if (sec.optSettingBool(Security.SETTING_AUTO_DECIMAL, true)) {
			retailPriceEdit.addTextChangedListener(this);
			salePriceEdit.addTextChangedListener(this);
		}
	}

	/**
	 * Required for TextWatcher implementation, event notifies that the watched text is about to
	 * change.
	 * @param s Initial text
	 * @param start Start of change
	 * @param count Count of original text to change
	 * @param after Count of change size
	 */
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

	/**
	 * Required for TextWatcher implementation, event notifies that the watched text has been
	 * changed.
	 * @param s Changed text
	 * @param start Start of change
	 * @param before Length of original text changed
	 * @param count Length of text after change
	 */
	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) { }

	/**
	 * Handles changed text in an editable by enforcing the auto decimal rules.
	 * @param s Changed editable
	 */
	@Override
	public void afterTextChanged(Editable s) {
		if (s.length() == 0) {
			return;
		}
		String current = s.toString();
		String adjust = current.replace(".", "");
		if (adjust.length() == 0) {
			adjust = "0.00";
		} else {
			double value = Double.parseDouble(adjust);
			adjust = String.format(Locale.getDefault(),"%1.2f", value / 100);
		}
		if (!adjust.equals(current)) {
			s.replace(0, s.length(), adjust);
		}
	}

	/**
	 * Saves our local state on demand.
	 * @param outState Receives local state
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Double retailPrice = validateInput(retailPriceEdit, null);
		Double salePrice = validateInput(salePriceEdit, null);
		ReorderStatus reorderStatus = ReorderStatus.fromName(reorderStatusButton.getText().toString());
		if ((retailPrice != null) && (retailPrice >= 0)) {
			outState.putDouble(STATE_RETAIL_PRICE, retailPrice);
		}
		if ((salePrice != null) && (salePrice >= 0)) {
			outState.putDouble(STATE_SALE_PRICE, salePrice);
		}
		if (reorderStatus != null) {
			outState.putInt(STATE_REORDER_STATUS, reorderStatus.getId());
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
		if (context instanceof UpdateProductListener) {
			mListener = (UpdateProductListener) context;
		} else {
			throw new RuntimeException(context.toString()
					+ " must implement UpdateProductListener");
		}
	}

	/**
	 * Dereferences the listener on detach.
	 */
	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	/**
	 * Returns our page name.
	 * @param context Application context for string resource lookup
	 * @return Display page name
	 */
	@Override
	public String getPageName(Context context) {
		return context.getString(R.string.message_updateprod_title);
	}

	/**
	 * Indicates that the page should shrink to make space for the keyboard, rather than overlap.
	 * @return Shrink flag
	 */
	@Override
	public boolean shrinkForKeyboard() {
		return true;
	}

	/**
	 * Saves on back navigation.
	 * @return Allow default back navigation flag
	 */
	@Override
	public boolean onBack() {
		saveProduct();
		return true;
	}

	/**
	 * Updates UI when the page appears.
	 */
	@Override
	public void onPageAppearing() {
		// Make sure the SKU conditions are displayed properly
		updateSKUConditions();

		// Focus the retail price
		retailPriceEdit.requestFocus();
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null) {
			imm.showSoftInput(retailPriceEdit, InputMethodManager.SHOW_IMPLICIT);
		}
	}

	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated
	 * to the activity and potentially other fragments contained in that
	 * activity.
	 */
	public interface UpdateProductListener {
		/**
		 * Handles update to the passed product.
		 * @param updated Updated product
		 * @param scan Updated (or added) product scan
		 */
		void onProductUpdated(ProductStatus updated, Scan scan);
	}

	/**
	 * Displays information about the product we're updating.
	 */
	private void onInfo() {
		showPage(InfoProductPage.newInstance(productStatus.getProduct()));
	}

	/**
	 * Gets a new reorder status from the user.
	 */
	private void onReorderStatus() {
		// Collect options to offer
		List<CharSequence> displayOptions = new ArrayList<>();
		final List<ReorderStatus> resultOptions = new ArrayList<>();
		for (ReorderStatus status : ReorderStatus.Statuses) {
			if (!status.isValid()) {
				continue;
			}
			if (!isAllowedToSetInStock && (status == ReorderStatus.IN_STOCK)) {
				continue;
			}
			displayOptions.add(status.getName());
			resultOptions.add(status);
		}

		// Offer the reorder options to the user
		new AlertDialog.Builder(getContext())
			.setTitle(R.string.message_reorder_status_message)
			.setItems(displayOptions.toArray(new CharSequence[0]), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					dialogInterface.dismiss();
					if ((i >= 0) && (i < resultOptions.size())) {
						reorderStatusButton.setText(resultOptions.get(i).getName());
					}
				}
			})
			.show();
	}

	/**
	 * Validates a dollar amount input.
	 * @param entry Edit text entry
	 * @param description For formatting error toast or null for quiet
	 * @return Valid input, null for empty, -1 for invalid
	 */
	private Double validateInput(EditText entry, String description) {
		// Is there a value?
		String text = entry.getText().toString();
		if (text.matches("^\\s*$")) {
			// Accept no/blank input
			return null;
		}

		// Validate input format
		if (text.matches("^\\d*.?\\d{1,2}?$")) {
			try {
				Double parsed = Double.valueOf(text);
				if (isAutoDecimalEnabled && text.matches("^\\d{2,}$")) {
					parsed /= 100;
				}
				return parsed;
			} catch (Exception exc) {
				return -1.;
			}
		}

		// Invalid
		if (description != null) {
			Toast.makeText(getContext(),
				getString(R.string.message_updateprod_enter_valid_for) + " " + description,
				Toast.LENGTH_SHORT).show();
		}
		return -1.;
	}

	/**
	 * Saves the product and navigates back to the list on success.
	 */
	void saveProduct() {
		// Parse inputs, starting with retail price
		final Double retailPriceValue = validateInput(retailPriceEdit, getString(R.string.message_updateprod_descr_retail_price));
		if ((retailPriceValue != null) && (retailPriceValue < 0)) {
			// Invalid retail price
			return;
		}

		// Parse the sale price
		final Double salePriceValue = validateInput(salePriceEdit, getString(R.string.message_updateprod_descr_sale_price));
		if ((salePriceValue != null) && (salePriceValue < 0)) {
			// Invalid sale price
			return;
		}

		// Get the reorder status
		final ReorderStatus statusValue = ReorderStatus.fromName(reorderStatusButton.getText().toString());
		if (statusValue == ReorderStatus.IN_STOCK) {
			// Do we have minimum or maximum prices?
			Product product = productStatus.getProduct();
			Double minValue = product.getInStockPriceMin();
			Double maxValue = product.getInStockPriceMax();
			if ((minValue == null) || (maxValue == null)) {
				Security sec = new Security(getContext().getApplicationContext());
				if (minValue == null) {
					minValue = sec.optSettingDouble(Security.SETTING_IN_STOCK_PRICE_MIN);
				}
				if (maxValue == null) {
					maxValue = sec.optSettingDouble(Security.SETTING_IN_STOCK_PRICE_MAX);
				}
			}

			// Do we have price ranges to check?
			String rangeMessage = null;
			NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.getDefault());
			if (minValue != null) {
				// Test minimum value
				if (((retailPriceValue != null) && (retailPriceValue < minValue)) ||
						((salePriceValue != null) && (salePriceValue < minValue))) {
					// One of the prices is less than the minimum
					rangeMessage = (maxValue == null)
							? getString(R.string.message_updateprod_in_stock_min, nf.format(minValue))
							: getString(R.string.message_updateprod_in_stock_range, nf.format(minValue), nf.format(maxValue));
				}
			}
			if ((rangeMessage == null) && (maxValue != null)) {
				// Test maximum value
				if (((retailPriceValue != null) && (retailPriceValue > maxValue)) ||
						((salePriceValue != null) && (salePriceValue > maxValue))) {
					// One of the prices is higher than the maximum
					rangeMessage = (minValue == null)
							? getString(R.string.message_updateprod_in_stock_max, nf.format(maxValue))
							: getString(R.string.message_updateprod_in_stock_range, nf.format(minValue), nf.format(maxValue));
				}
			}
			if (rangeMessage != null) {
				// Show confirmation
				new AlertDialog.Builder(getContext())
						.setMessage(rangeMessage)
						.setNegativeButton(R.string.button_go_back, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
							}
						})
						.setPositiveButton(R.string.button_save_anyway, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								updateDatabase(retailPriceValue, salePriceValue, statusValue);
							}
						})
						.show();
				return;
			}
		}

		// Update the database
		updateDatabase(retailPriceValue, salePriceValue, statusValue);
	}

	/**
	 * Saves updates to the database, and exits this screen.
	 * @param retailPriceValue Retail prices to save
	 * @param salePriceValue Sale price to save
	 * @param statusValue Reorder status to save
	 */
	void updateDatabase(Double retailPriceValue, Double salePriceValue, ReorderStatus statusValue) {
		try (AuditDatabase db = new AuditDatabase(getContext())) {
			// Do we have an existing scan?
			if (updateScan == null) {
				// No, save new scan
				Scan applyScan = new Scan(audit, productStatus.getProduct(), rawScan,
						retailPriceValue, salePriceValue);
				db.addScan(applyScan);
				updateScan = applyScan;
			} else {
				// Has the existing scan changed?
				Scan applyScan = updateScan.
						createRescan(rawScan, retailPriceValue, salePriceValue);
				if (applyScan != null) {
					db.updateScan(applyScan);
					updateScan = applyScan;
				}
			}
			if (statusValue != null) {
				// Do we have an update to the reorder status?
				if (updateReport == null) {
					// Only save the default when there's a change
					if ((retailPriceValue != null) || (salePriceValue != null) || (statusValue != oldReorderStatus)) {
						// Insert new report
						Report applyReport = new Report(updateScan, productStatus.getProduct(), statusValue.getId());
						db.addReport(applyReport);
						updateReport = applyReport;
						productStatus.setReorderStatus(applyReport, updateScan);
					}
				} else {
					// Did the report change?
					int origStatusId = updateReport.getReorderStatusId();
					if (statusValue.getId() != origStatusId) {
						// Update existing report
						Report applyReport = new Report(updateReport, updateScan, statusValue.getId());
						db.updateReport(applyReport);
						updateReport = applyReport;
						productStatus.setReorderStatus(applyReport, updateScan);
					}
				}
			}

			// Make sure the keyboard is dismissed
			InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			if (imm != null) {
				imm.hideSoftInputFromWindow(retailPriceEdit.getWindowToken(), 0);
			}
			mListener.onProductUpdated(productStatus, updateScan);
			IPageParent parent = getParent();
			if (parent != null) {
				parent.popPage();
			}
		} catch (MobileClientException exc) {
			// Show the error
			Toast.makeText(getContext(), exc.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Updates the SKU condition button for the current state.
	 */
	@SuppressWarnings("ConstantConditions")
	private void updateSKUConditions() {
		Button button = getView().findViewById(R.id.skuConditionsButton);
		if (!isSKUConditionsEnabled) {
			// Hide for disabled option
			button.setVisibility(View.GONE);
		} else {
			// Update button for current state
			int selectionCount = 0;
			try (AuditDatabase db = new AuditDatabase(getContext())) {
				Set<Integer> selections = db.getSelectedSKUConditions(audit, productStatus.getProduct().getId());
				selectionCount = (selections == null) ? 0 : selections.size();
			} catch (MobileClientException exc) {
				// Already logged
			}

			// Format the button text
			button.setText(getString(R.string.button_updateprod_sku_conditions, selectionCount));
		}
	}

	/**
	 * Displays the SKU conditions page.
	 */
	private void onSKUConditions() {
		showPage(SKUConditionsPage.newInstance(audit, productStatus.getProduct().getId()));
	}

	private static final String ARG_AUDIT = "auditArg";
	private static final String ARG_PRODUCT_STATUS = "productStatusArg";
	private static final String ARG_RAW_SCAN = "rawScanArg";
	private static final String ARG_INIT_REORDER_STATUS = "initReorderStatus";

	private static final String STATE_RETAIL_PRICE = "retailPrice";
	private static final String STATE_SALE_PRICE = "salePrice";
	private static final String STATE_REORDER_STATUS = "reorderStatus";

	private UpdateProductListener mListener;
	private Audit audit;
	private ProductStatus productStatus;
	private String rawScan;

	private Scan updateScan;
	private Report updateReport;
	private boolean isSKUConditionsEnabled;
	private boolean isAllowedToSetInStock;
	private boolean isAutoDecimalEnabled;
	private ReorderStatus initReorderStatus;
	private ReorderStatus oldReorderStatus;

	private EditText retailPriceEdit;
	private EditText salePriceEdit;
	private Button reorderStatusButton;
}
