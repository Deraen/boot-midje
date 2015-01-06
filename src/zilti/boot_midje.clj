(ns zilti.boot-midje
  {:boot/export-tasks true}
  (:refer-clojure :exclude [test])
  (:require [boot.core :as core]
            [boot.pod :as pod]
            [boot.task.built-in :as built-in]
            [clojure.set :as set]
            [clojure.java.io :as io]
            midje.util.ecosystem
            midje.config
            midje.repl))

(def pod-deps '[])

(defn init [fresh-pod]
  (doto fresh-pod
    (pod/with-eval-in
      (require 'midje.repl)
      (defn run-tests* [form]
        (eval form)))))

(defn add-namespaces [baseform namespaces]
  `(~@baseform ~@namespaces))

(defn add-filters [baseform filters]
  (if (seq filters)
    `(~@baseform :filter ~@filters)
    baseform))

(defn add-sources [baseform sources]
  `(~@baseform :files ~@(if (seq sources) sources (:source-paths (core/get-env)))))

(defn add-verbosity-level [baseform level]
  (if level
    `(~@baseform ~level)
    baseform))

(defn update-fileset [fileset test-path]
  (let [fileset (loop [test-path test-path
                       fileset fileset]
                  (if-not (empty? test-path)
                    (recur (rest test-path)
                           (core/add-source fileset (io/file (first test-path))))
                    fileset))]
    (core/commit! fileset)
    
    (alter-var-root #'midje.util.ecosystem/leiningen-paths-var
                    (constantly (map str (core/input-dirs fileset))))
    
    fileset))

(defn do-singletest [worker-pods namespace filter level]
  (let [namespace (if (seq namespace) namespace [:all])
        form (-> `(midje.repl/load-facts) (add-namespaces namespace) (add-filters filter) (add-verbosity-level level))]
    (println "Running tests...")
    (pod/with-eval-in (worker-pods :refresh)
      (run-tests* '~form))))

(defn do-autotest [worker-pods dirs filter]
  (let [form (-> `(midje.repl/autotest) (add-sources dirs) (add-filters filter))]
    (pod/with-eval-in (worker-pods :refresh)
      (run-tests* '~form))))

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
  (let [worker-pods (pod/pod-pool (update-in (core/get-env) [:dependencies] into pod-deps) :init init)]
    (core/set-env! :source-paths (set/union test-paths (:source-paths (core/get-env)) (:resource-paths (core/get-env))))
    (core/cleanup (worker-pods :shutdown))
    (core/with-pre-wrap fileset
      (println "Preparing environment...")
      (when (seq config)
        (midje.util.ecosystem/set-config-files! config))
      (if autotest
        (do (println "Press \"Ctrl-C\" to terminate.")
            (do-autotest worker-pods sources filters)
            @(promise))
        (do (do-singletest worker-pods namespaces filters level)
            (core/commit! fileset))))))
