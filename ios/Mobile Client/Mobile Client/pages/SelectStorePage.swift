//
//  SelectStorePage.swift
//  Mobile Client
//
//  Created by Eric Ruck on 3/21/18.
//  Copyright 2018-2019 AuditPro. All rights reserved.
//

import UIKit


/**
 * Manages a fragment to display the select store page.
 * @author Eric Ruck
 */
class SelectStorePage : BasePage, UITableViewDelegate, UITableViewDataSource, UITextFieldDelegate {

	@IBOutlet var chainButton: UIButton?
	@IBOutlet var gpsButton: UIButton?
	@IBOutlet var lastStoreButton: UIButton?
	@IBOutlet var nameButton: UIButton?
	@IBOutlet var storeList: UITableView?
	@IBOutlet var searchEdit: UITextField?

	/** Options for sorting stores on the page. */
	private enum SortOption { case gps, lastStore, name }

	/** Current sort option. */
	private var currentSortOption: SortOption = .name

	/** Current sort order. */
	private var currentSortDirection: Bool = true

	/** Option for filtering stores by chain. */
	private var currentChain: Chain? = nil

	/** Stores to show within the current filter. */
	private var showStores: [Store]? = nil;

	/** Provides all store options for the user. */
	private var allStores: [Store]? = nil


	/**
	 * Initializes the controller when the view loads.
	 */
	override func viewDidLoad() {
		super.viewDidLoad()

		// Get all the stores
		let db = StoresDatabase()
		if let allStores = db?.getStores() {
			self.allStores = allStores
		} else {
			self.allStores = [Store]()
		}

		// Determine reasonable initial state
		if (AppDelegate.lastLocation != nil) {
			currentSortOption = .gps;
		} else if (Security.getLastAuditPos() != nil) {
			currentSortOption = .lastStore
		} else {
			currentSortOption = .name
		}

		// Complete initializing the store
		updateFilter()
	}

	/**
	 * Provides a title for the action bar.
	 * @param context Application context
	 * @return Action bar title
	 */
	override var pageName: String? {
		return "Audit Store"
	}

	/**
	 * Turns on location to sort stores by proximity
	 */
	override var isLocationNeeded: BasePage.LocationNeed {
		return .on
	}

