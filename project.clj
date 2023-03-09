(defproject com.github.strojure/zmap "1.3.19-SNAPSHOT"
  :description "Persistent maps with support for lazy values."
  :url "https://github.com/strojure/zmap"
  :license {:name "The Unlicense" :url "https://unlicense.org"}

  :dependencies []

  :profiles {:provided {:dependencies [[org.clojure/clojure "1.11.1"]
                                       [org.clojure/clojurescript "1.11.60"]]}
             :dev,,,,, {:dependencies [;; clojurescript repl deps
                                       [com.google.guava/guava "31.1-jre"]
                                       ;; competitors
                                       [malabarba/lazy-map "1.3"]]
                        :source-paths ["doc"]}}

  :deploy-repositories [["clojars" {:url "https://clojars.org/repo" :sign-releases false}]])
