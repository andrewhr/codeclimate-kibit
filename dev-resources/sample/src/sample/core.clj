(ns sample.core)

(defn not-ok-1
  [& args]
  (-> args first) ; unnecessary threading
  )


(defn not-ok-2 [arg]
  (if (string? arg)
    (println arg)
    nil))

(defn- cb [a1 a2] (str a1 a2))

(defn broken-anonymous-fn [arg]
  (map #(cb %1 %2) args))
