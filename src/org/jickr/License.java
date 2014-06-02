package org.jickr;

public class License {
	/**
	 * Photo licenses of Flickr 
	 * API url: https://secure.flickr.com/services/api/flickr.photos.licenses.getInfo.html
	 */
	public enum LicenseType {
		ALL_RIGHTS_RESERVED(0),
		ATTRIBUTION_NON_COMMERCIAL_SHAREALIKE_LICENSE(1), // http://creativecommons.org/licenses/by-nc-sa/2.0/
		ATTRIBUTION_NON_COMMERCIAL_LICENSE(2),			  // http://creativecommons.org/licenses/by-nc/2.0/
		ATTRIBUTION_NON_COMMERCIAL_NO_DERIVS_LICENSE(3),  // http://creativecommons.org/licenses/by-nc-nd/2.0/
		ATTRIBUTION_LICENSE(4),							  // http://creativecommons.org/licenses/by/2.0/
		ATTRIBUTION_SHAREALIKE_LICENSE(5),				  // http://creativecommons.org/licenses/by-sa/2.0/
		ATTRIBUTION_NO_DERIVS_LICENSE(6),				  // http://creativecommons.org/licenses/by-nd/2.0/
		NO_KNOWN_COPYRIGHT_RESTRICTIONS(7),				  // http://flickr.com/commons/usage/
		UNITED_STATES_GOVERNMANT_WORK(8);				  // http://www.usa.gov/copyright.shtml
		
		private int xmlValue;
		
		private LicenseType(int xmlValue) {
			this.xmlValue = xmlValue;
		}
		
		public int getXmlValue() {
			return xmlValue;
		}
		
		public String getTitle(){
			switch (this) {
			case ALL_RIGHTS_RESERVED:							return "All Rights Reserved";
			case ATTRIBUTION_NON_COMMERCIAL_SHAREALIKE_LICENSE:	return "Attribution-NonCommercial-ShareAlike License";
			case ATTRIBUTION_NON_COMMERCIAL_LICENSE:			return "Attribution-NonCommercial License";
			case ATTRIBUTION_NON_COMMERCIAL_NO_DERIVS_LICENSE: 	return "Attribution-NonCommercial-NoDerivs License";
			case ATTRIBUTION_LICENSE: 							return "Attribution License";
			case ATTRIBUTION_SHAREALIKE_LICENSE: 				return "Attribution-ShareAlike License";
			case ATTRIBUTION_NO_DERIVS_LICENSE: 				return "Attribution-NoDerivs License";
			case NO_KNOWN_COPYRIGHT_RESTRICTIONS: 				return "No known copyright restrictions";
			case UNITED_STATES_GOVERNMANT_WORK: 				return "United States Government Work";
			default: return null;
			}
		}
		
		public static LicenseType getEnumFromValue(int value){
			switch (value) {
			case 0:	return ALL_RIGHTS_RESERVED;
			case 1:	return ATTRIBUTION_NON_COMMERCIAL_SHAREALIKE_LICENSE;
			case 2:	return ATTRIBUTION_NON_COMMERCIAL_LICENSE;
			case 3: return ATTRIBUTION_NON_COMMERCIAL_NO_DERIVS_LICENSE;
			case 4: return ATTRIBUTION_LICENSE;
			case 5: return ATTRIBUTION_SHAREALIKE_LICENSE;
			case 6: return ATTRIBUTION_NO_DERIVS_LICENSE;
			case 7: return NO_KNOWN_COPYRIGHT_RESTRICTIONS;
			case 8: return UNITED_STATES_GOVERNMANT_WORK;
			default: return null;
			}
		}
	}
	
	/**
	 * Update the photo license. If type is null, the licence is removed (All rights reserved).
	 */
	public static void updateLicense(String photoId, LicenseType type) throws FlickrException{
		if (photoId==null) throw new IllegalArgumentException("photoId cannot be null");
		Request req = new Request(Request.POST);
        req.setParameter("method","flickr.photos.licenses.setLicense");
        req.setParameter("photo_id", photoId);
        req.setParameter("license_id",type==null ? String.valueOf(LicenseType.ALL_RIGHTS_RESERVED.getXmlValue()) : String.valueOf(type.getXmlValue()));
        // There's no return except for status
        req.getResponse();
	}
	
	/**
	 * Update the photo license. If type is null, the licence is removed (All rights reserved).
	 */
	public static void updateLicense(Photo photo, LicenseType type) throws FlickrException{
		if (photo==null || photo.getID()==null) throw new IllegalArgumentException("photo cannot be null and must have an ID");
		updateLicense(photo.getID(), type);
	}
}

