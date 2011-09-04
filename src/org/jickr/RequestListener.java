package org.jickr;

import java.util.EventListener;

/**
 * Request listener 
 * @author jbrek
 *
 */
public interface RequestListener extends EventListener {
	public void progressRequest(RequestEvent event);
}
