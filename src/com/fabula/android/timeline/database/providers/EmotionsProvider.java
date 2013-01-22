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
package com.fabula.android.timeline.database.providers;

import java.util.HashMap;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.fabula.android.timeline.database.SQLStatements;
import com.fabula.android.timeline.models.Emotion.EmotionColumns;
import com.fabula.android.timeline.models.Event.EventColumns;

public class EmotionsProvider extends BaseContentProvider {
	
	public static final Uri CONTENT_URI = Uri
			.parse("content://com.fabula.android.timeline.database.providers.emotionsprovider");

	private static HashMap<String, String> emotionsColumnMapping;
	
	public static final String AUTHORITY = "com.fabula.android.timeline.database.providers.emotionsprovider";

	public Cursor query(Uri uri, String[] columns, String where,
			String[] whereArgs, String sortOrder) {
		
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		
		queryBuilder.setTables(SQLStatements.EMOTIONS_DATABASE_TABLE_NAME);
		
		queryBuilder.setProjectionMap(emotionsColumnMapping);
		
		Cursor cursorOnRetriewedRows = queryBuilder.query(
				super.getDatabase(), 
				columns, 
				where, 
				whereArgs, 
				null, 
				null, 
				null);
		
		cursorOnRetriewedRows.setNotificationUri(getContext().getContentResolver(), uri);
		
		return cursorOnRetriewedRows;
	}
	
	public int delete(Uri uri, String eventID, int emotionType, String[] whereArgs) {
		
		int count = 0;
		
		String where = eventID+ " = " +EmotionColumns.EVENT_ID+ " AND "+emotionType+ " = "+ EmotionColumns.EMOTION_TYPE;
		
		count = super.getDatabase().delete(SQLStatements.EMOTIONS_DATABASE_TABLE_NAME, where, whereArgs);
		return count;
	}
	
	public int delete(Uri uri, String eventID, String[] whereArgs) {
		int count = 0;
		
		String where = EmotionColumns.EVENT_ID +" = '"+eventID+"'";
		
		count = super.getDatabase().delete(SQLStatements.EMOTIONS_DATABASE_TABLE_NAME, where, whereArgs);
		return count;
	}
	
	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
        
        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }
        
        long rowId = super.getDatabase().insertWithOnConflict(SQLStatements.EMOTIONS_DATABASE_TABLE_NAME, "", values, SQLiteDatabase.CONFLICT_REPLACE);
        
        //---if added successfully---
	      if (rowId>0)
	      {
	         Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowId);
	         getContext().getContentResolver().notifyChange(_uri, null);    
	         return _uri;                
	      }   
		throw new SQLException("Failed to insert row into " + uri);
	}

	public int update(Uri uri, ContentValues values, String eventID, int emotionType,
			String[] whereArgs) {
	
		String where = eventID+ " = " +EmotionColumns._ID+ " AND "+emotionType+ " = "+ EmotionColumns.EMOTION_TYPE;
        int count = super.getDatabase().update(SQLStatements.EMOTIONS_DATABASE_TABLE_NAME, values, where, whereArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
	}
	static{
		emotionsColumnMapping = new HashMap<String, String>();
		emotionsColumnMapping.put(EmotionColumns.EMOTION_ID, EmotionColumns.EMOTION_ID);
		emotionsColumnMapping.put(EmotionColumns.EVENT_ID, EventColumns._ID);
		emotionsColumnMapping.put(EmotionColumns.EMOTION_TYPE, EmotionColumns.EMOTION_TYPE);
	}
}
