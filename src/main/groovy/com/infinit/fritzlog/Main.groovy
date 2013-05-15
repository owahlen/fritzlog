package com.infinit.fritzlog

import com.infinit.fritzlog.facade.FritzBoxFacade
import com.infinit.fritzlog.reporting.DailyMacAttendance
import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.apache.log4j.PropertyConfigurator

/**
 * Main class
 */
class Main {

	/**
	 * main method
	 * @param args
	 */
	static void main(String[] args) {

		configureLogger()

		CliOptions opts = new CliOptions()
		if (!processAndExtractCommandLineOptions(args, opts)) {
			return
		}

		FritzBoxFacade facade = new FritzBoxFacade(opts.server, opts.password)
		if (opts.macInfo) {
			facade.setMacInfo(opts.macInfo.toURI().toURL())
		}

		List<DailyMacAttendance> dailyMacAttendances = facade.getDailyMacAttendances()

		Reader reader = facade.getAttendanceCsvReader(dailyMacAttendances)
		System.out << reader
	}

	private static Boolean processAndExtractCommandLineOptions(String[] args, CliOptions options) {

		CliBuilder cli = new CliBuilder(usage: 'fritzlog -h | -s <server> -p <password> [-i <macinfo>] [-d]', posix: false)
		cli.s(longOpt: 'server', args: 1, argName: 'server', 'IP address or hostname of Fritz!Box to connect to')
		cli.p(longOpt: 'password', args: 1, argName: 'password', 'password of web interface')
		cli.i(longOpt: 'macinfo', args: 1, argName: 'macinfo', 'path to a mac info file for MAC lookup')
		cli.d(longOpt: 'debug', 'enable debugging')
		cli.h(longOpt: 'help', 'usage information')
		OptionAccessor opt = cli.parse(args)

		// Option processing phase 1
		if (!opt || opt.h) {
			cli.usage()
			return false
		}
		List<String> illegalArguments = opt.arguments()
		if (illegalArguments) {
			println "error: Illegal arguments: " + illegalArguments.join(",")
			cli.usage()
			return false
		}

		// Option processing phase 2
		List<String> errors = []
		if (opt.d) {
			Logger.getLogger('org.apache.http.wire').setLevel(Level.DEBUG)
			// Logger.getRootLogger().setLevel(Level.DEBUG)
		}
		if (opt.i) {
			String macInfoFileName = opt.i
			options.macInfo = new File(macInfoFileName)
			if (!options.macInfo.exists()) {
				errors << "error: The file $macInfoFileName could not be found."
			}
		}
		if (opt.s) {
			options.server = opt.s
		} else {
			errors << "error: Missing option: -s"
		}
		if (opt.p) {
			options.password = opt.p
		} else {
			errors << "error: Missing option -p"
		}
		if (errors) {
			errors.each {
				println it
			}
			cli.usage()
			return false
		}
		return true
	}

	/**
	 * Configure logger based on the log4j groovy file in resources directory
	 */
	private static void configureLogger() {
		ConfigObject config = new ConfigSlurper().parse(Main.getResource('/mainLog4j.groovy'))
		PropertyConfigurator.configure(config.toProperties())
	}

	private static class CliOptions {
		String server
		String password
		File macInfo
	}

}
