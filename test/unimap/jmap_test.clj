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

(ns unimap.jmap-test
  (:use clojure.test unimap.jmap))

(deftest test-jmap-wrap

  (testing "assoc"
    (let [mw (jmap-wrap)]
      (is (identical? mw (assoc mw :a 1)))
      (is (= (list [:a 1]) (seq mw))))
    (let [mw (jmap-wrap :a 1)]
      (is (identical? mw (assoc mw :b 2)))
      (is (= #{[:a 1] [:b 2]} (set mw)))))

  (testing "merge"
    (let [mw (jmap-wrap)]
      (is (identical? mw (merge mw {:a 1})))
      (is (= (list [:a 1]) (seq mw)))
      (is (identical? mw (merge mw {:a 2})))
      (is (= (list [:a 2]) (seq mw)))
      (is (= #{[:a 2] [:b 3] [:c 4]} (set (merge mw {:b 3 :c 4})))))
    (let [mw (jmap-wrap)]
      (is (identical? mw (merge mw (jmap-wrap :a 1))))
      (is (= (list [:a 1]) (seq mw)))
      (is (= (list [:a 2]) (seq (merge mw (jmap-wrap :a 2)))))
      (is (= #{[:a 2] [:b 3] [:c 4]}
             (set (merge mw (jmap-wrap :b 3 :c 4)))))))

  (testing "merge from"
    (is (= (list [:a 1]) (seq (merge {} (jmap-wrap :a 1))))))

  (testing "dissoc"
    (let [mw (jmap-wrap :a 1 :b 2)]
      (is (identical? mw (dissoc mw :a)))
      (is (= (list [:b 2]) (seq mw)))))

  (testing "accessors;"
    (let [mw (jmap-wrap :a 1 :b false)]
      (testing "find"
        (is (= [:a 1]     (find mw :a)))
        (is (= [:b false] (find mw :b)))
        (is (nil?         (find mw :c))))
      (testing "get"
        (is (= 1     (get mw :a)))
        (is (= false (get mw :b)))
        (is (= false (get mw :b 666)))
        (is (nil?    (get mw :c)))
        (is (= 3     (get mw :c 3))))
      (testing "IFn"
        (is (= 1     (mw :a)))
        (is (= false (mw :b)))
        (is (= false (mw :b 666)))
        (is (nil?    (mw :c)))
        (is (= 3     (mw :c 3))))
      (testing "contains?"
        (is (contains? mw :a))
        (is (not (contains? mw :c))))
      (testing "keys"
        (is (= #{:a :b} (set (keys mw)))))
      (testing "vals"
        (is (= #{1 false} (set (vals mw)))))
      (testing "map?"
        (is (map? mw)))
      (testing "seq"
        (is (instance? clojure.lang.MapEntry (first (seq mw))))
        (is (= #{[:a 1] [:b false]} (set (seq mw)))))))

  (testing "empty"
    (let [mw (jmap-wrap :a 1)]
      (is (= 0 (count (empty mw))))
      (is (= 1 (count mw)))))

  (testing "conj, cons"
    (is (= #{[:a 1] [:b 2]} (set (conj (jmap-wrap :a 1) {:b 2}))))
    (is (= #{[:a 1] [:b 2]}
           (set (conj (jmap-wrap :a 1) (first (seq {:b 2}))))))
    (is (= #{[:a 1] [:b 2]} (set (conj (jmap-wrap :a 1) [:b 2]))))
    (is (thrown? ClassCastException (conj (jmap-wrap :a 1) 33)))
    (is (= (list [:a 1] [:b 2]) (cons [:a 1] (jmap-wrap :b 2))))))

(deftest test-standard-map
  "FIXME: Just for confirming behavior of clojure persistent maps"
  (is (= 1 ({:a 1} :a)))
  (is (= {:a 1 :b 2} (conj {:a 1} {:b 2})))
  (is (= {:a 1 :b 2} (conj {:a 1} [:b 2])))
  (is (= {:a 1 :b 2} (conj {:a 1} (first (seq {:b 2})))))
  (is (= (list [:a 1] [:b 2]) (cons [:a 1] {:b 2}))))
