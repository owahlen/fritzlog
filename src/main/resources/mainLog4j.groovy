// log4j configuration
log4j {
	rootLogger = "ERROR, A1"
	appender.A1 = "org.apache.log4j.ConsoleAppender"
	appender.'A1.layout' = "org.apache.log4j.PatternLayout"
	appender.'A1.layout.ConversionPattern' = "%d [%t] %-5p %c %x - %m%n"

	logger {
		com.infinit.fritzlog = 'WARN'
		org.apache.http.wire = 'DEBUG'
	}

}
