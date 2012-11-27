(defproject unimap "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [com.gravitext/gravitext-util "1.7.0" ]]
  :java-source-paths [ "java" ]
  :aot [unimap.jmap unimap.core]
  :warn-on-reflection true)
