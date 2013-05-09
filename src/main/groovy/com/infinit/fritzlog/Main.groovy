package com.infinit.fritzlog

import com.infinit.fritzlog.adapter.FritzBoxAdapter
import org.apache.log4j.PropertyConfigurator

/**
 * Main class
 */
class Main {

	static main(args) {
		configureLogger()
		FritzBoxAdapter adapter = new FritzBoxAdapter(host: "192.168.2.1", password: "gomfia")
		System.out << adapter.eventReader
	}

	private static void configureLogger() {
		ConfigObject config = new ConfigSlurper().parse(Main.getResource('/mainLog4j.groovy'))
		PropertyConfigurator.configure(config.toProperties())
	}

}
