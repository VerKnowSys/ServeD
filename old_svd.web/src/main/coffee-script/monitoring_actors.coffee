class AkkaActors
    constructor: (id)->
        @sys = arbor.ParticleSystem(2000, 600, 0.2)
        @sys.parameters({gravity: true})
        @sys.renderer = new AkkaActorsRenderer(id)

    renderer: () -> new AkkaActorsRenderer

    parseActors: (list, parent) ->
        for e in list
            node = @sys.addNode(e.uuid, name: @shortClassName(e.className))
            @sys.addEdge(node, parent) if parent
            @parseActors(e.linkedActors, node)

    shortClassName: (className) ->
        className.split(".").reverse()[0];

    run: (data) -> 
        @parseActors(data)


class AkkaActorsRenderer
    constructor: (canvas) ->
        @canvas = $(canvas).get(0)
        @ctx = @canvas.getContext("2d")
        
    init: (@particleSystem) ->
        @particleSystem.screenSize(@canvas.width, @canvas.height)
        @particleSystem.screenPadding(80)
        
    colors:
        "SupervisorActor":      "#A71717"
        "SvdAccountsManager":   "#A76D17"
        "SvdAccountManager":    "#8AA717"
        "SvdGitManager":        "#34A717"
        "SvdGatherer":          "#17A751"
        "SvdFileEventsManager": "#17A7A7"
        "SvdSystemManager":     "#1751A7"
        "SvdMaintainer":        "#3417A7"
        
    redraw: () ->
        @ctx.textAlign = "center"
        @ctx.font = "9pt Menlo"
        @ctx.fillStyle = "#222"
        @ctx.fillRect(0 ,0, @canvas.width, @canvas.height)
        
        @particleSystem.eachEdge (edge, pt1, pt2) =>
            @ctx.strokeStyle = "#888"
            @ctx.lineWidth = 1
            @ctx.beginPath()
            @ctx.moveTo(pt1.x, pt1.y)
            @ctx.lineTo(pt2.x, pt2.y)
            @ctx.stroke()

        @particleSystem.eachNode (node, pt) =>
            @ctx.strokeStyle = node.data.state == "run" ? "#fff" : "#aaa"
            @ctx.fillStyle = @colors[node.data.name] || "555"
            @ctx.lineWidth = 2
            
            rad = if node.data.name == "SupervisorActor" then 20 else 10 
            
            # circle
            @ctx.beginPath()
            @ctx.arc(pt.x, pt.y, rad, 0, Math.PI*2, true);
            @ctx.fill()
            @ctx.stroke()
            
            # name
            @ctx.fillStyle = "rgba(100,100,100,0.9)"
            @ctx.fillText(node.data.name || "", pt.x, pt.y+ 2*rad)
            
$ ->
   new AkkaActors("#graph").run(LogData)

