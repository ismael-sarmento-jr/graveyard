package org.mobilizadores.ccmp.mojo.compress;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.maven.plugin.MojoExecutionException;
import org.mobilizadores.ccmp.cli.CommandLineHelper;
import org.mobilizadores.ccmp.mojo.AbstractMojoExecutor;
import org.mobilizadores.ccmp.support.files.FilesHandler;
import org.mobilizadores.ccmp.support.files.InputFile;
import org.mobilizadores.ccmp.support.notification.Notification;
import org.mobilizadores.ccmp.support.notification.TaskStatus;

/**
 * Handles the compression task.
 */
public class CompressMojoExecutor extends AbstractMojoExecutor<CompressMojo>{

	public static final String NO_INPUT_FILES_FOUND_MSG = "No javascript files were found in input directories. "
										+ " \nTo ignore this type of error, set the parameter 'failOnNoInputFilesFound' to false.";
	FilesHandler filesHandler = new FilesHandler();
	CommandLineHelper clh;
	private String tempFolder;
	
	/**
	 * Initiates a new command line helper and adds the default observer.
	 * 
	 * @param mojo
	 * 
	 * @throws FileNotFoundException                  
	 * 					the file 'log.out' is actually created, if not found, so this
	 *                  exception won't be thrown anyways.
	 * @throws InstantiationException 
	 */
	public CompressMojoExecutor(CompressMojo mojo)
			throws FileNotFoundException, InstantiationException {
		super(mojo);
		this.clh = new CommandLineHelper(mojo);
		this.getObservers().add(getSingleObserver());
	}
	
	/**
	 * Do initial checks
	 */
	@Override
	public void beforeExecution() throws MojoExecutionException {
		checkRequiredInputFilesProperties();
		this.tempFolder = this.mojo.suffix == null // if the names of the files are preserved (suffix is null), a temporary
				// folder needs to be used so the files are not overwritten
				&& this.mojo.outputFile == null ? File.separator + "temp" : "";		
	}

	/**
	 * Copy result files to target destination
	 */
	@Override
	public void afterExecution() {		
		copyTempOutputFiles(tempFolder);
	}
	
	@Override
	public List<String[]> getCommandList() throws InvalidScriptImportException, EmptyInputFolderException {
		List<String[]> commands = new ArrayList<String[]>();
		if (this.mojo.outputFile == null) {
			commands.addAll(this.getCompressCommandsForMultipleOutputFiles());
		} else {
			String[] command = this.getCompressCommandForBundle();
			commands.add(command);
		}
		return commands;
	}
	
	protected List<String[]> getCompressCommandsForMultipleOutputFiles() throws EmptyInputFolderException, InvalidScriptImportException {
		List<String[]> commands = new ArrayList<String[]>();
		List<InputFile> effectiveInputFilesList = this.findInputFiles();
		//FIXME this.mojo.includeFiles
		Iterator<InputFile> iterator = effectiveInputFilesList.iterator();
		while(iterator.hasNext()) {
			InputFile inputFile = iterator.next();
			Set<String> fileWithDeps = this.filesHandler.listOwnFileAndRequiredModules(inputFile.getTargetFile());
			if (fileWithDeps.size() > 0) {
				String outputFilePath = this.mojo.outputDirectory.getAbsolutePath() + tempFolder
						+ this.filesHandler.getResultFileRelativePath(inputFile.getBaseDirectory(), inputFile.getTargetFile(), this.mojo.suffix);
				String[] command = getCommand(outputFilePath, fileWithDeps.toArray(new String[] {}));
				commands.add(command);
			}
		};
		return commands;
	}
	
	protected String[] getCompressCommandForBundle() throws EmptyInputFolderException {
		List<InputFile> inputFiles = this.findInputFiles();
		List<String> inputFilesPaths = this.getTargetFilesPaths(inputFiles);
		String[] command = this.getCommand(this.mojo.outputFile.getAbsolutePath(), inputFilesPaths.toArray(new String[] {}));
		return command;
	}

	/**
	 * @param inputFiles
	 * 				list of {@link InputFile}
	 * @return
	 * 		the paths of each input files as a list of strings
	 */
	private List<String> getTargetFilesPaths(List<InputFile> inputFiles) {
		return inputFiles.stream().map(inputFile -> {
														return inputFile.getTargetFile().getPath();
													}).collect(Collectors.toList());
	}
	
	/**
	 * Finds the js input files inside the input directories.
	 * @return
	 * 		the list of input files
	 * @throws EmptyInputFolderException
	 * 					if not input file is found
	 */
	private List<InputFile> findInputFiles() throws EmptyInputFolderException {
		List<InputFile> inputFiles = this.filesHandler.getEffectiveInputFilesList(this.mojo.inputDirectories);
		if (this.mojo.failOnNoInputFilesFound && inputFiles.isEmpty()) {
			throw new EmptyInputFolderException(NO_INPUT_FILES_FOUND_MSG);
		}
		return inputFiles;
	}

	/**
	 * Uses the input and output files passed as parameters to get the formatted
	 * command line args..
	 */
	private String[] getCommand(String outputFilePath, String[] inputArray) {
		String[] externsStr = this.mojo.externFiles.stream().map(extern -> extern.getAbsolutePath())
				.collect(Collectors.toList()).toArray(new String[] {});
		String[] commandLine = this.getCommandLine(outputFilePath, inputArray, externsStr);
		return commandLine;
	}
	
	/**
	 * @return the final array of options for the command
	 */
	protected String[] getCommandLine(String outputFile, String[] inputFiles, String[] externs) {
		List<String> commandList = new ArrayList<>();
		commandList.addAll(this.clh.getGeneralArgs());
		commandList.addAll(this.clh.getFilesArgs(outputFile, inputFiles, externs));
		return commandList.toArray(new String[] {});
	}

	/**
	 * @return
	 * 		an observer that logs info for each file compiled
	 */
	private Observer getSingleObserver() {
		return (Observable o, Object obj) -> {
			if (obj != null) {
				Notification notif = (Notification) obj;
				String[] args = notif.getArgs();
				if(args != null && args.length > 0) {				
					OptionalInt firstOutputFile = IntStream.range(0, args.length)
							.filter(i -> "--js_output_file".equals(args[i])).findFirst();
					if(firstOutputFile.isPresent()) {
						int pos = firstOutputFile.getAsInt();
						if (notif.getStatus().equals(TaskStatus.ERROR)) {
							this.mojo.getLog().error(notif.getDescription() +": " + args[pos + 1]);
						} else {
							this.mojo.getLog().info(notif.getDescription() + ": " + args[pos + 1]);
						}
					}
				}
			}
		};
	}
	

	private void copyTempOutputFiles(String tempFolder) {
		try {
			if (tempFolder != null && !tempFolder.isEmpty()) {
				this.filesHandler.copyFilesFromTempFolder(this.mojo.outputDirectory.getAbsolutePath() + tempFolder,
						this.mojo.outputDirectory.getAbsolutePath());
			}
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	/**
	 * At least one of the parameters 'inputDirectory' or 'includeFiles' is required
	 * to be specified;
	 */
	private void checkRequiredInputFilesProperties() throws MojoExecutionException {
		if (this.mojo.inputDirectories == null && this.mojo.includeFiles == null)
			throw new MojoExecutionException("Either parameter 'includeFiles' or 'inputDirectory' must be specified");
	}


}
