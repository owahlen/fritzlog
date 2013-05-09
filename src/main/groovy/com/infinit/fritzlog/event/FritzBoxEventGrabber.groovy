package com.infinit.fritzlog.event

import groovy.util.slurpersupport.GPathResult
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import org.apache.http.HttpResponse
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

/**
 * Extract Events from the Fritz!Box System->Events menu
 */
class FritzBoxEventGrabber {
	private HTTPBuilder httpBuilder
	private String sid

	/**
	 * Setter for the Fritz Box domain name or IP address
	 * @param fritzHost domain or IP address
	 */
	void setHost(String fritzHost) {
		String url = "http://${fritzHost}/system/syslog.lua"
		httpBuilder = new HTTPBuilder(url)
	}

	List<Event> grabEvents(EventType eventType) {
		GPathResult syslogPage = getSyslogPage(eventType)
		List<Event> events = extractEventsFromSyslogPage(syslogPage)
		events.sort { it.timestamp }
		return events
	}

	/**
	 * Get the relevant web page from the event log.
	 * @param challengeResponse to be sent to the Fritz Box
	 * @return session id (sid)
	 */
	private GPathResult getSyslogPage(EventType eventType) {
		GPathResult syslogPage = null
		httpBuilder.request(Method.GET, ContentType.HTML) {
			uri.query = ['sid': sid, 'tab': eventType.tab]

			response.success = { HttpResponse resp, GPathResult html ->
				syslogPage = html
			}
		}
		return syslogPage
	}

	/**
	 * Populate the list of Event instances from the html page of the Fritz!Box
	 * @param syslogPage the GPathResult representing the syslog page
	 * @return the list of Event instances
	 */
	private List<Event> extractEventsFromSyslogPage(GPathResult syslogPage) {
		GPathResult contentDiv = (GPathResult) syslogPage.depthFirst().find { it.name() == 'DIV' && it.@id == 'page_content' }
		List<GPathResult> rows = contentDiv.depthFirst().findAll { it.name() == 'TR' }
		DateTimeFormatter dateTimeFormatter = getFritzBoxDateTimeFormatter()
		return rows.collect { GPathResult row ->
			String timestampString = row.TD[0].text() + " " + row.TD[1].text()
			DateTime timestamp = dateTimeFormatter.parseDateTime(timestampString)
			String message = row.TD[2].text()
			return new Event(timestamp: timestamp, message: message)
		}
	}

	private DateTimeFormatter getFritzBoxDateTimeFormatter() {
		// According to Hotline this is hardcoded into the box
		DateTimeZone fritzBoxTimeZone = DateTimeZone.forID("Europe/Berlin")
		return DateTimeFormat.forPattern("dd.MM.yy HH:mm:ss").withZone(fritzBoxTimeZone)
	}

}
