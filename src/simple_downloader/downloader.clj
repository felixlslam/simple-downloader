(ns simple-downloader.downloader
  (:require [clojure.string :as string]
            [taoensso.timbre :as timbre :refer [log trace debug info warn error fatal spy]]
            [clojure.data.json :as json]
            [clojure.stacktrace :as stacktrace])
  (:import (org.apache.commons.io FileUtils)
           (java.net URL UnknownHostException)
           (java.io File FileInputStream)))

(defn get-detail-str-from-throwable [t]
  (str "Exception Class: " (class t) "\n"
       "Cause: " (.getCause t) "\n"
       "Message: " (.getMessage t) "\n"
       "StackTrace: " (apply str (interpose "\n" (.getStackTrace t)))))

(defn download [url dest]
  (FileUtils/copyURLToFile
    (URL. url) (File. dest)))

(defn gen-random-foldername [len]
  (apply str (take len (repeatedly #(char (+ (rand 26) 65))))))

(defn getfilename-from-url [url]
  (let [filename-in-url (string/replace url #".*/" "")]
    (if (= filename-in-url "")
      "downloaded_file"
      filename-in-url)))

(defn download-handler [client-ip username password url]
  (let [filename (getfilename-from-url url)
        download-home "./download"
        download-folder (str download-home "/" (gen-random-foldername 10))
        _ (.mkdir (File. download-folder))
        destpath (str download-folder "/" filename)]
    (try
      (do
        (download url destpath)
        (timbre/info (json/write-str
                       {:client-ip    client-ip
                        :username     username
                        :download-url url
                        :result       "OK"}))
        {:status  200
         :headers {"Content-Type"        "application/octet-stream"
                   "Content-Disposition" (format "attachment; filename=\"%s\"" filename)}
         :body    (FileInputStream. destpath)})
      ;FIXME: The Exception caught here cannot show the original Exception messages and stacktrace
      (catch Throwable t
        (do
          (timbre/error (json/write-str
                          {:client-ip    client-ip
                           :username     username
                           :download-url url
                           :result       "FAILED"
                           :Exception    (get-detail-str-from-throwable t)}))
          {:status       404
           :content-type "text/html"
           :body         (str "Failed to download the file.\nException:\n" (get-detail-str-from-throwable t))}))
      (finally
        (FileUtils/deleteQuietly (File. download-folder))))))


