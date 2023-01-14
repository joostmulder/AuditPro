//
//  PriceButton.swift
//  Mobile Client
//
//  Created by Eric Ruck on 9/1/18.
//  Copyright 2018-2019 AuditPro. All rights reserved.
//

import UIKit


/**
 * Draws the custom price button.
 * @author Eric Ruck
 */
class PriceButton: UIButton {

	/**
	 * Disables display of the title label.
	 */
	override func awakeFromNib() {
		if let label = titleLabel {
			label.removeFromSuperview()
		}
	}

	/**
	 * Forces repaint of parent view behind label.
	 * @param title Title to display
	 * @param state State in which title is displayed
	 */
	override func setTitle(_ title: String?, for state: UIControl.State) {
		super.setTitle(title, for: state)
		setNeedsDisplay()
	}

	/**
	 * Custom painting for this button control.
	 * @param rect Drawing rectangle
	 */
    override func draw(_ rect: CGRect) {
    	// Setup default drawing
        UIColor.lightGray.setStroke()
        UIColor.white.setFill()

        // Determine what to paint
        var text = title(for: .normal) ?? ""
        let tokens = text.components(separatedBy: " ")
        var paintIcon: String? = nil
		if (tokens.count > 0) {
			let status = ReorderStatus.from(code: tokens[0])
			if ((status == ReorderStatus.IN_STOCK) || (status == ReorderStatus.OUT_OF_STOCK)) {
				// Display for in/out of stock
				text = (tokens.count > 1) ? tokens[1] : status!.code
				if (status == ReorderStatus.IN_STOCK) {
					UIColor(red: 0.61, green: 0.80, blue: 0.33, alpha: 1.0).setStroke()
					paintIcon = "check_instock"
				} else {
					// Show out of stock border color
					UIColor.red.setStroke()
				}
			} else if (status != nil) {
				// Display code only
				text = status!.code
			}
		}

        // Draw the border with a clear center
        let borderRect = CGRect(x: bounds.origin.x + 1, y: bounds.origin.y + 1, width: bounds.size.width - 2, height: bounds.size.height - 2)
        let border = UIBezierPath(roundedRect: borderRect, cornerRadius: 5)
        border.lineWidth = 2
        border.stroke()

		// Draw the text
		let offset = (bounds.size.height - 10) / 2
		let drawRect = CGRect(x: bounds.origin.x, y: bounds.origin.y + offset, width: bounds.size.width, height: bounds.size.height - offset)
		let style = NSMutableParagraphStyle()
		style.alignment = .center
		let attribs = [
			NSAttributedString.Key.font: UIFont(name: "hero", size:12) ?? UIFont.systemFont(ofSize: 12),
			NSAttributedString.Key.foregroundColor: UIColor.black,
			NSAttributedString.Key.paragraphStyle: style,
			NSAttributedString.Key.baselineOffset: NSNumber(floatLiteral: 0.0)
		]
		let drawText = NSAttributedString(string: text, attributes: attribs)
		drawText.draw(in: drawRect)

        // Do we have an icon to paint
		if (paintIcon != nil) {
			// Draw in the icon
			if let image = UIImage(named: paintIcon!) {
				image.draw(in: CGRect(x: bounds.width - 14, y: bounds.origin.y + 2, width: 12, height: 12))
			}
		}
    }
}
