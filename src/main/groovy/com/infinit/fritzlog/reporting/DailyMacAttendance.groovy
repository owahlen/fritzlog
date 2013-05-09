package com.infinit.fritzlog.reporting

import org.joda.time.DateTime

/**
 * Contains information when a MAC address was first and last seen on a day
 */
class DailyMacAttendance {
	String macAddress
	DateTime day
	DateTime firstLogOn
	DateTime lastLogOff
}
