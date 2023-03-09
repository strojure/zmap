(defproject com.github.strojure/zmap "1.3.27-SNAPSHOT"
  :description "Persistent maps with support for lazy values."
  :url "https://github.com/strojure/zmap"
  :license {:name "The Unlicense" :url "https://unlicense.org"}

  :dependencies []

  :profiles {:provided {:dependencies [[org.clojure/clojure "1.11.1"]
                                       [org.clojure/clojurescript "1.11.60"]]}
             :dev,,,,, {:dependencies [;; clojurescript tests
                                       [com.google.guava/guava "31.1-jre"]
                                       [olical/cljs-test-runner "3.8.0"]
                                       ;; competitors
                                       [malabarba/lazy-map "1.3"]]
                        :source-paths ["doc"]}}

  :aliases {"cljs-test" ["run" "-m" "cljs-test-runner.main"]}

  :clean-targets ["target" "cljs-test-runner-out"]

  :deploy-repositories [["clojars" {:url "https://clojars.org/repo" :sign-releases false}]])
