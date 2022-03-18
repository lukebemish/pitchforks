(ns com.github.lukebemish.pitchforks.client.thrownpitchforkrenderer
  (:import (com.mojang.blaze3d.vertex PoseStack)
           (com.mojang.math Vector3f)
           (net.minecraft.util Mth)
           (net.minecraft.world.entity Entity)
           (net.minecraft.client.renderer.entity ItemRenderer EntityRenderer)
           (net.minecraft.client.model Model TridentModel)
           (net.minecraft.client.renderer.texture OverlayTexture))
  (:require [com.github.lukebemish.pitchforks.entity.thrownpitchfork :as thrownpitchfork]
            [com.github.lukebemish.clojurewrapper.api.util :as util]
            [com.github.lukebemish.pitchforks.shared :as shared]
            [com.github.lukebemish.pitchforks.client.shared :as shared-client])
  (:gen-class
    :name com.github.lukebemish.pitchforks.client.ThrownPitchforkRenderer
    :extends net.minecraft.client.renderer.entity.EntityRenderer
    :state state
    :init init
    :exposes-methods {render p-render}))


(defn -init [context] [[context] (TridentModel. (.bakeLayer context shared-client/pitchfork-layer))])

(defn -render [this ^Entity thrown f g ^PoseStack posestack source i]
  (do
    (.pushPose posestack)
    (.mulPose posestack (.rotationDegrees Vector3f/YP (- (Mth/lerp ^float g ^float (.yRotO thrown) ^float (.getYRot thrown)) (float 90))))
    (.mulPose posestack (.rotationDegrees Vector3f/ZP (+ (Mth/lerp ^float g ^float (.xRotO thrown) ^float (.getXRot thrown)) (float 90))))
    (let [vertex-consumer (ItemRenderer/getFoilBufferDirect source (.renderType ^Model (.state this) (.getTextureLocation this thrown)) false (thrownpitchfork/isFoil thrown))]
      (.renderToBuffer (.state this) posestack vertex-consumer i OverlayTexture/NO_OVERLAY (float 1) (float 1) (float 1) (float 1)))
    (.popPose posestack)
    (.p-render this thrown f g posestack source i)))

(defn -getTextureLocation [this thrown] (util/resource-location shared/modid "textures/entity/pitchfork.png"))