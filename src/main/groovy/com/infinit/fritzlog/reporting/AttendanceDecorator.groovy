package com.infinit.fritzlog.reporting

/**
 * Functionality to further decorate DailyMacAttendance objects
 */
class AttendanceDecorator {

	ConfigObject macInfoConfig

	AttendanceDecorator(URL macInfoUrl) {
		macInfoConfig = new ConfigSlurper().parse(macInfoUrl)
	}

	void decorateDailyMacAttendances(List<DailyMacAttendance> dailyMacAttendances) {
		dailyMacAttendances.each { DailyMacAttendance attendance ->
			ConfigObject macConfig = macInfoConfig.macInfo[attendance.macAddress]
			attendance.alias = macConfig ? macConfig.alias : null
		}
	}

}
