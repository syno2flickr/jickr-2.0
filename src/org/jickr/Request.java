/*
 * CallMap.java
 *
 * Copyright (c) 2006, James G. Driscoll
 *
 * Created on July 21, 2006, 10:07 AM
 *
 * This software is Open Source.  It's under the BSD Software License, and 
 * it is copyright James Driscoll (jgd@jamesgdriscoll.com).  See LICENSE.TXT
 * for details.
 *
 */

package org.jickr;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.swing.event.EventListenerList;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;


/**
 * A class for encapsulating a request to Flickr.  This is an implementation class, not
 * intended for general use.
 * @author Jim Driscoll <a href="mailto:jgd@jamesgdriscoll.com">jgd@jamesgdriscoll.com</a>
 */
class Request {

	/**
	 * Use to signal a GET request.  Used for retrieving data from Flickr.
	 */
	static final int GET = 0;
	/**
	 * Use to signal a POST request. Used for changing data on Flickr.
	 */
	static final int POST = 1;
	/**
	 * Use to signal a FORM request.  Used to send files.
	 */
	static final int FORM = 2;

	private TreeMap<String,Object> parameters = new TreeMap<String,Object>();
	private static boolean signed = false;
	private int type;

	private String base = Flickr.getBase();

	// POST request const
	private static final String lineEnd = "\r\n";
	private static final String twoHyphens = "--";
	private static final String boundary =  "*****";

	/** 
	 * Listeners
	 */
	private EventListenerList listeners = new EventListenerList();

	// Listener methods for request notifications
	public void addRequestListener(RequestListener l){ listeners.add(RequestListener.class, l); }	
	public void removeRequestListener(RequestListener l){ listeners.remove(RequestListener.class, l); }	
	public void fireRequestProgress(File progressFile, long progress, long totalProgress){
		RequestListener[] listenerList = (RequestListener[])listeners.getListeners(RequestListener.class);
		for(RequestListener l : listenerList){
			l.progressRequest(new RequestEvent(this, progressFile, progress, totalProgress));
		}
	}


	/**
	 * Creates a new Authenticated request, type GET.
	 * @param user The user to authenticate the call as.
	 */
	Request() throws FlickrException {
		this(GET);
	}

	/** 
	 * Creates a new instance of request with a custom call base.
	 * Adds authentication from the thread's AuthContext. 
	 * @param type - one of GET or POST
	 */
	Request(int type) throws FlickrException {
		if (type != GET && type != POST) throw new IllegalArgumentException("type not allowed");
		this.type = type;
		parameters.put("api_key",Flickr.getApiKey());
		User user = Auth.getAuthContext();
		if (user != null) parameters.put("auth_token",Auth.getToken(user));
	}

	/**
	 * Create a request with an unusual URL as the base.  Used for authentication.
	 * A shorthand way to generate a signed call.
	 * @param customBase - An unusual URL to use for the request.  Used for Authentication.
	 */
	Request(String customBase) {
		base = customBase;
		parameters.put("api_key",Flickr.getApiKey());
	}

	/** Creates a new instance of request with a custom call base.
	 *  A shorthand way to generate a signed call.
	 * @param type - one of GET or POST
	 * @param customBase - An unusual URL to use for the request. Used to do POST by example.
	 */
	public Request(int type, String customBase) throws FlickrException {
		if (type != GET && type != POST) throw new IllegalArgumentException("type not allowed");
		base = customBase;
		this.type = type;
		parameters.put("api_key",Flickr.getApiKey());
		User user = Auth.getAuthContext();
		if (user != null) parameters.put("auth_token",Auth.getToken(user));
	}

	void setParameter(String parameter, Object value) {
		parameters.put(parameter, value);
	}

	/**
	 * Return a Map of the parameters for this request.  This map is a copy -
	 * to set parameters, use the setParameter method.
	 * @return A copy of all the parameters set for this request.
	 */
	Map<String,Object> getParameters() {
		return new TreeMap<String,Object>(parameters);
	}

	/**
	 * Set whether to sign all requests or not.  Requires shared key.
	 */
	static void sign(boolean sign) {
		signed = sign;
	}

