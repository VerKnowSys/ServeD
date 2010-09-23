package com.verknowsys.served

import org.eclipse.jgit.lib.{ObjectId, PersonIdent}
import org.eclipse.jgit.revwalk.RevCommit
	
package object git {
	implicit def RevCommitToCommit(c: RevCommit) = new Commit(c)
	implicit def PersonIdentToAuthor(c: PersonIdent) = new Author(c)
	implicit def StringToObjectId(s: String) = ObjectId.fromString(s)
}
