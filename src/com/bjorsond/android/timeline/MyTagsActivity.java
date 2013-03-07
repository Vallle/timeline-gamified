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
package com.bjorsond.android.timeline;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.bjorsond.android.timeline.adapters.TagListAdapter;
import com.bjorsond.android.timeline.database.DatabaseHelper;
import com.bjorsond.android.timeline.database.TimelineDatabaseHelper;
import com.bjorsond.android.timeline.database.contentmanagers.ContentAdder;
import com.bjorsond.android.timeline.database.contentmanagers.TagManager;
import com.bjorsond.android.timeline.dialogs.EventDialog;
import com.bjorsond.android.timeline.models.BaseEvent;
import com.bjorsond.android.timeline.models.Experience;
import com.bjorsond.android.timeline.utilities.Constants;
import com.bjorsond.android.timeline.utilities.Utilities;
import com.bjorsond.android.timeline.R;
import com.swarmconnect.SwarmActivity;

/**
 * The activity containg the tag management view
 * 
 * 
 * @author andekr
 *
 */
public class MyTagsActivity extends SwarmActivity {

	private Button addNewTagButton, showInTimelineButton;
	private ImageButton homeButton;
	private ArrayList <String> allTags;
	private TagManager tagManager;
	private ListView myTagsList;
	private TagListAdapter tagListAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tagactivitylayout);
		
		setupHelpers();
		setupViews();
		
		if(isNewTagIntent()){
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
			openNewTagNameInputDialog();
		}
	}

	/**
	 * 
	 * 
	 * @return true if the Intent is to create a new tag(typically if called from the {@link EventDialog}.
	 */
	private boolean isNewTagIntent() {
		return getIntent().getAction()!= null && getIntent().getAction().equals(Constants.INTENT_ACTION_NEW_TAG);
	}
	
	/**
	 * Add a new group to the database
	 * @param groupName. The group name of the new group
	 */
	protected void addNewTag(String tagName) {
		tagManager.addTagToDatabase(tagName);
		Toast.makeText(MyTagsActivity.this.getApplicationContext(), "You have created the tag: " +tagName , Toast.LENGTH_SHORT).show();
		if(isNewTagIntent()){
	        setResult(RESULT_OK, getIntent());
			finish();
		}
		else
			setupViews();
	}
	
	/**
	 * Input dialog for the writing the name of a new group
	 */
	private void openNewTagNameInputDialog() {
		
		final AlertDialog.Builder tagNameInputDialog = new AlertDialog.Builder(
				this);
		
		Context mContext = getApplicationContext();
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.newgroupdialog, (ViewGroup) findViewById(R.id.newgroupdialogroot));
		tagNameInputDialog.setView(layout);
		
		final EditText inputTextField = (EditText)layout.findViewById(R.id.NewGroupeditText);
		inputTextField.setHint("Enter a tag name");

		tagNameInputDialog.setTitle("Enter a name for tag!");
		tagNameInputDialog.setPositiveButton("Ok",
				new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				String inputName = inputTextField.getText().toString().trim();
				addNewTag(inputName);
				dialog.dismiss();
			}
		});

		tagNameInputDialog.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		}).setOnCancelListener(new OnCancelListener() {
			
			public void onCancel(DialogInterface dialog) {
				dialog.dismiss();
			}
		});
		
		tagNameInputDialog.show();
	}
	
	
	//LISTENERS
	
	
	private android.view.View.OnClickListener newTagButtonListener = new View.OnClickListener() {
		
		public void onClick(View v) {
			openNewTagNameInputDialog();
		}
	};
	
	private android.view.View.OnClickListener showInTimelineButtonListener = new View.OnClickListener() {
		
		public void onClick(View v) {
				createAndOpenNewExperienceBasedOnSelectedTags();
		}	
	};
	
	/**
	 * The method to set up and create a new timeline based on selected tags
	 * 
	 * TODO: This method should be subject to some refactoring
	 * 
	 */
	private void createAndOpenNewExperienceBasedOnSelectedTags() {
		List<String> selectedTagsName = tagListAdapter.getCheckedTags();
		List<BaseEvent> eventsTaggedWithSelectedTags = tagManager.getAllEventsConnectedToTag(selectedTagsName);
		Log.i(this.getClass().getSimpleName(), "Got "+eventsTaggedWithSelectedTags.size()+" connected to tags");
		
		if(!tagListAdapter.getCheckedTags().isEmpty() && !eventsTaggedWithSelectedTags.isEmpty()) {
			String experienceTitle = "Tags: ";
			for (int i = 0; i < Math.min(3, selectedTagsName.size()); i++) {
				experienceTitle +=selectedTagsName.get(i)+" ";
			}
			Experience tagExperience = new Experience(experienceTitle.trim(), false, Utilities.getUserAccount(this));
			for (BaseEvent baseEvent : eventsTaggedWithSelectedTags) {
				baseEvent.setExperienceid(tagExperience.getId());
				baseEvent.generateNewId();
				tagExperience.addEvent(baseEvent);
			}
				
			String databaseName = tagExperience.getTitle() + ".db";

			Intent timelineIntent = new Intent(this, TimelineActivity.class);
			timelineIntent.setAction(Constants.INTENT_ACTION_NEW_TIMELINE);
			timelineIntent.putExtra(Constants.DATABASENAME_REQUEST, databaseName);
			timelineIntent.putExtra(Constants.SHARED_REQUEST, tagExperience.isShared());
			timelineIntent.putExtra(Constants.EXPERIENCEID_REQUEST, tagExperience.getId());
			timelineIntent.putExtra(Constants.EXPERIENCECREATOR_REQUEST, tagExperience.getUser().name);
			
			new TimelineDatabaseHelper(this, Constants.ALL_TIMELINES_DATABASE_NAME);
			new DatabaseHelper(this, databaseName);
			ContentAdder adder = new ContentAdder(this);
			adder.addExperienceToTimelineContentProvider(tagExperience);
			DatabaseHelper.getCurrentTimelineDatabase().close();
			TimelineDatabaseHelper.getCurrentTimeLineDatabase().close();
			startActivity(timelineIntent);
			finish();
		}
		else if(tagListAdapter.getCheckedTags().isEmpty()) {
			Toast.makeText(getApplicationContext(), "No tag selected! Select one or more tags to use this functionality", Toast.LENGTH_SHORT).show();
		}
		else if(eventsTaggedWithSelectedTags.isEmpty()) {
			Toast.makeText(getApplicationContext(), "The tag(s) you have selected has no attached events!", Toast.LENGTH_SHORT).show();
		}
	}
	
	private void setupHelpers() {
		new TimelineDatabaseHelper(this, Constants.ALL_TIMELINES_DATABASE_NAME);
		tagManager = new TagManager(this);
	}
		
	/**
	 * Setup views and instansiate objects the activity is going to use
	 */
	private void setupViews() {
		myTagsList = (ListView) findViewById(R.id.tagListlistView);
		
		addNewTagButton = (Button) findViewById(R.id.tagsCreateButton);
		addNewTagButton.setOnClickListener(newTagButtonListener);
		
		showInTimelineButton = (Button) findViewById(R.id.tagShowInTimelineButton);
		showInTimelineButton.setOnClickListener(showInTimelineButtonListener);
		
		allTags = tagManager.getAllTags();
		System.out.println("Antall tags: "+allTags.size());
		tagListAdapter = new TagListAdapter(this, R.layout.list_tags_view , allTags, new ArrayList<String>());
		myTagsList.setAdapter(tagListAdapter);
		
		homeButton = (ImageButton)findViewById(R.id.TagHomeButton);
		homeButton.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				finish();
			}
		});
		
		registerForContextMenu(myTagsList);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.tagcontextmenu, menu);
	}
	
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info;
		switch (item.getItemId()) {
		
		case R.id.MENU_DELETE_ITEM:
			try {
				info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
			} catch (Exception e) {
				Log.e("ERROR", "bad menuInfo", e);
			    return false;
			}
			deleteTagConfirmationDialog(tagListAdapter.getItem(info.position));
			break;
		}
		return false;
	}
	
	/**
	 * Confirmation dialog that pops when you try to delete a tag
	 */
	private void deleteTagConfirmationDialog(final String tagName) {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Do you really want to delete tag \"" +tagName+"\"?")
		.setPositiveButton(R.string.yes_label, new OnClickListener() {
			
			public void onClick(DialogInterface arg0, int arg1) {
				tagListAdapter.remove(tagName);
				tagManager.DeleteTag(tagName);
			}
		})
		.setNegativeButton(R.string.no_label, new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
		}
	})
		.setOnCancelListener(new OnCancelListener() {
		public void onCancel(DialogInterface dialog) {
			dialog.dismiss();					
		}
	});
		AlertDialog confirmation = builder.create();
		confirmation.show();
	}

}
