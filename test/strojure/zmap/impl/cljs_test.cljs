(ns strojure.zmap.impl.cljs-test
  (:require [clojure.test :as test :refer [deftest testing]]
            [strojure.zmap.impl.cljs :as impl*]
            [strojure.zmap.impl.core :as impl :include-macros true]))

(declare thrown?)

#_(test/run-tests)

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

(def ^:private -e (#'impl*/boxed-map-entry :k (impl/boxed-delay :x)))

(deftest boxed-map-entry-t
  (test/are [expr result] (= result expr)

    (key -e) #_= :k
    (val -e) #_= :x

    (count -e) #_= 2

    (nth -e 0) #_= :k
    (nth -e 1) #_= :x
    (nth -e 2 :not-found) #_= :not-found

    -e #_= [:k :x]
    (let [[k v] -e] [k v]) #_= [:k :x]

    (conj -e :y) #_= [:k :x :y]
    (pop -e) #_= [:k]
    (peek -e) #_= :x

    (assoc -e 0 :kk) #_= [:kk :x]
    (assoc -e 1 :xx) #_= [:k :xx]
    (assoc -e 1 (impl/boxed-delay :xx)) #_= [:k :xx]
    (assoc -e 2 :y) #_= [:k :x :y]

    (contains? -e 0) #_= true
    (contains? -e 1) #_= true
    (contains? -e 2) #_= false

    (find -e 0) #_= [0 :k]
    (find -e 1) #_= [1 :x]
    (find -e 2) #_= nil
    (let [a (atom :pending)
          e (#'impl*/boxed-map-entry :k (impl/boxed-delay (reset! a :realized)
                                                          :x))
          e (find e 1)]
      [e @a]) #_= [[1 :x] :pending]

    (empty -e) #_= (empty (->MapEntry :k :x nil))

    (realized? (seq -e)) #_= false
    (first (seq -e)) #_= :k
    (second (seq -e)) #_= :x

    (let [a (atom :pending)
          e (#'impl*/boxed-map-entry :k (impl/boxed-delay (reset! a :realized)
                                                          :x))]
      [(first e) @a]) #_= [:k :pending]

    (let [a (atom :pending)
          e (#'impl*/boxed-map-entry :k (impl/boxed-delay (reset! a :realized)
                                                          :x))]
      [(second e) @a]) #_= [:x :realized]

    (= -e -e) #_= true
    (= -e [:k :x]) #_= true
    (= -e (#'impl*/boxed-map-entry :k (impl/boxed-delay :x))) #_= true
    (= -e [:k :y]) #_= false

    (sequential? -e) #_= true

    (rseq -e) #_= '(:x :k)

    )

  (testing "Exceptional operations"
    (test/are [expr] expr

      (thrown? js/Error
               (nth -e 2))
      (thrown? js/Error "Key must be integer"
               (assoc -e :x nil))

      )))

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

(def ^:private -m
  (impl/wrap {:a (impl/boxed-delay :x)
              :b :y}))

(defn- persistent-map-proxy? [x]
  (instance? impl*/PersistentMapProxy x))

(deftest ->PersistentMapProxy-t
  (test/are [expr result] (= result expr)

    (get -m :a) #_= :x
    (get -m :b) #_= :y
    (get -m :c) #_= nil

    (get -m :a :not-found) #_= :x
    (get -m :b :not-found) #_= :y
    (get -m :c :not-found) #_= :not-found

    (:a -m) #_= :x
    (:b -m) #_= :y
    (:c -m) #_= nil

    (-m :a) #_= :x
    (-m :b) #_= :y
    (-m :c) #_= nil

    (= -m -m) #_= true
    (= -m {:a :x :b :y}) #_= true
    (= {:a :x :b :y} -m) #_= true
    (= (impl/wrap {:a (impl/boxed-delay :x)})
       (impl/wrap {:a (impl/boxed-delay :x)})) #_= true

    (assoc -m :a :xx) #_= {:a :xx :b :y}
    (assoc -m :b :yy) #_= {:a :x :b :yy}
    (assoc -m :c :zz) #_= {:a :x :b :y :c :zz}

    (dissoc -m :a) #_= {:b :y}
    (dissoc -m :b) #_= {:a :x}
    (dissoc -m :c) #_= {:a :x :b :y}

    (update -m :a name) #_= {:a "x" :b :y}
    (update -m :b name) #_= {:a :x :b "y"}

    (select-keys -m [:a :b]) #_= {:a :x :b :y}
    (select-keys -m [:a]) #_= {:a :x}
    (select-keys -m [:b]) #_= {:b :y}

    (seq -m) #_= '([:a :x] [:b :y])

    (into {} -m) #_= {:a :x :b :y}
    (into -m {}) #_= {:a :x :b :y}
    (into {:c :z} -m) #_= {:a :x :b :y :c :z}
    (into -m {:c :z}) #_= {:a :x :b :y :c :z}
    (persistent-map-proxy? (into {:c :z} -m)) #_= false
    (persistent-map-proxy? (into -m {:c :z})) #_= true

    (merge {} -m) #_= {:a :x :b :y}
    (merge -m {}) #_= {:a :x :b :y}
    (merge {:c :z} -m) #_= {:a :x :b :y :c :z}
    (merge -m {:c :z}) #_= {:a :x :b :y :c :z}
    (persistent-map-proxy? (merge {:c :z} -m)) #_= false
    (persistent-map-proxy? (merge -m {:c :z})) #_= true

    (counted? -m) #_= true
    (count -m) #_= 2

    (set (keys -m)) #_= #{:a :b}
    (set (vals -m)) #_= #{:x :y}

    (conj -m [:c :z]) #_= {:a :x, :b :y, :c :z}
    (conj -m [:c (impl/boxed-delay :z)]) #_= {:a :x, :b :y, :c :z}

    (empty -m) #_= {}
    (persistent-map-proxy? (empty -m)) #_= true

    (find -m :a) #_= [:a :x]

    (reduce-kv conj [] -m) #_= [:a :x :b :y]

    (str -m) #_= "{:a :x, :b :y}"

    )

  (testing "Value laziness"
    (test/are [expr result] (= result expr)

      (let [a (atom :pending) m (impl/wrap {:a (impl/boxed-delay
                                                 (reset! a :realized)
                                                 :x)})
            before @a
            x (get m :a)
            after @a]
        [before x after]) #_= [:pending :x :realized]

      (let [a (atom :pending) m (impl/wrap {:a (impl/boxed-delay
                                                 (reset! a :realized)
                                                 :x)})
            m (assoc m :a :xx)]
        [@a m]) #_= [:pending {:a :xx}]

      (let [a (atom :pending) m (impl/wrap {:a (impl/boxed-delay
                                                 (reset! a :realized)
                                                 :x)})
            m (assoc m :b :yy)]
        [@a m]) #_= [:pending {:a :x :b :yy}]

      (let [a (atom :pending) m (impl/wrap {:a (impl/boxed-delay
                                                 (reset! a :realized)
                                                 :x)
                                            :b :y})
            m (dissoc m :a)]
        [@a m]) #_= [:pending {:b :y}]

      (let [a (atom :pending) m (impl/wrap {:a (impl/boxed-delay
                                                 (reset! a :realized)
                                                 :x)
                                            :b :y})
            m (dissoc m :b)]
        [@a m]) #_= [:pending {:a :x}]

      (let [a (atom :pending) m (impl/wrap {:a (impl/boxed-delay
                                                 (reset! a :realized)
                                                 :x)
                                            :b :y})
            m (update m :b name)]
        [@a m]) #_= [:pending {:a :x :b "y"}]

      (let [a (atom :pending) m (impl/wrap {:a (impl/boxed-delay
                                                 (reset! a :realized)
                                                 :x)
                                            :b :y})
            m (select-keys m [:a :b])]
        [@a m]) #_= [:realized {:a :x :b :y}]

      (let [a (atom :pending) m (impl/wrap {:a (impl/boxed-delay
                                                 (reset! a :realized)
                                                 :x)
                                            :b :y})
            m (select-keys m [:b])]
        [@a m]) #_= [:pending {:b :y}]

      (let [a (atom :pending) m (impl/wrap {:a (impl/boxed-delay
                                                 (reset! a :realized)
                                                 :x)
                                            :b :y})
            _ (doall (seq m))]
        [@a]) #_= [:pending]

      (let [a (atom :pending) m (impl/wrap {:a (impl/boxed-delay
                                                 (reset! a :realized)
                                                 :x)
                                            :b :y})
            m (into m {:c :z})]
        [@a m]) #_= [:pending {:a :x, :b :y, :c :z}]

      (let [a (atom :pending) m (impl/wrap {:a (impl/boxed-delay
                                                 (reset! a :realized)
                                                 :x)
                                            :b :y})
            m (into {:c :z} m)]
        [@a m]) #_= [:realized {:a :x, :b :y, :c :z}]

      (let [a (atom :pending) m (impl/wrap {:a (impl/boxed-delay
                                                 (reset! a :realized)
                                                 :x)
                                            :b :y})
            m (conj m [:c :z])]
        [@a m]) #_= [:pending {:a :x, :b :y, :c :z}]

      (let [a (atom :pending) m (impl/wrap {:a (impl/boxed-delay
                                                 (reset! a :realized)
                                                 :x)
                                            :b :y})
            k (key (find m :a))]
        [@a k]) #_= [:pending :a]

      (let [a (atom :pending) m (impl/wrap {:a (impl/boxed-delay
                                                 (reset! a :realized)
                                                 :x)
                                            :b :y})
            v (val (find m :a))]
        [@a v]) #_= [:realized :x]

      (let [a (atom :pending) m (impl/wrap {:a (impl/boxed-delay
                                                 (reset! a :realized)
                                                 :x)
                                            :b :y})
            m (persistent! (transient m))]
        [@a m]) #_= [:pending {:a :x, :b :y}]

      ))
  )

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
