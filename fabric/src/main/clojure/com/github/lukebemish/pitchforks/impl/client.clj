(ns com.github.lukebemish.pitchforks.impl.client
  (:require [com.github.lukebemish.pitchforks.entity :as entity]
            [com.github.lukebemish.clojurewrapper.api.util.functional :as functional]
            [com.github.lukebemish.pitchforks.client.shared :as shared-client]
            [com.github.lukebemish.pitchforks.item :as item]
            [com.github.lukebemish.clojurewrapper.api.util :as util]
            [com.github.lukebemish.pitchforks.shared :as shared])
  (:import (net.fabricmc.fabric.api.client.rendering.v1 EntityRendererRegistry EntityModelLayerRegistry EntityModelLayerRegistry$TexturedModelDataProvider)
           (net.minecraft.client.renderer.entity EntityRendererProvider)
           (net.minecraft.client.model TridentModel)
           (com.github.lukebemish.pitchforks.client ThrownPitchforkRenderer)
           (net.fabricmc.fabric.api.object.builder.v1.client.model FabricModelPredicateProviderRegistry)
           (net.minecraft.client.renderer.item ClampedItemPropertyFunction)
           (net.minecraft.world.entity LivingEntity)))

(defn register-renderers []
  (do
    (EntityRendererRegistry/register (entity/thrown-pitchfork) (functional/functional (fn [context] (new ThrownPitchforkRenderer context)) EntityRendererProvider))
    (EntityModelLayerRegistry/registerModelLayer shared-client/pitchfork-layer (functional/functional (fn [] (TridentModel/createLayer)) EntityModelLayerRegistry$TexturedModelDataProvider))
    (FabricModelPredicateProviderRegistry/register (item/pitchfork-item) (util/resource-location "throwing")
                                                   (functional/functional (fn [item-stack client-level living-entity i]
                                                                            (if (and (not (nil? living-entity)) (.isUsingItem ^LivingEntity living-entity) (= (.getUseItem ^LivingEntity living-entity) item-stack)) (float 1) (float 0))) ClampedItemPropertyFunction))))