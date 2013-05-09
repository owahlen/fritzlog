package com.infinit.fritzlog.adapter

import com.infinit.fritzlog.exceptions.AuthenticationException
import groovy.util.slurpersupport.GPathResult
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import org.apache.commons.codec.digest.DigestUtils
import org.apache.http.HttpResponse

import java.nio.charset.Charset

class FritzBoxAdapter {
	private HTTPBuilder httpBuilder
	private String password

	/**
	 * Setter for the Fritz Box domain name or IP address
	 * @param fritzHost domain or IP address
	 */
	void setHost(String fritzHost) {
		String url = "http://${fritzHost}/cgi-bin/webcm"
		httpBuilder = new HTTPBuilder(url)
	}

	/**
	 * Obtain a reader for the events in the Fritz Box event log table
	 * @return Reader for the event log table
	 */
	Reader getEventReader() {
		String sid = getSid()
		return new StringReader("SID: "+sid)
	}

	/**
	 * The Fritzbox creates a session id (sid) using a challenge response protocol
	 * @return sid
	 */
	private String getSid() {
		GPathResult loginSidXml = getLoginSidXml()
		if (loginSidXml.iswriteaccess == 1) {
			return loginSidXml.SID
		} else {
			String challenge = loginSidXml.Challenge
			String response = computeResponse(challenge)
			return receiveSidFromChallengeResponse(response)
		}
	}

	/**
	 * Obtain an XML data structure that contains the challenge String genereated by the FritzBox
	 * @return GPathResult representing an XML datastructure containing the challenge
	 */
	private GPathResult getLoginSidXml() {
		GPathResult loginSidXml = null
		httpBuilder.request(Method.GET, ContentType.XML) {
			uri.query = [getpage: '../html/login_sid.xml']

			response.success = { HttpResponse resp, GPathResult xml ->
				loginSidXml = xml
			}
		}
		return loginSidXml
	}

	/**
	 * Compute the response on the challenge using the password
	 * @param challenge from the Fritz Box
	 * @return response to be sent to complete the challenge response protocol
	 */
	private String computeResponse(String challenge) {
		String challengePassword = challenge + "-" + password
		return challenge + "-" + DigestUtils.md5Hex(challengePassword.getBytes("UTF-16LE"))
	}

	/**
	 * Send the challenge response to the Fritz Box and extract the sid from the returned HTML page.
	 * @param challengeResponse to be sent to the Fritz Box
	 * @return session id (sid)
	 */
	private String receiveSidFromChallengeResponse(String challengeResponse) {
		GPathResult feedbackHtml = null
		httpBuilder.request(Method.POST, ContentType.HTML) {
			send ContentType.URLENC, ['login:command/response': challengeResponse, 'getpage': '../html/de/menus/menu2.html']

			response.success = { HttpResponse resp, GPathResult html ->
				feedbackHtml = html
			}
		}
		String sid = (GPathResult) feedbackHtml.depthFirst().find { it.name = 'input' && it.@name == 'sid' }.@value
		if (!sid || sid ==~ /0+/) {
			 throw new AuthenticationException("unable to authenticate against Fritz Box")
		}
		return sid
	}

}