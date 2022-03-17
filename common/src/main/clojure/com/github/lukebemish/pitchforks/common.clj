(ns com.github.lukebemish.pitchforks.common
  (:import (org.apache.logging.log4j LogManager Logger)
           (net.minecraft.world.entity EntityType$Builder EntityType$EntityFactory MobCategory)
           (com.github.lukebemish.pitchforks.entity thrownpitchfork))
  (:require [com.github.lukebemish.clojurewrapper.api.mod :as mod-api]
            [com.github.lukebemish.clojurewrapper.api.util :as util]
            [com.github.lukebemish.clojurewrapper.api.sided :as sided]
            [com.github.lukebemish.pitchforks.item :as item]
            [com.github.lukebemish.clojurewrapper.api.util.functional :as functional]
            [com.github.lukebemish.pitchforks.shared :as shared]))

(def ^Logger logger (LogManager/getLogger shared/modid))

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
      {(util/resource-location shared/modid "pitchfork") #(.build
                                                     (doto (EntityType$Builder/of (functional/functional (fn [entity-type level] (thrownpitchfork. entity-type level)) EntityType$EntityFactory)
                                                                                  MobCategory/MISC)
                                                       (.sized (float 0.5) (float 0.5))
                                                       (.clientTrackingRange (int 4))
                                                       (.updateInterval (int 20)))
                                                     (.toString (util/resource-location shared/modid "pitchfork")))}}
     }))
