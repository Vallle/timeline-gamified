package com.bjorsond.android.timeline.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bjorsond.android.timeline.R;
import com.swarmconnect.SwarmLeaderboardScore;

// An ArrayAdapter for handling SwarmLeaderboardScore's and populating
// their data into the Leaderboard that is displayed to the user
public class LeaderboardAdapter extends ArrayAdapter<SwarmLeaderboardScore> {

	int color1;
	int color2;
	private Typeface font = Typeface.DEFAULT;

	public LeaderboardAdapter(Context context, int textViewResourceId,
		ArrayList<SwarmLeaderboardScore> items) {
		
		super(context, textViewResourceId, items);
        color1 = context.getResources().getColor(R.color.list_item_color1);
        color2 = context.getResources().getColor(R.color.list_item_color2);
	}

	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) getContext().getSystemService(
					Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(R.layout.leaderboard_list_item, null);
		}
		
		SwarmLeaderboardScore swarmLeaderboardScore = getItem(position);
		TextView positionTextView = (TextView) v.findViewById(R.id.leaderboard_position);
		positionTextView.setTypeface(font);
		
		TextView scoreTextView = (TextView) v.findViewById(R.id.leaderboard_playername);
		scoreTextView.setTypeface(font);
		
		TextView nameTextView = (TextView) v.findViewById(R.id.leaderboard_points);
		nameTextView.setTypeface(font);
		
		positionTextView.setText("#" + swarmLeaderboardScore.rank);
		scoreTextView.setText(" " + (int)swarmLeaderboardScore.score);
		nameTextView.setText(swarmLeaderboardScore.user.username);

		if(position % 2 == 1){
			v.setBackgroundColor(color1);
		}else{
			v.setBackgroundColor(color2);
		}
		
		return v;
	}

}