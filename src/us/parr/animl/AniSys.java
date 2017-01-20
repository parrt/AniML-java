/*
 * Copyright (c) 2017 Terence Parr. All rights reserved.
 * Use of this file is governed by the BSD 3-clause license that
 * can be found in the LICENSE file in the project root.
 */

package us.parr.animl;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.PumpStreamHandler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AniSys {
	/** Given an executable command name and arguments, execute the process
	 *  and return the {exit code (as a string), stdout, stderr} in
	 *  a String array.
	 *
	 *  The returned output strings are "" if empty, not null.
	 */
	public static String[] exec(String executable, String... args) {
		CommandLine cmdLine = new CommandLine(executable);
		cmdLine.addArguments(args);
		return exec(cmdLine);
	}

	/** Given a command line including executable command name and arguments,
	 *  execute the process and return the {exit code (as a string),
	 *  stdout, stderr} in a String array.
	 *
	 *  The returned output strings are "" if empty, not null.
	 */
	public static String[] exec(String cmdLine) {
		return exec(CommandLine.parse(cmdLine));
	}

	public static String[] exec(CommandLine cmdLine) {
		int exitCode = 1;
		ByteArrayOutputStream stdout = new ByteArrayOutputStream();
		ByteArrayOutputStream stderr = new ByteArrayOutputStream();
		try {
		    DefaultExecutor executor = new DefaultExecutor();
			executor.setStreamHandler(new PumpStreamHandler(stdout, stderr));
			exitCode = executor.execute(cmdLine);
		}
		catch (ExecuteException e) {
			//e.printStackTrace(System.err);
			// stderr should have reason
		}
		catch (IOException ioe) {
			ioe.printStackTrace(System.err);
		}
		return new String[] {String.valueOf(exitCode), stdout.toString(), stderr.toString()};
	}
}
