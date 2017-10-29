(ns simple-downloader.logger
  (:require [taoensso.timbre.appenders.core :as appenders]
            [taoensso.timbre :as timbre :refer [log trace debug info warn error fatal spy]]
            [clojure.data.json :as json]))

(defn json-output-fn
  [{:keys [vargs_ hostname_ timestamp_ level] :as args}]
  (let [
        messages (map (fn [msg] {:timestamp @timestamp_
                                 :level     level
                                 :hostname  @hostname_
                                 :message   msg})
                      @vargs_)
        json-messages (map #(json/write-str %) messages)]
    (clojure.string/join "\n" json-messages)))

(defn init-timbre []
  (timbre/merge-config!
    {:appenders {
                 :println nil
                 :spit (merge (appenders/spit-appender {:fname "./log/simple-downloader.log"}) {:output-fn json-output-fn})
                 }}))