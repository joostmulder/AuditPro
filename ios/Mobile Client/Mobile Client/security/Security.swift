//
//  Security.swift
//  Mobile Client
//
//  Created by Eric Ruck on 2/24/18.
//  Copyright 2018-2019 AuditPro. All rights reserved.
//

import Foundation
import Crashlytics


/**
 * Manages user state and offline access to the application.
 * @author Eric Ruck
 */
class Security {
	/**
	 * Gets the user identifier. The returned value is cached if there is no current session.  If
	 * there is no cached user, returns -1.
	 * @return The current user identifier
	 */
	static var userId: Int {
		let userId = UserDefaults.standard.integer(forKey: USERID_KEY)
		return userId > 0 ? userId : -1
	}

	/**
	 * Gets the user name formatted as "last, first".  The return value os cached if there is no
	 * current session.  If there is no cached user, returns an empty string.
	 * @return User name
	 */
	static var userName: String {
		let lastName = UserDefaults.standard.string(forKey: LASTNAME_KEY)
		let firstName = UserDefaults.standard.string(forKey: FIRSTNAME_KEY)
		if ((lastName == nil) || (lastName!.count == 0)) {
			return (firstName == nil) ? "" : firstName!
		}
		if ((firstName == nil) || (firstName!.count == 0)) {
			return lastName!
		}
		return String(format:"%@, %@", lastName!, firstName!)
	}

	/**
	 * Gets the unique client identifier. The returned value is cached if there is no current
	 * session.  If there is no cached user, returns -1.
	 * @return The current client identifier
	 */
	static var clientId: Int {
		let clientId = UserDefaults.standard.integer(forKey: CLIENTID_KEY)
		return (clientId > 0) ? clientId : -1
	}

	/**
	 * Gets the last authenticated e-mail.
	 * @return Email or empty string if none
	 */
	static var lastEmail: String {
		let email = UserDefaults.standard.string(forKey: EMAIL_KEY)
		return (email == nil) ? "" : email!
	}

	/**
	 * Gets the shared password if there is one, otherwise returns an empty string.
	 * @return Saved password or empty string if none
	 */
	static var  savedPassword: String {
		let password = UserDefaults.standard.string(forKey: PASSWORD_KEY)
		return (password == nil) ? "" : password!
	}

	/**
	 * Gets the client display name. The returned value is cached if there is no current session.
	 * If there is no cached user, returns an empty string.
	 * @return The current client display name
	 */
	static var clientName: String {
		let name = UserDefaults.standard.string(forKey: CLIENTNAME_KEY)
		return (name == nil) ? "" : name!
	}

	/**
	 * Gets the client account SKU conditions, if any, otherwise nil.
	 * @return SKU conditions or nll
	 */
	static var skuConditions: [Int: SKUCondition]? {
		return SKUCondition.from(string: UserDefaults.standard.string(forKey: SKUCONDITIONS_KEY))
	}

	// Known setting names, from web service
	public static let SETTING_IN_STOCK_REQUIRES_SCAN = "in_stock_requires_scan" // Bool
	public static let SETTING_IN_STOCK_PRICE_MAX = "in_stock_price_max" // Double
	public static let SETTING_IN_STOCK_PRICE_MIN = "in_stock_price_min" // Double
	public static let SETTING_SCAN_FORCES_IN_STOCK = "scan_forces_in_stock" // Bool
	public static let SETTING_ALLOW_CHAIN_SKU = "allow_chain_sku" // Bool
	public static let SETTING_ALLOW_SMART_SCAN = "allow_smart_scan" // Bool
	public static let SETTING_AUDIT_DISTANCE_MAX_MILES = "audit_distance_max_miles" // Double
	public static let SETTING_NO_NOTES_WARNING = "no_notes_warning" // Bool
	public static let SETTING_AUDIT_STORE_NOTES = "allow_store_notes" // Bool default false
	public static let SETTING_PRINT_VOIDS = "print_voids" // Bool default false
	public static let SETTING_PRINT_CONDITIONS = "print_conditions" // Bool default false
	public static let SETTING_PRINT_STORE_NOTES = "print_store_notes" // Bool default false

	// Known setting names, local
	public static let SETTING_AUTOSYNC_WIFI = "autosync_wifi"; // Bool
	public static let SETTING_AUTO_DECIMAL = "auto_decimal"; // Bool

	/**
	 * Gets a boolean setting.
	 * @param name Setting name
	 * @param defaultValue Default value to use if setting not found
	 * @return Setting value or default
	 */
	static func optSetting(name: String, defaultValue: Bool = false) -> Bool {
		let prefs = UserDefaults.standard
		let keyName = CLIENT_SETTING_KEY_PREFIX + name
		if (!prefs.dictionaryRepresentation().keys.contains(keyName)) {
			return defaultValue
		}
		return prefs.bool(forKey: keyName)
	}

	/**
	 * Gets a double setting.
	 * @param name Setting name
	 * @return Double setting or null if not set
	 */
	static func optSetting(name: String) -> Double? {
		let prefs = UserDefaults.standard
		let keyName = CLIENT_SETTING_KEY_PREFIX + name
		if (!prefs.dictionaryRepresentation().keys.contains(keyName)) {
			return nil
		}
		return prefs.double(forKey: keyName)
	}

	/**
	 * Saves a boolean setting.
	 * @param name Setting name
	 * @param value New value for setting
	 */
	static func setSetting(name: String, value: Bool) {
		let keyName = CLIENT_SETTING_KEY_PREFIX + name
		UserDefaults.standard.set(value, forKey: keyName)
	}

