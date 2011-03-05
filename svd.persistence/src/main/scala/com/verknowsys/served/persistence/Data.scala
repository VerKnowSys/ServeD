package com.verknowsys.served.persistence

case class AccountConfig(uuid: UUID, projects: List[ProjectConfig])

case class ProjectConfig(name: String, )




// abstract class ModuleConfig(username: String)
// case class CIConfig(username: String, gitpath: String, ...) extends ModuleConfig(username)