package com.bjorsond.android.timeline;

import java.util.Calendar;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

// The class has to extend the BroadcastReceiver to get the notification from the system
public class TimeAlarm extends BroadcastReceiver {

	

	
	@Override
	public void onReceive(Context context, Intent paramIntent) {
		Calendar c = Calendar.getInstance();
		Log.i("CALENDAR TESTING", "in TimeAlarm");
		if (	(paramIntent.getAction().equals("notify"))// &&
				//c.get(Calendar.DAY_OF_WEEK) != DashboardActivity.getLastRefDate().get(Calendar.DAY_OF_WEEK)
			) notification(context, paramIntent);
		
		
	
	 }

	
	public void notification(Context context, Intent paramIntent) {
		// Request the notification manager
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		  
		// Create a new intent which will be fired if you click on the notification
		Intent intent = new Intent(context, TimeAlarm.class);
		
		// Attach the intent to a pending intent
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		 
		// Create the notification
		Notification notification = new Notification(R.drawable.ic_menu_sumday, "Time to reflect?", System.currentTimeMillis());
		notification.setLatestEventInfo(context, "Time to reflect", "To keep the consecutive bonus, add a new reflection!",pendingIntent);
		// Hide the notification after its selected
	    notification.flags |= Notification.FLAG_AUTO_CANCEL;
		// Fire the notification
		notificationManager.notify(1, notification);
	}

	

}
