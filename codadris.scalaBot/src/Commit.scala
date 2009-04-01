package scalabot

class Commit(commitMsg: String, branchName: String) {
	
	require(commitMsg.length != 0)
	require(branchName.length != 0)
	
	val commitMessage = commitMsg
	val branch = branchName
	
	def this(commitMsg: String) = this(commitMsg, "master")
	
}