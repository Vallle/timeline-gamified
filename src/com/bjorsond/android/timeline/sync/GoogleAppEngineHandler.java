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
package com.bjorsond.android.timeline.sync;

import java.util.List;

import android.util.Log;

import com.bjorsond.android.timeline.models.BaseEvent;
import com.bjorsond.android.timeline.models.Event;
import com.bjorsond.android.timeline.models.EventItem;
import com.bjorsond.android.timeline.models.Experience;
import com.bjorsond.android.timeline.models.Experiences;
import com.bjorsond.android.timeline.models.Group;
import com.bjorsond.android.timeline.models.SimplePicture;
import com.bjorsond.android.timeline.models.SimpleRecording;
import com.bjorsond.android.timeline.models.SimpleVideo;
import com.bjorsond.android.timeline.models.User;
import com.bjorsond.android.timeline.utilities.Constants;
import com.google.myjson.Gson;
import com.google.myjson.GsonBuilder;

/**
 * 
 * Handler for Google App Engine synchronization.
 * 
 * All actions to the Google App Engine is routed through this method.
 * 
 * 
 */
public class GoogleAppEngineHandler {
	private static final String TAG = "Google App Engine Handler";
	

	//ADDERS
	/**
	 * Sends an entire object to persist on server
	 * 
	 * @param object The object to send. Experiences, experience or event
	 */
	public static void persistTimelineObject(Object object){
		GsonBuilder gsonB = new GsonBuilder();
		gsonB.registerTypeAdapter(BaseEvent.class, new Serializers.EventSerializer());
		gsonB.registerTypeAdapter(Event.class, new Serializers.EventSerializer());
		gsonB.registerTypeAdapter(Experience.class, new Serializers.ExperienceSerializer());
		gsonB.registerTypeAdapter(Experiences.class, new Serializers.ExperiencesSerializer());
		
		Gson gson = gsonB.create();
		String jsonString ="";
 
		try {
			jsonString = gson.toJson(object, object.getClass());
		} catch (Exception e) {
			Log.e("save", e.getMessage());
		}
		
		    Log.i(TAG, "Saving TimelineObject-JSON to Google App Engine "+jsonString);
		    ServerUploader.putToGAE(object, jsonString);
		    
		    Log.i(TAG, "Saving files on server");
		    storeFilesOnServer(object);
		  
	}
	
	public static void addGroupToServer(Group groupToAdd){
		Gson gson = new Gson();
		String jsonString ="";

		try {
			jsonString = gson.toJson(groupToAdd, Group.class);
		} catch (Exception e) {
			Log.e("save", e.getMessage());
		}
		
	    System.out.println();
	    Log.i(TAG, "Saving group-JSON on Google App Engine: "+jsonString);
	    ServerUploader.putGroupToGAE(jsonString);
	    
	}
	

	public static void addUserToServer(User userToAdd){
		Gson gson = new Gson();
		String jsonString ="";

		try {
			jsonString = gson.toJson(userToAdd, User.class);
		} catch (Exception e) {
			Log.e("save", e.getMessage());
		}
		
		Log.i(TAG, "Saving user-JSON on Google App Engine: "+jsonString);
	    ServerUploader.putUserToGAE(jsonString);
	}
	
	public static void addUserToGroupOnServer(Group groupToGetNewMember, User userToAddToGroup) {
		Log.i(TAG,"Adding "+ userToAddToGroup +"  to "+groupToGetNewMember.getName()+" on Google App Engine");
		ServerUploader.putUserToGroupToGAE(groupToGetNewMember, userToAddToGroup);
	}
	
	
	
	//REMOVERS
	public static void removeUserFromGroupOnServer(Group groupToRemoveMember, User userToRemoveFromGroup) {
		ServerDeleter.deleteUserFromGroupToGAE(groupToRemoveMember, userToRemoveFromGroup);
	}
	
	public static void removeGroupFromDatabase(Group selectedGroup) {
		ServerDeleter.deleteUserFromGroupToGAE(selectedGroup);
	}
	
	//GETTERS
	
	public static double[] getAverageMoodForExperience(Experience experience){
		return ServerDownloader.getAverageMoodForExperience(experience);
	}
	
	public static Experiences getAllSharedExperiences(User user){
		return ServerDownloader.getAllSharedExperiencesFromServer(user);
	}
	
	public static List<User> getUsers(){
		return ServerDownloader.getUsersFromServer().getUsers();
	}
	
	public static boolean IsUserRegistered(String username) {
		return ServerDownloader.IsUserRegistered(username);
	}
	
	public static void sendEmailWithActivity(User userToEmail) {
		ServerDownloader.sendStatusMailToUser(userToEmail);
	}
	

	
	//HELPERS
	
	/**
	 * Saving pictures to server. 
	 * TODO: Any better way to do this than the almighty nesting going on here?
	 */
	
	private static void storeFilesOnServer(Object object) {
		if(object instanceof Experiences){
			Log.i("F�rste if", object.toString());
			if(((Experiences) object).getExperiences()!=null){
				Log.i("Andre if", object.toString());
		    	for (Experience ex : ((Experiences) object).getExperiences()) {
		    		if(((Experience) ex).getEvents()!=null){
		    			Log.i("Tredje if", object.toString());
			    		for (BaseEvent baseEvent : ex.getEvents()) {
			    			if(baseEvent instanceof Event){
			    				Log.i("Fjerde if", object.toString());
			    				Event event = (Event)baseEvent;
			    			if(event.getEventItems()!=null && event.isShared()){
			    				Log.i("Femte if", object.toString());
						    		for (EventItem eventI : event.getEventItems()) {
								    	if(eventI instanceof SimplePicture){
								    		ServerUploader.uploadFile(Constants.IMAGE_STORAGE_FILEPATH+((SimplePicture)eventI).getPictureFilename(), 
								    				((SimplePicture)eventI).getPictureUrl());
								    	}else if(eventI instanceof SimpleVideo){
								    		ServerUploader.uploadFile(Constants.VIDEO_STORAGE_FILEPATH+((SimpleVideo)eventI).getVideoFilename(), 
								    				((SimpleVideo)eventI).getVideoUrl());
								    	}else if(eventI instanceof SimpleRecording){
								    		ServerUploader.uploadFile(Constants.RECORDING_STORAGE_FILEPATH+((SimpleRecording)eventI).getRecordingFilename(), 
								    				((SimpleRecording)eventI).getRecordingUrl());
								    	}
									}
			    				}
			    			}
						}
		    		}
				} 
			}
			
		}else if(object instanceof Event){
			for (EventItem eventI : ((Event)object).getEventItems()) {
		    	if(eventI instanceof SimplePicture){
		    		ServerUploader.uploadFile(Constants.IMAGE_STORAGE_FILEPATH+((SimplePicture)eventI).getPictureFilename(), 
		    				((SimplePicture)eventI).getPictureUrl());
		    	}else if(eventI instanceof SimpleVideo){
		    		ServerUploader.uploadFile(Constants.VIDEO_STORAGE_FILEPATH+((SimpleVideo)eventI).getVideoFilename(), 
		    				((SimpleVideo)eventI).getVideoUrl());
		    	}else if(eventI instanceof SimpleRecording){
		    		ServerUploader.uploadFile(Constants.RECORDING_STORAGE_FILEPATH+((SimpleRecording)eventI).getRecordingFilename(), 
		    				((SimpleRecording)eventI).getRecordingUrl());
		    	}
			}
		}
	}

}
