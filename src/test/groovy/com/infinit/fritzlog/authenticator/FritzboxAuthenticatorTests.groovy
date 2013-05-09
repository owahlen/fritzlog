package com.infinit.fritzlog.authenticator

import com.gargoylesoftware.htmlunit.ScriptResult
import com.gargoylesoftware.htmlunit.html.HtmlPage
import com.infinit.fritzlog.testbase.UnitTestBase
import org.junit.Test

import com.gargoylesoftware.htmlunit.WebClient

class FritzboxAuthenticatorTests extends UnitTestBase {

	private final static List<String> PASSWORDS = ['test','^°!"','§$%&','/()=','?ß´`','€+*#','<>¥','≈ç√∫','~µ,;','.:-_','@äöüÄÖÜ01234567','89é¿']
	private final static String CHALLENGE = "ae08039e"

	@Test
	void testMakeDots() {
		log.info("Starting testMakeDots")
		FritzBoxAuthenticator authenticator = new FritzBoxAuthenticator()
		PASSWORDS.each { String password ->
			assert javaScriptExecute("makeDots('$password');") == authenticator.metaClass.invokeMethod(authenticator, 'makeDots', password)
		}
	}

	@Test
	void testComputeResponse() {
		FritzBoxAuthenticator authenticator = new FritzBoxAuthenticator()
		PASSWORDS.each { String password ->
			authenticator.metaClass.setProperty(authenticator, 'password', password)
			assert javaScriptExecute("setResponse('$password','$CHALLENGE');") == authenticator.metaClass.invokeMethod(authenticator, 'computeResponse', CHALLENGE)
		}
	}

	private String javaScriptExecute(String javaScript) {
		URL authenticatorURL = FritzboxAuthenticatorTests.getResource('/com/infinit/fritzlog/authenticator/authenticator.html')
		assert authenticatorURL
		WebClient webClient = new WebClient()
		HtmlPage htmlPage = webClient.getPage(authenticatorURL)
		ScriptResult scriptResult = htmlPage.executeJavaScript(javaScript)
		String result = scriptResult.javaScriptResult
		assert result
		return result
	}
}
