//
//  SelectProductPage.swift
//  Mobile Client
//
//  Created by Eric Ruck on 3/21/18.
//  Copyright 2018-2019 AuditPro. All rights reserved.
//

import Foundation
import UIKit


/**
 * Manages the page to select products during an audit.
 * @author Eric Ruck
 */
class SelectProductPage : BasePage, UITableViewDataSource, UITableViewDelegate, UITextFieldDelegate, UIImagePickerControllerDelegate, UINavigationControllerDelegate {
	@IBOutlet var filterButton: UIButton?
	@IBOutlet var brandsButton: UIButton?
	@IBOutlet var searchEdit: UITextField?
	@IBOutlet var productList: UITableView?
    
    @IBOutlet var cameraReportView: UIView!
    @IBOutlet var internalNotes: UITextView!
    @IBOutlet var externalNotes: UITextView!
    @IBOutlet var previewImage: UIImageView!
    @IBOutlet var thumbDownButton: CustomButton!
    @IBOutlet var deleteButton: CustomButton!
    @IBOutlet var cameraButton: CustomButton!
    @IBOutlet var thumbUpButton: CustomButton!
    
	private var audit: Audit? = nil
	private var allProducts: [ProductStatus] = [ProductStatus]()
	private var allProductTypes: [String] = [String]()
	private var filterReorderStatus: [ReorderStatus]? = nil
	private var filterProductTypes: [String]? = nil
	private var filteredProducts: [ProductStatus]? = nil
	private var allBrands: [String]? = nil
	private var filterBrands: Set<String>? = nil
	private var isInModal: Bool = false
	private var menuView: UIView? = nil
	private var isSetUnscannedEnabled = false
	private var battery: BatteryIcon?
	private var batteryTimer: Timer?

	private enum BarcodeState {
		case unchanged
		case none
		case connecting
		case connected
		case disconnected
	}

	private var barcodeReader: KDCReader? = nil
	private var barcodeState: BarcodeState = BarcodeState.none
	private var isBarcodeNotified = false


	/**
	 * Initializes to update the products in the passed audit.
	 * @param audit Audit to update
	 */
	convenience init(audit: Audit) {
		self.init()
		self.audit = audit
	}

	/**
	 * Completes initialization on view load.
	 */
	override func viewDidLoad() {
		super.viewDidLoad()

		// Load product data
		let dbStores = StoresDatabase()
		var loaded = dbStores?.getProductsForStore(storeId: audit!.storeId)
		loaded?.sort(by: { prod0, prod1 in
			return prod0.productName < prod1.productName
		})

		// Convert to product status
		var uniqueTypes = Set<String>()
		var uniqueBrands = Set<String>()
		loaded?.forEach({ product in
			allProducts.append(ProductStatus(product: product))
			if let productTypeName = product.productTypeName {
				uniqueTypes.insert(productTypeName)
			}
			if product.displayBrandName.count > 0 {
				uniqueBrands.insert(product.displayBrandName)
			}
		})

		// Apply any existing reports to the product status
		let dbAudit = AuditDatabase()
		let reports = dbAudit?.getAllReports(audit: audit!, products: nil)
		reports?.forEach() { report in
			for ps in allProducts {
				if (ps.product.id == report.productId) {
					let scan: Scan? =
						(report.reorderStatusId == ReorderStatus.IN_STOCK.id) ||
						(report.reorderStatusId == ReorderStatus.OUT_OF_STOCK.id)
						? dbAudit?.getScan(audit: audit!, productId: ps.product.id)
						: nil
					ps.setReorderStatus(fromReport: report, scan: scan)
					break
				}
			}
		}

		// Setup product types
		allProductTypes = [String](uniqueTypes)
		allBrands = uniqueBrands.sorted()
		if allBrands!.count < 2 {
			// Not enough brands to worry about filtering
			brandsButton?.isHidden = true
		}
        
        // Setup camera button
        cameraButton.titleLabel?.font = UIFont.fontAwesome(ofSize: 20, style: .solid)
        cameraButton.setTitle(String.fontAwesomeIcon(name: .camera), for: .normal)
        
        // Setup camera report view
        internalNotes?.layer.borderColor = UIColor.black.cgColor
        internalNotes?.layer.borderWidth = 1.0
        internalNotes?.layer.cornerRadius = 5.0
        externalNotes?.layer.borderColor = UIColor.black.cgColor
        externalNotes?.layer.borderWidth = 1.0
        externalNotes?.layer.cornerRadius = 5.0
        previewImage.layer.cornerRadius = 5.0
        
        deleteButton.backgroundColor = UIColor(displayP3Red: 201/255, green: 45/255, blue: 57/255, alpha: 1.0)
        thumbDownButton.backgroundColor = UIColor(displayP3Red: 201/255, green: 45/255, blue: 57/255, alpha: 1.0)
        thumbDownButton.titleLabel?.font = UIFont.fontAwesome(ofSize: 30, style: .solid)
        thumbDownButton.setTitle(String.fontAwesomeIcon(name: .frown), for: .normal)
        thumbDownButton.setTitleColor(.white, for: .normal)
        thumbUpButton.titleLabel?.font = UIFont.fontAwesome(ofSize: 30, style: .solid)
        thumbUpButton.setTitle(String.fontAwesomeIcon(name: .smile), for: .normal)
        thumbUpButton.setTitleColor(UIColor.white, for: .normal)
        
        
        let window = UIApplication.shared.keyWindow!
        cameraReportView.frame = CGRect(x: 0, y: 1000, width: window.frame.width, height: window.frame.height)
        window.addSubview(cameraReportView)
        
	}

