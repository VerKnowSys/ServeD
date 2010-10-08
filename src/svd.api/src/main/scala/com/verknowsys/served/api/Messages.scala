package com.verknowsys.served.api

// ServeD -> Client messages
case class Success(val msg: String)
case class Notice(val msg: String)
case class Error(val msg: String)

// Client -> ServeD messages
case class CreateGitRepository(val name: String)
case class RemoveGitRepository(val name: String)
