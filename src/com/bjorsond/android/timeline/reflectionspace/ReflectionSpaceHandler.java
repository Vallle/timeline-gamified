package com.bjorsond.android.timeline.reflectionspace;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import javax.xml.datatype.Duration;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jivesoftware.smack.SmackAndroid;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;

import com.bjorsond.android.timeline.R;
import com.swarmconnect.SwarmActivity;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import de.imc.mirror.sdk.DataObject;
import de.imc.mirror.sdk.DataObjectListener;
import de.imc.mirror.sdk.OfflineModeHandler.Mode;
import de.imc.mirror.sdk.Space.PersistenceType;
import de.imc.mirror.sdk.android.ConnectionConfiguration;
import de.imc.mirror.sdk.android.ConnectionConfigurationBuilder;
import de.imc.mirror.sdk.android.ConnectionHandler;
import de.imc.mirror.sdk.android.DataHandler;
import de.imc.mirror.sdk.android.DataObjectBuilder;
import de.imc.mirror.sdk.android.Space;
import de.imc.mirror.sdk.android.SpaceConfiguration;
import de.imc.mirror.sdk.android.SpaceHandler;
import de.imc.mirror.sdk.android.SpaceMember;
import de.imc.mirror.sdk.exceptions.ConnectionStatusException;
import de.imc.mirror.sdk.exceptions.SpaceManagementException;
import de.imc.mirror.sdk.exceptions.UnknownEntityException;

public class ReflectionSpaceHandler extends SwarmActivity{

//	static String userName = "timelinetester";
//	static String userPassword = "timetest";
	static String domain = "mirror-server-ntnu";
	static String appID = "TimelineApplication";
	static String serverIP = "129.241.103.122";
	static int port = 5222;
	
	static String testSpaceID = "team#74";
	
	public static void insertToReflectionSpace(Context context, String content){
//		System.setProperty("smack.debugEnabled", "true");
//		XMPPConnection.DEBUG_ENABLED = true;
		SmackConfiguration.setPacketReplyTimeout(10000);
		ConnectionConfigurationBuilder builder = new ConnectionConfigurationBuilder(domain, appID);
		builder.setHost(serverIP);
		builder.setPort(port);
		ConnectionConfiguration connectionConfig = builder.build();

		ReflectionSpaceUserPreferences loginInfo = ReflectionSpaceUserPreferences.load(context);
		String userName = loginInfo.getString(ReflectionSpaceUserPreferences.PREF_USER_NAME, null);
		String password = loginInfo.getString(ReflectionSpaceUserPreferences.PREF_PASSWORD, null);
		if(userName == null || password == null){
			Toast.makeText(context, "Login failed, please login to the reflection space again", Toast.LENGTH_SHORT).show();
			return;
		}
		
		Log.i("GET USER+PW", userName+"+"+password);

		ConnectionHandler connectionHandler = new ConnectionHandler(userName, password, connectionConfig);
		
		try{
			connectionHandler.connect();
		} catch(ConnectionStatusException e){
			Log.w("COULD NOT CONNECT", e);
		}
		
		
		SpaceHandler spaceHandler = new SpaceHandler(context, connectionHandler, "offlineDB");
		spaceHandler.setMode(Mode.ONLINE);
		
		
		String userJID = userName + "@" + connectionHandler.getConfiguration().getDomain();
		registerUserOnReflectionSpace(testSpaceID, userJID, context);
		
		Space reflectionSpace = (Space) spaceHandler.getSpace(testSpaceID);
		

		//Data handler
		DataHandler dataHandler = new DataHandler(connectionHandler, spaceHandler);
		dataHandler.setMode(Mode.ONLINE);
		dataHandler.clear();
		
		try {
			dataHandler.registerSpace(reflectionSpace.getId());
		} catch (UnknownEntityException e) {
			e.printStackTrace();
		}
		DataObjectListener myListener = new DataObjectListener() {
			public void handleDataObject(DataObject dataObject, String spaceId) {
				String objectId = dataObject.getId();
				Log.i("Received object ", objectId + " from space " + spaceId);
			}
		};
		dataHandler.addDataObjectListener(myListener);
		
		
		//Create the data object using the object builder
		DataObjectBuilder dataObjectBuilder = new DataObjectBuilder("foo", "mirror:application:"+appID+":foo");
		dataObjectBuilder.addElement("bar", content, false);
		DataObject dataObject = dataObjectBuilder.build();

		publishElementToSpace(dataObject, reflectionSpace, dataHandler, context);
		
		List<DataObject> allObjectsFromSpace = getAllObjectsFromSpace(reflectionSpace.getId(), dataHandler);
		Log.i("SIZE OF LIST WITH OBJECTS FROM SPACE",allObjectsFromSpace.size()+"");
		if(!allObjectsFromSpace.isEmpty())
			for (int j = 0; j < allObjectsFromSpace.size(); j++) {
				Log.i("Object number "+j+" from list",allObjectsFromSpace.iterator().next()+"");
			}
		else
			Log.i("List is empty","List is empty");
	}
	
	
	
