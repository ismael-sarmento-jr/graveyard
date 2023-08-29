package org.mobilizadores.ccmp.support.files;

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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.plexus.util.FileUtils;
import org.mobilizadores.ccmp.mojo.compress.InvalidRelativePathException;
import org.mobilizadores.ccmp.mojo.compress.InvalidScriptImportException;

import com.google.common.io.Files;
import com.google.javascript.jscomp.LoggerErrorManager;
import com.google.javascript.jscomp.SourceFile;
import com.google.javascript.jscomp.deps.DependencyInfo;
import com.google.javascript.jscomp.deps.DependencyInfo.Require;
import com.google.javascript.jscomp.deps.JsFileRegexParser;
import com.google.javascript.jscomp.jarjar.com.google.common.collect.UnmodifiableIterator;

/**
 * Contains some utility functions to compute modules' paths and to copy/move files.
 * Uses {@link DependencyInfo} and {@link JsFileRegexParser} to construct the dependency tree for js modules.
 */
public class FilesHandler {

	Logger logger = Logger.getLogger(FileHandler.class.getName());
	JsFileRegexParser jsParser = new JsFileRegexParser(new LoggerErrorManager(this.logger));
	
	public static final String PREV_DIR = "../";
	public static final String SYS_SEPARATOR = File.separator;
	public static final String ESCAPE = "\\";
	//private List<File> effectiveInputFilesList;

	/**
	 * @param depRelPath        path to the dependence file relative to the path of
	 *                          the file in which it's imported
	 * @param sourceFileAbsPath absolute path (without the file name) for the file
	 *                          that requires the dependence
	 * @return The absolute path for the dependence file, given its relative path
	 *         and the source file's absolute path.
	 */
	public String getDepAbsolutePath(String depRelPath, String sourceFileAbsPath) {
		sourceFileAbsPath = normalizePath(sourceFileAbsPath);
		String[] modPath = sourceFileAbsPath.contains(SYS_SEPARATOR) ? sourceFileAbsPath.split(ESCAPE + SYS_SEPARATOR)
				: new String[] { sourceFileAbsPath };
		int countChangeDir = StringUtils.countMatches(depRelPath, PREV_DIR);
		if (modPath.length < countChangeDir)
			throw new InvalidRelativePathException("Invalid path: " + depRelPath);
		return String.join(SYS_SEPARATOR, Arrays.copyOf(modPath, modPath.length - countChangeDir)) + SYS_SEPARATOR
				+ depRelPath.substring(depRelPath.lastIndexOf(PREV_DIR) + PREV_DIR.length(), depRelPath.length());
	}

	/**
	 * Replaces the file separators in the path by the system separators.
	 */
	public static String normalizePath(String path) {
		String match = SYS_SEPARATOR.equals("/") ? "\\\\" : "\\/";
		return path.replaceAll(match, ESCAPE + SYS_SEPARATOR);
	}

	public static List<String> getNormalizedPaths(String[] jsSources) {
		return Arrays.asList(jsSources).stream().map(js -> new File(js).getPath()).collect(Collectors.toList());
	}

	/**
	 * @param file 
	 * 				the file to be compressed.
	 * @return a list with the absolute paths of the main file and its dependencies.
	 * @throws InvalidScriptImportException 
	 */
	public Set<String> listOwnFileAndRequiredModules(File file) throws InvalidScriptImportException {
		Set<String> resultList = new HashSet<>();
		resultList.add(file.getPath());

		try {
			DependencyInfo dependencyInfo = this.jsParser.parseFile(file.getPath(), file.getName(),
					SourceFile.fromFile(file.getPath()).getCode());
			UnmodifiableIterator<Require> requires = dependencyInfo.getRequires().iterator();
			while(requires.hasNext()) {
				Require require = requires.next();
				resultList.addAll(listOwnFileAndRequiredModules(new File(getDepAbsolutePath(require.getRawText(),
						file.getPath().substring(0, file.getPath().lastIndexOf(SYS_SEPARATOR))))));
			}
		} catch (IOException e) {
			throw new InvalidScriptImportException("Couldn't resolve required module(s); " + e.getClass().getName(), (Throwable) e);
		}
		return resultList;
	}

	/**
	 * Copies all the files in the source folder to the destination folder,
	 * maintaining the hierarchy of subfolders, then deletes the temporary folder
	 * with its contents.
	 * 
	 * @param source
	 * @param dest
	 * @throws IOException
	 */
	public void copyFilesFromTempFolder(String source, String dest) throws IOException {
		if (source != null && !source.isEmpty()) {
			File temp = new File(source);
			FileUtils.copyDirectoryStructure(temp, new File(dest));
			FileUtils.deleteDirectory(temp);
		}
	}

	/**
	 * @param file
	 * 			the target file.
	 * @param baseDirectory
	 * 				the parent path which will be removed from the target file path, remaining the relative path.
	 * @return The relative path - including the name of the file - in which the
	 *         compressed file will be written. The path starts with a system
	 *         dependent separator: '/' or '\'
	 */
	public String getResultFileRelativePath(File baseDirectory, File file, String suffix) {
		String fileExtension = Files.getFileExtension(file.getName());
		String fileName = suffix != null ? file.getName().replace(fileExtension, suffix + "." + fileExtension)
				: file.getName();
		return file.getParent().replace(baseDirectory.getPath(), "") + File.separator + fileName;
	}
	
	/**
	 * 
	 * @param inputDirectories
	 * @param failOnNoInputFilesFound
	 * 					if it should throw exception in case an input directory is empty
	 * @return
	 * 			a map containing a list of files for their respective input directories.
	 */
	public List<InputFile> getEffectiveInputFilesList(List<File> inputDirectories) {
		List<InputFile> effectiveInputFilesList = new ArrayList<>();
		Iterator<File> iterator = inputDirectories.iterator();
		while (iterator.hasNext()) {
			File inputDirectory = iterator.next();
			effectiveInputFilesList.addAll(this.getEffectiveInputFilesList(inputDirectory));
		}
		return effectiveInputFilesList;
	}

	/**
	 * Finds distinct javascript files in the inputDirectory and adds them to the
	 * includeFiles list.
	 * 
	 * @param inputDirectory
	 * 				directory to be traversed, containing target files
	 */
	public List<InputFile> getEffectiveInputFilesList(File inputDirectory) {
		List<InputFile> effectiveInputFilesList = new ArrayList<>();
		effectiveInputFilesList.addAll(findAllJSFilesInInputDirectory(inputDirectory));
		return effectiveInputFilesList.stream().distinct().collect(Collectors.toList());
	}

	/**
	 * Adds all the files' paths with extension 'js' to the input files list, except extern js files.
	 * @param inputDirectory
	 */
	private List<InputFile> findAllJSFilesInInputDirectory(File inputDirectory) {
		List<InputFile> jsFiles = new ArrayList<>();
		if (inputDirectory != null) {
			Files.fileTraverser().breadthFirst(inputDirectory).forEach(file -> {
				if (file.isFile() && "js".equals(Files.getFileExtension(file.getName()))
						&& !file.getPath().contains("extern")) {
					jsFiles.add(new InputFile(inputDirectory, file));
				}
			});
		}
		return jsFiles;
	}
}
