package org.mobilizadores.ccmp.support.system;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Offer factory function to create new threads for JSCOMP thread group.
 */
class JscompGroupThreadFactory implements ThreadFactory {
    
	public static final String JSCOMP_PREFIX = "jscomp";
	private final ThreadGroup group;
	private final String threadNamePrefix;

    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private final AtomicInteger threadNumber = new AtomicInteger(1);

    JscompGroupThreadFactory() {
        SecurityManager securityManager = System.getSecurityManager();
        this.group = securityManager != null ? securityManager.getThreadGroup() : Thread.currentThread().getThreadGroup();
        threadNamePrefix = JSCOMP_PREFIX + "-" + this.group.getName() + "-" + poolNumber.getAndIncrement();
    }

    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r, threadNamePrefix + threadNumber.getAndIncrement(), 0);
        setForegroundPriority(t, Thread.NORM_PRIORITY);
        return t;
    }

    /**
     * Sets the prority to the foreground thread
     */
	private void setForegroundPriority(Thread t, int priority) {
		if (t.isDaemon()) {
        	t.setDaemon(false);
        }
		t.setPriority(priority);
	}
}