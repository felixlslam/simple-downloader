(ns simple-downloader.core
  (:require [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.content-type :refer :all]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [simple-downloader.downloader :as downloader]
            [clojure.string :as string])
  (:gen-class)
  (:import (java.io FileInputStream File)
           (org.apache.commons.io FileUtils)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

(defn getfilename-from-url [url]
  (let [filename-in-url (string/replace url #".*/" "")]
    (if (= filename-in-url "")
      "downloaded_file"
      filename-in-url)))

(defroutes app
           (GET "/" [] {:status       200
                        :content-type "text/html"
                        :body         "Hello World."})
           (GET "/download" [url]
             (let [filename (getfilename-from-url url)
                   destpath (str "./" filename)]
               (try
                 (do
                   (downloader/download url destpath)
                   {:status  200
                    :headers {"Content-Type"        "application/octet-stream"
                              "Content-Disposition" (format "attachment; filename=\"%s\"" filename)}
                    :body    (FileInputStream. destpath)}
                   )
                 (catch Exception e
                   {:status       404
                    :content-type "text/html"
                    :body         (str "Failed to download the file. \n Exception: " (.getMessage e))})
                 (finally
                   (FileUtils/deleteQuietly (File. destpath)))))))

(def handler
  (-> app
      (wrap-keyword-params)
      (wrap-params)))