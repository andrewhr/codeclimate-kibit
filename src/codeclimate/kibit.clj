(ns codeclimate.kibit
  (:require [clojure.java.io :as io]
            [clojure.pprint :as pp]
            [kibit.driver :as kibit]
            [kibit.reporters :as reporters]
            [cheshire.core :as json])
  (:import (java.io StringWriter File))
  (:gen-class))

(defn pprint-code [form]
  (let [string-writer (StringWriter.)]
    (pp/write form
              :dispatch pp/code-dispatch
              :stream string-writer
              :pretty true)
    (str string-writer)))

(defn codeclimate-reporter
  [check-map]
  (let [{:keys [file line expr alt]} check-map
        issue {:type               "issue"
               :check_name         "kibit/suggestion",
               :description        (str "Consider use `" (pprint-code alt) "`"),
               :categories         ["Clarity" "Style"],
               :location           {:path  (str file)
                                    :lines {:begin line
                                            :end   line}},
               :content            (str "Consider using:\n"
                                        "```clojure\n"
                                        (pprint-code alt) "\n"
                                        "```\n"
                                        "instead of:\n"
                                        "```clojure\n"
                                        (pprint-code expr) "\n"
                                        "```")
               :remediation_points 500}]
    (println (json/generate-string issue {:pretty true}))))

(defn exclude? [excluded-paths candidate]
  (some #(.startsWith candidate %) excluded-paths))

(defn target-files
  [dir config]
  (let [excluded (map #(str (io/file dir %)) (:exclude_paths config))]
    (->> (file-seq dir)
         (filter #(.isFile ^File %))
         (remove #(exclude? excluded (str ^File %))))))

(defn analize
  [dir config]
  (let [reporter-name "codeclimate"
        reporters-map (assoc reporters/name-to-reporter reporter-name
                                                        codeclimate-reporter)
        target-files  (target-files dir config)]
    (with-redefs [reporters/name-to-reporter reporters-map]
      (kibit/run target-files "-r" reporter-name))))

(defn -main [& args]
  (let [target-dir  (io/file "/code")
        config-file (io/file "/config.json")
        config-data (when (.exists config-file) (json/parse-stream config-file))]
    (analize target-dir config-data)))
