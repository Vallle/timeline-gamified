package com.bjorsond.android.timeline;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.bjorsond.android.timeline.reflectionspace.ReflectionSpaceUserPreferences;
import com.swarmconnect.SwarmActivity;

public class ReflectionSpaceUserActivity extends SwarmActivity{
	
	private EditText userName, password;
	private Button login, createNew;
	private Intent createUserIntent, dashboardIntent;
	private ImageButton homeButton;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reflectionspacelogin);
		
		createUserIntent = new Intent(this, ReflectionSpaceNewUserActivity.class);
		dashboardIntent = new Intent(this, DashboardActivity.class);
		setupViews();
	}
	
	//LISTENER
	private OnClickListener createUserListener = new OnClickListener() {
		public void onClick(View v) {
			startActivity(createUserIntent);
		}
	};
	private OnClickListener loginUserListener = new OnClickListener() {
		public void onClick(View v) {
			ReflectionSpaceUserPreferences userPrefs = ReflectionSpaceUserPreferences.load(getApplicationContext());
			
			Log.i("GET USER+PW BEFORE SET", userPrefs.getString(ReflectionSpaceUserPreferences.PREF_USER_NAME, null)+"+"+userPrefs.getString(ReflectionSpaceUserPreferences.PREF_PASSWORD, null));
			
			userPrefs.putString(ReflectionSpaceUserPreferences.PREF_USER_NAME, userName.getText().toString());
			userPrefs.putString(ReflectionSpaceUserPreferences.PREF_PASSWORD, password.getText().toString());
			
			Log.i("GET USER+PW", userPrefs.getString(ReflectionSpaceUserPreferences.PREF_USER_NAME, null)+"+"+userPrefs.getString(ReflectionSpaceUserPreferences.PREF_PASSWORD, null));
			
			Toast.makeText(ReflectionSpaceUserActivity.this, R.string.User_logged_in_toast, Toast.LENGTH_SHORT).show();
			
			dashboardIntent.setAction(Intent.ACTION_MAIN);
			startActivity(dashboardIntent);
		}
	};
	
	private void setupViews(){
		userName = (EditText) findViewById(R.id.reflectionUsername);
		password = (EditText) findViewById(R.id.reflectionPassword);
		
		login = (Button) findViewById(R.id.loginRefSpaceButton);
		login.setOnClickListener(loginUserListener);
		
		createNew = (Button) findViewById(R.id.newRefSpaceUserButton);
		createNew.setOnClickListener(createUserListener);
		
		homeButton = (ImageButton) findViewById(R.id.refSpaceLoginHeaderHomeButton);
		homeButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				finish();
			}
		});
		
		if(getIntent().getExtras() != null){
			userName.setText(getIntent().getExtras().getString("EXTRA_USERNAME"));
			password.setText(getIntent().getExtras().getString("EXTRA_PASSWORD"));
		}
	}
}
