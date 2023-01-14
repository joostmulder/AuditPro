/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.pages;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.auditpro.mobile_client.Analytics;
import com.auditpro.mobile_client.MobileClientException;
import com.auditpro.mobile_client.controls.BatteryIcon;
import com.auditpro.mobile_client.database.AuditDatabase;
import com.auditpro.mobile_client.database.StoresDatabase;
import com.auditpro.mobile_client.dialog.BeginAuditDialog;
import com.auditpro.mobile_client.entities.Audit;
import com.auditpro.mobile_client.entities.Product;
import com.auditpro.mobile_client.entities.ProductStatus;
import com.auditpro.mobile_client.entities.ReorderStatus;
import com.auditpro.mobile_client.entities.Report;
import com.auditpro.mobile_client.entities.Scan;
import com.auditpro.mobile_client.entities.Store;
import com.auditpro.mobile_client.security.Security;
import com.auditpro.mobile_client.test.R;

import java.lang.ref.WeakReference;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import koamtac.kdc.sdk.KDCBarcodeDataReceivedListener;
import koamtac.kdc.sdk.KDCConnectionListener;
import koamtac.kdc.sdk.KDCConstants;
import koamtac.kdc.sdk.KDCData;
import koamtac.kdc.sdk.KDCReader;


/**
 * Manages the page to select products during an audit.
 * @author Eric Ruck
 */
