---
title: File events
layout: default
---

## General information
`FileEventsManager` is an actor that takes care of receiving and updating kqueue file events. It is written using JNA to call `kqueue` and `kevent` BSD syscalls. It was made to be used only from within other actors.

## Usage
Below there is a description of recommended way of using `FileEventsManager` by mixing `FileEventsReactor` trait into any actor
{% highlight scala %}
class MyActor extends Actor with FileEventsReactor {
    // ...
}
{% endhighlight %}

To register a new file event call `registerFileEventFor` inside `preStart` method (**IMPORTANT!** It cannot be called from constructor)
{% highlight scala %}
override def preStart {
    registerFileEventFor("path/to/file", Modified)
}
{% endhighlight %}

This will register a new file event in **existing** `FileEventsManager` instance, thus it is important that when `MyActor` is started then there is already one running `FileEventsManager` actor.

When a file event occurs your actor will be notified with `FileEvent` message. You can handle it like this
{% highlight scala %}
def receive = {
    case FileEvent(path, flags) => // handle event
}
{% endhighlight %}

**IMPORTANT!** If you need to override `preRestart` or `postStop` hooks remember to call `super`. If you do not call this methods registered file events will never be unregistered.
{% highlight scala %}
override def preRestart(reason: Throwable){
    super.preRestart(reason) 
    // other code
}

override def postStop {
    super.postStop
    // other code
}
{% endhighlight %}
