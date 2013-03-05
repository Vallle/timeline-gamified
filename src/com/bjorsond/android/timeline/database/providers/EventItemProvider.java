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
package com.bjorsond.android.timeline.database.providers;

import java.util.HashMap;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.bjorsond.android.timeline.database.SQLStatements;
import com.bjorsond.android.timeline.models.Event.EventColumns;
import com.bjorsond.android.timeline.models.EventItem.EventItemsColumns;

public class EventItemProvider extends BaseContentProvider {

	public static final String AUTHORITY = "com.bjorsond.android.timeline.database.providers.EventItemProvider";
	
	private static final int EVENT_ID = 1;
	private static final int EVENT_ITEM_ID = 2;
	
    public static final Uri CONTENT_URI = 
        Uri.parse("content://"+ AUTHORITY + "/eventItem");
    
      private static final UriMatcher uriMatcher;

      private static HashMap<String, String> eventsToEventItemsProjectionMap;

    
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		
		Long rowID = super.getDatabase().insertWithOnConflict(SQLStatements.EVENT_TO_EVENT_ITEM_DATABASE_TABLE_NAME, "", values, SQLiteDatabase.CONFLICT_REPLACE);
		
		if(rowID > 0){
			Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
			getContext().getContentResolver().notifyChange(_uri, null);
			return _uri;
		}

		throw new SQLException("Failed to insert row into " + uri);
	}
	
	@Override      
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		
		SQLiteQueryBuilder sqlBuilder = new SQLiteQueryBuilder();
	      sqlBuilder.setTables(SQLStatements.EVENT_TO_EVENT_ITEM_DATABASE_TABLE_NAME);
	      	            	      
	      Cursor c = sqlBuilder.query(
	    		 super.getDatabase(), 
	 	         projection, 
	 	         selection, 
	 	         selectionArgs, 
	 	         null, 
	 	         null, 
	 	         sortOrder);

	      c.setNotificationUri(getContext().getContentResolver(), uri);
	      return c;
	}
	
	@Override
	public String getType(Uri uri) {
		switch (uriMatcher.match(uri)) {
		
		//find all experiences
		case EVENT_ITEM_ID:
			return "vnd.android.cursor.item/vnd.com.bjorsond.android.eventToItemMapper";
		
		//find one particular experience
		case EVENT_ID:
			return "vnd.android.cursor.item/vnd.com.bjorsond.android.eventToItemMapper";
			
		default:
	          throw new IllegalArgumentException("Unsupported URI: " + uri); 
		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String where,
			String[] whereArgs) {
		
		int count = 0;
		switch (uriMatcher.match(uri)) {
		case EVENT_ITEM_ID:
			String eventItemID = uri.getPathSegments().get(1);
			//update row where experienceItemID=experienceItemID
			count = super.getDatabase().update(SQLStatements.EVENT_TO_EVENT_ITEM_DATABASE_TABLE_NAME, values, eventItemID+"="+EventColumns.EVENT_ITEMS_ID
					+ (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
			break;

		default:
			break;
		}
		return count;
	}

    static {
    	uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    	uriMatcher.addURI(AUTHORITY, SQLStatements.EVENT_TO_EVENT_ITEM_DATABASE_TABLE_NAME+"/#", EVENT_ID);
    	uriMatcher.addURI(AUTHORITY, SQLStatements.EVENT_TO_EVENT_ITEM_DATABASE_TABLE_NAME+"/#", EVENT_ITEM_ID);
    	
    	eventsToEventItemsProjectionMap = new HashMap<String, String>();
    	eventsToEventItemsProjectionMap.put(EventColumns._ID, EventColumns._ID);
    	eventsToEventItemsProjectionMap.put(EventItemsColumns.EVENT_ITEM_ID, EventItemsColumns.EVENT_ITEM_ID);
    }
	
}
