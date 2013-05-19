package com.infinit.fritzlog.facade

import com.infinit.fritzlog.authenticator.FritzBoxAuthenticator
import com.infinit.fritzlog.event.Event
import com.infinit.fritzlog.event.EventType
import com.infinit.fritzlog.event.FritzBoxEventGrabber
import com.infinit.fritzlog.reporting.AttendanceDecorator
import com.infinit.fritzlog.reporting.DailyMacAttendance
import org.joda.time.DateTime
import org.joda.time.LocalDate
import spock.lang.Specification

/**
 * Spock tests for FritzBoxFacade
 */
class FritzBoxFacadeSpec extends Specification {

	String host = "hostStub"
	String password = "passwordStub"

	def setup() {

		// globally mock FritzBoxAuthenticator
		GroovySpy(FritzBoxAuthenticator, global: true) {
			getSid() >> "1b72561b0dff15b3"
		}

		// globally mock FritzBoxEventGrabber
		GroovySpy(FritzBoxEventGrabber, global: true) {
			grabEvents(EventType.WLAN) >> [
					new Event(timestamp: new DateTime("2013-05-19T19:18:37.000+02:00"), message: "WLAN-Gerät angemeldet. Geschwindigkeit 130 Mbit/s. MAC-Adresse: 7C:6D:62:76:12:66."),
					new Event(timestamp: new DateTime("2013-05-19T19:28:37.000+02:00"), message: "WLAN-Gerät hat sich abgemeldet. MAC-Adresse: 7C:6D:62:76:12:66.")
			]
		}

	}

	def "getEvents with WLAN EventType"() {

		setup:
		FritzBoxFacade facade = new FritzBoxFacade(host, password)

		when:
		List<Event> events = facade.getEvents(EventType.WLAN)

		then:
		events.size() == 2
		events[0].timestamp.isEqual(new DateTime("2013-05-19T19:18:37.000+02:00"))
		events[0].message == "WLAN-Gerät angemeldet. Geschwindigkeit 130 Mbit/s. MAC-Adresse: 7C:6D:62:76:12:66."
		events[1].timestamp.isEqual(new DateTime("2013-05-19T19:28:37.000+02:00"))
		events[1].message == "WLAN-Gerät hat sich abgemeldet. MAC-Adresse: 7C:6D:62:76:12:66."
	}

	def "getDailyMacAttendances without AttendanceDecorator"() {

		setup:
		FritzBoxFacade facade = new FritzBoxFacade(host, password)

		when:
		List<DailyMacAttendance> attendances = facade.getDailyMacAttendances()

		then:
		attendances.size() == 1
		attendances.first().with {
			date == new LocalDate("2013-05-19")
			macAddress == "7C:6D:62:76:12:66"
			alias == null
			firstLogOn == new DateTime("2013-05-19T19:18:37.000+02:00")
			lastLogOff == new DateTime("2013-05-19T19:28:37.000+02:00")
		}
	}

	def "getDailyMacAttendances with AttendanceDecorator"() {

		setup:
		AttendanceDecorator attendanceDecorator = Mock(AttendanceDecorator) {
			decorateDailyMacAttendances(_) >> { ArrayList<DailyMacAttendance> dailyMacAttendances ->
				dailyMacAttendances.each {
					it.alias = "aliasStub"
				}
			}
		}

		FritzBoxFacade facade = new FritzBoxFacade(host, password)
		facade.metaClass.setProperty(facade, "attendanceDecorator", attendanceDecorator)

		when:
		List<DailyMacAttendance> attendances = facade.getDailyMacAttendances()

		then:
		attendances.size() == 1
		attendances.first().with {
			date == new LocalDate("2013-05-19")
			macAddress == "7C:6D:62:76:12:66"
			alias == "aliasStub"
			firstLogOn == new DateTime("2013-05-19T19:18:37.000+02:00")
			lastLogOff == new DateTime("2013-05-19T19:28:37.000+02:00")
		}
	}

	def "getAttendanceCsvReader without AttendanceDecorator"() {

		setup:
		List<DailyMacAttendance> attendances = [
				new DailyMacAttendance(
						date: new LocalDate("2013-05-19"),
						macAddress: "7C:6D:62:76:12:66",
						alias: null,
						firstLogOn: new DateTime("2013-05-19T19:18:37.000+02:00"),
						lastLogOff: new DateTime("2013-05-19T19:28:37.000+02:00"))

		]

		when:
		Reader reader = FritzBoxFacade.getAttendanceCsvReader(attendances)
		List<String> lines = reader.readLines()

		then:
		lines.size() == 2
		lines[0] == "Date;MAC;First Seen;Last Seen;Hours Online"
		lines[1] == "19.05.2013;7C:6D:62:76:12:66;;19:18:37;19:28:37;0,17"
	}

	def "getAttendanceCsvReader with AttendanceDecorator"() {

		setup:
		List<DailyMacAttendance> attendances = [
				new DailyMacAttendance(
						date: new LocalDate("2013-05-19"),
						macAddress: "7C:6D:62:76:12:66",
						alias: "attendanceStub",
						firstLogOn: new DateTime("2013-05-19T19:18:37.000+02:00"),
						lastLogOff: new DateTime("2013-05-19T19:28:37.000+02:00"))

		]

		when:
		Reader reader = FritzBoxFacade.getAttendanceCsvReader(attendances)
		List<String> lines = reader.readLines()

		then:
		lines.size() == 2
		lines[0] == "Date;MAC;First Seen;Last Seen;Hours Online"
		lines[1] == "19.05.2013;7C:6D:62:76:12:66;attendanceStub;19:18:37;19:28:37;0,17"
	}

}
