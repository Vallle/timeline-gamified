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
package com.bjorsond.android.timeline.models;


import android.accounts.Account;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.util.Linkify;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bjorsond.android.timeline.database.providers.ReflectionProvider;
import com.bjorsond.android.timeline.R;

public class ReflectionNote extends EventItem{
	
	public ReflectionNote() {}
	
	
	public ReflectionNote(Context c) {
		super(c);
		className = "ReflectionNote";
	}
	
	public ReflectionNote(String reflectionText, Context c) {
		super(c);
		className = "ReflectionNote";
		this.reflectionText = reflectionText;
	}
	
	public ReflectionNote(String id, String title, String reflectionText, Account u){
		super(id, u);
		className = "ReflectionNote";
		this.reflectionText = reflectionText;
		this.reflectionTitle = title;
	}
	
	public String getReflectionText() {
		return reflectionText;
	}

	public void setReflectionText(String reflectionText) {
		this.reflectionText = reflectionText;
	}
	
	public String getReflectionTitle() {
		return reflectionTitle;
	}

	public void setReflectionTitle(String reflectionTitle) {
		this.reflectionTitle = reflectionTitle;
	}
	
	@Override
	public Uri getUri() {
		return ReflectionColumns.CONTENT_URI;
	}

	@Override
	public View getView(Context context) {
		LinearLayout textLayout = new LinearLayout(context);
        TextView reflectionTextView = new TextView(context);
        reflectionTextView.setTag(this);
        reflectionTextView.setText(noteText);
        reflectionTextView.setTextSize(20);
        reflectionTextView.setTextColor(context.getResources().getColor(R.color.Black));
        reflectionTextView.setLinkTextColor(context.getResources().getColor(R.color.Black));
        Linkify.addLinks( reflectionTextView, Linkify.ALL);
        
        TextView reflectionTitleTextView = new TextView(context);
        reflectionTitleTextView.setTag(this);
        reflectionTitleTextView.setText(reflectionTitle);
        reflectionTitleTextView.setTextSize(14);
        reflectionTitleTextView.setTextColor(context.getResources().getColor(R.color.Black));
        

        textLayout.setClickable(true);
        textLayout.setOrientation(LinearLayout.VERTICAL);
        textLayout.addView(reflectionTitleTextView);
        textLayout.addView(reflectionTextView);
        textLayout.setTag(this);
        textLayout.setPadding(10, 10, 0, 0);
        
        return textLayout;
	}
	
	@Override
	public Intent getIntent() {
		return null;
	}
	
	public static final class ReflectionColumns implements  BaseColumns {
		
		// This class cannot be instantiated
        private ReflectionColumns() {}

        public static final Uri CONTENT_URI = Uri.parse("content://" + ReflectionProvider.AUTHORITY + "/reflections");
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.bjorsond.reflections";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.bjorsond.reflection";
        public static final String DEFAULT_SORT_ORDER = "modified DESC";
        public static final String TITLE = "title";
        public static final String REFLECTION = "reflection";
        public static final String CREATED_DATE = "created";
        public static final String MODIFIED_DATE = "modified";
    }
}



