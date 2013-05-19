package com.infinit.fritzlog.reporting

import com.infinit.fritzlog.event.Event
import org.joda.time.DateTime
import org.joda.time.LocalDate
import spock.lang.Specification

/**
 * Spock tests for EventAggregator
 */
class EventAggregatorSpec extends Specification {
	def "test getDailyMacAttendances"() {

		setup:
		List<Event> events = [
				new Event(timestamp: new DateTime("2013-05-19T19:18:37.000+02:00"), message: "WLAN-Gerät angemeldet. Geschwindigkeit 130 Mbit/s. MAC-Adresse: 00:00:00:00:00:00."),
				new Event(timestamp: new DateTime("2013-05-19T19:28:37.000+02:00"), message: "WLAN-Gerät hat sich abgemeldet. MAC-Adresse: 00:00:00:00:00:00."),

				new Event(timestamp: new DateTime("2013-05-18T19:18:37.000+02:00"), message: "WLAN-Gerät angemeldet. Geschwindigkeit 130 Mbit/s. MAC-Adresse: 00:00:00:00:00:01."),
				new Event(timestamp: new DateTime("2013-05-19T19:28:37.000+02:00"), message: "WLAN-Gerät hat sich abgemeldet. MAC-Adresse: 00:00:00:00:00:01."),

				new Event(timestamp: new DateTime("2013-05-19T19:28:37.000+02:00"), message: "WLAN-Gerät hat sich abgemeldet. MAC-Adresse: 00:00:00:00:00:02."),
				new Event(timestamp: new DateTime("2013-05-19T19:38:37.000+02:00"), message: "WLAN-Gerät angemeldet. Geschwindigkeit 130 Mbit/s. MAC-Adresse: 00:00:00:00:00:02."),

				new Event(timestamp: new DateTime("2013-05-19T19:08:37.000+02:00"), message: "WLAN-Gerät angemeldet. Geschwindigkeit 130 Mbit/s. MAC-Adresse: 00:00:00:00:00:03."),
				new Event(timestamp: new DateTime("2013-05-19T19:18:37.000+02:00"), message: "WLAN-Gerät angemeldet. Geschwindigkeit 130 Mbit/s. MAC-Adresse: 00:00:00:00:00:03."),
				new Event(timestamp: new DateTime("2013-05-19T19:28:37.000+02:00"), message: "WLAN-Gerät hat sich abgemeldet. MAC-Adresse: 00:00:00:00:00:03."),

				new Event(timestamp: new DateTime("2013-05-19T19:18:37.000+02:00"), message: "WLAN-Gerät angemeldet. Geschwindigkeit 130 Mbit/s. MAC-Adresse: 00:00:00:00:00:04."),
				new Event(timestamp: new DateTime("2013-05-19T19:28:37.000+02:00"), message: "WLAN-Gerät hat sich abgemeldet. MAC-Adresse: 00:00:00:00:00:04."),
				new Event(timestamp: new DateTime("2013-05-19T19:38:37.000+02:00"), message: "WLAN-Gerät hat sich abgemeldet. MAC-Adresse: 00:00:00:00:00:04."),

				new Event(timestamp: new DateTime("2013-05-19T19:18:37.000+02:00"), message: "WLAN-Gerät angemeldet. Geschwindigkeit 130 Mbit/s. MAC-Adresse: 00:00:00:00:00:05."),
				new Event(timestamp: new DateTime("2013-05-19T19:28:37.000+02:00"), message: "WLAN-Gerät hat sich abgemeldet. MAC-Adresse: 00:00:00:00:00:05."),
				new Event(timestamp: new DateTime("2013-05-20T18:18:37.000+02:00"), message: "WLAN-Gerät angemeldet. Geschwindigkeit 130 Mbit/s. MAC-Adresse: 00:00:00:00:00:05."),
				new Event(timestamp: new DateTime("2013-05-20T18:28:37.000+02:00"), message: "WLAN-Gerät hat sich abgemeldet. MAC-Adresse: 00:00:00:00:00:05.")
		]

		EventAggregator eventAggregator = new EventAggregator()

		when:
		List<DailyMacAttendance> attendances = eventAggregator.getDailyMacAttendances(events)

		then:
		attendances.size() == 8
		attendances[0].with {
			date == new LocalDate("2013-05-18")
			macAddress == "00:00:00:00:00:01"
			alias == null
			firstLogOn == new DateTime("2013-05-18T19:18:37.000+02:00")
			lastLogOff == null
		}
		attendances[1].with {
			date == new LocalDate("2013-05-19")
			macAddress == "00:00:00:00:00:00"
			alias == null
			firstLogOn == new DateTime("2013-05-19T19:18:37.000+02:00")
			lastLogOff == new DateTime("2013-05-19T19:28:37.000+02:00")
		}
		attendances[2].with {
			date == new LocalDate("2013-05-19")
			macAddress == "00:00:00:00:00:01"
			alias == null
			firstLogOn == null
			lastLogOff == new DateTime("2013-05-19T19:28:37.000+02:00")
		}
		attendances[3].with {
			date == new LocalDate("2013-05-19")
			macAddress == "00:00:00:00:00:02"
			alias == null
			firstLogOn == new DateTime("2013-05-19T19:38:37.000+02:00")
			lastLogOff == new DateTime("2013-05-19T19:28:37.000+02:00")
		}
		attendances[4].with {
			date == new LocalDate("2013-05-19")
			macAddress == "00:00:00:00:00:03"
			alias == null
			firstLogOn == new DateTime("2013-05-19T19:08:37.000+02:00")
			lastLogOff == new DateTime("2013-05-19T19:28:37.000+02:00")
		}
		attendances[5].with {
			date == new LocalDate("2013-05-19")
			macAddress == "00:00:00:00:00:04"
			alias == null
			firstLogOn == new DateTime("2013-05-19T19:18:37.000+02:00")
			lastLogOff == new DateTime("2013-05-19T19:38:37.000+02:00")
		}
		attendances[6].with {
			date == new LocalDate("2013-05-19")
			macAddress == "00:00:00:00:00:05"
			alias == null
			firstLogOn == new DateTime("2013-05-19T19:18:37.000+02:00")
			lastLogOff == new DateTime("2013-05-19T19:28:37.000+02:00")
		}
		attendances[7].with {
			date == new LocalDate("2013-05-19")
			macAddress == "00:00:00:00:00:05"
			alias == null
			firstLogOn == new DateTime("2013-05-20T18:18:37.000+02:00")
			lastLogOff == new DateTime("2013-05-20T18:28:37.000+02:00")
		}
	}
}
