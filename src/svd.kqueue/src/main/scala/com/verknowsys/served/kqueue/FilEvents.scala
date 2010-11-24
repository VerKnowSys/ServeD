package com.verknowsys.served.kqueue

object FileEvents {
	val kqueue = new Kqueue
	kqueue.start
	
	def watch(path: String, delete: Boolean = false,
							write: Boolean = false,
							modify: Boolean = false,
							extend: Boolean = false,
							attrib: Boolean = false,
							link: Boolean = false,
							rename: Boolean = false,
							revoke: Boolean = false)(f: => Unit) = {
		var flags = 0
			
		if(delete) flags |= Kqueue.E_DELETE
		if(write) flags |= Kqueue.E_WRITE	
		if(extend) flags |= Kqueue.E_EXTEND
		if(attrib) flags |= Kqueue.E_ATTRIB
		if(link) flags |= Kqueue.E_LINK	
		if(rename) flags |= Kqueue.E_RENAME
		if(revoke) flags |= Kqueue.E_REVOKE
		if(modify) flags |= Kqueue.E_WRITE | Kqueue.E_EXTEND
		
		
		kqueue.registerEvent(path, flags, new KqueueListener(){
			def handle = f
		})
	}
}