	/**
	 * Gets the number of rows to display in the store list.
	 * @param tableView References our table
	 * @param section Section whose row count we want
	 * @return Row count
	 */
	func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
		return showStores?.count ?? 0
	}

	/**
	 * Gets a cell to display a store.
	 */
	func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
		// Get the cell to use
		var cell = tableView.dequeueReusableCell(withIdentifier: "store")
		if (cell == nil) {
			cell = (Bundle.main.loadNibNamed("SelectStoreTableCell", owner: nil, options: nil)![0] as! UITableViewCell)
		}

		// Populate the cell
		(cell!.contentView.subviews[0] as! UILabel).text =
			showStores![indexPath.row].storeName

		// Provide the cell
		return cell!
	}

	/**
	 * Handles store selection from list.
	 */
	func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
		// Unselect row
		tableView.deselectRow(at: indexPath, animated: true)

		// Verify selection with user
		let store = showStores![indexPath.row]
		let dialog = BeginAuditDialog(store: store, review: false) { store in
			// Was the audit canceled?
			if (store == nil) {
				// Canceled
				return
			}

			// Get initial audit location
			var currentLat: Double? = nil
			var currentLon: Double? = nil
			if let location = AppDelegate.lastLocation {
				currentLat = location.coordinate.latitude
				currentLon = location.coordinate.longitude
			}

			// Begin a new audit
			let db = AuditDatabase()
			guard let audit = db?.startAudit(userId: Security.userId,
				storeId: store!.storeId, storeDescr: store!.description,
				auditTypeId: AuditType.STANDARD.id,
				latitude: currentLat, longitude: currentLon)
			else {
				self.view.makeToast(message: "Failed to start audit, please contact customer support.")
				return
			}

			// Record the location for the next time
			if (store!.isGeocoded) {
				Security.setLastAuditPos(latitude: store!.storeLat, longitude: store!.storeLon)
			} else {
				Security.setLastAuditPos(latitude: currentLat, longitude: currentLon)
			}

			// Show the products page
			self.host!.swapPage(SelectProductPage(audit: audit))
		}

		// Display the dialog
		host!.showDialog(dialog)
	}

	/**
	 * Handles tap on the chain button.
	 */
	@IBAction func onChain() {
		// Determine the chain options to display
		host!.pushPage(SelectChainPage(onChain: { chain in
			self.currentChain = chain
			self.chainButton?.setTitle(chain?.chainName ?? "ALL CHAINS", for: .normal)
			self.updateFilter()
		}))
	}

	/**
	 * Handles tap on the GPS button.
	 */
	@IBAction func onGps() {
		onSortOption(.gps)
	}

	/**
	 * Handles tap on the sort option button.
	 */
	@IBAction func onLastStore() {
		onSortOption(.lastStore)
	}

	/**
	 * Handles tap on the name button.
	 */
	@IBAction func onName() {
		onSortOption(.name)
	}

	/**
	 * Handles change to the search text.
	 */
	@IBAction func onSearchChanged() {
		updateFilter()
	}

	/**
	 * Dismisses keyboard on return.
	 * @param textField Field with focus
	 * @return Handled flag
	 */
	func textFieldShouldReturn(_ textField: UITextField) -> Bool {
		textField.resignFirstResponder()
		return true
	}

	/**
	 * Updates the current sort option.
	 * @param value New sort option
	 */
	private func onSortOption(_ option: SortOption) {
		let lastLocation = AppDelegate.lastLocation
		if (option == currentSortOption) {
			// Reverse direction
			currentSortDirection = !currentSortDirection
		} else if ((option == .gps) && (lastLocation == nil)) {
			// GPS unavailable
			view.makeToast(message: "GPS coordinates not available.")
			return
		} else if ((option == .lastStore) && (Security.getLastAuditPos() == nil)) {
			// No last audit position
			view.makeToast(message: "There is no last store recorded.")
			return
		} else {
			// Select option
			currentSortOption = option
			currentSortDirection = true
		}

		// Display the current sort
		updateSort()
	}

	/**
	 * Updates the list to reflect the stores within the current filters.
	 */
	private func updateFilter() {
		// Check for search text tokens
		let searchText = searchEdit?.text ?? ""
		var tokens = searchText.trimmingCharacters(in: .whitespaces).isEmpty
			? nil
			: searchText.lowercased().components(separatedBy: " ").filter() { token in return !token.isEmpty }
		if (tokens?.count ?? 0 == 0) {
			tokens = nil
		}

		// Is there a filter?
		if ((tokens == nil) && (currentChain == nil)) {
			// No filter
			showStores = allStores
		} else {
			// Determine the stores within the filter
			showStores = allStores?.filter({ store in
				// Test the chain
				if (currentChain != nil) && (store.chainId != currentChain!.chainId) {
					return false
				}
				if (tokens != nil) {
					// Test the tokens
					for token in tokens! {
						if !store.storeName.lowercased().contains(token) {
							return false
						}
					}
				}
				return true
			})
		}

		// Reassert sort
		updateSort();
	}

	/**
	 * Updates the list adapter to set the current sort.
	 */
	private func updateSort() {
		// Update visuals
		updateSortButtons()

		// Verify thart we can use the current sort option
		var useSortOption = currentSortOption
		let storeGeo = Security.getLastAuditPos()
		let lastLocation = AppDelegate.lastLocation
		if lastLocation == nil && useSortOption == .gps {
			// Fall back on name
			NSLog("Store sort by GPS selected but no location available")
			useSortOption = .name
		} else if storeGeo == nil && useSortOption == .lastStore {
			// Fall back on name
			NSLog("Store sort by last store but no last store available")
			useSortOption = .name
		}

		// Are we sorting by name?
		if (useSortOption == .name) {
			// Easy case, compare by name
			showStores?.sort(by: { store1, store2 in
				let res = store1.storeName < store2.storeName
				return currentSortDirection ? res : !res
			})
			storeList?.reloadData()
			return
		}

		// Compare by geocode
		let sourceLat = (useSortOption == .gps) ? lastLocation!.coordinate.latitude  : storeGeo!.0
		let sourceLon = (useSortOption == .gps) ? lastLocation!.coordinate.longitude : storeGeo!.1

		// Precalculate comparison source
		let rlatSource = Double.pi * sourceLat / 180
		let srlatSource = sin(rlatSource)
		let crlatSource = cos(rlatSource)

		// Apply sort comparator
		showStores?.sort(by: { store1, store2 in
			// Trivial case, check geocoding
			if (!store1.isGeocoded) {
				if (store2.isGeocoded) {
					return currentSortDirection
				}

				// Fall back on name
				let res = store1.storeName < store2.storeName
				return currentSortDirection ? res : !res
			} else if (!store2.isGeocoded) {
				return !currentSortDirection
			}

			// Compare distances from source
			let rlatX = Double.pi * store1.storeLat! / 180
			let rlatY = Double.pi * store2.storeLat! / 180
			let thetaX = sourceLon - store1.storeLon!
			let rthetaX = Double.pi * thetaX / 180
			let thetaY = sourceLon - store2.storeLon!
			let rthetaY = Double.pi * thetaY / 180
			let distXCos = srlatSource * sin(rlatX) + crlatSource * cos(rlatX) * cos(rthetaX)
			let distX = acos(distXCos)
			let distYCos = srlatSource * sin(rlatY) + crlatSource * cos(rlatY) * cos(rthetaY)
			let distY = acos(distYCos)
			let res = (distX == distY)
				? store1.storeName < store2.storeName
				: distX < distY
			return currentSortDirection ? res : !res
		})
		storeList?.reloadData()
	}

	/**
	 * Updates the appearance of all the sort buttons to match the current state.
	 */
	private func updateSortButtons() {
		selectSort(button: gpsButton!, isSelected: currentSortOption == .gps)
		selectSort(button: lastStoreButton!, isSelected: currentSortOption == .lastStore)
		selectSort(button: nameButton!, isSelected: currentSortOption == .name)
	}

	/**
	 * Updates the selected state of the passed sort button.
	 * @param button Button to update
	 * @param isSelected Selected state
	 */
	private func selectSort(button: UIButton, isSelected: Bool) {
		button.setTitleColor(isSelected ? .red : .black, for: .normal)
	}
}
