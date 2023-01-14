/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.api;

import android.annotation.SuppressLint;
import android.util.Log;

import com.auditpro.mobile_client.entities.Product;
import com.auditpro.mobile_client.entities.Store;
import com.crashlytics.android.Crashlytics;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;


/**
 * Provides access to the remote web services in support of this application.
 * Note that this class does not implement any threading.  Callers of this class should only
 * access API functions on a worker thread, and marshal the results back to the UI as
 * necessary.
 * @author Eric Ruck
 */
public class ApiClient {

	private static final String SUCCESS_STATUS = "success";
	private static final String ERROR_STATUS = "error";

	/**
	 * Initializes a new client API instance.
	 * @param initToken Initial authentication token, or null if not yet authenticated
	 */
	public ApiClient(String initToken) {
		token = initToken;
	}

	/**
	 * Access the security token for the current API session.
	 * @return Token or null if none
	 */
	public String getToken() {
		return token;
	}

	/**
	 * Gets the status returned from the last web service call.
	 * May be null if there is no completed web service call.
	 * @return API result status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * Gets the message returned from the last web service call.
	 * May be null if there is no completed web service call, or empty if the last completed
	 * call was successful.
	 * @return API result message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Logs the user in to the application.  Saves the authentication token to this instance so
	 * that the class can immediately be used for authenticated APIs.
	 * @param email Identifies the user to authenticate
	 * @param password Provides the password for authentication
	 * @return Details of the user logged in or null on failure
	 */
	@SuppressLint("DefaultLocale")
	public UserResponse login(String email, String password) {
		// Make sure we really want to do this
		String loginToken;
		if (token != null) {
			// Warn that we're already logged in
			Log.w(LOG_TAG, "Processing login while already logged in");
		}

		// Login the user
		String loginEndpoint = String.format("%slogin/%s/%s", baseUrl, email, password);
		JSONObject data = (JSONObject) openClient(loginEndpoint, "login", false);
		if (data == null) {
			// Login web service failed
			if (responseCode != 200) {
				// Replace generic error message with specific login message
				message = String.format("Invalid user name or password (%d)", responseCode);
			}
			return null;
		}
		loginToken = data.optString("session_id");
		if (loginToken == null) {
			status = ERROR_STATUS;
			message = "Unexpected missing login token";
			return null;
		}

		// Get the user details
		String userEndpoint = String.format("%suser/%s", baseUrl, loginToken);
		data = (JSONObject) openClient(userEndpoint, "get user", false);
		if (data == null) {
			// Get user web service failed
			return null;
		}

		// Parse the user data
		UserResponse res = new UserResponse(data);
		if (!res.isValid()) {
			status = ERROR_STATUS;
			message = "Invalid user data received";
			return null;
		}

		// Success
		token = loginToken;
		return res;
	}

	/**
	 * Gets the stores where the authenticated user can conduct audits.
	 * @return List of stores or null on failure
	 */
	public List<Store> getStores() {
		// Verify that we have authenticated
		if (token == null) {
			// Required authentication
			status = ERROR_STATUS;
			message = AUTH_MESSAGE;
			return null;
		}

		// Get the stores from the web service
		String endpoint = baseUrl + "stores/" + token;
		JSONArray data = (JSONArray) openClient(endpoint, "get stores", true);
		if (data == null) {
			return null;
		}
		List<Store> res = StoreResponse.fromJSON(data);
		if ((res == null) || (res.size() == 0)) {
			status = ERROR_STATUS;
			message = "No valid stores received from web service";
			return null;
		}

		// Return results
		return res;
	}

	/**
	 * Gets the products on which  authenticated user can conduct audits.
	 * @return Products results
	 */
	public List<Product> getProducts() {
		// Verify that we have authenticated
		if (token == null) {
			// Required authentication
			status = ERROR_STATUS;
			message = AUTH_MESSAGE;
			return null;
		}

		// Execute the products query
		String endpoint = baseUrl + "products/" + token;
		JSONArray data = (JSONArray) openClient(endpoint, "get products", true);
		if (data == null) {
			return null;
		}
		List<Product> res = ProductResponse.fromJSON(data);
		if ((res == null) || (res.size() == 0)) {
			status = ERROR_STATUS;
			message = "No valid products received from web service";
			return null;
		}

		// Return results
		return res;
	}

