package com.infinit.fritzlog.event

/**
 * Representation of the different event types of the Fritz!Box
 */
public enum EventType {
	ALL('aus'),
	TELEPHONE('telefon'),
	INTERNET('internt'),
	USB('usb'),
	WLAN('wlan'),
	SYSTEM('system')

	private final String tab

	EventType(String tab) {
		this.tab = tab
	}

	String getTab() {
		return tab
	}
}