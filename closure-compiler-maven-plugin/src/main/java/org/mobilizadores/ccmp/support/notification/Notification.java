package org.mobilizadores.ccmp.support.notification;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Flexible representation of notification.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

	/**
	 * One of the enumerated status codes
	 */
	private TaskStatus status;
	
	/**
	 * The body of the message/notification
	 */
	private String description;
	
	/**
	 * Serves as opportunity to pass any additional string argument from the notifier to the notified
	 */
	private String[] args;

}
