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

import net.sondbjor.android.ActionItem;
import net.sondbjor.android.QuickAction;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.bjorsond.android.timeline.models.SimpleNote;
import com.bjorsond.android.timeline.reflectionspace.ReflectionSpaceHandler;
import com.bjorsond.android.timeline.utilities.Constants;
import com.bjorsond.android.timeline.R;
import com.swarmconnect.Swarm;
import com.swarmconnect.SwarmActivity;

/**
 * The Activity for creation of a {@linkplain SimpleNote}.
 * 
 * Thwe activity can be started with an intent to edit an existing note. 
 * 
 * @author andekr
 *
 */
public class NoteActivity extends SwarmActivity {
	
	
	private Button saveButton, discardButton, shareButton;
	private EditText noteTitle, noteText;
	private QuickAction qa;
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.notescreen);

		Swarm.setActive(this);
		setupViews();
//		setupAddButtonQuickAction();
	}

	/**
	 * Sets up the views by getting elements from layout XML and setting listeners for the buttons.
	 * Sets up the text fields if the Activity is started with Utilities.EDIT_NOTE as request code.
	 * 
	 */
	private void setupViews() {
		saveButton = (Button)findViewById(R.id.SaveNoteButton);
		discardButton = (Button)findViewById(R.id.DiscardNoteButton);
		shareButton = (Button)findViewById(R.id.ShareNoteButton);
		shareButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				saveNote();
				shareNote();
				finish();
			}
		});
		
		noteTitle = (EditText)findViewById(R.id.NoteTitleEditText);
		noteText = (EditText)findViewById(R.id.NoteTextEditText);
		
		if(Constants.EDIT_NOTE == getIntent().getExtras().getInt(Constants.REQUEST_CODE)){
			noteTitle.setText(getIntent().getExtras().getString(Intent.EXTRA_SUBJECT));
			noteText.setText(getIntent().getExtras().getString(Intent.EXTRA_TEXT));
		}
		
		saveButton.setOnClickListener(saveNoteListener);
		discardButton.setOnClickListener(discardNoteListener);
	}
	
	
//	private void setupAddButtonQuickAction() {
//		final ActionItem other = new ActionItem();
//		
//		other.setIcon(getResources().getDrawable(R.drawable.share_to_other));
//		other.setOnClickListener(new View.OnClickListener() {
//			public void onClick(View v) {
//				saveNote();
//				shareNote();
//				finish();
//			}
//		});
//		
//		final ActionItem reflectionSpace = new ActionItem();
//		
//		reflectionSpace.setIcon(getResources().getDrawable(R.drawable.share_to_spaces));
//		reflectionSpace.setOnClickListener(new View.OnClickListener() {
//			public void onClick(View v) {
//				Log.i("BUTTON PRESSED", "");
//				saveNote();
//				ReflectionSpaceHandler.insertToReflectionSpace(getBaseContext(), noteTitle.getText().toString() + "\n"
//																				+noteText.getText().toString());
//				finish();
//			}
//		});
//		
//		shareButton = (Button)findViewById(R.id.ShareNoteButton);
//		shareButton.setOnClickListener(new OnClickListener() {
//			public void onClick(View v) {
//				qa = new QuickAction(v);
//				
//				qa.addActionItem(other);
//				qa.addActionItem(reflectionSpace);
//				qa.setAnimStyle(QuickAction.ANIM_AUTO);
//				qa.show();
//			}
//		});
//		
//	}
	
	
	private OnClickListener saveNoteListener = new OnClickListener() {
		
		public void onClick(View v) {
			saveNote();
            finish();		
		}
	};
	
	private OnClickListener discardNoteListener = new OnClickListener() {
		
		public void onClick(View v) {
			Intent discardNoteIntent = new Intent();
            setResult(RESULT_CANCELED, discardNoteIntent);
            finish();					
		}
	};
	
	private OnClickListener shareNoteListener = new OnClickListener() {
		public void onClick(View v) {
			saveNote();
			shareNote();
			finish();
		}
	};
	
	/**
	 * Saves the note by putting the data in an Intent and sending back to the calling activity.
	 * 
	 */
	private void saveNote(){
		Intent saveNoteIntent = new Intent();
		saveNoteIntent.putExtra("NOTE_ID", getIntent().getExtras().getInt("NOTE_ID")); 
        saveNoteIntent.putExtra(Intent.EXTRA_SUBJECT, noteTitle.getText().toString()); 
        saveNoteIntent.putExtra(Intent.EXTRA_TEXT, noteText.getText().toString()); 
        setResult(RESULT_OK, saveNoteIntent);
	}
	
	private void shareNote() {
		Intent shareNoteIntent = new Intent(Intent.ACTION_SEND);
		shareNoteIntent.setType("text/plain");
        shareNoteIntent.putExtra(Intent.EXTRA_SUBJECT, noteTitle.getText().toString()); 
        shareNoteIntent.putExtra(Intent.EXTRA_TEXT, noteText.getText().toString()); 

		startActivity(Intent.createChooser(shareNoteIntent, getString(R.string.Share_note_label)));
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	    Swarm.setActive(this);
		
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	    Swarm.setInactive(this);
	}
}
