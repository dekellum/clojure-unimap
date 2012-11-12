(ns unimap.core-test
  (:import (java.util Map Map$Entry)
           (com.gravitext.htmap UniMap KeySpace Key))
  (:use clojure.test unimap.core))

(unimap-create-key f1)
(unimap-create-key f2)
(unimap-create-key f3)

(deftest test-unimap-keys
  (is (instance? Key f1))
  (is (instance? Key unimap.core-test/f1))
  (is (= Object (.valueType ^Key f1)))
  (is (= (list f1 f2 f3) (.keys unimap-key-space))))

(defn sort-seq [coll]
  (sort-by #(.name (.getKey %)) (seq coll)))

(deftest test-unimap-wrap

  (testing "assoc"
    (let [mw (unimap-wrap)]
      (is (identical? mw (assoc mw f1 1)))
      (is (= (list [f1 1]) (seq mw))))
    (let [mw (unimap-wrap f1 1)]
      (is (identical? mw (assoc mw f2 2)))
      (is (= (list [f1 1] [f2 2]) (sort-seq mw)))))

  (testing "merge"
    (let [mw (unimap-wrap)]
      (is (identical? mw (merge mw {f1 1})))
      (is (= (list [f1 1]) (seq mw)))
      (is (identical? mw (merge mw {f1 2})))
      (is (= (list [f1 2]) (seq mw)))
      (is (= (list [f1 2] [f2 3] [f3 4]) (sort-seq (merge mw {f2 3 f3 4})))))
    (let [mw (unimap-wrap)]
      (is (identical? mw (merge mw (unimap-wrap f1 1))))
      (is (= (list [f1 1]) (seq mw)))
      (is (= (list [f1 2]) (seq (merge mw (unimap-wrap f1 2)))))
      (is (= (list [f1 2] [f2 3] [f3 4])
             (sort-seq (merge mw (unimap-wrap f2 3 f3 4)))))))

  (testing "merge from"
    (is (= (list [f1 1]) (seq (merge {} (unimap-wrap f1 1))))))

  (testing "dissoc"
    (let [mw (unimap-wrap f1 1 f2 2)]
      (is (identical? mw (dissoc mw f1)))
      (is (= (list [f2 2]) (seq mw)))))

  (testing "accessors;"
    (let [mw (unimap-wrap f1 1 f2 2)]
      (testing "find"
        (is (= [f1 1] (find mw f1)))
        (is (= [f2 2] (find mw f2)))
        (is (nil?     (find mw f3))))
      (testing "get"
        (is (= 1  (get mw f1)))
        (is (= 2  (get mw f2)))
        (is (nil? (get mw f3)))
        (is (= 3  (get mw f3 3))))
      (testing "IFn"
        (is (= 1  (mw f1)))
        (is (= 2  (mw f2)))
        (is (nil? (mw f3)))
        (is (= 3  (mw f3 3))))
      (testing "contains?"
        (is (contains? mw f1))
        (is (not (contains? mw f3))))
      (testing "keys"
        (is (= (list f1 f2) (sort-by #(.name %) (keys mw)))))
      (testing "vals"
        (is (= '(1 2) (sort (vals mw)))))
      (testing "map?"
        (is (map? mw)))
      (testing "seq"
        (is (instance? clojure.lang.MapEntry (first (seq mw))))
        (is (= (list [f1 1] [f2 2]) (sort-seq mw))))))

  (testing "empty"
    (let [mw (unimap-wrap f1 1)]
      (is (= 0 (count (empty mw))))
      (is (= 1 (count mw)))))

  (testing "conj, cons"
    (is (= (list [f1 1] [f2 2]) (sort-seq (conj (unimap-wrap f1 1) {f2 2}))))
    (is (= (list [f1 1] [f2 2])
           (sort-seq (conj (unimap-wrap f1 1) (first (seq {f2 2}))))))
    (is (= (list [f1 1] [f2 2]) (sort-seq (conj (unimap-wrap f1 1) [f2 2]))))
    (is (thrown? ClassCastException (conj (unimap-wrap f1 1) 33)))
    (is (= (list [f1 1] [f2 2]) (cons [f1 1] (unimap-wrap f2 2))))))
