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

public class LeaderboardActivity extends SwarmActivity{
	
	private Intent dashboardIntent;
	private ImageButton homeButton;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.leaderboardactivitylayout);
		
		dashboardIntent = new Intent(this, DashboardActivity.class);
		setupViews();
	}
	
	//LISTENER
	private OnClickListener createUserListener = new OnClickListener() {
		public void onClick(View v) {
			
		}
	};
	private OnClickListener loginUserListener = new OnClickListener() {
		public void onClick(View v) {
			
		}
	};
	
	private void setupViews(){
//		userName = (EditText) findViewById(R.id.reflectionUsername);
//		password = (EditText) findViewById(R.id.reflectionPassword);
		
		homeButton = (ImageButton) findViewById(R.id.refSpaceLoginHeaderHomeButton);
		homeButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				finish();
			}
		});
	}
}
