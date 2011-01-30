package com.verknowsys.served.utils.git

import org.eclipse.jgit.lib.PersonIdent

/**
 * JGit PersonIdent wrapper for more scala-friendly syntax
 *
 * @author teamon
 **/
class Author(val origin: PersonIdent) {
    def this(name: String, email: String) = this(new PersonIdent(name, email))
    /**
     * Returns authors`s name
     *
     * @author teamon
     */
    def name = origin.getName

    /**
     * Returns authors`s email
     *
     * @author teamon
     */
    def email = origin.getEmailAddress
}
