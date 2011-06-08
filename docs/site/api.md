---
title: API
layout: default
---

## Git

### Creating repository

<div class="api">
{% highlight scala %}
CreateRepository(
  name: String  // name of new repository
)
{% endhighlight %}

{% highlight scala %}
Success
RespositoryExistsError
{% endhighlight %}
</div>


### Removing repository

<div class="api">
{% highlight scala %}
RemoveRepository(
  uuid: UUID  // repository unique id
)
{% endhighlight %}

{% highlight scala %}
Success
RespositoryDoesNotExistError
{% endhighlight %}
</div>