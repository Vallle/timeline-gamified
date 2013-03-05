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

import java.util.UUID;

import android.net.Uri;
import android.provider.BaseColumns;

import com.bjorsond.android.timeline.database.providers.EmotionsProvider;
import com.bjorsond.android.timeline.R;

public class Emotion {
	
	private String emotionid;
	private EmotionEnum emotionType;

	public Emotion (){}
	
	public Emotion(EmotionEnum emotionType) {
		this.emotionid = UUID.randomUUID().toString();
		this.emotionType = emotionType;
	}
	
	public Emotion(String emotionid, EmotionEnum emotionType) {
		this.emotionid = emotionid;
		this.emotionType = emotionType;
	}
	
	public String getEmotionid() {
		return emotionid;
	}

	public void setEmotionid(String emotionid) {
		this.emotionid = emotionid;
	}


	public EmotionEnum getEmotionType() {
		return emotionType;
	}

	public void setEmotionType(EmotionEnum emotionType) {
		this.emotionType = emotionType;
	}

	public enum EmotionEnum{
		LIKE(R.drawable.emo_im_happy, 1), COOL(R.drawable.emo_im_cool,2), DISLIKE(R.drawable.emo_im_dislike,3), SAD(R.drawable.emo_im_sad,4);

		private int icon;
		private int type;
		
		EmotionEnum(int icon, int type){
			this.icon = icon;
			this.type = type;
		}
		
		public int getIcon(){
			return icon;
		}
		public int getType() {
			return type;
		}
		
		public String getName(){
			return name();
		}
	}
	
	public static final class EmotionColumns implements BaseColumns {
		
		public static final Uri CONTENT_URI = Uri.parse("content://"+EmotionsProvider.AUTHORITY);

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.bjorsond.emotions";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.bjorsond.emotions";
        public static final String EVENT_ID = "exp_id";
        public static final String EMOTION_ID = "_id";
        public static final String EMOTION_TYPE = "type";
	}

}

