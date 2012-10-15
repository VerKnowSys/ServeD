$ ->

  console.log("Running main code")
  $('div.target').unbind().bind 'click', ->
    console.log("Clicked target div")

