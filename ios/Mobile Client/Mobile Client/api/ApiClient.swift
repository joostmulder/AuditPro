//
//  ApiClient.swift
//  Mobile Client
//
//  Created by Eric Ruck on 2/10/18.
//  Copyright © 2018 AuditPro. All rights reserved.
//

import Foundation

// TODO Consider logging errors to Crashlytics


/**
 * Provides access to the remote web services in support of this application.
 * @author Eric Ruck
 */
class ApiClient {

	/**
	 * Initializes a new client API instance.
	 * @param initToken Initial authentication token, or null if not yet authenticated
	 */
	init(token: String? = nil) {
		_token = token;
	}

	/**
	 * Access the security token for the current API session.
	 * @return Token or null if none
	 */
	var token: String? {
		get { return _token }
	}

	/**
	 * Gets the message returned from the last web service call.
	 * May be null if there is no completed web service call, or empty if the last completed
	 * call was successful.
	 * @return API result message
	 */
	var message: String? {
		get { return _message }
	}

	var response: Any? {
		get{ return _response }
	}

	/**
	 * Logs the user in to the application.  Saves the authentication token to this instance so
	 * that the class can immediately be used for authenticated APIs.
	 * @param email Identifies the user to authenticate
	 * @param password Provides the password for authentication
	 */
	func login(email: String, password: String, completion: @escaping (ApiClient) -> Void) {
		// Make sure we really want to do this
		if (_token != nil) {
			// Warn that we're already logged in
			print("Processing login while already logged in")
		}

		// Login the user
		let endpoint = String(format: "%@login/%@/%@",
			ApiClient.BASE_URL, email, password)
		self.openClient(endpoint: endpoint, descr: "login", completion: { (loginResult) in
			if (loginResult == nil) {
				completion(self)
				return
			}
			guard let loginToken = loginResult!["session_id"] as? String else {
				self._message = "Missing login token"
				completion(self)
				return
			}

			// Keep the token
			self._token = loginToken
			self.user(completion: completion)
		})
	}

	/**
	 * Gets information about the user associated with the current access token.
	 */
	func user(completion: @escaping (ApiClient) -> Void) {
		// Make sure we really want to do this
		if (self._token == nil) {
			// Unable to proceed without a token
			self._message = "Login required to get user information"
			completion(self)
			return
		}

		// Get the logged in user info
		let endpoint = String(format: "%@user/%@",
			ApiClient.BASE_URL, self._token!)
		self.openClient(endpoint: endpoint, descr: "user", completion: { (userResult) in
			if (userResult != nil) {
				guard let response = UserResponse(json: userResult!) else {
					self._message = "Missing user response"
					completion(self)
					return
				}
				self._response = response;
			}
			completion(self)
		})
	}

	/**
	 * Gets the stores where the authenticated user can conduct audits.
	 */
	func getStores(completion: @escaping (ApiClient) -> Void) {
		// Verify that we have authenticated
		if (self._token == nil) {
			// Unable to proceed without a token
			self._message = "Login required to get store information"
			completion(self)
			return
		}

		// Get the stores from the web service
		let endpoint = String(format: "%@stores/%@", ApiClient.BASE_URL, self._token!);
		self.openClient(endpoint: endpoint, descr: "get stores", completion: { (storesResult) in
			if (storesResult == nil) {
				completion(self)
				return
			}
			guard let storesArray = storesResult!["values"] as? [[String: Any]] else {
				self._message = "Invalid stores response"
				completion(self)
				return
			}
			var response = [Store]()
			for store in storesArray {
				guard let parsed = Store(json: store) else {
					continue
				}
				response.append(parsed)
			}
			if (response.count == 0) {
				self._message = "No valid stores received from web service"
			} else {
				self._response = response;
			}
			completion(self)
		})
	}

	/**
	 * Gets the products on which authenticated user can conduct audits.
	 */
	func getProducts(completion: @escaping (ApiClient) -> Void) {
		// Verify that we have authenticated
		if (self._token == nil) {
			// Unable to proceed without a token
			self._message = "Login required to get product information"
			completion(self)
			return
		}

		// Get the products from the web service
		let endpoint = String(format: "%@products/%@", ApiClient.BASE_URL, self._token!);
		self.openClient(endpoint: endpoint, descr: "get products", completion: { (productsResult) in
			if (productsResult == nil) {
				completion(self)
				return
			}
			guard let productsArray = productsResult!["values"] as? [[String: Any]] else {
				self._message = "Invalid products response"
				completion(self)
				return
			}
			var response = [Product]()
			for product in productsArray {
				guard let parsed = Product(json: product) else {
					continue
				}
				response.append(parsed)
			}
			if (response.count == 0) {
				self._message = "No valid products received from web service"
			} else {
				self._response = response;
			}
			completion(self)
		})
	}

