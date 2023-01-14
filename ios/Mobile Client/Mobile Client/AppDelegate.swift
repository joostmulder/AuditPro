//
//  AppDelegate.swift
//  Mobile Client
//
//  Created by Eric Ruck on 12/31/17.
//  Copyright 2017-2019 AuditPro. All rights reserved.
//

import UIKit
import CoreLocation

import Fabric
import Crashlytics


/**
 * Provides the main application event handling delegate, as well as
 * singleton instances of application data.
 * @author Eric Ruck
 */
@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate, CLLocationManagerDelegate {

	var window: UIWindow?
	var reachability: Reachability?

	private var _sessionToken: String?
	private var _lastLocation: CLLocation? = nil
	private var _locationManager: CLLocationManager? = nil


	/**
	 * Handles application launching by initializing our resources.
	 * @param application Parent application instance
	 * @param launchOptions Details about how we were launched
	 * @return Launch success flag
	 */
	func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
		// Setup Crashlytics
        Fabric.with([Crashlytics.self])

		// Initialize network reachability
		reachability = Reachability()!

		// Initialize locations
		_locationManager = CLLocationManager()
		_locationManager!.delegate = self

		// Initialize the application window
		window = UIWindow(frame: UIScreen.main.bounds)
		let homeViewController = MainViewController()
		window?.rootViewController = homeViewController
		window?.makeKeyAndVisible()
		return true
	}

	func applicationWillResignActive(_ application: UIApplication) {
	}

	/**
	 * Handles app backgrounding by unsubscribing from network and location
	 * updates, as necessary.
	 * @param application Parent application reference
	 */
	func applicationDidEnterBackground(_ application: UIApplication) {
		// Stop network notifications
		reachability?.stopNotifier()

		// Need the main VC to get the current location status
		if let mainVC = getMainVC(caller: "App backgrounding") {
			if (mainVC.isLocationEnabled) {
				// Shut off notifications for backgrounding
				AppDelegate.endLocationUpdates()
			}

			// Propagate notification to the VC
			mainVC.onDidEnterBackground()
		}
	}

	func applicationWillEnterForeground(_ application: UIApplication) {
	}

	/**
	 * Handles app becoming active by turning on services that may have been
	 * turned off when the app was backgrounded.
	 * @param application Parent application reference
	 */
	func applicationDidBecomeActive(_ application: UIApplication) {
		do {
			// Turn on notifier
			try reachability?.startNotifier()
		} catch {
			NSLog("Failed to start reachability notifier")
		}
		if let mainVC = getMainVC(caller: "App activating") {
			// Do we need to turn on location notifications?
			if mainVC.isLocationEnabled {
				AppDelegate.startLocationUpdates()
			}

			// Propagate active event
			mainVC.onDidBecomeActive()
		}
	}

	func applicationWillTerminate(_ application: UIApplication) {
		// Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
	}

	/**
	 * Gets the main view controller from the window hierarchy.
	 * @return Main view controller or nil if not present
	 */
	private func getMainVC(caller: String? = nil) -> MainViewController? {
		if let mainVC = window?.rootViewController as? MainViewController {
			return mainVC
		}

		// Unable to find main VC
		NSLog("ERROR \(caller == nil ? "" : caller! + " ")unable to locate main view controller")
		return nil
	}

	/**
	 * Gets network connected state.
	 * @return Network connected flag
	 */
	static var isNetworkConnected: Bool {
		let app = UIApplication.shared.delegate as! AppDelegate
		return app.reachability?.connection != Reachability.Connection.none
	}

	/**
	 * Gets network connected to wifi state.
	 * @return Wifi connected flag
	 */
	static var isWifiConnected: Bool {
		let app = UIApplication.shared.delegate as! AppDelegate
		return app.reachability?.connection == Reachability.Connection.wifi
	}

	/**
	 * Keeps the session token for validation to the remote web service,
	 * or nil if not validated.
	 */
	static var sessionToken: String? {
		get {
			let app = UIApplication.shared.delegate as! AppDelegate
			return app._sessionToken
		}
		set {
			let app = UIApplication.shared.delegate as! AppDelegate
			app._sessionToken = newValue
		}
	}

	/**
	 * Indicates if a current session is active.
	 * @return Active session flag
	 */
	static var isInSession: Bool {
		return AppDelegate.sessionToken != nil
	}

	/**
	 * Gets the last location reported by the GPS, while we're monitoring the GPS position.
	 * Returns null if we don't have a location fix, or if we're not currently monitoring.
	 * @return Last reported location or null if none
	 */
	static var lastLocation: CLLocation? {
		let app = UIApplication.shared.delegate as! AppDelegate
		return app._lastLocation
	}

	/**
	 * Begins location updates for use by the current page.
	 */
	static func startLocationUpdates() {
		let app = UIApplication.shared.delegate as! AppDelegate
		switch CLLocationManager.authorizationStatus() {
			case .notDetermined:
				app._locationManager!.requestWhenInUseAuthorization()
				break
			case .authorizedWhenInUse, .authorizedAlways:
				app.subscribeLocation()
				break
			default:
				NSLog("Location permission previously denied")
		}
	}

	/**
	 * Ends location updates for use by the current page.
	 */
	static func endLocationUpdates() {
		let app = UIApplication.shared.delegate as! AppDelegate
		app._locationManager?.stopUpdatingLocation()
		app._lastLocation = nil
	}

	/**
	 * Subscribes to location updates once we know we're authorized.
	 */
	private func subscribeLocation() {
   		_locationManager!.desiredAccuracy = kCLLocationAccuracyHundredMeters
		_locationManager!.distanceFilter = 100.0  // In meters
   		_locationManager!.startUpdatingLocation()
	}

	/**
	 * Handles location authorization update notification.
	 * @param manager Authorized manager
	 * @param status Updated status
	 */
	func locationManager(_ manager: CLLocationManager, didChangeAuthorization status: CLAuthorizationStatus) {
		switch status {
			case .authorizedWhenInUse, .authorizedAlways:
				if let mainVC = getMainVC(caller: "Location auth"), mainVC.isLocationEnabled {
					subscribeLocation()
				}
				break
			default:
				NSLog("Location permission previously denied")
		}
	}

	/**
	 * Handles location updates.
	 */
	func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
		_lastLocation = locations.last!
	}

	/**
	 * Handles location manager failure.
	 */
	func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
		manager.stopUpdatingLocation()
		NSLog("Failed to start updating locations: %@", error.localizedDescription)
	}
}
