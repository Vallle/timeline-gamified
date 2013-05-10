package com.bjorsond.android.timeline.reflectionspace;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.jivesoftware.smack.SmackConfiguration;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;
import de.imc.mirror.sdk.DataObjectListener;
import de.imc.mirror.sdk.OfflineModeHandler.Mode;
import de.imc.mirror.sdk.DataObject;
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

public class MSFHelper {
	
    ConnectionHandler xmppConnection;
    SpaceHandler spaceHandler;
    DataHandler dataHandler;
    Space reflectionSpace;
    Context mContext;
    static String domain = "mirror-server-ntnu";
	static String appID = "timeline";
	static String serverIP = "129.241.103.122";
	static String testSpaceID = "team#79";
	static int port = 5222;
	
   
    public MSFHelper(Context context) {
    	mContext = context;
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
		
		xmppConnection = new ConnectionHandler(userName, password, connectionConfig);
		try{
			xmppConnection.connect();
		} catch(ConnectionStatusException e){
			Log.w("COULD NOT CONNECT", e);
		}
		
		spaceHandler = new SpaceHandler(context, xmppConnection, "offlineDB");
		spaceHandler.setMode(Mode.ONLINE);
		
		Log.i("SPACEMODE", spaceHandler.getMode()+"");
      
		if (spaceHandler.getSpace(testSpaceID) == null) {
			// register the user only of he cannot see the space == is not registered
			String userJID = userName + "@" + xmppConnection.getConfiguration().getDomain();
			registerUserOnReflectionSpace(testSpaceID, userJID, context);
		}
      
		dataHandler = new DataHandler(xmppConnection, spaceHandler);
		dataHandler.setMode(Mode.ONLINE);
		Log.i("DATAMODE", dataHandler.getMode()+"");
		try {
			dataHandler.registerSpace(testSpaceID);
		} catch (UnknownEntityException e) {
			Log.w("Failed to register space", e);
		}
		DataObjectListener myListener = new DataObjectListener() {
			public void handleDataObject(de.imc.mirror.sdk.DataObject dataObject, String spaceId) {
				String objectId = dataObject.getId();
				Log.i("Received object ", objectId + " from space " + spaceId);
			}
		};
		dataHandler.addDataObjectListener(myListener);
		
		reflectionSpace = (Space) spaceHandler.getSpace(testSpaceID);
    }
   
   /**
    * Build and publish an object to the reflection space
    * @param content
    */
    public void publishElementToSpace(String content) {
       DataObjectBuilder dataObjectBuilder = new DataObjectBuilder("foo", "mirror:application:"+appID+":foo");
       dataObjectBuilder.addElement("bar", content, false);
       DataObject dataObject = dataObjectBuilder.build();

       try{
    	   dataHandler.publishDataObject(dataObject, reflectionSpace.getId());
    	   Log.i("Publish object", "Attempted to published on space "+reflectionSpace.getName()+", no exceptions cathed.");
    	   Toast.makeText(mContext, "Note published on space "+reflectionSpace.getName(), Toast.LENGTH_SHORT).show();
       } catch(UnknownEntityException e){
    	   Log.e("Space does not exist or is not acessible", e.getMessage());
       }
    }
   
    
    
    /**
     * Prints all objects gotten from space in LogCat
     * @param spaceId
     */
    public void listData(String spaceId) {
       List<DataObject> allObjectsFromSpace = getAllObjectsFromSpace(reflectionSpace.getId());
       Log.i("SIZE OF LIST WITH OBJECTS FROM SPACE",allObjectsFromSpace.size()+"");
       for (int i = 0; i < allObjectsFromSpace.size(); i++) {
           Log.i("OBJECT NUMBER "+i,allObjectsFromSpace.get(i)+"");
       }
    }
   
    
    /**
     * Returns a list with all objects contained in the space with the @param spaceID
     * @param spaceId
     * @return
     */
    private List<DataObject> getAllObjectsFromSpace(String spaceId) {
       List<DataObject> allObjectsFromSpace = null;
       try {
    	   allObjectsFromSpace = dataHandler.retrieveDataObjects(testSpaceID);
       } catch (UnknownEntityException e) {
    	   Log.w("COULD NOT RETRIEVE DATAOBJECTS", e);
       }
       return allObjectsFromSpace;
    }
    
    
    /**
     * Login with an admin account to add the user to the reflection space
     * @param spaceID
     * @param userJID
     * @param context
     */
    private static void registerUserOnReflectionSpace(String spaceID, String userJID, Context context) {
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
    
    
    public void disconnect(){
    	xmppConnection.disconnect();
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
}