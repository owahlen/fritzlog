package com.infinit.fritzlog.reporting

import org.joda.time.DateTime
import org.joda.time.LocalDate

/**
 * Contains information when a MAC address was first and last seen on a day
 */
class DailyMacAttendance {
	LocalDate date
	String macAddress
	DateTime firstLogOn
	DateTime lastLogOff
}