	/**
	 * Get the signature for this request, based on the current list of parameters.
	 * @return signature of this request.
	 */
	private String getSig(String params) {
		String signString = Flickr.getSharedSecret() + params;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			return hexStringFromBytes(md.digest(signString.getBytes()));
		} catch (NoSuchAlgorithmException ex) {
			// There's no fixin' this, so let's just give up
			throw new Error("Missing MD5 Algorithm",ex);
		}
	}

	// Found this algorithm on the web, adapted it to java
	private String hexStringFromBytes(byte[] b) {
		char[] hexChars ={'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
		String hex = "";
		int big;
		int little = 0;
		int i;
		// MSB maps to idx 0
		for (i = 0; i < b.length; i++) {
			big = ((int)b[i] & 0x000000FF) / 16;
			little = ((int)b[i] & 0x000000FF) % 16;
			hex = hex + hexChars[big] + hexChars[little];
		}
		return(hex);
	}

	/**
	 * Get the URL for this request.
	 * @return The URL in string form.
	 */
	String getURL() {
		// The Request URL we'll actually use for the Flickr request
		String requestURL = base;
		if (type == GET) {
			requestURL += "?"+getParams();
		}
		return requestURL;
	}

	private String getParams() {
		String requestParam = "";
		// string to compute the signature
		String paramString = "";
		boolean first = true;
		for (Map.Entry<String, Object> entry : parameters.entrySet()) {

			if (entry.getValue() instanceof String) {
				paramString += entry.getKey() + entry.getValue();
			}

			try {
				if (first) {
					first = false;
				} else {
					requestParam += "&";
				}
				requestParam += URLEncoder.encode(entry.getKey(), "UTF-8")
						+ "="
						+ URLEncoder.encode((String) entry.getValue(), "UTF-8");
			} catch (UnsupportedEncodingException ex) {
				ex.printStackTrace();
				// No Fixing this, really
				throw new Error("Unsupported Encoding Exception", ex);
			}
		}

		// Now compute and add the signature, if appropriate
		if (signed) {
			requestParam += "&api_sig=" + getSig(paramString);
		}
		return requestParam;
	}

	/**
	 * Utility method to send POST params (to upload files by example)
	 * @param os DataOutputStream got by getConnectionOutputStream() method
	 * @param key the param name
	 * @param value the value of param
	 * @throws FlickrException 
	 * @throws UnsupportedEncodingException 
	 * @throws IOException
	 */
	private void sendPOSTParams(OutputStream os, String key, Object value) throws FlickrException, FlickrException, UnsupportedEncodingException, IOException{

		DataOutputStream dos = (DataOutputStream) os;

		// Start line 
		dos.writeBytes(twoHyphens + boundary + lineEnd);

		// String value
		if (value instanceof String) {
			dos.writeBytes("Content-Disposition: form-data; name=\""+
					key+"\""+lineEnd);
			dos.writeBytes(lineEnd);
			dos.writeBytes((String) value);
		}

		// File value
		else if (value instanceof File){

			// Declaration
			int bytesRead, bytesAvailable, bufferSize;
			byte[] buffer;
			int maxBufferSize = 4*1024;

			File f = (File) value;
			FileInputStream fileInputStream = new FileInputStream(f);

			dos.writeBytes("Content-Disposition: form-data; name=\""+
					key+"\";" 
					+" filename=\""+f.getPath()+"\"" + lineEnd);
			dos.writeBytes("Content-Type: "+MimeType.getMimeType(f.getPath())+lineEnd);
			dos.writeBytes(lineEnd);

			// create a buffer of maximum size
			long bytesReaded=0;
			long fileSize = f.length();
			bytesAvailable = fileInputStream.available();
			bufferSize = Math.min(bytesAvailable, maxBufferSize);
			buffer = new byte[bufferSize];

			// read file and write it into form...
			bytesRead = fileInputStream.read(buffer, 0, bufferSize);
			fireRequestProgress(f, bytesReaded, fileSize);

			while (bytesRead > 0)
			{
				dos.write(buffer, 0, bufferSize);
				bytesAvailable = fileInputStream.available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				bytesRead = fileInputStream.read(buffer, 0, bufferSize);
				bytesReaded += bytesRead;
				fireRequestProgress(f, bytesReaded, fileSize);
			}

			// Close the FileInputStream
			fileInputStream.close();
			fireRequestProgress(f, fileSize, fileSize);

		} else throw new FlickrException("Param value class type not supported: "+value.getClass().getName());


		// Final end line
		dos.writeBytes(lineEnd);

	}

	private InputStream getConnectionResponse() throws IOException {
		String urlString = getURL();
		URL url = new URL(urlString);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		InputStream in = null;
		switch (type) {
		case GET:
			con.setRequestMethod("GET");
			con.setDoOutput(false);
			con.setDoInput(true);
			con.connect();
			in = con.getInputStream();
			break;
		case POST:
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			con.setDoInput(true);
			con.connect();
			OutputStream out = con.getOutputStream();
			String param = getParams();
			byte[] buff = param.getBytes("UTF8");
			out.write(buff);
			out.flush();
			out.close();
			in = con.getInputStream();
			break;
		}

		return in;
	}

	/**
	 * Establish POST HTTP connection and return the connection
	 * @param urlString URL of Flickr services (see Flickr class)
	 * @return the URLConnection ready to stream
	 * @throws IOException
	 */
	private URLConnection getURLConnectionPOST(String urlString) throws IOException{

		// Declaration / instantiation
		URL url = new URL(urlString);
		HttpURLConnection urlConn;
		DataOutputStream dos = null;

		// Open New URL connection channel.
		urlConn = (HttpURLConnection) url.openConnection();
		urlConn.setDoInput (true);
		urlConn.setDoOutput (true);

		// We want no caching
		urlConn.setUseCaches (false);		  
		urlConn.setRequestMethod("POST");		 
		urlConn.setRequestProperty("Connection", "Keep-Alive");		    
		urlConn.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);

		return urlConn;
	}

	/**
	 * Utility method to do a POST request and get a JDOM Document corresponding to
	 * the Flickr Response. Used to upload or replace photos.
	 * 
	 * @param urlString URL of Flickr service (see Flickr class)
	 * @return doc JDOM Document returned by Flickr
	 * @throws FlickrException In the event of any error.
	 */
	Document postAndGetResponse() throws FlickrException{

		SAXBuilder sb = new SAXBuilder();
		XMLOutputter out = new XMLOutputter();
		Document doc;
		Element root;
		InputStream in = null;
		DataOutputStream dos = null;
		HttpURLConnection urlConn=null;

		try {
			// Get HTTP connection
			urlConn = (HttpURLConnection) getURLConnectionPOST(base);

			// Generate signature
			String paramString = "";
			for (Map.Entry<String, Object> entry : parameters.entrySet()) {		    	
				// Prepare signature
				if (entry.getValue() instanceof String) {
					paramString += entry.getKey() + entry.getValue();
				}			
			}

			// Signature to parameters
			parameters.put("api_sig", getSig(paramString));

			// Precompute POST size
			int sizePOST = precomputePOSTSize(parameters);

			// Fixed POST Size
			urlConn.setFixedLengthStreamingMode(sizePOST);

			// Instantiate and get the connection DataOutputStream
			dos = new DataOutputStream( urlConn.getOutputStream() );

			// Send pairs paramName/value
			for (Map.Entry<String, Object> entry : parameters.entrySet()) {
				// Send data
				sendPOSTParams(dos, entry.getKey(), entry.getValue());			
			}		    	

			// End transmission
			dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

			// Close the DataOutputStrem
			dos.flush();
			dos.close();

			// Get server response
			if(urlConn.getResponseCode()>400){
				in = urlConn.getErrorStream();
				throw new FlickrException("HTTP Error: "+urlConn.getResponseCode()+"\n"+urlConn.getResponseMessage(), -999);
			} else
				in = urlConn.getInputStream();
			doc = sb.build(in);
			root = doc.getRootElement();

			// Check result
			verifyResponse (root);

			return doc;
		} catch (IOException ex) {
			throw new FlickrException("IO Error: "+ex.getMessage(),ex);
		} catch (JDOMException ex) {
			throw new FlickrException("Parse Error: "+ex.getMessage(),ex);
		} finally {
			try {
				in.close();
			} catch (Exception ex) {
				// And silently ignore, since we're just trying to see if it works.  
				// If it doesn't work, that's good too.
				ex.printStackTrace();
			}
		}

	}

	/**
	 * Utility method to get a JDOM Document corresponding to the Flickr Response.
	 * Screens for errors, throws FlickrExceptions if any failures.  Attempts to
	 * set return codes in the FlickrException in the event of errors.  Notably,
	 * "Not Found" errors are return code 1.   See Flickr documentation for a complete list
	 *  of error codes.  Note:  End users usually won't need
	 * to use this method, but may wish to in order to get functionality not provided
	 * by the API.
	 *
	 * @return doc JDOM Document returned by Flickr
	 * @throws FlickrException In the event of any error.
	 */
	Document getResponse() throws FlickrException {

		SAXBuilder sb = new SAXBuilder();
		XMLOutputter out = new XMLOutputter();
		Document doc;
		Element root;
		InputStream in = null;

		try {

			in = getConnectionResponse();
			doc = sb.build(in);
			root = doc.getRootElement();

			// Check result
			verifyResponse (root);

			return doc;
		} catch (IOException ex) {
			throw new FlickrException("IO Error: "+ex.getMessage(),ex);
		} catch (JDOMException ex) {
			throw new FlickrException("Parse Error: "+ex.getMessage(),ex);
		} finally {
			try {
				in.close();
			} catch (Exception ex) {
				// And silently ignore, since we're just trying to see if it works.  
				// If it doesn't work, that's good too.
			}
		}
	}

	/**
	 * Verify the response returned after a Flickr request call. If an error is returned
	 * the method throws a FlickrException.

	 * @param rootResponse root response of Flickr request
	 * @throws FlickrException throw FlickrException if an error is returned from Response
	 */
	private void verifyResponse(Element rootResponse) throws FlickrException{

		if (!rootResponse.getAttributeValue("stat").equals("ok")) {
			Element err = rootResponse.getChild("err");
			String codeString = err.getAttributeValue("code");
			int code = 0;
			if (codeString != null) {
				try {
					code = Integer.parseInt(codeString);
				} catch (NumberFormatException nfe) {
					// I'm stumped, we'll have to carry on
					Logger.global.severe("Warning: Unexpected Return Code Returned, continuing: "+codeString);
				}
			}
			FlickrException fe = new FlickrException("Error: "+err.getAttributeValue("msg")
					+" (Code: "+err.getAttributeValue("code")+")",code);
			throw fe;
		}
	}

	/**
	 * Precompute request POST size for {@link HttpURLConnection#setFixedLengthStreamingMode(int)}
	 * Fixed a bug with nginx server 1.2.0 of Flickr whether use {@link HttpURLConnection#setChunkedStreamingMode(int)}...
	 */
	private int precomputePOSTSize(Map<String, Object> parameters){
		int size = 0;
		StringBuilder header = new StringBuilder();
		for (Map.Entry<String, Object> entry : parameters.entrySet()) {
			header.append(twoHyphens + boundary + lineEnd); // Start line

			// String param
			if(entry.getValue() instanceof String){
				header.append("Content-Disposition: form-data; name=\""+entry.getKey()+"\""+lineEnd);
				header.append(lineEnd);
				header.append((String) entry.getValue());
			} else if (entry.getValue() instanceof File){
				File f = (File) entry.getValue();
				header.append("Content-Disposition: form-data; name=\""+entry.getKey()+"\";"+" filename=\""+f.getPath()+"\"" + lineEnd);
				try {
					header.append("Content-Type: "+MimeType.getMimeType(f.getPath())+lineEnd);
				} catch (IOException e) {}
				header.append(lineEnd);
				size += f.length();
			}
			size += lineEnd.length();
		}    	
		size += header.toString().length();
		String endLine = twoHyphens + boundary + twoHyphens + lineEnd;
		size += endLine.length();
		return size;
	}

	public String toString() {
		return getURL();
	}
}
