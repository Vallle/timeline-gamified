package com.bjorsond.android.timeline;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bjorsond.android.timeline.reflectionspace.ReflectionSpaceHandler;
import com.swarmconnect.SwarmActivity;

public class ReflectionSpaceNewUserActivity extends SwarmActivity{
	
	private EditText userName, password, email;
	private Button createNew;
	private Intent loginUserIntent;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reflectionspacenewuser);
		
		loginUserIntent = new Intent(this, ReflectionSpaceUserActivity.class);
		setupViews();
	}
	
	//LISTENER
	private OnClickListener createUserListener = new OnClickListener() {
		public void onClick(View v) {
			ReflectionSpaceHandler.createUserOnServer(userName.getText().toString(), password.getText().toString(), email.getText().toString());
			
			Toast.makeText(ReflectionSpaceNewUserActivity.this, R.string.User_created_toast, Toast.LENGTH_SHORT).show();
			startActivity(loginUserIntent);
		}
	};
	
	private void setupViews(){
		userName = (EditText) findViewById(R.id.newReflectionUsername);
		password = (EditText) findViewById(R.id.newReflectionUserPassword);
		email = (EditText) findViewById(R.id.newReflectionUserMail);
		
		createNew = (Button) findViewById(R.id.newRefSpaceUserButton);
		createNew.setOnClickListener(createUserListener);
	}
}
