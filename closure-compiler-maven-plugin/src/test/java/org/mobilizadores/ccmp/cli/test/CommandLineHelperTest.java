package org.mobilizadores.ccmp.cli.test;

import java.util.Iterator;
import java.util.List;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.mobilizadores.ccmp.cli.CommandLineHelper;
import org.mobilizadores.ccmp.mojo.compress.CompressMojo;

@RunWith(BlockJUnit4ClassRunner.class)
public class CommandLineHelperTest extends AbstractMojoTestCase {
  
  CompressMojo ccm;
  CommandLineHelper clh;
  
  @Before
  public void setUp() throws Exception {
    super.setUp();
    try {		
    	this.ccm = (CompressMojo) lookupMojo("compress", "src/test/resources/its/extensive.params.pom.xml");
	} catch (Exception e) {
		Assert.fail("Coudn't lookup mojo: " + e.getCause().getMessage());
	}
    this.clh = new CommandLineHelper(this.ccm);
  }
  
  @Test
  public void shouldReturn_FilesArgs_ConsistentJsPairs() {
    String outputFile = "src/test/resource/test-results/output.js";
    String[] inputFiles = {"src/test/resource/dir1/file11", "src/test/resource/dir1/dir1.1/file21"};
    String[] externs = {"src/test/resources/dir1/extern1.js"};
    List<String> filesArgs = clh.getFilesArgs(outputFile, inputFiles, externs );
    assertAlternateArgs(filesArgs);
  }
  
  @Test
  public void shouldCapture_allAndOnly_MojoParams_WithValues() {
	  List<String> generalArgs = this.clh.getGeneralArgs();
	  //contains
	  Assert.assertTrue(generalArgs.contains("--js_module_root"));
	  Assert.assertTrue(generalArgs.contains("--language_in"));
	  Assert.assertTrue(generalArgs.contains("--language_out"));
	  Assert.assertTrue(generalArgs.contains("--compilation_level"));
	  Assert.assertTrue(generalArgs.contains("--logging_level"));
	  Assert.assertTrue(generalArgs.contains("--warning_level"));
	  //doesn't contain
	  Assert.assertFalse(generalArgs.contains("--js_output_file"));
	  Assert.assertFalse(generalArgs.contains("--externs"));
  }

  /**
   * Asserts that args are alternated between flag and value
   */
  private void assertAlternateArgs(List<String> args) {
    boolean isArg, isPreviousArg = false;
    Iterator<String> iterator = args.iterator();
    while (iterator.hasNext()) {
      isArg = iterator.next().startsWith("-");
      Assert.assertTrue(isArg ^ isPreviousArg);
      isPreviousArg = isArg;
    } 
  }
  
}
