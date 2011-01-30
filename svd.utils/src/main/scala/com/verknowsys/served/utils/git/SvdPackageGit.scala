package com.verknowsys.served.utils

import org.eclipse.jgit.lib.{ObjectId, PersonIdent, Ref}
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.transport.URIish
import org.eclipse.jgit.transport.RemoteConfig


/**
 * Package object holding implicit conversions from JGit objects to its wrappers
 *
 * @author teamon
 */
package object git {
    implicit def RevCommitToCommit(c: RevCommit) = new Commit(c)

    implicit def PersonIdentToAuthor(c: PersonIdent) = new Author(c)

    implicit def StringToObjectId(s: String) = ObjectId.fromString(s)

    implicit def RefWrap(r: Ref) = new { 
        def name = r.getName
        def sha = ObjectId.toString(r.getObjectId) 
    }

    implicit def StringToGitRepository(s: String) = new GitRepository(s)

    implicit def StringToURIish(s: String) = new URIish(s)
    
    implicit def RemoteConfigWrap(rc: RemoteConfig) = new {
        def name = rc.getName
        def uris = rc.getURIs
    }
    
}
