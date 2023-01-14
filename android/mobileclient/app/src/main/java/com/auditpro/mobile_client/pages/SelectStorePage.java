/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.pages;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.auditpro.mobile_client.Analytics;
import com.auditpro.mobile_client.MainActivity;
import com.auditpro.mobile_client.MobileClientException;
import com.auditpro.mobile_client.database.AuditDatabase;
import com.auditpro.mobile_client.database.StoresDatabase;
import com.auditpro.mobile_client.dialog.BeginAuditDialog;
import com.auditpro.mobile_client.entities.Audit;
import com.auditpro.mobile_client.entities.AuditType;
import com.auditpro.mobile_client.entities.Chain;
import com.auditpro.mobile_client.entities.Store;
import com.auditpro.mobile_client.security.Security;
import com.auditpro.mobile_client.test.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * Manages a fragment to display the select store page.
 * @author Eric Ruck
 */
public class SelectStorePage extends BasePage implements BeginAuditDialog.Listener {

	/**
	 * Required empty public constructor.
	 */
	public SelectStorePage() { }

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 * @param latitude Optional latitude when this instance is created
	 * @param longitude Optional longitude when this instance is created
	 * @return A new instance of fragment SelectStorePage.
	 */
	public static SelectStorePage newInstance(Double latitude, Double longitude) {
		SelectStorePage fragment = new SelectStorePage();
		Bundle args = new Bundle();
		if ((latitude != null) && (longitude != null)) {
			args.putDouble(ARG_LATITUDE, latitude);
			args.putDouble(ARG_LONGITUDE, longitude);
			fragment.setArguments(args);
		}
		return fragment;
	}

