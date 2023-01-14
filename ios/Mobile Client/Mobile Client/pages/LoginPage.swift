//
//  LoginPage.swift
//  Mobile Client
//
//  Created by Eric Ruck on 1/6/18.
//  Copyright 2018-2019 AuditPro. All rights reserved.
//

import UIKit


/**
 * Implements the login page controller.
 * @author Eric Ruck
 */
class LoginPage: BasePage, UITextFieldDelegate {

	// Control outlets
	@IBOutlet weak var emailText: UITextField!
	@IBOutlet weak var passwordText: UITextField!
	@IBOutlet weak var saveSwitch: UISwitch!
	@IBOutlet weak var loginButton: UIButton!
	@IBOutlet weak var versionLabel: UILabel!

	private var sessionFlag: Bool = false
	private var preserveInput: Bool = false
	private var client: ApiClient? = nil


	/**
	 * Initializes the session flag optionally to true, to indicate that we're
	 * showing the login within an offline session, rather that at the start
	 * of the application.
	 * @param sessionFlag In session flag
	 */
	convenience init(sessionFlag: Bool = false) {
		self.init()
		self.sessionFlag = sessionFlag
	}

	/**
	 * Updates when we attach to the parent.
	 * @param parent Parent view controller
	 */
	override func didMove(toParent parent: UIViewController?) {
		// Initialize the version
		versionLabel.text = String.init(format: versionLabel.text!, host!.version)
	}

	/**
	 * Handles tap on the login button.
	 * @param sender Login button
	 */
	@IBAction func onLogin() {
		// Make sure the keyboard is dismissed
		view.endEditing(true)

		// Do we have a network connection?
		if (AppDelegate.isNetworkConnected) {
			loginConnected()
		} else if (sessionFlag) {
			view.makeToast(message: "Please connect to the Internet to log in.")
		} else {
			loginOffline()
		}
	}

	/**
	 * Handles tap on the help button.
	 * @param sender Help button
	 */
	@IBAction func onHelp() {
		preserveInput = true
		host!.pushPage(HelpPage(helpFile: "LoginHelp.html"))
	}

	/**
	 * Updates the enabled state of the login button depending on the validity
	 * of the text field input state.
	 */
	@IBAction func textFieldEditingDidChange(sender: UITextField) {
		let emailTest = NSPredicate(format:"SELF MATCHES[c] %@",
			"(?:[a-z0-9!#$%\\&'*+/=?\\^_`{|}~-]+(?:\\.[a-z0-9!#$%\\&'*+/=?\\^_`{|}"+"~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\"+"x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-"+"z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5"+"]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-"+"9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21"+"-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])")
		let isEmailValid =
			emailTest.evaluate(with: emailText.text!)
		let isPasswordValid =
			(passwordText.text!.count > 0)
		NSLog("email isValid: %@, password valid %@", isEmailValid ? "true" : "false", isPasswordValid ? "true":"false")
		loginButton.isEnabled = isEmailValid && isPasswordValid
	}

	/**
	 * Handles completion input on text field.
	 * @param textField Control on which event occurred
	 * @return Perform default flag
	 */
	func textFieldShouldReturn(_ textField: UITextField) -> Bool {
		if (textField == emailText) {
			if (emailText.text!.count > 0) {
				passwordText.becomeFirstResponder()
			}
			return false
		}
		if (textField == passwordText) {
			if (passwordText.text!.count > 0) {
				if (loginButton.isEnabled) {
					onLogin()
				} else {
					emailText.becomeFirstResponder()
				}
			}
			return false
		}

		// Allow default handling
		return true
	}

	/**
	 * Updates the user interface when the page appears.
	 */
	override func onPageAppearing() {
		// Calls default implementation
		super.onPageAppearing()
		if (preserveInput) {
			// Don't reset the input
			preserveInput = false;
		} else {
			// Reinitialize our edits
			let savedPassword = Security.savedPassword
			emailText.text = Security.lastEmail
			passwordText.text = savedPassword
			saveSwitch.isOn = savedPassword.count > 0
			textFieldEditingDidChange(sender: emailText)
		}
	}

	/**
	 * Ensures background tasks are canceled when the page is disappearing.
	 */
	override func onPageDisappearing() {
		client?.cancel()
	}

	/**
	 * Validates the login against remote web services.
	 */
	private func loginConnected() {
		// Show activity
		host!.isActivityShowing = true

		// Async login to web service
		let email = emailText.text!
		let password = passwordText.text!
		client = ApiClient()
		client!.login(email: email, password: password, completion: { (client) in
			// Are we still showing?
			self.client = nil
			if (self.host == nil) {
				return
			}
			DispatchQueue.main.async {
				// Hide activity
				self.host!.isActivityShowing = false

				// Evaluate response
				if (client.message != nil) {
					// Failed to login, show error toast
					self.view.makeToast(message: client.message!)
				} else {
					// Complete login
					AppDelegate.sessionToken = client.token
					Security.setLogin(email: email, password: password,
						isPasswordSaved: self.saveSwitch.isOn,
						userResponse: client.response as! UserResponse)
					if (self.sessionFlag) {
						// Return to the calling page
						self.host!.popPage()
					} else {
						// Go to the main menu
						self.host!.pushPage(MainMenuPage())
					}
				}
			}
		})
	}

	/**
	 * Logs in the user with cached credentials.
	 */
	private func loginOffline() {
		// Do we have cached credentials?

		let lastEmail = Security.lastEmail
		if (lastEmail == "")  {
			// No cached credentials
			view.makeToast(message: "Please connect to the Internet to log in for the first time.")
		} else if ((lastEmail.caseInsensitiveCompare(emailText.text!) == .orderedSame) ||
				!Security.verifyLastPassword(password: passwordText.text!, isPasswordSaved: saveSwitch!.isOn)) {
			// Credentials don't match
			view.makeToast(message: "Your e-mail or password is not correct.")
		} else {
			// We're logged in
			host!.pushPage(MainMenuPage())
		}
	}
}
