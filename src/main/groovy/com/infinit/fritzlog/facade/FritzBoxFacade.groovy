package com.infinit.fritzlog.facade

import com.infinit.fritzlog.authenticator.FritzBoxAuthenticator
import com.infinit.fritzlog.event.EventType
import com.infinit.fritzlog.event.FritzBoxEventGrabber

class FritzBoxFacade {

	private String host
	private String password

	/**
	 * Obtain a reader for the events in the Fritz Box event log table
	 * @return Reader for the event log table
	 */
	Reader getEventReader() {
		FritzBoxAuthenticator fritzBoxAuthenticator = new FritzBoxAuthenticator(host: host, password: password)
		String sid = fritzBoxAuthenticator.getSid()
		FritzBoxEventGrabber fritzBoxEventGrabber = new FritzBoxEventGrabber(host: host, sid: sid)
		fritzBoxEventGrabber.grabEvents(EventType.WLAN)
		return new StringReader("SID: " + sid)
	}

}