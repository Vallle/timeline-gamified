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

import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.bjorsond.android.timeline.database.SQLStatements;
import com.bjorsond.android.timeline.models.EventItem.EventItemsColumns;
import com.bjorsond.android.timeline.models.ReflectionNote.ReflectionColumns;
import com.bjorsond.android.timeline.models.SimpleNote.NoteColumns;

public class ReflectionProvider extends BaseContentProvider {
	public static final String TAG = "ReflectionProvider";

    private static final int REFLECTIONS = 1;
    private static final int REFLECTION_ID = 2;
    private static final UriMatcher sUriMatcher;
    private static HashMap<String, String> sReflectionsProjectionMap;
    
    public static final String AUTHORITY = "com.bjorsond.android.timeline.database.providers.ReflectionProvider";

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		
		queryBuilder.setTables(SQLStatements.REFLECTION_DATABASE_TABLE_NAME);
		
		switch (sUriMatcher.match(uri)) {
		//select all notes
		case REFLECTIONS:
			queryBuilder.setProjectionMap(sReflectionsProjectionMap);
			break;
			
		//select notes by ID
		case REFLECTION_ID: 
			queryBuilder.setProjectionMap(sReflectionsProjectionMap);
			queryBuilder.appendWhere(ReflectionColumns._ID + "=" + uri.getPathSegments().get(1));
			break;
			
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
        // Get the database and run the query

        Cursor c = queryBuilder.query(super.getDatabase(), projection, selection, selectionArgs, null, null, null);

        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
	}
	
	@Override
	public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
        case REFLECTIONS:
            return NoteColumns.CONTENT_TYPE;

        case REFLECTION_ID:
            return NoteColumns.CONTENT_ITEM_TYPE;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
	}

	@Override
	public int update(Uri uri, ContentValues values, String where,
			String[] whereArgs) {
		
	        int count;
	        switch (sUriMatcher.match(uri)) {
	        case REFLECTIONS:
	            count = super.getDatabase().update(SQLStatements.NOTE_DATABASE_TABLE_NAME, values, where, whereArgs);
	            break;

	        case REFLECTION_ID:
	            String noteId = uri.getPathSegments().get(1);
	            count = super.getDatabase().update(SQLStatements.NOTE_DATABASE_TABLE_NAME, values, NoteColumns._ID + "=" + noteId
	                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
	            break;

	        default:
	            throw new IllegalArgumentException("Unknown URI " + uri);
	        }

	        getContext().getContentResolver().notifyChange(uri, null);
	        return count;
	}
	  
    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, SQLStatements.REFLECTION_DATABASE_TABLE_NAME, REFLECTIONS);
        sUriMatcher.addURI(AUTHORITY, SQLStatements.REFLECTION_DATABASE_TABLE_NAME+"/#", REFLECTION_ID);
        
        sReflectionsProjectionMap = new HashMap<String, String>();
        sReflectionsProjectionMap.put(NoteColumns._ID, NoteColumns._ID);
        sReflectionsProjectionMap.put(NoteColumns.TITLE, NoteColumns.TITLE);
        sReflectionsProjectionMap.put(NoteColumns.NOTE, NoteColumns.NOTE);
        sReflectionsProjectionMap.put(NoteColumns.CREATED_DATE, NoteColumns.CREATED_DATE);
        sReflectionsProjectionMap.put(NoteColumns.MODIFIED_DATE, NoteColumns.MODIFIED_DATE);
        sReflectionsProjectionMap.put(EventItemsColumns.USERNAME, EventItemsColumns.USERNAME);
    }
}
