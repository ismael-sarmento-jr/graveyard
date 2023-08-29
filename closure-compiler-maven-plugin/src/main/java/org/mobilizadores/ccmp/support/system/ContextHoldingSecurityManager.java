package org.mobilizadores.ccmp.support.system;

/*
 * Licensed to Mobilizadores.org under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Mobilizadores licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.
 * Complete information can be found at: https://dev.mobilizadores.com/licenses
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * You may obtain a copy of the License at: http://www.apache.org/licenses/LICENSE-2.0
 */
import java.security.Permission;

import org.apache.maven.plugin.logging.Log;
import org.mobilizadores.ccmp.support.notification.MojoLogger;

class ContextHoldingSecurityManager extends SecurityManager {

	Log log = MojoLogger.getLog();
	
	private static ContextHoldingSecurityManager securityManager = new ContextHoldingSecurityManager();
	
	private boolean exitEnabled;

	private ContextHoldingSecurityManager() {
		super();
	}
	
	public static ContextHoldingSecurityManager getInstance() {
		return securityManager;
	}

	void enableExit() {
		this.exitEnabled = true;
	}
	
	void disableExit() {
		this.exitEnabled = false;
	}
	
	@Override
	public void checkPermission(Permission perm) {
	}

	@Override
	public void checkExit(int status) {
		if(Thread.currentThread().getName().startsWith(JscompGroupThreadFactory.JSCOMP_PREFIX)) {
			throw new CompilerExitAttemptException(status);
		}
		if(!this.exitEnabled) {	
			throw new SystemExitNotAllowedException();
		}
		return;
	}

}
