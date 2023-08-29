package org.mobilizadores.ccmp.support.system;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.maven.plugin.logging.Log;
import org.mobilizadores.ccmp.support.compiler.RunnableClosureCompiler;
import org.mobilizadores.ccmp.support.notification.MojoLogger;

import lombok.Getter;
import lombok.Setter;

/**
 * Centralizes system's exit command and orchestrates the thread pool and thread execution.
 * Holds the single lock, to be used by instances of {@link RunnableClosureCompiler}, to provide 
 * synchronization and thread safety.
 */
public class ExecutionEnvironment {

	Log log = MojoLogger.getLog();
	
	/**
	 * By using {@link ContextHoldingSecurityManager} the system exiting is disabled
	 * before tasks execution and re-enabled after all the tasks are finished.
	 */
	ContextHoldingSecurityManager securityManager = ContextHoldingSecurityManager.getInstance();

	@Getter
	Lock lock = new ReentrantLock();
	
	@Getter
	private ExecutorService executorPoolService;

	@Setter
	@Getter
	private int maxNumberOfThreads;

	public ExecutionEnvironment() {
		this(10);
	}

	/**
	 * Initiates the thread pool, sets security manager and default notification stream.
	 * @param maxNumberOfThreads
	 */
	public ExecutionEnvironment(int maxNumberOfThreads) {
		super();
		this.maxNumberOfThreads = maxNumberOfThreads;
		this.executorPoolService = Executors.newFixedThreadPool(this.maxNumberOfThreads, new JscompGroupThreadFactory());
		System.setSecurityManager(this.securityManager);
	}

	/**
	 * Makes sure the parameter instance of runnable compiler uses this lock and then
	 * enqueue the task.
	 * 
	 * @param syncdRunnable
	 * 			the instance of a {@link SynchronizedRunnable} compiler
	 */
	public void execute(SynchronizedRunnable syncdRunnable) {
		syncdRunnable.setLock(this.lock);
		this.securityManager.disableExit();
		this.executorPoolService.execute(syncdRunnable);
	}

	public void awaitTasksTerminationAfterShutdown() {
		this.executorPoolService.shutdown();
		try {
			if (!this.executorPoolService.awaitTermination(10, TimeUnit.SECONDS)) {
				this.executorPoolService.shutdownNow();
			}
		} catch (InterruptedException e) {
			this.log.error(e);
			this.executorPoolService.shutdownNow();
			Thread.currentThread().interrupt();
		} finally {
			this.securityManager.enableExit();
		}
	}

}
