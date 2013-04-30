package com.bjorsond.android.timeline;

import android.os.Bundle;
import android.widget.EditText;

import com.swarmconnect.SwarmActivity;

public class ReflectionSpaceNewUserActivity extends SwarmActivity{
	
	private EditText userName, password;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reflectionspacelogin);
		
		setupViews();
	}
	
	private void setupViews(){
		userName = (EditText) findViewById(R.id.reflectionUsername);
		userName.setOnClickListener();
		
		
	}
}
