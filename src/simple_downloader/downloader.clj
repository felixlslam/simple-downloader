(ns simple-downloader.downloader
  (:import (org.apache.commons.io FileUtils)
           (java.net URL)
           (java.io File)))

(defn download [url dest]
  (FileUtils/copyURLToFile
    (URL. url) (File. dest)))


