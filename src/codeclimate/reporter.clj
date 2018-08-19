(ns codeclimate.reporter
  (:require [cheshire.core :as json]
            [clojure.java.io :as io]
            [clojure.pprint :as pp]
            [clojure.string :as s]
            kibit.check
            kibit.driver)
  (:import (java.io StringWriter File)))

(defn- >errf
  "Like printf but prints to STDERR"
  [fmt & args]
  (.println *err* (apply format (apply conj [fmt] args))))

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
               :description        (str "Non-idiomatic code found in `" expr "`")
               :categories         ["Style"]
               :location           {:path  (str file)
                                    :lines {:begin line
                                            :end   line}}
               :content            {:body (template-solution alt expr)}
               :remediation_points 50000}]
    (println (str (json/generate-string issue) "\0"))))

(defn build-file-list
  "Builds a list of files to analyze by kibit.
  Initial list is based on `src` dir +  `include_paths`.
  Then it's reduced by taking paths found in `exclude_paths` key.

  Since CodeClimate config uses globbing syntax (e.g. test/**/*)
  it has to deal with it somehow- for now it converts path matchers to
  regexes and uses that for matching.
  It could use JDK7+ NIO globbing support.
  All paths are looked up against project-dir"
  [project-dir
   {:keys [exclude_paths include_paths] :as config}]
  (let [exclusion-patterns (->> exclude_paths
                                ;; convert *
                                (map #(s/replace % #"\*{1,}" ".+"))
                                ;; make dir separators optional
                                (map #(s/replace % #"/" "/?"))
                                ;; prepend project dir
                                (map #(re-pattern (str "^" project-dir ".*/?" %))))
        ;; src is a standard for most Clojure projects, so lets use that
        include-paths (apply conj ["src"] include_paths)
        ;; prepend project dir
        all-paths (map #(str project-dir "/" %) include-paths)
        all-files (->> (map io/file all-paths)
                       (map kibit.driver/find-clojure-sources-in-dir)
                       flatten
                       (map str)
                       distinct)
        filtered-paths (remove (fn remover [path]
                                 (some (fn matcher [rgx]
                                         (re-find rgx path)) exclusion-patterns))
                               all-files)]
    (mapv io/file filtered-paths)))

(defn- do-analysis
  "Shortcut for easier mapping"
  [path]
  (>errf "[cc-kibit] analyzing %s" (str path))
  (try
    (kibit.check/check-file path
                            :reporter codeclimate-reporter)
    (catch Exception e
      (>errf "[cc-kibit] failed to analyze %s" (str path)))))

(defn analyze
  "Runs analysis for all Clojure files found in `project-dir`
  Config is parsed config.json"
  [project-dir config]
  (let [files-to-analyze (build-file-list project-dir config)]
    (mapv do-analysis files-to-analyze)))
