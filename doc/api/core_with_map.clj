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
