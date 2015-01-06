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
  (if (seq sources)
    `(~@baseform :files ~@sources)
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

(defn do-singletest [worker-pods namespace filter]
  (let [namespace (if (seq namespace) namespace [:all])
        form (-> `(midje.repl/load-facts) (add-namespaces namespace) (add-filters filter))]
    (println "Running tests...")
    (pod/with-eval-in (worker-pods :refresh)
      (run-tests* '~form))))

(defn do-autotest [worker-pods dirs filter]
  (let [form (-> `(midje.repl/autotest) (add-sources dirs) (add-filters filter))]
    (pod/with-eval-in (worker-pods :refresh)
      (run-tests* '~form))))

(core/deftask midje
  "Run midje tests in boot."
  [t test-path TESTPATH #{str} "Additional paths where the test files reside (analogous to :source-paths)."
   n namespace NAMESPACE #{sym} "Symbols of the namespaces to run tests in."
   a autotest bool "Use Midje's built-in autotest."
   s source SOURCE #{str} "Sources to be watched by autotest; both filenames and directory names are accepted."
   f filter FILTER #{str} "Midje filters."
   c config CONFIG #{str} "List of midje config files."]
  (let [worker-pods (pod/pod-pool (update-in (core/get-env) [:dependencies] into pod-deps) :init init)]
    (core/set-env! :source-paths (set/union test-path (:source-paths (core/get-env))))
    (core/cleanup (worker-pods :shutdown))
    (core/with-pre-wrap fileset
      (let [fileset (update-fileset fileset test-path)]
        (when (seq config)
          (midje.util.ecosystem/set-config-files! config))
        (if autotest
          (do (do-autotest worker-pods source filter)
              (built-in/wait (core/commit! fileset)))
          (do (do-singletest worker-pods namespace filter)
              (core/commit! fileset)))))))
