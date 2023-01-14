/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.pages;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.auditpro.mobile_client.MainActivity;
import com.auditpro.mobile_client.api.ApiClient;
import com.auditpro.mobile_client.api.UserResponse;
import com.auditpro.mobile_client.security.Security;
import com.auditpro.mobile_client.test.R;

import java.lang.ref.WeakReference;


/**
 * Implements the login "page" as a fragment.
 * @author Eric Ruck
 */
@SuppressWarnings("ConstantConditions")
public class LoginPage extends BasePage {

	/**
	 * Required empty public constructor.
	 */
	public LoginPage() { }

	/**
	 * Creates a new instance to get a real login from a cached session.
	 */
	public static LoginPage sessionInstance() {
		LoginPage fragment = new LoginPage();
		Bundle args = new Bundle();
		args.putBoolean(ARG_SESSION_FLAG, true);
		fragment.setArguments(args);
		return fragment;
	}

	/**
	 * Handles initial creation.
	 * @param savedInstanceState Optional state
	 */
	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			sessionFlag = getArguments().getBoolean(ARG_SESSION_FLAG, false);
		}
	}

	/**
	 * Creates the view for the login page fragment.
	 * @param inflater Layout inflater to use
	 * @param container Parent for inflated view
	 * @param savedInstanceState Optional state
	 * @return Created view
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_login_page, container, false);
	}

	/**
	 * Initialize the view once it has been created.
	 * @param view Created view
	 * @param savedInstanceState Optional state
	 */
	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Wire up controls
		emailText = getView().findViewById(R.id.emailText);
		passwordText = getView().findViewById(R.id.passwordText);
		savePasswordCheck = getView().findViewById(R.id.savePasswordCheck);
		passwordText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
				if ((i == EditorInfo.IME_ACTION_UNSPECIFIED) && (keyEvent != null) &&
						(keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
					if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
						return true;
					}
					i = EditorInfo.IME_ACTION_DONE;
				}
				if (i == EditorInfo.IME_ACTION_DONE) {
					if (!loginButton.isEnabled()) {
						// Still need to enter a valid e-mail
						emailText.requestFocus();
					} else {
						// Attempt login
						onLogin();
					}
					return true;
				}
				return false;
			}
		});
		passwordText.addTextChangedListener(new TextWatcher() {
			@Override public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
			@Override public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
			@Override public void afterTextChanged(Editable editable) {
				onLoginInputChanged();
			}
		});
		loginButton = getView().findViewById(R.id.loginButton);
		loginButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onLogin();
			}
		});
		getView().findViewById(R.id.helpButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				onHelp();
			}
		});

		// Can we set the version?
		String version = getParent().getVersion();
		TextView versionView = getView().findViewById(R.id.versionText);
		if (version != null) {
			// Set the version
			versionView.setText(
					getString(R.string.app_version, version)
			);
		} else {
			// Clear the version, warning already logged
			versionView.setText("");
		}
	}

	/**
	 * Cancels login task in progress on stop.
	 */
	@Override
	public void onStop() {
		super.onStop();
		ConnectedLoginTask inProgress = (loginTask != null) ? loginTask.get() : null;
		if (inProgress != null) {
			inProgress.cancel(true);
		}
	}

	/**
	 * Updates the user interface when the page appears.
	 */
	@Override
	public void onPageAppearing() {
		// Calls default implementation
		super.onPageAppearing();
		if (preserveInput) {
			// Don't reset the input
			preserveInput = false;
		} else {
			// Reinitialize our edits
			Security sec = new Security(getContext().getApplicationContext());
			String savedPassword = sec.getSavedPassword();
			emailText.setText(sec.getLastEmail());
			passwordText.setText(savedPassword);
			savePasswordCheck.setChecked(savedPassword.length() > 0);
			onLoginInputChanged();
		}
	}

	/**
	 * Updates the login button depending on the validity of the input state.
	 */
	private void onLoginInputChanged() {
		String email = emailText.getText().toString();
		String password = passwordText.getText().toString();
		boolean isLoginEnabled = (password.length() > 0) &&
				(email.matches(EMAIL_REGEX));
		loginButton.setEnabled(isLoginEnabled);
	}

	/**
	 * Handles request to login using entered credentials
	 */
	private void onLogin() {
		// Make sure the keyboard is dismissed
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(passwordText.getWindowToken(), 0);

		// Do we have a network connection?
		if (((MainActivity) getActivity()).isNetworkConnected()) {
			loginConnected();
		} else if (sessionFlag) {
			Toast.makeText(getContext(), R.string.message_login_inet_req, Toast.LENGTH_SHORT).show();
		} else {
			loginOffline();
		}
	}

	/**
	 * Provides reference-safe async task to login on a worker thread.
	 */
	private static class ConnectedLoginTask extends AsyncTask<Void, Void, UserResponse> {
		private String email;
		private String password;
		private boolean isPasswordSaved;
		private WeakReference<LoginPage> host;
		private ApiClient api;

		/**
		 * Initialize task to call the remote log in web service.
		 * @param host Parent host page
		 */
		ConnectedLoginTask(LoginPage host) {
			this.email = host.emailText.getText().toString();
			this.password = host.passwordText.getText().toString();
			this.isPasswordSaved = host.savePasswordCheck.isChecked();
			this.host = new WeakReference<>(host);
		}

		/**
		 * Shows activity spinner while we run.
		 */
		@Override
		protected void onPreExecute() {
			LoginPage page = host.get();
			IPageParent parent = (page == null) ? null : page.getParent();
			if (parent != null) {
				parent.setActivity(true);
			}
		}

		/**
		 * Execute remove login web service
		 * @param voids Placeholder for no parameters
		 * @return Web service result
		 */
		@Override
		protected UserResponse doInBackground(Void... voids) {
			api = new ApiClient(null);
			return api.login(email, password);
		}

		/**
		 * Handles the web service result in the user interface.
		 * @param userResponse Web service result
		 */
		@Override
		protected void onPostExecute(UserResponse userResponse) {
			super.onPostExecute(userResponse);
			LoginPage self = host.get();
			IPageParent parent = (self == null) ? null : self.getParent();
			if (parent == null) {
				// The page was popped before we completed
				return;
			}
			parent.setActivity(false);
			if (userResponse == null) {
				// Failed to login, show error toast
				Toast.makeText(self.getContext(), api.getMessage(), Toast.LENGTH_LONG).show();
			} else {
				// Complete login
				((MainActivity) self.getActivity()).setLogin(api.getToken(), email, password, isPasswordSaved, userResponse);
				if (self.sessionFlag) {
					// Return to the calling page
					parent.popPage();
				} else {
					// Go to the main menu
					parent.pushPage(new MainMenuPage());
				}
			}
		}
	}

	private WeakReference<ConnectedLoginTask> loginTask;

	/**
	 * Validates the login against remote web services.
	 */
	private void loginConnected() {
		loginTask = new WeakReference<>(new ConnectedLoginTask(this));
		loginTask.get().execute();
	}

	/**
	 * Logs in the user with cached credentials.
	 */
	private void loginOffline() {
		// Do we have cached credentials?
		Security security = new Security(getContext().getApplicationContext());
		String lastEmail = security.getLastEmail();
		if (lastEmail == null)  {
			// No cached credentials
			Toast.makeText(getContext(), R.string.message_login_inet_first, Toast.LENGTH_LONG).show();
		} else if (!lastEmail.toLowerCase().equals(emailText.getText().toString().toLowerCase()) ||
				!security.verifyLastPassword(passwordText.getText().toString(), savePasswordCheck.isChecked())) {
			// Credentials don't match
			Toast.makeText(getContext(), R.string.message_login_incorrect, Toast.LENGTH_SHORT).show();
		} else {
			// We're logged in
			IPageParent parent = getParent();
			if (parent != null) {
				// Show the main menu
				parent.pushPage(new MainMenuPage());
			}
		}
	}

	/**
	 * Handles request to show help.
	 */
	private void onHelp() {
		IPageParent parent = getParent();
		if (parent != null) {
			// Show the help screen
			preserveInput = true;
			parent.pushPage(HelpPage.newInstance("LoginHelp.html"));
		}
	}

	private static final String ARG_SESSION_FLAG = "sessionFlag";
	private boolean sessionFlag;

	private EditText emailText;
	private EditText passwordText;
	private Button loginButton;
	private CheckBox savePasswordCheck;
	private boolean preserveInput;

	private static final String EMAIL_REGEX = "\\A(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?)\\Z";
}
