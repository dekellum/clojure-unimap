(ns unimap.core
  (:import (com.gravitext.htmap UniMap KeySpace Key)))

(def
  ^{:tag KeySpace}
  unimap-key-space UniMap/KEY_SPACE)

(defmacro unimap-create-key
  ([sym] `(unimap-create-key ~sym java.lang.Object))
  ([sym type]
     `(def ^{:tag Key} ~sym
       (.createGeneric unimap-key-space (name '~sym) ~type))))

(deftype UniMapWrapper [^UniMap umap]) ;forward declare

(defn unimap-wrap
  "Create a mutable but clojure accessable wrapper around a new or
   provided HTMap. With 2, 4, etc. arguments associates
   key,values with a new UniMap."
  ([]
     (UniMapWrapper. (new UniMap)))
  ([^java.util.Map umap]
      (UniMapWrapper. umap))
  ([key val & kvs]
     (apply assoc (unimap-wrap) key val kvs)))

(deftype UniMapWrapper [^UniMap umap]

  clojure.lang.ILookup
  (valAt [_ key]
    (.get umap key))
  (valAt [_ key not-found]
    (or (.get umap key) not-found))

  clojure.lang.IPersistentMap
  (assoc [this key val]
    (.put umap key val)
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
     (instance? java.util.Map obj) (.putAll umap obj)
     (vector? obj) (.put umap (first obj) (second obj))
     :else (throw (java.lang.IllegalArgumentException.
                   (str "Can't cons type " (class obj)))))
    this)
  (empty [_]
    (unimap-wrap)) ; FIXME: or clear this map?
  (equiv [_ other]
    (clojure.lang.Util/equiv umap other))

  clojure.lang.Seqable
  (seq [_]
    (map #(clojure.lang.MapEntry. (.getKey %) (.getValue %)) (seq umap)))

  clojure.lang.IFn
  (invoke [_ key]
    (.get umap key))
  (invoke [_ key not-found]
    (or (.get umap key) not-found)))
