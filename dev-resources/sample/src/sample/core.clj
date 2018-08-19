(ns sample.core)

(defn not-ok-1
  [& args]
  (-> args first) ; unnecessary threading
  )


(defn not-ok-2 [arg]
  (if (string? arg)
    (println arg)
    nil))
