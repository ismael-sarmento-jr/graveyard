package org.mobilizadores.ccmp.mojo.test;

import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.junit.Assert;
import org.mobilizadores.ccmp.support.system.ExecutionEnvironment;

public abstract class BasePluginTestCase  extends AbstractMojoTestCase {

	protected ExecutionEnvironment executionEnvironment = new ExecutionEnvironment();
	protected Mojo mojo;
	
	public void setUpEnvironment() {
		try {
			super.setUp();
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
	
	public void initMojo(String mojoName, String pomPath) {
		try {
			this.mojo = super.lookupMojo(mojoName, pomPath);
		} catch (Exception e) {
			Assert.fail("Couldn't look up for mojo. " + e.getMessage());
		}
	}
	
}
