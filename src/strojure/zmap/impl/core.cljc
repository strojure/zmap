(ns strojure.zmap.impl.core
  "Implementation interfaces and base types."
  #?(:clj (:import (clojure.lang Delay IDeref IPending))))

#?(:clj  (set! *warn-on-reflection* true)
   :cljs (set! *warn-on-infer* true))

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

(defprotocol Wrap
  (wrap
    [m]
    "Wraps `m` to zmap if it is not zmap."))

(defprotocol Unwrap
  (unwrap
    [zmap]
    "Returns underlying map wrapped in zmap."))

(extend-protocol Unwrap nil (unwrap [_] nil))

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

#?(:clj  (deftype BoxedDelay [^Delay d]
           IDeref (deref [_] (.deref d))
           IPending (isRealized [_] (.isRealized d)))

   :cljs (deftype BoxedDelay [d]
           IDeref (-deref [_] (-deref d))
           IPending (-realized? [_] (-realized? d))))

(defmacro boxed-delay
  "Returns boxed delay for the `body`."
  [& body]
  `(BoxedDelay. (delay ~@body)))

(defn boxed-delay?
  "True if `x` is a boxed delay."
  {:inline (fn [x] `(instance? BoxedDelay ~x))}
  [x]
  (instance? BoxedDelay x))

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
