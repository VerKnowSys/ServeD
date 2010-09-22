// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package com.verknowsys.served.utils.jar.comparator

import _root_.java.util.jar.{JarEntry, JarFile}
import java.util.zip.ZipException

/**
 * User: dmilith
 * Date: Jun 18, 2009
 * Time: 8:53:22 PM
 */

case class JarFileFields( val name_and_path: String, val crc: Long)

class JarEntryComparator {
	
	private var p_elements = List[JarFileFields]()
	private var p_elements2 = List[JarFileFields]()
	private var broken = false

	val ignoreList = List( "pom.properties", "MANIFEST.MF", "VERKNOWS.SF" )

	def add(t: JarFileFields) = p_elements = p_elements ::: List(t) ::: Nil

	def add2(t: JarFileFields) = p_elements2 = p_elements2 ::: List(t) ::: Nil

	def elements = p_elements

	def elements2 = p_elements2

	def diff = p_elements2 -- p_elements

	def diff_? = if (!broken && diff == List() && size == size2) false else true
	           
	def size = p_elements size

	def size2 = p_elements2 size

	def loadAndThrowListOfCrcs(file :String): List[String] = {
		var outList = List[String]()
		try {
			val jarFile = new JarFile(file)
			val entries = jarFile.entries
			while (entries.hasMoreElements) {
				entries.nextElement match {
					case ne: JarEntry => {
						if (!ne.isDirectory) {
							outList ::= ne.getCrc.toString.trim
						}
					}
				}
			}
		} catch {
			case x: ZipException => {
				return List()
			}
		}
		outList
	}

	def load(file: String, file2: String) = {
		try {
			val jarFile = new JarFile(file)
			val jarFile2 = new JarFile(file2)
			val entries = jarFile entries
			val entries2 = jarFile2 entries

			while (entries.hasMoreElements) {
				entries.nextElement match {
					case ne: JarEntry => {
						if (!ne.isDirectory) {
							ignoreList.foreach( element =>
									if (ne.getName.contains(element)) {
//										logger.info("Ignoring " + element)
									} else {
										add(new JarFileFields(ne.getName, ne.getCrc))
									}
								)
						}
					}
				}
			}
			while (entries2.hasMoreElements) {
				entries2.nextElement match {
					case ne: JarEntry => {
						if (!ne.isDirectory) {
							ignoreList.foreach( element =>
									if (ne.getName.contains(element)) {
//										logger.info("Ignoring " + element)
									} else {
										add2(new JarFileFields(ne.getName, ne.getCrc))
									}
								)
						}
					}
				}
			}
		} catch {
			case x: ZipException => {
				broken = true
			}
		}
	}
	

}