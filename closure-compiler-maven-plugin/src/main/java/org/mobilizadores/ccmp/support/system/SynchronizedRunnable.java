package org.mobilizadores.ccmp.support.system;

import java.util.concurrent.locks.Lock;

/**
 * Provides access to the lock for runnables.
 */
public interface SynchronizedRunnable extends Runnable {

	public Lock getLock();
	
	public void setLock(Lock lock);
}
