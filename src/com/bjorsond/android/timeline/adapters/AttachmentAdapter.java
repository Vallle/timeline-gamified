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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bjorsond.android.timeline.R;

/**
 * Adapter for attachment list. Renders a list of icons and text.
 * 
 * @author andekr
 *
 */
public class AttachmentAdapter extends BaseAdapter {
		 private LayoutInflater mInflater;
		 private Context mContext;
		 private static String[] attachments;

		 public AttachmentAdapter(Context context) {
			 this.mContext = context;
			 attachments = context.getResources().getStringArray(R.array.attachment_types);
			 mInflater = LayoutInflater.from(context); 
		 }

		public int getCount() {
			return attachments.length;
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder holder;
			
			if (convertView == null) {
				
				 convertView = mInflater.inflate(R.layout.attachmentadderview, null, false);
				 
				 holder = new ViewHolder();
				 holder.icon = (ImageView) convertView.findViewById(R.id.attachmentIcon);
				 holder.text = (TextView) convertView.findViewById(R.id.attachmentType);

				 convertView.setTag(holder);
				 
			 } else {
				 holder = (ViewHolder) convertView.getTag();
			 }
				 
				 setIconForDifferentAttachementTypes(attachments[position], holder.icon);
				 holder.text.setText(attachments[position]);

				 return convertView;
		 }
		
		private void setIconForDifferentAttachementTypes(String attachmentType, ImageView imageView){

			if(attachmentType.equals(mContext.getResources().getString(R.string.picture))) {
				imageView.setImageResource(R.drawable.ic_menu_gallery);
			}
			else if(attachmentType.equals(mContext.getResources().getString(R.string.video))) {
				imageView.setImageResource(R.drawable.ic_menu_video);
			}
			else if(attachmentType.equals(mContext.getResources().getString(R.string.file))) {
				imageView.setImageResource(R.drawable.ic_menu_attachment);
			}
			else if(attachmentType.equals(mContext.getResources().getString(R.string.audio))) {
				imageView.setImageResource(R.drawable.ic_menu_audio);
			}
			else if(attachmentType.equals(mContext.getResources().getString(R.string.barcode))) {
				imageView.setImageResource(R.drawable.ic_menu_barcode);
			}
			else if(attachmentType.equals(mContext.getResources().getString(R.string.url))) {
				imageView.setImageResource(R.drawable.url_icon);
			}
			else{
				imageView.setImageResource(R.drawable.icon);
			}
		}
	}
	
	 class ViewHolder {
		 ImageView icon;
		 TextView text;
		 TextView text2;
		 CheckBox checkBox;
		 }
	 

