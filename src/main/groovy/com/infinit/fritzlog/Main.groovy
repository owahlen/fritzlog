package com.infinit.fritzlog

import com.infinit.fritzlog.facade.FritzBoxFacade
import com.infinit.fritzlog.reporting.DailyMacAttendance
import org.apache.log4j.PropertyConfigurator

/**
 * Main class
 */
class Main {

	/**
	 * main method
	 * @param args
	 */
	static main(args) {
		configureLogger()
		FritzBoxFacade facade = new FritzBoxFacade("192.168.2.1", "gomfia")
		List<DailyMacAttendance> dailyMacAttendances = facade.getDailyMacAttendances()
		Reader reader = facade.getAttendanceCsvReader(dailyMacAttendances)
		System.out << reader
	}

	/**
	 * Configure logger based on the log4j groovy file in resources directory
	 */
	private static void configureLogger() {
		ConfigObject config = new ConfigSlurper().parse(Main.getResource('/mainLog4j.groovy'))
		PropertyConfigurator.configure(config.toProperties())
	}

}
