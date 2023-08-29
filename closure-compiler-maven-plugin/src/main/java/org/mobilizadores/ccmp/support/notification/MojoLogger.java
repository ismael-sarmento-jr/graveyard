package org.mobilizadores.ccmp.support.notification;

import org.apache.maven.plugin.logging.Log;

/**
 * Encapsulates the default logger, instantiated by Maven engine. Must be used throughout
 * the plugin, to guarantee cohese message format for
 * output and also outputs debug messages only if Maven's debug mode is enabled. 
 */
public class MojoLogger {

	private static Log log;

	public static Log getLog() {
		return log;
	}
	
	public static void setLog(Log log) {
		MojoLogger.log = log;
	}
}
