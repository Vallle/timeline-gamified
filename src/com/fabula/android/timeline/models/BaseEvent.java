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
package com.fabula.android.timeline.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import android.accounts.Account;
import android.location.Location;

import com.google.android.maps.GeoPoint;

/**
 * The model for the BaseEvent which includes common properites of events.
 * 
 * 
 * 
 * @author andekr
 *
 */
public class BaseEvent {
	
	private String id;
	private String experienceid;
	private transient Location location;
	private transient Date datetime;
	private double longitude;
	private double latitude;
	private long datetimemillis;
	protected boolean shared;
	private transient Account user;
	private String creator;
	private transient List<String> tags;
	//For GSON only
	protected String className;
	protected List<EventItem> eventItems;
	protected List<Emotion> emotionList;
	private boolean average;
	private double arousal;
	private double valence;
	
	public BaseEvent() {	
	}
	
	public BaseEvent(String exID, Location location, Account user) {
		super();
		id =  UUID.randomUUID().toString();
		this.experienceid = exID;
		
		setDatetime(new Date());
		setLocation(location);
		setUser(user);
	}
	
	public BaseEvent(String id, String exID, Date dateTime, Location location, Account user) {
		super();
		this.id = id;
		this.experienceid = exID;
		setDatetime(dateTime);
		setLocation(location);
		setUser(user);
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public void generateNewId() {
		id =  UUID.randomUUID().toString();
	}

	public String getExperienceid() {
		return experienceid;
	}

	public void setExperienceid(String experienceid) {
		this.experienceid = experienceid;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		try {
			this.longitude = location.getLongitude();
			this.latitude = location.getLatitude();
			this.location = location;
		} catch (Exception e) {
			// TODO: handle exception
		}
	
	}

	public Date getDatetime() {
		return datetime;
	}

	public void setDatetime(Date datetime) {
		datetimemillis = datetime.getTime();
		this.datetime = datetime;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public long getDatetimemillis() {
		return datetimemillis;
	}

	private void setUser(Account user) {
		this.creator = user.name;
		this.user = user;
		
	}
	
	public Account getUser() {
		if(user==null)
			this.user = new Account(creator, "com.google");
		return user;
	}
	
	public GeoPoint getGeoPointLocation() {
		return new GeoPoint((int)(getLatitude() * 1E6) , (int) (getLongitude() * 1E6));
	}
	
	public boolean isShared() {
		return shared;
	}

	public void setShared(boolean shared) {
		this.shared = shared;
	}
	
	public int isSharedAsInt() {
		return isShared() ? 1 : 0;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public void setMoodX(double x) {
		this.valence = x;
	}
	
	public void setMoodY(double y) {
		this.arousal = y;
	}
	
	public double getMoodX() {
		return this.valence;
	}
	
	public double getMoodY() {
		return this.arousal;
	}
	
	public List<EventItem> getEventItems() {
		if(eventItems==null)
			eventItems = new ArrayList<EventItem>();
		return eventItems;
	}
	public void setEventItems(List<EventItem> eventItems) {
		this.eventItems = eventItems;
	}
	
	public List<Emotion> getEmotionList() {
		if(emotionList==null)
			emotionList = new ArrayList<Emotion>();
		return emotionList;
	}

	public void setEmotionList(List<Emotion> emotionList) {
		this.emotionList = emotionList;
	}

	public boolean isAverage() {
		return average;
	}

	public void setAverage(boolean average) {
		this.average = average;
	}

	public List<String> getTags() {
		if(tags==null)
			tags = new ArrayList<String>();
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}
	
	public boolean hasTag(String tagName) {
		for (String tag : getTags()) {
			if(tag.equals(tagName)) {
				return true;
			}
		}
		return false;
	}
	
	

}
