$ ->

  console.log("Running main code.")
  $('div.target').unbind().bind 'click', ->
    $.ajax
      url: "/GetUserProcesses"
      type: "POST"
      cache: false
      dataType: "html" # jsonp
      success: (data) ->
        console.log("Clicked target div, data is #{data}")
        $('div.target2').text(data)
      done:
        console.log("Done")
