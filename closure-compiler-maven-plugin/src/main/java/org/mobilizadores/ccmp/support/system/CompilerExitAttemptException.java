package org.mobilizadores.ccmp.support.system;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Compiler is an external library and shouldn't attempt system exit at any time.
 */
@ToString
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class CompilerExitAttemptException extends SecurityException {

	private static final long serialVersionUID = -7305888211257609387L;
	/**
	 * Numeric code that should follow description/message with it
	 */
	private Integer code;

	public CompilerExitAttemptException() {
		super();
	}

	public CompilerExitAttemptException(Integer code) {
		this.code = code;
	}
	
	public CompilerExitAttemptException(Integer code, String message) {
		super(message);
		this.code = code;
	}

}
