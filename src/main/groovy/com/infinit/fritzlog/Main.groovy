package com.infinit.fritzlog

import com.infinit.fritzlog.facade.FritzBoxFacade
import org.apache.log4j.PropertyConfigurator

/**
 * Main class
 */
class Main {

	static main(args) {
		configureLogger()
		FritzBoxFacade adapter = new FritzBoxFacade(host: "192.168.2.1", password: "gomfia")
		System.out << adapter.eventReader
	}

	private static void configureLogger() {
		ConfigObject config = new ConfigSlurper().parse(Main.getResource('/mainLog4j.groovy'))
		PropertyConfigurator.configure(config.toProperties())
	}

}
