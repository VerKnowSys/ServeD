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
        console.log("Clicked Authorize")
        $.post "/Authorize/0df25c85cef740557ee7be110d93fbdb7c899ad6", (data) ->
          console.log("Auth info: " + data.message)

        # $.ajax
        #   url: "/Authorize/0df25c85cef740557ee7be110d93fbdb7c899ad6" # sha1 of "dmilith"
        #   type: "post"
        #   contentType: "application/json"
        #   dataType: "json"
        #   cache: false
        #   async: false
        #   processData: false
        #   success: (data) =>
        #     console.log("Clicked Authorize")
        #     $('.ServicesResult').text(JSON.stringify data.message)
        #   done:
        #     console.log("Done")

      $('div.get_user_processes').unbind().bind 'click', -> # initialize click handlers on divs in Admin section
        $.post "/GetUserProcesses", (data) ->
          console.log("Clicked GetUserProcesses")
          $('div.services_result').text(JSON.stringify data.message)
          $('div.services_data').text(JSON.stringify data.content)


      $('div.get_stored_services').unbind().bind 'click', ->
        $.ajax
          url: "/GetStoredServices"
          type: "post"
          contentType: "application/json"
          dataType: "json"
          cache: false
          processData: false
          success: (data) =>
            console.log("Clicked GetStoredServices")
            $('div.services_result').text(JSON.stringify data.message)
            $('div.services_data').text(JSON.stringify data.content)
          done:
            console.log("Done")

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


