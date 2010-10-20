/* 
 * Copyright 2010 Sanjay Dasgupta, sanjay.dasgupta@gmail.com
 * 
 * This file is part of SNA.
 *
 * SNA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 3 
 * of the License, or (at your option) any later version.
 *
 * SNA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with SNA.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.java.dev.sna.examples

import net.java.dev.sna.SNA

class ST (
		var wYear: Short = 0,
		var wMonth: Short = 0,
		var wDayOfWeek: Short = 0,
		var wDay: Short = 0,
		var wHour: Short = 0,
		var wMinute: Short = 0,
		var wSecond: Short = 0,
		var wMilliseconds: Short = 0) extends com.sun.jna.Structure

object Test01 extends SNA {

	snaLibrary = "Kernel32"
	val GetSystemTime = SNA[ST, Unit]
	val Beep = SNA[Int, Int, Unit]
	val GetLogicalDrives = SNA[Int]

	def main(args : Array[String]) {
		Beep(500, 100)
		println(GetLogicalDrives())
		val st = new ST
		GetSystemTime(st)
		printf("Date: %d-%d-%d Time: %d:%d:%d.%d GMT%n", st.wYear, st.wMonth, 
				st.wDay, st.wHour, st.wMinute, st.wSecond, st.wMilliseconds)
	}
}
