package com.bjorsond.android.timeline;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.swarmconnect.SwarmActivity;

public class ReflectionSpaceUserActivity extends SwarmActivity{
	
	private EditText userName, password;
	private Button login, createNew;
	private Intent createUserIntent;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reflectionspacelogin);
		
		createUserIntent = new Intent(this, ReflectionSpaceNewUserActivity.class);
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
			DashboardActivity.setReflectionSpaceUserName();
			DashboardActivity.setReflectionSpaceUserPass();
		}
	};
	
	private void setupViews(){
		userName = (EditText) findViewById(R.id.reflectionUsername);
		password = (EditText) findViewById(R.id.reflectionPassword);
		
		login = (Button) findViewById(R.id.loginRefSpaceButton);
		login.setOnClickListener(loginUserListener);
		
		createNew = (Button) findViewById(R.id.newRefSpaceUserButton);
		createNew.setOnClickListener(createUserListener);
	}
}
