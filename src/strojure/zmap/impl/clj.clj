(ns strojure.zmap.impl.clj
  "Clojure implementation."
  (:require [strojure.zmap.impl.core :as impl])
  (:import (clojure.lang IDeref IEditableCollection IFn IKVReduce IMapEntry IMeta
                         IObj IPersistentMap IPersistentVector ITransientMap MapEntry MapEquivalence RT)
           (java.util Iterator Map)))

(set! *warn-on-reflection* true)

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

(defn- boxed-map-entry
  "Returns map entry with delayed value which is deref’ed when accessed."
  ([k, boxed-v]
   (boxed-map-entry (MapEntry. k boxed-v)))
  ([^MapEntry e]
   (reify
     IMapEntry
     (key
       [_]
       (.key e))
     (val
       [_]
       (.deref ^IDeref (.val e)))
     (getKey
       [_]
       (.key e))
     (getValue
       [_]
       (.deref ^IDeref (.val e)))
     IPersistentVector
     (count
       [_]
       2)
     (length
       [_]
       2)
     (containsKey
       [_ i]
       (.containsKey e i))
     (valAt
       [_ i]
       (cond-> ^IDeref (.valAt e i)
         (= i 1) (.deref)))
     (valAt
       [_ i not-found]
       (cond-> ^IDeref (.valAt e i not-found)
         (= i 1) (.deref)))
     (entryAt
       [_ i]
       (cond-> (.entryAt e i)
         (= i 1) (boxed-map-entry)))
     (cons
       [_ o]
       [(.key e), (.deref ^IDeref (.val e)), o])
     (assoc
       [this i o]
       (if (int? i)
         (.assocN this i o)
         (throw (IllegalArgumentException. "Key must be integer"))))
     (assocN
       [this i o]
       (case i
         0 (boxed-map-entry o (.val e))
         1 (if (impl/boxed-delay? o)
             (boxed-map-entry (.key e) o)
             [(.key e) o])
         2 (.cons this o)
         (.assocN e i o)))
     (seq
       [_]
       (lazy-seq (cons (.key e) (lazy-seq (cons (.deref ^IDeref (.val e)) nil)))))
     (rseq
       [_]
       (rseq [(.key e) (.deref ^IDeref (.val e))]))
     (nth
       [_ i]
       (cond-> ^IDeref (.nth e i)
         (= i 1) (.deref)))
     (nth
       [_ i not-found]
       (cond-> ^IDeref (.nth e i not-found)
         (= i 1) (.deref)))
     (pop
       [_]
       [(.key e)])
     (peek
       [_]
       (.deref ^IDeref (.val e)))
     (empty
       [_]
       (.empty e))
     (equiv
       [_ o]
       (.equiv [(.key e) (.deref ^IDeref (.val e))] o)))))

(defn map-entry
  "Returns map entry, the standard one or the implementation for boxed value."
  [^IMapEntry e]
  (if (impl/boxed-delay? (.val e))
    (boxed-map-entry e)
    e))

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

(def ^:private NA (Object.))

