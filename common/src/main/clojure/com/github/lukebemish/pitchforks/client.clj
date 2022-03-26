(ns com.github.lukebemish.pitchforks.client
  (:require [com.github.lukebemish.clojurewrapper.api.mod :as mod-api]
            [com.github.lukebemish.clojurewrapper.api.platform :as platform])
  (:import (net.minecraft.client.resources.model ModelBakery)))

(defn init []
  (mod-api/mod-load
    {
     :main
     #(do
        (platform/run-platform 'com.github.lukebemish.pitchforks.impl.client/register-renderers))
     }))