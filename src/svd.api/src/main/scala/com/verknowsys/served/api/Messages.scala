package com.verknowsys.served.api

case class Success(val msg: String)
case class Error(val msg: String)
case class CreateGitRepository(val name: String)
case class RemoveGitRepository(val name: String)
