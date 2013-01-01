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
    {link: "GetRegisteredDomains", element: "div.registered_domains", params: "" },
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
    {link: "RegisterAccount", element: "div.register_account", params: "RegisterAccount=zbyszek1337" },
    {link: "CreateFileWatch", element: "div.create_file_watch", params: "CreateFileWatch=Src/index&Flags=127&ServiceName=Memcached" },
    {link: "CreateFileWatch", element: "div.create_file_watch1", params: "CreateFileWatch=Src/some64&Flags=1&ServiceName=Nginx" },
    {link: "CreateFileWatch", element: "div.create_file_watch2", params: "CreateFileWatch=Src/jakieśtakieśzótęłów&Flags=16&ServiceName=Mongodb" },
    {link: "CreateFileWatch", element: "div.create_file_watch3", params: "CreateFileWatch=Src/zdzibrub&Flags=32&ServiceName=RedisDrugi" },

    {link: "DestroyFileWatch", element: "div.destroy_file_watch", params: "DestroyFileWatch=Src/index" },
    {link: "DestroyFileWatch", element: "div.destroy_file_watch1", params: "DestroyFileWatch=Src/some2" },
    {link: "DestroyFileWatch", element: "div.destroy_file_watch2", params: "DestroyFileWatch=Src/jakieśtakieśzótęłów" },
    {link: "DestroyFileWatch", element: "div.destroy_file_watch3", params: "DestroyFileWatch=Src/zdzibrub" },
    {link: "GetAccountPriviledges", element: "div.account_security_pass", params: ""},
    {link: "RestartAccountManager", element: "div.restart_account_manager", params: ""},
    {link: "RemoveAllReservedPorts", element: "div.remove_all_reserved_ports", params: ""},
    {link: "RegisterUserPort", element: "div.register_user_port", params: "RegisterUserPort=12344"},
    {link: "GetUserPorts", element: "div.get_user_ports", params: ""}
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
          $('div.services_result').text(data?.message)
          $('div.services_data').text(data?.content)



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

