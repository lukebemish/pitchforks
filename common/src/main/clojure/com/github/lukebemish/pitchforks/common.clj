(ns com.github.lukebemish.pitchforks.common
  (:import (org.apache.logging.log4j LogManager Logger))
  (:require [com.github.lukebemish.clojurewrapper.api.mod :as mod-api]
            [com.github.lukebemish.clojurewrapper.api.util :as util]
            [com.github.lukebemish.clojurewrapper.api.sided :as sided]
            [com.github.lukebemish.pitchforks.item :as item]))

(def ^String modid "pitchforks")
(def ^Logger logger (LogManager/getLogger modid))

(defn init []
  (mod-api/mod-load
    {:main
     (fn []
       ())
     :client #(sided/run-sided 'com.github.lukebemish.pitchforks.client/init)
     :registries
     {:items
      {(util/resource-location modid "pitchfork") #(item/pitchfork-item)
       }}
     }))
