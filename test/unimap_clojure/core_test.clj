(ns unimap-clojure.core-test
  (:use clojure.test
        unimap-clojure.core))

(deftest java-map-wrapper
  (testing "access methods"
    (let [mw (assoc (new-java-map-wrapper (new java.util.HashMap)) :a 1 :b 2)]
      (is (= [:a 1] (find mw :a)))
      (is (= [:b 2] (find mw :b)))
      (is (nil?     (find mw :c)))
      (is (= 1  (get mw :a)))
      (is (= 2  (get mw :b)))
      (is (nil? (get mw :c)))
      (is (= 3  (get mw :c 3)))
      (is (= clojure.lang.MapEntry (class (first (seq mw)))))
      (is (= (sort( seq {:a 1 :b 2})) (sort (seq mw)))))))
