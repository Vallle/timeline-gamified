package com.bjorsond.android.timeline;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.bjorsond.android.timeline.reflectionspace.ReflectionSpaceHandler;
import com.swarmconnect.SwarmActivity;

public class ReflectionSpaceNewUserActivity extends SwarmActivity{
	
	private EditText userName, password, email, name;
	private Button createNew;
	private Intent loginUserIntent;
	private ImageButton homeButton;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reflectionspacenewuser);
		
		loginUserIntent = new Intent(this, ReflectionSpaceUserActivity.class);
		setupViews();
	}
	
	//LISTENER
	private OnClickListener createUserListener = new OnClickListener() {
		public void onClick(View v) {
			//Create the user on the Openfire XMPP server
			ReflectionSpaceHandler.createUserOnServer(userName.getText().toString(), password.getText().toString(), name.getText().toString(), email.getText().toString());
			
			Toast.makeText(ReflectionSpaceNewUserActivity.this, R.string.User_created_toast, Toast.LENGTH_SHORT).show();
			
			//Send username and password to the login screen so the user can login by the press of one button
			Bundle extras = new Bundle();
			extras.putString("EXTRA_USERNAME", userName.getText().toString());
			extras.putString("EXTRA_PASSWORD", password.getText().toString());
			loginUserIntent.putExtras(extras);
			startActivity(loginUserIntent);
		}
	};
	
	private void setupViews(){
		userName = (EditText) findViewById(R.id.newReflectionUsername);
		password = (EditText) findViewById(R.id.newReflectionUserPassword);
		name = (EditText) findViewById(R.id.newReflectionUserYourName);
		email = (EditText) findViewById(R.id.newReflectionUserMail);
		
		createNew = (Button) findViewById(R.id.newRefSpaceUserButton);
		createNew.setOnClickListener(createUserListener);
		
		homeButton = (ImageButton) findViewById(R.id.refSpaceCreateUserHeaderHomeButton);
		homeButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				finish();
			}
		});
	}
}
