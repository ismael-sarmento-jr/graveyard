package org.mobilizadores.ccmp.support.compiler;

import org.mobilizadores.ccmp.support.notification.DelayedInternalStream;

/**
 * Provides new instances of {@link RunnableClosureCompiler}.
 */
public class CompilerProvider {

	/**
	 * The resulting log of the compression must be delayed until
	 * the end of all compiling tasks, to avoid cross writing to system's output.
	 */
	public static DelayedInternalStream compilerStream = new DelayedInternalStream(System.out);
	
	/**
	 * @return A new instance of a runnable with the mojo's stream, for delayed report and the args
	 *         for the final command line.
	 */
	public static RunnableClosureCompiler getNewRunnableClosureCompiler(final String[] commandLine) {
		return new RunnableClosureCompiler(commandLine, compilerStream);
	}
}
