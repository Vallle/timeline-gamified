package com.bjorsond.android.timeline;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bjorsond.android.timeline.adapters.LeaderboardAdapter;
import com.bjorsond.android.timeline.reflectionspace.ReflectionSpaceUserPreferences;
import com.bjorsond.android.timeline.utilities.Constants;
import com.swarmconnect.Swarm;
import com.swarmconnect.SwarmActiveUser;
import com.swarmconnect.SwarmUser;
import com.swarmconnect.SwarmActiveUser.GotFriendsCB;
import com.swarmconnect.SwarmActivity;
import com.swarmconnect.SwarmLeaderboard;
import com.swarmconnect.SwarmLeaderboard.DateRange;
import com.swarmconnect.SwarmLeaderboard.GotLeaderboardCB;
import com.swarmconnect.SwarmLeaderboard.GotScoresCB;
import com.swarmconnect.SwarmLeaderboard.SubmitScoreCB;
import com.swarmconnect.SwarmLeaderboardScore;

public class FriendsActivity extends SwarmActivity{
	
	private TextView friendsText;
	private ImageView homeButton;
	private ListView listView;
    int numberOfAchievements = 0;
    private static List<SwarmUser> friends;
	
    
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friends_activity_layout);
		
		setupViews();
	}
	
	
	private GotFriendsCB callback = new GotFriendsCB() {
		
		@Override
		public void gotFriends(List<SwarmUser> friends, List<SwarmUser> friendsRequest) {
			// If Swarm returns valid friends list
			if (friends != null) {
            	
            	// Save the leaderboard data for future use
                FriendsActivity.friends = friends;
                
                // Display the leaderboard list to the user
                showFriendsList(); 
			} else {
            	
            	// If Swarm returns null SwarmLeaderboard data (due to an error), tell the user to check their network connection
                Toast.makeText(getApplicationContext(), R.string.connection_to_swarm_failed_check_your_network_connection, Toast.LENGTH_LONG).show();
            }
		}; 
  
    };
	
    
    
    // Get the SwarmLeaderboardScore's from the SwarmLeaderboard and populate the list shown on the Leaderboard Screen
    private void showFriendsList() {
    	
    }

    
    private GotScoresCB gotScoresCB = new GotScoresCB() {

        @Override
        public void gotScores(int arg0, List<SwarmLeaderboardScore> list) {
        	
            if (list != null) {
            	
                int position = 0;
                
                for (SwarmLeaderboardScore score : list) {
                	
                    if (score.user.userId == Swarm.user.userId) {
                        position = list.indexOf(score);
                    }
                    
                }
                
                listView.setVisibility(View.VISIBLE);
                
                LeaderboardAdapter adapter = new LeaderboardAdapter(getApplicationContext(), R.id.leaderboard_playername, new ArrayList<SwarmLeaderboardScore>(list)); 
                
                listView.setAdapter(adapter);
                listView.setSelection(position);
            }
        }
    };

    
	
	
	//LISTENER
	private OnClickListener createUserListener = new OnClickListener() {
		public void onClick(View v) {
			
		}
	};
	
	private void setupViews(){
		listView = (ListView)findViewById(R.id.friends_items_list);
		
		friendsText = (TextView)findViewById(R.id.leaderboardText);
		friendsText.setText(R.string.Leaderboard_headline_label);
		
		homeButton = (ImageView) findViewById(R.id.leaderboardHeader);
		homeButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
	}
}
