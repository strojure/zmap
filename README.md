# zmap

Persistent map with lazily evaluated values for Clojure(Script).

[![cljdoc badge](https://cljdoc.org/badge/com.github.strojure/zmap)](https://cljdoc.org/d/com.github.strojure/zmap)
[![Clojars Project](https://img.shields.io/clojars/v/com.github.strojure/zmap.svg)](https://clojars.org/com.github.strojure/zmap)

Pronounced as `[Zee Map]`.

## Motivation

* Access map values with delayed evaluation as ordinary map values.
* Pass maps with delayed values to code which should not care that values are
  delayed.
