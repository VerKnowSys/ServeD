# svd.web

## Development mode

    % sbt
    > jetty-run         // start jetty
    > ~prepare-webapp   // auto recompile when source change
    > jetty-stop        // stop jetty

## References
- scalatra - http://github.com/scalatra/scalatra
- scaml http://scalate.fusesource.org/documentation/scaml-reference.html


## Guide

### Scala loop in .scaml files

    %ul
      - for(e <- list)
        %li= e
