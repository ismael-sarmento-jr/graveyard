package org.mobilizadores.ccmp.support.compiler;

import java.io.PrintStream;
import java.util.ConcurrentModificationException;
import java.util.Observable;
import java.util.concurrent.locks.Lock;

import org.mobilizadores.ccmp.cli.MavenCommandLineRunner;
import org.mobilizadores.ccmp.support.notification.Notification;
import org.mobilizadores.ccmp.support.notification.TaskStatus;
import org.mobilizadores.ccmp.support.system.CompilerExitAttemptException;
import org.mobilizadores.ccmp.support.system.SynchronizedRunnable;

import com.google.javascript.jscomp.CommandLineRunner;

import lombok.Getter;
import lombok.Setter;

/**
 * Runs the compiler according to the arguments passed and notify the observers
 * on the results of the run.
 */
@Getter
@Setter
public class RunnableClosureCompiler extends Observable implements SynchronizedRunnable {

	private String[] args;
	private Lock lock;
	private PrintStream stream;
	
	public RunnableClosureCompiler(String[] args, PrintStream stream) {
		super();
		this.args = args;
		this.stream = stream;
	}
	
	public RunnableClosureCompiler(String[] args, Lock lock, PrintStream stream) {
		super();
		this.args = args;
		this.lock = lock;
		this.stream = stream;
	}

	/**
	 * Invokes the method <i>run</i> in the command line runner instance (clr). The
	 * list of 'mixedJsSources' in {@link CommandLineRunner#Flags} is static, set
	 * during a new <i>clr</i> instantiation and read during configuration
	 * initiation, therefore the instantiation of <i>clr</i> and subsequent access
	 * to the list are synchronized. The results of the tasks are reported to the
	 * observers, so they can be logged out.
	 */
	@Override
	public void run() {
		try {
			CommandLineRunner clr = null;
			lock.lock();
			clr = new MavenCommandLineRunner(this.args, this.stream, this.stream);
			if (clr != null) {
				setChanged();
				notifyObservers(new Notification(TaskStatus.INFO, "Compiler task started", this.args));
				lock.unlock();
				runClosureCompiler(clr);
			}
		} catch (CompilerExitAttemptException e) {
			setChanged();
			handleCompilerExitAttempt(e);
		} catch (Throwable e) {
			handleException(e);
		} 
	}

	/**
	 * Checks if the command line runner can run then runs it.
	 */
	public void runClosureCompiler(CommandLineRunner clr) {
		if(clr.shouldRunCompiler()) {					
			clr.run();
		}
	}
	
	/**
	 * Treats exception by type and notify observers.
	 */
	private void handleException(Throwable e) {
		setChanged();
		Throwable cause = e.getCause();
		if(cause != null) {			
			if (cause.getClass().isAssignableFrom(CompilerExitAttemptException.class)) {
				handleCompilerExitAttempt((CompilerExitAttemptException) cause);
			} else if (cause.getClass().isAssignableFrom(ConcurrentModificationException.class)) {
				notifyObservers(
						new Notification(TaskStatus.ERROR, "Concurrent modification error trying to compress ", this.args));
			}
			return;
		}
		notifyObservers(new Notification(TaskStatus.ERROR, "Error in the plugin, trying to compress ", this.args));
	}
	
	public void handleCompilerExitAttempt(CompilerExitAttemptException e) {
		if (e.getCode() == TaskStatus.SUCCESS.getCode()) {
			notifyObservers(new Notification(TaskStatus.SUCCESS, "Compiler task finished", this.args));
		} else {
			notifyObservers(
					new Notification(TaskStatus.ERROR, "Error reported by the compressor trying to compress ", this.args));
		}
	}

}
