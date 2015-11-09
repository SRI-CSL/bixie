/*
 * jimple2boogie - Translates Jimple (or Java) Programs to Boogie
 * Copyright (C) 2013 Martin Schaef and Stephan Arlt
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package bixie.translation;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import bixie.boogie.ProgramFactory;
import bixie.translation.soot.SootPrelude;
import bixie.translation.soot.SootRunner;
import bixie.translation.util.MhpInfo;
import bixie.util.Log;

/**
 * Dispatcher
 * 
 * @author schaef
 */
public class Main {

	
	public static void main(String[] args) {
		Options options = Options.v();
		CmdLineParser parser = new CmdLineParser(options);		
		try {
			// parse command-line arguments
			parser.parseArgument(args);
			run(Options.v().getJarFile(),
					Options.v().getBoogieFile());
		} catch (CmdLineException e) {
			Log.error(e.toString());
			Log.error("java -jar joogie.jar [options...] arguments...");
			parser.printUsage(System.err);
		}
	}	
	

	/**
	 * Runs the dispatcher
	 */
	public static void run(String input, String output) {
		try {
			runSoot(input, output);
		} catch (Exception e) {
			Log.error(e.toString());
		} 
	}
	
	public static void setClassPath(String cp) {
		Options.v().setClasspath(cp);
	}

	/**
	 * Use this run function if you plan to use jar2bpl as a library.
	 * It runs soot, creates a Boogie AST and returns the ProgramFactory but
	 * deletes all soot related data from memory.
	 * @param input
	 * @return
	 */
	public static ProgramFactory run(String input) {
		ProgramFactory pf = null;
		try {
			runSoot(input, null);
			pf = GlobalsCache.v().getPf();
		} catch (Exception e) {
			Log.error(e.toString());
		} finally {
			GlobalsCache.resetInstance();
			SootPrelude.resetInstance();
			MhpInfo.resetInstance();
			Options.resetInstance();
			soot.G.reset();
		}
		return pf;
	}
	
	
	/**
	 * Runs Soot
	 */
	protected static void runSoot(String input, String output) {
		SootRunner sootRunner = new SootRunner();
		sootRunner.run(input);
	}

}
