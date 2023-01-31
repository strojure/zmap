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

(deftest update-t
  (test/are [expr result] (= result expr)

    (let [a! (atom [])
          zmap (-> {:a 1}
                   (zmap/update :a (fn [x]
                                     (swap! a! conj :update)
                                     (inc x)))
                   (zmap/wrap))]
      [zmap @a! (:a zmap) @a!])

    #_= [{:a 2} [] 2 [:update]]

    (let [a! (atom [])
          zmap (-> (zmap/wrap {:a (zmap/delay
                                    (swap! a! conj :init)
                                    1)})
                   (zmap/update :a (fn [x]
                                     (swap! a! conj :update)
                                     (inc x))))]
      [zmap @a! (:a zmap) @a!])

    #_= [{:a 2} [] 2 [:init :update]]

    ))

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

(deftest with-map-t
  (test/are [expr result] (= result expr)

    (let [a! (atom nil)
          zmap (zmap/with-map [m (zmap/wrap {})]
                 (reset! a! (type m))
                 (assoc m :a (zmap/delay 1)))]
      [zmap @a! (type zmap)])

    #_= [{:a 1} (type {}) (type (zmap/wrap {}))]

    ))

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
