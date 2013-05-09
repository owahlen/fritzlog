package com.infinit.fritzlog.testbase

import org.apache.log4j.Logger
import org.apache.log4j.PropertyConfigurator
import org.junit.BeforeClass

/**
 * Base class for unit tests
 */
class UnitTestBase {

	@BeforeClass
	static void initialize() {
		configureLogger()
	}

	Logger getLog() {
		Logger.getLogger(this.class.getCanonicalName())
	}

	private static void configureLogger() {
		ConfigObject config = new ConfigSlurper().parse(UnitTestBase.getResource('/testLog4j.groovy'))
		PropertyConfigurator.configure(config.toProperties())
	}

}
