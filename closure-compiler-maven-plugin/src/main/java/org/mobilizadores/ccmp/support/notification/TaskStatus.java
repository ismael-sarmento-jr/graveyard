package org.mobilizadores.ccmp.support.notification;

public enum TaskStatus {

	SUCCESS (0, "Finished with success code"),
	ERROR (1, "Finished with error code"),
	INFO (2, "General information");
	
	Integer code;
	String description;
	
	TaskStatus(Integer code, String description) {
		this.code = code;
		this.description = description;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
}
