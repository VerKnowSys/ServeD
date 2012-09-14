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

import java.util.Date;

/**
 * NTP Timestamp according to RFC-2030.
 * 
 * It's main purpose is to represent timestamp as 64 bit byte array.
 * First 4 bytes are integer seconds part, other 4 bytes are seconds fraction.
 * 
 * @author Viktoras Agejevas
 * @version $Id: Timestamp.java 31 2009-02-05 06:13:48Z v.agejevas $
 *
 */
public class Timestamp {

	/**
	 * Byte array that holds binary representation of this timestamp.
	 */
	private byte[] data = new byte[8];
	
	/**
	 * Number of seconds since 0h on 1 January 1900.
	 */
	private double ntpTimestamp;
	
	/**
	 * Seconds between NTP and POSIX time (1 January 1900 -- 1 January 1970)
	 */
	public static final double UNIX_TIME_CORRECTION = 2208988800.0;
	
	/**
	 * Maximal seconds value that can be represented as 4 bytes - 2^32
	 */
	public static final double NTP_SCALE = 4294967296.0;


	/**
	 * Construct NTP timestamp with unix timesamp in milliseconds.
	 * Encodes byte array representation of timestamp.
	 * 
	 * @param timestamp unix timestamp in milliseconds
	 */
	public Timestamp(long timestamp) {
		this(timestamp / 1000.0);
	}
	
	/**
	 * Construct NTP timestamp with unix timesamp in seconds.
	 * Encodes byte array representation of timestamp.
	 * 
	 * @param timestamp unix timestamp in seconds
	 */
	public Timestamp(double timestamp) {
		ntpTimestamp = timestamp + UNIX_TIME_CORRECTION;
		int k = 0;

		double d = ntpTimestamp / NTP_SCALE;
		for (int i = 0; i < 8; ++i) {
			if ((k = (int) (d *= 256.0)) >= 256) {
				k = 255;
			}
			data[i] = (byte) k;
			d -= k;
		}
	}

	/**
	 * Construct NTP timestamp with 64 bit byte array.
	 * Decodes 64 bit byte array timestamp to double value.
	 * 
	 * @param data 64 bit byte array representing NTP timestamp
	 */
	public Timestamp(byte[] data) {
		if (data == null || data.length != 8) {
			throw new IllegalArgumentException(
					"Byte array must be exactly 64 bits");
		}

		this.data = data;
		double d = 0.0;

		for (int i = 0; i < 8; ++i) {
			d = 256.0 * d + byteToUnsignedInt(data[i]);
		}

		ntpTimestamp = d / NTP_SCALE;
	}
	
	/**
	 * Convert byte to unsigned value 0 - 255.
	 * 
	 * @param b
	 * @return unsigned byte value
	 */
	private int byteToUnsignedInt(byte b) {
		return (b < 0) ? 256 + b : b;
	}

	/**
	 * Get byte array representation of NTP timestamp.
	 * 
	 * @return 64 bit byte array timestamp
	 */
	public byte[] getByteArray() {
		return data;
	}

	/**
	 * Get NTP timestamp in seconds.
	 * 
	 * @return NTP timestamp in seconds
	 */
	public double getTimestamp() {
		return ntpTimestamp;
	}
	
	/**
	 * Get this timestamp represented as POSIX time.
	 * 
	 * @return unix timestamp in seconds
	 */
	public double getUnixTimestamp() {
		return getTimestamp() - UNIX_TIME_CORRECTION;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("Unix timestamp: ");
		b.append(getUnixTimestamp());
		b.append(", NTP timestamp: ");
		b.append(getTimestamp());
		b.append("Date representation: ");
		b.append(new Date((long)getUnixTimestamp()*1000));
		return b.toString();
	}

}
