package com.verknowsys.served.api

case class Success(msg: String)
case class Error(msg: String)
case class CreateGitRepository(name: String)
case class RemoveGitRepository(name: String)
