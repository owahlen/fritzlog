package com.infinit.fritzlog.authenticator

import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.HtmlButton
import com.gargoylesoftware.htmlunit.html.HtmlPage
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput
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
		HtmlPage loginPage = webClient.getPage("http://${host}/logincheck.lua")
		HtmlPasswordInput passwordInput = (HtmlPasswordInput) loginPage.getFirstByXPath("//input[@type='password']")
		HtmlButton submitButton = (HtmlButton) loginPage.getFirstByXPath("//button[@type='submit']")
		passwordInput.setValueAttribute(password)
		return submitButton.click()
	}

	private String getSidFromHomePage(HtmlPage homePage) {
		URL homePageUrl = homePage.url
		List<NameValuePair> requestParameters = URLEncodedUtils.parse(homePageUrl.toURI(), "UTF-8")
		return requestParameters.find { it.name == 'sid' }.value
	}

}
