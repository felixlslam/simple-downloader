(defproject simple-downloader "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [commons-io "2.4"]
                 [ring-server "0.5.0"]
                 [ring/ring-core "1.5.0"]
                 [compojure "1.6.0"]
                 [com.taoensso/timbre "4.10.0"]
                 [ring/ring-jetty-adapter "1.2.1"]
                 [environ "1.1.0"]
                 [org.clojure/data.json "0.2.6"]]
  :plugins [[lein-ring "0.9.7"]]
  :ring {:handler simple-downloader.core/handler}
  :main ^:skip-aot simple-downloader.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
