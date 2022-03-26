(ns com.github.lukebemish.pitchforks.common
  (:import (org.apache.logging.log4j LogManager Logger))
  (:require [com.github.lukebemish.clojurewrapper.api.mod :as mod-api]
            [com.github.lukebemish.clojurewrapper.api.util :as util]
            [com.github.lukebemish.clojurewrapper.api.sided :as sided]
            [com.github.lukebemish.pitchforks.item :as item]
            [com.github.lukebemish.pitchforks.shared :as shared]
            [com.github.lukebemish.pitchforks.entity :as entity]))

(defn init []
  (mod-api/mod-load
    {:main
     (fn []
       ())
     :client #(sided/run-sided 'com.github.lukebemish.pitchforks.client/init)
     :registries
     {:items
      {(util/resource-location shared/modid "pitchfork") #(item/pitchfork-item)
       }
      :entity-types
      {(util/resource-location shared/modid "pitchfork") #(entity/thrown-pitchfork)}}
     }))
