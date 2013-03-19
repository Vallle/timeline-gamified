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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import android.accounts.Account;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bjorsond.android.timeline.database.DatabaseHelper;
import com.bjorsond.android.timeline.database.TimelineDatabaseHelper;
import com.bjorsond.android.timeline.database.UserGroupDatabaseHelper;
import com.bjorsond.android.timeline.database.contentmanagers.ContentAdder;
import com.bjorsond.android.timeline.database.contentmanagers.ContentLoader;
import com.bjorsond.android.timeline.database.contentmanagers.UserGroupManager;
import com.bjorsond.android.timeline.dialogs.NewTimelineDialog;
import com.bjorsond.android.timeline.dialogs.TimelineBrowserDialog;
import com.bjorsond.android.timeline.models.Experience;
import com.bjorsond.android.timeline.models.Experiences;
import com.bjorsond.android.timeline.models.User;
import com.bjorsond.android.timeline.sync.GoogleAppEngineHandler;
import com.bjorsond.android.timeline.sync.UserAndGroupServiceHandler;
import com.bjorsond.android.timeline.utilities.Constants;
import com.bjorsond.android.timeline.utilities.MyLocation;
import com.bjorsond.android.timeline.utilities.Utilities;
import com.bjorsond.android.timeline.R;
import com.swarmconnect.Swarm;
import com.swarmconnect.SwarmAchievement;
import com.swarmconnect.SwarmActivity;


/**
 * The starting activity for the application.
 * The application is mainly a collection of button listeners for the different menu buttons.
 * 
 * Implemented buttons:
 * -New timeline
 * -My private timelines({@link Experience} with shared=false)
 * -My groups
 * -My shared timelines({@link Experience} with shared=true)
 * -My tags
 * -Synchronize(collects all shared experiences and sends to a server, 
 * as well as downloading all of the users shared experiences from the server)
 * 
 * @author andekr
 *
 */
public class DashboardActivity extends SwarmActivity implements ProgressDialogActivity {

