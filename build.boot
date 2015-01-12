;; -*- mode: Clojure; -*-
(set-env!
 :source-paths #{"src"}
 :dependencies '[[org.clojure/clojure "1.6.0"     :scope "provided"]
                 [boot/core           "2.0.0-rc1" :scope "provided"]
                 [adzerk/bootlaces    "0.1.8"     :scope "test"]])

(require '[adzerk.bootlaces :refer :all]
         '[zilti.boot-midje :refer [midje]])

(def +version+ "0.1.1")

(bootlaces! +version+)

(task-options!
 pom {:project 'zilti/boot-midje
      :version +version+
      :description "Run midje tests in boot."
      :url "https://github.com/zilti/boot-midje"
      :scm {:url "https://github.com/zilti/boot-midje"}
      :license {"Eclipse Public License" "http://www.eclipse.org/legal/epl-v10.html"}}
 midje {:test-paths #{"test"}})
