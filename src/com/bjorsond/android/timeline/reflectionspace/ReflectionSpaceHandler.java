package com.bjorsond.android.timeline.reflectionspace;

import javax.xml.datatype.Duration;

import com.swarmconnect.SwarmActivity;

import android.content.Context;
import de.imc.mirror.sdk.DataObject;
import de.imc.mirror.sdk.DataObjectListener;
import de.imc.mirror.sdk.OfflineModeHandler.Mode;
import de.imc.mirror.sdk.Space.PersistenceType;
import de.imc.mirror.sdk.android.ConnectionConfiguration;
import de.imc.mirror.sdk.android.ConnectionConfigurationBuilder;
import de.imc.mirror.sdk.android.ConnectionHandler;
import de.imc.mirror.sdk.android.DataHandler;
import de.imc.mirror.sdk.android.Space;
import de.imc.mirror.sdk.android.SpaceConfiguration;
import de.imc.mirror.sdk.android.SpaceHandler;
import de.imc.mirror.sdk.android.SpaceMember;
import de.imc.mirror.sdk.exceptions.ConnectionStatusException;
import de.imc.mirror.sdk.exceptions.SpaceManagementException;
import de.imc.mirror.sdk.exceptions.UnknownEntityException;

public class ReflectionSpaceHandler extends SwarmActivity{

	
	public static void insertToReflectionSpace(Context context){

//		String userName = "admin";
//		String userPassword = "mirror";
		String userName = "timelinetester";
		String userPassword = "timetest";
		String domain = "mirror-server-ntnu";
		String appID = "TimelineApplication";
		String serverIP = "129.241.103.122";
		int port = 5222;
		ConnectionConfigurationBuilder builder = new ConnectionConfigurationBuilder(domain, appID);
		builder.setHost(serverIP);
		builder.setPort(port);
		ConnectionConfiguration connectionConfig = builder.build();
		ConnectionHandler connectionHandler = new ConnectionHandler(userName, userPassword, connectionConfig);
		
		try{
			connectionHandler.connect();
		} catch(ConnectionStatusException e){
			// add proper exception handling
		}
		
		
		SpaceHandler spaceHandler = new SpaceHandler(context, connectionHandler, "offlineDB");
		spaceHandler.setMode(Mode.ONLINE);
		
		
		//Following example requests the private space of the current user. If the space doesn't exist, it is created.
		Space myPrivateSpace = spaceHandler.getDefaultSpace();
		if(myPrivateSpace == null){
			try{
				myPrivateSpace = spaceHandler.createDefaultSpace();
			} catch (SpaceManagementException e){
				//failed to create space
				//add proper exception handling
			} catch (ConnectionStatusException e){
				//cannot create a space when offline
				//add proper exception handling
			}
		}
		System.out.print(myPrivateSpace.getMembers().iterator().next().getJID());
		

		
		
		//Data handler
		DataHandler dataHandler = new DataHandler(connectionHandler, spaceHandler);
		dataHandler.setMode(Mode.ONLINE);
		
		//Configure datahandler to listen to space we just created
//		try {
//			dataHandler.registerSpace(myNewTeamSpace.getId());
//		} catch (UnknownEntityException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		DataObjectListener myListener = new DataObjectListener() {
			//implement this interface in a controller class of your application
			public void handleDataObject(DataObject dataObject, String spaceId) {
				// TODO Auto-generated method stub
				String objectID = dataObject.getId();
				System.out.println("Received object " + objectID + " from space " + spaceId);
			}
		};
		dataHandler.addDataObjectListener(myListener);
	}
	
	
	/**
	 * Uses plugin documentet at http://www.igniterealtime.org/projects/openfire/plugins/userservice/readme.html
	 */
	public void createUserOnServer(){
		//get user & pw from textfields
		//send httprequest to create user
		//connect to server for posting ref note with user and pw
		//handle creation of new users and login
	}
}