	/**
	 * Cleanup when our view is being removed.
	 * @param animated Animated removal flag
	 */
	override func viewDidDisappear(_ animated: Bool) {
		super.viewDidDisappear(animated)

		// Cleanup state
		isInModal = false

		// Make sure we release the barcode scanner
		disconnectBarcode()
		if (isBarcodeNotified) {
			// Disconnect scanner notifications
			NotificationCenter.default.removeObserver(self,
				name: NSNotification.Name.kdcConnectionChanged, object: nil)
			NotificationCenter.default.removeObserver(self,
				name: NSNotification.Name.kdcBarcodeDataArrived, object: nil)
			isBarcodeNotified = false
		}
	}

	/**
	 * Returns our page display name.
	 * @return Page display name
	 */
	 override var pageName: String? {
		return "Select Product"
	 }

	/**
	 * Initiates scanned connection when the page appears.
	 */
	override func onPageAppearing() {
		super.onPageAppearing()
		connectBarcode()
	}

	/**
	 * Formally disconnects the barcode scanner when the application backgrounds.
	 */
	override func onDidEnterBackground() {
		disconnectBarcode()
	}

	/**
	 * Reconnects to the barcode scanner when the application restores from
	 * the background.
	 */
	override func onDidBecomeActive() {
		connectBarcode()
	}

	/**
	 * Indicates that our view should resize around the keyboard.
	 * @return Resize flag
	 */
	override var shrinkForKeyboard: Bool {
		return true;
	}

	/**
	 * Handles tap on the back navigation by confirming that the user wants
	 * to exit the audit without completing it.
	 */
	override func onBack() -> Bool {
		// Prepare the confirmation alert
		let alert = UIAlertController(title: "Exit Audit",
			message: "Are you sure you want to exit this audit without completing it? " +
				"You can resume this audit at a later time, but you cannot start " +
				"a new one until this one is completed.",
			preferredStyle: .alert)
		alert.addAction(UIAlertAction(title: "No", style: .cancel) { action in
			self.isInModal = false
		})
		alert.addAction(UIAlertAction(title: "Yes", style: .default) { action in
			self.isInModal = false
			self.host!.popPage()
		})

		// Show the alert
		isInModal = true
		present(alert, animated: true)

		// Don't go back yet
		return true
	}

