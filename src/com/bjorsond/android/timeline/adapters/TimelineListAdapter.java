/*******************************************************************************
 * Copyright (c) 2011 Andreas Storlien and Anders Kristiansen.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Andreas Storlien and Anders Kristiansen - initial API and implementation
 ******************************************************************************/
package com.bjorsond.android.timeline.adapters;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bjorsond.android.timeline.models.Experience;
import com.bjorsond.android.timeline.R;


/**
 * List adapter that adds an icon to the timeline name.
 * The icons represent the share status of the timeline.
 * 
 * @author andekr
 *
 */
public class TimelineListAdapter extends ArrayAdapter<Experience> {
		 private LayoutInflater mInflater;
		 private Context mContext;
		 private ArrayList<Experience> experiences;

		 public TimelineListAdapter(Context context, ArrayList<Experience> experiences) {
			 super(context, 0);
			 this.mContext = context;
			 this.experiences = experiences;
			 mInflater = LayoutInflater.from(mContext); 
		 }

		public int getCount() {
			return experiences.size();
		}

		public Experience getItem(int position) {
			return experiences.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder holder;
			
			if (convertView == null) {
				
				 convertView = mInflater.inflate(R.layout.list_timeline_view, null, false);
				 
				 holder = new ViewHolder();
				 holder.icon = (ImageView) convertView.findViewById(R.id.listIcon);
				 holder.text = (TextView) convertView.findViewById(R.id.timelineTitle);
				 holder.text2 = (TextView) convertView.findViewById(R.id.groupTitle);
				 

				 convertView.setTag(holder);
				 
			 } else {
				 holder = (ViewHolder) convertView.getTag();
			 }
				 
				 setIconForTimeLineType(experiences.get(position), holder.icon);
				 holder.text.setText(experiences.get(position).toString());
				 try {
					 holder.text2.setText(R.string.Shared_with_label+experiences.get(position).getSharingGroupObject().toString());
				} catch (NullPointerException e) {
					 holder.text2.setText(R.string.Private_timeline_label);
				}
				

				 return convertView;
		 }
		
		private void setIconForTimeLineType(Experience experience, ImageView imageView){
			
			if(experience.isShared()) {
				imageView.setImageResource(R.drawable.shared_timeline);
			}
			else{
				imageView.setImageResource(R.drawable.individual_timeline);
			}
		}
	}	 

