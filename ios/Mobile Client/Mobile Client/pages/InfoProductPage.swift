//
//  InfoProductPage.swift
//  Mobile Client
//
//  Created by Eric Ruck on 4/3/18.
//  Copyright © 2018 AuditPro. All rights reserved.
//

import UIKit


/**
 * Displays the product extended information.
 * @author Eric Ruck
 */
class InfoProductPage : BasePage {
	@IBOutlet var productNameLabel: UILabel?
	@IBOutlet var brandNameLabel: UILabel?
	@IBOutlet var msrpLabel: UILabel?
	@IBOutlet var averageLabel: UILabel?
	@IBOutlet var minLabel: UILabel?
	@IBOutlet var maxLabel: UILabel?
	@IBOutlet var weighedLabel: UILabel?
	@IBOutlet var upcLabel: UILabel?
	@IBOutlet var skuLabel: UILabel?
	@IBOutlet var reorderCodeLabel: UILabel?
	@IBOutlet var categoryLabel: UILabel?
	@IBOutlet var subcategoryLabel: UILabel?
	@IBOutlet var productTypeLabel: UILabel?

	private var product: Product? = nil

	/**
	 * Initializes for the display of the passed product.
	 * @param product Product to display
	 */
	convenience init(_ product: Product) {
		self.init()
		self.product = product
	}

	/**
	 * Updates the view contents on load.
	 */
	override func viewDidLoad() {
		super.viewDidLoad()
		// Populate with passed product
		productNameLabel?.text = product?.productName
		populate(label: brandNameLabel, text: product?.brandName, altText: product?.brandNameShort)
		populate(label: msrpLabel, dollarValue: product?.msrp)
		populate(label: averageLabel, dollarValue: product?.retailPriceAverage)
		populate(label: minLabel, dollarValue: product?.retailPriceMin)
		populate(label: maxLabel, dollarValue: product?.retailPriceMax)
		populate(label: weighedLabel, formatValue: product?.randomWeight)
		populate(label: upcLabel, text: product?.upc)
		populate(label: skuLabel, text: product?.brandSku)
		populate(label: reorderCodeLabel, text: product?.currentReorderCode)
		populate(label: categoryLabel, text: product?.categoryName)
		populate(label: subcategoryLabel, text: product?.subcategoryName)
		populate(label: productTypeLabel, text: product?.productTypeName)
	}

	/**
	 * Gets the page display name.
	 * @return Page display name
	 */
	override var pageName: String? {
		return "Product Info"
	}

	/**
	 * Populates the label with a product detail.
	 * @param label Label to populate
	 * @param text Desired detail text
	 * @param altText Alternate detail text or null if none
	 */
	private func populate(label: UILabel?, text: String?, altText: String? = nil) {
		if (!(text?.trimmingCharacters(in: .whitespaces).isEmpty ?? true)) {
			label?.text = text
		} else if (!(altText?.trimmingCharacters(in: .whitespaces).isEmpty ?? true)) {
			label?.text = altText
		} else {
			label?.text = "(Not Specified)"
		}
	}

	/**
	 * Populates the label with a boolean value.
	 * @param label Label to populate
	 * @param formatValue Value to format
	 */
	private func populate(label: UILabel?, formatValue: Bool?) {
		label?.text = (formatValue == nil) ? "(NS)" : (formatValue! ? "Yes" : "No")
	}

	/**
	 * Populates the label with a product detail in dollars.
	 * @param label Label to populate
	 * @param dollarValue Dollars amount to display
	 */
	private func populate(label: UILabel?, dollarValue: Double?) {
		if (dollarValue == nil) {
			label?.text = "(NS)"
		} else {
			let formatter = NumberFormatter()
			formatter.locale = Locale.current
			formatter.numberStyle = .currency
			label?.text = formatter.string(from: NSNumber(floatLiteral: dollarValue!))
		}
	}
}
