(ns unimap.core
  (:import (com.gravitext.htmap UniMap KeySpace Key)))

(def
  ^{:tag KeySpace}
  unimap-key-space UniMap/KEY_SPACE)

(defmacro unimap-create-key
  ([sym] `(unimap-create-key ~sym java.lang.Object))
  ([sym type]
     `(def
       ^{:tag Key}
       ~sym
       (.createGeneric unimap-key-space (name '~sym) ~type))))
