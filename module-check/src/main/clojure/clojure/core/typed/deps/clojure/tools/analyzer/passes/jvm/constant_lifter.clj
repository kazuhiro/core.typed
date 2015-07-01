(ns clojure.core.typed.deps.clojure.tools.analyzer.passes.jvm.constant-lifter
  (:require [clojure.core.typed.deps.clojure.tools.analyzer.passes.constant-lifter :as orig]
            [clojure.core.typed.deps.clojure.tools.analyzer :refer [-analyze]]
            [clojure.core.typed.deps.clojure.tools.analyzer.utils :refer [constant? classify]]
            [clojure.core.typed.deps.clojure.tools.analyzer.passes.jvm.analyze-host-expr :refer [analyze-host-expr]]
            [clojure.core.typed.deps.clojure.tools.analyzer.passes.elide-meta :refer [elide-meta]]))

(defn constant-lift*
  [ast]
  (if (= :var (:op ast))
    (let [{:keys [var env form]} ast]
     (if (constant? var)
       (let [val @var]
         (assoc (-analyze :const val env (classify val))
           :form form))
       ast))
    (orig/constant-lift ast)))

(defn constant-lift
  "Like clojure.core.typed.deps.clojure.tools.analyzer.passes.constant-lifter/constant-lift but
   transforms also :var nodes where the var has :const in the metadata
   into :const nodes and preserves tag info"
  {:pass-info {:walk :post :depends #{} :after #{#'elide-meta #'analyze-host-expr}}}
  [ast]
  (merge (constant-lift* ast)
         (select-keys ast [:tag :o-tag :return-tag :arglists])))