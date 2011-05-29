var AkkaActors = function(){
    function Renderer(canvas){
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
        
    function parseActors(list, parent){
        $.each(list, function(i,e){
            var node = sys.addNode(e.uuid, {name: shortClassName(e.className)})
            if(parent){
                sys.addEdge(node, parent)
            }
            parseActors(e.linkedActors, node)
        })
    }
    
    function shortClassName(className){
        return className.split(".").reverse()[0];
    }
    
    var sys = arbor.ParticleSystem(2000, 600, 0.2)
    sys.parameters({gravity: true})
    sys.renderer = Renderer("#graph")

    parseActors(LogData)
}


$(document).ready(function(){
   AkkaActors();
})