public class SelectProductPage extends BasePage implements
		FilterStatusProvider,
		UpdateProductPage.UpdateProductListener,
		KDCBarcodeDataReceivedListener,
		KDCConnectionListener {

	/**
	 * Required empty public constructor.
	 */
	public SelectProductPage() { }

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @param audit Audit in progress
	 * @return A new instance of fragment SelectProductPage.
	 */
	public static SelectProductPage newInstance(Audit audit) {
		SelectProductPage fragment = new SelectProductPage();
		Bundle args = new Bundle();
		args.putParcelable(ARG_AUDIT, audit);
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
			audit = getArguments().getParcelable(ARG_AUDIT);
		}
	}

	/**
	 * Creates a view for this new page fragment.
	 * @param inflater View inflater to use
	 * @param container Parent container
	 * @param savedInstanceState Optional state
	 * @return Created view
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_select_product_page, container, false);
	}

	/**
	 * Initializes the view once it's been created
	 * @param view View to initialize
	 * @param savedInstanceState Optional state
	 */
	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Wire up our controls
		filterButton = view.findViewById(R.id.filterButton);
		brandsButton = view.findViewById(R.id.brandsButton);
		searchEdit = view.findViewById(R.id.searchEdit);
		searchButton = view.findViewById(R.id.searchButton);
		view.findViewById(R.id.notesButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onNotes();
			}
		});
		view.findViewById(R.id.completeButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onComplete();
			}
		});
		filterButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onFilter();
			}
		});
		searchButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onSearchIcon();
			}
		});

		// Setup the search filter
		searchEdit.addTextChangedListener(new TextWatcher() {
			@Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
			@Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
			@Override public void afterTextChanged(Editable editable) {
				applyFilter(false);
			}
		});
		if (savedInstanceState != null) {
			// Restore from state
			String filterSearch = savedInstanceState.getString(STATE_FILTER_SEARCH);
			int filterReorderStatusIds[] = savedInstanceState.getIntArray(STATE_FILTER_REORDER_STATUS);
			searchEdit.setText((filterSearch == null) ? "" : filterSearch);
			filterProductTypes = savedInstanceState.getStringArrayList(STATE_FILTER_PRODUCT_TYPES);
			if ((filterReorderStatusIds == null) || (filterReorderStatusIds.length == 0)) {
				filterReorderStatus = null;
			} else {
				filterReorderStatus = new ReorderStatus[filterReorderStatusIds.length];
				for (int index = 0; index < filterReorderStatusIds.length; ++index) {
					filterReorderStatus[index] = ReorderStatus.fromId(filterReorderStatusIds[index]);
				}
			}
		} else {
			// Clear filter initially
			searchEdit.setText("");
			filterReorderStatus = null;
			filterProductTypes = null;
		}
	}

	/**
	 * Ensures products are loaded on a worker thread once the UI is fully initialized.
	 */
	@Override
	public void onStart() {
		super.onStart();
		if ((allProducts == null) || (allProducts.size() == 0)) {
			// Load the products et al on a worker thread
			new LoadProducts(this).execute();
		}
	}

	/**
	 * Loads the products and related data on a worker thread.
	 */
	private static class LoadProducts extends AsyncTask<Void, Void, String> {

		private WeakReference<SelectProductPage> pageRef;
		private Audit audit;
		private List<ProductStatus> loadedProducts;
		private List<String> loadedProductTypes;
		private SortedSet<String> loadedBrands;
		private boolean unscannedShowing = true;

		/**
		 * Initializes to display the results on the passed page.
		 * @param page Page receives results
		 */
		LoadProducts(SelectProductPage page) {
			pageRef = new WeakReference<>(page);
			audit = page.audit;
		}

		/**
		 * Safely gets the context from the weak reference if we can.
		 * @return Context or null
		 */
		private Context getContext() {
			SelectProductPage page = pageRef.get();
			return (page == null) ? null : page.getContext().getApplicationContext();
		}

		/**
		 * Executes the load on the worker thread.
		 * @param voids Ignored
		 * @return Ignored
		 */
		@Override
		protected String doInBackground(Void... voids) {
			// Get the context
			Context context = getContext();
			if (context == null) {
				// Page is already gone
				return null;
			}

			// Load product data
			try (StoresDatabase db = new StoresDatabase(context)) {
				// Cache the list of products
				Product[] loaded = db.
						getProductsForStore(audit.getStoreId()).toArray(new Product[0]);
				Arrays.sort(loaded, new Comparator<Product>() {
					@Override
					public int compare(Product product, Product t1) {
						return product.getProductName().compareTo(t1.getProductName());
					}
				});

				// Convert to product status
				Set<String> uniqueTypes = new HashSet<>();
				loadedProducts = new ArrayList<>();
				loadedBrands = new TreeSet<>(new Comparator<String>() {
					@Override
					public int compare(String o1, String o2) {
						return o1.compareTo(o2);
					}
				});
				for(Product product : loaded) {
					// Add the current product to all products and types
					loadedProducts.add(new ProductStatus(product));
					uniqueTypes.add(product.getProductTypeName());

					// Do we have a brand name?
					String brandName = product.getDisplayBrandName();
					if (brandName != null) {
						loadedBrands.add(brandName);
					}
				}

				// Apply any existing reports to the product status
				try (AuditDatabase adb = new AuditDatabase(context)) {
					for (Report report : adb.getAllReports(audit, null)) {
						for (ProductStatus ps : loadedProducts) {
							if (ps.getProduct().getId() == report.getProductId()) {
								Scan scan =
										(report.getReorderStatusId() == ReorderStatus.IN_STOCK.getId()) ||
												(report.getReorderStatusId() == ReorderStatus.OUT_OF_STOCK.getId())
												? adb.getScan(audit, ps.getProduct().getId())
												: null;
								ps.setReorderStatus(report, scan);
								break;
							}
						}
					}
				}

				// Setup product types
				String[] typesArray = uniqueTypes.toArray(new String[0]);
				Arrays.sort(typesArray);
				loadedProductTypes = new ArrayList<>(Arrays.asList(typesArray));

				// Check for any unscanned products
				// Are there any unscanned products?
				unscannedShowing = false;
				for (ProductStatus test : loadedProducts) {
					// Is the current product unscanned?
					if (test.getReorderStatus() == ReorderStatus.NONE) {
						// Found
						unscannedShowing = true;
						break;
					}
				}

				// Report success
				return null;
			} catch (MobileClientException exc) {
				return exc.getMessage();
			}
		}

		/**
		 * Displays the results on the main thread.
		 * @param message Error message or null on success
		 */
		@Override
		protected void onPostExecute(String message) {
			super.onPostExecute(message);

			// Is the page still around?
			SelectProductPage page = pageRef.get();
			if (page == null) {
				// Nothing to do
				return;
			}

			// Did we succeed?
			if (message != null) {
				// No, show toast
				Toast.makeText(page.getContext(), message, Toast.LENGTH_SHORT).show();
				return;
			}

			// Update the user interface
			page.completeLoad(loadedProducts, loadedProductTypes, loadedBrands, unscannedShowing);
		}
	}

	/**
	 * Displays products once the load is completed.
	 * @param loadedProducts Products loaded from the database
	 * @param loadedProductTypes Distinct product types identified
	 * @param loadedBrands Distinct brands identified
	 * @param unscannedShowing Flags one or more unscanned product
	 */
	@SuppressWarnings("ConstantConditions")
	void completeLoad(List<ProductStatus> loadedProducts, List<String> loadedProductTypes,
					  SortedSet<String> loadedBrands, boolean unscannedShowing) {
		// Make sure the user didn't dismiss while we were loading
		View view = getView();
		if (view == null) {
			// Dismissed
			return;
		}

		// Keep the passed loaded data
		allProducts = loadedProducts;
		allProductTypes = loadedProductTypes;
		allBrands = loadedBrands;

		// Update the user interface
		ListView productStatusList = view.findViewById(R.id.productList);
		productStatusList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int position, long rowId) {
				onSelectProduct(productStatusAdapter.getItem(position), (String) null);
			}
		});
		productStatusAdapter = new ArrayAdapter<ProductStatus>(getContext(), R.layout.list_item_product, R.id.productText) {
			@NonNull @Override
			public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
				// Default view creation
				View res = super.getView(position, convertView, parent);

				// Determine the initial reorder status by looking up the product
				TextView productView = res.findViewById(R.id.productText);
				String productName = productView.getText().toString();
				String statusName = ReorderStatus.NONE.getCode();
				for (ProductStatus productStatus : allProducts) {
					if (productStatus.getProductName().equals(productName)) {
						ReorderStatus showStatus = productStatus.getReorderStatus();
						statusName = showStatus.getCode();
						if (((showStatus == ReorderStatus.IN_STOCK) || (showStatus == ReorderStatus.OUT_OF_STOCK))
								&& productStatus.hasDisplayPrice()) {
							// Append display price
							statusName += String.format(" %s",
									NumberFormat.getCurrencyInstance().format(productStatus.getDisplayPrice()));
						}
						break;
					}
				}
				// Set the reorder button
				TextView reorderButton = res.findViewById(R.id.itemReorderStatusButton);
				reorderButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						onItemReorderStatus(view);
					}
				});
				reorderButton.setText(statusName);

				// Return the initialized view
				return res;
			}
		};
		productStatusAdapter.sort(new Comparator<ProductStatus>() {
			@Override
			public int compare(ProductStatus t0, ProductStatus t1) {
				return t0.getProductName().compareTo(t1.getProductName());
			}
		});
		productStatusList.setAdapter(productStatusAdapter);

		// Can we show the brands?
		if (allBrands.size() > 1) {
			// Handle brands button
			brandsButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					onBrands();
				}
			});
		} else {
			// Not enough brands to select
			brandsButton.setVisibility(View.GONE);
		}

		// Are there unscanned products?
		if (!unscannedShowing && (unscannedMenuItem != null)) {
			// Hide unscanned
			unscannedMenuItem.setVisible(false);
		}

		// Update UI for current filters
		applyFilter(true);
	}

	/**
	 * Saves the page state on demand
	 * @param outState Receives state to save
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(STATE_FILTER_SEARCH, searchEdit.getText().toString());
		outState.putStringArrayList(STATE_FILTER_PRODUCT_TYPES, filterProductTypes);
		if ((filterReorderStatus != null) && (filterReorderStatus.length > 0)) {
			int filterReorderStatusIds[] = new int[filterReorderStatus.length];
			for (int index = 0; index < filterReorderStatus.length; ++index) {
				filterReorderStatusIds[index] = filterReorderStatus[index].getId();
			}
			outState.putIntArray(STATE_FILTER_REORDER_STATUS, filterReorderStatusIds);
		}
	}

	/**
	 * Handles stop event by disconnecting the barcode reader.
	 */
	@Override
	public void onStop() {
		super.onStop();
		isInModal = false;
		disconnectBarcode();
	}

	/**
	 * Returns our page display name.
	 * @param context Application context for string resource lookup
	 * @return Page display name
	 */
	@Override
	public String getPageName(Context context) {
		return context.getResources().getString(R.string.message_prods_title);
	}

	/**
	 * Indicates that our view should resize around the keyboard.
	 * @return Resize flag
	 */
	@Override
	public boolean shrinkForKeyboard() {
		return true;
	}

	/**
	 * Handles page appearing by checking the barcode scanner connection.
	 */
	@Override
	public void onPageAppearing() {
		super.onPageAppearing();
		connectBarcode();
	}

	/**
	 * Overrides back button to provide confirmation.
	 * @return Back handled flag
	 */
	@Override
	public boolean onBack() {
		// Display the alert asynchronously
		isInModal = true;
		new AlertDialog.Builder(getContext())
			.setTitle(R.string.message_prods_exit_title)
			.setMessage(R.string.message_prods_exit_message)
			.setNegativeButton(R.string.button_no, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					isInModal = false;
					dialogInterface.dismiss();
				}
			})
			.setPositiveButton(R.string.button_yes, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					isInModal = false;
					dialogInterface.dismiss();
					IPageParent parent = getParent();
					if (parent != null) {
						parent.popPage();
					}
				}
			})
			.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialogInterface) {
					isInModal = false;
					dialogInterface.dismiss();
				}
			})
			.show();

		// Don't go back yet
		return true;
	}

	/**
	 * Displays our menu when we have unscanned items.
	 * @param inflater Inflater to use on menu resource
	 * @param menu Parent receives menu items
	 * @return Handled flag
	 */
	@Override
	public boolean onCreateMenu(MenuInflater inflater, Menu menu) {
		// Show the menu
		inflater.inflate(R.menu.menu_main, menu);
		barcodeMenuItem = menu.findItem(R.id.action_barcode_status);
		setBarcodeState(BarcodeState.UNCHANGED);
		unscannedMenuItem = menu.findItem(R.id.action_set_unscanned);
		MenuItem batteryItem = menu.findItem(R.id.action_barcode_battery);
		if (battery == null) {
			// Create new battery control
			battery = new BatteryIcon(batteryItem, getString(R.string.battery_device_scanner));
		} else {
			// Attach to new menu item
			battery.attach(batteryItem);
		}

		// Let the caller know we've created a menu
		return true;
	}

	/**
	 * Handles our menu items when they are selected
	 * @param item Item selected
	 * @return Handled flag
	 */
	@Override
	public boolean onMenuItem(MenuItem item) {
		// Are we interested in this item?
		int id = item.getItemId();
		if (id == R.id.action_set_unscanned) {
			// Offer set unscanned options
			onSetUnscanned();
			return true;
		}
		if (id == R.id.action_barcode_status) {
			// Barcode status
			Analytics.menuAction("Barcode Status",
				(barcodeState == BarcodeState.DISCONNECTED) ? "connect" : "nop");
			if (barcodeState == BarcodeState.DISCONNECTED) {
				// Attempt to connect now
				connectBarcode();
			}
			return true;
		}
		if ((id == R.id.action_barcode_battery) && (battery != null)) {
			// Show battery details
			Analytics.menuAction("Barcode Battery", battery.describeState(getContext()));
			Toast.makeText(getContext(), battery.describeState(getContext()), Toast.LENGTH_SHORT).show();
			return true;
		}
		if (id == R.id.action_store_history) {
			// Show the store history
			Analytics.menuAction("Store History");
			try (StoresDatabase db = new StoresDatabase(getContext())) {
				Store store = db.getStore(audit.getStoreId());
				if (store == null) {
					// Failed to get the store
					Toast.makeText(getContext(), R.string.emssage_prods_store_not_found, Toast.LENGTH_SHORT).show();
				} else {
					// Show the store history
					DialogFragment dialog = BeginAuditDialog.newInstance(store, true);
					dialog.show(getFragmentManager(), "Review Store Dialog");
				}
			} catch (MobileClientException exc) {
				// Show error
				Toast.makeText(getContext(), exc.getMessage(), Toast.LENGTH_SHORT).show();
			}
			return true;
		}

		// Not handled
 		return false;
	}

	/**
	 * Gets the current filter on reorder status.
	 * @return Current filter on reorder status
	 */
	@Override
	public ReorderStatus[] getFilterReorderStatus() {
		return filterReorderStatus;
	}

	/**
	 * Gets the current filter on product types.
	 * @return Current filter on product types
	 */
	@Override
	public ArrayList<String> getFilterProductTypes() {
		return filterProductTypes;
	}

	/**
	 * Gets all of the possible product types.
	 * @return All possible product types
	 */
	@Override
	public List<String> getAllProductTypes() {
		return allProductTypes;
	}

	/**
	 * Sets the current filter status.
	 * @param reorderStatuses New filter status
	 */
	@Override
	public void setFilterStatus(ReorderStatus[] reorderStatuses, ArrayList<String> productTypes) {
		// Check for same as null, no filter
		ReorderStatus[] updateReorderStatuses =
			((reorderStatuses == null) || (reorderStatuses.length == 0) || (reorderStatuses.length == ReorderStatus.Statuses.length))
				? null : reorderStatuses;
		boolean isSame = ((updateReorderStatuses == null) && (filterReorderStatus == null)) ||
				((updateReorderStatuses != null) && (filterReorderStatus != null) && (updateReorderStatuses.length == filterReorderStatus.length));
		if (isSame && (updateReorderStatuses != null)) {
			// Might be different, test
			for (int index = 0; isSame && (index < updateReorderStatuses.length); ++index) {
				ReorderStatus status = updateReorderStatuses[index];
				isSame = false;
				for (ReorderStatus test : filterReorderStatus) {
					if (test == status) {
						isSame = true;
						break;
					}
				}
			}
		}
		if (isSame) {
			// Check product types
			if ((productTypes != null) && (filterProductTypes != null)) {
				if (productTypes.size() != filterProductTypes.size()) {
					// Different filter count
					isSame = false;
				} else {
					// Check product types contents
					for (int index = 0; index < productTypes.size(); ++index) {
						if (!productTypes.get(index).equals(filterProductTypes.get(index))) {
							isSame = false;
							break;
						}
					}
				}
			} else if ((productTypes != null) || (filterProductTypes != null)) {
				// One but not both is null;
				isSame = false;
			}
		}

		if (!isSame) {
			// Update the filter
			filterReorderStatus = updateReorderStatuses;
			filterProductTypes = productTypes;
			applyFilter(false);
		}
	}

	/**
	 * Gets the current filter in product brands.
	 * @return Current filter on product brands
	 */
	@Override
	public Set<String> getFilterBrands() {
		return filterBrands;
	}

	/**
	 * Gets all of the available product brands.
	 * @return All product brands
	 */
	@Override
	public 	Set<String> getAllBrands() {
		return allBrands;
	}

	/**
	 * Sets the current brands filter.
	 * @param value New list of products in filter
	 */
	@Override
	public 	void setFilterBrands(Set<String> value) {
		// Has the filter changed?
		if (Objects.equals(filterBrands, value)) {
			// No change
			return;
		}

		// Update the filter
		filterBrands = value;
		applyFilter(false);
	}

	/**
	 * Handles update to the passed product.
	 * @param updated Updated product
	 * @param scan Updated (or added) product scan
	 */
	@Override
	public void onProductUpdated(ProductStatus updated, Scan scan) {
		// Check to make sure the product is still in the filter
		if (filterReorderStatus != null) {
			boolean isInFilter = false;
			for (ReorderStatus test : filterReorderStatus) {
				if (test == updated.getReorderStatus()) {
					isInFilter = true;
					break;
				}
			}
			if (!isInFilter) {
				// The product is now outside the filter, remove
				productStatusAdapter.remove(updated);
				return;
			}
		}

		// Make sure the status display is up to date
		// Note: because of parceling the updated product will be a different instance than the
		//   product in the the list adapter.
		int pos = productStatusAdapter.getPosition(updated);
		ProductStatus actual = ((pos >= 0) && (pos < productStatusAdapter.getCount()))
			? productStatusAdapter.getItem(pos)
			: null;
		if (actual != null) {
			actual.updateFrom(updated, scan);
			productStatusAdapter.notifyDataSetChanged();
		}
	}

	/**
	 * Handles change in connection state with the scanner
	 * @param bluetoothDevice Identifies which KDC device prompted the change of connection state
	 * @param state Current state of the KDC device (KDCConstants.CONNECTION_STATE_*)
	 */
	@Override
	public void ConnectionChanged(BluetoothDevice bluetoothDevice, int state) {
		Activity activity = getActivity();
		if (activity == null) {
			Log.w(LOG_TAG, "Connection changed activity disconnected");
			return;
		}
		switch (state) {
			case KDCConstants.CONNECTION_STATE_CONNECTED:
				// Update connected status
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						setBarcodeState(BarcodeState.CONNECTED);
					}
				});
				break;
			case KDCConstants.CONNECTION_STATE_FAILED:
			case KDCConstants.CONNECTION_STATE_LOST:
			case KDCConstants.CONNECTION_STATE_NONE:
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						setBarcodeState(BarcodeState.DISCONNECTED);
					}
				});
				break;
		}
	}

	/**
	 * Handles receipt of barcode from scanner.
	 * @param kdcData Received barcode
	 */
	@Override
	public void BarcodeDataReceived(KDCData kdcData) {
		IPageParent parent = getParent();
		if (isInModal || (parent == null) || !parent.isTopPage(this)) {
			// Ignore
			return;
		}

		// Process the barcode on the UI thread
		final String barcode = kdcData.GetData();
		getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if ((battery != null) && (barcodeReader != null)) {
					// Update the battery state
					battery.setValue(barcodeReader.GetBatteryLevel());
				}
				try {
					// Find the product
					for (ProductStatus productStatus : allProducts) {
						if (productStatus.getProduct().getUPC().equals(barcode)) {
							// Found, show the update screen
							onSelectProduct(productStatus, barcode);
							return;
						}
					}

					// Product not found
					String message = getString(R.string.message_prods_barcode_not_found, barcode);
					Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
				} catch (IllegalStateException exc) {
					Log.w(LOG_TAG, "Application likely lost focus after scan received", exc);
				}
			}
		});
	}

	/**
	 * Attempts to connect to the barcode reader.
	 */
	private void connectBarcode() {
		if ((barcodeState == BarcodeState.CONNECTING) || (barcodeState == BarcodeState.CONNECTED)) {
			// Already connected, more or less
			return;
		}

		// Are we paired with a reader?
		if (KDCReader.GetAvailableDeviceList().size() == 0) {
			// No barcode reader paired
			setBarcodeState(BarcodeState.NONE);
			return;
		}

		// Attempt to connect now
		disconnectBarcode();
		setBarcodeState(BarcodeState.CONNECTING);
		new Thread(new Runnable() {
			// Note that the non-deprecated version has trouble connecting to new scanners,
			// TODO: Follow up with Koamtac on that
			@SuppressWarnings("deprecation")
			@Override
			public void run() {
				// Begin the connection
				barcodeReader = new KDCReader(
						null,
						null,
						SelectProductPage.this,
						null,
						null,
						null,
						SelectProductPage.this,
						false);

				try {
					// Record/initialize timeouts
					int initScanTimeout = barcodeReader.GetScanTimeout();
					KDCConstants.SleepTimeout initSleepTimeout = barcodeReader.GetSleepTimeout();
					Log.i(LOG_TAG, String.format("Koamtac initial scan timeout: %d, sleep timeout: %s",
							initScanTimeout, initSleepTimeout.toString()));
					barcodeReader.SetSleepTimeout(KDCConstants.SleepTimeout.DISABLED);
				} catch (Exception exc) {
					Log.w(LOG_TAG, "Error initializing scanner, probably exiting page before init", exc);
				}
			}
		}).start();
	}

	/**
	 * Disconnects from the barcode reader, if we're connected.
	 */
	private void disconnectBarcode() {
		setBarcodeState(BarcodeState.DISCONNECTED);
		if (barcodeReader != null) {
			barcodeReader.Disconnect();
			barcodeReader.Dispose();
			barcodeReader = null;
		}
	}

	/**
	 * Displays notes editor on user request.
	 */
	private void onNotes() {
		showPage(NotesPage.newInstance(audit));
	}

	/**
	 * Displays filter editor on user request.
	 */
	private void onFilter() {
		showPage(FilterStatusPage.newInstance());
	}

	/**
	 * Displays the brands editor on user request.
	 */
	private void onBrands() {
		showPage(FilterBrandPage.newInstance());
	}

	/**
	 * Handles click on the search icon.
	 */
	private void onSearchIcon() {
		searchEdit.setText("");
		searchEdit.requestFocus();
	}

	/**
	 * Shows the audit complete interface.
	 */
	private void onComplete() {
		disconnectBarcode();
		showPage(CompletePage.newInstance(audit));
	}

	/**
	 * Displays the update screen for the selected product.
	 * @param productStatus Selected product details
	 * @param rawScan Raw input from scanner or null if not scanned
	 */
	private void onSelectProduct(ProductStatus productStatus, String rawScan) {
		showPage(UpdateProductPage.newInstance(audit, productStatus, rawScan));
	}

	/**
	 * Displays the update screen for the selected product.
	 * @param productStatus Selected product details
	 * @param initReorderStatus Initial reorder status to push for the product
	 */
	private void onSelectProduct(ProductStatus productStatus, ReorderStatus initReorderStatus) {
		showPage(UpdateProductPage.newInstance(audit, productStatus, null, initReorderStatus));
	}

	/**
	 * Handles click on a status button in the list.
	 * @param buttonView Clicked button
	 */
	private void onItemReorderStatus(final View buttonView) {
		// We need to find the product based on the button
		// Note: assumes product name is unique, we may have to revisit
		ProductStatus update = null;
		ViewGroup rowView = (ViewGroup) buttonView.getParent();
		TextView productNameView = rowView.findViewById(R.id.productText);
		String productName = productNameView.getText().toString();
		for (ProductStatus find : allProducts) {
			if (find.getProductName().equals(productName)) {
				// Found
				update = find;
				break;
			}
		}
		if (update == null) {
			// Unexpected, not found
			Toast.makeText(getContext(), R.string.message_prods_not_found, Toast.LENGTH_SHORT).show();
			return;
		}

		// Offer the reorder options to the user
		isInModal = true;
		Security sec = new Security(getContext().getApplicationContext());
		boolean inStockRequiresScan =
				!update.getProduct().isRandomWeight() &&
				(update.getReorderStatus() != ReorderStatus.IN_STOCK) &&
				sec.optSettingBool(Security.SETTING_IN_STOCK_REQUIRES_SCAN, false);
		final List<CharSequence> displayOptions = new ArrayList<>();
		final List<ReorderStatus> selectOptions = new ArrayList<>();
		for (ReorderStatus status : ReorderStatus.Statuses) {
			if (!status.isValid()) {
				continue;
			}
			if (inStockRequiresScan && (status == ReorderStatus.IN_STOCK)) {
				continue;
			}
			displayOptions.add(status.getName());
			selectOptions.add(status);
		}
		final ProductStatus applyUpdate = update;
		new AlertDialog.Builder(getContext())
			.setTitle(R.string.message_reorder_status_message)
			.setItems(displayOptions.toArray(new CharSequence[0]), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					isInModal = false;
					dialogInterface.dismiss();
					if ((i >= 0) && (i < selectOptions.size())) {
						ReorderStatus selected = selectOptions.get(i);
						updateProductReorderStatus((TextView) buttonView, applyUpdate, selected);
					}
				}
			})
			.setOnCancelListener(new DialogInterface.OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialogInterface) {
					isInModal = false;
					dialogInterface.dismiss();
				}
			})
			.show();
	}

	/**
	 * Updates the reorder status of the passed product in the database, the local model and the UI.
	 * @param button Button associated with product
	 * @param update Product in the local model to update
	 * @param selectedStatus Selected status to update
	 */
	private void updateProductReorderStatus(TextView button, ProductStatus update, ReorderStatus selectedStatus) {
		// Special case if in stock is selected
		if (selectedStatus == ReorderStatus.IN_STOCK) {
			onSelectProduct(update, selectedStatus);
			return;
		}

		try (AuditDatabase db = new AuditDatabase(getContext())) {
			// Check for existing record to update
			Report report = db.getReport(audit, update.getProduct().getId());
			if (report == null) {
				// New report
				report = new Report(audit, update.getProduct(), selectedStatus.getId());
			} else {
				// Update existing report
				report = new Report(report, null, selectedStatus.getId());
			}

			// Update the record
			db.updateReport(report);

			// Do we need to get the scan?
			Scan scan = null;
			if (selectedStatus == ReorderStatus.OUT_OF_STOCK) {
				scan = db.getScan(audit, update.getProduct().getId());
			}

			// Update the data model
			update.setReorderStatus(report, scan);
		} catch (MobileClientException exc) {
			// Failed to update the database
			Toast.makeText(getContext(), exc.getMessage(), Toast.LENGTH_SHORT).show();
			return;
		}

		// Is the updated status in the filter?
		boolean isInFilter = filterReorderStatus == null;
		for (int index = 0; !isInFilter && (index < filterReorderStatus.length); ++index) {
			if (update.getReorderStatus() == filterReorderStatus[index]) {
				isInFilter = true;
			}
		}
		if (isInFilter) {
			// Update the filter status on the button
			ReorderStatus displayStatus = update.getReorderStatus();
			String statusName = displayStatus.getCode();
			if (((displayStatus == ReorderStatus.IN_STOCK) || (displayStatus == ReorderStatus.OUT_OF_STOCK)) && update.hasDisplayPrice()) {
				// Append display price
				statusName += String.format(" %s",
						NumberFormat.getCurrencyInstance().format(update.getDisplayPrice()));
			}
			button.setText(statusName);
			if (update.getReorderStatus() == ReorderStatus.IN_STOCK) {
				onSelectProduct(update, (String) null);
			}
		} else {
			// New status filtered out of list
			productStatusAdapter.remove(update);
		}
	}

	/**
	 * Applies the current filter as the user types, or performs any action that might change
	 * the filter.
	 * @param initial Initial data flag
	 */
	private void applyFilter(boolean initial) {
		// Have we been loaded yet?
		if (productStatusAdapter == null) {
			// Not yet...
			return;
		}

		// Make sure the reorder status filter button text is up to date
		if (filterReorderStatus != null) {
			filterButton.setText((filterProductTypes == null)
					? R.string.button_prods_filter_reorder_status
					: R.string.button_prods_filter_multi);
		} else {
			filterButton.setText((filterProductTypes == null)
					? R.string.button_prods_filter_all
					: R.string.button_prods_filter_product_type);
		}
		brandsButton.setText((filterBrands == null)
				? R.string.button_prods_brand_all
				: R.string.button_prods_brand_filter);

		// Check for trivial case, no filter
		String searchText = searchEdit.getText().toString();
		boolean isSearchTextEmpty = searchText.matches("^\\s*$");
		searchButton.setImageResource(isSearchTextEmpty
			? android.R.drawable.ic_search_category_default
			: android.R.drawable.ic_menu_close_clear_cancel);
		String[] tokens = isSearchTextEmpty
				? null
				: TextUtils.split(searchText, "\\s+");
		if ((tokens == null) && (filterReorderStatus == null) && (filterProductTypes == null) && (filterBrands == null)) {
			// Simple case, all products
			productStatusAdapter.clear();
			productStatusAdapter.addAll(allProducts);
			if (!initial) {
				// Log cleared filter to analytics
				Analytics.filter("Products", new ArrayList<String>(), "");
			}
		} else {
			// Filter the products
			List<ProductStatus> filteredProducts = new ArrayList<>();
			for (ProductStatus product : allProducts) {
				// Is there a reorder status filter?
				if (filterReorderStatus != null) {
					// Check if the product is within this filter
					boolean isInFilter = false;
					for (ReorderStatus status : filterReorderStatus) {
						if (status == product.getReorderStatus()) {
							isInFilter = true;
							break;
						}
					}
					if (!isInFilter) {
						continue;
					}
				}

				// Is there a product type filter?
				if (filterProductTypes != null) {
					// Check if the product is within this filter
					if (!filterProductTypes.contains(product.getProductType())) {
						// Not in product type filter
						continue;
					}
				}

				// Is there a brand filter?
				if (filterBrands != null) {
					// Check if the product is within this filter
					if (!filterBrands.contains(product.getProduct().getDisplayBrandName())) {
						// Not in filter
						continue;
					}
				}

				// Are there search tokens?
				if (tokens != null) {
					// Check all the tokens in the product
					boolean isInFilter = true;
					for (String token : tokens) {
						if (!product.getProduct().hasToken(token)) {
							isInFilter = false;
							break;
						}
					}
					if (!isInFilter) {
						continue;
					}
				}

				// The product is within the filter
				filteredProducts.add(product);
			}

			// Push the filtered list
			productStatusAdapter.clear();
			productStatusAdapter.addAll(filteredProducts);

			// Record analytics
			ArrayList<String> options = new ArrayList<>();
			if (filterReorderStatus == null) {
				options.add("All Reorder Statuses");
			} else {
				for(ReorderStatus status : filterReorderStatus) {
					options.add(status.getCode());
				}
			}
			if (filterProductTypes == null) {
				options.add("All Product Types");
			} else {
				options.addAll(filterProductTypes);
			}
			if (filterBrands == null) {
				options.add("All Brands");
			} else {
				options.addAll(filterBrands);
			}
			Analytics.filter("Products", options, isSearchTextEmpty ? "" : searchText);
		}
	}

	/**
	 * Sets the current barcode state and adjusts the user interface accordingly.
	 * @param state New state, or UNCHANGED pseudo-state
	 */
	private void setBarcodeState(BarcodeState state) {
		// Should we update the state?
		if (state != BarcodeState.UNCHANGED) {
			if (state == barcodeState) {
				// No change
				return;
			}
			barcodeState = state;
		}
		if (barcodeMenuItem != null) {
			switch (barcodeState) {
				case NONE:
					// No barcode reader, hide icon
					barcodeMenuItem.setVisible(false);
					if (battery != null) {
						battery.setVisible(false);
					}
					return;
				case CONNECTED:
					// Show connected icon
					barcodeMenuItem.setIcon(R.drawable.ic_barcode_scan_online);

					// Make sure this didn't cross paths with the page closing
					if ((barcodeReader != null) && (battery != null)) {
						// Still valid, update battery state
						battery.setVisible(true);
						battery.setValue(barcodeReader.GetBatteryLevel());
						batteryTimer = new Timer();
						batteryTimer.scheduleAtFixedRate(new UpdateBattery(), 60000, 60000);
					}
					break;
				case DISCONNECTED:
					// Show disconnected icon
					barcodeMenuItem.setIcon(R.drawable.ic_barcode_scan_offline);
					if (batteryTimer != null) {
						batteryTimer.cancel();
						batteryTimer = null;
					}
					break;
				case CONNECTING:
					// Show connecting icon
					barcodeMenuItem.setIcon(R.drawable.ic_barcode_scan_connecting);
					break;
			}
			barcodeMenuItem.setVisible(true);
		}
	}

	/**
	 * Asks the user what they want to set to, apply.
	 */
	private void onSetUnscanned() {
		CharSequence displayOptions[] = new CharSequence[] {
				ReorderStatus.OUT_OF_STOCK.getName(),
				ReorderStatus.VOID.getName(),
				getString(R.string.button_cancel)
		};
		isInModal = true;
		new AlertDialog.Builder(getContext())
				.setTitle(R.string.message_prods_set_unscanned_prompt)
				.setItems(displayOptions, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						isInModal = false;
						dialogInterface.dismiss();

						// Should we apply a new status?
						ReorderStatus apply;
						switch(i) {
							case 0: apply = ReorderStatus.OUT_OF_STOCK; break;
							case 1: apply = ReorderStatus.VOID; break;
							default: apply = ReorderStatus.NONE; break;
						}

						// Record analytics
						String actionType = (apply == ReorderStatus.NONE) ? "canceled" : apply.getCode();
						Analytics.menuAction("Set Unscanned", actionType);
						if (apply != ReorderStatus.NONE) {
							// Launch background unscanned task
							new SetUnscannedTask(SelectProductPage.this).execute(apply);
						}
					}
				})
				.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialogInterface) {
						isInModal = false;
						dialogInterface.dismiss();
					}
				})
				.show();
	}

	/**
	 * Sets unscanned products reorder status on a worker thread.
	 */
	private static class SetUnscannedTask extends AsyncTask<ReorderStatus, Void, String> {

		private WeakReference<SelectProductPage> pageRef;
		private boolean isAnyUpdated;
		private boolean isAllUpdated;

		SetUnscannedTask(SelectProductPage page) {
			pageRef = new WeakReference<>(page);
		}

		@Override
		protected String doInBackground(ReorderStatus... reorderStatuses) {
			// Get our page
			SelectProductPage page = pageRef.get();
			if (page == null) {
				return "Set unscanned task canceled";
			}
			try (AuditDatabase db = new AuditDatabase(page.getContext())) {
				// Cycle through our products
				int applyStatus = reorderStatuses[0].getId();
				for (ProductStatus product : page.allProducts) {
					if (product.getReorderStatus() != ReorderStatus.NONE) {
						// This one is scanned, do not update
						continue;
					}

					// Create a report
					Report report = new Report(page.audit, product.getProduct(), applyStatus);
					db.updateReport(report);
					product.setReorderStatus(report, null);
					isAnyUpdated = true;
				}
				isAllUpdated = true;
				return null;
			} catch (MobileClientException exc){
				// Failed to update the database
				return exc.getMessage();
			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			SelectProductPage page = pageRef.get();
			IPageParent parent = (page == null) ? null : page.getParent();
			if (parent == null) {
				cancel(true);
			} else {
				page.isInModal = true;
				parent.setActivity(true);
			}
		}

		@Override
		protected void onPostExecute(String s) {
			super.onPostExecute(s);

			// Do we still have a page reference?
			SelectProductPage page = pageRef.get();
			IPageParent parent = (page == null) ? null : page.getParent();
			if (page == null) {
				return;
			}

			// Hide the wait indicator
			page.isInModal = false;
			parent.setActivity(false);
			if (s != null) {
				// Show the error
				Toast.makeText(page.getContext(), s, Toast.LENGTH_SHORT).show();
			} else {
				// Show success
				Toast.makeText(page.getContext(), isAnyUpdated
						? R.string.message_prods_set_unscanned_updated
						: R.string.message_prods_set_unscanned_none,
					Toast.LENGTH_SHORT).show();
			}
			if (isAnyUpdated) {
				// Refresh the list
				page.applyFilter(true);
			}
			if (isAllUpdated) {
				((Activity) parent).invalidateOptionsMenu();
			}
		}
	}

	/**
	 * Updates the battery on a regular basis.
	 */
	class UpdateBattery extends TimerTask {
		/** Updates the battery status as necessary. */
		@Override
		public void run() {
			if ((barcodeState == BarcodeState.CONNECTED) && (barcodeReader != null) &&
					(battery != null) && (battery.isUpdateNeeded(60))) {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						battery.setValue(barcodeReader.GetBatteryLevel());
					}
				});
			}
		}
	}

	private static final String LOG_TAG = "SelecProductPage";
	private static final String ARG_AUDIT = "argAudit";
	private static final String STATE_FILTER_SEARCH = "filterSearch";
	private static final String STATE_FILTER_REORDER_STATUS = "filterReorderStatus";
	private static final String STATE_FILTER_PRODUCT_TYPES = "filterProductTypes";

	private Audit audit;
	private ReorderStatus[] filterReorderStatus = new ReorderStatus[0];
	private boolean isInModal = false;
	private List<ProductStatus> allProducts;
	private List<String> allProductTypes;
	private ArrayList<String> filterProductTypes;
	private SortedSet<String> allBrands;
	private Set<String> filterBrands;

	private Button filterButton;
	private Button brandsButton;
	private ArrayAdapter<ProductStatus> productStatusAdapter;
	private EditText searchEdit;
	private ImageButton searchButton;
	private KDCReader barcodeReader;
	private BatteryIcon battery;
	private Timer batteryTimer;

	private enum BarcodeState { UNCHANGED, NONE, CONNECTING, CONNECTED, DISCONNECTED }
	private BarcodeState barcodeState = BarcodeState.NONE;
	private MenuItem barcodeMenuItem;
	private MenuItem unscannedMenuItem;
}
