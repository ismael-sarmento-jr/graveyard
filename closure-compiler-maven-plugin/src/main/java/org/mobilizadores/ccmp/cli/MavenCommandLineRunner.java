package org.mobilizadores.ccmp.cli;

import java.io.PrintStream;

import com.google.javascript.jscomp.CommandLineRunner;

/**
 * Simple extension of google's command line runner. As stated in {@link CommandLineRunner}'s 
 * javadoc, we must extend it, instead of using it directly.
 */
public class MavenCommandLineRunner extends CommandLineRunner {

	public MavenCommandLineRunner(String[] args, PrintStream out, PrintStream err) {
		super(args, out, err);
	}

}