(deftype PersistentMapProxy [^:unsynchronized-mutable realized!
                             ^IPersistentMap m]
  Map
  (size
    [_]
    (.count m))
  (get
    [this k]
    (.valAt this k))
  MapEquivalence
  IFn
  (invoke
    [_ k]
    (let [v (.valAt m k NA)]
      (if (identical? NA v)
        nil
        (cond-> ^IDeref v (impl/boxed-delay? v)
                          (.deref)))))
  (invoke
    [_ k not-found]
    (let [v (.valAt m k NA)]
      (if (identical? NA v)
        not-found
        (cond-> ^IDeref v (impl/boxed-delay? v)
                          (.deref)))))
  IPersistentMap
  (valAt
    [_ k]
    (let [v (.valAt m k NA)]
      (if (identical? NA v)
        nil
        (cond-> ^IDeref v (impl/boxed-delay? v)
                          (.deref)))))
  (valAt
    [_ k not-found]
    (let [v (.valAt m k NA)]
      (if (identical? NA v)
        not-found
        (cond-> ^IDeref v (impl/boxed-delay? v)
                          (.deref)))))
  (entryAt
    [_ k]
    (map-entry (.entryAt m k)))
  (containsKey
    [_ k]
    (.containsKey m k))
  (assoc
    [_ k v]
    (PersistentMapProxy. nil (.assoc m k v)))
  (assocEx
    [_ k v]
    (PersistentMapProxy. nil (.assocEx m k v)))
  (cons
    [_ o]
    (PersistentMapProxy. nil (.cons m o)))
  (without
    [_ k]
    (PersistentMapProxy. nil (.without m k)))
  (empty
    [_]
    (PersistentMapProxy. nil (.empty m)))
  (count
    [_]
    (.count m))
  (seq
    [_]
    (some->> (.seq m) (map map-entry)))
  (equiv
    [_ o]
    (= o (or realized! (set! realized! (into {} (map map-entry) m)))))
  (iterator
    [_]
    (let [it (.iterator m)]
      (reify Iterator
        (hasNext [_] (.hasNext it))
        (next [_] (some-> (.next it) map-entry)))))
  IKVReduce
  (kvreduce
    [_ f init]
    (.kvreduce ^IKVReduce m
               (fn [x k v] (f x k (cond-> ^IDeref v (impl/boxed-delay? v)
                                                    (.deref))))
               init))
  IEditableCollection
  (asTransient
    [_]
    (impl/wrap (.asTransient ^IEditableCollection m)))
  IObj
  (withMeta
    [_ meta*]
    (PersistentMapProxy. nil (with-meta m meta*)))
  IMeta
  (meta
    [_]
    (.meta ^IMeta m))
  Object
  (toString
    [_]
    (RT/printString m))
  impl/Wrap
  (wrap
    [this] this)
  impl/Unwrap
  (unwrap
    [_] m)
  impl/Update
  (update0
    [_ k f]
    (PersistentMapProxy. nil (.assoc m k (impl/boxed-apply f (.valAt m k)))))
  (update1
    [_ k f x]
    (PersistentMapProxy. nil (.assoc m k (impl/boxed-apply f (.valAt m k) x))))
  (update2
    [_ k f x y]
    (PersistentMapProxy. nil (.assoc m k (impl/boxed-apply f (.valAt m k) x y))))
  (update3
    [_ k f x y z]
    (PersistentMapProxy. nil (.assoc m k (impl/boxed-apply f (.valAt m k) x y z))))
  (update*
    [_ k f x y z more]
    (PersistentMapProxy. nil (.assoc m k (impl/boxed-apply f (.valAt m k) x y z more)))))

(extend-type IPersistentMap
  impl/Wrap (wrap [m] (PersistentMapProxy. nil m))
  impl/Unwrap (unwrap [m] m)
  impl/Update
  (update0
    [m k f]
    (.assoc m k (impl/boxed-apply f (.valAt m k))))
  (update1
    [m k f x]
    (.assoc m k (impl/boxed-apply f (.valAt m k) x)))
  (update2
    [m k f x y]
    (.assoc m k (impl/boxed-apply f (.valAt m k) x y)))
  (update3
    [m k f x y z]
    (.assoc m k (impl/boxed-apply f (.valAt m k) x y z)))
  (update*
    [m k f x y z more]
    (.assoc m k (impl/boxed-apply f (.valAt m k) x y z more))))

(extend-protocol impl/Wrap nil
  (wrap [_] (PersistentMapProxy. nil {})))

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

(deftype TransientMapProxy [^ITransientMap m]
  ITransientMap
  (assoc
    [_ k v]
    (TransientMapProxy. (.assoc m k v)))
  (conj
    [_ o]
    (TransientMapProxy. (.conj m o)))
  (without
    [_ k]
    (TransientMapProxy. (.without m k)))
  (persistent
    [_]
    (impl/wrap (.persistent m)))
  (count
    [_]
    (.count m))
  (valAt
    [_ k]
    (let [v (.valAt m k NA)]
      (if (identical? NA v)
        nil
        (cond-> ^IDeref v (impl/boxed-delay? v)
                          (.deref)))))
  (valAt
    [_ k not-found]
    (let [v (.valAt m k NA)]
      (if (identical? NA v)
        not-found
        (cond-> ^IDeref v (impl/boxed-delay? v)
                          (.deref)))))
  impl/Wrap
  (wrap
    [this] this)
  impl/Unwrap
  (unwrap
    [_] m))

(extend-type ITransientMap
  impl/Wrap (wrap [m] (TransientMapProxy. m))
  impl/Unwrap (unwrap [m] m))

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
