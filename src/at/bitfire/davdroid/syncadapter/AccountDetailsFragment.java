/*******************************************************************************
 * Copyright (c) 2014 Richard Hirner (bitfire web engineering).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Richard Hirner (bitfire web engineering) - initial API and implementation
 ******************************************************************************/
package at.bitfire.davdroid.syncadapter;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import at.bitfire.davdroid.Constants;
import at.bitfire.davdroid.R;

public class AccountDetailsFragment extends Fragment implements TextWatcher {
	public static final String KEY_SERVER_INFO = "server_info";
	
	ServerInfo serverInfo;
	
	EditText editAccountName;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.account_details, container, false);
		
		serverInfo = (ServerInfo)getArguments().getSerializable(KEY_SERVER_INFO);
		
		editAccountName = (EditText)v.findViewById(R.id.account_name);
		editAccountName.addTextChangedListener(this);
		
		TextView textAccountNameInfo = (TextView)v.findViewById(R.id.account_name_info);
		if (!serverInfo.hasEnabledCalendars())
			textAccountNameInfo.setVisibility(View.GONE);
	
		setHasOptionsMenu(true);
		return v;
	}
	
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	    inflater.inflate(R.menu.account_details, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.add_account:
			addAccount();
			break;
		default:
			return false;
		}
		return true;
	}


	// actions
	
	void addAccount() {
		ServerInfo serverInfo = (ServerInfo)getArguments().getSerializable(KEY_SERVER_INFO);
			String accountName = editAccountName.getText().toString();
			
			AccountManager accountManager = AccountManager.get(getActivity());
			Account account = new Account(accountName, Constants.ACCOUNT_TYPE);
			Bundle userData = new Bundle();
			userData.putString(Constants.ACCOUNT_KEY_BASE_URL, serverInfo.getBaseURL());
			userData.putString(Constants.ACCOUNT_KEY_USERNAME, serverInfo.getUserName());
			userData.putString(Constants.ACCOUNT_KEY_AUTH_PREEMPTIVE, Boolean.toString(serverInfo.isAuthPreemptive()));
			
			boolean syncContacts = false;
			for (ServerInfo.ResourceInfo addressBook : serverInfo.getAddressBooks())
				if (addressBook.isEnabled()) {
					userData.putString(Constants.ACCOUNT_KEY_ADDRESSBOOK_PATH, addressBook.getPath());
					ContentResolver.setIsSyncable(account, ContactsContract.AUTHORITY, 1);
					syncContacts = true;
					continue;
				}
			if (syncContacts) {
				ContentResolver.setIsSyncable(account, ContactsContract.AUTHORITY, 1);
				ContentResolver.setSyncAutomatically(account, ContactsContract.AUTHORITY, true);
			} else
				ContentResolver.setIsSyncable(account, ContactsContract.AUTHORITY, 0);
			
			if (accountManager.addAccountExplicitly(account, serverInfo.getPassword(), userData)) {
				// account created, now create calendars
				ContentResolver.setIsSyncable(account, "com.android.calendar", 0);
				
				getActivity().finish();				
			} else
				Toast.makeText(getActivity(), "Couldn't create account (account with this name already existing?)", Toast.LENGTH_LONG).show();
	}

	
	// input validation
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		boolean ok = false;
		ok = editAccountName.getText().length() > 0;
		MenuItem item = menu.findItem(R.id.add_account);
		item.setEnabled(ok);
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		getActivity().supportInvalidateOptionsMenu();
	}

	@Override
	public void afterTextChanged(Editable s) {
	}
}