	/**
	 * Publishes a dataobject to the given space
	 * @param dataObject
	 * @param space
	 * @param dataHandler
	 * @param context
	 */
	public static void publishElementToSpace(DataObject dataObject, Space space, DataHandler dataHandler, Context context){
		try{
			dataHandler.publishDataObject(dataObject, space.getId());
			Log.i("Publish object", "Attempted to published on space "+space.getName()+", no exceptions cathed.");
			Toast.makeText(context, "Object published on space "+space.getName(), Toast.LENGTH_SHORT).show();
		} catch(UnknownEntityException e){
			Log.e("Space does not exist or is not acessible", e.getMessage());
		}
	}
	
	
	
	/**
	 * Returns a list with all objects contained in the space with the @param spaceID
	 * @param spaceID
	 * @param dataHandler
	 * @return
	 */
	public static List<DataObject> getAllObjectsFromSpace(String spaceID, DataHandler dataHandler){
		List<DataObject> allObjectsFromSpace = null;
		try {
			allObjectsFromSpace = dataHandler.retrieveDataObjects(testSpaceID);
			
			
			//Logging things for debugging purposes
			List<de.imc.mirror.sdk.Space> handledSpaces = dataHandler.getHandledSpaces();
			if(!handledSpaces.isEmpty()){
				Log.i("Size of handledSpaces", handledSpaces.size()+"");
				for(int i=0; i<handledSpaces.size(); i++){
					Log.i("Spaces handled by datahandler", handledSpaces.get(i).toString());
				}
			}else Log.i("NO SPACES HANDLED BY DATAHANDLER LOL", "WTF");
			Log.i("Mode of dataHandler", dataHandler.getMode().toString());
		} catch (UnknownEntityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.i("Size of all objects gotten from space before return", allObjectsFromSpace.size()+"");
		return allObjectsFromSpace;
	}
	
	
	
	/**
	 * Login with an admin account to add the user to the reflection space
	 * @param spaceID
	 * @param userJID
	 */
	public static void registerUserOnReflectionSpace(String spaceID, String userJID, Context context){
		SmackConfiguration.setPacketReplyTimeout(10000);
		ConnectionConfigurationBuilder builder = new ConnectionConfigurationBuilder(domain, appID);
		builder.setHost(serverIP);
		builder.setPort(port);
		ConnectionConfiguration connectionConfig = builder.build();

		ConnectionHandler connectionHandler = new ConnectionHandler("timelinetester", "timetest", connectionConfig);
		
		try{
			connectionHandler.connect();
		} catch(ConnectionStatusException e){
			Log.w("COULDN'T CONNECT", e);
		}
		
		SpaceHandler spaceHandler = new SpaceHandler(context, connectionHandler, "offlineDB");
		spaceHandler.setMode(Mode.ONLINE);
		Space reflectionSpace;
		reflectionSpace = (Space) spaceHandler.getSpace(spaceID);	
		
		SpaceConfiguration spaceConfig = reflectionSpace.generateSpaceConfiguration();
		spaceConfig.addMember(new SpaceMember(userJID, SpaceMember.Role.MEMBER));
		try {
			spaceHandler.configureSpace(spaceID, spaceConfig);
		} catch (SpaceManagementException e) {
			Log.w("Failed to configure space", e);
			e.printStackTrace();
		} catch (ConnectionStatusException e) {
			Log.w("Cannot create space when offline", e);
			e.printStackTrace();
		}
		
		connectionHandler.disconnect();
	}
	
	
	
	
	
	
	/**
	 * Uses plugin documented at http://www.igniterealtime.org/projects/openfire/plugins/userservice/readme.html
	 */
	public static void createUserOnServer(String username, String password, String name, String email){
		String secretKey = "vXHN3sLx";
		String encodedUsername = null;
		String encodedPassword = null;
		String encodedName = null;
		String encodedEmail = null;
		try {
			encodedUsername = URLEncoder.encode(username, "UTF-8");
			encodedPassword = URLEncoder.encode(password, "UTF-8");
			encodedName = URLEncoder.encode(name, "UTF-8");
			encodedEmail = URLEncoder.encode(name, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
		
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost("http://"+serverIP+":9090/plugins/userService/userservice?type=add&secret="+secretKey+"&username="
										+encodedUsername+"&password="+encodedPassword+"&name="+encodedName+"&email="+encodedEmail);

	    try {
	        // Execute HTTP Post Request
	        HttpResponse response = httpclient.execute(httppost);
	        
	        String responseBody = EntityUtils.toString(response.getEntity());
	        Log.i("RESPONSE HTTP REQUEST (CREATE NEW USER)",responseBody);
	        
	    } catch (ClientProtocolException e) {
	        // TODO Auto-generated catch block
	    } catch (IOException e) {
	        // TODO Auto-generated catch block
	    } 
	}
	
	
	
	

	
	
//	// create space configuration with the current user as space moderator
//	Space.Type type = Space.Type.TEAM;
//	String name = "Dream Team";
//	String owner = connectionHandler.getCurrentUser().getBareJID();
//	PersistenceType isPersistent = PersistenceType.ON;
//	Duration persistenceDuration = null;
//	SpaceConfiguration spaceConfig = new SpaceConfiguration(type, name, owner, isPersistent, persistenceDuration);
//
//	String userJID = loginInfo.getString(ReflectionSpaceUserPreferences.PREF_USER_NAME, null) + "@" + connectionHandler.getConfiguration().getDomain();
//	spaceConfig.addMember(new SpaceMember(userJID, SpaceMember.Role.MEMBER));
//	// create space with this configuration
//	Space myNewTeamSpace = null;
//	try {
//		myNewTeamSpace = (Space) spaceHandler.createSpace(spaceConfig);
//	} catch (SpaceManagementException e) {
//	// failed to create space
//	// add proper exception handling
//	} catch (ConnectionStatusException e) {
//	// cannot create a space when offline
//	// add proper exception handling
//	}
//	Log.i("DREAM TEAM SPACE ID", myNewTeamSpace.getId());
	
	

//	List<de.imc.mirror.sdk.Space> spaces = spaceHandler.getAllSpaces();
//	for(de.imc.mirror.sdk.Space space : spaces){
//		Log.i("MEMBER OF SPACES",space.getName());
//	}
	


	//Request the private space of the current user. If the space doesn't exist, it is created.
//	Space myPrivateSpace = spaceHandler.getDefaultSpace();
//	if(myPrivateSpace == null){
//		try{
//			myPrivateSpace = spaceHandler.createDefaultSpace();
//		} catch (SpaceManagementException e){
//			Log.wtf("Failed to create space", e);
//		} catch (ConnectionStatusException e){
//			Log.e("Space offline", "Cannot create a space when offline");
//			Log.w("Space offline", e);
//		}
//	}
//	Log.i("Members of user's private space", myPrivateSpace.getMembers().iterator().next().getJID());
	
}
