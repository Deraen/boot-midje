(ns zilti.boot-midje-test
  (:use midje.sweet)
  (:require [zilti.boot-midje :as boot-midje]))

(fact "Adding namespaces"
      (boot-midje/add-namespaces `(midje.repl/load-facts) [:all]) => `(midje.repl/load-facts :all)
      (boot-midje/add-namespaces `(midje.repl/load-facts) '[a.b c.d]) => `(midje.repl/load-facts a.b c.d))

(fact "Adding filters"
      (boot-midje/add-filters `(midje.repl/load-facts) nil) => `(midje.repl/load-facts)
      (boot-midje/add-filters `(midje.repl/load-facts) [:aa "bcd"]) => `(midje.repl/load-facts :filter :aa "bcd"))

(fact "Random test"
      (+ 1 1) => 2)
