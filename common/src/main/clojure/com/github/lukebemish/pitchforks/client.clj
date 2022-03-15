(ns com.github.lukebemish.pitchforks.client
  (:require [com.github.lukebemish.clojurewrapper.api.mod :as mod-api]
            [com.github.lukebemish.clojurewrapper.api.platform :as platform]))

(defn init []
  (mod-api/mod-load
    {
     :main
     #(platform/run-platform 'com.github.lukebemish.pitchforks.impl.client/register-renderers)
     }))