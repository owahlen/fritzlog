package com.infinit.fritzlog.facade

import com.gargoylesoftware.htmlunit.WebClient
import com.infinit.fritzlog.authenticator.FritzBoxAuthenticator
import com.infinit.fritzlog.event.Event
import com.infinit.fritzlog.event.EventType
import com.infinit.fritzlog.event.FritzBoxEventGrabber
import com.infinit.fritzlog.reporting.DailyMacAttendance
import com.infinit.fritzlog.reporting.EventAggregator
import org.joda.time.DateTimeConstants
import org.joda.time.Seconds
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.math.RoundingMode
import java.text.NumberFormat

class FritzBoxFacade {

	private String host
	private WebClient webClient
	private FritzBoxAuthenticator fritzBoxAuthenticator
	private String sid = null

	/**
	 * Constructor for a new FritzBoxFacade
	 * @param host hostname of the fritzbox or its IP address
	 * @param password login credential for the Fritz!Box web pages
	 */
	FritzBoxFacade(String host, String password) {
		this.host = host
		webClient = new WebClient()
		webClient.getOptions().setCssEnabled(false)
		fritzBoxAuthenticator = new FritzBoxAuthenticator(host: host, password: password, webClient: webClient)
	}

	/**
	 * Create a list of DailyMacAttendance records based on all available WLAN events in the Fritz!Box
	 * @return list of DailyMacAttendance objects
	 */
	List<DailyMacAttendance> getDailyMacAttendances() {
		List<Event> events = getEvents(EventType.WLAN)
		EventAggregator eventAggregator = new EventAggregator()
		return eventAggregator.getDailyMacAttendances(events)

	}

	/**
	 * Get the list of events stored in the Fritz!Box event log table
	 * @return Reader for the event log table
	 */
	List<Event> getEvents(EventType eventType) {
		FritzBoxEventGrabber fritzBoxEventGrabber = new FritzBoxEventGrabber(host: host, sid: getSid(), webClient: webClient)
		List<Event> events = fritzBoxEventGrabber.grabEvents(eventType)
		webClient.closeAllWindows()
		return events
	}

	/**
	 * Invalidate the access token used to communicate with the Fritz!Box.
	 * This forces re-authentication the next time the box is queried.
	 */
	void invalidateSession() {
		sid == null
	}

	static Reader getAttendanceCsvReader(List<DailyMacAttendance> attendances) {
		DateTimeFormatter dateFormatter = DateTimeFormat.forPattern("dd.MM.yyyy")
		DateTimeFormatter timeFormatter = DateTimeFormat.forPattern("HH:mm:ss")
		NumberFormat nf = NumberFormat.getNumberInstance(Locale.GERMAN)
		StringBuffer stringBuffer = new StringBuffer("Date;MAC;First Seen;Last Seen;Hours Online\n")
		attendances.each { DailyMacAttendance it ->
			List<String> fields = []
			fields << dateFormatter.print(it.date)
			fields << it.macAddress
			if (it.firstLogOn != null) {
				fields << timeFormatter.print(it.firstLogOn)
			} else {
				fields << ""
			}
			if (it.lastLogOff != null) {
				fields << timeFormatter.print(it.lastLogOff)
			} else {
				fields << ""
			}
			if (it.firstLogOn != null && it.lastLogOff != null) {
				fields << nf.format(
						BigDecimal.valueOf(Seconds.secondsBetween(it.firstLogOn, it.lastLogOff).seconds)
								.divide(DateTimeConstants.SECONDS_PER_HOUR, 2, RoundingMode.HALF_DOWN)
				)
			} else {
				fields << ""
			}
			stringBuffer << fields.join(';') << '\n'
		}
		return new StringReader(stringBuffer.toString())
	}

	/**
	 * Getter for the session id (SID) of the Fritz!Box that is used in all requests as authentication token.
	 * The sid is obtained if not yet existing.
	 * @return sid
	 */
	private String getSid() {
		if (sid == null) {
			sid = fritzBoxAuthenticator.getSid()
		}
		return sid
	}

}