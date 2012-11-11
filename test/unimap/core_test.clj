(set! *warn-on-reflection* true)

(ns unimap.core-test
  (:import (com.gravitext.htmap UniMap KeySpace))
  (:use clojure.test unimap.core))

(unimap-create-key foo)

(prn (class foo))
(prn (.valueType foo))
(prn (.keys unimap-key-space))
