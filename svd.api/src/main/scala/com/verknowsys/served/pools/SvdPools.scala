package com.verknowsys.served.api.pools


object SvdPools {
    
    /**
     *  @author dmilith
     *
     *   userPortPool describes a range of user allowed pool of TCP ports
     */
    def userPortPool = 10000 until 60000
    
    
    /**
     *  @author dmilith
     *
     *   svdPortPool describes a range of root allowed pool of TCP ports
     */
    def svdPortPool = 0 until 1024
    
    
    /**
     *  @author dmilith
     *
     *   userUidPool describes a range of system uids for users
     */
    def userUidPool = 10000 until 60000
    

    /**
     *  @author dmilith
     *
     *   userGidPool describes a range of system gids for users
     */
    def userGidPool = 10000 until 60000

}


object SvdPoolRandomized {
    // PENDING
    def randomUserPort = 12
    def randomSystemPort = 13
    def randomUserUid = 14
    def randomUserGid = 15
    
}
