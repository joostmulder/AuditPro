/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.auditpro.mobile_client.api.UserResponse;
import com.auditpro.mobile_client.dialog.BeginAuditDialog;
import com.auditpro.mobile_client.entities.ProductStatus;
import com.auditpro.mobile_client.entities.ReorderStatus;
import com.auditpro.mobile_client.entities.Scan;
import com.auditpro.mobile_client.entities.Store;
import com.auditpro.mobile_client.pages.BasePage;
import com.auditpro.mobile_client.pages.FilterStatusProvider;
import com.auditpro.mobile_client.pages.IPageParent;
import com.auditpro.mobile_client.pages.LoginPage;
import com.auditpro.mobile_client.pages.MainMenuPage;
import com.auditpro.mobile_client.pages.UpdateProductPage;
import com.auditpro.mobile_client.security.Security;
import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import io.fabric.sdk.android.Fabric;

import com.auditpro.mobile_client.test.R;


/**
 * Implements the main activity for the AuditPro Mobile Client application
 * @author Eric Ruck
 */
@SuppressWarnings("ConstantConditions")
public class MainActivity extends AppCompatActivity implements
		IPageParent,
		FilterStatusProvider,
		UpdateProductPage.UpdateProductListener,
		BeginAuditDialog.Listener {

	private static final String LOG_TAG = "mobileclient_bt";
	private static final String STATE_SESSION_TOKEN = "sessionToken";
	private static final String STATE_LAST_LOC_LAT = "lastLocLat";
	private static final String STATE_LAST_LOC_LON = "lastLocLon";

	private ViewGroup pagesFrame;
	private View activityView;
	private String sessionToken;
	private Location lastLocation;
	private boolean syncRequested;


	/**
	 * Preserves our successful login.
	 * @param sessionToken Token for remote API access
	 * @param email Validate user e-mail
	 * @param password Password entered to authenticate user
	 * @param isPasswordSaved Should the password be saved in the store?
	 * @param userResponse User information from the server API
	 */
	public void setLogin(String sessionToken, String email, String password,
			boolean isPasswordSaved, UserResponse userResponse) {
		Security sec = new Security(getApplicationContext());
		this.sessionToken = sessionToken;
		sec.setLogin(email, password, isPasswordSaved, userResponse);

		// Format for analytics
		Crashlytics.setUserIdentifier(Integer.toString(userResponse.getUserId()));
		Crashlytics.setUserEmail(email);
		Crashlytics.setUserName(sec.getUserName());
	}

	/**
	 * Logs out of our current session.
	 */
	public void logout() {
		sessionToken = null;
	}

	/**
	 * Gets the API access token for the current session.
	 * @return Session token or null if none
	 */
	public String getSessionToken() {
		return sessionToken;
	}

	/**
	 * Indicates if a current session is active.
	 * @return Active session flag
	 */
	public boolean isInSession() {
		return sessionToken != null;
	}

	/**
	 * Gets an indication of whether we have network connectivity.
	 * @return Network available flag
	 */
	public boolean isNetworkConnected() {
		ConnectivityManager cm =
				(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		return activeNetwork != null &&
				activeNetwork.isConnectedOrConnecting();
	}

	/**
	 * Verifies that we have an Internet connection over wifi (as opposed to cell).
	 * @return Wifi connection flag
	 */
	public boolean isWifiConnected() {
		ConnectivityManager cm =
				(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		if (activeNetwork != null) {
			int type = activeNetwork.getType();
			return (type == ConnectivityManager.TYPE_WIFI) || (type == ConnectivityManager.TYPE_ETHERNET);
		}
		return false;
	}

	/**
	 * Gets the version name from our package resources.  Returns null and logs a warning
	 * on failure.
	 * @return Version name or null
	 */
	@Override
	public String getVersion() {
		try {
			PackageInfo pInfo = this.getPackageManager().getPackageInfo(getPackageName(), 0);
			return pInfo.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			Log.w(LOG_TAG, "Version name not found", e);
			return null;
		}
	}

	/**
	 * Requests permission to read GPS if we don't already have it.
	 */
	public void requestLocationPermission() {
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
				!= PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this,
					new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
					1);
		}
	}

	/**
	 * Handles receiving permission for GPS by enabling on the showing page.
	 * @param requestCode App request code
	 * @param permissions Permissions requested
	 * @param grantResults Grant results for each permission
	 */
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (grantResults.length > 0
				&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
			// Is the showing page interested in GPS?
			BasePage page = attachedPages.get(attachedPages.size() - 1);
			if (page instanceof LocationListener) {
				startLocationUpdates();
			}
		}
	}

	/**
	 * Attempts to enable GPS location updates.
	 * @return Started flag
	 */
	public boolean startLocationUpdates() {
		if (ContextCompat.checkSelfPermission(this,
				Manifest.permission.ACCESS_FINE_LOCATION)
				!= PackageManager.PERMISSION_GRANTED) {
			return false;
		}

		// Get the location manager
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		if (lm == null) {
			Log.w(LOG_TAG, "Unable to access GPS location");
			return false;
		}

		// Enable location updates
		boolean isGpsRequested = false;
		boolean isNetRequested = false;
		try {
			// Request GPS location
			lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, gpsListener);
			isGpsRequested = true;
		} catch (Exception exc) {
			// Unable to access GPS location
			Log.w(LOG_TAG, "Unable to access GPS location", exc);
		}
		try {
			// Request network location
			lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, netListener);
			isNetRequested = true;
		} catch (Exception exc) {
			// Unable to access network location
			Log.w(LOG_TAG, "Unable to access network location", exc);
		}

		// Indicate if we have any location updates requested
		return isGpsRequested || isNetRequested;
	}

	/**
	 * Ends GPS location updates.
	 */
	public void endLocationUpdates() {
		// Get the location manager
		LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		if (lm != null) {
			// Remove our listeners
			lm.removeUpdates(gpsListener);
			lm.removeUpdates(netListener);
		}

		// Clear any prior collected location
		lastLocation = null;
	}

	/**
	 * Gets the last location reported by the GPS, while we're monitoring the GPS position.
	 * Returns null if we don't have a location fix, or if we're not currently monitoring.
	 * @return Last reported location or null if none
	 */
	public Location getLastLocation() {
		return lastLocation;
	}

	/**
	 * Handles creation event for the application's main activity.
	 * @param savedInstanceState Saved state from prior instance or null
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Initialize our internal state
		if (savedInstanceState != null) {
			sessionToken = savedInstanceState.getString(STATE_SESSION_TOKEN, null);
			if (savedInstanceState.containsKey(STATE_LAST_LOC_LAT) &&
					savedInstanceState.containsKey(STATE_LAST_LOC_LON)) {
				double lat = savedInstanceState.getDouble(STATE_LAST_LOC_LAT);
				double lon = savedInstanceState.getDouble(STATE_LAST_LOC_LON);
				lastLocation = new Location(LOG_TAG);
				lastLocation.setLatitude(lat);
				lastLocation.setLongitude(lon);
			}
		}

		// Setup Crashlytics
		Fabric.with(this, new Crashlytics());
		Security sec = new Security(getApplicationContext());
		int userId = sec.getUserId();
		if (userId > 0) {
			Crashlytics.setUserIdentifier(Integer.toString(userId));
			Crashlytics.setUserEmail(sec.getLastEmail());
			Crashlytics.setUserName(sec.getUserName());
		}

		// Initialize our view
		setContentView(R.layout.activity_main);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		toolbar.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onNavigation();
			}
		});
		activityView = findViewById(R.id.activityView);

		// Start with login page
		pagesFrame = findViewById(R.id.pagesFrame);
		if ((pagesFrame != null) && (savedInstanceState == null)) {
			// Create a new Fragment to be placed in the activity layout
			pushPage(new LoginPage());
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(STATE_SESSION_TOKEN, sessionToken);
		if (lastLocation != null) {
			outState.putDouble(STATE_LAST_LOC_LAT, lastLocation.getLatitude());
			outState.putDouble(STATE_LAST_LOC_LON, lastLocation.getLongitude());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Do we have any pages?
		int size = attachedPages.size();
		if (size == 0) {
			// No top page means no menu
			return false;
		}

		// Delegate to the top page
		return attachedPages.get(size - 1).onCreateMenu(getMenuInflater(), menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Do we have any pages?
		int size = attachedPages.size();
		if (size == 0) {
			// No top page means no menu
			return false;
		}
		if (attachedPages.get(size - 1).onMenuItem(item)) {
			// Handled by delegate
			return true;
		}

		// Default handling
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Pushes a page onto our display stack.
	 * @param push Page to push
	 */
	public void pushPage(BasePage push) {
		// Are we covering an active page?
		int size = attachedPages.size();
		if (size > 0) {
			// Yes, alert it
			attachedPages.get(size - 1).onPageDisappearing();
		}
		try {
			// Push the page
			getSupportFragmentManager().beginTransaction()
					.add(R.id.pagesFrame, push).commit();
			setToolbarFor(push);
		} catch (IllegalStateException e) {
			// Turns out the way newer versions of Androids queue events, it's possible to process
			// a click in a state save lifecycle status, during which regular control access works
			// but not fragment manager transactions.  Possibly cause by either the user bouncing
			// an input or the OS being really slow.
			Log.e(LOG_TAG, "Unexpected state exception pushing page " + push.getClass().getSimpleName(), e);
		}
	}

	/**
	 * Swaps the current top page with the passed page.
	 * @param swapIn Page to swap with the top
	 */
	public void swapPage(BasePage swapIn) {
		popPage();
		pushPage(swapIn);
	}

	/**
	 * Pops a page from our display stack.
	 */
	public void popPage() {
		// Validate
		int size = attachedPages.size();
		if (size == 0) {
			// Page stack empty
			Log.w(LOG_TAG, "Attempt to pop empty page stack");
			return;
		}

		// Clear activity in case the last page left it set
		setActivity(false);

		// Pop it
		BasePage popped = attachedPages.get(--size);
		try {
			// Protect fragment transaction
			popped.onPageDisappearing();
			getSupportFragmentManager().beginTransaction()
					.remove(popped).commit();
			BasePage show = (size == 0) ? null : attachedPages.get(size - 1);
			setToolbarFor(show);
			if (show != null) {
				// Show the next one down
				show.onPageAppearing();
			}
		} catch (IllegalStateException e) {
			Log.e(LOG_TAG, "Unexpected state exception popping page " + popped.getClass().getSimpleName(), e);
		}
	}

	/**
	 * Pop pages off the top of the stack until we get to the target page.
	 * @param targetPage Target page class type
	 */
	public void popTo(Class<? extends BasePage> targetPage) {
		try {
			FragmentTransaction txn = getSupportFragmentManager().beginTransaction();
			int index = attachedPages.size();
			while (--index > 0) {
				// Should we stop on this page?
				BasePage checkPage = attachedPages.get(index);
				if (targetPage.isInstance(checkPage)) {
					// Stop here
					setToolbarFor(checkPage);
					checkPage.onPageAppearing();
					break;
				}

				// Pop this class
				checkPage.onPageDisappearing();
				txn.remove(checkPage);
			}
			txn.commit();
		} catch (IllegalStateException e) {
			Log.e(LOG_TAG, "Unexpected state exception popping to page " + targetPage.getSimpleName(), e);
		}
	}

	/**
	 * Determines if the passed page is at the top of the stack (showing).
	 * @param testPage Page to test
	 * @return Is top flag
	 */
	@Override
	public boolean isTopPage(BasePage testPage) {
		int index = attachedPages.size();
		return (index > 0) && (attachedPages.get(index - 1) == testPage);
	}

	/**
	 * Updates the toolbar state for the passed page
	 * @param page Page to sync with toolbar state
	 */
	private void setToolbarFor(BasePage page) {
		String title = (page != null) ? page.getPageName(this) : null;
		if (title == null) {
			// No toolbar for page
			getSupportActionBar().hide();
		} else {
			// Show toolbar
			getSupportActionBar().show();
			getSupportActionBar().setDisplayShowHomeEnabled(page.showNavigation());

			// Update toolbar
			Toolbar toolbar = findViewById(R.id.toolbar);
			toolbar.setTitle(title);
		}

		// On page transitions make sure the keyboard is hidden
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(pagesFrame.getWindowToken(), 0);
		if (page.shrinkForKeyboard()) {
			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN |
					WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		} else {
			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN |
					WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
		}
	}

	/**
	 * Handles navigation request from the toolbar.
	 */
	private void onNavigation() {
		onBackPressed();
	}

	/**
	 * Provides our ordered list of attached pages.
	 */
	private List<BasePage> attachedPages = new ArrayList<>();

	/**
	 * Attaches a page to our list.
	 * @param attach Page to attach.
	 */
	public void attachPage(BasePage attach) {
		attachedPages.add(attach);
		invalidateOptionsMenu();
	}

	/**
	 * Removes a page from our list.
	 * @param detach Page to remove
	 */
	public void detachPage(BasePage detach) {
		attachedPages.remove(detach);
		invalidateOptionsMenu();
	}

	/**
	 * Overrides default back button to implement our internal navigation.
	 */
	@Override
	public void onBackPressed() {
		int size = attachedPages.size();
		if (size <= 1) {
			// Leave
			finish();
		} else {
			// Navigate
			BasePage headPage = attachedPages.get(size - 1);
			if (!headPage.onBack()) {
				// Default navigation
				popPage();
			}
		}
	}

	/**
	 * Gets the current filter on reorder status.
	 * @return Current filter on reorder status
	 */
	@Override
	public ReorderStatus[] getFilterReorderStatus() {
		FilterStatusProvider provider = findFilterProvider();
		if (provider != null) {
			return provider.getFilterReorderStatus();
		}
		return new ReorderStatus[0];
	}

	/**
	 * Gets the current filter on product types.
	 * @return Current filter on product types
	 */
	@Override
	public ArrayList<String> getFilterProductTypes() {
		FilterStatusProvider provider = findFilterProvider();
		if (provider != null) {
			return provider.getFilterProductTypes();
		}
		return null;
	}

	/**
	 * Gets all of the available product types.
	 * @return All product types
	 */
	@Override
	public List<String> getAllProductTypes() {
		FilterStatusProvider provider = findFilterProvider();
		if (provider != null) {
			return provider.getAllProductTypes();
		}
		return null;
	}

	/**
	 * Sets the current filter.
	 * @param reorderStatuses New filter on reorder status
	 * @param productTypes New filter on product types
	 */
	@Override
	public void setFilterStatus(ReorderStatus[] reorderStatuses, ArrayList<String> productTypes) {
		FilterStatusProvider provider = findFilterProvider();
		if (provider != null) {
			provider.setFilterStatus(reorderStatuses, productTypes);
		}
	}

	/**
	 * Gets the current filter in product brands.
	 * @return Current filter on product brands
	 */
	@Override
	public Set<String> getFilterBrands() {
		FilterStatusProvider provider = findFilterProvider();
		if (provider != null) {
			return provider.getFilterBrands();
		}
		return null;
	}

	/**
	 * Gets all of the available product brands.
	 * @return All product brands
	 */
	@Override
	public Set<String> getAllBrands() {
		FilterStatusProvider provider = findFilterProvider();
		if (provider != null) {
			return provider.getAllBrands();
		}
		return null;
	}

	/**
	 * Sets the current brands filter.
	 * @param value New list of products in filter
	 */
	@Override
	public void setFilterBrands(Set<String> value) {
		FilterStatusProvider provider = findFilterProvider();
		if (provider != null) {
			provider.setFilterBrands(value);
		}
	}

	/**
	 * Finds a page to marshal filter provider requests.
	 * @return Found provider page or null if none
	 */
	private FilterStatusProvider findFilterProvider() {
		// Find the page
		for (int index = attachedPages.size() - 1; index >= 0; --index) {
			BasePage checkPage = attachedPages.get(index);
			if (checkPage instanceof FilterStatusProvider) {
				return (FilterStatusProvider) checkPage;
			}
		}

		// Not found
		Log.w(LOG_TAG, "Filter provider page not found");
		return null;
	}

	/**
	 * Handles update to the passed product.
	 * @param updated Updated product
	 * @param scan Updated (or added) product scan
	 */
	@Override
	public void onProductUpdated(ProductStatus updated, Scan scan) {
		// Find the page
		for (int index = attachedPages.size() - 1; index >= 0; --index) {
			BasePage checkPage = attachedPages.get(index);
			if (checkPage instanceof UpdateProductPage.UpdateProductListener) {
				((UpdateProductPage.UpdateProductListener) checkPage).onProductUpdated(updated, scan);
			}
		}

		// Not found
		Log.w(LOG_TAG, "Update product listener not found");
	}

	/**
	 * Handles confirmation that the user wants to audit a store.
	 *
	 * @param store Provides the store that the user wants to audit
	 */
	@Override
	public void onConfirmStoreAudit(Store store) {
		int pageCount = attachedPages.size();
		if (pageCount == 0) {
			// Nothing to do
			return;
		}
		BasePage checkPage = attachedPages.get(pageCount - 1);
		if (checkPage instanceof BeginAuditDialog.Listener) {
			((BeginAuditDialog.Listener) checkPage).onConfirmStoreAudit(store);
		}
	}

	/**
	 * Sets the activity view state.
	 * @param isShowing Activity view showing state
	 */
	public void setActivity(boolean isShowing) {
		activityView.setVisibility(isShowing ? View.VISIBLE : View.GONE);
	}

	/**
	 * Requests sync from the main menu.
	 */
	@Override
	public void requestSync() {
		syncRequested = true;
		popTo(MainMenuPage.class);
	}

	/**
	 * Indicates if sync has been requested, and resets the request.
	 * @return Sync requested flag
	 */
	@Override
	public boolean isSyncRequested() {
		boolean res = syncRequested;
		syncRequested = false;
		return res;
	}

	/**
	 * Provides the GPS location listener for the entire activity.
	 */
	private LocationListener gpsListener = new LocationListener() {
		@Override
		public void onLocationChanged(Location location) {
			lastLocation = location;
		}

		@Override public void onStatusChanged(String s, int i, Bundle bundle) { }
		@Override public void onProviderEnabled(String s) { }
		@Override public void onProviderDisabled(String s) { }
	};

	/**
	 * Provides the network location listener for the entire activity.
	 */
	private LocationListener netListener = new LocationListener() {
		@Override
		public void onLocationChanged(Location location) {
			if ((lastLocation == null) ||
				!lastLocation.getProvider().equals(LocationManager.GPS_PROVIDER) ||
				(SystemClock.elapsedRealtimeNanos() - lastLocation.getElapsedRealtimeNanos() > 6e+10 * 5)) { // 5 minutes
				lastLocation = location;
			}
		}

		@Override public void onStatusChanged(String s, int i, Bundle bundle) { }
		@Override public void onProviderEnabled(String s) { }
		@Override public void onProviderDisabled(String s) { }
	};
}
