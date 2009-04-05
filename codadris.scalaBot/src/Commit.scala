// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package scalabot

import java.util.Date

class Commit(commitSha: String, unRead: Boolean) {
	
	require(commitSha.length != 0)
	
	val commitSha1 = commitSha
	var toRead = unRead
	var date = new Date
	
	def this(commitSha: String) = this(commitSha, true)
	
}