package com.verknowsys.served.kqueue

/**
 * FileEvents kqueue API
 *	
 * @author teamon
 *	
 * kqueue VNODE flags
 *	
 * NOTE_DELETE    The unlink() system call was called on the file referenced by the descriptor.
 * NOTE_WRITE     A write occurred on the file referenced by the descriptor.
 * NOTE_EXTEND    The file referenced by the descriptor was extended.
 * NOTE_ATTRIB    The file referenced by the descriptor had its attributes changed.
 * NOTE_LINK      The link count on the file changed.
 * NOTE_RENAME    The file referenced by the descriptor was renamed.
 * NOTE_REVOKE    Access to the file was revoked via revoke(2) or the underlying fileystem was unmounted.
 */
object FileEvents {
	val kqueue = new Kqueue
	kqueue.start
	
	/**
	 * Watch for specified events
	 *
	 * Possible events:
	 * 	- modified
     *  - deleted
     *  - renamed
     * 
     *  - attributes changed
	 *
	 * @author teamon 
	 * @example
	 *      val watch = FileEvents.watch("/tmp/xx", modified = true){
	 *          println("File modified")
     *      }
     *      // ...
     *      watch.stop
	 * 
	 */
	def watch(path: String, modified: Boolean = false, 
	                        deleted: Boolean = false,
	                        renamed: Boolean = false,
	                        attributes: Boolean = false)(f: => Unit) = {

	    import Kqueue.CLibrary._
	    
		var flags = 0

		if(modified)    flags |= NOTE_WRITE | NOTE_EXTEND
		if(deleted)     flags |= NOTE_DELETE
		if(attributes)  flags |= NOTE_ATTRIB
		if(renamed)     flags |= NOTE_RENAME
		
		/**
		 * 
         * touch /tmp/aa        -> attributes
         * echo "x" > /tmp/aa   -> modified & attributes
         * echo "a" >> tmp/aa   -> modified
         * mv /tmp/aa /tmp/bb   -> renamed
         * rm /tmp/bb           -> deleted 
		 *
		 */
		
	    kqueue.registerEvent(path, flags, new KqueueListener(){
			def handle = f
		})
	}
	
	def main(args: Array[String]): Unit = {
		val watch = FileEvents.watch("/tmp/aa", modified = true){
			println("MODIFIED")
		}
		
		FileEvents.watch("/tmp/aa", deleted = true){
			println("DELETED")
		}
		
		FileEvents.watch("/tmp/aa", renamed = true){
			println("RENAMED")
		}
		
		FileEvents.watch("/tmp/aa", attributes = true){
			println("ATTRIBUTES")
		}
		
		Thread.sleep(5000)
		
		watch.stop
	}
}