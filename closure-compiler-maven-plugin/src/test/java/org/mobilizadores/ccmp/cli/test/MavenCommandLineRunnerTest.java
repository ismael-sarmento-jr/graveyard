package org.mobilizadores.ccmp.cli.test;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.mobilizadores.ccmp.cli.MavenCommandLineRunner;
import org.mobilizadores.ccmp.mojo.test.BasePluginTestCase;
import org.mobilizadores.ccmp.support.compiler.RunnableClosureCompiler;
import org.mobilizadores.ccmp.support.files.FilesHandler;

import com.google.javascript.jscomp.CommandLineRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(BlockJUnit4ClassRunner.class)
public class MavenCommandLineRunnerTest extends BasePluginTestCase {
	
	RunnableClosureCompiler rcc;
	String[] js = { "src/test/resources/dir1/file11.js", "src/test/resources/dir1/dir1.1/file21.js" };
	String[] externs = { "src/test/resources/dir1/extern1.js" };
	String outputFile = "src/test/resources/ouptput.js";
	String[] args = { "--js", "src/test/resources/dir1/file11.js", "--js", "src/test/resources/dir1/dir1.1/file21.js",
			"--externs", "src/test/resources/dir1/extern1.js", "--js_output_file", "src/test/resources/ouptput.js" };

	@Before
	public void setUp() {
		super.setUpEnvironment();
		this.rcc = new RunnableClosureCompiler(this.args, super.executionEnvironment.getLock(), System.out);
	}
	
	@Test
	@SuppressWarnings("unchecked")
	public void shouldReturn_NewCompilerInstance_WithConsistentConfiguration() {
		CommandLineRunner clr;
		try {
			clr = new MavenCommandLineRunner(rcc.getArgs(), rcc.getStream(), rcc.getStream());

			Assert.assertNotNull(clr);
			Object config = getConfig(clr);

			List<Object> flagsEntries = (List<Object>) getConfigParam(config, "mixedJsSources");
			List<String> jsSources = getJsSources(flagsEntries);

			Assert.assertTrue(jsSources.containsAll(FilesHandler.getNormalizedPaths(this.js)));

			String outputFile = (String) getConfigParam(config, "jsOutputFile");
			Assert.assertEquals(this.outputFile, outputFile);

			List<String> externs = (List<String>) getConfigParam(config, "externs");
			Assert.assertTrue(externs.containsAll(Arrays.asList(this.externs)));
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}

	public Object getConfigParam(Object config, String paramName) {
		Object configParamValue = null;
		try {
			configParamValue = FieldUtils.readDeclaredField(config, paramName, true);
		} catch (IllegalAccessException e) {
			Assert.fail("Error in the test: could not read field " + paramName
					+ " in AbstractCommandLineRunner.CommandLineConfig: ");
		}
		;
		return configParamValue;
	}

	public List<String> getJsSources(List<Object> flagsEntries) {
		List<String> sources = new ArrayList<>();
		flagsEntries.stream().forEach((flagEntry) -> {
			try {
				sources.add((String) FieldUtils.readDeclaredField(flagEntry, "value", true));
			} catch (IllegalAccessException e) {
				Assert.fail("Error in the test: could not read fields in AbstractCommandLineRunner.FlagEntry: "
						+ e.getMessage());
			}
		});
		return sources;
	}

	public Object getConfig(CommandLineRunner clr) {
		Object config = null;
		try {
			config = FieldUtils.readField(clr, "config", true);
		} catch (IllegalAccessException e) {
			Assert.fail("Error in the test: could not read 'config' in AbstractCommandLineRunner: " + e.getMessage());
		}
		return config;
	}
}
