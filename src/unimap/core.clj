; Copyright (c) 2012 David Kellum
;
; Licensed under the Apache License, Version 2.0 (the "License"); you
; may not use this file except in compliance with the License.  You may
; obtain a copy of the License at
;
;    http://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
; implied.  See the License for the specific language governing
; permissions and limitations under the License.

(ns unimap.core
  (:import (java.util Map Map$Entry)
           (com.gravitext.htmap KeySpace Key ArrayHTMap UniMap)))

(def ^KeySpace unimap-key-space
  "Access to UniMap.KEY_SPACE"
  UniMap/KEY_SPACE)

(defmacro defkey
  "Create a UniMap.KEY_SPACE Key with the same name as the provided
  symbol and binds it to a global var of the same name in the current
  namespace. An optional (value) type may also be passed (defaults to
  Object)."
  ([sym] `(defkey ~sym Object))
  ([sym type]
     `(def ^:const ^Key ~sym
       (.createGeneric unimap-key-space (name '~sym) ~type))))

(defmacro import-keys []
  "Import all defined UniMap keys into the current namespace as
  variables matching their (lower-case by convention) names."
  `(do ~@(->> (.keys unimap-key-space)
              (map (fn [^Key k]
                     [(symbol (.name k)) k]))
              (map (fn [[sym key]]
                     `(def ^:const ^Key ~sym ~key))))
       nil))

(deftype UniMapWrapper [^ArrayHTMap tmap]) ;forward declare

(defn ^UniMapWrapper unimap-wrap
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
  "General wrapper capabilities"
  (unwrap [this]
    "Return the wrapped ArrayHTMap object")
  (trans-get [this ^Key key]
    "Get key from wrapped map, applying clojure-specific
    transformations to value.")
  (trans-set [this ^Key key value]
    "Set key to value on wrapped map, after possibly tranforming the
    value to the valueType of key."))

(deftype UniMapWrapper [^ArrayHTMap tmap]
  UMapWrapper
  (unwrap [_] tmap)
  (trans-get [_ key]
    (let [vt (.valueType ^Key key)]
      (cond
       (.isAssignableFrom ArrayHTMap vt) (unimap-wrap (.get tmap key))
       :else                             (.get tmap key))))
  (trans-set [this key val]
    (let [vt (.valueType ^Key key)]
      (cond
       (satisfies? UMapWrapper val) (.set tmap key (unwrap val))
       (= Float vt)                 (.set tmap key (float val))
       (= Integer vt)               (.set tmap key (int val))
       :else                        (.set tmap key val))
      this))

  clojure.lang.ILookup
  (valAt [this key]
    (trans-get this key))
  (valAt [this key notfound]
    (let [v (trans-get this key)]
      (if-not (nil? v) v notfound)))

  clojure.lang.IPersistentMap
  (assoc [this key val]
    (trans-set this key val))
  (without [this key]
    (.remove tmap key)
    this)

  clojure.lang.Associative
  (containsKey [_ key]
    (.containsKey tmap key))
  (entryAt [this key]
    (let [v (trans-get this key)]
      (if-not (nil? v)
        (clojure.lang.MapEntry. key v))))

  clojure.lang.Counted
  (count [_]
    (.size tmap))

  clojure.lang.IPersistentCollection
  (cons [this obj]
    (cond
     (vector? obj)                (trans-set this (first obj) (second obj))
     (satisfies? UMapWrapper obj) (.putAll tmap ^ArrayHTMap (unwrap obj))
     (instance? ArrayHTMap obj)   (.putAll tmap ^ArrayHTMap obj)
     :else                        (.putAll tmap ^Map obj))
                                  ; FIXME: trans-set here?
    this)
  (empty [_]
    (unimap-wrap)) ;New empty UniMap
  (equiv [_ omap]
    (if (satisfies? UMapWrapper omap)
      (.equals tmap (unwrap omap))
      (.equals tmap omap)))

  clojure.lang.Seqable
  (seq [_]
    (map (fn [^Map$Entry e]
           (clojure.lang.MapEntry. (.getKey e) (.getValue e)))
         (seq tmap)))

  clojure.lang.IFn
  (invoke [this key]
    (trans-get this key))
  (invoke [this key notfound]
    (let [v (trans-get this key)]
      (if-not (nil? v) v notfound)))

  java.util.Map
  (remove [_ key]
    (.remove tmap key))
  (get [this key]
    (trans-get this key))
  (put [this key val]
    (trans-set this key val))
  (equals [_ omap]
    (if (satisfies? UMapWrapper omap)
      (.equals tmap (unwrap omap))
      (.equals tmap omap)))
  (values [_]
    (.values tmap))
  (hashCode [_]
    (.hashCode tmap))
  (clear [_]
    (.clear tmap))
  (isEmpty [_]
    (.isEmpty tmap))
  (size [_]
    (.size tmap))
  (entrySet [_]
    (.entrySet tmap))
  (putAll [_ omap]
    (cond
     (satisfies? UMapWrapper omap) (.putAll tmap ^ArrayHTMap (unwrap omap))
     (instance? ArrayHTMap omap)   (.putAll tmap ^ArrayHTMap omap)
     :else                         (.putAll tmap ^Map omap)))
  (keySet [_]
    (.keySet tmap))
  (containsValue [_ val]
    (.containsValue tmap val)))
