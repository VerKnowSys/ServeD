$ ->
  console.log("Running main code.")

  $.ajax
    url: "/GetUserProcesses"
    type: "post"
    contentType: "application/json"
    dataType: "json"
    cache: true
    processData: false
    success: (data) =>
      destData = JSON.parse(JSON.stringify(data))
      console.log("Clicked target div, data is #{destData} -> #{destData.message}")
      $('div.target2').text(destData.message)
    done:
      console.log("Done")

  $.ajax
    url: "/Header"
    type: "get"
    contentType: "text/html"
    dataType: "html"
    cache: false
    processData: false
    success: (data) =>
      $('section.header').html(data)
    done:
      console.log("Done")


  $.ajax
    url: "/ProcList"
    type: "get"
    contentType: "text/html"
    dataType: "html"
    cache: false
    processData: false
    success: (data) =>
      $('section.pslist').html(data)
    done:
      console.log("Done")

