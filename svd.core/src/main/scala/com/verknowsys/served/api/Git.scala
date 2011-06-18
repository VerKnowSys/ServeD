package com.verknowsys.served.api

import com.verknowsys.served.db.DB

/**
 * This factory objects for accessing database should not be exposed in API
 * so they are here
 */

package git {
    object RepositoryDB extends DB[Repository]
}
