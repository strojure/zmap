(ns strojure.zmap.core
  (:refer-clojure :exclude [delay update])
  (:require [strojure.zmap.impl.core :as impl :include-macros true]
            #?(:clj  [strojure.zmap.impl.clj]
               :cljs [strojure.zmap.impl.cljs]))
  #?(:cljs (:require-macros [strojure.zmap.core :refer [delay]])))

#?(:clj  (set! *warn-on-reflection* true)
   :cljs (set! *warn-on-infer* true))

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

(defn wrap
  "Wraps map with zmap proxy interface which provides direct access to delayed
  values. If `m` is zmap then returns it intact."
  {:inline (fn [m] `(impl/wrap ~m))}
  [m]
  (impl/wrap m))

(defmacro delay
  "Returns customized delay for the `body` to be used as zmap values when the
  map is constructed manually."
  [& body]
  `(impl/boxed-delay ~@body))

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

(defn update
  "Like `clojure.core/update` but with delayed new value. Returns instance of
  the same type as input `m`, so standard maps should be wrapped afterwards."
  {:added "1.2"}
  ([m k f]
   (impl/update0 m k f))
  ([m k f x]
   (impl/update1 m k f x))
  ([m k f x y]
   (impl/update2 m k f x y))
  ([m k f x y z]
   (impl/update3 m k f x y z))
  ([m k f x y z & more]
   (impl/update* m k f x y z more)))

(comment
  (def -m {:a {}})
  (def -m {:a 1})
  (def -m (wrap {:a (delay 1)}))
  (update -m :a assoc :x 0)
  (update -m :a inc)
  (update nil :a (fnil inc 0))
  )

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

(defmacro with-map
  "The macro binds unwrapped `zmap` to `ident`, then wraps result of the `body`
  back to zmap. Useful to minimize overhead of multiple transformations over
  existing zmap.

  Example:

      (zmap/with-map [m (zmap/wrap {})]
        ;; work with non-wrapped map here
        (println \"Inside:\" (class m))
        (assoc m :a (zmap/delay 1)))

      ;Inside: clojure.lang.PersistentArrayMap
      ;=> {:a 1}
  "
  [[ident zmap :as bindings] & body]
  (assert (vector? bindings))
  (assert (simple-symbol? ident))
  (assert (= 2 (count bindings)))
  `(let [~ident (impl/unwrap ~zmap)]
     (impl/wrap (do ~@body))))

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
