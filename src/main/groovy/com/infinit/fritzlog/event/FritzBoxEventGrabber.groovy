package com.infinit.fritzlog.event

import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.HtmlPage
import com.gargoylesoftware.htmlunit.html.HtmlTableCell
import com.gargoylesoftware.htmlunit.html.HtmlTableRow
import org.apache.http.client.utils.URIBuilder
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter

/**
 * Extract Events from the Fritz!Box System->Events menu
 */
class FritzBoxEventGrabber {

	String sid

	private String host
	private WebClient webClient

	/**
	 * Grab the events from the Fritz!Box event log
	 * @param eventType type of events to be fetched
	 * @return list of Events
	 */
	List<Event> grabEvents(EventType eventType) {
		HtmlPage syslogPage = getSyslogPage(eventType)
		List<Event> events = extractEventsFromSyslogPage(syslogPage)
		events.sort { it.timestamp }
		return events
	}

	/**
	 * Get the content of the syslog page as xml string
	 * @param eventType the type of events that are supposed to be extracted
	 * @return content of the systlog page as XML string
	 */
	private HtmlPage getSyslogPage(EventType eventType) {
		URL syslogUrl = createSyslogUrl(eventType)
		HtmlPage syslogPage = webClient.getPage(syslogUrl)
		return syslogPage
	}

	/**
	 * Create the URL in order to receive the systlog
	 * @param eventType the type of events that are supposed to be extracted
	 * @return URL of the syslog page
	 */
	private URL createSyslogUrl(EventType eventType) {
		URIBuilder uriBuilder = new URIBuilder("http://${host}/system/syslog.lua")
		uriBuilder.addParameter('sid',sid)
		uriBuilder.addParameter('tab',eventType.tab)
		return uriBuilder.build().toURL()
	}

	/**
	 * Populate the list of Event instances from the html page of the Fritz!Box
	 * @param syslogXml the GPathResult representing the syslog page
	 * @return the list of Event instances
	 */
	private List<Event> extractEventsFromSyslogPage(HtmlPage syslogPage) {
		List<HtmlTableRow> rows = (List<HtmlTableRow>) syslogPage.getByXPath("//tr")
		DateTimeFormatter dateTimeFormatter = getFritzBoxDateTimeFormatter()
		return rows.collect { HtmlTableRow row ->
			List<HtmlTableCell> cells = row.getCells()
			String timestampString = cells[0].asText() + " " + cells[1].asText()
			DateTime timestamp = dateTimeFormatter.parseDateTime(timestampString)
			String message = cells[2].asText()
			return new Event(timestamp: timestamp, message: message)
		}
	}

	/**
	 * Get a DateTimeFormatter that is capable of parsing the date time format presented
	 * in the Fritz!Box event log.
	 * @return new DateTimeFormatter instance
	 */
	private DateTimeFormatter getFritzBoxDateTimeFormatter() {
		// According to Hotline this is hardcoded into the box
		DateTimeZone fritzBoxTimeZone = DateTimeZone.forID("Europe/Berlin")
		return DateTimeFormat.forPattern("dd.MM.yy HH:mm:ss").withZone(fritzBoxTimeZone)
	}

}
