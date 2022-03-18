(ns com.github.lukebemish.pitchforks.impl.client
  (:require [com.github.lukebemish.pitchforks.entity :as entity]
            [com.github.lukebemish.clojurewrapper.api.util.functional :as functional]
            [com.github.lukebemish.pitchforks.client.shared :as shared-client])
  (:import (net.fabricmc.fabric.api.client.rendering.v1 EntityRendererRegistry EntityModelLayerRegistry EntityModelLayerRegistry$TexturedModelDataProvider)
           (net.minecraft.client.renderer.entity EntityRendererProvider)
           (net.minecraft.client.model TridentModel)
           (com.github.lukebemish.pitchforks.client ThrownPitchforkRenderer)))

(defn register-renderers []
  (do
    (EntityRendererRegistry/register (entity/thrown-pitchfork) (functional/functional (fn [context] (new ThrownPitchforkRenderer context)) EntityRendererProvider))
    (EntityModelLayerRegistry/registerModelLayer shared-client/pitchfork-layer (functional/functional (fn [] (TridentModel/createLayer)) EntityModelLayerRegistry$TexturedModelDataProvider))))