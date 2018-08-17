(ns codeclimate.reporter
  (:require [clojure.java.io :as io]
            [clojure.pprint :as pp]
            [clojure.tools.cli :as cli]
            kibit.driver
            kibit.check
            [kibit.reporters :as reporters]
            [cheshire.core :as json])
  (:import (java.io StringWriter File)))

(defn pprint-code [form]
  (let [string-writer (StringWriter.)]
    (pp/write form
              :dispatch pp/code-dispatch
              :stream string-writer
              :pretty true)
    (str string-writer)))

(defn template-solution [alt expr]
  (str "<p>Consider using:</p>"
       "<pre>```" (pprint-code alt) "```</pre>"
       "<p>instead of:</p>"
       "<pre>```" (pprint-code expr) "```</pre>"))

(defn codeclimate-reporter
  [check-map]
  (let [{:keys [file line expr alt]} check-map
        issue {:type               "issue"
               :check_name         "kibit/suggestion"
               :description        (str "Non-idiomatic code found in `" (first (seq expr)) "`")
               :categories         ["Style"]
               :location           {:path  (str file)
                                    :lines {:begin line
                                            :end   line}}
               :content            {:body (template-solution alt expr)}
               :remediation_points 50000}]
    (println (str (json/generate-string issue) "\0"))))

(defn target-files
  [config]
  (->> (:include_paths config)
       (map #(cond
               (string? %) (io/file %)
               :else %))
       (map kibit.driver/find-clojure-sources-in-dir)
       flatten
       distinct))

(defn analyze
  [dir config]
  (let [target-files (target-files (update config :include_paths conj dir))]
    (mapv #(kibit.check/check-file %
                                   :reporter codeclimate-reporter)
          target-files)))
