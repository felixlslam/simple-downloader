(ns simple-downloader.core
  (:require [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.content-type :refer :all]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [simple-downloader.downloader :as downloader]
            [simple-downloader.authenticator :as authenticator]
            [clojure.string :as string]
            [ring.util.response :as resp]
            [taoensso.timbre :as timbre :refer [log trace debug info warn error fatal spy]]
            [simple-downloader.logger :as logger]
            [ring.adapter.jetty :as jetty]
            [environ.core :refer [env]]
            [clojure.data.json :as json])
  (:gen-class)
  (:import (java.io FileInputStream File)
           (org.apache.commons.io FileUtils)))



(defroutes app
           (GET "/" [] (resp/content-type (resp/resource-response "index.html" {:root "public"}) "text/html"))
           (POST "/download" [username password url :as request]
             ;FIXME : Change IPv6 address to IPv4
             (let [client-ip (or (get-in request [:headers "x-forwarded-for"]) (:remote-addr request))]
               (if (authenticator/authenticate username password)
                 (downloader/download-handler client-ip username password url)
                 (do
                   (timbre/warn (json/write-str
                                  {:client-ip    client-ip
                                   :username     username
                                   :download-url url
                                   :result       "FAILED"
                                   :Exception    "Authentication Failed"}))
                   {:status 403 :content-type "text/html" :body "Authentication Failed !"})))))

(def handler
  (-> app
      (wrap-keyword-params)
      (wrap-params)))

(defn -main
  [& args]
  (logger/init-timbre)
  (jetty/run-jetty handler {:port         (Integer/parseInt (env :http-port))
                            :ssl?         true
                            :ssl-port     (Integer/parseInt (env :https-port))
                            :keystore     (env :keystore)
                            :key-password (env :keystore-pass)}))