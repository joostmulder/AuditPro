//
//  UpdateProductPage.swift
//  Mobile Client
//
//  Created by Eric Ruck on 3/29/18.
//  Copyright © 2018 AuditPro. All rights reserved.
//

import UIKit


/**
 * Displays the product details for updating.
 * @author Eric Ruck
 */
class UpdateProductPage : BasePage, UITextFieldDelegate {
	private var audit: Audit?
	private var productStatus: ProductStatus?
	private var rawScan: String?

	private var updateScan: Scan?
	private var updateReport: Report?
	private var initReorderStatus: ReorderStatus?
	private var oldReorderStatus: ReorderStatus?
	private var isAllowedToSetInStock: Bool = false
	private var isSKUConditionsEnabled: Bool = false
	private var onProductUpdated: ((ProductStatus) -> Void)? = nil

	@IBOutlet var retailPriceEdit: UITextField?
	@IBOutlet var salePriceEdit: UITextField?
	@IBOutlet var reorderStatusButton: UIButton?
	@IBOutlet var productNameLabel: UILabel?
	@IBOutlet var detailLabel: UILabel?
	@IBOutlet var skuConditionsButton: UIButton?


	/**
	 * Creates a page to update a product in an audit
	 * @param audit The audit being conducted
	 * @param productStatus The product being updated
	 * @param rawScan Raw scan data for product or null if manual
	 * @param delegate Receives product status update
	 */
	convenience init(audit: Audit, productStatus: ProductStatus, rawScan: String?,
			initReorderStatus: ReorderStatus? = nil,
			delegate: @escaping (ProductStatus) -> Void ) {
		self.init()
		self.audit = audit
		self.productStatus = productStatus
		self.rawScan = rawScan
		self.initReorderStatus = initReorderStatus
		self.onProductUpdated = delegate
	}

	/**
	 * Initializes our view once loaded.
	 */
	override func viewDidLoad() {
		super.viewDidLoad()

		// Set the product name
		productNameLabel?.text = productStatus?.getProductName

		// Set the product details
		var details = ""
		if let reorderCode = productStatus?.product.currentReorderCode {
			if !reorderCode.trimmingCharacters(in: .whitespaces).isEmpty {
				details = "Reorder Code: " + reorderCode
			}
		}
		if let sku = productStatus?.product.brandSku {
			if (!sku.trimmingCharacters(in: .whitespaces).isEmpty) {
				details += (details.isEmpty ? "" : details + ", ") +  "SKU: " + sku
			}
		}
		if (details.isEmpty) {
			// Don't show the details label
			detailLabel?.isHidden = true
		} else {
			// Update the details text
			detailLabel?.text = details
		}

		// Figure out initial values for the price edits
		let db = AuditDatabase()
		let productId = productStatus!.product.id
		updateScan = db?.getScan(audit: audit!, productId: productId)
		updateReport = db?.getReport(audit: audit!, productId: productId)
		let retailPrice = updateScan?.retailPrice
		let salePrice = updateScan?.salePrice
		let reorderStatusId = updateReport?.reorderStatusId

		// Get security options for product
		isSKUConditionsEnabled = Security.skuConditions != nil
		isAllowedToSetInStock = (rawScan != nil) ||
				productStatus!.product.randomWeight ||
				(productStatus!.reorderStatus.id == ReorderStatus.IN_STOCK.id)
		if (!isAllowedToSetInStock) {
			// Check settings
			if (!Security.optSetting(name: Security.SETTING_IN_STOCK_REQUIRES_SCAN, defaultValue: false)) {
				isAllowedToSetInStock = true
			}
		}
		updateSKUConditions()

		// Initialize the reorder status
		if (reorderStatusId == nil) {
			oldReorderStatus = ReorderStatus.NONE
		} else {
			oldReorderStatus = ReorderStatus.from(id: reorderStatusId!)
			if (oldReorderStatus == nil) {
				oldReorderStatus = ReorderStatus.NONE
			}
		}
		var reorderStatus = initReorderStatus ?? oldReorderStatus
		if ((rawScan != nil) && Security.optSetting(name: Security.SETTING_SCAN_FORCES_IN_STOCK, defaultValue: false)) {
			// Scan sets status to in stock
			reorderStatus = ReorderStatus.IN_STOCK
		} else if ((oldReorderStatus?.id == ReorderStatus.NONE.id) && isAllowedToSetInStock) {
			// Default display to in stock
			oldReorderStatus = ReorderStatus.IN_STOCK;
			reorderStatus = ReorderStatus.IN_STOCK;
		}

		// Set the input controls
		retailPriceEdit?.text = (retailPrice == nil) ? "" : String(format:"%.2f", retailPrice!)
		salePriceEdit?.text = (salePrice == nil) ? "" : String(format:"%.2f", salePrice!)
		let reorderStatusTitle = reorderStatus?.name ?? ReorderStatus.IN_STOCK.name
		UIView.performWithoutAnimation {
			reorderStatusButton?.setTitle(reorderStatusTitle, for: .normal)
			reorderStatusButton?.layoutIfNeeded()
		}

		// Focus the retail price
		DispatchQueue.main.async {
			self.retailPriceEdit?.becomeFirstResponder()
		}
	}

