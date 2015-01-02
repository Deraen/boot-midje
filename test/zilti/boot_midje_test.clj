(ns zilti.boot-midje-test
  (:use midje.sweet)
  (:require [zilti.boot-midje :as boot-midje]))

(fact "The whole darn thing works"
      (+ 1 1) => 2)

(fact "Blah"
      (+ 5 5) => 10)

(fact "Oh darn."
      (* 5 5) => 25)

(fact "Yeehaa"
      (+ 2 2) => 4)
