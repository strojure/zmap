(ns api.core-update
  "Example of `zmap/update`."
  (:require [strojure.zmap.core :as zmap]))

(def ^:private -map1
  (-> {:a 1}
      (zmap/update :a (fn [x]
                        (println "Update")
                        (inc x)))
      (zmap/wrap)))

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
