//
//  HelpPage.swift
//  Mobile Client
//
//  Created by Eric Ruck on 1/6/18.
//  Copyright 2018-2019 AuditPro. All rights reserved.
//

import UIKit


/**
 * Manages the help page of the application.
 * Note: Pages should include font-family:"Hero" to use the custom font.
 * TODO: Consider injecting into the code or browser component somehow?
 * @author Eric Ruck
 */
class HelpPage: BasePage {
	@IBOutlet weak var webView: UIWebView!
	private var helpFile: String? = nil

	/**
	 * Initializes to display a help file.
	 * @param helpFile Name of help file embedded in bundle
	 */
	convenience init(helpFile: String) {
		self.init()
		self.helpFile = helpFile
	}

	/**
	 * Starts loading the help page when our resources load.
	 */
    override func viewDidLoad() {
        super.viewDidLoad()
		if (helpFile != nil) {
			// Load the help file
			let path = Bundle.main.path(forResource: helpFile, ofType: nil)
			let html = try? String(contentsOfFile: path!, encoding: String.Encoding.utf8)
			webView.loadHTMLString(html!, baseURL: nil)
		}
    }

	/**
	 * Shows our page name in the action bar.
	 * @return Our page name
	 */
	override var pageName: String? {
		return "Help"
	}

	/**
	 * Keeps location turned on for the main menu while we're displayed
	 */
	override var isLocationNeeded: BasePage.LocationNeed {
		return  .keep
	}
}
