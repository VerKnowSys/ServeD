$ ->
  console.log("Running main code.")

  $.ajax
    url: "/Header"
    type: "get"
    contentType: "text/html"
    dataType: "html"
    cache: false
    async: true
    processData: false
    success: (data) =>
      $('section.header').html(data)
    done:
      console.log("Done")


  apiCalls = -> [
      link: "Authorize"
      element: "div.authorize"
      params: "Authorize=0df25c85cef740557ee7be110d93fbdb7c899ad6"
    ,
      link: "GetUserProcesses"
      element: "div.get_user_processes"
      params: ""
    ,
      link: "RegisterDomain"
      element: "div.register_domain"
      params: "RegisterDomain=dupa123.fibi.tallica.pl"
    ,
    {link: "RegisteredDomains", element: "div.registered_domains", params: "" },
    {link: "GetStoredServices", element: "div.get_stored_services", params: "" },
    {link: "TerminateServices", element: "div.terminate_services", params: "" },
    {link: "StoreServices", element: "div.store_services", params: "" },
    {link: "SpawnService", element: "div.spawn_service", params: "SpawnService=Redis" },
    {link: "TerminateService", element: "div.terminate_service", params: "TerminateService=Redis" },
    {link: "ShowAvailableServices", element: "div.show_available_services", params: "" },
    {link: "SpawnServices", element: "div.spawn_services", params: "" },
    {link: "GetServiceStatus", element: "div.get_service_status", params: "GetServiceStatus=Redis" },
    {link: "GetServicePort", element: "div.get_service_port", params: "GetServicePort=Redis" },
    {link: "CloneIgniterForUser", element: "div.clone_igniter_for_user", params: "IgniterName=Redis&UserIgniterName=RedisDrugi" },
    {link: "RegisterAccount", element: "div.register_account", params: "RegisterAccount=zbyszek1337" }
  ]


  handleAPIAction = (link, element, params) ->
    $(element).unbind().bind 'click', ->
      $.ajax
        url: "/#{link}"
        type: "POST"
        # contentType: "application/x-www-form-urlencoded"
        dataType: "json"
        cache: false
        processData: false
        data:
          params
        success: (data) =>
          $('div.services_result').text(data.message)



  $.ajax
    url: "/AdminPanel"
    type: "get"
    contentType: "text/html"
    dataType: "html"
    cache: false
    async: true
    processData: false
    success: (data) =>
      $('section.admin').html(data)

      # Load all handlers for API elements:
      (handleAPIAction(element.link, element.element, element.params) for element in apiCalls())

