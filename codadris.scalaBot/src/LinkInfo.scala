// © Copyright 2009 Daniel Dettlaff. ® All Rights Reserved.
// This Software is a close code project. You may not redistribute this code without permission of author.

package scalabot

import java.util.Date

class LinkInfo(p_author: String, p_channel: String, p_message: String) {
	
	require(p_author.length > 0)
	require(p_channel.length > 0)
	require(p_message.length > 0)
	
	val author = p_author
	val channel = p_channel
	val message = p_message
	var date = new Date
	
}