$(function(){
    $(".api").each(function(i,e){
        var e = $(e);
        var rr = e.children(".highlight")
        
        e.append($("<div class='side'></div>").append("<h4>Request</h4>").append(rr[0]))
        e.append($("<div class='side'></div>").append("<h4>Response</h4>").append(rr[1]))
    })
})