	/**
	 * Posts a payload containing the details of an audit that will be processed by the
	 * back end system.
	 * @param auditJson Audit JSON to post to the server
	 * @return Success flag
	 */
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	public boolean postPayload(String auditJson) {
		// Verify that we have authenticated
		if (token == null) {
			// Required authentication
			status = ERROR_STATUS;
			message = AUTH_MESSAGE;
			return false;
		}

		// Send the payload to the web service
		// TODO Apply token when implemented on the server side
		String endpoint = baseUrl + "payload/v1/";
		return postClient(endpoint, auditJson, "audit");
	}

	/**
	 * Convenience function to pull possibly null strings from JSON results
	 * @param src Source results
	 * @param name Name of field
	 * @return String value
	 */
	static String jsonString(JSONObject src, String name) {
		if (src.isNull(name)) {
			return null;
		}
		return src.optString(name, null);
	}

	/**
	 * Generally call the web service.
	 * @param endpoint Web service endopoint
	 * @param descr Service description for messages
	 * @param isArray Response type is array (vs object)
	 * @return Parsed response data or null on error
	 */
	@SuppressLint("DefaultLocale")
	private Object openClient(String endpoint, String descr, boolean isArray) {
		InputStream ins = null;
		Object res = null;
		try {
			// Execute the web service call
			URL endpointURL = new URL(endpoint);
			client = (HttpsURLConnection) endpointURL.openConnection();
			responseCode = client.getResponseCode();
			if (responseCode != 200) {
				// Report failure
				Log.e(LOG_TAG, String.format("Unexpected error response from %s %d: %s",
						descr, responseCode, client.getResponseMessage()));
				status = ERROR_STATUS;
				message = String.format("Server error (%d) trying to %s", responseCode, descr);
			} else {
				// Read and convert the results
				ins = client.getInputStream();
				Scanner scanner = new Scanner(ins).useDelimiter("\\A");
				String raw = scanner.hasNext() ? scanner.next() : null;
				if (raw == null) {
					// No response body
					status = ERROR_STATUS;
					message = "No response to login request";
				} else {
					// Parse the body as JSON
					JSONObject parsed = new JSONObject(raw);
					status = parsed.optString("status");
					message = parsed.optString("message");
					if (!Objects.equals(status, SUCCESS_STATUS)) {
						// Non success status received
						status = (status == null) ? ERROR_STATUS : status;
						message = (message == null) ? status : message;
					} else if (isArray) {
						// Get the data array
						res = parsed.getJSONArray("data");
					} else {
						// Get the data object
						res = parsed.optJSONObject("data");
					}
				}
			}
		} catch (JSONException excJSON) {
			// Failed to parse login response
			status = ERROR_STATUS;
			message = String.format("Failed to understand %s response from the server", descr);
			Log.e(LOG_TAG, message, excJSON);
		} catch (Exception exc) {
			// Probably net/IO exception fetching login
			status = ERROR_STATUS;
			message = String.format("Failed to contact server for %s", descr);
			Log.e(LOG_TAG, message, exc);
		} finally {
			// Cleanup
			closeClient(ins);
		}

		// Return the result
		return res;
	}

