//
//  CustomPercent.swift
//  Mobile Client
//
//  Created by Eric Ruck on 8/13/18.
//  Copyright 2018-2019 AuditPro. All rights reserved.
//

import UIKit


/**
 * Draws the percentage around the outside of the control.
 * @author Eric Ruck
 */
class CustomPercent: UILabel {
	private static let STROKE_WIDTH: CGFloat = 2.0
	private static let FULL_COLOR = UIColor.lightGray
	private static let PCT_COLOR = UIColor.blue

	/**
	 * Custom painting for this label control.
	 * @param rect Drawing rectangle
	 */
    override func draw(_ rect: CGRect) {
        // Default first
        super.draw(rect)
		guard let text = self.text else {
			// Unable to parse percentage
			return
		}
		if	let range = text.range(of: "^\\d+", options: .regularExpression, range: nil, locale: nil),
		 	let percent = Int(text[range]) {
			// Draw the percentage in our text
	 		let radius = (bounds.size.width - CustomPercent.STROKE_WIDTH) / 2
	 		let offset = radius + (CustomPercent.STROKE_WIDTH / 2)
	 		let center = CGPoint(x: offset, y: offset)
		 	if (percent >= 0) && (percent < 100) {
		 		// Draw full arc
		 		let path = UIBezierPath(arcCenter: center, radius: radius, startAngle: 0, endAngle: .pi * 2, clockwise: true)
		 		path.lineWidth = CustomPercent.STROKE_WIDTH
		 		CustomPercent.FULL_COLOR.setStroke()
		 		path.stroke()
			}
		 	if (percent > 0) && (percent <= 100) {
		 		// Draw percent arc
		 		let startAngle = CGFloat(.pi * 1.5)
		 		let endAngle = CGFloat(percent - 25) / 50.0 * .pi
		 		let path = UIBezierPath(arcCenter: center, radius: radius, startAngle: startAngle, endAngle: endAngle, clockwise: true)
		 		path.lineWidth = CustomPercent.STROKE_WIDTH
		 		CustomPercent.PCT_COLOR.setStroke()
		 		path.stroke()
			}
		}
    }
}
