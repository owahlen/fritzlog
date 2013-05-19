package com.infinit.fritzlog.reporting

import org.joda.time.DateTime
import org.joda.time.LocalDate
import spock.lang.Specification

/**
 * Spock tests for AttendanceDecorator
 */
class AttendanceDecoratorSpec extends Specification {

	def setup() {
		ConfigObject config = new ConfigSlurper().parse("macInfo { '7C:6D:62:76:12:66' { alias = 'aliasStub'} }")

		// globally mock ConfigSlurper
		GroovySpy(ConfigSlurper, global: true) {
			parse(_) >> config
		}
	}

	def "test decorateDailyMacAttendances"() {
		setup:
		List<DailyMacAttendance> attendances = [
				new DailyMacAttendance(
						date: new LocalDate("2013-05-19"),
						macAddress: "7C:6D:62:76:12:66",
						alias: null,
						firstLogOn: new DateTime("2013-05-19T19:18:37.000+02:00"),
						lastLogOff: new DateTime("2013-05-19T19:28:37.000+02:00"))

		]

		AttendanceDecorator attendanceDecorator = new AttendanceDecorator(null)

		when:
		attendanceDecorator.decorateDailyMacAttendances(attendances)

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

}