	/**
	 * Displays our menu in the status bar.
	 * @return Our menu view
	 */
	override func onCreateMenu() -> UIView? {
		// Have we loaded our menu view?
		if (menuView == nil) {
			// No, load it now
			menuView = Bundle.main.loadNibNamed("SelectProductPageMenu",
				owner: nil, options: nil)![0] as? UIView

			// Initialize loaded view
			setBarcodeState(.unchanged)
			if let barcodeButton = menuView?.viewWithTag(1) as? UIButton {
				barcodeButton.addTarget(self, action: #selector(connectBarcode), for: .touchUpInside)
			}
			if let menuButton = menuView?.viewWithTag(2) as? UIButton {
				menuButton.addTarget(self,
					action: #selector(onMenu), for: .touchUpInside)
			}

			// Determine set unscanned enabled state
			isSetUnscannedEnabled = false
			for test in allProducts {
				if test.reorderStatus.id == ReorderStatus.NONE.id {
					// Found unscanned
					isSetUnscannedEnabled = true
					break
				}
			}
			if let batteryButton = menuView?.viewWithTag(3) as? UIButton {
				// Setup the battery
				battery = BatteryIcon(button: batteryButton, for: "Scanner")
				batteryButton.addTarget(self, action: #selector(onScannerBattery), for: .touchUpInside)
			}
		}

		// Provide back the menu
		return menuView
	}

	/**
	 * Handles tap on the notes button.
	 */
	@IBAction func onNotes() {
		host!.pushPage(NotesPage(audit: audit!))
	}

	/**
	 * Shows the audit complete interface.
	 */
	@IBAction func onComplete() {
		disconnectBarcode()
		host?.pushPage(CompletePage(audit: audit!))
	}

	/**
	 * Handles tap on the filter button.
	 */
	@IBAction func onFilter() {
		host?.pushPage(FilterStatusPage(filterReorderStatus: filterReorderStatus,
			allProductTypes: allProductTypes, filterProductTypes: filterProductTypes)
		{ reorderStatuses, productTypes in
			// Check for same as null, no filter
			let reorderStatusesCount = reorderStatuses?.count ?? 0
			let productTypesCount = productTypes?.count ?? 0
			var isSame =
				(reorderStatusesCount == (self.filterReorderStatus?.count ?? 0)) &&
				(productTypesCount == (self.filterProductTypes?.count ?? 0))
			reorderStatuses?.forEach() { test in
				if !(self.filterReorderStatus?.contains(test) ?? false) {
					isSame = false
				}
			}
			productTypes?.forEach() { test in
				if !(self.filterProductTypes?.contains(test) ?? false) {
					isSame = false
				}
			}
			if (!isSame) {
				// Update the filter
				self.filterReorderStatus = reorderStatusesCount > 0 ? reorderStatuses : nil
				self.filterProductTypes = productTypesCount > 0 ? productTypes : nil
				self.applyFilter()
			}
		})
	}

	/**
	 * Handles a tap on the brands button.
	 */
	@IBAction func onBrands() {
		host?.pushPage(FilterBrandPage(allBrands: allBrands!, filteredBrands: filterBrands) { updateFilter in
			if (self.filterBrands != updateFilter) {
				// Update the filter
				self.filterBrands = updateFilter
				self.applyFilter()
			}
		})
	}
    
    /**
    * Handles change to the camera button.
    */
    @IBAction func onCamera(_ sender: Any) {
        showAlert()
    }
    
    @IBAction func onCancel(_ sender: Any) {
        
        UIView.animate(withDuration: 0.5) {
            let window = UIApplication.shared.keyWindow!
            self.cameraReportView.frame = CGRect(x: 0, y: 1000, width: window.frame.width, height: window.frame.height)
        }
        
    }
    

	/**
	 * Handles change to the search text.
	 */
	@IBAction func onSearchChanged() {
		applyFilter()
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
	 * Informs the table how many products to display
     * @param tableView Table whose row count is wanted
	 * @param section Section within table
	 * @return Row count in section
	 */
	func tableView(_ tableView: UITableView, numberOfRowsInSection section: Int) -> Int {
		// Only one table and section
		return filteredProducts?.count ?? allProducts.count
	}

	/**
	 * Provides a cell view for a table row.
	 * @param tableView Table who needs row view
	 * @param indexPath Path of needed row
	 * @return Row view
	 */
	func tableView(_ tableView: UITableView, cellForRowAt indexPath: IndexPath) -> UITableViewCell {
		// Get the cell to use
		var cell = tableView.dequeueReusableCell(withIdentifier: "product")
		if (cell == nil) {
			// Need to create a new one
			cell = Bundle.main.loadNibNamed("SelectProductTableCell", owner: nil, options: nil)?[0] as? UITableViewCell
			(cell?.viewWithTag(2) as? UIButton)?.addTarget(self, action: #selector(onItemReorderStatus(sender:)), for: .touchUpInside)
		}

		// Get the backing product status
		let formatter = NumberFormatter()
		formatter.numberStyle = .currency
		let productStatus = (filteredProducts ?? allProducts)[indexPath.row]
		let statusText =
			(productStatus.reorderStatus == ReorderStatus.IN_STOCK || productStatus.reorderStatus == ReorderStatus.OUT_OF_STOCK)
				&& productStatus.hasDisplayPrice
			? String(format: "%@ %@", productStatus.reorderStatus.code, formatter.string(from: NSNumber(value: productStatus.displayPrice!))!)
			: productStatus.reorderStatus.code

		// Populate the cell
		(cell?.viewWithTag(1) as? UILabel)?.text = productStatus.description
		(cell?.viewWithTag(2) as? UIButton)?.setTitle(statusText, for: .normal)

		// Return the populated cell
		return cell!
	}

	/**
	 * Handles product row selection.
	 * @param tableView Table in which row was selected
	 * @param indexPath Path of selected row
	 */
	func tableView(_ tableView: UITableView, didSelectRowAt indexPath: IndexPath) {
		tableView.deselectRow(at: indexPath, animated: true)
		let productStatus = (filteredProducts ?? allProducts)[indexPath.row]
		selectProduct(productStatus)
	}

	/**
	 * Handles tap on any item reorder status button.
	 * @param sender Instance of tapped button
	 */
	@objc func onItemReorderStatus(sender: UIButton) {
		// We need to find the product based on the button
		let listProducts = filteredProducts ?? allProducts
		guard
			let path = productList?.indexPath(for: sender.superview?.superview as! UITableViewCell),
			path.row < listProducts.count
		else {
			view.makeToast(message: "Internal error, unable to find product for status button")
			return
		}

		// Offer the reorder options to the user
		isInModal = true;
		let update = (filteredProducts ?? allProducts)[path.row]
		let inStockRequiresScan =
			!update.product.randomWeight &&
			(update.reorderStatus.id != ReorderStatus.IN_STOCK.id) &&
			Security.optSetting(name: Security.SETTING_IN_STOCK_REQUIRES_SCAN, defaultValue: false)
		let alert = UIAlertController(title: "Select a Reorder Status", message: nil, preferredStyle: .actionSheet)
		ReorderStatus.Statuses.forEach { status in
			if (status.isValid && (!inStockRequiresScan || (status.id != ReorderStatus.IN_STOCK.id))) {
				alert.addAction(UIAlertAction(title: status.name, style: .default) { action in
					// Process selection
					self.isInModal = false
					self.updateProductReorderStatus(sender, update, status)
				})
			}
		}
		alert.addAction(UIAlertAction(title: "Cancel", style: .cancel) { alert in
			self.isInModal = false
		})
		present(alert, animated: true)
	}


	/**
	 * Updates the reorder status of the passed product in the database, the local model and the UI.
	 * @param button Button associated with product
	 * @param update Product in the local model to update
	 * @param selectedStatus Selected status to update
	 */
	private func updateProductReorderStatus(_ button: UIButton, _ update: ProductStatus, _ selectedStatus: ReorderStatus) {
		// Special case if in stock is selected
		if (selectedStatus.id == ReorderStatus.IN_STOCK.id) {
			selectProduct(update, initReorderStatus: selectedStatus)
			return
		}

		// Check for existing record to update
		guard let db = AuditDatabase() else {
			view.makeToast(message: "Unable to update product reorder status")
			return
		}
		var report = db.getReport(audit: audit!, productId: update.product.id)
		if (report != nil) {
			// Update existing report
			report = Report(source: report!, scan: nil, reorderStatusId: selectedStatus.id)
		} else {
			// Create new report
			report = Report(audit: audit!, product: update.product, reorderStatusId: selectedStatus.id)
		}

		// Update the record
		if (!db.updateReport(update: report!)) {
			view.makeToast(message: "Unable to update product reorder status")
			return
		}

		// Do we need to get the scan?
		var scan: Scan?
		if (selectedStatus == ReorderStatus.OUT_OF_STOCK) {
			scan = db.getScan(audit: audit!, productId: update.product.id)
		}

		// Update the data model
		update.setReorderStatus(fromReport: report, scan: scan)

		// Is the updated status in the filter?
		let isInFilter =
			(filterReorderStatus == nil) ||
			(filterReorderStatus!.first(where: { status in
				return update.reorderStatus.id == status.id
			}) != nil)
		if (isInFilter) {
			// Update the filter status on the button
			let displayStatus = update.reorderStatus
			var statusName = displayStatus.code
			if (((displayStatus == ReorderStatus.IN_STOCK) || (displayStatus == ReorderStatus.OUT_OF_STOCK)) && update.hasDisplayPrice) {
				// Append display price
				let formatter = NumberFormatter()
				formatter.locale = Locale.current
				formatter.numberStyle = .currency
				let currency = formatter.string(from: NSNumber(floatLiteral: update.displayPrice!))
				statusName += String(format:" %s", currency!)
			}
			UIView.performWithoutAnimation {
				button.setTitle(statusName, for: .normal)
				button.layoutIfNeeded()
			}
		} else {
			// New status filtered out of list
			if let index = filteredProducts?.firstIndex(of: update) {
				filteredProducts?.remove(at: index)
				productList?.reloadData()
			}
		}
	}

	/**
	 * Applies the current filter as the user types, or performs any action that might change
	 * the filter.
	 */
	private func applyFilter() {
		// Make sure the reorder status filter button text is up to date
		UIView.performWithoutAnimation {
			if (filterReorderStatus != nil) {
				let title = (filterProductTypes == nil)
					? "FILTER STATUS"
					: "FILTER STATUS, TYPE"
				filterButton?.setTitle(title, for: .normal)
			} else {
				let title = (filterProductTypes == nil)
					? "FILTER ALL"
					: "FILTER TYPE"
				filterButton?.setTitle(title, for: .normal)
			}
			filterButton?.layoutIfNeeded()
			brandsButton?.setTitle(filterBrands == nil ? "ALL BRANDS" : "FILTER BRANDS", for: .normal)
			brandsButton?.layoutIfNeeded()
		}

		// Check for trivial case, no filter
		let searchText = searchEdit?.text ?? ""
		var tokens = searchText.trimmingCharacters(in: .whitespaces).isEmpty
			? nil
			: searchText.components(separatedBy: " ").filter() { token in return !token.isEmpty }
		if (tokens?.count ?? 0 == 0) {
			tokens = nil
		}
		if ((tokens == nil) && (filterReorderStatus == nil) && (filterProductTypes == nil) && (filterBrands == nil)) {
			// Simple case, all products
			filteredProducts = nil
		} else {
			// Filter the products
			filteredProducts = [ProductStatus]()
			for product in allProducts {
				// Is there a reorder status filter?
				if (filterReorderStatus != nil) {
					// Check if the product is within this filter
					var isInFilter = false
					for status in filterReorderStatus! {
						if (status.id == product.reorderStatus.id) {
							isInFilter = true
							break
						}
					}
					if (!isInFilter) {
						continue
					}
				}

				// Is there a product type filter?
				if (filterProductTypes != nil) {
					// Check if the product is within this filter
					if (!filterProductTypes!.contains(product.productType ?? "")) {
						// Not in product type filter
						continue;
					}
				}

				// Is there a brand filter?
				if (filterBrands != nil) {
					// Check if the product is within this filter
					if (!filterBrands!.contains(product.product.displayBrandName)) {
						// Not in filter
						continue;
					}
				}

				// Are there search tokens?
				if (tokens != nil) {
					// Check all the tokens in the product
					var isInFilter = true
					for token in tokens! {
						if (!product.product.hasToken(token)) {
							isInFilter = false
							break
						}
					}
					if (!isInFilter) {
						continue
					}
				}

				// The product is within the filter
				filteredProducts?.append(product)
			}

			// Apply name sort
			filteredProducts?.sort(by: { ps1, ps2 in
				return ps1.description < ps2.description
			})
		}

		// Display the filtered list
		productList?.reloadData()
	}

	/**
	 * Displays the update screen for the selected product.
	 * @param productStatus Selected product details
	 * @param rawScan Raw input from scanner or nil if not scanned
	 * @param initReorderStatus Initial reorder status override
	 */
	private func selectProduct(_ productStatus: ProductStatus, forScan rawScan: String? = nil,
			initReorderStatus: ReorderStatus? = nil) {
		host!.pushPage(UpdateProductPage(audit: audit!, productStatus: productStatus,
				rawScan: rawScan, initReorderStatus: initReorderStatus) { updatedStatus in
			self.onProductUpdated(updatedStatus)
		})
	}

	/**
	 * Handles update to the passed product.
	 * @param updated Updated product
	 */
	private func onProductUpdated(_ updated: ProductStatus) {
		// Get the updated row
		if let index = (filteredProducts ?? allProducts).firstIndex(where: { test in test.product.id == updated.product.id }) {
			// Check to make sure the product is still in the filter
			if (!(filterReorderStatus?.contains(where: { test in test.id == updated.reorderStatus.id }) ?? true)) {
				// The product is now outside the filter, remove
				filteredProducts!.remove(at: index)
				productList?.reloadData()
			} else {
				// Make sure the status display is up to date
				productList?.reloadRows(at: [IndexPath(row: index, section: 0)], with: .none)
			}
		}
	}

	/**
	 * Handles change in connection state with the scanner.
	 * @param notification Notification details
	 */
	@objc func connectionChanged(notification: NSNotification) {
		setBarcodeState((barcodeReader?.isConnected() ?? false)
			? .connected
			: .disconnected
		)
	}

	/**
	 * Handles receipt of barcode from scanner.
	 * @param notification Notification details
	 */
	@objc func barcodeDataReceived(notification: NSNotification) {
		// Are we interested in the barcode data now?
		if (isInModal || !(host?.isTopPage(self) ?? false)) {
			// Ignore this barcode
			return
		}

		// Get the received data
		guard let barcode = barcodeReader?.getBarcodeData() else {
			// No data from barcode reader
			NSLog("No data received from barcode reader on data notification")
			return
		}

		// Is the battery UI hooked up?
		if let battery = battery,
		 	let level = barcodeReader?.getBatteryLevel() {
			// While we're here update the battery
			battery.updatePercent(Int(level))
		}

		// Find the product
		for productStatus in allProducts {
			if (productStatus.product.upc == barcode) {
				// Found, show the update screen
				selectProduct(productStatus, forScan: barcode)
				return
			}
		}

		// Product not found
		view.makeToast(message: String(format: "Record for UPC %@ Not Found", barcode))
	}

	/**
	 * Attempts to connect to the barcode reader.
	 */
	@objc private func connectBarcode() {
		if ((barcodeState == BarcodeState.connecting) || (barcodeState == BarcodeState.connected)) {
			// Already connected, more or less
			return
		}

		// Have we setup barcode notifications?
		if (!isBarcodeNotified) {
			// Setup to receive scanner notifications
			NotificationCenter.default.addObserver(self,
				selector: #selector(connectionChanged(notification:)),
				name: NSNotification.Name.kdcConnectionChanged, object: nil)
			NotificationCenter.default.addObserver(self,
				selector: #selector(barcodeDataReceived(notification:)),
				name: NSNotification.Name.kdcBarcodeDataArrived, object: nil)
			isBarcodeNotified = true
		}

		// Are we paired with a reader?
		disconnectBarcode()
		let reader = KDCReader()
		if (reader.getAvailableDeviceList().count == 0) {
			// No barcode reader paired
			setBarcodeState(.none)
			return
		}

		// We're connecting
		barcodeReader = reader
		barcodeReader?.connect()
		setBarcodeState(.connecting)

		// Record/initialize timeouts
		let initScanTimeout = reader.getScanTimeout()
		let initSleepTimeout = reader.getSleepTimeout()
		NSLog("Koamtac initial scan timeout: %@, sleep timeout: %@",
			String(describing: initScanTimeout), String(describing: initSleepTimeout))
		reader.setSleepTimeout(.SLEEP_TIMEOUT_DISABLED)
	}

	/**
	 * Shows menu options.
	 */
	@objc private func onMenu() {
		let alert = UIAlertController(title: nil, message: nil, preferredStyle: .actionSheet)
		alert.addAction(UIAlertAction(title: "Store History", style: .default) { action in
			if	let hostRef = self.host,
				let db = StoresDatabase(),
				let store = db.getStore(self.audit!.storeId) {
				hostRef.showDialog(BeginAuditDialog(store: store, review: true) { store in
					self.isInModal = false
				})
			} else {
				self.isInModal = false
			}
		})
		if isSetUnscannedEnabled {
			alert.addAction(UIAlertAction(title: "Set Unscanned Items...", style: .default) { action in
				self.onSetUnscanned()
			})
		}
		alert.addAction(UIAlertAction(title: "Cancel", style: .cancel) { action in
			self.isInModal = false
		})
		isInModal = true
		present(alert, animated: true)
	}

	/**
	 * Handles a tap on the scanner battery button by showing the current charge.
	 */
	@objc private func onScannerBattery() {
		if let state = battery?.describeState {
			Analytics.menuAction(type: "Barcode Battery", details: state)
			view.makeToast(message: state)
		}
	}

	/**
	 * Shows the options to set unscanned items.
	 */
	private func onSetUnscanned() {
		// Ask the user how to set unset items
		let alert = UIAlertController(title: "Set Unscanned Items",
			message: "What status would you like to set unscanned items?",
			preferredStyle: .actionSheet)
		alert.addAction(UIAlertAction(title: ReorderStatus.OUT_OF_STOCK.name, style: .default) { action in
			self.isInModal = false
			self.onMenuSetUnscanned(status: ReorderStatus.OUT_OF_STOCK)
		})
		alert.addAction(UIAlertAction(title: ReorderStatus.VOID.name, style: .default) { action in
			self.isInModal = false
			self.onMenuSetUnscanned(status: ReorderStatus.VOID)
		})
		alert.addAction(UIAlertAction(title: "Cancel", style: .cancel) { action in
			self.isInModal = false
		})
		isInModal = true
		present(alert, animated: true)
	}

	/**
	 * Sets the unscanned products to the passed status.
	 * @param status Status to set unscanned items
	 */
	private func onMenuSetUnscanned(status: ReorderStatus) {
		// Show activity
		host?.isActivityShowing = true
		DispatchQueue.global(qos: .background).async {
			// Update the products on the background thread
			let db = AuditDatabase()
			var isAnyUpdated = false
			var errorMessage: String? = nil

			// Cycle through our products
			for product in self.allProducts {
				if (product.reorderStatus.id == ReorderStatus.NONE.id) {
					// Create a report
					let report = Report(audit: self.audit!, product: product.product, reorderStatusId: status.id)
					if db!.updateReport(update: report) {
						product.setReorderStatus(fromReport: report, scan: nil)
						isAnyUpdated = true
						continue
					}

					// Failed to update report
					errorMessage = "Failed to set status in database."
				}
			}
			DispatchQueue.main.async {
				// Complete the UI
				self.host?.isActivityShowing = false
				if (isAnyUpdated) {
					// Refresh the list
					self.applyFilter()
					self.view.makeToast(message: errorMessage ?? "Unscanned products status set.")
				} else {
					// Nothing updated
					self.view.makeToast(message: errorMessage ?? "No unscanned products were found to set.")
				}
				if (errorMessage == nil) {
					self.isSetUnscannedEnabled = false
				}
				self.isInModal = false
			}
		}
	}

	/**
	 * Disconnects from the barcode reader, if we're connected.
	 */
	private func disconnectBarcode() {
		barcodeReader?.disconnect()
		barcodeReader = nil
		setBarcodeState(.disconnected)
	}

	/**
	 * Sets the current barcode state and adjusts the user interface accordingly.
	 * @param state New state, or unchanged pseudo-state
	 */
	private func setBarcodeState(_ state: BarcodeState) {
		// Should we update the state?
		if (state != BarcodeState.unchanged) {
			if (state == barcodeState) {
				// No change
				return;
			}
			barcodeState = state;
		}
		let barcodeMenuItem = menuView?.viewWithTag(1) as? UIButton
		switch (barcodeState) {
			case .none:
				barcodeMenuItem?.isHidden = true
				battery?.isVisible = false
				return
			case .connected:
				barcodeMenuItem?.setBackgroundImage(UIImage(named: "ic_barcode_scan_online"), for: .normal)
				battery?.isVisible = true
				if let level = barcodeReader?.getBatteryLevel() {
					battery?.updatePercent(Int(level))
				}
				batteryTimer = Timer.scheduledTimer(withTimeInterval: 60, repeats: true) { timer in
					if let barcode = self.barcodeReader, let battery = self.battery {
						if (self.barcodeState == .connected) && battery.isUpdateNeeded(afterSeconds: 60) {
							battery.updatePercent(Int(barcode.getBatteryLevel()))
						}
					}
				}
				break
			case .disconnected:
				barcodeMenuItem?.setBackgroundImage(UIImage(named: "ic_barcode_scan_offline"), for: .normal)
				batteryTimer?.invalidate()
				batteryTimer = nil
				break
			case .connecting:
				barcodeMenuItem?.setBackgroundImage(UIImage(named: "ic_barcode_scan_connecting"), for: .normal)
				break
			default:
				// No change
				break
		}
		barcodeMenuItem?.isHidden = false
	}
    
    //Show alert to selected the media source type.
    private func showAlert() {

        let alert = UIAlertController(title: "Photo Source", message: "", preferredStyle: .actionSheet)
        alert.addAction(UIAlertAction(title: "Camera", style: .default, handler: {(action: UIAlertAction) in
            self.getImage(fromSourceType: .camera)
        }))
        alert.addAction(UIAlertAction(title: "Photo Album", style: .default, handler: {(action: UIAlertAction) in
            self.getImage(fromSourceType: .photoLibrary)
        }))
        alert.addAction(UIAlertAction(title: "Cancel", style: .destructive, handler: nil))
        self.present(alert, animated: true, completion: nil)
    }

    //get image from source type
    private func getImage(fromSourceType sourceType: UIImagePickerController.SourceType) {

        //Check is source type available
        if UIImagePickerController.isSourceTypeAvailable(sourceType) {

            let imagePickerController = UIImagePickerController()
            imagePickerController.delegate = self
            imagePickerController.sourceType = sourceType
            self.present(imagePickerController, animated: true, completion: nil)
        }
    }
    
    //MARK:- UIImagePickerViewDelegate.
    func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey : Any]) {

        self.dismiss(animated: true) { [weak self] in

            guard let image = info[UIImagePickerController.InfoKey.originalImage] as? UIImage else { return }
            //Setting image to preview image view
            self?.previewImage.image = image
            
            //Pop up camera report view
            DispatchQueue.main.async {
                UIView.animate(withDuration: 0.5) {
                    let window = UIApplication.shared.keyWindow!
                    self!.cameraReportView.frame = CGRect(x: 0, y: 0, width: window.frame.width, height: window.frame.height)
                }
            }
            
        }
    }

    func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
        picker.dismiss(animated: true, completion: nil)
    }
    
    
    
}

