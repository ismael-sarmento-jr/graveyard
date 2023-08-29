package org.mobilizadores.ccmp.mojo.compress.test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.mobilizadores.ccmp.mojo.compress.InvalidRelativePathException;
import org.mobilizadores.ccmp.mojo.compress.InvalidScriptImportException;
import org.mobilizadores.ccmp.support.files.FilesHandler;
import org.mobilizadores.ccmp.support.files.InputFile;

@RunWith(BlockJUnit4ClassRunner.class)
public class FilesHandlerTest {

  FilesHandler filesHandler = new FilesHandler();
  
  @Test
  public void shouldReturn_correctRequireAbsPath() {
    String moduleAbsPath = "abs/mod/path";

    String requireRelPath = "./path.require";
    String requireAbsPath = filesHandler.getDepAbsolutePath(requireRelPath, moduleAbsPath);
    Assert.assertEquals("abs"+File.separator +"mod"+File.separator +"path" +File.separator +"path.require", requireAbsPath);
    
    requireRelPath = "../path.require";
    requireAbsPath = filesHandler.getDepAbsolutePath(requireRelPath, moduleAbsPath);
    Assert.assertEquals("abs"+File.separator +"mod"+File.separator +"path.require", requireAbsPath);
    
    requireRelPath = "../../path.require";
    requireAbsPath = filesHandler.getDepAbsolutePath(requireRelPath, moduleAbsPath);
    Assert.assertEquals("abs"+File.separator +"path.require", requireAbsPath);
  }
  
  @Test
  public void shouldThrowException_onAbsPathInvalidPath() {
    boolean exception = false;
    try {
      String moduleAbsPath = "abs/mod/path";
      String requireRelPath = "../../../../path.require";
      filesHandler.getDepAbsolutePath(requireRelPath, moduleAbsPath);
    } catch (InvalidRelativePathException e) {
      exception = true;
    }
    Assert.assertTrue(exception);
  }

  
  @Test
  public void shouldList_AllRequiredModules() {
    Set<String> list = null;
    File require = new File("src/test/resources/dir1/file11.js");
    try {
      list = filesHandler.listOwnFileAndRequiredModules(new File("src/test/resources/dir1/dir1.1/file21.js"));
    } catch (InvalidScriptImportException e) {
		Assert.fail(e.getMessage());
	}
    Assert.assertNotNull(list);
    Assert.assertTrue(!list.isEmpty());
    Assert.assertTrue(list.contains(require.getPath()));
  }
  
  @Test
  public void shouldThrowException_onModuleNotFound() {
    boolean exception = false;
    try {
      filesHandler.listOwnFileAndRequiredModules(new File("src/test/resources/dir3/js_with_invalid_import.js"));
    } catch (InvalidScriptImportException e) {
		exception = true;
	}
    Assert.assertTrue(exception);
  }
  
  @Test
  public void shouldReturn_correctRelativePaths() {
    String suffix = null;
    File baseDirectory = new File("src/test/resources");
    File file = new File("src/test/resources/dir1/dir1.1/file22.mjs");
    
    String relativePath = filesHandler.getResultFileRelativePath(baseDirectory, file, suffix);
    Assert.assertEquals(File.separator +"dir1"+File.separator +"dir1.1"+File.separator +"file22.mjs", relativePath);
    
    suffix = "min";
    baseDirectory = new File("src/test/resources/dir1");
    relativePath = filesHandler.getResultFileRelativePath(baseDirectory, file, suffix);
    Assert.assertEquals(File.separator +"dir1.1"+File.separator +"file22.min.mjs", relativePath);
    
    baseDirectory = new File("src/test");
    relativePath = filesHandler.getResultFileRelativePath(baseDirectory, file, suffix);
    Assert.assertEquals(File.separator +"resources"+File.separator +"dir1"+File.separator +"dir1.1"+File.separator +"file22.min.mjs", relativePath);
  }
  
  @Test
  public void shouldFind_DistinctJsFiles_InDirectories() {
    File inputDirectory = new File("src/test/resources/dir1");
    List<File> includeFiles = new ArrayList<>();
    File duplicateFile = new File("src/test/resources/dir1/file11.js");
    includeFiles.add(duplicateFile);
    List<InputFile> list = null;
    list = filesHandler.getEffectiveInputFilesList(inputDirectory);
    Assert.assertNotNull(list);
    Assert.assertTrue(!list.isEmpty());
    long count = list.stream().filter(file -> file.getTargetFile().equals(duplicateFile)).count();
    Assert.assertEquals(1L, count);
    
  }
  
}
