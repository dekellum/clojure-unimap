(ns unimap.core
  (:import (java.util Map Map$Entry)
           (com.gravitext.htmap KeySpace Key ArrayHTMap UniMap)))

(def
  ^{:tag KeySpace}
  unimap-key-space UniMap/KEY_SPACE)

(defmacro unimap-create-key
  ([sym] `(unimap-create-key ~sym Object))
  ([sym type]
     `(def ^Key ~sym
       (.createGeneric unimap-key-space (name '~sym) ~type))))

(deftype UniMapWrapper [^UniMap umap]) ;forward declare

(defn unimap-wrap
  "Create a mutable but clojure accessable wrapper around a new or
   provided UniMap. With 2, 4, etc. arguments associates
   key,values with a new UniMap."
  ([]
     (UniMapWrapper. (new UniMap)))
  ([^UniMap umap]
      (UniMapWrapper. umap))
  ([key val & kvs]
     (apply assoc (unimap-wrap) key val kvs)))

(defprotocol Wrapper
  (unwrap [this]))

(deftype UniMapWrapper [^UniMap umap]
  Wrapper
  (unwrap [_] umap)

  clojure.lang.ILookup
  (valAt [_ key]
    (.get umap key))
  (valAt [_ key notfound]
    (or (.get umap key) notfound))

  clojure.lang.IPersistentMap
  (assoc [this key val]
    (.set umap key val)
    this)
  (without [this key]
    (.remove umap key)
    this)

  clojure.lang.Associative
  (containsKey [_ key]
    (.containsKey umap key))
  (entryAt [_ key]
    (if-let [v (.get umap key)]
      (clojure.lang.MapEntry. key v)))

  clojure.lang.Counted
  (count [_]
    (.size umap))

  clojure.lang.IPersistentCollection
  (cons [this obj]
    (cond
     (vector? obj) (.set umap (first obj) (second obj))
     (satisfies? Wrapper obj)   (.putAll umap ^ArrayHTMap (unwrap obj))
     (instance? ArrayHTMap obj) (.putAll umap ^ArrayHTMap obj)
     :else (.putAll umap ^Map obj))
    this)
  (empty [_]
    (unimap-wrap)) ; FIXME: or clear this map?
  (equiv [_ other]
    (clojure.lang.Util/equiv umap other))

  clojure.lang.Seqable
  (seq [_]
    (map (fn [^Map$Entry e]
           (clojure.lang.MapEntry. (.getKey e) (.getValue e)))
         (seq umap)))

  clojure.lang.IFn
  (invoke [_ key]
    (.get umap key))
  (invoke [_ key notfound]
    (or (.get umap key) notfound)))
