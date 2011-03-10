/*
 * Copyright 2010 The Scripps Research Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.scripps.fl.pubchem.util;

import java.io.File;
import java.net.URL;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.xml.DOMConfigurator;

import edu.scripps.fl.pubchem.PubChemDB;

public class CommandLineHandler {

	private CommandLine line;
	private Options options;
	private String cmdName;

	public CommandLineHandler() {
		this("<command>");
	}

	public CommandLineHandler(String cmdName) {
		this.cmdName = cmdName;
		options = new Options();
		options.addOption(OptionBuilder.withLongOpt("hib_config").withType("").withValueSeparator('=').hasArg().isRequired().create());
		options.addOption(OptionBuilder.withLongOpt("log_config").withType("").withValueSeparator('=').hasArg().create());
	}

	public void configureOptions(Options options) {

	}

	public CommandLine getCommandLine() {
		return line;
	}

	public String[] handle(String[] args) throws Exception {
		configureOptions(options);
		try {
			line = new PosixParser().parse(options, args);
		} catch (Exception ex) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(cmdName, options, true);
			System.exit(0);
		}

		String log_config = line.getOptionValue("log_config");
		if (log_config == null) {
			URL url = getClass().getClassLoader().getResource("log4j.config.xml");
			DOMConfigurator.configure(url);
		}
		else
			DOMConfigurator.configure(log_config);

		String hib_config = line.getOptionValue("hib_config");
		URL hib_url = new File(hib_config).toURI().toURL();
		PubChemDB.setUp(hib_url);
		return line.getArgs();
	}
}