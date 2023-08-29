package org.mobilizadores.ccmp.mojo.version.test;

import java.util.Observable;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.mobilizadores.ccmp.mojo.test.BasePluginTestCase;
import org.mobilizadores.ccmp.mojo.version.VersionMojo;
import org.mobilizadores.ccmp.support.compiler.CompilerProvider;
import org.mobilizadores.ccmp.support.notification.Notification;
import org.mobilizadores.ccmp.support.notification.TaskStatus;

import com.google.javascript.jscomp.jarjar.com.google.re2j.Pattern;

@RunWith(BlockJUnit4ClassRunner.class)
public class VersionMojoIntegrationTest extends BasePluginTestCase {

	Notification result;
	
	@Before
	public void setUp() {
		super.setUpEnvironment();
		super.initMojo("version", "src/test/resources/its/version.mojo.pom.xml");
		((VersionMojo)this.mojo).getExecutor().registerObservers((Observable o, Object notification) -> {
				this.result = (Notification) notification;
		});
	}
	
	@Test
	public void shouldDisplayExpectedVersion() throws Exception {
		Assert.assertNull(this.result);
		Assert.assertEquals(2, ((VersionMojo)this.mojo).getExecutor().getObservers().size());
		try {
			this.mojo.execute();
			Assert.assertNotNull(result);
			Assert.assertEquals(TaskStatus.SUCCESS, result.getStatus());
			Pattern pattern = Pattern.compile("(?m).*Version:.*");
			Assert.assertTrue(pattern.matcher(CompilerProvider.compilerStream.reportAsString()).find());
		} catch (MojoExecutionException | MojoFailureException e) {
			Assert.fail(e.getMessage());
		}
	}
	
}
