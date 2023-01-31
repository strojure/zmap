(ns strojure.zmap.impl.cljs
  "ClojureScript implementation."
  (:require [strojure.zmap.impl.core :as impl]))

(set! *warn-on-infer* true)

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

(defn- boxed-map-entry
  "Returns map entry with delayed value which is deref’ed when accessed."
  ([k, boxed-v]
   (boxed-map-entry (->MapEntry k boxed-v nil)))
  ([e]
   (reify
     IMapEntry
     (-key
       [_]
       (-key e))
     (-val
       [_]
       (-deref (-val e)))
     ICounted
     (-count
       [_]
       2)
     IAssociative
     (-assoc
       [this i o]
       (if (int? i)
         (-assoc-n this i o)
         (throw (js/Error. "Key must be integer"))))
     (-contains-key?
       [_ i]
       (-contains-key? e i))
     ILookup
     (-lookup
       [_ i]
       (cond-> (-lookup e i)
         (= i 1) (-deref)))
     (-lookup
       [_ i not-found]
       (cond-> (-lookup e i not-found)
         (= i 1) (-deref)))
     IFind
     (-find
       [_ i]
       (cond-> (-find e i)
         (= i 1) (boxed-map-entry)))
     ICollection
     (-conj
       [_ o]
       [(-key e), (-deref (-val e)), o])
     IVector
     (-assoc-n
       [this i o]
       (case i
         0 (boxed-map-entry o (-val e))
         1 (if (impl/boxed-delay? o)
             (boxed-map-entry (-key e) o)
             [(-key e) o])
         2 (-conj this o)
         (-assoc-n e i o)))
     ISequential
     ISeqable
     (-seq
       [_]
       (lazy-seq (cons (-key e) (lazy-seq (cons (-deref (-val e)) nil)))))
     IReversible
     (-rseq
       [_]
       (rseq [(-key e) (-deref (-val e))]))
     IIndexed
     (-nth
       [_ i]
       (case i
         0 (-key e)
         1 (-deref (-val e))
         (-nth e i)))
     (-nth
       [_ i not-found]
       (case i
         0 (-key e)
         1 (-deref (-val e))
         not-found))
     IStack
     (-pop
       [_]
       [(-key e)])
     (-peek
       [_]
       (-deref (-val e)))
     IEmptyableCollection
     (-empty
       [_]
       (-empty e))
     IEquiv
     (-equiv
       [_ o]
       (-equiv [(-key e) (-deref (-val e))] o)))))

(defn map-entry
  "Returns map entry, the standard one or the implementation for boxed value."
  [e]
  (if (impl/boxed-delay? (-val e))
    (boxed-map-entry e)
    e))

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

(def ^:private NA (js/Object.))

(deftype PersistentMapProxy [^:mutable realized!, m]
  IFn
  (-invoke
    [_ k]
    (let [v (-lookup m k NA)]
      (if (identical? NA v)
        nil
        (cond-> v (impl/boxed-delay? v)
                  (-deref)))))
  (-invoke
    [_ k not-found]
    (let [v (-lookup m k NA)]
      (if (identical? NA v)
        not-found
        (cond-> v (impl/boxed-delay? v)
                  (-deref)))))
  ILookup
  (-lookup
    [_ k]
    (let [v (-lookup m k NA)]
      (if (identical? NA v)
        nil
        (cond-> v (impl/boxed-delay? v)
                  (-deref)))))
  (-lookup
    [_ k not-found]
    (let [v (-lookup m k NA)]
      (if (identical? NA v)
        not-found
        (cond-> v (impl/boxed-delay? v)
                  (-deref)))))
  IFind
  (-find
    [_ k]
    (map-entry (-find m k)))
  IAssociative
  (-contains-key?
    [_ k]
    (-contains-key? m k))
  (-assoc
    [_ k v]
    (PersistentMapProxy. nil (-assoc m k v)))
  ICollection
  (-conj
    [_ o]
    (PersistentMapProxy. nil (-conj m o)))
  IMap
  (-dissoc
    [_ k]
    (PersistentMapProxy. nil (-dissoc m k)))
  IEmptyableCollection
  (-empty
    [_]
    (PersistentMapProxy. nil (-empty m)))
  ICounted
  (-count
    [_]
    (-count m))
  ISeqable
  (-seq
    [_]
    (some->> (-seq m) (map map-entry)))
  IEquiv
  (-equiv
    [_ o]
    (= o (or realized! (set! realized! (into {} (map map-entry) m)))))
  IIterable
  (-iterator
    [_]
    (let [it (-iterator m)]
      (reify Object
        (hasNext [_] (.hasNext ^HashMapIter it))
        (next [_] (some-> (.next ^HashMapIter it) map-entry)))))
  IKVReduce
  (-kv-reduce
    [_ f init]
    (-kv-reduce m (fn [x k v] (f x k (cond-> v (impl/boxed-delay? v)
                                               (-deref))))
                init))
  IEditableCollection
  (-as-transient
    [_]
    (impl/wrap (-as-transient m)))
  IWithMeta
  (-with-meta
    [_ meta*]
    (PersistentMapProxy. nil (with-meta m meta*)))
  IMeta
  (-meta
    [_]
    (-meta m))
  IPrintWithWriter
  (-pr-writer
    [_ writer opts]
    (-pr-writer (or realized! (set! realized! (into {} (map map-entry) m))) writer opts))
  Object
  (toString
    [_]
    (pr-str* m))
  impl/Wrap
  (wrap
    [this] this)
  impl/Unwrap
  (unwrap
    [_] m)
  impl/Update
  (update0
    [_ k f]
    (PersistentMapProxy. nil (-assoc m k (impl/boxed-apply f (-lookup m k)))))
  (update1
    [_ k f x]
    (PersistentMapProxy. nil (-assoc m k (impl/boxed-apply f (-lookup m k) x))))
  (update2
    [_ k f x y]
    (PersistentMapProxy. nil (-assoc m k (impl/boxed-apply f (-lookup m k) x y))))
  (update3
    [_ k f x y z]
    (PersistentMapProxy. nil (-assoc m k (impl/boxed-apply f (-lookup m k) x y z))))
  (update*
    [_ k f x y z more]
    (PersistentMapProxy. nil (-assoc m k (impl/boxed-apply f (-lookup m k) x y z more)))))

