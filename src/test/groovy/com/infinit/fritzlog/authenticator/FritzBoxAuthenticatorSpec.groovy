package com.infinit.fritzlog.authenticator

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException
import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.HtmlButton
import com.gargoylesoftware.htmlunit.html.HtmlPage
import com.gargoylesoftware.htmlunit.html.HtmlParagraph
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput
import com.infinit.fritzlog.exceptions.AuthenticationException
import spock.lang.Specification

/**
 * Spock tests for FritzBoxAuthenticator
 */
class FritzBoxAuthenticatorSpec extends Specification {

	String host = "hostStub"
	String password = "passwordStub"
	String expectedSid = "1b72561b0dff15b3"

	FritzBoxAuthenticator fritzBoxAuthenticator
	String sid

	def "getSid when Fritz!Box is configured correctly and reachable"() {

		setup:
		// page that is loaded after successful login
		HtmlPage homePage = Mock(HtmlPage) {
			1 * getFirstByXPath("//input[@type='password']") >> null
			1 * getUrl() >> new URL("http://${host}/home/home.lua?sid=${expectedSid}")
		}

		// password field on login page
		HtmlPasswordInput passwordInput = Mock(HtmlPasswordInput) {
			1 * setValueAttribute(password)
		}

		// submit button on login page
		HtmlButton submitButton = Mock(HtmlButton) {
			1 * click() >> homePage
		}

		// login page
		HtmlPage loginPage = Mock(HtmlPage) {
			1 * getFirstByXPath("//input[@type='password']") >> passwordInput
			1 * getFirstByXPath("//button[@type='submit']") >> submitButton
		}

		// webclient from htmlunit
		WebClient webClient = Mock(WebClient) {
			1 * getPage("http://${host}/logincheck.lua") >> loginPage
		}

		// Service Under Specification:
		fritzBoxAuthenticator = new FritzBoxAuthenticator(host: host, password: password, webClient: webClient)

		when:
		sid = fritzBoxAuthenticator.getSid()

		then:
		expectedSid == sid
	}

	def "getSid when Fritz!Box password is incorrect"() {

		setup:
		// wrong password
		String wrongPassword = "wrongPasswordStub"

		// password field on re-loaded login page
		HtmlPasswordInput passwordInputOnReloadedLoginPage = Mock(HtmlPasswordInput) {
		}

		// error message on re-loaded login page
		HtmlParagraph errorParagraph = Mock(HtmlParagraph) {
			1 * asText() >> "Anmeldung fehlgeschlagen. Die Anmeldedaten sind falsch oder es fehlt die Berechtigung für diesen Bereich."
		}

		// login page that is re-loaded after incorrect password submission
		HtmlPage reloadedLoginPage = Mock(HtmlPage) {
			1 * getFirstByXPath("//input[@type='password']") >> passwordInputOnReloadedLoginPage
			1 * getFirstByXPath("//p[@class='error_text']") >> errorParagraph
		}

		// password field in login page
		HtmlPasswordInput passwordInput = Mock(HtmlPasswordInput) {
			1 * setValueAttribute(wrongPassword)
		}

		// submit button on login page
		HtmlButton submitButton = Mock(HtmlButton) {
			1 * click() >> reloadedLoginPage
		}

		// login page
		HtmlPage loginPage = Mock(HtmlPage) {
			1 * getFirstByXPath("//input[@type='password']") >> passwordInput
			1 * getFirstByXPath("//button[@type='submit']") >> submitButton
		}

		// webclient from htmlunit
		WebClient webClient = Mock(WebClient) {
			1 * getPage("http://${host}/logincheck.lua") >> loginPage
		}

		// Service Under Specification:
		fritzBoxAuthenticator = new FritzBoxAuthenticator(host: host, password: wrongPassword, webClient: webClient)

		when:
		sid = fritzBoxAuthenticator.getSid()

		then:
		AuthenticationException e = thrown(AuthenticationException)
		e.message == "Login into Fritz!Box failed: Anmeldung fehlgeschlagen. Die Anmeldedaten sind falsch oder es fehlt die Berechtigung für diesen Bereich."
	}

	def "getSid when Fritz!Box is unreachable"() {

		setup:
		String loginUrl = "http://${host}/logincheck.lua"

		// webclient from htmlunit
		WebClient webClient = Mock(WebClient) {
			1 * getPage(loginUrl) >> { throw new IOException() }
		}

		// Service Under Specification:
		fritzBoxAuthenticator = new FritzBoxAuthenticator(host: host, password: password, webClient: webClient)

		when:
		sid = fritzBoxAuthenticator.getSid()

		then:
		AuthenticationException e = thrown(AuthenticationException)
		e.cause instanceof IOException
		e.message == "Unable to login into Fritz!Box using url $loginUrl"
	}

	def "getSid when Server returns failing http status code"() {
		setup:

		String loginUrl = "http://${host}/logincheck.lua"

		FailingHttpStatusCodeException failingHttpStatusCodeException = Mock(FailingHttpStatusCodeException)

		// webclient from htmlunit
		WebClient webClient = Mock(WebClient) {
			1 * getPage(loginUrl) >> { throw failingHttpStatusCodeException }
		}

		// Service Under Specification:
		fritzBoxAuthenticator = new FritzBoxAuthenticator(host: host, password: password, webClient: webClient)

		when:
		sid = fritzBoxAuthenticator.getSid()

		then:
		AuthenticationException e = thrown(AuthenticationException)
		e.cause instanceof FailingHttpStatusCodeException
		e.message == "Unable to login into Fritz!Box using url $loginUrl"
	}

}
