package com.infinit.fritzlog.event

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException
import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.HtmlForm
import com.gargoylesoftware.htmlunit.html.HtmlPage
import com.gargoylesoftware.htmlunit.html.HtmlTableDataCell
import com.gargoylesoftware.htmlunit.html.HtmlTableRow
import com.infinit.fritzlog.exceptions.EventGrabberException
import org.joda.time.DateTime
import spock.lang.Specification

/**
 * Spock tests for FritzBoxEventGrabber
 */
class FritzBoxEventGrabberSpec extends Specification {

	String host = "hostStub"
	String sid = "1b72561b0dff15b3"

	FritzBoxEventGrabber fritzBoxEventGrabber

	def "grabEvents from syslog page"() {

		setup:
		// HtmlTableRows
		List<HtmlTableRow> rows = [
		        Mock(HtmlTableRow) {
			        1 * getCells() >> [
					        Mock(HtmlTableDataCell) { 1 * asText() >> "19.05.13" },
					        Mock(HtmlTableDataCell) { 1 * asText() >> "19:18:37" },
					        Mock(HtmlTableDataCell) { 1 * asText() >> "WLAN-Gerät angemeldet. Geschwindigkeit 130 Mbit/s. MAC-Adresse: 7C:6D:62:76:12:66."}
			        ]
		        },
				Mock(HtmlTableRow) {
					1 * getCells() >> [
							Mock(HtmlTableDataCell) { 1 * asText() >> "19.05.13" },
							Mock(HtmlTableDataCell) { 1 * asText() >> "19:28:37" },
							Mock(HtmlTableDataCell) { 1 * asText() >> "WLAN-Gerät hat sich abgemeldet. MAC-Adresse: 7C:6D:62:76:12:66."}
					]
				}
		]

		// syslog page
		HtmlPage syslogPage = Mock(HtmlPage) {
			1 * getFirstByXPath("//form[@action='/system/syslog.lua']") >> Mock(HtmlForm)
			1 * getByXPath("//tr") >> rows
		}

		// webclient from htmlunit
		WebClient webClient = Mock(WebClient) {
			1 * getPage(new URL("http://${host}/system/syslog.lua?sid=$sid&tab=${EventType.WLAN.tab}")) >> syslogPage
		}

		fritzBoxEventGrabber = new FritzBoxEventGrabber(host: host, sid: sid, webClient: webClient)

		when:
		List<Event> events = fritzBoxEventGrabber.grabEvents(EventType.WLAN)

		then:
		events.size() == 2
		events[0].timestamp.isEqual(new DateTime("2013-05-19T19:18:37.000+02:00"))
		events[0].message == "WLAN-Gerät angemeldet. Geschwindigkeit 130 Mbit/s. MAC-Adresse: 7C:6D:62:76:12:66."
		events[1].timestamp.isEqual(new DateTime("2013-05-19T19:28:37.000+02:00"))
		events[1].message == "WLAN-Gerät hat sich abgemeldet. MAC-Adresse: 7C:6D:62:76:12:66."
	}

	def "grabEvents without sid"() {

		setup:
		fritzBoxEventGrabber = new FritzBoxEventGrabber(host: host, sid: null, webClient: null)

		when:
		fritzBoxEventGrabber.grabEvents(EventType.WLAN)

		then:
		EventGrabberException e = thrown(EventGrabberException)
		e.message == "FritzBoxEventGrabber is not initialized with sid"
	}

	def "grabEvents when Fritz!Box has become unreachable"() {

		setup:
		URL syslogUrl = new URL("http://${host}/system/syslog.lua?sid=$sid&tab=${EventType.WLAN.tab}")

		// webclient from htmlunit
		WebClient webClient = Mock(WebClient) {
			1 * getPage(syslogUrl) >> { throw new IOException() }
		}

		fritzBoxEventGrabber = new FritzBoxEventGrabber(host: host, sid: sid, webClient: webClient)

		when:
		fritzBoxEventGrabber.grabEvents(EventType.WLAN)

		then:
		EventGrabberException e = thrown(EventGrabberException)
		e.message == "Unable to download syslog from url $syslogUrl"
	}

	def "grabEvents when Fritz!Box returns failing http status code"() {

		setup:
		URL syslogUrl = new URL("http://${host}/system/syslog.lua?sid=$sid&tab=${EventType.WLAN.tab}")

		FailingHttpStatusCodeException failingHttpStatusCodeException = Mock(FailingHttpStatusCodeException)

		// webclient from htmlunit
		WebClient webClient = Mock(WebClient) {
			1 * getPage(syslogUrl) >> { throw failingHttpStatusCodeException }
		}

		fritzBoxEventGrabber = new FritzBoxEventGrabber(host: host, sid: sid, webClient: webClient)

		when:
		fritzBoxEventGrabber.grabEvents(EventType.WLAN)

		then:
		EventGrabberException e = thrown(EventGrabberException)
		e.message == "Unable to download syslog from url $syslogUrl"
	}

