package org.mobilizadores.ccmp.support.notification;

import java.util.Observer;
import java.util.Set;

/**
 * Interface that manages the notifications, sending them from observables/subjects to observers/subscribers
 */
public interface Notifier {

	/**
	 * Adds observers which will serve as notifiables for logging, checks and testing
	 */
	public void registerObservers(Observer... object);
	
	/**
	 * Remove observers from the set
	 */
	public void unregisterObservers(Observer... object);

	/**
	 * Represents any object that can observe ({@link Observer}) and receive a notification.
	 */
	public Set<Observer> getObservers();
}
