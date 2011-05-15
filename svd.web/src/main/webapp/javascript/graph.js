$(document).ready(function(){
    var Renderer = function(canvas){
        var canvas = $(canvas).get(0)
        var ctx = canvas.getContext("2d")
        var particleSystem
        
        var that = {
            init: function(system){
                particleSystem = system
                particleSystem.screenSize(canvas.width, canvas.height)
                particleSystem.screenPadding(80)
            },
            

            colors: {
                "SupervisorActor": "#A71717",
                "SvdAccountsManager": "#A76D17",
                "SvdAccountManager": "#8AA717",
                "SvdGitManager": "#34A717",
                "SvdGatherer": "#17A751",
                "SvdFileEventsManager": "#17A7A7",
                "SvdSystemManager": "#1751A7",
                "SvdMaintainer": "#3417A7"
            },
            redraw: function(){
                ctx.textAlign = "center"
                ctx.font = "9pt Menlo"
                ctx.fillStyle = "#222"
                ctx.fillRect(0,0, canvas.width, canvas.height)
                
                particleSystem.eachEdge(function(edge, pt1, pt2){
                    ctx.strokeStyle = "#888"
                    ctx.lineWidth = 1
                    ctx.beginPath()
                    ctx.moveTo(pt1.x, pt1.y)
                    ctx.lineTo(pt2.x, pt2.y)
                    ctx.stroke()
                })
                
                particleSystem.eachNode(function(node, pt){
                    ctx.strokeStyle = node.data.state == "run" ? "#fff" : "#aaa"
                    ctx.fillStyle = that.colors[node.data.name] || "555"
                    ctx.lineWidth = 2
                                     
                    var rad = node.data.name == "SupervisorActor" ? 20 : 10 
                                       
                    // circle
                    ctx.beginPath()
                    ctx.arc(pt.x, pt.y, rad, 0, Math.PI*2, true);
                    ctx.fill()
                    ctx.stroke()
                    
                    // name
                    ctx.fillStyle = "rgba(100,100,100,0.9)"
                    ctx.fillText(node.data.name || "", pt.x, pt.y+ 2*rad)
                })
            }
        }
        
        return that
    }
    
    
    var sys = arbor.ParticleSystem(2000, 600, 0.2)
    sys.parameters({gravity: true})
    sys.renderer = Renderer("#graph")
    
    var current_state = 0
    
    
    function nextEvent(){
        if(current_state == LogData.length) {
            return null
        }
        
        var ev = LogData[current_state]
        
        if(ev.ev == "started"){
            console.log("started " + ev.id)
            
            var node = sys.getNode(ev.id)
            if(node) node.data.state = "run"
            else sys.addNode(ev.id, {name: ev.cls, state: "run"})
        } else if(ev.ev == "linked"){
            console.log("linked " + ev.aid + " to " + ev.bid)
            sys.addEdge(ev.aid, ev.bid)
            sys.getNode(ev.aid).data.name = ev.acls
            sys.getNode(ev.bid).data.name = ev.bcls
        }
        
        current_state++
        return ev
    }
    
    

    
    // sys.addEdge('a', 'b')
    // sys.addEdge('a', 'c')
    // sys.addEdge('a', 'd')
    // sys.addNode('f', {alone: true})
    
    $("#next_state").click(function(){
        if(current_state == 0){
        //     for(;;){
                var event = nextEvent()
        //         if(event == null) break;
                console.log(event)
        //         // if(event.acls == "SupervisorActor" || event.bcls == "SupervisorActor" || event.cls == "SupervisorActor") break
        //     }
        }
        
        nextEvent()
    })
    
    $("#full_state").click(function(){
        while(nextEvent()){}
    })
    
})
