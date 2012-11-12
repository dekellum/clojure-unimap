(ns unimap.jmap
  (:import (java.util Map Map$Entry HashMap)))

(deftype JavaMapWrapper [^Map jmap]) ;forward declare

(defn jmap-wrap
  "Create a mutable but clojure accessable wrapper around a new or
   provided java.util.HashMap. With 2, 4, etc. arguments associates
   key,values with a new java.util.HashMap."
  ([]
     (JavaMapWrapper. (new HashMap)))
  ([^Map jmap]
      (JavaMapWrapper. jmap))
  ([key val & kvs]
     (apply assoc (jmap-wrap) key val kvs)))

(defprotocol JMapWrapper
  (unwrap [this]))

(deftype JavaMapWrapper [^Map jmap]
  JMapWrapper
  (unwrap [_] jmap)

  clojure.lang.ILookup
  (valAt [_ key]
    (.get jmap key))
  (valAt [_ key notfound]
    (let [v (.get jmap key)]
      (if (not (nil? v)) v notfound)))

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
    (let [v (.get jmap key)]
      (if (not (nil? v))
        (clojure.lang.MapEntry. key v))))

  clojure.lang.Counted
  (count [_]
    (.size jmap))

  clojure.lang.IPersistentCollection
  (cons [this obj]
    (cond
     (vector? obj) (.put jmap (first obj) (second obj))
     (satisfies? JMapWrapper obj) (.putAll jmap (unwrap obj))
     :else                        (.putAll jmap obj))
    this)
  (empty [_]
    (jmap-wrap))
  (equiv [_ other]
    (clojure.lang.Util/equiv jmap other))

  clojure.lang.Seqable
  (seq [_]
    (map (fn [^Map$Entry e]
           (clojure.lang.MapEntry. (.getKey e) (.getValue e)))
         (seq jmap)))

  clojure.lang.IFn
  (invoke [_ key]
    (.get jmap key))
  (invoke [_ key notfound]
    (let [v (.get jmap key)]
      (if (not (nil? v)) v notfound))))
