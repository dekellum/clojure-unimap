(ns unimap.core
  (:import (java.util Map Map$Entry)
           (com.gravitext.htmap KeySpace Key ArrayHTMap UniMap)))

(def
  ^KeySpace
  unimap-key-space UniMap/KEY_SPACE)

(defmacro unimap-create-key
  ([sym] `(unimap-create-key ~sym Object))
  ([sym type]
     `(def ^Key ~sym
       (.createGeneric unimap-key-space (name '~sym) ~type))))

(deftype UniMapWrapper [umap]) ;forward declare

(defn unimap-wrap
  "Create a mutable but clojure accessable wrapper around a new UniMap
   or any provided ArrayHTMap. With 2, 4, etc. arguments associates
   key,values with a new UniMap."
  ([]
     (unimap-wrap (new UniMap)))
  ([^ArrayHTMap tmap]
      (UniMapWrapper. tmap))
  ([key val & kvs]
     (apply assoc (unimap-wrap) key val kvs)))

(defprotocol UMapWrapper
  (unwrap [this]))

(deftype UniMapWrapper [^ArrayHTMap tmap]
  UMapWrapper
  (unwrap [_] tmap)

  clojure.lang.ILookup
  (valAt [_ key]
    (.get tmap key))
  (valAt [_ key notfound]
    (let [v (.get tmap key)]
      (if-not (nil? v) v notfound)))

  clojure.lang.IPersistentMap
  (assoc [this key val]
    (.set tmap key val)
    this)
  (without [this key]
    (.remove tmap key)
    this)

  clojure.lang.Associative
  (containsKey [_ key]
    (.containsKey tmap key))
  (entryAt [_ key]
    (let [v (.get tmap key)]
      (if-not (nil? v)
        (clojure.lang.MapEntry. key v))))

  clojure.lang.Counted
  (count [_]
    (.size tmap))

  clojure.lang.IPersistentCollection
  (cons [this obj]
    (cond
     (vector? obj) (.set tmap (first obj) (second obj))
     (satisfies? UMapWrapper obj) (.putAll tmap ^ArrayHTMap (unwrap obj))
     (instance? ArrayHTMap obj)   (.putAll tmap ^ArrayHTMap obj)
     :else                        (.putAll tmap ^Map obj))
    this)
  (empty [_]
    (unimap-wrap)) ;New empty UniMap
  (equiv [_ other]
    (clojure.lang.Util/equiv tmap other))

  clojure.lang.Seqable
  (seq [_]
    (map (fn [^Map$Entry e]
           (clojure.lang.MapEntry. (.getKey e) (.getValue e)))
         (seq tmap)))

  clojure.lang.IFn
  (invoke [_ key]
    (.get tmap key))
  (invoke [_ key notfound]
    (let [v (.get tmap key)]
      (if-not (nil? v) v notfound))))
