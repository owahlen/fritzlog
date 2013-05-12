package com.infinit.fritzlog.reporting

import org.joda.time.DateTime
import org.joda.time.LocalDate

/**
 * Contains information when a MAC address was first and last seen on a day
 */
class DailyMacAttendance {

	// Date without time the attendance information is associated with
	LocalDate date

	// MAC address of the device being referenced
	String macAddress

	// a more meaningful alias for the MAC address
	String alias

	// first time the device has been seen on the date
	DateTime firstLogOn

	// last time the device has been seen on the date
	DateTime lastLogOff
}
