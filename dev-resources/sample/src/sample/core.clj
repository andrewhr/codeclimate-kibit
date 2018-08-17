(ns sample.failing)

(defn not-ok-1
  [& args]
  (-> args first) ; unnecessary threading
  )


(defn not-ok-2 [arg]
  "doc string after args"
  (if (string? arg)
    (println arg)
    nil))
