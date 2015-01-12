(ns zilti.boot-midje
  {:boot/export-tasks true}
  (:refer-clojure :exclude [test])
  (:require [boot.core :as core]
            [boot.pod :as pod]
            [boot.util :as util]
            [boot.task.built-in :as built-in]
            [clojure.set :as set]
            [clojure.java.io :as io]))

(def pod-deps '[[midje "1.6.3"]])

(defn init [config fresh-pod]
  (doto fresh-pod
    (pod/with-eval-in
      (require 'midje.repl 'midje.util.ecosystem)
      (alter-var-root #'midje.util.ecosystem/leiningen-paths-var (constantly ~(vec (core/get-env :directories))))
      (when (seq ~config)
        (midje.util.ecosystem/set-config-files! ~config)))))

(defn do-singletest [worker-pods namespace filters level]
  (util/info "Running tests...\n"
    (pod/with-eval-in (worker-pods :refresh)
      (midje.repl/load-facts ~@(cond-> []
                                 (seq namespace) (concat namespace)
                                 (seq filters) (concat [:filter filters])
                                 level (concat [level]))))))

(defn do-autotest [worker-pods sources filters]
  (pod/with-eval-in (worker-pods :refresh)
    (midje.repl/autotest ~@(cond-> (cons :files (seq (if (seq sources) sources (core/get-env :directories))) )
                             (seq filters) (concat [:filter filters])
                             ))))

(core/deftask midje
  "Run midje tests in boot."
  [t test-paths TESTPATH #{str} "additional paths where the test files reside (analogous to :source-paths).
                              A partial namespace ending in a '*' will load all sub-namespaces.
                              Example: `(load-facts 'midje.ideas.*)`
`"
   n namespaces NAMESPACE #{sym} "symbols of the namespaces to run tests in."
   a autotest bool "Use Midje's built-in autotest."
   s sources SOURCE #{str} "sources to be watched by autotest; both filenames and directory names are accepted."
   f filters FILTER #{str} "midje filters. Only facts matching one or more of the arguments are loaded. The filter arguments are:

                              :keyword      -- Does the metadata have a truthy value for the keyword?
                              \"string\"      -- Does the fact's name contain the given string?
                              #\"regex\"      -- Does any part of the fact's name match the regex?
                              a function    -- Does the function return a truthy value when given the fact's metadata?
`"
   c config CONFIG #{str} "list of midje config files."
   l level LEVEL int "Set Midje's verbosity level using one of the following options:

                              :print-normally    (0) -- failures and a summary.
                              :print-no-summary (-1) -- failures only.
                              :print-nothing    (-2) -- nothing is printed.
                                                     -- (but return value can be checked)
                              :print-namespaces  (1) -- print the namespace for each group of facts.
                              :print-facts       (2) -- print fact descriptions in addition to namespaces.

                             "]
  (let [worker-pods (pod/pod-pool (update-in (core/get-env) [:dependencies] into pod-deps) :init (partial init config))]
    (core/cleanup (worker-pods :shutdown))
    (core/with-pre-wrap fileset
      (util/info "Preparing environment...\n")
      (if autotest
        (do-autotest worker-pods sources filters)
        (do-singletest worker-pods namespaces filters level))
      fileset)))
