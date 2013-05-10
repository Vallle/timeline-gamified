package com.bjorsond.android.timeline;

import java.util.ArrayList;
import java.util.Map;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bjorsond.android.timeline.R;
import com.bjorsond.android.timeline.adapters.AchievementsAdapter;
import com.swarmconnect.SwarmAchievement;
import com.swarmconnect.SwarmAchievement.GotAchievementsMapCB;
import com.swarmconnect.SwarmActivity;

public class AchievementsScreen extends SwarmActivity {
	
	protected static Map<Integer, SwarmAchievement> achievements;
	private ListView listView;

	
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.achievements_screen);
		
		listView = (ListView)findViewById(R.id.achievements_items_list);
		
		
		// Call to Swarm to get the game's Achievements Map
		SwarmAchievement.getAchievementsMap(achievementsCallback); 
	}

	
	private GotAchievementsMapCB achievementsCallback = new GotAchievementsMapCB() {
		
	    public void gotMap(Map<Integer, SwarmAchievement> achievements) {
	    	
	        // Store the map of achievements somewhere to be used later.
	        AchievementsScreen.achievements = achievements;
	        
	        if (achievements != null) {
	        	
	        	// If achievements come back, then display them in the achievements list
	        	listView.setAdapter(new AchievementsAdapter(getApplicationContext(), R.id.achievements_title, new ArrayList<SwarmAchievement>(achievements.values())));
	        	
	        } else {
	        	
	        	// If null is returned (due to an error), then display a toast to remind the user to check their network connection
	            Toast.makeText(getApplicationContext(), R.string.connection_to_swarm_failed_check_your_network_connection, Toast.LENGTH_LONG).show();
	        	TextView textView = new TextView(getApplicationContext());
	        	
	        	// Display that no achievements were found
	        	textView.setText(R.string.no_achievements_found);
	        	listView.setEmptyView(textView);
	        }
	    }
	}; 
}
