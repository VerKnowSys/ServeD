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
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Very basic SNTP client.
 * 
 * @author Viktoras Agejevas
 * @version $Id: Client.java 35 2009-04-27 17:56:51Z v.agejevas $
 *
 */
public class Client {
	
	/**
	 * The packet will be considered lost after this number of milliseconds.
	 */
	private int timeout = 4000;

	/**
	 * NTP server.
	 */
	private InetAddress address;
	
	/**
	 * Create SntpClient.
	 * 
	 * @param server NTP server host name, ntp.nasa.gov for example
	 * @param timeout interval in milliseconds after which the packet 
	 * 	      is considered lost
	 * @throws UnknownHostException 
	 */
	public Client(String server, int timeout) throws UnknownHostException {
		this.timeout = timeout;
		address = InetAddress.getByName(server);
	}

	/**
	 * Create SntpClient.
	 * 
	 * @param server NTP server host name, ntp.nasa.gov for example
	 * @throws UnknownHostException 
	 */
	public Client(String server) throws UnknownHostException {
		address = InetAddress.getByName(server);
	}
	
	/**
	 * Connects to NTP server and counts local clock offset.
	 * 
	 * @return offset in seconds
	 * @throws IOException on timeout or connection problems
	 */
	public double getLocalOffset() throws IOException {
		DatagramSocket socket = null;
		
		try {
			socket = new DatagramSocket();
			socket.setSoTimeout(timeout);
			SntpMessage msg = new SntpMessage();
			msg.setTransmitTimestamp(new Timestamp(System.currentTimeMillis()));
			DatagramPacket out = 
				new DatagramPacket(msg.getByteArray(), 48, address, 123);
			socket.send(out);
			
			byte[] answer = new byte[48];
			DatagramPacket in = new DatagramPacket(answer, 48);
			socket.receive(in);
			msg = SntpMessage.valueOf(in.getData());
			
			double receive = msg.getReceiveTimestamp().getTimestamp();
			double originate = msg.getOriginateTimestamp().getTimestamp();
			double transmit = msg.getTransmitTimestamp().getTimestamp();
			double destination = 
				new Timestamp(System.currentTimeMillis()).getTimestamp();
			
			return ((receive - originate) + (transmit - destination)) / 2.0;
		} finally {
			if (socket != null) {
				socket.close();
			}
		}
	}
	
	/**
	 * Get UDP packet timeout setting.
	 * 
	 * @return timeout in milliseconds
	 */
	public int getTimeout() {
		return timeout;
	}

	/**
	 * Set UDP packet timeout setting.
	 * 
	 * @param timeout in milliseconds
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public InetAddress getAddress() {
		return address;
	}

	public void setAddress(InetAddress address) {
		this.address = address;
	}
	
}
