$.extend # extend jQuery object with out functions:


  serviceInfoLayout: (rsr, runtime, memory, iowrite, ioread) ->
    Runtime = rsr.text(0, 0, "#{runtime}")
    Runtime.attr
      id: "Runtime"
      fill: "#333333"
      "font-family": "Optima-Regular"
      "font-size": "30px"
      "stroke-width": "0"
      "stroke-opacity": "1"

    Runtime.transform("m1 0 0 1 408.1621 379.0156").data "id", "Runtime"
    Memory = rsr.text(0, 0, "#{memory}")
    Memory.attr
      id: "Memory"
      fill: "#333333"
      "font-family": "Optima-Regular"
      "font-size": "30px"
      "stroke-width": "0"
      "stroke-opacity": "1"

    Memory.transform("m1 0 0 1 408.1621 415.0156").data "id", "Memory"
    IOWrite = rsr.text(0, 0, "#{iowrite}")
    IOWrite.attr
      id: "IOWrite"
      fill: "#333333"
      "font-family": "Optima-Regular"
      "font-size": "30px"
      "stroke-width": "0"
      "stroke-opacity": "1"

    IOWrite.transform("m1 0 0 1 408.1621 451.0156").data "id", "IOWrite"
    IORead = rsr.text(0, 0, "#{ioread}")
    IORead.attr
      id: "IORead"
      fill: "#333333"
      " font-family": "Optima-Regular"
      " font-size": "30"
      "stroke-width": "0"
      "stroke-opacity": "1"

    IORead.transform("m1 0 0 1 408.1621 487.0156").data "id", "IORead"
    text_b = rsr.text(0, 0, "RUNTIME:")
    text_b.attr
      fill: "#333333"
      " font-family": "Optima-Regular"
      " font-size": "30"
      "stroke-width": "0"
      "stroke-opacity": "1"

    text_b.transform("m1 0 0 1 259.123 379.0156").data "id", "text_b"
    text_c = rsr.text(0, 0, "IO READ:")
    text_c.attr
      fill: "#333333"
      " font-family": "Optima-Regular"
      " font-size": "30"
      "stroke-width": "0"
      "stroke-opacity": "1"

    text_c.transform("m1 0 0 1 272.4731 487.0156").data "id", "text_c"
    BackgroundGroup = rsr.set()
    ServiceInfoElement6 = rsr.rect(200, 37.0156, 400, 347)
    ServiceInfoElement6.attr(
      id: "ServiceInfoElement6"
      x: "200"
      y: "37.0156"
      parent: "BackgroundGroup"
      fill: "#808080"
      stroke: "#333333"
      "stroke-width": "9"
      "stroke-opacity": "1"
    ).data "id", "ServiceInfoElement6"
    ServiceInfoElement6_2_ = rsr.rect(204.4585, 376.334, 390.8755, 37)
    ServiceInfoElement6_2_.attr(
      id: "ServiceInfoElement6_2_"
      x: "204.4585"
      y: "376.334"
      parent: "BackgroundGroup"
      fill: "#808080"
      "stroke-width": "0"
      "stroke-opacity": "1"
    ).data "id", "ServiceInfoElement6_2_"
    rect_d = rsr.rect(229, 410, 336, 67)
    rect_d.attr(
      x: "229"
      y: "410"
      parent: "BackgroundGroup"
      fill: "#808080"
      "stroke-width": "0"
      "stroke-opacity": "1"
    ).data "id", "rect_d"
    rect_e = rsr.rect(304, 467, 183, 67)
    rect_e.attr(
      x: "304"
      y: "467"
      parent: "BackgroundGroup"
      fill: "#808080"
      "stroke-width": "0"
      "stroke-opacity": "1"
    ).data "id", "rect_e"
    rect_f = rsr.rect(365, 525, 66, 25)
    rect_f.attr(
      x: "365"
      y: "525"
      parent: "BackgroundGroup"
      fill: "#808080"
      "stroke-width": "0"
      "stroke-opacity": "1"
    ).data "id", "rect_f"
    BackgroundGroup.attr
      id: "BackgroundGroup"
      name: "BackgroundGroup"

    group_a = rsr.set()
    ServiceInfoElement2 = rsr.path("M200,384.0234c0,91.7803,87.8877,166.0469,196.5,166.0469")
    ServiceInfoElement2.attr(
      id: "ServiceInfoElement2"
      parent: "group_a"
      fill: "#808080"
      stroke: "#333333"
      "stroke-width": "9"
      "stroke-miterlimit": "10"
      "stroke-opacity": "1"
    ).data "id", "ServiceInfoElement2"
    ServiceInfoElement1 = rsr.path("M600,384.0156c0,91.7881-91.0186,166.0625-203.5,166.0625")
    ServiceInfoElement1.attr(
      id: "ServiceInfoElement1"
      parent: "group_a"
      fill: "#808080"
      stroke: "#333333"
      "stroke-width": "9"
      "stroke-miterlimit": "10"
      "stroke-opacity": "1"
    ).data "id", "ServiceInfoElement1"
    group_a.attr name: "group_a"
    rsrGroups = [BackgroundGroup, group_a]
