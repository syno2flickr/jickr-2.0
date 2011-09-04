package org.jickr;

import java.net.FileNameMap;
import java.net.URLConnection;

/**
 * TODO: description
 * @author jbrek
 *
 */
public class MimeType {

	public static String getMimeType(String fileUrl) throws java.io.IOException {
		FileNameMap fileNameMap = URLConnection.getFileNameMap();
		String type = fileNameMap.getContentTypeFor(fileUrl);

		return type;
	}

	public static void main(String args[]) throws Exception {
		System.out.println(MimeType
				.getMimeType("file:///Users/jbrek/tmp/P1000944.JPG"));
	}
}
