(ns strojure.zmap.core
  (:refer-clojure :exclude [delay])
  (:require [strojure.zmap.impl.core :as impl :include-macros true]
            #?(:clj  [strojure.zmap.impl.clj]
               :cljs [strojure.zmap.impl.cljs])))

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
