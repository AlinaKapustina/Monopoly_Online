package com.icerockdev.icedroid.signinexample.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.icerockdev.icedroid.signinexample.R;
import com.icerockdev.icedroid.signinexample.utils.AuthorizationUtils;

public class MainActivity extends AppCompatActivity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

//		Here is a checkpoint if the user is authorized.
		if (!AuthorizationUtils.isAuthorized(this)) {

			onLogout();
			return;
		}

		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View view)
			{
				onFabClick();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId()) {
			case R.id.action_logout: {
				AuthorizationUtils.logout(this);
				onLogout();
				return true;
			}
			default: {
				return super.onOptionsItemSelected(item);
			}
		}
	}

	//	If user is not authorized we finish the main activity
	private void onLogout()
	{
		Intent login = new Intent(this, LoginActivity.class);
		login.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(login);
		finish();
	}

	private void onFabClick()
	{
		Toast.makeText(this, R.string.lorem_ipsum, Toast.LENGTH_LONG).show();
	}
}
