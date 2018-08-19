(ns codeclimate.kibit
  (:require [cheshire.core :as json]
            [clojure.java.io :as io]
            [clojure.string :as s]
            [clojure.tools.cli :as cli]
            [codeclimate.reporter :as cc.reporter])
  (:import (java.io StringWriter File))
  (:gen-class))

(def cli-options
  [["-C" "--config PATH" "Load PATH as a config file"]
   ["-h" "--help"]])

(defn usage [options-summary]
  (->> ["CodeClimate kibit engine"
        ""
        "Usage: java -jar codeclimate-kibit.jar [options] DIR"
        ""
        "Options:"
        options-summary
        ""]
       (s/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (s/join \newline errors)))

(defn run-checks
  "Runs checks against a project.
  Options map should contain `config` key which points
  at config.json passed by CodeClimates executor"
  [target-dir-path
                  {:keys [config] :as options}]
  (let [target-dir (io/file target-dir-path)
        config-file (io/file config)
        config-data (when (and config-file (.exists config-file))
                      (json/parse-string (slurp config-file) true))]
    (cc.reporter/analyze target-dir config-data)))

(defn exit [status message]
  (println message)
  (System/exit status))

(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args cli-options)]
    (cond
      (:help options) (exit 0 (usage summary))
      (not= (count arguments) 1) (exit 1 (usage summary))
      errors (exit 0 (error-msg errors)))
    (run-checks (first arguments) options)))