	/**
	 * Creates our view.
	 * @param inflater Inflater to use
	 * @param container Parent container for view
	 * @param savedInstanceState Optional state
	 * @return New view for fragment
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_select_store_page, container, false);
	}

	/**
	 * Initializes the view after it has been created.
	 * @param view Created view
	 * @param savedInstanceState Optional state
	 */
	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Wire up our buttons
		chainButton = view.findViewById(R.id.chainButton);
		chainButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onChain();
			}
		});
		gpsButton = view.findViewById(R.id.gpsButton);
		gpsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onSortOption(SortOption.Gps);
			}
		});
		lastStoreButton = view.findViewById(R.id.lastStoreButton);
		lastStoreButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onSortOption(SortOption.LastStore);
			}
		});
		nameButton = view.findViewById(R.id.nameButton);
		nameButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onSortOption(SortOption.Name);
			}
		});
		searchButton = view.findViewById(R.id.searchButton);
		searchButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onSearchIcon();
			}
		});

		// Setup the search filter
		searchEdit = view.findViewById(R.id.searchEdit);
		searchEdit.addTextChangedListener(new TextWatcher() {
			@Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
			@Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
			@Override public void afterTextChanged(Editable editable) {
				updateFilter(false);
			}
		});

		// Initialize the stores
		allStores = new ArrayList<>();
		allChains = new ArrayList<>();

		// Is there state to restore?
		if (savedInstanceState != null) {
			// Restore the state
			String filterSearch = savedInstanceState.getString(STATE_FILTER_SEARCH);
			searchEdit.setText((filterSearch == null) ? "" : filterSearch);
			currentSortOption = (SortOption) savedInstanceState.get(STATE_LAST_LOC_SORT_OPT);
			currentSortDirection = savedInstanceState.getInt(STATE_LAST_LOC_SORT_DIR);
			if (savedInstanceState.containsKey(STATE_LAST_LOC_FILT_CHAIN)) {
				int chainId = savedInstanceState.getInt(STATE_LAST_LOC_FILT_CHAIN);
				for(Chain chain : allChains) {
					if (chain.getChainId() == chainId) {
						currentChain = chain;
						break;
					}
				}
			}
		} else {
			// Determine reasonable initial state
			Security sec = new Security(getContext().getApplicationContext());
			searchEdit.setText("");
			if (getLocation() != null) {
				currentSortOption = SortOption.Gps;
			} else if (sec.getLastAuditPos() != null) {
				currentSortOption = SortOption.LastStore;
			} else {
				currentSortOption = SortOption.Name;
			}
		}
	}

	/**
	 * Loads the stores on a background thread once the UI is fully initialized.
	 */
	@Override
	public void onStart() {
		super.onStart();
		int countStores = (allStores == null) ? 0 : allStores.size();
		int countChains = (allChains == null) ? 0 : allChains.size();
		if (countStores + countChains == 0) {
			// Load the store on a worker thread
			getParent().setActivity(true);
			new LoadStores(this).execute();
		}
	}

	/**
	 * Persists our state.
	 * @param outState Receives state
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// Persist our state
		outState.putString(STATE_FILTER_SEARCH, searchEdit.getText().toString());
		outState.putSerializable(STATE_LAST_LOC_SORT_OPT, currentSortOption);
		outState.putInt(STATE_LAST_LOC_SORT_DIR, currentSortDirection);
		if (currentChain != null) {
			outState.putInt(STATE_LAST_LOC_FILT_CHAIN, currentChain.getChainId());
		}
	}

	/**
	 * Provides a title for the action bar.
	 * @param context Application context
	 * @return Action bar title
	 */
	@Override
	public String getPageName(Context context) {
		return context.getResources().getString(R.string.page_name_select_store);
	}

	/**
	 * Updates UI when the page appears.
	 */
	@Override
	public void onPageAppearing() {
		((MainActivity) getActivity()).startLocationUpdates();
	}

	/**
	 * Suspends GPS request when we're disappearing.
	 */
	@Override
	public void onPageDisappearing() {
		((MainActivity) getActivity()).endLocationUpdates();
	}

	/**
	 * Displays the chain filter options.
	 */
	private void onChain() {
		// Determine the chain options to display
		final List<String> options = new ArrayList<>();
		for (Chain chain : allChains) {
			options.add(chain.getChainName());
		}
		Collections.sort(options);

		// Prepend cleared "All Chains" option
		final String allChainsName = getResources().getString(R.string.button_stores_all_chains);
		options.add(0, allChainsName);

		// Let the user select
		final List<Chain> finalChains = allChains;
		new AlertDialog.Builder(getContext())
			.setTitle(R.string.message_stores_select_chain)
			.setItems(options.toArray(new CharSequence[0]), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					dialogInterface.dismiss();
					if (i > 0) {
						// Client filter selected
						String chainName = options.get(i);
						if ((currentChain != null) && currentChain.getChainName().equals(chainName)) {
							// No change
							return;
						}

						// Find new client filter
						for(Chain chain : finalChains) {
							if (chain.getChainName().equals(chainName)) {
								// Chain found
								chainButton.setText(chainName);
								currentChain = chain;
								updateFilter(false);
								return;
							}
						}
					}
					if (currentChain != null) {
						// Clear chain filter
						chainButton.setText(allChainsName);
						currentChain = null;
						updateFilter(false);
					}
				}
			})
			.show();
	}

	/**
	 * Handles click on the search icon.
	 */
	private void onSearchIcon() {
		searchEdit.setText("");
		searchEdit.requestFocus();
	}

	/**
	 * Updates the current sort option.
	 * @param value New sort option
	 */
	private void onSortOption(SortOption value) {
		Security sec = new Security(getContext().getApplicationContext());
		Location lastLocation = ((MainActivity) getActivity()).getLastLocation();
		if (value == currentSortOption) {
			// Reverse direction
			currentSortDirection *= -1;
		} else if ((value == SortOption.Gps) && (lastLocation == null)) {
			// GPS unavailable
			Toast.makeText(getContext(), R.string.message_stores_no_gps, Toast.LENGTH_SHORT).show();
			return;
		} else if ((value == SortOption.LastStore) && (sec.getLastAuditPos() == null)) {
			// No last audit position
			Toast.makeText(getContext(), R.string.message_stores_no_last, Toast.LENGTH_SHORT).show();
			return;
		} else {
			// Select option
			currentSortOption = value;
			currentSortDirection = 1;
		}

		// Display the current sort
		updateSort();
	}

	/**
	 * Updates the list adapter to set the current sort.
	 */
	private void updateSort() {
		// Have we been initialized yet?
		if (storeAdapter == null) {
			// Not yet...
			return;
		}

		// What is the current sort that we should apply?
		final int useDirection = currentSortDirection;
		if (currentSortOption == SortOption.Name) {
			// Basic store name sort
			storeAdapter.sort(new Comparator<Store>() {
				@Override
				public int compare(Store store, Store t1) {
					return store.getStoreName().compareTo(t1.getStoreName()) * useDirection;
				}
			});
			updateSortButtons();
			return;
		}

		// Compare by geocode
		Security sec = new Security(getContext().getApplicationContext());
		Pair<Double, Double> storeGeo = sec.getLastAuditPos();
		Location lastLocation = getLocation();
		final double sourceLat = (currentSortOption == SortOption.Gps) ? lastLocation.getLatitude() : storeGeo.first;
		final double sourceLon = (currentSortOption == SortOption.Gps) ? lastLocation.getLongitude() : storeGeo.second;

		// Precalculate comparison source
		final double rlatSource = Math.PI * sourceLat / 180;
		final double srlatSource = Math.sin(rlatSource);
		final double crlatSource = Math.cos(rlatSource);

		// Apply sort comparator
		storeAdapter.sort(new Comparator<Store>() {
			@Override
			public int compare(Store t0, Store t1) {
				// Trivial case, check geocoding
				if (!t0.isGeocoded()) {
					if (t1.isGeocoded()) {
						return useDirection;
					}

					// Fall back on name
					return t0.getStoreName().compareTo(t1.getStoreName()) * useDirection;
				} else if (!t1.isGeocoded()) {
					return -useDirection;
				}

				// Compare distances from source
				final double earth = 637300.0; // Earth radius decimeters
				double rlatX = Math.PI * t0.getStoreLat() / 180;
				double rlatY = Math.PI * t1.getStoreLat() / 180;
				double thetaX = sourceLon - t0.getStoreLon();
				double rthetaX = Math.PI * thetaX / 180;
				double thetaY = sourceLon - t1.getStoreLon();
				double rthetaY = Math.PI * thetaY / 180;
				double distX =
					srlatSource * Math.sin(rlatX) + crlatSource *
						Math.cos(rlatX) * Math.cos(rthetaX);
				distX = Math.round(Math.acos(distX) * earth);
				double distY =
					srlatSource * Math.sin(rlatY) + crlatSource *
						Math.cos(rlatY) * Math.cos(rthetaY);
				distY = Math.round(Math.acos(distY) * earth);
				if (distX == distY) {
					// Fall back on name
					return t0.getStoreName().compareTo(t1.getStoreName()) * useDirection;
				}
				return ((distY > distX) ? 1 : -1) * useDirection;
			}
		});
		updateSortButtons();
	}

	/**
	 * Updates the appearance of all the sort buttons to match the current state.
	 */
	private void updateSortButtons() {
		selectSortButton(gpsButton, currentSortOption == SortOption.Gps);
		selectSortButton(lastStoreButton, currentSortOption == SortOption.LastStore);
		selectSortButton(nameButton, currentSortOption == SortOption.Name);
	}

	/**
	 * Updates the selected state of the passed sort button.
	 * @param button Button to update
	 * @param isSelected Selected state
	 */
	private void selectSortButton(Button button, boolean isSelected) {
		button.setTextColor(isSelected ? 0xffff0000 : 0xff000000); // red : black
	}

	/**
	 * Updates the list to reflect the stores within the current filters.
	 * @param initial Flags first update after store load
	 */
	private void updateFilter(boolean initial) {
		// Have we been initialized yet?
		if (storeAdapter == null) {
			// Not yet...
			return;
		}

		// Is there a filter?
		storeAdapter.clear();
		String searchText = searchEdit.getText().toString();
		boolean isSearchTextEmpty = searchText.matches("^\\s*$");
		searchButton.setImageResource(isSearchTextEmpty
				? android.R.drawable.ic_search_category_default
				: android.R.drawable.ic_menu_close_clear_cancel);
		String[] tokens = isSearchTextEmpty
				? null
				: TextUtils.split(searchText.toLowerCase(), "\\s+");
		if ((tokens == null) && (currentChain == null)) {
			// No filter
			storeAdapter.addAll(allStores);
			if (!initial) {
				// Show cleared filter in analytics only after initial load
				Analytics.filter("Stores", new ArrayList<String>(), "");
			}
		} else {
			// Determine the stores within the filter
			List<Store> filteredStores = new ArrayList<>();
			for (Store store : allStores) {
				// Check if the store is within the chain
				if ((currentChain != null) && (store.getChainId() != currentChain.getChainId())) {
					continue;
				}

				// Are there search tokens?
				if (tokens != null) {
					// Check all the tokens in the store
					boolean isInFilter = true;
					for (String token : tokens) {
						if (!store.getStoreName().toLowerCase().contains(token)) {
							isInFilter = false;
							break;
						}
					}
					if (!isInFilter) {
						continue;
					}
				}

				// This store is within the filter
				filteredStores.add(store);
			}
			storeAdapter.addAll(filteredStores);

			// Log filter in analytics
			ArrayList<String> options = new ArrayList<>();
			if (currentChain != null) {
				options.add(currentChain.getChainName());
			}
			Analytics.filter("Stores", options, isSearchTextEmpty ? "" : searchText);
		}

		// Reassert sort
		updateSort();
	}

	/**
	 * Handles the selection of a store from our list.
	 * @param store Selected store
	 */
	private void onSelectStore(final Store store) {
		DialogFragment dialog = BeginAuditDialog.newInstance(store);
		dialog.show(getFragmentManager(), "Begin Audit Dialog");
	}

	/**
	 * Handles confirmation that the user wants to audit a store.
	 *
	 * @param store Provides the store that the user wants to audit
	 */
	@Override
	public void onConfirmStoreAudit(Store store) {
		try (AuditDatabase db = new AuditDatabase(getContext())) {
			// Begin a new audit
			Double currentLat = null;
			Double currentLon = null;
			Location location = getLocation();
			if (location != null) {
				currentLat = location.getLatitude();
				currentLon = location.getLongitude();
			}
			Security sec = new Security(getContext().getApplicationContext());
			Audit audit = db.startAudit(sec.getUserId(),
					store.getStoreId(), store.getDescription(),
					AuditType.STANDARD.getId(), currentLat, currentLon);

			// Record the location for the next time
			if (store.hasLatLong()) {
				sec.setLastAuditPos(store.getStoreLat(), store.getStoreLon());
			} else {
				sec.setLastAuditPos(currentLat, currentLon);
			}

			// Show the products page
			IPageParent parent = getParent();
			if (parent != null) {
				parent.swapPage(SelectProductPage.newInstance(audit));
			}
		} catch (MobileClientException exc) {
			// Show the error to the user, details already logged
			Toast.makeText(getContext(), exc.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Gets the current location to use for our business logic.
	 * @return Current location
	 */
	private Location getLocation() {
		// Start with the common location from the parent activity
		Location location = ((MainActivity) getActivity()).getLastLocation();
		if (location == null) {
			// See if we were passed a location from the prior page
			Bundle args = getArguments();
			if ((args != null) && args.containsKey(ARG_LATITUDE) && args.containsKey(ARG_LONGITUDE)) {
				location = new Location(LOG_TAG);
				location.setLatitude(args.getDouble(ARG_LATITUDE));
				location.setLongitude(args.getDouble(ARG_LONGITUDE));
			}
		}

		// Return the location
		return location;
	}

	/**
	 * Loads stores (and chains) asynchronously.
	 */
	private static class LoadStores extends AsyncTask<Void, Void, Void> {

		private WeakReference<SelectStorePage> pageRef;
		private List<Store> loadedStores;
		private List<Chain> loadedChains;

		/**
		 * Initializes to display the results on the passed page.
		 * @param page Page receives results
		 */
		LoadStores(SelectStorePage page) {
			pageRef = new WeakReference<>(page);
		}

		/**
		 * Safely gets the context from the weak reference if we can.
		 * @return Context or null
		 */
		private Context getContext() {
			SelectStorePage page = pageRef.get();
			return (page == null) ? null : page.getContext().getApplicationContext();
		}

		/**
		 * Loads the stores and chains from the database on a background thread.
		 * @param voids Part of the async task superclass, ignored
		 * @return Also ignored
		 */
		@Override
		protected Void doInBackground(Void... voids) {
			// Get the application context
			Context context = getContext();
			if (context == null) {
				// Page is already gone
				return null;
			}

			// Get all the stores
			try (StoresDatabase db = new StoresDatabase(context)) {
				loadedStores = db.getStores();
				loadedChains = db.getChains();
			} catch (MobileClientException exc) {
				// Should be logged by the database layer
			}
			return null;
		}

		/**
		 * Applies the loaded stores to the user interface.
		 * @param aVoid Ignored
		 */
		@Override
		protected void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);
			SelectStorePage page = pageRef.get();
			IPageParent parent = (page == null) ? null : page.getParent();
			if (parent != null) {
				// Complete the UI update
				parent.setActivity(false);
				if ((loadedStores != null) && (loadedChains != null)) {
					// Apply the loaded stores (and chains)
					page.completedLoad(loadedStores, loadedChains);
				}
			}
		}
	}

	/**
	 * Populates our store list once its loaded from the database.
	 * @param loadedStores Stores loaded from the database
	 * @param loadedChains Chains loaded from the database
	 */
	@SuppressWarnings("ConstantConditions")
	void completedLoad(List<Store> loadedStores, List<Chain> loadedChains) {
		// Apply the loaded stores and chains
		allStores = loadedStores;
		allChains = loadedChains;

		// Initialize the store list
		ListView storeList = getView().findViewById(R.id.storeList);
		storeAdapter = new ArrayAdapter<>(getContext(), R.layout.list_item_store, R.id.storeText);
		storeList.setAdapter(storeAdapter);
		storeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				Store selected = storeAdapter.getItem(i);
				if (selected != null) {
					onSelectStore(selected);
				}
			}
		});

		// Complete initializing the store
		updateFilter(true);
	}

	private static final String LOG_TAG = "SelectStorePage";
	private static final String ARG_LATITUDE = "paramLatitude";
	private static final String ARG_LONGITUDE = "paramLongitude";
	private static final String STATE_LAST_LOC_SORT_OPT = "lastLocSortOpt";
	private static final String STATE_LAST_LOC_SORT_DIR = "lastLocSortDir";
	private static final String STATE_LAST_LOC_FILT_CHAIN = "lastLocFiltChain";
	private static final String STATE_FILTER_SEARCH = "filterSearch";

	private Button chainButton;
	private Button gpsButton;
	private Button lastStoreButton;
	private Button nameButton;
	private EditText searchEdit;
	private ImageButton searchButton;
	private ArrayAdapter<Store> storeAdapter;
	private List<Store> allStores;
	private List<Chain> allChains;

	/**
	 * Options for sorting stores on the page.
	 */
	protected enum SortOption { Gps, LastStore, Name }

	/**
	 * Current sort option.
	 */
	private SortOption currentSortOption = SortOption.Name;

	/**
	 * Current sort order.
	 */
	private int currentSortDirection = 1;

	/**
	 * Option for filtering stores by chain.
	 */
	private Chain currentChain;
}
