(ns zilti.boot-midje
  {:boot/export-tasks true}
  (:refer-clojure :exclude [test])
  (:require [boot.core :as core]
            [clojure.set :as set]
            midje.util.ecosystem
            midje.config
            midje.repl))



(core/deftask midje
  "Run midje tests in boot."
  [t test-paths TESTPATHS #{str} "Additional paths where the test files reside. Alternatively, use :test-paths in set-env!"
   n namespaces NAMESPACES #{sym} "Symbols of the namespaces to run tests in."
   a autotest bool "Use midje's autotest functionality."
   f filter FILTER #{str} "Midje filters."]
  (core/with-pre-wrap fileset
    (let [namespaces (if (seq namespaces) namespaces [])]
      )))
