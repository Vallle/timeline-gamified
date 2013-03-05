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
package com.bjorsond.android.timeline.database.contentmanagers;

import android.content.Context;

import com.bjorsond.android.timeline.database.providers.EmotionsProvider;
import com.bjorsond.android.timeline.database.providers.EventProvider;
import com.bjorsond.android.timeline.database.providers.ExperienceProvider;
import com.bjorsond.android.timeline.models.Event;
import com.bjorsond.android.timeline.models.EventItem;
import com.bjorsond.android.timeline.models.Experience;

/**
 * Helper class to delete models from SQLite database using content providers.
 * 
 * @author andrstor
 *
 */
public class ContentDeleter {
	
	
	private Context context;

	public ContentDeleter(Context context){
		this.context = context;
	}
	
	public void deleteEventItemFromDB(EventItem item){
		context.getContentResolver().delete(item.getUri(), item.getId(), null);
	}
	
	public void deleteEventFromDB(Event event) {
		
		for (EventItem item : event.getEventItems()) {
			deleteEventItemFromDB(item);
		}
		
		deleteEmotionFromDB(event);
		context.getContentResolver().delete(EventProvider.CONTENT_URI, event.getId(), null);
	}
	
	public void deleteExperienceFromDB(Experience experience){
		context.getContentResolver().delete(ExperienceProvider.CONTENT_URI, experience.getId(), null);
	}

	private void deleteEmotionFromDB(Event event) {
		context.getContentResolver().delete(EmotionsProvider.CONTENT_URI, event.getId(), null);
	}

}