(extend-type PersistentArrayMap
  impl/Wrap (wrap [m] (PersistentMapProxy. nil m))
  impl/Unwrap (unwrap [m] m)
  impl/Update
  (update0
    [m k f]
    (-assoc m k (impl/boxed-apply f (-lookup m k))))
  (update1
    [m k f x]
    (-assoc m k (impl/boxed-apply f (-lookup m k) x)))
  (update2
    [m k f x y]
    (-assoc m k (impl/boxed-apply f (-lookup m k) x y)))
  (update3
    [m k f x y z]
    (-assoc m k (impl/boxed-apply f (-lookup m k) x y z)))
  (update*
    [m k f x y z more]
    (-assoc m k (impl/boxed-apply f (-lookup m k) x y z more))))

(extend-type PersistentHashMap
  impl/Wrap (wrap [m] (PersistentMapProxy. nil m))
  impl/Unwrap (unwrap [m] m)
  impl/Update
  (update0
    [m k f]
    (-assoc m k (impl/boxed-apply f (-lookup m k))))
  (update1
    [m k f x]
    (-assoc m k (impl/boxed-apply f (-lookup m k) x)))
  (update2
    [m k f x y]
    (-assoc m k (impl/boxed-apply f (-lookup m k) x y)))
  (update3
    [m k f x y z]
    (-assoc m k (impl/boxed-apply f (-lookup m k) x y z)))
  (update*
    [m k f x y z more]
    (-assoc m k (impl/boxed-apply f (-lookup m k) x y z more))))

(extend-protocol impl/Wrap nil
  (wrap [_] (PersistentMapProxy. nil {})))

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

(deftype TransientMapProxy [m]
  ITransientAssociative
  (-assoc!
    [_ k v]
    (TransientMapProxy. (-assoc! m k v)))
  ITransientCollection
  (-conj!
    [_ o]
    (TransientMapProxy. (-conj! m o)))
  (-persistent!
    [_]
    (impl/wrap (-persistent! m)))
  ITransientMap
  (-dissoc!
    [_ k]
    (TransientMapProxy. (-dissoc! m k)))
  ICounted
  (-count
    [_]
    (-count m))
  ILookup
  (-lookup
    [_ k]
    (let [v (-lookup m k NA)]
      (if (identical? NA v)
        nil
        (cond-> v (impl/boxed-delay? v)
                  (-deref)))))
  (-lookup
    [_ k not-found]
    (let [v (-lookup m k NA)]
      (if (identical? NA v)
        not-found
        (cond-> v (impl/boxed-delay? v)
                  (-deref)))))
  impl/Wrap
  (wrap
    [this] this)
  impl/Unwrap
  (unwrap
    [_] m))

(extend-type TransientArrayMap
  impl/Wrap (wrap [m] (TransientMapProxy. m))
  impl/Unwrap (unwrap [m] m))

(extend-type TransientHashMap
  impl/Wrap (wrap [m] (TransientMapProxy. m))
  impl/Unwrap (unwrap [m] m))

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
