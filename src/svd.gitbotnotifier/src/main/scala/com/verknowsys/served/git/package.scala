package com.verknowsys.served

import org.eclipse.jgit.lib.{ObjectId, PersonIdent}
import org.eclipse.jgit.revwalk.RevCommit

/**
 * Package object holding implicit conversions from JGit objects to its wrappers
 *
 * @author teamon
 */	
package object git {
	implicit def RevCommitToCommit(c: RevCommit) = new Commit(c)
	implicit def PersonIdentToAuthor(c: PersonIdent) = new Author(c)
	implicit def StringToObjectId(s: String) = ObjectId.fromString(s)
}
