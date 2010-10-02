package com.verknowsys.served

import org.eclipse.jgit.lib.{ObjectId, PersonIdent, Ref}
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.transport.URIish

/**
 * Package object holding implicit conversions from JGit objects to its wrappers
 *
 * @author teamon
 */
package object git {
    implicit def RevCommitToCommit(c: RevCommit) = new Commit(c)

    implicit def PersonIdentToAuthor(c: PersonIdent) = new Author(c)

    implicit def StringToObjectId(s: String) = ObjectId.fromString(s)

    implicit def RefToObjectId(r: Ref) = r.getObjectId

    implicit def StringToGitRepository(s: String) = new GitRepository(s)

    implicit def StringToURIish(s: String) = new URIish(s)
}
