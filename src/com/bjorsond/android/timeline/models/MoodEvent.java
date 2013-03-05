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
package com.bjorsond.android.timeline.models;

import java.text.DecimalFormat;
import java.util.Date;

import android.accounts.Account;
import android.location.Location;

import com.bjorsond.android.timeline.R;

public class MoodEvent extends BaseEvent{
	
	private MoodEnum mood;
	private boolean average;
	
	public MoodEvent(){
		className = this.getClass().getSimpleName();
		setShared(false);
		setAverage(false);
	}

	public MoodEvent(String experienceID, Location location, MoodEnum mood, Account user) {
		super(experienceID, location, user);
		this.mood = mood;
		className = this.getClass().getSimpleName();
		setShared(false);
		setAverage(false);
		setMoodX(mood.getMoodX());
		setMoodY(mood.getMoodY());
	}
	
	public MoodEvent(String id, String experienceID, Date dateTime, Location location, MoodEnum mood, Account user) {
		super(id, experienceID, dateTime, location, user);
		this.mood = mood;
		className = this.getClass().getSimpleName();
		setShared(false);
		setAverage(false);
		setMoodX(mood.getMoodX());
		setMoodY(mood.getMoodY());
	}

	public MoodEnum getMood() {
		return mood;
	}

	public void setMood(MoodEnum mood) {
		this.mood = mood;
	}
	
	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public boolean isShared() {
		return shared;
	}

	public void setShared(boolean shared) {
		this.shared = shared;
	}
	
	public boolean isAverage() {
		return average;
	}

	public void setAverage(boolean average) {
		setMoodX(getMood().getAvgX());
		setMoodY(getMood().getAvgY());
		this.average = average;
	}
	
	@Override
	public String toString() {
		DecimalFormat twoDecimals = new DecimalFormat("#0.00");
		
		if(average) {
			return "Valence: "+ twoDecimals.format(getMood().getAvgX())+ "   Arousal: "+twoDecimals.format(getMood().getAvgY());
		}
		return "Valence: "+ twoDecimals.format(getMoodX())+ "   Arousal: "+twoDecimals.format(getMoodY());
	}

	public enum MoodEnum {
		HAPPY(1,1), NERVOUS(0,1), CALM(1,0), SAD(0,0);

		private double x,y;
		private double avgX, avgY;
				
		MoodEnum(double x, double y){
			this.x = x;
			this.y = y;
		}
		
		public int getIcon(){
			
			switch (this) {
			case HAPPY:
				 return R.drawable.happy;
			case NERVOUS:
				return R.drawable.nervous;
			case CALM:
				 return R.drawable.calm;
			case SAD:
				return R.drawable.sad;
			default:
				return R.drawable.calm;
			}
		}
				
		public double getMoodX() {
			return x;
		}
		
		public double getMoodY() {
			return y;
		}
		
		public double getAvgX() {
			return avgX;
		}
		
		public double getAvgY()
		{
			return avgY;
		}
		public String getName(){
			return name();
		}
		
		public static MoodEnum getType(double x, double y) {
			
			if(x >= 0.5 && y >= 0.5) {
				
				MoodEnum m = MoodEnum.HAPPY;
				m.avgX = x;
				m.avgY = y;
				return m;
			}
			else if (x >= 0.5 && y <= 0.5 ) {
				MoodEnum m = MoodEnum.CALM;
				m.avgX = x;
				m.avgY = y;
				return m;
			}
			else if(x <= 0.5 && y <= 0.5) {
				MoodEnum m = MoodEnum.SAD;
				m.avgX = x;
				m.avgY = y;
				return m;
			}
			else if(x <= 0.5 && y >= 0.5) {
				MoodEnum m = MoodEnum.NERVOUS;
				m.avgX = x;
				m.avgY = y;
				return m;
			}
			else {
				MoodEnum m = MoodEnum.CALM;
				m.avgX = x;
				m.avgY = y;
				return m;
			}
		}
		
	}
	
}
