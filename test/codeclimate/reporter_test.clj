(ns codeclimate.reporter-test
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [codeclimate.reporter :as reporter]
            [clojure.string :as s]
            [cheshire.core :as json]))

(defn run-and-parse [path conf]
  (let [output (with-out-str
                 (reporter/analyze path
                                   conf))]
    (->> (s/split output #"\u0000")
         (map #(json/parse-string % true))
         (remove nil?))))

(deftest finding-files-for-analysis
  (testing "with include_paths + exclude_paths"
    (let [file-list (reporter/build-file-list (io/file "dev-resources/sample/")
                                              {:exclude_paths ["project.clj" "user/**/*"]
                                               :include_paths ["extra/"]})]
      (is (= 2 (count file-list)))
      (is (= ["dev-resources/sample/extra/extra.clj"
              "dev-resources/sample/src/sample/core.clj"]
             (sort (map str file-list))))))
  (testing "no duplicates"
    (let [file-list (reporter/build-file-list  (io/file "dev-resources/sample/")
                                               {:include_paths ["src"]})]
      (is (= 2 (count file-list)))
      (is (= ["dev-resources/sample/src/sample/core.clj"
              "dev-resources/sample/src/sample/user/user.clj"]
             (sort (map str file-list))))))
  (testing "no inclusion and no exclusion"
    (let [file-list (reporter/build-file-list  (io/file "dev-resources/sample/")  {})]
      (is (= 2 (count file-list)))
      (is (= ["dev-resources/sample/src/sample/core.clj"
              "dev-resources/sample/src/sample/user/user.clj"]
             (sort (map str file-list)))))))

(deftest it-analyzes-sample-project
  (testing "with full config"
    (let [project-path  (io/file "dev-resources/sample")
          config  {:include_paths ["extra"]
                   :exclude_paths ["user/**/*" "project.clj"]}
          parsed-output (run-and-parse project-path
                                       config)]
      (is (= 3 (count parsed-output))) ; extra(1 err) + core(2 errs), without excluded
      (is (= {:content
              {:body "<p>Consider using:</p><pre>```(first args)```</pre><p>instead of:</p><pre>```(-> args first)```</pre>"}
              :categories ["Style"]
              :check_name "kibit/suggestion"
              :remediation_points 50000
              :type "issue"
              :description "Non-idiomatic code found in `->`"
              :location {:lines {:begin 5 :end 5}
                         :path "dev-resources/sample/src/sample/core.clj"}}
             (first parsed-output)))
      (testing "it processes correct paths"
        (is (= [{:lines {:begin 5 :end 5}
                 :path "dev-resources/sample/src/sample/core.clj"}
                {:lines {:begin 10 :end 10}
                 :path "dev-resources/sample/src/sample/core.clj"}
                {:lines {:begin 4 :end 4}
                 :path "dev-resources/sample/extra/extra.clj"}]
               (mapv :location parsed-output))))))
  (testing "no config - by default only analyzes src/"
    (let [project-path  (io/file "dev-resources/sample")
          parsed-output (run-and-parse project-path {})]
      (is (= [{:lines {:begin 5 :end 5}
               :path "dev-resources/sample/src/sample/core.clj"}
              {:lines {:begin 10 :end 10}
               :path "dev-resources/sample/src/sample/core.clj"}
              {:lines {:begin 5 :end 5}
               :path "dev-resources/sample/src/sample/user/user.clj"}]
             (mapv :location parsed-output))))))
