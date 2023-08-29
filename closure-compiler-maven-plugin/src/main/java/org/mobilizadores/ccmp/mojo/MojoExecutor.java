package org.mobilizadores.ccmp.mojo;

import org.mobilizadores.ccmp.support.notification.Notifier;

/**
 * The executor has the same method 'execute' as the mojo; it is meant to be used
 * as a delegate, to remove the execution complexity from the mojo, so the mojo can
 * be used only to hold the configuration parameters.
 */
public interface MojoExecutor extends Notifier {

	/**
	 * Where the main execution logic must be performed
	 * @throws Exception 
	 */
	public void execute() throws Exception;

}
