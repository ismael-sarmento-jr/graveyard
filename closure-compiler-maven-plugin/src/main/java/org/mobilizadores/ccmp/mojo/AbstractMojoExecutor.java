package org.mobilizadores.ccmp.mojo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Observer;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.mobilizadores.ccmp.support.compiler.CompilerProvider;
import org.mobilizadores.ccmp.support.compiler.RunnableClosureCompiler;
import org.mobilizadores.ccmp.support.notification.MojoLogger;
import org.mobilizadores.ccmp.support.system.ExecutionEnvironment;

import lombok.Getter;

/**
 * Holds the common execution environment for compilation and the mojo instance
 * containing execution parameters. 
 * Provides notification capabilities, holding {@link Observer} instances which 
 * will be notified of the execution status.
 */
public abstract class AbstractMojoExecutor<T extends ClosureCompilerMojo> implements MojoExecutor {

	Log log = MojoLogger.getLog();
	
	protected T mojo;
	protected ExecutionEnvironment executionEnvironment = new ExecutionEnvironment();

	/**
	 * Allows other objects to check the result of the compression and provide logging or checks.
	 */
	@Getter
	private final Set<Observer> observers = Collections.newSetFromMap(new ConcurrentHashMap<Observer, Boolean>(2));

	public AbstractMojoExecutor(T mojo) throws InstantiationException {
		super();
		if (mojo != null) {
			this.mojo = mojo;
			executionEnvironment.setMaxNumberOfThreads(mojo.maxNumberOfThreads);
		} else {
			throw new InstantiationException("Parameter 'mojo' cannot be null");
		}
	}

	/**
	 * This is used as a template method and as such mustn't be overriden, but
	 * executed as defined.
	 */
	@Override
	public void execute() throws Exception {
		beforeExecution();
		doExecution();
		afterExecution();
	}

	protected void beforeExecution() throws MojoExecutionException {
	}

	protected void afterExecution() throws MojoExecutionException {
	}

	@Override
	public void registerObservers(Observer... observers) {
		this.observers.addAll(Arrays.asList(observers));
	}
	
	@Override
	public void unregisterObservers(Observer... observers) {
		this.observers.removeAll(Arrays.asList(observers));
	}

	/**
	 * Each command is an array of strings, being each array element a piece of the
	 * command line, in sequence
	 * 
	 * @return the list of commands to be executed.
	 * @throws Exception
	 */
	protected abstract List<String[]> getCommandList() throws Exception;

	/**
	 * Starts the enqueing for the commands/tasks and waits for tasks termination.. 
	 * 
	 * @throws Exception
	 */
	protected void doExecution() throws Exception {
		List<String[]> commands = getCommandList();
		enqueueCompilationTasks(commands);
		this.executionEnvironment.awaitTasksTerminationAfterShutdown();
		reportCompilerResult();
	}

	/**
	 * Outputs the delayed compiler stream using maven's logger.
	 */
	private void reportCompilerResult() {
		BufferedReader br = new BufferedReader(new StringReader(
					CompilerProvider.compilerStream.reportAsString()
				));
		String line;
		try {
			while((line = br.readLine()) != null) {
				this.log.info("[COMPILER] " + line);
			}
		} catch (IOException e) {
			this.log.warn(e);
		}
	}

	/**
	 * Instantiates rcc with given command line, adds observers for
	 * notification/logging and enqueues runnable
	 * 
	 * @param commands
	 */
	protected void enqueueCompilationTasks(List<String[]> commands) {
		commands.forEach(command -> {
			RunnableClosureCompiler rcc = CompilerProvider.getNewRunnableClosureCompiler(command);
			this.getObservers().forEach(observer -> rcc.addObserver(observer));
			this.executionEnvironment.execute(rcc);
		});
	}

}
