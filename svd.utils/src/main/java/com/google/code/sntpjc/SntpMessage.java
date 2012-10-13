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

import java.util.Arrays;

/**
 * SNTP Version 4 message according to RFC 2030.
 * Ignores fields irrelevant to SNTP.
 * 
 * @author Viktoras Agejevas
 * @version $Id: SntpMessage.java 9 2009-01-30 18:29:49Z v.agejevas $
 *
 */
public class SntpMessage {

	/**
	 * Leap Indicator (LI): This is a two-bit code warning of an impending
	 * leap second to be inserted/deleted in the last minute of the current
	 * day, with bit 0 and bit 1, respectively, coded as follows:
	 *
	 *    LI       Value     Meaning
	 *    -------------------------------------------------------
	 *    00       0         no warning
	 *    01       1         last minute has 61 seconds
	 *    10       2         last minute has 59 seconds)
	 *    11       3         alarm condition (clock not synchronized)
	 */
	private int leapIndicator;
	
	/**
	 * Version Number (VN): This is a three-bit integer indicating the
	 * NTP/SNTP version number. The version number is 3 for Version 3 (IPv4
	 * only) and 4 for Version 4 (IPv4, IPv6 and OSI). If necessary to
	 * distinguish between IPv4, IPv6 and OSI, the encapsulating context
	 * must be inspected.
	 */
	private int versionNumber = 3;
	
	/**
	 * Mode: This is a three-bit integer indicating the mode, with values
	 * defined as follows:
	 * 
	 * Mode     Meaning
	 * ------------------------------------
	 * 0        reserved
	 * 1        symmetric active
	 * 2        symmetric passive
	 * 3        client
	 * 4        server
	 * 5        broadcast
	 * 6        reserved for NTP control message
	 * 7        reserved for private use
	 * 
	 * In unicast and anycast modes, the client sets this field to 3
	 * (client) in the request and the server sets it to 4 (server) in the
	 * reply. In multicast mode, the server sets this field to 5
	 * (broadcast).
	 */
	private int mode = 3;
	
	/**
	 * Stratum: This is a eight-bit unsigned integer indicating the stratum
	 * level of the local clock, with values defined as follows:
	 * 
	 * Stratum  Meaning
	 * ----------------------------------------------
	 * 0        unspecified or unavailable
	 * 1        primary reference (e.g., radio clock)
	 * 2-15     secondary reference (via NTP or SNTP)
	 * 16-255   reserved
	 */
	private byte stratum = 3;
	
	/**
	 * Poll Interval: This is an eight-bit signed integer indicating the
	 * maximum interval between successive messages, in seconds to the
	 * nearest power of two. The values that can appear in this field
	 * presently range from 4 (16 s) to 14 (16284 s); however, most
	 * applications use only the sub-range 6 (64 s) to 10 (1024 s).
	 */
	private byte pollInterval;
	
	/**
	 * Precision: This is an eight-bit signed integer indicating the
	 * precision of the local clock, in seconds to the nearest power of two.
	 * The values that normally appear in this field range from -6 for
	 * mains-frequency clocks to -20 for microsecond clocks found in some
	 * workstations.
	 */
	private byte precision;
	
	/**
	 * Reference Timestamp: This is the time at which the local clock was
	 * last set or corrected, in 64-bit timestamp format.
	 */
	private Timestamp referenceTimestamp;
	
	/**
	 * Originate Timestamp: This is the time at which the request departed
	 * the client for the server, in 64-bit timestamp format.
	 */
	private Timestamp originateTimestamp;
	
	/**
	 * Receive Timestamp: This is the time at which the request arrived at
	 * the server, in 64-bit timestamp format.
	 */
	private Timestamp receiveTimestamp;
	
	/**
	 * Transmit Timestamp: This is the time at which the reply departed the
	 * server for the client, in 64-bit timestamp format.
	 */
	private Timestamp transmitTimestamp;
	
	/**
	 * Get SNTP message as byte array.
	 * Note that it ignores fields irrelevant to SNTP. 
	 * 
	 * @return byte array SNTP message
	 */
	public byte[] getByteArray() {
		byte[] message = new byte[48];
		
		message[0] = (byte) (leapIndicator << 6 | versionNumber << 3 | mode);
		message[1] = stratum;
		message[2] = pollInterval;
		message[3] = precision;
		
		byte[] original = originateTimestamp == null 
						  ? null 
						  : originateTimestamp.getByteArray();
		byte[] receive  = receiveTimestamp == null 
						  ? null 
						  : receiveTimestamp.getByteArray();
		byte[] transmit = transmitTimestamp == null 
						  ? null 
						  : transmitTimestamp.getByteArray();
		
		for (int i = 0; i < 8; i++) {
			message[24 + i] = original == null ? 0 : original[i];
			message[32 + i] = receive == null ? 0 : receive[i];
			message[40 + i] = transmit == null ? 0 : transmit[i];
		}
		
		return message;
	}
	
	/**
	 * Transform byte array SNTP message to SntpMessage object.
	 * 
	 * @param message byte array SNTP message
	 * @return SntpMessage
	 */
	public static SntpMessage valueOf(byte[] message) {
	    SntpMessage msg = new SntpMessage();
	    msg.setLeapIndicator((message[0] >> 6) & 0x03);
	    msg.setVersionNumber((message[0] >> 3) & 0x07);
	    msg.setMode(message[0] & 0x07);
	    msg.setStratum(message[1]);
	    msg.setPollInterval(message[2]);
	    msg.setPrecision(message[3]);
	    msg.setReferenceTimestamp(
	    		new Timestamp(Arrays.copyOfRange(message, 16, 24)));
	    msg.setOriginateTimestamp(
	    		new Timestamp(Arrays.copyOfRange(message, 24, 32)));
	    msg.setReceiveTimestamp(
	    		new Timestamp(Arrays.copyOfRange(message, 32, 40)));    
	    msg.setTransmitTimestamp(
	    		new Timestamp(Arrays.copyOfRange(message, 40, 48)));
		return msg;
	}

	public int getLeapIndicator() {
		return leapIndicator;
	}

	public void setLeapIndicator(int leapIndicator) {
		this.leapIndicator = leapIndicator;
	}

	public int getVersionNumber() {
		return versionNumber;
	}

	public void setVersionNumber(int versionNumber) {
		this.versionNumber = versionNumber;
	}

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	public byte getStratum() {
		return stratum;
	}

	public void setStratum(byte stratum) {
		this.stratum = stratum;
	}

	public byte getPollInterval() {
		return pollInterval;
	}

	public void setPollInterval(byte pollInterval) {
		this.pollInterval = pollInterval;
	}

	public byte getPrecision() {
		return precision;
	}

	public void setPrecision(byte precision) {
		this.precision = precision;
	}

	public Timestamp getReferenceTimestamp() {
		return referenceTimestamp;
	}

	public void setReferenceTimestamp(Timestamp referenceTimestamp) {
		this.referenceTimestamp = referenceTimestamp;
	}

	public Timestamp getOriginateTimestamp() {
		return originateTimestamp;
	}

	public void setOriginateTimestamp(Timestamp originateTimestamp) {
		this.originateTimestamp = originateTimestamp;
	}

	public Timestamp getReceiveTimestamp() {
		return receiveTimestamp;
	}

	public void setReceiveTimestamp(Timestamp receiveTimestamp) {
		this.receiveTimestamp = receiveTimestamp;
	}

	public Timestamp getTransmitTimestamp() {
		return transmitTimestamp;
	}

	public void setTransmitTimestamp(Timestamp transmitTimestamp) {
		this.transmitTimestamp = transmitTimestamp;
	}
}
