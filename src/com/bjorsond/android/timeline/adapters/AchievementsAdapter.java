package com.bjorsond.android.timeline.adapters;

import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bjorsond.android.timeline.R;
import com.swarmconnect.SwarmAchievement;
import com.swarmconnect.SwarmAchievement.AchievementUnlockedCB;

// An ArrayAdapter for handling SwarmAchievement's and populating
// their data into the Achievements list that is displayed to the user
public class AchievementsAdapter extends ArrayAdapter<SwarmAchievement> {

    private Typeface font = Typeface.DEFAULT;
    private int color2;
    private int color1;

    public AchievementsAdapter(Context context, int textViewResourceId, ArrayList<SwarmAchievement> items) {
        super(context, textViewResourceId, items);
        color1 = context.getResources().getColor(R.color.list_item_color1);
        color2 = context.getResources().getColor(R.color.list_item_color2);
//        font = Typeface.createFromAsset(context.getAssets(), "fonts/kperry.ttf");
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.achievements_list_item, null);
        }
        
        SwarmAchievement achievement = getItem(position);
        TextView titleTextView = (TextView) v.findViewById(R.id.achievements_title);
        titleTextView.setTypeface(font);
//        titleTextView.setWidth(100);
        
        TextView descriptionTextView = (TextView) v.findViewById(R.id.achievements_description);
        descriptionTextView.setTypeface(font);
//        descriptionTextView.setWidth(100);
       
        
        final ImageView statusTextView = (ImageView) v.findViewById(R.id.achievements_status);

        titleTextView.setText(achievement.title);
        descriptionTextView.setText(achievement.description);
//        titleTextView.set
        
        achievement.isUnlocked(new AchievementUnlockedCB() {

        	@Override
        	public void achievementUnlocked(boolean unlocked, Date date) {
        		if (unlocked) {
        			statusTextView.setVisibility(View.VISIBLE);
        		} else {
        			statusTextView.setVisibility(View.INVISIBLE);
        		}
        	}
        });
        


        if (position % 2 == 1) {
            v.setBackgroundColor(color1);
        } else {
            v.setBackgroundColor(color2);
        }

        return v;
    }
}
