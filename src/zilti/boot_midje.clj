(ns zilti.boot-midje
  {:boot/export-tasks true}
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

(defn do-singletest [worker-pods namespaces filters level]
  (util/info "Running tests...\n"
    (pod/with-eval-in (worker-pods :refresh)
      (midje.repl/load-facts
        ; Double quote each symbol so that when they are unquoted they are still quoted once
        ~@(if (seq namespaces) (map #(eval `''~%) namespaces) [])
        ~@(if (seq filters)    (map (comp eval read-string) filters) [])
        ~@(if level            [level] [])))))

(defn do-autotest [worker-pods filters]
  (pod/with-eval-in (worker-pods :refresh)
    (midje.repl/autotest
      :files ~@(core/get-env :directories)
      ~@(if (seq filters) [:filter filters] []))))

(core/deftask midje
  "Run midje tests in boot."
  [n namespaces NAMESPACE #{sym} "symbols of the namespaces to run tests in."
   a autotest             bool   "Use Midje's built-in autotest."
   f filters    FILTER    #{str} "midje filters. Only facts matching one or more of the arguments are loaded."
   c config     CONFIG    #{str} "list of midje config files."
   l level      LEVEL     int    "Set Midje's verbosity level."]
  (let [worker-pods (pod/pod-pool (update-in (core/get-env) [:dependencies] into pod-deps) :init (partial init config))]
    (core/cleanup (worker-pods :shutdown))
    (core/with-pre-wrap fileset
      (if autotest
        (do-autotest worker-pods filters)
        (do-singletest worker-pods namespaces filters level))
      fileset)))
