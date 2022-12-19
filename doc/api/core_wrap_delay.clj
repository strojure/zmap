(ns api.core-wrap-delay
  "Example of `zmap/wrap` with `zmap/delay`."
  (:require [strojure.zmap.core :as zmap]))

(def ^:private -map
  (zmap/wrap {:a (zmap/delay (println "Init") 1)}))

(get -map :a)
;Init
;=> 1
