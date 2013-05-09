fritzlog
========

fritzlog is a Java library and an executable that allow developers and users to extract the
event log from an AVM Fritz!Box.

Installation
============
After cloning the source code the jar files can be built using gradle.
The command "gradle build" will compile all relevant files.

Usage
=====
Currently there is no option processing.
Hostname or IP address of the Fritzbox and its password must be configured in the file Main.java.
After that use the command "gradle run" to extract the event log.

Development
===========
Using the command "gradle idea" the project files for Intellij Idea are generated.
This simplifies the project setup for development.