	/**
	 * Makes sure the page is up to date when it appears.
	 */
	override func onPageAppearing() {
		updateSKUConditions()
	}

	/**
	 * Updates the SKU condition button for the current state.
	 */
	private func updateSKUConditions() {
		skuConditionsButton?.isHidden = !isSKUConditionsEnabled
		if (isSKUConditionsEnabled) {
			// Update button for current state
			let db = AuditDatabase()
			let selectionCount = db?.getSelectedSKUConditions(
				audit: audit!, productId: productStatus!.product.id)?.count ?? 0
			UIView.performWithoutAnimation {
				// Format the button text
				skuConditionsButton?.setTitle("SKU CONDITIONS (\(selectionCount))", for: .normal)
				skuConditionsButton?.layoutIfNeeded()
			}
		}
	}

	/**
	 * Handles next button for text field input.
	 * @return Handled flag
	 */
	func textFieldShouldReturn(_ textField: UITextField) -> Bool {
		if (textField == retailPriceEdit) {
			salePriceEdit?.becomeFirstResponder()
		} else if (textField == salePriceEdit) {
			salePriceEdit?.resignFirstResponder()
			onReorderStatus()
		}
		return true
	}

	/**
	 * Provides our page name for the status bar.
	 * @return Display page name
	 */
	override var pageName: String? {
		return "Scan Product"
	}

	/**
	 * Indicates that the page should shrink to make space for the keyboard,
	 * rather than overlap.
	 * @return Shrink flag
	 */
	override var shrinkForKeyboard: Bool {
		return true
	}

	/**
	 * Saves on back navigation.
	 * @return Allow default back navigation flag
	 */
	override func onBack() -> Bool {
		saveProduct()
		return true
	}

	/**
	 * Displays information about the product we're updating.
	 */
	@IBAction func onInfo() {
		host?.pushPage(InfoProductPage(productStatus!.product))
	}

	/**
	 * Gets a new reorder status from the user.
	 */
	@IBAction func onReorderStatus() {
		// Offer the reorder options to the user
		let alert = UIAlertController(title: "Select a Reorder Status",
			message: nil, preferredStyle: .actionSheet)
		ReorderStatus.Statuses.forEach { status in
			if (status.isValid && ((status.id != ReorderStatus.IN_STOCK.id) || isAllowedToSetInStock)) {
				alert.addAction(UIAlertAction(title: status.name, style: .default) { action in
					UIView.performWithoutAnimation {
						self.reorderStatusButton?.setTitle(status.name, for: .normal)
						self.reorderStatusButton?.layoutIfNeeded()
					}
				})
			}
		}
		alert.addAction(UIAlertAction(title: "Cancel", style: .cancel))
		present(alert, animated: true)
	}

	/**
	 * Handles tap on the back to product list button.
	 */
	@IBAction func onProductList() {
		_ = onBack()
	}

