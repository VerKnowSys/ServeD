
# $ ->
#   console.log("Running ProcList code.")
#   paper = Raphael("holder")
#   elements_set = paper.set()
#   $.ajax
#     url: "/GetUserProcesses"
#     type: "post"
#     contentType: "application/json"
#     dataType: "json"
#     cache: true
#     processData: false
#     success: (data) =>
#       destData = JSON.parse(JSON.stringify(data))
#       $('div.target2').text(destData.message)
#       destData.content.map (elem) ->
#         # console.log("elem: #{elem.rss/1024/1024}")
#         rand_no = Math.random()
#         rand_no = rand_no * 1000
#         rand_no2 = Math.random()
#         rand_no2 = rand_no2 * 1000
#         elements_set.push paper.circle(rand_no % 500 + 100, rand_no2 % 400 + 100, elem.rss/2/1024/1024+10).attr
#             fill: "##{elem.rss % 99}FF#{elem.rss/1024 % 99}"
#           .attr "obj", elem
#         # elements_set.forEach (elem) ->
#           # console.log("ELEM: #{elem.pid} #{elem.ppid}")

#   setInterval ->
#       $.ajax
#         url: "/GetUserProcesses"
#         type: "post"
#         contentType: "application/json"
#         dataType: "json"
#         cache: true
#         processData: false
#         success: (data) =>
#           destData = JSON.parse(JSON.stringify(data))
#           $('div.target2').text(destData.message)
#           #     if($.inArray(el, uniqueNames) === -1) uniqueNames.push(el);


#           # elements_set.push paper.path("")
#           # elements_set.glow
#           #   widthnumber: 5
#           #   opacitynumber: 0.5

#           # console.log("Data Loaded: #{elements}")

#         done:
#           console.log("Done")
#     , 20000
