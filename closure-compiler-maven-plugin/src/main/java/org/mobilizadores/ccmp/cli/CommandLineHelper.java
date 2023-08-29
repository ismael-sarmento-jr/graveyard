package org.mobilizadores.ccmp.cli;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.logging.Log;
import org.kohsuke.args4j.Option;
import org.mobilizadores.ccmp.support.notification.MojoLogger;

/**
 * This class contains utility methods to properly build the command line to be
 * executed by the compressor. It wraps an instance of the maven mojo and makes
 * heavy use of reflection to set the values to the respective parameters. The
 * terms args and options are used as synonyms.
 */
public class CommandLineHelper {

	Log log = MojoLogger.getLog();

	/**
	 * @Option.name in
	 *              {@link com.google.javascript.jscomp.CommandLineRunner$Flags#jsOutputFile}
	 */
	public static final String JS_OUTPUT_FILE_OPTION = "--js_output_file";
	/**
	 * @Option.name in
	 *              {@link com.google.javascript.jscomp.CommandLineRunner$Flags#js}
	 */
	private static final String JS_OPTION = "--js";

	private static final String EXTERNS_OPTION = "--externs";

	/**
	 * Instance of the maven mojo with its parameters set
	 */
	private Mojo mojo;

	/**
	 * Fields in the {@link CommandLineHelper#classWithCommandOptions}, which
	 * provide information about the type of the option - if unique or iterable, for
	 * example - and respective option string formatted, for instance
	 * <code>--js_option</code>
	 */
	Field[] options;

	public CommandLineHelper(Mojo mojo) {
		this.mojo = mojo;
		this.options = mojo.getClass().getDeclaredFields();
	}

	/**
	 * @param outputFile
	 * @param inputFiles
	 * @return
	 */
	public List<String> getFilesArgs(String outputFile, String[] inputFiles, String[] externs) {
		List<String> commandList = new ArrayList<>();
		commandList.add(JS_OUTPUT_FILE_OPTION);
		commandList.add(outputFile);
		Arrays.asList(inputFiles).forEach(file -> {
			commandList.add(JS_OPTION);
			commandList.add(file);
		});
		Arrays.asList(externs).forEach(file -> {
			commandList.add(EXTERNS_OPTION);
			commandList.add(file);
		});
		return commandList;
	}

	/**
	 * @return the list of primitive and iterable args
	 */
	public List<String> getGeneralArgs() {
		List<String> commandList = new ArrayList<String>();
		Arrays.stream(this.options).forEach(option -> {
			if (option.isAnnotationPresent(Option.class)) {
				if (Iterable.class.isAssignableFrom(option.getType())) {
					commandList.addAll(this.getIterableArgsPairs(option));
				} else {
					commandList.addAll(getPrimitiveArgPair(option));
				}
			}
		});
		return commandList;
	}

	/**
	 * Checks if the option's type is not derived and adds it to the args' pairs.
	 * If it's a complex type - a BigDecimal, for instance - the argument is ignored.
	 * 
	 * @see ArgumentUtils#isSimpleType(Class)
	 * @param option
	 * @return the argument key-value pairs
	 */
	private List<String> getIterableArgsPairs(Field option) {
		List<String> argsPairs = new ArrayList<String>();
		try {
			Class<?> optionType = ArgumentUtils.getActualTypeOfIterable(option);
			if (ArgumentUtils.isSimpleType(optionType)) {
				argsPairs.addAll(getIterableArgsPairsInner(option));
			}
		} catch (WrongArgumentTypeException e) {
			log.warn(e);
		}
		return argsPairs;
	}


	/**
	 * 
	 * 
	 * @param option the arg to be set, as defined in the
	 *               {@link CommandLineHelper#optionsClass}
	 * @return a list of pairs containing the final option name and a value for each
	 *         copy of option added
	 */
	private List<String> getIterableArgsPairsInner(Field option) {
		final String optionName = option.getDeclaredAnnotation(Option.class).name();
		List<String> argsPairs = new ArrayList<String>();
		try {
				if (FieldUtils.readField(option, mojo, true) != null) {
					Consumer<String> consumer = (value) -> {
						argsPairs.add(optionName);
						argsPairs.add(value.toString());
					};
					MethodUtils.invokeMethod(option.get(mojo), true, "forEach", consumer);
				}
		} catch (IllegalAccessException | NoSuchMethodException | IllegalArgumentException e) {
			Throwable cause = e.getCause() != null ? e.getCause() : e;
			log.error("Couldn't create one or more of the iterable command line args.", cause);
		} catch (InvocationTargetException e) {
			Throwable cause = e.getTargetException() != null && e.getCause() != null ? e.getTargetException().getCause() : e;
			log.error("Couldn't create one or more of the iterable command line args", cause);
		}
		return argsPairs;
	}

	/**
	 * Checks if exists a mojo parameter equivalent to the option passed as
	 * parameter and if its value is not null and returns corresponding args pair.
	 * 
	 * @param option the arg to be set, as defined in the
	 *               {@link CommandLineHelper#optionsClass}
	 * @return a pair of strings with the option name followed by it's value
	 */
	private List<String> getPrimitiveArgPair(Field option) {
		List<String> commandList = new ArrayList<String>();
		try {
				if (FieldUtils.readField(option, mojo, true) != null
						&& ArgumentUtils.isSimpleType(option.getType())
					) {
					commandList.add(option.getDeclaredAnnotation(Option.class).name());
					commandList.add(String.valueOf(FieldUtils.readField(option, mojo)));
				}
		} catch (IllegalAccessException e) {
			log.error("Couldn't create one or more of the command line args: illegal access to field", e);
		}
		return commandList;
	}
}
