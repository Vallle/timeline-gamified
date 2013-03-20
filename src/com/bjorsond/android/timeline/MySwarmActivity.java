package com.bjorsond.android.timeline;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.swarmconnect.Swarm;
import com.swarmconnect.SwarmActivity;

/**
 * The activity containing the Swarm dashboard view
 * 
 * 
 * @author Bjørnar
 *
 */

public class MySwarmActivity extends SwarmActivity {

	private ImageButton achievements, swarm_profile, messages, friends, leaderboard, store;
	
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gamification_dashboard);

		//Setting Swarm active
		Swarm.setActive(this);
		
		setupViews();
	}
	
	private void openSwarmProfile() {
		Context context = getApplicationContext();
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(context, R.string.Not_implemented_toast, duration);
		toast.show();
	}	
	
	private void openSwarmLeaderboard() {
		Swarm.showLeaderboards();
	}	
	
	private void openSwarmAchievements() {
		Swarm.showAchievements();
	}
	
	private void openSwarmStore() {
		Swarm.showStore();
	}
	
	private void openSwarmMessages() {
		Swarm.show(5);
	}
	
	private void openSwarmFriends() {
		Swarm.show(3);
	}
	
	
	
	
	
	//LISTENERS
	
	
	private android.view.View.OnClickListener profileButtonListener = new View.OnClickListener() {
		
		public void onClick(View v) {
			openSwarmProfile();
		}
	};
	
	private android.view.View.OnClickListener achievementButtonListener = new View.OnClickListener() {
		
		public void onClick(View v) {
			openSwarmAchievements();
		}
	};
	
	private android.view.View.OnClickListener messagesButtonListener = new View.OnClickListener() {
		
		public void onClick(View v) {
			openSwarmMessages();
		}
	};
	
	private android.view.View.OnClickListener friendsButtonListener = new View.OnClickListener() {
		
		public void onClick(View v) {
			openSwarmFriends();
		}
	};
	
	private android.view.View.OnClickListener leaderboardButtonListener = new View.OnClickListener() {
		
		public void onClick(View v) {
			openSwarmLeaderboard();
		}
	};
	
	private android.view.View.OnClickListener storeButtonListener = new View.OnClickListener() {
		
		public void onClick(View v) {
			openSwarmStore();
		}
	};
	
	
	private void setupViews() {
		achievements = (ImageButton) findViewById(R.id.dash_achievements);
		achievements.setOnClickListener(achievementButtonListener);
		
		leaderboard = (ImageButton) findViewById(R.id.dash_leaderboard);
		leaderboard.setOnClickListener(leaderboardButtonListener);
		
		store = (ImageButton) findViewById(R.id.dash_store);
		store.setOnClickListener(storeButtonListener);
		
		friends = (ImageButton) findViewById(R.id.dash_friends);
		friends.setOnClickListener(friendsButtonListener);
		
		messages = (ImageButton) findViewById(R.id.dash_messages);
		messages.setOnClickListener(messagesButtonListener);
		
		swarm_profile = (ImageButton) findViewById(R.id.dash_swarm_profile);
		swarm_profile.setOnClickListener(profileButtonListener);
	}
		
	//Override to setActive/Inactive Swarm
		@Override
		public void onResume() {
		    super.onResume();
		    Swarm.setActive(this);
		}
		@Override
		public void onPause() {
		    super.onPause();
		    Swarm.setInactive(this);
		}
		
}
