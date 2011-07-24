(function() {
  var AkkaActors, AkkaActorsRenderer;
  var __bind = function(fn, me){ return function(){ return fn.apply(me, arguments); }; };
  AkkaActors = (function() {
    function AkkaActors(id) {
      this.sys = arbor.ParticleSystem(2000, 600, 0.2);
      this.sys.parameters({
        gravity: true
      });
      this.sys.renderer = new AkkaActorsRenderer(id);
    }
    AkkaActors.prototype.renderer = function() {
      return new AkkaActorsRenderer;
    };
    AkkaActors.prototype.parseActors = function(list, parent) {
      var e, node, _i, _len, _results;
      _results = [];
      for (_i = 0, _len = list.length; _i < _len; _i++) {
        e = list[_i];
        node = this.sys.addNode(e.uuid, {
          name: this.shortClassName(e.className)
        });
        if (parent) {
          this.sys.addEdge(node, parent);
        }
        _results.push(this.parseActors(e.linkedActors, node));
      }
      return _results;
    };
    AkkaActors.prototype.shortClassName = function(className) {
      return className.split(".").reverse()[0];
    };
    AkkaActors.prototype.run = function(data) {
      return this.parseActors(data);
    };
    return AkkaActors;
  })();
  AkkaActorsRenderer = (function() {
    function AkkaActorsRenderer(canvas) {
      this.canvas = $(canvas).get(0);
      this.ctx = this.canvas.getContext("2d");
    }
    AkkaActorsRenderer.prototype.init = function(particleSystem) {
      this.particleSystem = particleSystem;
      this.particleSystem.screenSize(this.canvas.width, this.canvas.height);
      return this.particleSystem.screenPadding(80);
    };
    AkkaActorsRenderer.prototype.colors = {
      "SupervisorActor": "#A71717",
      "SvdAccountsManager": "#A76D17",
      "SvdAccountManager": "#8AA717",
      "SvdGitManager": "#34A717",
      "SvdGatherer": "#17A751",
      "SvdFileEventsManager": "#17A7A7",
      "SvdSystemManager": "#1751A7",
      "SvdMaintainer": "#3417A7"
    };
    AkkaActorsRenderer.prototype.redraw = function() {
      this.ctx.textAlign = "center";
      this.ctx.font = "9pt Menlo";
      this.ctx.fillStyle = "#222";
      this.ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);
      this.particleSystem.eachEdge(__bind(function(edge, pt1, pt2) {
        this.ctx.strokeStyle = "#888";
        this.ctx.lineWidth = 1;
        this.ctx.beginPath();
        this.ctx.moveTo(pt1.x, pt1.y);
        this.ctx.lineTo(pt2.x, pt2.y);
        return this.ctx.stroke();
      }, this));
      return this.particleSystem.eachNode(__bind(function(node, pt) {
        var rad, _ref;
        this.ctx.strokeStyle = (_ref = node.data.state === "run") != null ? _ref : {
          "#fff": "#aaa"
        };
        this.ctx.fillStyle = this.colors[node.data.name] || "555";
        this.ctx.lineWidth = 2;
        rad = node.data.name === "SupervisorActor" ? 20 : 10;
        this.ctx.beginPath();
        this.ctx.arc(pt.x, pt.y, rad, 0, Math.PI * 2, true);
        this.ctx.fill();
        this.ctx.stroke();
        this.ctx.fillStyle = "rgba(100,100,100,0.9)";
        return this.ctx.fillText(node.data.name || "", pt.x, pt.y + 2 * rad);
      }, this));
    };
    return AkkaActorsRenderer;
  })();
  $(function() {
    return new AkkaActors("#graph").run(LogData);
  });
}).call(this);