	def "grabEvents when syslog page does not contain expected syslog form"() {

		setup:
		// syslog page
		HtmlPage syslogPage = Mock(HtmlPage) {
			1 * getFirstByXPath("//form[@action='/system/syslog.lua']") >> null
		}

		// webclient from htmlunit
		WebClient webClient = Mock(WebClient) {
			1 * getPage(new URL("http://${host}/system/syslog.lua?sid=$sid&tab=${EventType.WLAN.tab}")) >> syslogPage
		}

		fritzBoxEventGrabber = new FritzBoxEventGrabber(host: host, sid: sid, webClient: webClient)

		when:
		fritzBoxEventGrabber.grabEvents(EventType.WLAN)

		then:
		EventGrabberException e = thrown(EventGrabberException)
		e.message == "Validation of syslog page failed"
	}

	def "grabEvents when syslog table contains incorrect number of columns"() {

		setup:
		// HtmlTableRows
		List<HtmlTableRow> rows = [
				Mock(HtmlTableRow) {
					1 * getCells() >> [
							Mock(HtmlTableDataCell) { 0 * asText() >> "19.05.13" },
							Mock(HtmlTableDataCell) { 0 * asText() >> "19:18:37" }
					]
				},
				Mock(HtmlTableRow) {
					0 * getCells() >> [
							Mock(HtmlTableDataCell) { 0 * asText() >> "19.05.13" },
							Mock(HtmlTableDataCell) { 0 * asText() >> "19:28:37" }
					]
				}
		]

		// syslog page
		HtmlPage syslogPage = Mock(HtmlPage) {
			1 * getFirstByXPath("//form[@action='/system/syslog.lua']") >> Mock(HtmlForm)
			1 * getByXPath("//tr") >> rows
		}

		// webclient from htmlunit
		WebClient webClient = Mock(WebClient) {
			1 * getPage(new URL("http://${host}/system/syslog.lua?sid=$sid&tab=${EventType.WLAN.tab}")) >> syslogPage
		}

		fritzBoxEventGrabber = new FritzBoxEventGrabber(host: host, sid: sid, webClient: webClient)

		when:
		fritzBoxEventGrabber.grabEvents(EventType.WLAN)

		then:
		EventGrabberException e = thrown(EventGrabberException)
		e.message.startsWith("Unexpected number of columns in syslog table.")
	}

	def "grabEvents when syslog table contains unparsable date"() {

		setup:
		// HtmlTableRows
		List<HtmlTableRow> rows = [
				Mock(HtmlTableRow) {
					1 * getCells() >> [
							Mock(HtmlTableDataCell) { 1 * asText() >> "05/19/13" },
							Mock(HtmlTableDataCell) { 1 * asText() >> "19:18:37" },
							Mock(HtmlTableDataCell) { 0 * asText() >> "WLAN-Gerät angemeldet. Geschwindigkeit 130 Mbit/s. MAC-Adresse: 7C:6D:62:76:12:66."}
					]
				},
				Mock(HtmlTableRow) {
					0 * getCells() >> [
							Mock(HtmlTableDataCell) { 0 * asText() >> "05/19/13" },
							Mock(HtmlTableDataCell) { 0 * asText() >> "19:28:37" },
							Mock(HtmlTableDataCell) { 0 * asText() >> "WLAN-Gerät hat sich abgemeldet. MAC-Adresse: 7C:6D:62:76:12:66."}
					]
				}
		]

		// syslog page
		HtmlPage syslogPage = Mock(HtmlPage) {
			1 * getFirstByXPath("//form[@action='/system/syslog.lua']") >> Mock(HtmlForm)
			1 * getByXPath("//tr") >> rows
		}

		// webclient from htmlunit
		WebClient webClient = Mock(WebClient) {
			1 * getPage(new URL("http://${host}/system/syslog.lua?sid=$sid&tab=${EventType.WLAN.tab}")) >> syslogPage
		}

		fritzBoxEventGrabber = new FritzBoxEventGrabber(host: host, sid: sid, webClient: webClient)

		when:
		fritzBoxEventGrabber.grabEvents(EventType.WLAN)

		then:
		EventGrabberException e = thrown(EventGrabberException)
		e.message.startsWith("Unable to parse date and time in syslog")
	}

}
