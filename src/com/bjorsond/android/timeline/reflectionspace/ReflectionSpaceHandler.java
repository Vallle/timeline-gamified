package com.bjorsond.android.timeline.reflectionspace;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.xml.datatype.Duration;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jivesoftware.smack.SmackConfiguration;
import org.json.JSONException;
import org.json.JSONObject;

import com.bjorsond.android.timeline.DashboardActivity;
import com.swarmconnect.SwarmActivity;

import android.content.Context;
import android.util.Log;
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

//	String userName = "admin";
//	String userPassword = "mirror";
	static String userName = "timelinetester";
	static String userPassword = "timetest";
	static String domain = "mirror-server-ntnu";
	static String appID = "TimelineApplication";
	static String serverIP = "129.241.103.122";
	static int port = 5222;
	
	public static void insertToReflectionSpace(Context context){
		SmackConfiguration.setPacketReplyTimeout(10000);
		ConnectionConfigurationBuilder builder = new ConnectionConfigurationBuilder(domain, appID);
		builder.setHost(serverIP);
		builder.setPort(port);
		ConnectionConfiguration connectionConfig = builder.build();
		Log.i("USER+PW", DashboardActivity.getReflectionSpaceUserName()+"+"+DashboardActivity.getReflectionSpacePassword());
		ConnectionHandler connectionHandler = new ConnectionHandler(DashboardActivity.getReflectionSpaceUserName(), DashboardActivity.getReflectionSpacePassword(), connectionConfig);
		
		try{
			connectionHandler.connect();
		} catch(ConnectionStatusException e){
			Log.w("COULDN'T CONNECT", e);
		}
		
		
		SpaceHandler spaceHandler = new SpaceHandler(context, connectionHandler, "offlineDB");
		spaceHandler.setMode(Mode.ONLINE);
		
		List<de.imc.mirror.sdk.Space> spaces = spaceHandler.getAllSpaces();
		for(de.imc.mirror.sdk.Space space : spaces){
			System.out.println(space.getName());
		}
		
		//Following example requests the private space of the current user. If the space doesn't exist, it is created.
		Space myPrivateSpace = spaceHandler.getDefaultSpace();
		if(myPrivateSpace == null){
			try{
				myPrivateSpace = spaceHandler.createDefaultSpace();
			} catch (SpaceManagementException e){
				//failed to create space
				Log.wtf("Failed to create space", e);
			} catch (ConnectionStatusException e){
				//cannot create a space when offline
				Log.e("Space offline", "Cannot create a space when offline");
				Log.w("Space offline", e);
			}
		}
		System.out.print(myPrivateSpace.getMembers().iterator().next().getJID());
		
		
		
		
		//Data handler
		DataHandler dataHandler = new DataHandler(connectionHandler, spaceHandler);
		dataHandler.setMode(Mode.ONLINE);
		
		//Create the data object using the object builder
		DataObjectBuilder dataObjectBuilder = new DataObjectBuilder("foo", "mirror:application:myapp:foo");
		dataObjectBuilder.addElement("bar", "Some Content", false);
		DataObject dataObject = dataObjectBuilder.build();
		
		//Publish the data
		try{
			dataHandler.publishDataObject(dataObject, myPrivateSpace.getId());
		} catch(UnknownEntityException e){
			//space does not exist or is not accessible
			//add proper
		}
	}
	
	
	
	/**
	 * Uses plugin documented at http://www.igniterealtime.org/projects/openfire/plugins/userservice/readme.html
	 */
	public static void createUserOnServer(String username, String password, String name, String email){
		String secretKey = "vXHN3sLx";
		String encodedUsername = null;
		String encodedPassword = null;
		String encodedName = null;
		try {
			encodedUsername = URLEncoder.encode(username, "UTF-8");
			encodedPassword = URLEncoder.encode(password, "UTF-8");
			encodedName = URLEncoder.encode(name, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost("http://"+serverIP+":9090/plugins/userService/userservice?type=add&secret="+secretKey+"&username="
										+encodedUsername+"&password="+encodedPassword+"&name="+encodedName+"&email="+email);

	    try {
	        // Execute HTTP Post Request
	        HttpResponse response = httpclient.execute(httppost);
	        
	        String responseBody = EntityUtils.toString(response.getEntity());
	        Log.i("RESPONSE FROM HTTP REQUEST TO CREATE NEW USER",responseBody);
	        
	    } catch (ClientProtocolException e) {
	        // TODO Auto-generated catch block
	    } catch (IOException e) {
	        // TODO Auto-generated catch block
	    } 
	}
}
