(ns benchmarks.core
  (:require [lazy-map.core :as lazy]
            [strojure.zmap.core :as zmap]))

(set! *warn-on-reflection* true)

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

(def ^:private -core {:a 1})
(def ^:private -zmap (zmap/wrap {:a (zmap/delay 1)}))
(def ^:private -lazy (lazy/lazy-map {:a 1}))

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

;;; Get existing value

(get -core :a)
;             Execution time mean : 9,606814 ns
;    Execution time std-deviation : 0,237998 ns
;   Execution time lower quantile : 9,217641 ns ( 2,5%)
;   Execution time upper quantile : 9,852598 ns (97,5%)

(get -zmap :a)
;             Execution time mean : 10,456097 ns
;    Execution time std-deviation : 0,694974 ns
;   Execution time lower quantile : 9,834914 ns ( 2,5%)
;   Execution time upper quantile : 11,597879 ns (97,5%)

(get -lazy :a)
;             Execution time mean : 12,801762 ns
;    Execution time std-deviation : 0,234398 ns
;   Execution time lower quantile : 12,560056 ns ( 2,5%)
;   Execution time upper quantile : 13,119562 ns (97,5%)

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

;;; IFn existing value

(-core :a)
;             Execution time mean : 6,280807 ns
;    Execution time std-deviation : 0,317243 ns
;   Execution time lower quantile : 5,898651 ns ( 2,5%)
;   Execution time upper quantile : 6,594827 ns (97,5%)

(-zmap :a)                                        ; 40% slower
;             Execution time mean : 9,073045 ns
;    Execution time std-deviation : 0,261897 ns
;   Execution time lower quantile : 8,845612 ns ( 2,5%)
;   Execution time upper quantile : 9,395197 ns (97,5%)

(comment
  (-lazy :a))
;class lazy_map.core.LazyMap cannot be cast to class clojure.lang.IFn

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

;;; Get missing value

(get -core :b)
;             Execution time mean : 2,965421 ns
;    Execution time std-deviation : 0,402933 ns
;   Execution time lower quantile : 2,544531 ns ( 2,5%)
;   Execution time upper quantile : 3,389341 ns (97,5%)

(get -zmap :b)
;             Execution time mean : 2,907998 ns
;    Execution time std-deviation : 0,215718 ns
;   Execution time lower quantile : 2,662191 ns ( 2,5%)
;   Execution time upper quantile : 3,141564 ns (97,5%)

(get -lazy :b)
;             Execution time mean : 6,142416 ns
;    Execution time std-deviation : 0,682844 ns
;   Execution time lower quantile : 5,779022 ns ( 2,5%)
;   Execution time upper quantile : 7,313891 ns (97,5%)

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

;;; Initialization cost

{:a 1}
;             Execution time mean : 4,071649 ns
;    Execution time std-deviation : 1,224121 ns
;   Execution time lower quantile : 3,423284 ns ( 2,5%)
;   Execution time upper quantile : 6,197514 ns (97,5%)

(zmap/wrap {:a 1})
;             Execution time mean : 7,733032 ns
;    Execution time std-deviation : 0,407748 ns
;   Execution time lower quantile : 7,244742 ns ( 2,5%)
;   Execution time upper quantile : 8,355490 ns (97,5%)

(zmap/wrap {:a (zmap/delay 1)})
;             Execution time mean : 39,464243 ns
;    Execution time std-deviation : 4,322022 ns
;   Execution time lower quantile : 35,436512 ns ( 2,5%)
;   Execution time upper quantile : 44,238908 ns (97,5%)

(lazy/lazy-map {:a 1})
;             Execution time mean : 35,015277 ns
;    Execution time std-deviation : 3,315731 ns
;   Execution time lower quantile : 32,115508 ns ( 2,5%)
;   Execution time upper quantile : 39,575876 ns (97,5%)

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

;;; assoc

(assoc -core :b 2)
;             Execution time mean : 44,144617 ns
;    Execution time std-deviation : 1,822196 ns
;   Execution time lower quantile : 42,740405 ns ( 2,5%)
;   Execution time upper quantile : 47,200674 ns (97,5%)

(assoc -zmap :b 2)
;             Execution time mean : 49,051014 ns
;    Execution time std-deviation : 3,891761 ns
;   Execution time lower quantile : 46,100362 ns ( 2,5%)
;   Execution time upper quantile : 55,113721 ns (97,5%)

(assoc -lazy :b 2)
;             Execution time mean : 48,276389 ns
;    Execution time std-deviation : 2,774043 ns
;   Execution time lower quantile : 44,092595 ns ( 2,5%)
;   Execution time upper quantile : 51,086791 ns (97,5%)

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