	/**
	 * Post to the web service.
	 * @param endpoint Web service endopoint
	 * @param payload Payload to send to service
	 * @param descr Service description for messages
	 * @return Success flag
	 */
	@SuppressLint("DefaultLocale")
	private boolean postClient(String endpoint, String payload,
		   @SuppressWarnings("SameParameterValue") String descr) {
		InputStream ins = null;
		OutputStream outs = null;
		boolean isSuccess = false;
		try {
			// Execute the web service call
			URL endpointURL = new URL(endpoint);
			client = (HttpsURLConnection) endpointURL.openConnection();
			client.setDoOutput(true);
			client.setRequestMethod("POST");
			client.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
			outs = client.getOutputStream();
			outs.write(payload.getBytes("UTF-8"));
			outs.flush();
			outs.close();
			responseCode = client.getResponseCode();
			if (responseCode != 200) {
				// Log error details
				String logMsg = String.format("Unexpected error response from %s %d",
						descr, responseCode);
				String responseMessage = client.getResponseMessage();
				if (responseMessage != null) {
					logMsg += ": " + responseMessage;
				}
				Log.e(LOG_TAG, logMsg);

				// Format generic readable message
				status = ERROR_STATUS;
				message = String.format("Server error (%d) trying to %s", responseCode, descr);

				// Log extra information to Crashlytics for remote debugging
				try (InputStream ers = client.getErrorStream()) {
					Scanner scanner = new Scanner(ers).useDelimiter("\\A");
					String errorBody = scanner.hasNext() ? scanner.next() : null;
					logMsg += "; " + errorBody;
				} catch (Exception exc) {
					// Failed to read error body
					logMsg += "; Failed to read error body";
				}
				Crashlytics.log(logMsg);
			} else {
				// Read and convert the results
				ins = client.getInputStream();
				Scanner scanner = new Scanner(ins).useDelimiter("\\A");
				String raw = scanner.hasNext() ? scanner.next() : null;
				if (raw == null) {
					// No response body
					status = ERROR_STATUS;
					message = "No response to login request";
				} else {
					JSONObject parsed = new JSONObject(raw);
					status = parsed.optString("status");
					message = parsed.optString("message");
					if (!Objects.equals(status, SUCCESS_STATUS)) {
						// Non success status received
						status = (status == null) ? ERROR_STATUS : status;
						message = (message == null) ? status : message;
					} else {
						// Winner
						isSuccess = true;
					}
				}
			}
		} catch (JSONException excJSON) {
			// Failed to parse server response
			status = ERROR_STATUS;
			message = String.format("Failed to understand %s response from the server", descr);
			Log.e(LOG_TAG, message, excJSON);
		} catch (Exception exc) {
			// Probably net/IO exception fetching response
			status = ERROR_STATUS;
			message = String.format("Failed to contact server for %s", descr);
			Log.e(LOG_TAG, message, exc);
		} finally {
			// Cleanup
			if (outs != null) {
				try {
					outs.close();
				} catch(IOException excIO) {
					Log.w(LOG_TAG, "Unexpected error closing output stream", excIO);
				}
			}
			closeClient(ins);
		}

		// Return the result
		return isSuccess;
	}

	/**
	 * Cleanup after a client transaction.
	 * @param ins Input stream or null
	 */
	private void closeClient(InputStream ins) {
		if (ins != null) {
			try {
				// Close the input stream
				ins.close();
			} catch (IOException exc) {
				// Unexpected error closing the input stream
				Log.w(LOG_TAG, "Unexpected exception closing socket input stream");
			}
		}

		// Now close the client
		client.disconnect();
		client = null;
	}

	/** References current client side connection to the remote web service. */
	private HttpsURLConnection client;

	/** Provides our security token, null if not authenticated. */
	private String token;

	/** Provides the status of the most recent API call. */
	private String status;

	/** Provides a descriptive message of the most recent API call failure, or null on success. */
	private String message;

	/** Provides the HTTP response code from the most recent API call, or 0 if no call. */
	private int responseCode;

	/** Provides the base URL for our web services. */
	private static final String baseUrl = "https://api.auditpro.io/api/";

	/**
	 * Provides shared missing authentication message.
	 * TODO Externalize shared missing authentication message
	 */
	private static final String AUTH_MESSAGE = "Authentication required to access this service";

	/** Identifies our messages in the application log. */
	private static final String LOG_TAG = "ApiClient";
}
