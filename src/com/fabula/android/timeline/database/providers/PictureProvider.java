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

import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import com.fabula.android.timeline.database.SQLStatements;
import com.fabula.android.timeline.models.EventItem.EventItemsColumns;
import com.fabula.android.timeline.models.SimplePicture.PictureColumns;

public class PictureProvider extends BaseContentProvider {
	public static String TAG = "PictureProvider";
	
	public static final int PICTURES = 1;
	public static final int PICTURE_ID = 2;
	
	private static HashMap<String, String> imageColumnMapping;
	private static final UriMatcher imageUriMatcher;
	
	public static final String AUTHORITY = "com.fabula.android.timeline.database.providers.PictureProvider";
		
	/**
	 * @see android.content.ContentProvider#query(Uri,String[],String,String[],String)
	 */
	@Override
	public Cursor query(Uri uri, String[] columns, String where,
			String[] whereArgs, String sortOrder) {
		
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		
		queryBuilder.setTables(SQLStatements.PICTURE_DATABASE_TABLE_NAME);
		
		switch (imageUriMatcher.match(uri)) {
		//select all images
		case PICTURES:
			queryBuilder.setProjectionMap(imageColumnMapping);
			break;
			
		//select image by ID
		case PICTURE_ID: 
			queryBuilder.setProjectionMap(imageColumnMapping);
			queryBuilder.appendWhere(PictureColumns._ID + "=" + uri.getPathSegments().get(1));
			break;
			
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
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
	
	@Override
	public String getType(Uri uri) {
		switch (imageUriMatcher.match(uri)) {
		case PICTURES:
			return PictureColumns.CONTENT_TYPE;
		case PICTURE_ID:
			return PictureColumns.CONTENT_ITEM_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
		
	        int count;
	        switch (imageUriMatcher.match(uri)) {
	        case PICTURES:
	            count = super.getDatabase().update(SQLStatements.PICTURE_DATABASE_TABLE_NAME, values, where, whereArgs);
	            break;
	        case PICTURE_ID:
	            String imageID = uri.getPathSegments().get(1);
	            count = super.getDatabase().update(SQLStatements.PICTURE_DATABASE_TABLE_NAME, values, PictureColumns._ID + "=" + imageID
	                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
	            break;
	        default:
	            throw new IllegalArgumentException("Unknown URI " + uri);
	        }

	        getContext().getContentResolver().notifyChange(uri, null);
	        return count;
	}
	
	static{
		imageUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		imageUriMatcher.addURI(AUTHORITY, SQLStatements.PICTURE_DATABASE_TABLE_NAME, PICTURES);
		imageUriMatcher.addURI(AUTHORITY, SQLStatements.PICTURE_DATABASE_TABLE_NAME+"/#", PICTURE_ID);
		
        imageColumnMapping = new HashMap<String, String>();
        imageColumnMapping.put(PictureColumns._ID, PictureColumns._ID);
        imageColumnMapping.put(PictureColumns.TITLE, PictureColumns.TITLE);
        imageColumnMapping.put(PictureColumns.URI_PATH, PictureColumns.URI_PATH);
        imageColumnMapping.put(PictureColumns.CREATED_DATE, PictureColumns.CREATED_DATE);
        imageColumnMapping.put(PictureColumns.DESCRIPTION, PictureColumns.DESCRIPTION);
        imageColumnMapping.put(PictureColumns.FILENAME, PictureColumns.FILENAME);
        imageColumnMapping.put(EventItemsColumns.USERNAME, EventItemsColumns.USERNAME);
	}
}
