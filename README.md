# zmap

Persistent map with lazily evaluated values for Clojure(Script).

[![cljdoc badge](https://cljdoc.org/badge/com.github.strojure/zmap)](https://cljdoc.org/d/com.github.strojure/zmap)
[![Clojars Project](https://img.shields.io/clojars/v/com.github.strojure/zmap.svg)](https://clojars.org/com.github.strojure/zmap)

Pronounced as `[Zee Map]`.

## Motivation

* Access map values with delayed evaluation as ordinary map values.
* Pass maps with delayed values to code which should not care that values are
  delayed.

## Features

* Keep pending delayed values not realized until used.
* Do not mix zmap delays with delays created by `clojure.core/delay`.
* Support `IFn` interface of persistent map.
* Support transients.
* Transparent IPersistentMap behaviour with minimal overhead.
* The `toString` does not realize delayed values, shows them like delays.

## API

### `wrap`/`delay`

The function `zmap/wrap` is used to wrap Clojure map having values created with
`zmap/delay` to access those values directly without `deref`.

```clojure
(ns api.core-wrap-delay
  "Example of `zmap/wrap` with `zmap/delay`."
  (:require [strojure.zmap.core :as zmap]))

(def ^:private -map
  (zmap/wrap {:a (zmap/delay (println "Init") 1)}))

(get -map :a)
;Init
;=> 1
```

### `update`

The function `zmap/update` is used for delayed map update.

```clojure
(ns api.core-update
  "Example of `zmap/update`."
  (:require [strojure.zmap.core :as zmap]))

(def ^:private -map1
  (zmap/update {:a 1} :a (fn [x]
                           (println "Update")
                           (inc x))))

(get -map1 :a)
;Update
;=> 2

(def ^:private -map2
  (-> (zmap/wrap {:a (zmap/delay (println "Init") 1)})
      (zmap/update :a (fn [x]
                        (println "Update")
                        (inc x)))))

(get -map2 :a)
;Init
;Update
;=> 2
```

### `with-map`

The macro `zmap/with-map` is used to make multiple transformations of underlying
Clojure map with decreased overhead caused by zmap proxying.

```clojure
(ns api.core-with-map
  "Example of `zmap/with-map`."
  (:require [strojure.zmap.core :as zmap]))

;; We work with non-wrapped map inside `zmap/with-map` macro.

(zmap/with-map [m (zmap/wrap {})]
  ;; work with non-wrapped map here
  (println "Inside:" (class m))
  (assoc m :a (zmap/delay 1)))
;Inside: clojure.lang.PersistentArrayMap
;=> {:a 1}

;; The result of `with-map` body is wrapped back to zmap.

(class (zmap/with-map [m (zmap/wrap {})]
         (assoc m :a (zmap/delay 1))))
;=> strojure.zmap.impl.clj.PersistentMapProxy
```

## Performance

See benchmarks [here](doc/benchmarks/core.clj).