	/**
	 * Keeps the last audit position.  Pass null for either or both parameters to clear this position.
	 * @param latitude Optional latitude
	 * @param longitude Optional longitude
	 */
	static func setLastAuditPos(latitude: Double?, longitude: Double?) {
		let prefs = UserDefaults.standard
		if ((latitude == nil) || (longitude == nil)) {
			prefs.removeObject(forKey: LASTAUDITLAT_KEY)
			prefs.removeObject(forKey: LASTAUDITLON_KEY)
		} else {
			prefs.set(latitude!, forKey: LASTAUDITLAT_KEY)
			prefs.set(longitude!, forKey: LASTAUDITLON_KEY)
		}
	}

	/**
	 * Gets the last audit position.
	 * @return The last audit position (lat, lon) as a tuple or null if none.
	 */
	static func getLastAuditPos() -> (Double, Double)? {
		// Are the values set?
		let prefs = UserDefaults.standard
		if (prefs.dictionaryRepresentation().keys.contains(LASTAUDITLAT_KEY) &&
			prefs.dictionaryRepresentation().keys.contains(LASTAUDITLON_KEY)) {
			// Yes, return the tuple
			return (
				lat: prefs.double(forKey: LASTAUDITLAT_KEY),
				lon: prefs.double(forKey: LASTAUDITLON_KEY)
			)
		}

		// No values
		return nil
	}

	/**
	 * Verifies that the passed password matches the last authentication,
	 * for offline access to the application.
	 * @param password Password to verify
	 * @param isPasswordSaved Should the password be saved in the store?
	 * @return Password verified offline flag
	 */
	static func verifyLastPassword(password: String, isPasswordSaved: Bool) -> Bool {
		// Verify the password
		let prefs = UserDefaults.standard
		let savedHash = prefs.string(forKey: HASH_KEY)
		let isVerified = hashPassword(password) == savedHash
		if (isVerified && isPasswordSaved) {
			// Update saved password state
			prefs.set(password, forKey: PASSWORD_KEY)
		}

		// Indicate verified state
		return isVerified;
	}

	/**
	 * Preserves our successful login.
	 * @param email Validate user e-mail
	 * @param password Password entered to authenticate user
	 * @param isPasswordSaved Should the password be saved in the store?
	 * @param userResponse Response from user query to API
	 */
	static func setLogin(email: String, password: String, isPasswordSaved: Bool, userResponse: UserResponse) {
		// Keep the input preferences
		let prefs = UserDefaults.standard
		prefs.set(email, forKey: EMAIL_KEY)
		prefs.set(isPasswordSaved ? password : "", forKey: PASSWORD_KEY)
		prefs.set(hashPassword(password), forKey: HASH_KEY)

		// Keep the queried user information
		prefs.set(userResponse.userId, forKey: USERID_KEY)
		prefs.set(userResponse.firstName, forKey: FIRSTNAME_KEY)
		prefs.set(userResponse.lastName, forKey: LASTNAME_KEY)
		prefs.set(userResponse.clientId, forKey: CLIENTID_KEY)
		prefs.set(userResponse.clientName, forKey: CLIENTNAME_KEY)
		prefs.set(SKUCondition.toJSONString(userResponse.skuConditions), forKey: SKUCONDITIONS_KEY)

		// Transfer the settings
		for (key, value) in userResponse.clientSettings {
			prefs.set(value, forKey: CLIENT_SETTING_KEY_PREFIX + key)
		}

		// Format for analytics
		Crashlytics.sharedInstance().setUserIdentifier(userResponse.userId.description)
		Crashlytics.sharedInstance().setUserEmail(email)
		Crashlytics.sharedInstance().setUserName(Security.userName)
	}

	/**
	 * Saves the store database version following sync.
	 * @param storeVersion Current store database version code at sync
	 */
	static func setStoreVersionOnSync(_ storeVersion: Int) {
		let prefs = UserDefaults.standard
		prefs.set(storeVersion, forKey: SYNCSTORESVER_KEY)
	}

	/**
	 * Determines if we need a full sync.
	 * @param storeVersion Current store database version.
	 * @return Needs sync flag
	 */
	static func isSyncNeeded(forVersion storeVersion: Int) -> Bool {
		let prefs = UserDefaults.standard
		return prefs.integer(forKey: SYNCSTORESVER_KEY) < storeVersion
	}

	/**
	 * Applies a secure password hash.
	 * @param password Password to hash
	 * @return Hashed password
	 */
	private static func hashPassword(_ password: String) -> String {
        let data = password.data(using: String.Encoding.utf8)!
        var digest = [UInt8](repeating: 0, count:Int(CC_SHA1_DIGEST_LENGTH))
        data.withUnsafeBytes {
            _ = CC_SHA1($0.bindMemory(to: UInt8.self).baseAddress, CC_LONG(data.count), &digest)
        }
        let hexBytes = digest.map { String(format: "%02hhx", $0) }
        return hexBytes.joined()
	}

	private static let EMAIL_KEY = "email"
	private static let PASSWORD_KEY = "password"
	private static let HASH_KEY = "hash"
	private static let USERID_KEY = "userid"
	private static let FIRSTNAME_KEY = "firstname"
	private static let LASTNAME_KEY = "lastname"
	private static let CLIENTID_KEY = "clientid"
	private static let CLIENTNAME_KEY = "clientname"
	private static let SKUCONDITIONS_KEY = "skuConditions"
	private static let LASTAUDITLAT_KEY = "lastauditlat"
	private static let LASTAUDITLON_KEY = "lastauditlon"
	private static let SYNCSTORESVER_KEY = "syncstoresver"
	private static let CLIENT_SETTING_KEY_PREFIX = "clientsetting_"
}
