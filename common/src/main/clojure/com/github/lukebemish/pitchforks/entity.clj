(ns com.github.lukebemish.pitchforks.entity
  (:require [com.github.lukebemish.clojurewrapper.api.util.functional :as functional]
            [com.github.lukebemish.clojurewrapper.api.util :as util]
            [com.github.lukebemish.pitchforks.shared :as shared]
            [com.github.lukebemish.pitchforks.entity.setup])
  (:import (net.minecraft.world.entity MobCategory EntityType$Builder EntityType$EntityFactory)
           (com.github.lukebemish.pitchforks.entity ThrownPitchfork)))

(def thrown-pitchfork (memoize (fn [] (do
                                        (.build
                                          (doto (EntityType$Builder/of (functional/functional (fn [entity-type level] (new ThrownPitchfork
                                                                                                                           entity-type level)) EntityType$EntityFactory)
                                                                       MobCategory/MISC)
                                            (.sized (float 0.5) (float 0.5))
                                            (.clientTrackingRange (int 4))
                                            (.updateInterval (int 20)))
                                          (.toString (util/resource-location shared/modid "pitchfork")))))))