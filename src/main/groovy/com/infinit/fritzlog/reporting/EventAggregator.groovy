package com.infinit.fritzlog.reporting

import com.infinit.fritzlog.event.Event
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Contains static methods in order to create reports from an event list
 */
class EventAggregator {

	/**
	 * Aggregation is done on dates in the timezone
	 */
	DateTimeZone dateTimeZone = DateTimeZone.forID("Europe/Berlin")

	/**
	 * Create a list of DailyMacAttendance records based on the list of events.
	 * Note that the list of events must be sorted ascending by timestamp.
	 * @param events list of events from syslog
	 * @return list of DailyMacAttendance objects
	 */
	List<DailyMacAttendance> getDailyMacAttendances(List<Event> events) {
		Map<LocalDate, Map<String, DailyMacAttendance>> dateMacMap = createDateMacMap(events)
		List<DailyMacAttendance> dailyMacAttendances = extractAttendances(dateMacMap)
		return dailyMacAttendances
	}

	private Map<LocalDate, Map<String, DailyMacAttendance>> createDateMacMap(List<Event> events) {
		List<Event> logonOrLogoffEvents = events.findAll { isLogon(it) || isLogoff(it) }
		Map<LocalDate, Map<String, DailyMacAttendance>> dateMacMap = [:]
		logonOrLogoffEvents.each { Event event ->
			LocalDate date = getLocalDate(event.timestamp)
			String mac = getMac(event)
			DailyMacAttendance attendance = getAttendance(dateMacMap, date, mac)
			if (isLogon(event) && (attendance.firstLogOn == null || event.timestamp < attendance.firstLogOn)) {
				attendance.firstLogOn = event.timestamp
			}
			if (isLogoff(event) && (attendance.lastLogOff == null || event.timestamp > attendance.lastLogOff)) {
				attendance.lastLogOff = event.timestamp
			}
		}
		return dateMacMap
	}

	private LocalDate getLocalDate(DateTime dateTime) {
		return dateTime.withZone(dateTimeZone).toLocalDate()
	}

	private Boolean isLogon(Event event) {
		event.message.contains("angemeldet")
	}

	private Boolean isLogoff(Event event) {
		event.message.contains("abgemeldet")
	}

	private String getMac(Event event) {
		String x = "[0-9a-fA-F][0-9a-fA-F]"
		Pattern pattern = Pattern.compile([x, x, x, x, x, x].join(':'))
		Matcher matcher = pattern.matcher(event.message)
		return matcher.find() ? matcher.group() : null
	}

	private DailyMacAttendance getAttendance(Map<LocalDate, Map<String, DailyMacAttendance>> dateMacMap, LocalDate date, String mac) {
		Map<String, DailyMacAttendance> macMap = dateMacMap[date]
		if (macMap == null) {
			macMap = [:]
			dateMacMap[date] = macMap
		}
		DailyMacAttendance dailyMacAttendance = macMap[mac]
		if (dailyMacAttendance == null) {
			dailyMacAttendance = new DailyMacAttendance(date: date, macAddress: mac)
			macMap[mac] = dailyMacAttendance
		}
		return dailyMacAttendance
	}

	private extractAttendances(Map<LocalDate, Map<String, DailyMacAttendance>> dateMacMap) {
		List<DailyMacAttendance> unsortedAttendance = dateMacMap.values()*.values().flatten()
		return unsortedAttendance.sort { x, y -> x.date <=> y.date ?: x.macAddress <=> y.macAddress }
	}


}
