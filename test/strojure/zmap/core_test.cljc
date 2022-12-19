(ns strojure.zmap.core-test
  (:require [clojure.test :as test :refer [deftest]]
            [strojure.zmap.core :as zmap :include-macros true]))

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

#_(test/run-tests)

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

(deftest wrap-t
  (test/are [expr result] (= result expr)

    (zmap/wrap {:a (zmap/delay 1)}) #_= {:a 1}

    ))

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

(deftest with-map-t
  (test/are [expr result] (= result expr)

    (let [a! (atom nil)
          zmap (zmap/with-map [m (zmap/wrap {})]
                 (reset! a! (type m))
                 (assoc m :a (zmap/delay 1)))]
      [{:a 1} @a! (type zmap)])

    #_= [{:a 1} (type {}) (type (zmap/wrap {}))]

    ))

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
