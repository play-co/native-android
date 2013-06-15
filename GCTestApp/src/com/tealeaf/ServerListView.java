/* @license
 * This file is part of the Game Closure SDK.
 *
 * The Game Closure SDK is free software: you can redistribute it and/or modify
 * it under the terms of the Mozilla Public License v. 2.0 as published by Mozilla.

 * The Game Closure SDK is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Mozilla Public License v. 2.0 for more details.

 * You should have received a copy of the Mozilla Public License v. 2.0
 * along with the Game Closure SDK.  If not, see <http://mozilla.org/MPL/2.0/>.
 */
package com.tealeaf;

import com.tealeaf.ServerFinder;
import com.tealeaf.test_app.R;
import com.tealeaf.test_app.TestAppActivity;
import com.tealeaf.ServerFinder.ServerInfo;
import android.text.Spanned;

import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.content.Context;

public class ServerListView extends ListView implements View.OnClickListener, AdapterView.OnItemClickListener {
	private EditText host, port;
	private TestAppActivity activity;
	private Settings settings;
	private ServerFinder finder;
	private boolean hasStarted = false;
	private Button goButton;

	public ServerListView(TestAppActivity activity) {
		super(activity);
		this.activity = activity;

		ArrayAdapter<Spanned> adapter = new ArrayAdapter<Spanned>(activity, R.layout.simpletextview);

		settings = new Settings(activity);

		View v = activity.getLayoutInflater().inflate(R.layout.serverinput, null);

		host = (EditText)v.findViewById(R.id.serverHost);
		host.setText(settings.getLastHost());


		port = (EditText)v.findViewById(R.id.serverPort);
		port.setText(Integer.toString(settings.getLastPort()));

		goButton = (Button)v.findViewById(R.id.goButton);
		goButton.setOnClickListener(this);

		addHeaderView(v);
		setOnItemClickListener(this);

		setAdapter(adapter);
		this.finder = new ServerFinder(activity);
		finder.setAdapter(adapter);
	}

	public void refresh() {
		ArrayAdapter<Spanned> adapter = new ArrayAdapter<Spanned>(activity, R.layout.simpletextview);
		setAdapter(adapter);
		this.finder = new ServerFinder(activity);
		finder.setAdapter(adapter);
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onClick(View v) {

		if (v == goButton) {

			if (hasStarted) {
				return;
			}
			hasStarted = true;

			InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(host.getWindowToken(), 0);
			imm.hideSoftInputFromWindow(port.getWindowToken(), 0);
			String hostValue = host.getText().toString();
			int portValue = Integer.parseInt(port.getText().toString());
			settings.setLastServer(hostValue, portValue);


			this.activity.setHost(hostValue);
			this.activity.setPort(portValue);
			this.activity.setIsTestApp(true);
			this.activity.switchToApps();

		}
	}

	@Override
	public void onItemClick(AdapterView<?> a, View v, int position, long id) {
		// Prevent two fast mouse clicks from starting game twice causing a crash
		if (hasStarted) {
			return;
		}
		hasStarted = true;

		ServerInfo server = finder.getServer(position - 1);
		if (server == null) return;
		settings.setLastServer(server.host, server.port);
		this.activity.setHost(server.host);
		this.activity.setPort(server.port);
		this.activity.setIsTestApp(true);
		this.activity.switchToApps();
	}

	public void reset() {
		String lastHost = settings.getLastHost();
		int lastPort = settings.getLastPort();
		host.setText(lastHost);
		port.setText(Integer.toString(lastPort));
		hasStarted = false;
	}
}
