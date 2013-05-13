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
import com.swarmconnect.SwarmActivity;
import com.swarmconnect.SwarmLeaderboard;
import com.swarmconnect.SwarmLeaderboard.DateRange;
import com.swarmconnect.SwarmLeaderboard.GotLeaderboardCB;
import com.swarmconnect.SwarmLeaderboard.GotScoresCB;
import com.swarmconnect.SwarmLeaderboard.SubmitScoreCB;
import com.swarmconnect.SwarmLeaderboardScore;

public class LeaderboardActivity extends SwarmActivity{
	
	private TextView leaderboardText;
	private ImageView homeButton;
	private ListView listView;
	protected static SwarmLeaderboard leaderboard;
    boolean isSubmitScoreIntent = false;
    int score = 0;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.leaderboardactivitylayout);
		
		setupViews();
		
		SwarmLeaderboard.getLeaderboardById(Constants.leaderboardID, callback);
	}
	
	
	private GotLeaderboardCB callback = new GotLeaderboardCB() {

        public void gotLeaderboard(SwarmLeaderboard leaderboard) {
        	
        	// If Swarm returns valid SwarmLeaderboard data
            if (leaderboard != null) {
            	
            	// Save the leaderboard data for future use
                LeaderboardActivity.leaderboard = leaderboard;
                
                // Submit the users score (if needed)
                if (isSubmitScoreIntent) {
                    submitScore();
                }
                
                // Display the leaderboard list to the user
                showLeaderBoardList();
                
            } else {
            	
            	// If Swarm returns null SwarmLeaderboard data (due to an error), tell the user to check their network connection
                Toast.makeText(getApplicationContext(), R.string.connection_to_swarm_failed_check_your_network_connection, Toast.LENGTH_LONG).show();
            }
        }
    };
	
    
    
    // Get the SwarmLeaderboardScore's from the SwarmLeaderboard and populate the list shown on the Leaderboard Screen
    private void showLeaderBoardList() {
    	leaderboard.getPageOfScoresForCurrentUser(DateRange.ALL, gotScoresCB);
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

    // Submit a score to the leaderboard (powered by Swarm)
    private void submitScore() {
    	
        leaderboard.submitScore(score, "", new SubmitScoreCB() {

            @Override
            public void scoreSubmitted(int arg0) {
                showLeaderBoardList();
            }
        });
    }
	
	
	private void setupViews(){
		listView = (ListView)findViewById(R.id.leaderboard_items_list);
		
		leaderboardText = (TextView)findViewById(R.id.leaderboardText);
		leaderboardText.setText(R.string.Leaderboard_headline_label);
		
		homeButton = (ImageView) findViewById(R.id.leaderboardHeader);
		homeButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
	}
}
