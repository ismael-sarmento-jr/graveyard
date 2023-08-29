package org.mobilizadores.ccmp.mojo.compress.test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.mobilizadores.ccmp.mojo.compress.CompressMojo;
import org.mobilizadores.ccmp.mojo.compress.CompressMojoExecutor;
import org.mobilizadores.ccmp.mojo.test.BasePluginTestCase;
import org.mobilizadores.ccmp.support.notification.Notification;
import org.mobilizadores.ccmp.support.notification.TaskStatus;

@RunWith(BlockJUnit4ClassRunner.class)
public class CompressMojoIntegrationTest extends BasePluginTestCase {

	File outputFolder = new File("src/test/resources/output");
	List<Notification> results = new ArrayList<>();
	
	public void setUp(String pomPath) {
		super.setUpEnvironment();
		super.initMojo("compress", pomPath);
		try {
			FileUtils.cleanDirectory(outputFolder);
		} catch (IOException e) {
			Assert.fail("Couldn't clean up output folder.");
		}
		Assert.assertEquals(0, outputFolder.list().length);
		((CompressMojo)this.mojo).getExecutor().registerObservers((Observable o, Object arg) -> {
			if (arg != null)
				results.add((Notification) arg);
		});
	}

	@Test
	public void shouldCompress_SingleOutputFile() {
		this.setUp("src/test/resources/its/bundle.output.pom.xml");
		executeCompression();
		assertResultSuccessAndCompressedFiles();
	}
	
	@Test
	public void shouldFailMojo_OnNoInputFilesFound_InDirectories() {
		boolean exception = false;
		this.setUp("src/test/resources/its/empty.input.dir.pom.xml");
		try {
			this.mojo.execute();
		} catch (Exception e) {
			if(CompressMojoExecutor.NO_INPUT_FILES_FOUND_MSG.equals(e.getMessage())) {
				exception = true;
			}
		}
		Assert.assertTrue(exception);
	}

	@Test
	public void shouldCompress_MultipleOutputFiles_WithSuffix() throws Exception {
		this.setUp("src/test/resources/its/mult.output.no.suffix.pom.xml");
		executeCompression();
		assertResultSuccessAndCompressedFiles();
	}

	@Test
	public void shouldCompress_MultipleOutputFiles_NoSuffix() throws Exception {
		this.setUp("src/test/resources/its/mult.output.suffix.pom.xml");
		executeCompression();
		assertResultSuccessAndCompressedFiles();
	}
	
	@Test
	public void shouldInclude_JsFiles_FromMultipleDirectories() {
		this.setUp("src/test/resources/its/mult.input.dir.pom.xml");
		executeCompression();
		assertResultSuccessAndCompressedFiles();
	}

	private void executeCompression() {
		try {
			mojo.execute();
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}

	public void assertResultSuccessAndCompressedFiles() {
		Assert.assertFalse(results.isEmpty());
		this.results.stream().forEach(notif -> {
			Assert.assertNotNull(notif);
			Assert.assertNotEquals(TaskStatus.ERROR, notif.getStatus());
		});
	}

}
