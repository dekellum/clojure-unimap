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

(ns unimap.core-test
  (:import (java.util Date Map Map$Entry)
           (com.gravitext.htmap UniMap KeySpace Key)
           (unimap.sample Keys))
  (:use (clojure [set :only (intersection)]
                 test)
         unimap.core)
  (:refer-clojure :rename {type ctype}))

(defkey f1)  ; Object type
(defkey f2)
(defkey f3)

(import-keys) ; def all keys, re-def f1-3 identically and all from
              ; java sample Keys

(defn- jmap-assoc [^Map m & [k v & more]]
  (.put m k v)
  (if more (recur m more) m))

(defn ^Map java-hash-map
  ([] (new java.util.HashMap))
  ([& kvs]
     (apply jmap-assoc (java-hash-map) kvs)))

(defn contains-all? [s1 s2]
  (= (intersection (set s1) s2) s2))

(deftest test-keys
  (is (instance? Key f1))
  (is (instance? Key unimap.core-test/f1))
  (is (= Object (.valueType ^Key f1)))
  (is (contains-all? (.keys unimap-key-space) #{f1 f2 f3})))

(deftest test-import
  (is (instance? Key priority))
  (is (= Float (.valueType ^Key priority))))

(deftest test-wrap
  (testing "assoc"
    (let [mw (unimap-wrap)]
      (is (identical? mw (assoc mw f1 1)))
      (is (= (list [f1 1]) (seq mw))))
    (let [mw (unimap-wrap f1 1)]
      (is (identical? mw (assoc mw f2 2)))
      (is (= #{[f1 1] [f2 2]} (set mw)))))

  (testing "merge"
    (let [mw (unimap-wrap)]
      (is (identical? mw (merge mw {f1 1})))
      (is (= (list [f1 1]) (seq mw)))
      (is (identical? mw (merge mw {f1 2})))
      (is (= (list [f1 2]) (seq mw)))
      (is (= #{[f1 2] [f2 3] [f3 4]} (set (merge mw {f2 3 f3 4})))))
    (let [mw (unimap-wrap)]
      (is (identical? mw (merge mw (unimap-wrap f1 1))))
      (is (= (list [f1 1]) (seq mw)))
      (is (= (list [f1 2]) (seq (merge mw (unimap-wrap f1 2)))))
      (is (= #{[f1 2] [f2 3] [f3 4]}
             (set (merge mw (unimap-wrap f2 3 f3 4)))))))

  (testing "merge from"
    (is (= (list [f1 1]) (seq (merge {} (unimap-wrap f1 1))))))

  (testing "dissoc"
    (let [mw (unimap-wrap f1 1 f2 2)]
      (is (identical? mw (dissoc mw f1)))
      (is (= (list [f2 2]) (seq mw)))))

  (testing "accessors;"
    (let [mw (unimap-wrap f1 1 f2 false)]
      (testing "find"
        (is (= [f1 1]     (find mw f1)))
        (is (= [f2 false] (find mw f2)))
        (is (nil?         (find mw f3))))
      (testing "get"
        (is (= 1     (get mw f1)))
        (is (= false (get mw f2)))
        (is (= false (get mw f2 666)))
        (is (nil?    (get mw f3)))
        (is (= 3     (get mw f3 3))))
      (testing "IFn"
        (is (= 1     (mw f1)))
        (is (= false (mw f2)))
        (is (= false (mw f2 666)))
        (is (nil?    (mw f3)))
        (is (= 3     (mw f3 3))))
      (testing "contains?"
        (is (contains? mw f1))
        (is (not (contains? mw f3))))
      (testing "keys"
        (is (= #{f1 f2} (set (keys mw)))))
      (testing "vals"
        (is (= #{1 false} (set (vals mw)))))
      (testing "map?"
        (is (map? mw)))
      (testing "= equality"
        (is (= (unimap-wrap f1 1 f2 false) mw))
        (is (= (java-hash-map f1 1 f2 false) mw))
        (is (= mw (java-hash-map f1 1 f2 false)))
        (is (= mw {f1 1 f2 false})))
      (testing "equals"
        (is (.equals (unimap-wrap f1 1 f2 false) mw))
        (is (.equals (java-hash-map f1 1 f2 false) mw))
        (is (.equals mw (java-hash-map f1 1 f2 false)))
        (is (.equals ^Map (hash-map f1 1 f2 false) mw)))
      (testing "seq"
        (is (instance? clojure.lang.MapEntry (first (seq mw))))
        (is (= #{[f1 1] [f2 false]} (set (seq mw)))))))

  (testing "empty"
    (let [mw (unimap-wrap f1 1)]
      (is (= 0 (count (empty mw))))
      (is (= 1 (count mw)))))

  (testing "conj, cons"
    (is (= #{[f1 1] [f2 2]} (set (conj (unimap-wrap f1 1) {f2 2}))))
    (is (= #{[f1 1] [f2 2]}
           (set (conj (unimap-wrap f1 1) (first (seq {f2 2}))))))
    (is (= #{[f1 1] [f2 2]} (set (conj (unimap-wrap f1 1) [f2 2]))))
    (is (thrown? ClassCastException (conj (unimap-wrap f1 1) 33)))
    (is (= (list [f1 1] [f2 2]) (cons [f1 1] (unimap-wrap f2 2))))))

(deftest test-java-keys
  (testing "assoc"
    (let [now (Date.),
          mw (unimap-wrap
              type "PAGE"
              status 200
              priority 33.3
              next_visit_after now)
          ref (unimap-wrap
               type "PAGE"
               status 300
               referer mw)]
      (assoc mw references [ref])
      (is (= "PAGE" (mw type)))
      (is (== 200 (mw status)))
      (is (= (float 33.3) (mw priority)))
      (is (= now (mw next_visit_after)))
      (is (identical? now (mw next_visit_after)))
      (is (= mw (ref referer)))
      (is (== 300 (ref status)))
      (is (= ref (first (mw references)))))))
