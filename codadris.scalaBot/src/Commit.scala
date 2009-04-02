// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.

package scalabot

class Commit(commitSha: String, unRead: Boolean) {
	
	require(commitSha.length != 0)
	
	val commitSha1 = commitSha
	var toRead = unRead
	
	def this(commitSha: String) = this(commitSha, true)
	
}