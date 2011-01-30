package com.verknowsys.served.utils.git

import org.eclipse.jgit.lib.PersonIdent

/**
 * JGit PersonIdent wrapper for more scala-like syntax
 *
 * @author teamon
 **/
class Author(val origin: PersonIdent) {
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

    /**
     * Returns authors`s name and email formatted as "name [email]"
     *
     * @author teamon
     */
    def nameAndEmail = "%s [%s]".format(name, email)
}
