//
//  CustomButton.swift
//  Mobile Client
//
//  Created by Eric Ruck on 12/31/17.
//  Copyright © 2017 AuditPro. All rights reserved.
//

import UIKit

class CustomButton: UIButton {
	override func awakeFromNib() {
		self.backgroundColor = customColor
		self.layer.cornerRadius = 5
		self.clipsToBounds = true
		self.titleLabel?.font = UIFont(name: "hero", size:(self.titleLabel?.font.pointSize)!)
		setTitleColor(UIColor.black, for: .normal)
		setTitleColor(UIColor.lightGray, for: .disabled)
	}

	override var isEnabled: Bool {
		didSet {
			if (isEnabled) {
				backgroundColor = customColor
			} else {
				backgroundColor = customDisabledColor
			}
		}
	}

	var customColor: UIColor {
		return UIColor.init(red: 0.61, green: 0.80, blue: 0.33, alpha: 1.0)
	}

	var customDisabledColor: UIColor {
		var fRed : CGFloat = 0
		var fGreen : CGFloat = 0
		var fBlue : CGFloat = 0
		var fAlpha: CGFloat = 0
		customColor.getRed(&fRed, green: &fGreen, blue: &fBlue, alpha: &fAlpha)
		return UIColor.init(red: fRed * 1.2, green: fGreen * 1.2, blue: fBlue * 1.2, alpha: 1.0)
	}
}
