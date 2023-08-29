package org.mobilizadores.ccmp.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.mobilizadores.ccmp.support.notification.MojoLogger;

import lombok.Getter;
import lombok.Setter;

/**
 * Parent mojo that contains parameters for this maven plugin configuration.
 */
@Getter
@Setter
public abstract class ClosureCompilerMojo extends AbstractMojo {

	protected MojoExecutor executor;
	
	/**
	 * Defines the size of a thread pool; it can be tunned by the plugin user,
	 * according to the number of output files to be produced, to obtain better
	 * performance.
	 */
	@Parameter(defaultValue = "10")
	public Integer maxNumberOfThreads = 10;

	public ClosureCompilerMojo() {
		super();
		MojoLogger.setLog(this.getLog());
	}

	@Override
	public void execute() throws MojoExecutionException {
		try {
			this.executor.execute();
		} catch (Exception e) {
			System.setSecurityManager(null);
			throw new MojoExecutionException(e.getMessage(), e.getCause());
		}
	}
}
