package com.infinit.fritzlog.facade

import com.gargoylesoftware.htmlunit.WebClient
import com.infinit.fritzlog.authenticator.FritzBoxAuthenticator
import com.infinit.fritzlog.event.Event
import com.infinit.fritzlog.event.EventType
import com.infinit.fritzlog.event.FritzBoxEventGrabber
import com.infinit.fritzlog.reporting.AttendanceDecorator
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
	private String password
	private WebClient webClient
	private Integer webClientNestingLevel = 0
	private String sid = null
	private AttendanceDecorator attendanceDecorator

	/**
	 * Constructor for a new FritzBoxFacade
	 * @param host hostname of the Fritz!Box or its IP address
	 * @param password login credential for the Fritz!Box web pages
	 */
	FritzBoxFacade(String host, String password) {
		this.host = host
		this.password = password
	}

	/**
	 * Create a list of DailyMacAttendance records based on all available WLAN events in the Fritz!Box
	 * @return list of DailyMacAttendance objects
	 */
	List<DailyMacAttendance> getDailyMacAttendances() {
		List<Event> events = getEvents(EventType.WLAN)
		EventAggregator eventAggregator = new EventAggregator()
		List<DailyMacAttendance> dailyMacAttendances = eventAggregator.getDailyMacAttendances(events)
		if(attendanceDecorator) {
			attendanceDecorator.decorateDailyMacAttendances(dailyMacAttendances)
		}
		return dailyMacAttendances
	}

	/**
	 * Get the list of events stored in the Fritz!Box event log table
	 * @return List of Event instances representing rows of the event log
	 */
	List<Event> getEvents(EventType eventType) {
		List<Event> events = null
		try {
			openWebClient()
			FritzBoxEventGrabber fritzBoxEventGrabber = new FritzBoxEventGrabber(host: host, sid: getSid(), webClient: webClient)
			events = fritzBoxEventGrabber.grabEvents(eventType)
		} finally {
			closeWebClient()
		}
		return events
	}

	void setMacInfo(URL macInfo) {
		 attendanceDecorator = new AttendanceDecorator(macInfo)
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
			fields << (it.alias?:"")
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
			if (it.firstLogOn != null && it.lastLogOff != null && it.firstLogOn <= it.lastLogOff) {
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
			try {
				openWebClient()
				FritzBoxAuthenticator fritzBoxAuthenticator = new FritzBoxAuthenticator(host: host, password: password, webClient: webClient)
				sid = fritzBoxAuthenticator.getSid()
			} finally {
				closeWebClient()
			}
		}
		return sid
	}

	/**
	 * Create a new WebClient if it does not yet exist and initialize it for communication with the Fritz!Box.
	 * @param webClientOpener handle for the method that tries to open the WebClient
	 */
	private void openWebClient() {
		if (webClientNestingLevel == 0) {
			// there should not be a WebClient in nesting level 0
			assert null == this.webClient
			webClient = new WebClient()
			webClient.getOptions().setCssEnabled(false)
		} else {
			// there should already be a WebClient opened by some outer method call
			assert null != this.webClient
			assert 0 < webClientNestingLevel
		}
		webClientNestingLevel += 1
	}

	/**
	 * Close the webClient that has previously been opened.
	 * @param webClientOpener handle for the method that has tried to open the WebClient before
	 */
	private void closeWebClient() {
		assert null != this.webClient
		assert 0 < this.webClientNestingLevel
		if (webClientNestingLevel == 1) {
			// we are entering nesting level 0 so everything must be closed
			webClient.closeAllWindows()
			webClient = null
			sid == null
		}
		webClientNestingLevel -= 1
	}

}