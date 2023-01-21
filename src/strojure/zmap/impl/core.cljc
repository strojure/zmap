(ns strojure.zmap.impl.core
  "Implementation interfaces and base types."
  #?(:clj  (:import (clojure.lang Delay IDeref IPending))
     :cljs (:require-macros [strojure.zmap.impl.core :refer [boxed-delay]])))

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

(defprotocol Update
  (update0 [m k f])
  (update1 [m k f x])
  (update2 [m k f x y])
  (update3 [m k f x y z])
  (update* [m k f x y z more]))

(extend-protocol Update nil
  (update0 [_ k f] (update0 {} k f))
  (update1 [_ k f x] (update1 {} k f x))
  (update2 [_ k f x y] (update2 {} k f x y))
  (update3 [_ k f x y z] (update3 {} k f x y z))
  (update* [_ k f x y z more] (update* {} k f x y z more)))

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

(defn boxed-apply
  "Returns boxed delay for application of function `f` to possibly delayed
  value `v`."
  ([f v]
   (boxed-delay (f (cond-> ^IDeref v (boxed-delay? v) #?(:clj .deref :cljs -deref)))))
  ([f v x]
   (boxed-delay (f (cond-> ^IDeref v (boxed-delay? v) #?(:clj .deref :cljs -deref)) x)))
  ([f v x y]
   (boxed-delay (f (cond-> ^IDeref v (boxed-delay? v) #?(:clj .deref :cljs -deref)) x y)))
  ([f v x y z]
   (boxed-delay (f (cond-> ^IDeref v (boxed-delay? v) #?(:clj .deref :cljs -deref)) x y z)))
  ([f v x y z more]
   (boxed-delay (apply f (cond-> ^IDeref v (boxed-delay? v) #?(:clj .deref :cljs -deref)) x y z more))))

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
