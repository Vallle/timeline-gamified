package com.bjorsond.android.timeline.reflectionspace;

import android.content.Context;
import android.content.SharedPreferences;

public class ReflectionSpaceUserPreferences {
	
	/** The name of the user name preference. */
	public static final String PREF_USER_NAME = "user_name";
	/** The name of the password preference. */
	public static final String PREF_PASSWORD = "password";
	
	/** The name of the default preferences file. */
	public static final String PREF_DEFAULT_FILE_NAME = "user.pref";
	
	private SharedPreferences mPreferences;
	private boolean mLoaded = false;
	
	/**
	 * Private constructor. Use <code>ReflectionSpaceUserPreferences.load(Context)</code>.
	 * @see ReflectionSpaceUserPreferences#load(Context)
	 */
	private ReflectionSpaceUserPreferences() { }
	
	private void loadPreferences(String preferenceFile, Context context) {
		mPreferences = context.getSharedPreferences(
				preferenceFile, Context.MODE_PRIVATE);
		
		mLoaded = true;
	}
	
	private void throwIfNotLoaded() throws IllegalStateException {
		if (!mLoaded)
			throw new IllegalStateException("Preferences not loaded");
	}
	
	public String getString(String key, String defValue) {
		throwIfNotLoaded();
		
		return mPreferences.getString(key, defValue);
	}
	
	public int getInt(String key, int defValue) {
		throwIfNotLoaded();
		
		return mPreferences.getInt(key, defValue);
	}
	
	public long getLong(String key, long defValue) {
		throwIfNotLoaded();
		
		return mPreferences.getLong(key, defValue);
	}
	
	public boolean getBoolean(String key, boolean defValue) {
		throwIfNotLoaded();
		
		return mPreferences.getBoolean(key, defValue);
	}
	
	public void putString(String key, String value) {
		mPreferences.edit().putString(key, value).commit();
	}
	
	public void putInt(String key, int value) {
		mPreferences.edit().putInt(key, value).commit();
	}
	
	/**
	 * Loads the shared preferences from the specified file.
	 * @param preferenceFile The file to load from.
	 * @param context The context to use.
	 * @return A <code>ReflectionSpaceUserPreferences</code> instance from which preferences
	 * can be read.
	 */
	public static ReflectionSpaceUserPreferences load(String preferenceFile, Context context) {
		ReflectionSpaceUserPreferences preferences = new ReflectionSpaceUserPreferences();
		preferences.loadPreferences(preferenceFile, context);
		
		return preferences;
	}
	
	/**
	 * Loads the shared preferences from the default preference file.
	 * @param context The context to use.
	 * @return A <code>ReflectionSpaceUserPreferences</code> instance from which preferences
	 * can be read.
	 */
	public static ReflectionSpaceUserPreferences load(Context context) {
		return load(PREF_DEFAULT_FILE_NAME, context);
	}
}
