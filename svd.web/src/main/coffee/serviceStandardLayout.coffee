$.extend # extend jQuery object with out functions:


  serviceStandardLayout: (rsr, layoutDomain) ->
    ServiceStandard = rsr.set()
    ServiceStandardCircle = rsr.circle(203, 357, 125)
    ServiceStandardCircle.attr(
      id: "ServiceStandardCircle"
      parent: "ServiceStandard"
      fill: "#BEBFBB"
      stroke: "#333333"
      "stroke-width": "5"
      "stroke-miterlimit": "10"
      "stroke-opacity": "1"
    ).data "id", "ServiceStandardCircle"
    ServiceStandardCaption = rsr.text(130, -5, "#{layoutDomain}")
    ServiceStandardCaption.attr
      id: "ServiceStandardCaption"
      parent: "ServiceStandard"
      fill: "#333333"
      "font-family": "Arial"
      "font-size": "36px"
      "stroke-width": "0"
      "stroke-opacity": "1"

    ServiceStandardCaption.transform("m0.9955 0 0 1 347.0942 167.9629").data "id", "ServiceStandardCaption"
    ServiceStandard.attr
      id: "ServiceStandard"
      name: "ServiceStandard"

    InfoLine2 = rsr.set()
    InfoLine2.attr
      id: "InfoLine2"
      parent: "ServiceStandard"
      name: "InfoLine2"

    group_a = rsr.set()
    path_c = rsr.path("M286.7256,266.7002c26.7803-29.5503,53.5605-59.1006,80.3408-88.6514     c0.4458-0.4922-1.4287-0.2676-1.7617,0.0996c-26.7803,29.5503-53.5605,59.1006-80.3408,88.6514     C284.5181,267.292,286.3926,267.0674,286.7256,266.7002L286.7256,266.7002z")
    path_c.attr(
      parent: "ServiceStandard"
      fill: "#333333"
      "stroke-width": "0"
      "stroke-opacity": "1"
    ).data "id", "path_c"
    group_a.attr
      parent: "ServiceStandard"
      name: "group_a"

    InfoLine1 = rsr.set()
    InfoLine1.attr
      id: "InfoLine1"
      parent: "ServiceStandard"
      name: "InfoLine1"

    group_b = rsr.set()
    path_d = rsr.path("M346.6274,178.5601c43.5049,0,87.0093,0,130.5142,0c48.4287,0,96.8584,0,145.2881,0     c0.3994,0,0.7988,0,1.1982,0c0.7383,0,2.0508-1.1201,0.7441-1.1201c-43.5039,0-87.0088,0-130.5137,0     c-48.4287,0-96.8589,0-145.2881,0c-0.3994,0-0.7988,0-1.1978,0C346.6343,177.4399,345.3208,178.5601,346.6274,178.5601     L346.6274,178.5601z")
    path_d.attr(
      parent: "ServiceStandard"
      fill: "#333333"
      "stroke-width": "0"
      "stroke-opacity": "1"
    ).data "id", "path_d"
    group_b.attr
      parent: "ServiceStandard"
      name: "group_b"

    rsrGroups = [ServiceStandard, InfoLine2, group_a, InfoLine1, group_b]
    ServiceStandard.transform("t100,100r45t-100,0")
