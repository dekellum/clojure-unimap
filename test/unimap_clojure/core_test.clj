(set! *warn-on-reflection* true)

(ns unimap-clojure.core-test
  (:use clojure.test
        unimap-clojure.core))

(defn sort-seq [coll]
  (sort (seq coll)))

(deftest test-wrap-jmap
  (testing "assoc"
    (let [mw (wrap-jmap)]
      (is (identical? mw (assoc mw :a 1)))
      (is (= '([:a 1]) (seq mw)))))

  (testing "dissoc"
    (let [mw (wrap-jmap :a 1 :b 2)]
      (is (identical? mw (dissoc mw :a)))
      (is (= '([:b 2]) (seq mw)))))

  (testing "accessors"
    (let [mw (wrap-jmap :a 1 :b 2)]
      (testing "find"
        (is (= [:a 1] (find mw :a)))
        (is (= [:b 2] (find mw :b)))
        (is (nil?     (find mw :c))))
      (testing "get"
        (is (= 1  (get mw :a)))
        (is (= 2  (get mw :b)))
        (is (nil? (get mw :c)))
        (is (= 3  (get mw :c 3))))
      (testing "seq"
        (is (= clojure.lang.MapEntry (class (first (seq mw)))))
        (is (= '([:a 1] [:b 2]) (sort-seq mw))))))

  (testing "empty"
    (let [mw (wrap-jmap :a 1)]
      (is (= 0 (count (empty mw))))
      (is (= 1 (count mw)))))

  (testing "conj, cons"
    (is (= '([:a 1] [:b 2]) (sort-seq (conj (wrap-jmap :a 1) {:b 2}))))
    (is (= '([:a 1] [:b 2]) (sort-seq (conj (wrap-jmap :a 1) (first (seq {:b 2}))))))
    (is (= '([:a 1] [:b 2]) (sort-seq (conj (wrap-jmap :a 1) [:b 2]))))
    (is (thrown? IllegalArgumentException (conj (wrap-jmap :a 1) 33)))
    (is (= '([:a 1] [:b 2]) (cons [:a 1] (wrap-jmap :b 2))))))

(deftest test-standard-map
  "FIXME: Just for confirming comparable behavior"
  (is (= {:a 1 :b 2} (conj {:a 1} {:b 2})))
  (is (= {:a 1 :b 2} (conj {:a 1} [:b 2])))
  (is (= {:a 1 :b 2} (conj {:a 1} (first (seq {:b 2})))))
  (is (= '([:a 1] [:b 2]) (cons [:a 1] {:b 2}))))