	/**
	 * Posts a payload containing the details of an audit that will be processed by the
	 * back end system.
	 * @param auditJson Audit JSON to post to the server
	 */
	func postPayload(auditJson: String, completion: @escaping (ApiClient) -> Void) {
		// Verify that we have authenticated
		if (self._token == nil) {
			// Unable to proceed without a token
			self._message = "Login required to send audit information"
			completion(self)
			return
		}

		// Send the payload to the web service
		// TODO Apply token when implemented on the server side
		let endpoint = String(format: "%@payload/v1/", ApiClient.BASE_URL)
		self.postClient(endpoint: endpoint, payload: auditJson, descr: "audit", completion: { (responsePost) in
			completion(self)
		})
	}

	/**
	 * Cancels the current task in progress, if any.
	 */
	func cancel() {
		_task?.cancel()
	}

	/**
	 * Indicates if the current task has been canceled.
	 * @return Canceled flag
	 */
	var isCanceled: Bool {
		return _task == nil ? false : _task!.state == .canceling
	}

	/**
	 * General internal access to the web service APIs.
	 * @param endpoint Endpoint to invoke
	 * @param descr Description of API function for generic error formatting
	 * @param completion Call back with results payload or nil on error
	 */
	private func openClient(endpoint: String, descr: String, completion: @escaping ([String: Any]?) -> Void) {
		let urlRequest = URLRequest(url: URL(string: endpoint)!)
		_task = URLSession.shared.dataTask(with: urlRequest, completionHandler: { (data, response, error) in
			do {
				guard let httpresponse = response as? HTTPURLResponse,
					httpresponse.statusCode == 200,
					error == nil,
					data != nil,
					let json = try JSONSerialization.jsonObject(with: data!, options: []) as? [String: Any],
					let status = json["status"] as? String,
					let message = json["message"] as? String
				else {
					self._message = "Unexpected response from " + descr + " request"
					completion(nil)
					return
				}

				// Are we successful?
				if (status != "success") {
					self._message = message
					completion(nil)
				} else {
					// Get the results from the payload
					if let result = json["data"] as? [Any] {
						// Array results
						self._message = nil
						completion(["values": result])
						return
					}
					guard let result = json["data"] as? [String: Any] else {
						self._message = "Unexpected response from " + descr + " request"
						completion(nil)
						return
					}

					// Pass along the results
					self._message = nil
					completion(result)
				}
			} catch {
				self._message = "Unexpected response from " + descr + " request"
				completion(nil)
			}
		})
		_task?.resume()
	}

	/**
	 * General internal access to the web service APIs that post.
	 * @param endpoint Endpoint to invoke
	 * @param payload Payload to post
	 * @param descr Description of API function for generic error formatting
	 * @param completion Call back with results payload or nil on error
	 */
	private func postClient(endpoint: String, payload: String, descr: String, completion: @escaping ([String: Any]?) -> Void) {
		var urlRequest = URLRequest(url: URL(string: endpoint)!)
		urlRequest.setValue("application/json; charset=UTF-8", forHTTPHeaderField: "Content-Type")
		urlRequest.httpMethod = "POST"
		urlRequest.httpBody = payload.data(using: .utf8)
		_task = URLSession.shared.dataTask(with: urlRequest, completionHandler: { (data, response, error) in
			do {
				guard let httpresponse = response as? HTTPURLResponse,
					httpresponse.statusCode == 200,
					error == nil,
					data != nil,
					let json = try JSONSerialization.jsonObject(with: data!, options: []) as? [String: Any],
					let status = json["status"] as? String,
					let message = json["message"] as? String
				else {
					self._message = "Unexpected response from " + descr + " request"
					completion(nil)
					return
				}

				// Are we successful?
				if (status != "success") {
					// Error status received
					self._message = message
					completion(nil)
				} else {
					// Success
					// Note: We don't get back any specific data
					self._message = nil
					completion(json)
				}
			} catch {
				self._message = "Unexpected response from " + descr + " request"
				completion(nil)
			}
		})
		_task?.resume()
	}

	private static let SUCCESS_STATUS = "success"
	private static let ERROR_STATUS = "error"
	private static let AUTH_MESSAGE = "Authentication required to access this service"
	private static let BASE_URL = "https://api.auditpro.io/api/"

	private var _token: String? = nil
	private var _message: String? = nil
	private var _response: Any? = nil
	private var _task: URLSessionTask? = nil
}
