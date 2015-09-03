(defproject codeclimate-kibit "0.1.0-SNAPSHOT"
  :description "Code Climate Analysis Engine for clojure kibit"
  :url "http://github.com/andrewhr/codeclimate-kibit"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [jonase/kibit "0.1.2"]
                 [cheshire "5.5.0"]]
  :aot :all
  :main codeclimate.kibit
  :uberjar-name "codeclimate-kibit.jar")
