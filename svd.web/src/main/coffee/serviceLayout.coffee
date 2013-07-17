$.extend # extend jQuery object with out functions:


  serviceLayout: (rsr, myDomainName, cb) ->
    Service = rsr.set()
    ServiceCircle = rsr.ellipse(375.2119, 300.0088, 229.6611, 232.2031)
    ServiceCircle.attr(
      id: "ServiceCircle"
      parent: "Service"
      fill: "#8CC63F"
      stroke: "#333333"
      "stroke-width": "9"
      "stroke-miterlimit": "10"
      "stroke-opacity": "1"
    ).data "id", "ServiceCircle"
    ServiceDomainName = rsr.text(170, 0, "#{myDomainName}")
    ServiceDomainName.attr
      id: "ServiceDomainName"
      parent: "Service"
      fill: "#4D4D4D"
      "font-family": "ArialMT"
      "font-size": "36px"
      "stroke-width": "0"
      "stroke-opacity": "1"

    ServiceDomainName.transform("m1 0 0 1 206.4927 317.0088").data "id", "ServiceDomainName"
    ServiceButton1 = rsr.circle(525, 118, 50)
    ServiceButton1.attr(
      id: "ServiceButton1"
      parent: "Service"
      opacity: "0.5"
      fill: "#F2F2F2"
      stroke: "#736357"
      "stroke-width": "9"
      "stroke-miterlimit": "10"
      "enable-background": "new    "
      "stroke-opacity": "1"
    ).data "id", "ServiceButton1"
    ServiceButton2 = rsr.circle(586, 212, 50)
    ServiceButton2.attr(
      id: "ServiceButton2"
      parent: "Service"
      opacity: "0.5"
      fill: "#F2F2F2"
      stroke: "#736357"
      "stroke-width": "9"
      "stroke-miterlimit": "10"
      "enable-background": "new    "
      "stroke-opacity": "1"
    ).data "id", "ServiceButton2"
    ServiceButton3 = rsr.circle(605, 325, 50)
    ServiceButton3.attr(
      id: "ServiceButton3"
      parent: "Service"
      opacity: "0.5"
      fill: "#F2F2F2"
      stroke: "#736357"
      "stroke-width": "9"
      "stroke-miterlimit": "10"
      "enable-background": "new    "
      "stroke-opacity": "1"
    ).data "id", "ServiceButton3"
    Service.attr
      id: "Service"
      name: "Service"

    rsrGroups = [Service]

    Service.push ServiceCircle
    Service.push ServiceDomainName
    Service.push ServiceButton1
    Service.push ServiceButton2
    Service.push ServiceButton3

    cb(Service)
