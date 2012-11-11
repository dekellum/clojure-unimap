(ns unimap.jmap)

(deftype JavaMapWrapper [^java.util.Map jmap]) ;forward declare

(defn wrap-jmap
  "Create a mutable but clojure accessable wrapper around a new or
   provided java.util.HashMap. With 2, 4, etc. arguments associates
   key,values with a new java.util.HashMap."
  ([]
     (JavaMapWrapper. (new java.util.HashMap)))
  ([^java.util.Map jmap]
      (JavaMapWrapper. jmap))
  ([key val & kvs]
     (apply assoc (wrap-jmap) key val kvs)))

(deftype JavaMapWrapper [^java.util.Map jmap]

  clojure.lang.ILookup
  (valAt [_ key]
    (.get jmap key))
  (valAt [_ key not-found]
    (or (.get jmap key) not-found))

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
  (cons [this obj]
    (cond
     (instance? java.util.Map obj) (.putAll jmap obj)
     (vector? obj) (.put jmap (first obj) (second obj))
     :else (throw (java.lang.IllegalArgumentException.
                   (str "Can't cons type " (class obj)))))
    this)
  (empty [_]
    (wrap-jmap)) ; FIXME: or clear this map?
  (equiv [_ other]
    (clojure.lang.Util/equiv jmap other))

  clojure.lang.Seqable
  (seq [_]
    (map #(clojure.lang.MapEntry. (.getKey %) (.getValue %)) (seq jmap)))

  clojure.lang.IFn
  (invoke [_ key]
    (.get jmap key))
  (invoke [_ key not-found]
    (or (.get jmap key) not-found)))
