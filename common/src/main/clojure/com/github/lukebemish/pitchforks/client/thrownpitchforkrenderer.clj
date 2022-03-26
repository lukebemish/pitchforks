(ns com.github.lukebemish.pitchforks.client.thrownpitchforkrenderer
  (:require [com.github.lukebemish.pitchforks.entity.thrownpitchfork :as thrownpitchfork]
            [com.github.lukebemish.clojurewrapper.api.util :as util]
            [com.github.lukebemish.pitchforks.shared :as shared]
            [com.github.lukebemish.pitchforks.client.shared :as shared-client])
  (:import (com.mojang.blaze3d.vertex PoseStack)
           (com.mojang.math Vector3f)
           (net.minecraft.util Mth)
           (net.minecraft.world.entity Entity)
           (net.minecraft.client.renderer.entity EntityRendererProvider$Context ItemRenderer)
           (net.minecraft.client.renderer.texture TextureAtlas OverlayTexture)
           (com.github.lukebemish.pitchforks.entity ThrownPitchfork)
           (net.minecraft.client.renderer.block.model ItemTransforms$TransformType))
  (:gen-class
    :name com.github.lukebemish.pitchforks.client.ThrownPitchforkRenderer
    :extends net.minecraft.client.renderer.entity.EntityRenderer
    :state state
    :init init
    :exposes-methods {render prender}))


(defn -init [^EntityRendererProvider$Context context] [[context] (.getItemRenderer context)])

(defn -render [this ^Entity thrown f g ^PoseStack posestack source i]
  (do
    (.pushPose posestack)
    (.mulPose posestack (.rotationDegrees Vector3f/YP (- (Mth/lerp ^float g ^float (.yRotO thrown) ^float (.getYRot thrown)) (float 90))))
    (.mulPose posestack (.rotationDegrees Vector3f/ZP (+ (Mth/lerp ^float g ^float (.xRotO thrown) ^float (.getXRot thrown)) (float 90))))
    (.mulPose posestack (.rotationDegrees Vector3f/ZP (float 225)))
    (.translate posestack (float -0.4) (float 0) (float 0))
    (.scale posestack (float 1.2) (float 1.2) (float 1.2))
    (.renderStatic ^ItemRenderer (.state this) (thrownpitchfork/getitem ^ThrownPitchfork thrown)
                   ItemTransforms$TransformType/GROUND i OverlayTexture/NO_OVERLAY posestack source (.getId thrown))
    (.popPose posestack)
    (.prender this thrown f g posestack source i)))

(defn -getTextureLocation [this thrown] TextureAtlas/LOCATION_BLOCKS)