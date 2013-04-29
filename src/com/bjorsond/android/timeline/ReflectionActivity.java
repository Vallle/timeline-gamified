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
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.bjorsond.android.timeline.models.ReflectionNote;
import com.bjorsond.android.timeline.utilities.Constants;
import com.bjorsond.android.timeline.R;
import com.swarmconnect.Swarm;
import com.swarmconnect.SwarmActivity;

/**
 * The Activity for creation of a {@linkplain NoteActivity.javaNote}.  -- now reflection note
 * 
 * The activity can be started with an intent to edit an existing note. 
 * 
 * @author andekr
 * @author sondrelo
 *
 */
public class ReflectionActivity extends SwarmActivity {
	
	
	private Button saveButton, discardButton, shareButton;
	private EditText reflectionTitle;
	private QuickAction qa;
//	private EditText reflectionText;
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.reflectionscreen);

		Swarm.setActive(this);
		setupViews();
		setupAddButtonQuickAction();
	}

	/**
	 * Sets up the views by getting elements from layout XML and setting listeners for the buttons.
	 * Sets up the text fields if the Activity is started with Utilities.EDIT_NOTE as request code.
	 * 
	 */
	private void setupViews() {
		saveButton = (Button)findViewById(R.id.SaveReflectionButton);
		discardButton = (Button)findViewById(R.id.DiscardReflectionButton);
		
		reflectionTitle = (EditText)findViewById(R.id.ReflectionTitleEditText);
//		reflectionText = (EditText)findViewById(R.id.ReflectionTextEditText);
		
		reflectionTitle.setText(getString(R.string.Refleqtion_question_one) + "\n\n" + getString(R.string.Refleqtion_question_two) + "\n");
		
		if(Constants.EDIT_REFLECTION == getIntent().getExtras().getInt(Constants.REQUEST_CODE)){
			reflectionTitle.setText(getIntent().getExtras().getString(Intent.EXTRA_SUBJECT));
//			reflectionText.setText(getIntent().getExtras().getString(Intent.EXTRA_TEXT));
		}
		
		saveButton.setOnClickListener(saveReflectionListener);
		discardButton.setOnClickListener(discardReflectionListener);
	}
	
	
	private void setupAddButtonQuickAction() {
		final ActionItem facebook = new ActionItem();
		
		facebook.setIcon(getResources().getDrawable(R.drawable.swarm_facebook));
		facebook.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				
			}
		});
		
		final ActionItem other = new ActionItem();
		
		other.setIcon(getResources().getDrawable(R.drawable.share_to_other));
		other.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				saveReflection();
				shareReflection();
				finish();
			}
		});
		
		shareButton = (Button)findViewById(R.id.ShareReflectionButton);
		shareButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				qa = new QuickAction(v);
				
				qa.addActionItem(facebook);
				qa.addActionItem(other);
				qa.setAnimStyle(QuickAction.ANIM_AUTO);
				qa.show();
			}
		});
		
	}
	
	
	
	//LISTENERS
	private OnClickListener saveReflectionListener = new OnClickListener() {
		
		public void onClick(View v) {
			saveReflection();
            finish();		
		}
	};
	
	private OnClickListener discardReflectionListener = new OnClickListener() {
		
		public void onClick(View v) {
			Intent discardReflectionIntent = new Intent();
            setResult(RESULT_CANCELED, discardReflectionIntent);
            finish();					
		}
	};
	
	
	/**
	 * Saves the note by putting the data in an Intent and sending back to the calling activity.
	 * 
	 */
	private void saveReflection(){
		Intent saveReflectionIntent = new Intent();
		saveReflectionIntent.putExtra("REFLECTION_ID", getIntent().getExtras().getInt("REFLECTION_ID")); 
        saveReflectionIntent.putExtra(Intent.EXTRA_SUBJECT, reflectionTitle.getText().toString()); 
//        saveReflectionIntent.putExtra(Intent.EXTRA_TEXT, reflectionText.getText().toString());
        setResult(RESULT_OK, saveReflectionIntent);
	}
	
	private void shareReflection() {
		Intent shareReflectionIntent = new Intent(Intent.ACTION_SEND);
		shareReflectionIntent.setType("text/plain");
        shareReflectionIntent.putExtra(Intent.EXTRA_TEXT, reflectionTitle.getText().toString()); 
//        shareReflectionIntent.putExtra(Intent.EXTRA_TEXT, reflectionText.getText().toString()); 

        String clip = reflectionTitle.getText().toString();
        
        ClipboardManager clipBoard = (ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
        clipBoard.setText(clip);		//Horrible workaround for sharing to Facebook. These lines of code copy the text to clipboard, so that the user can manually paste his text after having chosen Facebook from the chooser.
        
		startActivity(Intent.createChooser(shareReflectionIntent, getString(R.string.Share_reflection_label)));
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
