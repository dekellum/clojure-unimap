(ns unimap-clojure.core)

(deftype JavaMapWrapper [jmap]

  clojure.lang.ILookup
  (valAt [_ key]
    (.get jmap key))
  (valAt [_ key not-found]
    (or (.get jmap key)
        not-found))

  clojure.lang.IPersistentMap
  (assoc [this key val]
    (.put jmap key val)
    this)
  (without [this key]
    (.remove jmap key)
    this)

  clojure.lang.Associative
  (containsKey [_ key]
    (.containsKey jmap key))
  (entryAt [_ key]
    (if-let [v (.get jmap key)]
      (clojure.lang.MapEntry. key v)))

  clojure.lang.Counted
  (count [_]
    (.size jmap))

  clojure.lang.IPersistentCollection
  (cons [this elem]
    (.putAll jmap elem)
    this) ;FIXME : Also support [key val] or MapEntry
  (empty [_]
    (JavaMapWrapper. (new java.util.HashMap) )) ;FIXME: or clear in this case?
  (equiv [_ other]
    (clojure.lang.Util/equiv jmap other))

  clojure.lang.Seqable
  (seq [_]
    (map #(clojure.lang.MapEntry. (.getKey %) (.getValue %)) (seq jmap))))

(defn new-java-map-wrapper [jmap]
  (JavaMapWrapper. jmap))
