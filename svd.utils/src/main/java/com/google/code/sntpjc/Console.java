/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.code.sntpjc;

import java.io.IOException;
import java.net.UnknownHostException;

/**
 * Console interface for SntpClient.
 * 
 * @author Viktoras Agejevas
 * @version $Id: Console.java 16 2009-01-30 21:14:46Z v.agejevas $
 *
 */
public class Console {
	
	/**
	 * Number of retries (needed due to timeouts / network problems).
	 */
	public static int RETRIES = 3;
	
	/**
	 * Timeout. Packet is considered lost after this number of milliseconds.
	 */
	public static int TIMEOUT = 3000;
	
	
	public static void main(String[] args) {
		if (args.length == 0) {
			System.err.println("Usage: java -jar sntpjc-<version>.jar <ntp server>");
			System.exit(1);
		}

		Client cli = null;
		
		try {
			cli = new Client(args[0], TIMEOUT);
		} catch (UnknownHostException e) {
			System.err.println("Unknown host: " + args[0]);
			System.exit(3);
		}

		while (RETRIES > 0) {
			try {
				String host = cli.getAddress().getHostAddress();
				System.out.println("Checking with: " + args[0] + " ("+ host +")");
				System.out.println(String.format(
						"System time offset is: %.6f s", cli.getLocalOffset()));
				break;
			} catch (IOException e) {
				if (--RETRIES > 0) {
					System.out.println("Packet is lost, retrying...");
					continue;
				} else {
					System.err.println("No answer on 123 port!");
					System.exit(2);
				}
			}
		}
	}
}
