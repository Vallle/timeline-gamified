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

import java.lang.reflect.Type;

import android.accounts.Account;

import com.bjorsond.android.timeline.models.EventItem;
import com.bjorsond.android.timeline.models.SimpleNote;
import com.bjorsond.android.timeline.models.SimplePicture;
import com.bjorsond.android.timeline.models.SimpleRecording;
import com.bjorsond.android.timeline.models.SimpleVideo;
import com.google.myjson.JsonDeserializationContext;
import com.google.myjson.JsonDeserializer;
import com.google.myjson.JsonElement;
import com.google.myjson.JsonParseException;



public class Deserializers {
	
	/**
	 * Custom deserializer for GSON. The deserializer takes a {@link EventItem}-JSON as input,
	 * and using the className attribute instantiates the correct EventItem subtype.
	 * 
	 * Note: Has to be edited if class names are changed, or new {@link EventItem}s are added
	 * 
	 * @author andekr
	 *
	 */
	protected static class EventItemDeserializer implements JsonDeserializer<EventItem> {
		  public EventItem deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
		      throws JsonParseException {
			  
			 String className = json.getAsJsonObject().get("className").getAsString();
			 String id = json.getAsJsonObject().get("id").getAsString();
			 Account creator = new Account(json.getAsJsonObject().get("creator").getAsString(), "com.google");
			 EventItem ei;
			if(className.equals("SimplePicture")){
				 String filename = json.getAsJsonObject().get("filename").getAsString();
				ei = new SimplePicture(id, creator, filename);
				  return ei;
			}else if(className.equals("SimpleNote")){
				String noteTitle = json.getAsJsonObject().get("noteTitle").getAsString();
				String noteText = json.getAsJsonObject().get("noteText").getAsString();
				ei = new SimpleNote(id, noteTitle, noteText, creator);
				  return ei;
			} else if(className.equals("SimpleRecording")){
				 String filename = json.getAsJsonObject().get("filename").getAsString();
					ei = new SimpleRecording(id, creator, filename);
					return ei;
			}else if(className.equals("SimpleVideo")){
				    String filename = json.getAsJsonObject().get("filename").getAsString();
					ei = new SimpleVideo(id, creator, filename);
					return ei;
			}else 
				  return null;
			  
		  }
		}
	
}
