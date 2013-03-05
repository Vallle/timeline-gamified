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
package com.fabula.android.timeline.sync;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;

import android.util.Log;

import com.fabula.android.timeline.models.BaseEvent;
import com.fabula.android.timeline.models.Experience;
import com.fabula.android.timeline.models.Experiences;
import com.fabula.android.timeline.models.Group;
import com.fabula.android.timeline.models.User;
import com.fabula.android.timeline.utilities.Constants;
import com.fabula.android.timeline.utilities.Utilities;

public class ServerUploader {
	
	protected static void uploadFile(String locationFilename, String saveFilename){
		System.out.println("saving "+locationFilename+"!! ");
		if(!saveFilename.contains("."))
			saveFilename = saveFilename+Utilities.getExtension(locationFilename);
		
		if(!fileExistsOnServer(saveFilename)){
		
			HttpURLConnection connection = null;
			DataOutputStream outputStream = null;
	
			String pathToOurFile = locationFilename;
			String urlServer = "http://folk.ntnu.no/bjornava/upload/upload.php";
			String lineEnd = "\r\n";
			String twoHyphens = "--";
			String boundary =  "*****";
	
			int bytesRead, bytesAvailable, bufferSize;
			byte[] buffer;
			int maxBufferSize = 1*1024*1024*1024;
	
			try
			{
			FileInputStream fileInputStream = new FileInputStream(new File(pathToOurFile) );
	
			URL url = new URL(urlServer);
			connection = (HttpURLConnection) url.openConnection();
	
			// Allow Inputs & Outputs
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);
	
			// Enable POST method
			connection.setRequestMethod("POST");
	
			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);
	
			outputStream = new DataOutputStream( connection.getOutputStream() );
			outputStream.writeBytes(twoHyphens + boundary + lineEnd);
			outputStream.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + saveFilename +"\"" + lineEnd);
			outputStream.writeBytes(lineEnd);
	
			bytesAvailable = fileInputStream.available();
			bufferSize = Math.min(bytesAvailable, maxBufferSize);
			buffer = new byte[bufferSize];
	
			// Read file
			bytesRead = fileInputStream.read(buffer, 0, bufferSize);
	
			while (bytesRead > 0)
			{
			outputStream.write(buffer, 0, bufferSize);
			bytesAvailable = fileInputStream.available();
			bufferSize = Math.min(bytesAvailable, maxBufferSize);
			bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			}
	
			outputStream.writeBytes(lineEnd);
			outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
	
			// Responses from the server (code and message)
			int serverResponseCode = connection.getResponseCode();
			String serverResponseMessage = connection.getResponseMessage();
			
	
			fileInputStream.close();
			outputStream.flush();
			outputStream.close();
			
			System.out.println("Server response: "+serverResponseCode+" Message: "+serverResponseMessage);
			}
			catch (Exception ex)
			{
			//Exception handling
			}
		}
		else{
			System.out.println("image exists on server");
		}
	}
	
	protected static void putToGAE(Object o, String jsonString){       
		// Using PUT here
		HttpPut httpPut = makeHttpPutBasedOnObjectType(o);
		makeJSONHttpRequestContentTypeHeader(httpPut);
		sendJSONTOGAEServer(jsonString, Constants.targetHost, httpPut);
	}

	
	protected static void putGroupToGAE(final String jsonString){  
		// Using PUT here
		final HttpPut httpPut = new HttpPut("/rest/group/");
		makeJSONHttpRequestContentTypeHeader(httpPut);
		sendJSONTOGAEServer(jsonString, Constants.targetHost, httpPut);
	}
	
	protected static void putUserToGroupToGAE(Group groupToAddUser, User userToAddToGroup){  
		// Using PUT here
		final HttpPut httpPut = new HttpPut("/rest/group/"+groupToAddUser.getId()+"/user/"+userToAddToGroup.getUserName()+"/");
		makeJSONHttpRequestContentTypeHeader(httpPut);
		sendJSONTOGAEServer("", Constants.targetHost, httpPut);
	}
	
	protected static void putUserToGAE(final String jsonString){  
		// Using PUT here
		final HttpPut httpPut = new HttpPut("/rest/user/");
		makeJSONHttpRequestContentTypeHeader(httpPut);
		
		sendJSONTOGAEServer(jsonString, Constants.targetHost, httpPut);
		
	}
	
	/**
	 * Sets the uri of the host based on the kind of {@link Object} to persist.
	 * 
	 * @param o The object to persist
	 * @return {@link HttpPut} containing the string uri of the REST service to address.
	 */
	private static HttpPut makeHttpPutBasedOnObjectType(Object o) {
		HttpPut httpPut = null;
		if(o instanceof Experiences)
			httpPut = new HttpPut("/rest/experiences/");
		else if(o instanceof Experience)
			httpPut = new HttpPut("/rest/experience/");
		else if(o instanceof BaseEvent)
			httpPut = new HttpPut("/rest/event/");
		
		httpPut.getParams().setParameter(CoreProtocolPNames.USER_AGENT, "TimelineAndroid");
		return httpPut;
	}
	
	/**
	 * Sets the content header of the HTTP request to send and accept JSON.
	 * 
	 * @param httpRequest The {@link HttpRequest} to add headers.
	 */
	private static void makeJSONHttpRequestContentTypeHeader(HttpRequestBase httpRequest) {
		// Make sure the server knows what kind of a response we will accept
		httpRequest.addHeader("Accept", "application/json");
		// Also be sure to tell the server what kind of content we are sending
		httpRequest.addHeader("Content-Type", "application/json");
//		httpRequest.addHeader(CoreProtocolPNames.USER_AGENT, "TimelineAndroid");
		
	}
	

	/**
	 * Sends a JSON-string to the Google App Engine Server. This runs async in a separate thread.
	 * 
	 * @param jsonString Content of HTTP request, as JSON.
	 * @param targetHost The host of the server
	 * @param httpPut The HTTP PUT request.
	 */
	private static void sendJSONTOGAEServer(final String jsonString,
			final HttpHost targetHost, final HttpPut httpPut) {
		Runnable sendRunnable = new Runnable() {
			
			public void run() {
				try
				{
					DefaultHttpClient httpClient = new DefaultHttpClient();
					
				    StringEntity entity = new StringEntity(jsonString, "UTF-8");
				    httpPut.setEntity(entity);
				   
				        // execute is a blocking call, it's best to call this code in a thread separate from the ui's
				    HttpResponse response = httpClient.execute(targetHost, httpPut);

				    Log.v("Put to GAE", response.getStatusLine().toString());
				}
				catch (Exception ex)
				{
				        ex.printStackTrace();
				}
					}
		};
		
		Thread thread =  new Thread(null, sendRunnable, "putToGAE");
        thread.start();
	
	}
	
	private static boolean fileExistsOnServer(String URLName){
		URL url;
		try {
			url = new URL(URLName);
			URLConnection connection = url.openConnection();

				connection.connect();
			
			// Cast to a HttpURLConnection
			if ( connection instanceof HttpURLConnection)
			{
			   HttpURLConnection httpConnection = (HttpURLConnection) connection;

			   int code = httpConnection.getResponseCode();

			   if(code==200)
				   return true;
			}
			else
			{
			   System.err.println ("error - not a http request!");
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		}

		return false;
	}

	


	
}
