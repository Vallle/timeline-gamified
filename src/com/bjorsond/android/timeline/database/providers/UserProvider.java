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

import com.bjorsond.android.timeline.database.SQLStatements;
import com.bjorsond.android.timeline.models.User.UserColumns;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class UserProvider extends BaseContentProvider {
	
	public static final Uri CONTENT_URI = Uri.parse("content://com.bjorsond.android.timeline.database.providers.userprovider");
	public static final String AUTHORITY = "com.bjorsond.android.timeline.database.providers.userprovider";
	
	private static HashMap<String, String> userColumnsMapping;
	
	
	@Override
	public Cursor query(Uri uri, String[] columns, String where,
			String[] whereArgs, String sortOrder) {
		
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		
		queryBuilder.setTables(SQLStatements.USER_DATABASE_TABLE_NAME);
		queryBuilder.setProjectionMap(userColumnsMapping);
		
		Cursor cursorOnRetriewedRows = queryBuilder.query(
				super.getUserDatabase(), 
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
	public Uri insert(Uri uri, ContentValues initialValues) {
        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }
        
        long rowId = super.getUserDatabase().insert(SQLStatements.USER_DATABASE_TABLE_NAME, "", values);
        
		if (rowId > 0) {
		    Uri userUri = ContentUris.withAppendedId(UserColumns.CONTENT_URI, rowId);
		    getContext().getContentResolver().notifyChange(userUri, null);
		    return userUri;
		}
		throw new SQLException("Failed to insert row into " + uri);
	}
	
	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		int count = 0;
		count = super.getUserDatabase().delete(SQLStatements.USER_DATABASE_TABLE_NAME, where, whereArgs);
		return count;
	}
	
	static {
		userColumnsMapping = new HashMap<String, String>();
		userColumnsMapping.put(UserColumns._ID, UserColumns._ID);
		userColumnsMapping.put(UserColumns.USER_NAME, UserColumns.USER_NAME);
	}
}
