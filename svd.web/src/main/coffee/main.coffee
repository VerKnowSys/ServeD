$ ->
  console.log("Running main code.")

  $.ajax
    url: "/Header"
    type: "get"
    contentType: "text/html"
    dataType: "html"
    cache: false
    # async: false
    processData: false
    success: (data) =>
      $('section.header').html(data)
    done:
      console.log("Done")


  $.ajax
    url: "/AdminPanel"
    type: "get"
    contentType: "text/html"
    dataType: "html"
    cache: false
    # async: false
    processData: false
    success: (data) =>
      $('section.admin').html(data)
      $("div.authorize").unbind().bind 'click', ->
        $.ajax
          url: "/Authorize"
          type: "POST"
          contentType: "application/x-www-form-urlencoded"
          dataType: "json"
          cache: false
          processData: false
          data:
            "Authorize=0df25c85cef740557ee7be110d93fbdb7c899ad6" # sha1 of "dmilith"}
          success: (data) =>
            $('div.services_result').text(data.message)

      $('div.get_user_processes').unbind().bind 'click', ->
        $.ajax
          url: "/GetUserProcesses"
          type: "post"
          contentType: "application/x-www-form-urlencoded"
          dataType: "json"
          cache: false
          processData: false
          success: (data) =>
            console.log("Clicked GetUserProcesses")
            $('div.services_result').text(JSON.stringify data.message)
            $('div.services_data').text(JSON.stringify data.content)

      $('div.get_stored_services').unbind().bind 'click', ->
        $.ajax
          url: "/GetStoredServices"
          type: "post"
          contentType: "application/x-www-form-urlencoded"
          dataType: "json"
          cache: false
          processData: false
          success: (data) =>
            console.log("Clicked GetStoredServices")
            $('div.services_result').text(JSON.stringify data.message)
            $('div.services_data').text(JSON.stringify data.content)

      $('div.register_domain').unbind().bind 'click', ->
        $.ajax
          url: "/RegisterDomain"
          type: "post"
          contentType: "application/x-www-form-urlencoded"
          dataType: "json"
          cache: false
          data:
            "RegisterDomain=zdupy.fibi.tallica.pl"
          processData: false
          success: (data) =>
            console.log("Clicked RegisterDomain")
            $('div.services_result').text(JSON.stringify data.message)
            $('div.services_data').text(JSON.stringify data.content)

      $('div.store_services').unbind().bind 'click', ->
        $.ajax
          url: "/StoreServices"
          type: "post"
          contentType: "application/x-www-form-urlencoded"
          dataType: "json"
          cache: false
          processData: false
          success: (data) =>
            $('div.services_result').text(JSON.stringify data.message)
            $('div.services_data').text(JSON.stringify data.content)

      $('div.terminate_services').unbind().bind 'click', ->
        $.ajax
          url: "/TerminateServices"
          type: "post"
          contentType: "application/x-www-form-urlencoded"
          dataType: "json"
          cache: false
          processData: false
          success: (data) =>
            $('div.services_result').text(JSON.stringify data.message)
            $('div.services_data').text(JSON.stringify data.content)

      $('div.spawn_services').unbind().bind 'click', ->
        $.ajax
          url: "/SpawnServices"
          type: "post"
          contentType: "application/x-www-form-urlencoded"
          dataType: "json"
          cache: false
          processData: false
          success: (data) =>
            $('div.services_result').text(JSON.stringify data.message)
            $('div.services_data').text(JSON.stringify data.content)

      $('div.spawn_service').unbind().bind 'click', ->
        $.ajax
          url: "/SpawnService"
          type: "post"
          contentType: "application/x-www-form-urlencoded"
          dataType: "json"
          cache: false
          data:
            "SpawnService=Redis"
          processData: false
          success: (data) =>
            $('div.services_result').text(JSON.stringify data.message)
            $('div.services_data').text(JSON.stringify data.content)


      $('div.terminate_service').unbind().bind 'click', ->
        $.ajax
          url: "/TerminateService"
          type: "post"
          contentType: "application/x-www-form-urlencoded"
          dataType: "json"
          cache: false
          data:
            "TerminateService=Redis"
          processData: false
          success: (data) =>
            $('div.services_result').text(JSON.stringify data.message)
            $('div.services_data').text(JSON.stringify data.content)


      $('div.get_service_status').unbind().bind 'click', ->
        $.ajax
          url: "/GetServiceStatus"
          type: "post"
          contentType: "application/x-www-form-urlencoded"
          dataType: "json"
          cache: false
          data:
            "GetServiceStatus=Redis"
          processData: false
          success: (data) =>
            $('div.services_result').text(JSON.stringify data.message)
            $('div.services_data').text(JSON.stringify data.content)


      $('div.show_available_services').unbind().bind 'click', ->
        $.ajax
          url: "/ShowAvailableServices"
          type: "post"
          contentType: "application/x-www-form-urlencoded"
          dataType: "json"
          cache: false
          processData: false
          success: (data) =>
            $('div.services_result').text(JSON.stringify data.message)
            $('div.services_data').text(JSON.stringify data.content)


      $('div.clone_igniter_for_user').unbind().bind 'click', ->
        $.ajax
          url: "/CloneIgniterForUser"
          type: "post"
          contentType: "application/x-www-form-urlencoded"
          dataType: "json"
          cache: false
          data:
            "UserIgniterName=RedisDrugiPamPam&IgniterName=Redis"
          processData: false
          success: (data) =>
            $('div.services_result').text(JSON.stringify data.message)
            $('div.services_data').text(JSON.stringify data.content)


      $('div.register_account').unbind().bind 'click', ->
        $.ajax
          url: "/RegisterAccount"
          type: "post"
          contentType: "application/x-www-form-urlencoded"
          dataType: "json"
          cache: false
          data:
            "RegisterAccount=zdzisław"
          processData: false
          success: (data) =>
            $('div.services_result').text(JSON.stringify data.message)
            $('div.services_data').text(JSON.stringify data.content)



    # $('.GetStoredServices').unbind().bind 'click', ->
    #   $.ajax
    #     url: "/GetStoredServices"
    #     type: "post"
    #     contentType: "application/json"
    #     dataType: "json"
    #     cache: false
    #     async: false
    #     processData: false
    #     success: (data) =>
    #       console.log("Clicked GetStoredServices")
    #       $('div.ServicesResult').text(JSON.stringify data)
    #     done:
    #       console.log("Done")





  # $.ajax
  #   url: "/ProcList"
  #   type: "get"
  #   contentType: "text/html"
  #   dataType: "html"
  #   cache: false
  #   async: false
  #   processData: false
  #   success: (data) =>
  #     $('section.pslist').html(data)
  #   done:
  #     console.log("Done")


