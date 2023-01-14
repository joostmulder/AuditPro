/*
  AuditPRO Mobile Client Android
  Copyright 2018 AuditPRO All Rights Reserved
 */
package com.auditpro.mobile_client.pages;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.auditpro.mobile_client.test.R;


/**
 * Manages the help page of the application.
 * @author Eric Ruck
 */
public class HelpPage extends BasePage {

	private static final String ARG_HELP_FILE = "helpFile";
	private String helpFile;

	/**
	 * Required default constructor for fragment.
	 */
	public HelpPage() { }

	/**
	 * Creates a new instance to show a help page.
	 * @param helpFile Name of help file
	 * @return A new instance of fragment HelpPage.
	 */
	public static HelpPage newInstance(String helpFile) {
		HelpPage fragment = new HelpPage();
		Bundle args = new Bundle();
		args.putString(ARG_HELP_FILE, helpFile);
		fragment.setArguments(args);
		return fragment;
	}

	/**
	 * Restores saved state and arguments on creation.
	 * @param savedInstanceState Optional saved state
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {
			helpFile = getArguments().getString(ARG_HELP_FILE);
		}
	}

	/**
	 * Creates fragment view.
	 * @param inflater Inflater to use on layout
	 * @param container Container receives inflated view
	 * @param savedInstanceState Optional instance state
	 * @return View for fragment
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_help_page, container, false);
	}

	/**
	 * Initializes created view.
	 * @param view Created view parent
	 * @param savedInstanceState Optional instance state
	 */
	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		WebView webView = view.findViewById(R.id.webView);
		getParent().setActivity(true);
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				IPageParent parent = getParent();
				if (parent != null) {
					parent.setActivity(false);
				}
			}

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
				// Process what to do with the request
				Uri url = request.getUrl();
				if (url.getScheme().startsWith("http") || url.getScheme().startsWith("mailto")) {
					// Open the browser
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setData(Uri.parse(url.toString()));
					startActivity(i);
				} else {
					// Unable to handle for now, log warning
					Log.w(LOG_TAG, "Unable to handle request: " + url.toString());
				}

				// Prevent web view handling
				return true;
			}
		});
		webView.loadUrl("file:///android_asset/" + helpFile);
	}

	@Override
	public String getPageName(Context context) {
		return context.getString(R.string.page_name_help);
	}

	private static final String LOG_TAG = "HelpPage";
}