	private ImageButton newTimeLineButton;
	private ImageButton browseMyTimelinesButton;
	private ImageButton profileButton;
	private ImageButton browseSharedTimelinesButton;
	private ImageButton syncronizeButton;
	private ImageButton myGroupsButton;
	private TextView lastSyncedTextView;
	private Intent timelineIntent;
	private Intent myGroupsIntent;
	private Intent tagsIntent;
	private Intent swarmIntent;
	private ContentAdder contentAdder;
	private ContentLoader contentLoader;
	private Account creator;
	private User user;
	Runnable syncThread, addGroupToServerThread, checkUserRunnable;
	private long lastSynced=0;
	boolean registered=false;
	private UserGroupDatabaseHelper userGroupDatabaseHelper;
	private UserGroupManager uGManager;
	private ProgressDialog progressDialog;
	private TimelineDatabaseHelper timelineDatabaseHelper;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dashboard);
		MyLocation.getInstance(this);//Starts the LocationManager right away so a location will be available as soon as possible
		
		Swarm.init(this, 4651, "6ef1c4f59752007d40bd3d8828f789f2");
		
		Swarm.setActive(this);
		
		creator = Utilities.getUserAccount(this);
		user = new User(creator.name);
		
		//Initializes the content managers
		setupHelpers();
		
		uGManager.addUserToUserDatabase(user);
		
		setupIntents();

		progressDialog = new ProgressDialog(this);
		
		//Check if user is registered
		checkUserRunnable = new Runnable() {
			public void run() {
				checkIfUserIsRegisteredOnServer();
			}
		};
		
		//Checks for Internet connection
		if(Utilities.isConnectedToInternet(this)){
			Thread checkUserThread = new Thread(checkUserRunnable, "checkUserThread");
			checkUserThread.start();
			progressDialog = ProgressDialog.show(DashboardActivity.this,    
		              "", "", true);
		}else{
			Toast.makeText(this, R.string.No_connection_toast, Toast.LENGTH_LONG).show();
		}
		
		try {
			lastSynced = getLastSynced();
		} catch (Exception e) {
			Log.e(this.getClass().getSimpleName(), getString(R.string.Could_not_sync));
		}
		
		setupViews();
		
		/*
		 * If the application is started with a SEND- or share Intent, change the Intent to add to a timeline
		 * This is for cases where the users clicks "share" in another application and selects "Timeline" as application
		 * to share with
		 */
		if (getIntent().getAction().equals(Intent.ACTION_SEND)
				|| getIntent().getAction().equals("share")) {
			timelineIntent = getIntent();
			timelineIntent.setAction(Constants.INTENT_ACTION_ADD_TO_TIMELINE);
			timelineIntent.setClass(this, TimelineActivity.class); //Changes the class to start
			browseAllTimelines(Constants.SHARED_ALL);
		}
		
		//Prepares the sync thread
 		syncThread = new Runnable() {
			public void run() {
				syncTimelines();
			}
		};
		
	}

	/**
	 * 
	 * Checks if user is registered on the Timeline server.
	 * If not, the user is registered, using the e-mail address from the primary
	 * Google Account on the device.
	 * 
	 */
	private void checkIfUserIsRegisteredOnServer() {
		runOnUiThread(new Runnable() {
			public void run() {
				progressDialog.setMessage(getString(R.string.Checking_user));
			}
		});
		registered = GoogleAppEngineHandler.IsUserRegistered(user.getUserName());
		//Register user if not registered
		if(!registered){
			GoogleAppEngineHandler.addUserToServer(user);
			runOnUiThread(new Runnable() {
				public void run() {
					progressDialog.setMessage(getString(R.string.Not_registered));
				}
			});
		}else{
			runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(DashboardActivity.this, R.string.Already_registered_toast, Toast.LENGTH_LONG).show();
				}
			});
		}
		progressDialog.dismiss();
	}
	
	private void setLastSyncedTextView() {
		if(lastSynced!=0){
			String lastSyncedFormattedString = DateFormat.format
   		 ("dd MMMM yyyy "+DateFormat.HOUR_OF_DAY+":mm:ss", new Date(lastSynced)).toString();
			lastSyncedTextView.setText(getResources().getString(R.string.Last_synced_label).toString()+" "+lastSyncedFormattedString);
		}else{
			lastSyncedTextView.setText(getResources().getString(R.string.Last_synced_label).toString()+" Never");
		}
	}

	/**
	 * Adds the new timeline to the database containing all the timelines.
	 * 
	 * 
	 * @param experience The experience to add to database
	 */
	private void addNewTimelineToTimelineDatabase(Experience experience) {
		contentAdder.addExperienceToTimelineContentProvider(experience);
	}


	/**
	 * Opens a dialog based on an integer constant defined in {@link Constants}.
	 * 
	 */
	private void browseAllTimelines(int shared) {
		TimelineBrowserDialog dialog = new TimelineBrowserDialog(this,
				timelineIntent, shared);

		if (dialog.getNumberOfTimelinesSaved() != 0) {
			dialog.show();
		} else {
			switch (shared) {
			case Constants.SHARED_TRUE:
				Toast.makeText(this,
						R.string.No_shared_timelines_toast,
						Toast.LENGTH_LONG).show();
				break;
			case Constants.SHARED_FALSE:
				Toast.makeText(this,
						R.string.No_private_timelines_toast,
						Toast.LENGTH_LONG).show();
				break;
			default:
				Toast.makeText(this,
						R.string.No_timelines_toast,
						Toast.LENGTH_SHORT).show();
				break;
			}

		}
	}
	
	/**
	 * Synchronize shared timelines with database.
	 * First all local objects are persisted on server, then all content for this user on the server is downloaded.
	 * 
	 * TODO: This should be refactored into a new class, and be optimized to help poor run time.
	 * 
	 */
	private void syncTimelines() {
		UserAndGroupServiceHandler ugHandler =  new UserAndGroupServiceHandler(this, this);
		ugHandler.downloadUsersAndGroups();
		ArrayList<Experience> sharedExperiences = contentLoader.LoadAllSharedExperiencesFromDatabase();
		for (Experience experience : sharedExperiences) {
			new DatabaseHelper(this, experience.getTitle()+".db");
			DatabaseHelper.getCurrentTimelineDatabase().beginTransaction();
			experience.setEvents(contentLoader.LoadAllEventsFromDatabase());
			DatabaseHelper.getCurrentTimelineDatabase().setTransactionSuccessful();
			DatabaseHelper.getCurrentTimelineDatabase().endTransaction();
			DatabaseHelper.getCurrentTimelineDatabase().close();
		}
		
		Experiences experiences = new Experiences(sharedExperiences);
		GoogleAppEngineHandler.persistTimelineObject(experiences);
		
		Experiences exps = GoogleAppEngineHandler.getAllSharedExperiences(user);
		if(exps!=null){
			for (Experience e : exps.getExperiences()) {
				e.setSharingGroupObject(uGManager.getGroupFromDatabase(e.getSharingGroup()));
				contentAdder.addExperienceToTimelineContentProvider(e);
				addNewTimelineToTimelineDatabase(e);

			}
		}
		runOnUiThread(confirmSync);
	}
	
	/**
	 * Stores the time of the last sync in a local file.
	 * 
	 * @param lastSyncedInMillis Time of last sync in milliseconds
	 */
	private void storeLastSynced(long lastSyncedInMillis){
		String FILENAME = "lastSynced";
		String lastSynced = String.valueOf(lastSyncedInMillis);
		this.lastSynced = lastSyncedInMillis;
		setLastSyncedTextView();
		
		try {
			FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
			fos.write(lastSynced.getBytes());
			fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Gets the time of last sync.
	 * 
	 * 
	 * Used in setting last synced TextView.
	 * Other possible usages is to use this as input when syncing, so that only new content is synced.
	 * 
	 * 
	 * @return Time of last sync in milliseconds
	 */
	private long getLastSynced(){
		final String FILENAME = "lastSynced";
		int ch;
	    StringBuffer strContent = new StringBuffer("");
		
		try {
			FileInputStream fis = openFileInput(FILENAME);
			while( (ch = fis.read()) != -1)
		        strContent.append((char)ch);
			fis.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return Long.valueOf(strContent.toString());
	}
	
	/**
	 * Thread to notify user that timelines have been synced. Intended run on UI thread.
	 * 
	 */
    private Runnable confirmSync = new Runnable() {
        public void run(){
        	try {
        		storeLastSynced(new Date().getTime());
        		progressDialog.dismiss();
        		Toast.makeText(DashboardActivity.this, R.string.Synced_toast, Toast.LENGTH_SHORT).show();
			} catch (Exception e) {
			}
        }
      };
    
	@Override
	protected void onDestroy() {
		super.onDestroy();
		closeDatabaseHelpers();
	}
	
	@Override
	public void finish() {
		super.finish();
		closeDatabaseHelpers();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		Swarm.setInactive(this);
		closeDatabaseHelpers();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Swarm.setActive(this);
		setupDatabaseHelpers();
	}
	
	@Override
	protected void onStop() {
		closeDatabaseHelpers();
		super.onStop();
	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		timelineIntent = new Intent(this, TimelineActivity.class);
		timelineIntent.setAction("NEW");
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.dashboard_menu, menu);
	    return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.SEND_EMAIL_WITH_CONTENT:
			Runnable sendEmailRunnable = new Runnable() {
				public void run() {
					GoogleAppEngineHandler.sendEmailWithActivity(user);
				}
			};
			Thread sendEmailThread = new Thread(sendEmailRunnable);
			sendEmailThread.start();
			return true;
			
			
		//GAMIFICATION: Swarm menu
		case R.id.SWARM_MENU_BUTTON:
	        SwarmAchievement.unlock(10839);
	        startActivity(swarmIntent);
			
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	//LISTENERS 
	
	private OnClickListener newTimeLineListener = new OnClickListener() {
		public void onClick(View v) {
				if(Utilities.isConnectedToInternet(getApplicationContext())) {
					UserAndGroupServiceHandler ugHandler =  new UserAndGroupServiceHandler(DashboardActivity.this, DashboardActivity.this);
					ugHandler.startDownloadUsersAndGroups();
				}
				else {
					Toast.makeText(getApplicationContext(), R.string.Offline_toast, Toast.LENGTH_SHORT).show();
					NewTimelineDialog newTimelineDialog = new NewTimelineDialog(DashboardActivity.this, null,timelineIntent);
					newTimelineDialog.show();
				}
			
		}
	};

	private OnClickListener browseTimeLineListener = new OnClickListener() {

		public void onClick(View v) {
			browseAllTimelines(Constants.SHARED_FALSE);
		}
	};
	
	private OnClickListener browseSharedTimeLinesListener = new OnClickListener() {

		public void onClick(View v) {
			browseAllTimelines(Constants.SHARED_TRUE);		
		}

	};
	
	private OnClickListener openMyGroupsListener = new OnClickListener() {
		public void onClick(View v) {
			if(Utilities.isConnectedToInternet(getApplicationContext())) {
				startActivity(myGroupsIntent);
				closeDatabaseHelpers();
			}
			else {
				Toast.makeText(getApplicationContext(), R.string.Online_functionality_toast, Toast.LENGTH_SHORT).show();
			}
		}
	};
	
	private OnClickListener viewProfileListener = new OnClickListener() {
		public void onClick(View v) {
			startActivity(tagsIntent);
		}
	};
	
	private OnClickListener syncListener = new OnClickListener() {
		public void onClick(View v) {
			
			if(Utilities.isConnectedToInternet(getApplicationContext())) {
				progressDialog = ProgressDialog.show(DashboardActivity.this,    
			              "", "", true);
				progressDialog.setMessage(getString(R.string.Synchronizing));
				Thread shareThread = new Thread(syncThread, "shareThread");
				shareThread.start();
			}
			else {
				Toast.makeText(getApplicationContext(), R.string.Online_functionality_toast, Toast.LENGTH_SHORT).show();
			}
		}
	};
	
	//SETUP HELPERS
	
	private void setupIntents() {
		myGroupsIntent = new Intent(this, MyGroupsActivity.class);
		myGroupsIntent.putExtra("ACCOUNT", creator);
		
		timelineIntent = new Intent(this, TimelineActivity.class);
		timelineIntent.setAction(Constants.INTENT_ACTION_NEW_TIMELINE); //Default Intent action for TimelineActivity is to create/open a timeline.
		tagsIntent = new Intent(this, MyTagsActivity.class);
		
		swarmIntent = new Intent(this, MySwarmActivity.class);
	}

	private void setupHelpers() {
		setupDatabaseHelpers();
		contentAdder = new ContentAdder(getApplicationContext());
		contentLoader = new ContentLoader(getApplicationContext());
		uGManager = new UserGroupManager(getApplicationContext());
	}

	public void callBack() {
		NewTimelineDialog newTimelineDialog = new NewTimelineDialog(DashboardActivity.this, null, timelineIntent);
		newTimelineDialog.show();
	}
	
	private void setupDatabaseHelpers() {
		userGroupDatabaseHelper = new UserGroupDatabaseHelper(this, Constants.USER_GROUP_DATABASE_NAME);
		timelineDatabaseHelper = new TimelineDatabaseHelper(this, Constants.ALL_TIMELINES_DATABASE_NAME);
	}
	
	private void closeDatabaseHelpers() {
		userGroupDatabaseHelper.close();
		timelineDatabaseHelper.close();
	}
	
	/**
	 * Sets up the views by getting the views from layout XML and attaching listeners to buttons. 
	 * 
	 */
	private void setupViews() {
		
		newTimeLineButton = (ImageButton) findViewById(R.id.dash_new_timeline);
		newTimeLineButton.setOnClickListener(newTimeLineListener);
		browseMyTimelinesButton = (ImageButton) findViewById(R.id.dash_my_timelines);
		browseMyTimelinesButton.setOnClickListener(browseTimeLineListener);
		browseSharedTimelinesButton = (ImageButton) findViewById(R.id.dash_shared_timelines);
		browseSharedTimelinesButton.setOnClickListener(browseSharedTimeLinesListener);
		myGroupsButton = (ImageButton) findViewById(R.id.dash_my_groups);
		myGroupsButton.setOnClickListener(openMyGroupsListener);
		profileButton = (ImageButton) findViewById(R.id.dash_profile);
		profileButton.setOnClickListener(viewProfileListener);
		syncronizeButton = (ImageButton)findViewById(R.id.dash_sync);
		syncronizeButton.setOnClickListener(syncListener);
		lastSyncedTextView = (TextView)findViewById(R.id.DashLastSyncedTextView);
		setLastSyncedTextView();	
	}
	
	@Override
	public void onBackPressed() {
		this.finish();
	}
}