	/**
	 * Handles changed text in a price field by enforcing the auto decimal rules.
	 * @param edit Changed field
	 */
	@IBAction func onPriceEditChanged(_ edit: UITextField) {
		if (!Security.optSetting(name: Security.SETTING_AUTO_DECIMAL, defaultValue: true)) {
			// Don't enforce auto decimal
			return;
		}
		if let current = edit.text {
			if current.isEmpty {
				return
			}
			var adjust = current.replacingOccurrences(of: ".", with: "")
			if (adjust.isEmpty) {
				adjust = "0.00"
			} else {
				if let value = Double(adjust) {
					adjust = String(format: "%1.2f", value / 100)
				}
			}
			if (adjust != current) {
				edit.text = adjust
			}
		}
	}

	/**
	 * Handles tap on the SKU Conditions button.
	 */
	@IBAction func onSkuConditions() {
		host!.pushPage(SKUConditionsPage(audit: audit!, productId: productStatus!.product.id))
	}

	/**
	 * Saves the product and navigates back to the list on success.
	 */
	private func saveProduct() {
		// Parse inputs, starting with retail price
		let retailPriceValue = validateInput(retailPriceEdit, description: "Retail Price")
		if (retailPriceValue ?? 0 < 0) {
			// Invalid retail price
			return
		}

		// Parse the sale price
		let salePriceValue = validateInput(salePriceEdit, description: "Sale Price")
		if (salePriceValue ?? 0 < 0) {
			// Invalid sale price
			return
		}

		// Get the reorder status
		let statusValue = ReorderStatus.from(name: reorderStatusButton?.title(for: .normal))
		if (statusValue?.id == ReorderStatus.IN_STOCK.id) {
			// Do we have minimum or maximum prices?
			var minValue = productStatus?.product.inStockPriceMin
			var maxValue = productStatus?.product.inStockPriceMax
			if (minValue == nil) {
				minValue = Security.optSetting(name: Security.SETTING_IN_STOCK_PRICE_MIN)
			}
			if (maxValue == nil) {
				maxValue = Security.optSetting(name: Security.SETTING_IN_STOCK_PRICE_MAX)
			}

			// Do we have price ranges to check?
			var rangeMessage: String? = nil
			if (minValue != nil) {
				// Test minimum value
				if ((retailPriceValue ?? minValue! < minValue!) ||
					(salePriceValue ?? minValue! < minValue!)) {
					// One of the prices is less than the minimum
					rangeMessage = (maxValue == nil)
							? String(format:"The prices for in stock items should be at least %@. Are you sure you want to save prices outside this range?",
								formatCurrency(amount: minValue))
							: String(format: "The prices for in stock items should be between %@ and %@. Are you sure you want to save prices outside this range?",
								formatCurrency(amount: minValue), formatCurrency(amount: maxValue))
				}
			}
			if ((rangeMessage == nil) && (maxValue != nil)) {
				// Test maximum value
				if ((retailPriceValue ?? maxValue! > maxValue!) ||
					(salePriceValue ?? maxValue! > maxValue!)) {
					// One of the prices is higher than the maximum
					rangeMessage = (minValue == nil)
							? String(format: "The prices for in stock items should be not more than %s. Are you sure you want to save prices outside this range?",
								formatCurrency(amount: maxValue))
							: String(format: "The prices for in stock items should be between %@ and %@. Are you sure you want to save prices outside this range?",
								formatCurrency(amount: minValue), formatCurrency(amount: maxValue))
				}
			}
			if (rangeMessage != nil) {
				// Show confirmation
				let alert = UIAlertController(title: nil, message: rangeMessage, preferredStyle: .alert)
				alert.addAction(UIAlertAction(title: "Go Back", style: .cancel, handler: nil))
				alert.addAction(UIAlertAction(title: "Save Anyway", style: .default) { action in
					self.updateDatabase(retailPriceValue, salePriceValue, statusValue)
				})
				present(alert, animated: true)
				return
			}
		}

		// Update the database
		updateDatabase(retailPriceValue, salePriceValue, statusValue)
	}

