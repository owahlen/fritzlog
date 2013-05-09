package com.infinit.fritzlog.adapter

import com.infinit.fritzlog.authenticator.FritzBoxAuthenticator

class FritzBoxAdapter {

	private String host
	private String password

	/**
	 * Obtain a reader for the events in the Fritz Box event log table
	 * @return Reader for the event log table
	 */
	Reader getEventReader() {
		FritzBoxAuthenticator fritzBoxAuthenticator = new FritzBoxAuthenticator(host: host, password: password)
		String sid = fritzBoxAuthenticator.getSid()
		return new StringReader("SID: " + sid)
	}

}