package org.mobilizadores.ccmp.mojo.compress;

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
import java.io.FileNotFoundException;
import java.util.List;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.kohsuke.args4j.Option;
import org.mobilizadores.ccmp.cli.UnavailableCommandLineOptionsException;
import org.mobilizadores.ccmp.mojo.ClosureCompilerMojo;

/**
 * This class is the default maven mojo of the closure compiler plugin. Its
 * goal is "compress" - which can minify and bundle js files according to configuration 
 * provided - and the default lifecycle phase is "prepare
 * package".
 * 
 */
@Mojo(name = "compress", defaultPhase = LifecyclePhase.PREPARE_PACKAGE)
public class CompressMojo extends ClosureCompilerMojo {

	public CompressMojo() throws UnavailableCommandLineOptionsException, FileNotFoundException, InstantiationException {
		super();
		this.executor = new CompressMojoExecutor(this);
	}
	
	/*
	 * ************************* 
	 * FILES PROPS
	 *************************/
	@Parameter
	public String suffix;
	
	@Parameter(required = true)
	public List<File> inputDirectories;
	
	@Parameter(defaultValue = "target/${project.build.finalName}/WEB-INF/js")
	public File outputDirectory;
	
	@Parameter(alias = "js")
	@Option(name = "--js",
	        usage =
	            "The JavaScript filename. You may specify multiple. "
	                + "The flag name is optional, because args are interpreted as files by default. "
	                + "You may also use minimatch-style glob patterns. For example, use "
	                + "--js='**.js' --js='!**_test.js' to recursively include all "
	                + "js files that do not end in _test.js")
	public List<File> includeFiles;
	
	@Parameter(alias = "externs")
	@Option(name = "--externs",
	        usage = "The file containing JavaScript externs. You may specify multiple")
	public List<File> externFiles;
	
	/**
	 * If the outputFile is not specified, then the files are going to be compressed
	 * separately.
	 */
	@Parameter(alias = "jsOutputFile")
	@Option(name = "--js_output_file",
	        usage = "Primary output filename. If not specified, output is written to stdout")
	public File outputFile;

	/*
	 * ************************* 
	 * MVN PLUGIN PROPS
	 *************************/
	
	@Parameter(defaultValue = "true")
	public Boolean failOnNoInputFilesFound = true;
	
	/*
	 * COMMON COMPILER PROPS
	 */
	@Parameter(defaultValue = "STABLE")
	@Option(name = "--language_in",
			usage =
            "Sets the language spec to which input sources should conform. "
                + "Options: ECMASCRIPT3, ECMASCRIPT5, ECMASCRIPT5_STRICT, "
                + "ECMASCRIPT_2015, ECMASCRIPT_2016, ECMASCRIPT_2017, "
                + "ECMASCRIPT_2018, ECMASCRIPT_2019, ECMASCRIPT_2020,"
                + "ECMASCRIPT_2021, STABLE, ECMASCRIPT_NEXT (latest features supported)")
	String languageIn;
	
	@Parameter(defaultValue = "STABLE")
	@Option(name = "--language_out",
			usage =
            "Sets the language spec to which output should conform. "
                + "Options: ECMASCRIPT3, ECMASCRIPT5, ECMASCRIPT5_STRICT, "
                + "ECMASCRIPT_2015, ECMASCRIPT_2016, ECMASCRIPT_2017, "
                + "ECMASCRIPT_2018, ECMASCRIPT_2019, ECMASCRIPT_2020, ECMASCRIPT_2021, STABLE"
	)
	String languageOut;
	
	@Parameter(alias="jsModuleRoot")
	@Option(name = "--js_module_root")
	List<String> moduleRoot;
	
	@Parameter(defaultValue = "BROWSER")
	@Option(name = "--env",
	        usage = "Determines the set of builtin externs to load. "
	                + "Options: BROWSER, CUSTOM. Defaults to BROWSER.")
	String environment = "BROWSER";
	
	/*
	 * COMPILER CONFIG PROPS
	 */
	@Parameter
	@Option(name = "--compilation_level",
	        aliases = {"-O"},
	        usage =
	            "Specifies the compilation level to use. Options: "
	                + "BUNDLE, "
	                + "WHITESPACE_ONLY, "
	                + "SIMPLE (default), "
	                + "ADVANCED")
	String compilationLevel;
	
	@Parameter(defaultValue = "WARNING")
	@Option(name = "--logging_level",
	        hidden = true,
	        usage =
	            "The logging level (standard java.util.logging.Level"
	                + " values) for Compiler progress. Does not control errors or"
	                + " warnings for the JavaScript code under compilation")
	String loggingLevel = "WARNING";
	
	@Parameter(defaultValue = "QUIET")
	@Option(
	        name = "--warning_level",
	        aliases = {"-W"},
	        usage = "Specifies the warning level to use. Options: " + "QUIET, DEFAULT, VERBOSE")
	String warningLevel = "QUIET";
	
	@Parameter
	@Option(name = "--charset",
	        usage =
	            "Input and output charset for all files. By default, we "
	                + "accept UTF-8 as input and output US_ASCII")
	String charset;
	
	@Parameter(defaultValue = "1")
	@Option(name = "--num_parallel_threads",
	        usage = "Use multiple threads to parallelize parts of the compilation.")
	Integer numParallelThreads;
	
	@Parameter
	@Option(name = "--summary_detail_level",
	        usage = "Controls how detailed the compilation summary is. Values:"
	                + " 0 (never print summary), 1 (print summary only if there are "
	                + "errors or warnings), 2 (print summary if the 'checkTypes' "
	                + "diagnostic  group is enabled, see --jscomp_warning), "
	                + "3 (always print summary). The default level is 1")
	Integer summaryDetailLevel;
}
