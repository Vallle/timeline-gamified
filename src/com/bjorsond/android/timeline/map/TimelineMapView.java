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
package com.bjorsond.android.timeline.map;
import java.util.ArrayList;
import java.util.List;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;

import com.bjorsond.android.timeline.database.DatabaseHelper;
import com.bjorsond.android.timeline.database.TimelineDatabaseHelper;
import com.bjorsond.android.timeline.database.UserGroupDatabaseHelper;
import com.bjorsond.android.timeline.database.contentmanagers.ContentLoader;
import com.bjorsond.android.timeline.models.BaseEvent;
import com.bjorsond.android.timeline.models.Experience;
import com.bjorsond.android.timeline.utilities.Constants;
import com.bjorsond.android.timeline.utilities.MyLocation;
import com.bjorsond.android.timeline.utilities.Utilities;
import com.bjorsond.android.timeline.R;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

/**
 * This class open a new mapview of the timeline that the application is running at a given time.
 * @author andrstor
 *
 */

public class TimelineMapView extends MapActivity {

	private MapView mapView;
	private List<Overlay> mapOverlays;
	private Drawable drawable;
	private TimelineMapItemizedOverlay itemizedOverlay;
	private ContentLoader contentLoader;
	private MapController mapController;
	private DatabaseHelper eventDatabaseHelper;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.timelinemaplayout);
		setupViews();
		setUpMapControllers();
		
		if(getIntent().getAction().equals(Constants.INTENT_ACTION_OPEN_MAP_VIEW_FROM_TIMELINE)) {
			addEventsToMap(loadEventsWithGeolocationFromDatabase());
		}
		else if(getIntent().getAction().equals(Constants.INTENT_ACTION_OPEN_MAP_VIEW_FROM_DASHBOARD)) {
			addAllTimelineAppEventsToMap();
		}
	}

	private void setUpMapControllers() {
		mapView.setBuiltInZoomControls(true);
		mapController = mapView.getController();
		mapController.setZoom(18);
		try {
			mapController.animateTo(MyLocation.getInstance(this).getGeoPointLocation());
		} catch (NullPointerException e) {
			Log.e(this.getClass().getSimpleName(), "Location not availiable");
		}
		
		mapOverlays = mapView.getOverlays();
	}
	
	/**
	 * Adds all the events saved in all the timeline to the map view
	 */
	private void addAllTimelineAppEventsToMap() {
		new TimelineDatabaseHelper(this, Constants.ALL_TIMELINES_DATABASE_NAME);
		contentLoader = new ContentLoader(this);
		new UserGroupDatabaseHelper(this, Constants.USER_GROUP_DATABASE_NAME);
		ArrayList<Experience> experiences = contentLoader.LoadAllExperiencesFromDatabase();
		TimelineDatabaseHelper.getCurrentTimeLineDatabase().close();
		UserGroupDatabaseHelper.getUserDatabase().close();
		
		for (Experience experience : experiences) {
			eventDatabaseHelper = new DatabaseHelper(this, experience.getTitle()+".db");
			System.out.println(experience.getTitle());
			addEventsToMap(loadEventsWithGeolocationFromDatabase());
			eventDatabaseHelper.close();
		}
		
	}
	
	private void setupViews() {
		mapView = (MapView) findViewById(R.id.mapview);
	}
	@Override	
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	/**
	 * Loads all events in the timeline from the database
	 * @return 
	 */
	private ArrayList <BaseEvent> loadEventsWithGeolocationFromDatabase() {
		contentLoader = new ContentLoader(this);
		return contentLoader.LoadAllEventsFromDatabase();
	}
	
	/**
	 * Add events to the map overlay
	 * @param allEvents
	 */
	private void addEventsToMap(ArrayList<BaseEvent> allEvents) {
		
		for (BaseEvent event : allEvents) {
			OverlayItem overlayItem = new OverlayItem(event.getGeoPointLocation(), "", event.getId());

			drawable = this.getResources().getDrawable(Utilities.getMapImageIcon(event)); //CASTED FROM BASEEVENT TO EVENT
			
			itemizedOverlay = new TimelineMapItemizedOverlay(drawable, this);
			itemizedOverlay.addOverlay(overlayItem);
			mapOverlays.add(itemizedOverlay);
		}
	}
}