	/**
	 * Formats passed amount as currency for the current locale.
	 * @param amount Amount to format
	 * @return formatted amount
	 */
	private func formatCurrency(amount: Double?) -> String {
		// Check for nil
		if (amount == nil) {
			return "n/a"
		}

		// Format non-nil amount for current locale
		let formatter = NumberFormatter()
		formatter.locale = Locale.current
		formatter.numberStyle = .currency
		return formatter.string(from: NSNumber(floatLiteral: amount!)) ?? "n/a"
	}

	/**
	 * Validates a dollar amount input.
	 * @param entry Edit text entry
	 * @param description For formatting error toast or null for quiet
	 * @return Valid input, nil for empty, -1 for invalid
	 */
	private func validateInput(_ entry: UITextField?, description: String) -> Double? {
		// Is there a value?
		let text = entry?.text?.trimmingCharacters(in: .whitespaces)
		if (text?.isEmpty ?? true) {
			// Accept no/blank input
			return nil;
		}

		// Validate input format
		if text!.range(of: "^\\d*.?\\d{1,2}?$", options: .regularExpression, range: nil, locale: nil) != nil {
			if var parsed = Double(text!) {
				let isAutoDecimalEnabled = Security.optSetting(name: Security.SETTING_ALLOW_SMART_SCAN, defaultValue: false)
				if (isAutoDecimalEnabled && (text!.range(of: "^\\d{2,}$", options: .regularExpression, range: nil, locale: nil) != nil)) {
					parsed /= 100
				}
				return parsed
			}
		}

		// Invalid
		view.makeToast(message: "Enter a valid amount for " + description)
		return -1
	}

	/**
	 * Saves updates to the database, and exits this screen.
	 * @param retailPriceValue Retail prices to save
	 * @param salePriceValue Sale price to save
	 * @param statusValue Reorder status to save
	 */
	private func updateDatabase(_ retailPriceValue: Double?, _ salePriceValue: Double?, _ statusValue: ReorderStatus?) {
		// Access the database
		guard let db = AuditDatabase() else {
			view.makeToast(message: "Failed to access audits database")
			return
		}

		// Do we have an existing scan?
		if (updateScan == nil) {
			// No, save new scan
			let applyScan = Scan(audit: audit!, product: productStatus!.product,
				scanData: rawScan, scanRetail: retailPriceValue, scanSale: salePriceValue)
			if (!db.addScan(scan: applyScan)) {
				view.makeToast(message: "Failed to save new scan")
				return
			}
			updateScan = applyScan
			productStatus?.updateFrom(scan: applyScan)
		} else {
			// Has the existing scan changed?
			if let applyScan = Scan(source: updateScan!, scanData: rawScan,
				scanRetail: retailPriceValue, scanSale: salePriceValue) {
				if (!db.updateScan(update:applyScan)) {
					view.makeToast(message: "Failed to update product scan")
					return
				}
				updateScan = applyScan
				productStatus?.updateFrom(scan: applyScan)
			}
		}
		if (statusValue != nil) {
			// Do we have an update to the reorder status?
			if (updateReport == nil) {
				// Only save the default when there's a change
				if ((retailPriceValue != nil) || (salePriceValue != nil) || (statusValue?.id != oldReorderStatus?.id)) {
					// Insert new report
					let applyReport = Report(scan: updateScan!,
						product: productStatus!.product, reorderStatusId: statusValue!.id)
					if (!db.addReport(report: applyReport)) {
						view.makeToast(message: "Failed to save new report")
						return
					}
					updateReport = applyReport
					productStatus?.setReorderStatus(fromReport: applyReport, scan: updateScan)
				}
			} else {
				// Did the report change?
				if (statusValue!.id != updateReport!.reorderStatusId) {
					// Update existing report
					let applyReport = Report(source: updateReport!,
						scan: updateScan, reorderStatusId: statusValue!.id)
					if (!db.updateReport(update: applyReport)) {
						view.makeToast(message: "Failed to update report")
						return
					}
					updateReport = applyReport
					productStatus?.setReorderStatus(fromReport: applyReport, scan: updateScan)
				}
			}
		}

		// Make sure the keyboard is dismissed
		view.endEditing(true)
		onProductUpdated!(productStatus!)
		host!.popPage()
	}
}
