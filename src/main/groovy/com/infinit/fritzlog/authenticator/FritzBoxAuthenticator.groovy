package com.infinit.fritzlog.authenticator

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException
import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.HtmlButton
import com.gargoylesoftware.htmlunit.html.HtmlPage
import com.gargoylesoftware.htmlunit.html.HtmlParagraph
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput
import com.infinit.fritzlog.exceptions.AuthenticationException
import org.apache.http.NameValuePair
import org.apache.http.client.utils.URLEncodedUtils

/**
 * Authentication functionality against an AVM Fritz!Box
 */
class FritzBoxAuthenticator {
	private String host
	private String password
	private WebClient webClient

	/**
	 * Obtain a session id that is used in future request as token
	 * @return sid session id
	 */
	public String getSid() {
		HtmlPage homePage = getHomePage()
		String sid = getSidFromHomePage(homePage)
		return sid
	}

	private HtmlPage getHomePage() {
		String loginUrl = "http://${host}/logincheck.lua"
		HtmlPage loginPage
		try {
			loginPage = webClient.getPage(loginUrl)
		} catch (FailingHttpStatusCodeException | IOException e) {
			throw new AuthenticationException("Unable to login into Fritz!Box using url $loginUrl", e)
		}
		HtmlPasswordInput passwordInput = (HtmlPasswordInput) loginPage.getFirstByXPath("//input[@type='password']")
		if (passwordInput == null) {
			throw new AuthenticationException("Password input field not found in login page $loginUrl")
		}
		HtmlButton submitButton = (HtmlButton) loginPage.getFirstByXPath("//button[@type='submit']")
		if (submitButton == null) {
			throw new AuthenticationException("Submit button not found in login page $loginUrl")
		}
		passwordInput.setValueAttribute(password)
		HtmlPage homePage
		try {
			homePage = submitButton.click()
		} catch (IOException e) {
			throw new AuthenticationException("Unable to push submit button on login page $loginUrl", e)
		}
		validateLoginSuccessful(homePage)
		return homePage
	}

	private static String getSidFromHomePage(HtmlPage homePage) {
		URL homePageUrl = homePage.url
		List<NameValuePair> requestParameters = URLEncodedUtils.parse(homePageUrl.toURI(), "UTF-8")
		String sid = requestParameters.find { it.name == 'sid' }.value
		if (sid == null) {
			throw new AuthenticationException("Unable to extract sid from homepage url: $homePageUrl")
		}
		return sid
	}

	private void validateLoginSuccessful(HtmlPage homePage) {
		if (homePage.getFirstByXPath("//input[@type='password']") != null) {
			// the page contains a password field which means that the login went wrong
			HtmlParagraph errorParagraph = homePage.getFirstByXPath("//p[@class='error_text']")
			throw new AuthenticationException("Login into Fritz!Box failed: " + (errorParagraph ? errorParagraph.asText() : "unknown error"))
		}
	}

}
