(ns com.github.lukebemish.pitchforks.entity.thrownpitchfork
  (:import (net.minecraft.world.entity EntityType LivingEntity)
           (net.minecraft.world.level Level ItemLike)
           (net.minecraft.world.item ItemStack)
           (net.minecraft.world.item.enchantment EnchantmentHelper))
  (:require [com.github.lukebemish.pitchforks.item :as item]
            [com.github.lukebemish.pitchforks.entity.shared :as shared])
  (:gen-class
    :extends net.minecraft.world.entity.projectile.AbstractArrow
    :state state
    :init init
    :post-init post-init
    :exposes-methods {defineSynchedData p-defineSynchedData}
    :prefix "-"
    :main false))

(defn setfield
  [this key value]
  (swap! (.state this) into {key value}))

(defn getfield
  [this key]
  (@(.state this) key))

(defn -init
  ([type ^Level level]
   [[type level]
    (atom {:item-stack nil})])
  ([type ^LivingEntity entity ^Level level ^ItemStack item-stack]
   [[type entity level]
    (atom {:item-stack nil})]))

(defn -post-init
  ([this type ^Level level]
   (setfield this :item-stack (ItemStack. ^ItemLike (item/pitchfork-item))))
  ([this type ^LivingEntity entity ^Level level ^ItemStack item-stack]
   (do
     (.set (.getEntityData this) (shared/id-loyalty) ^byte (EnchantmentHelper/getLoyalty item-stack))
     (.set (.getEntityData this) (shared/id-foil) (.hasFoil item-stack))
     (setfield this :item-stack (.copy item-stack)))))

(defn -defineSynchedData [this]
  (do
    (.p-defineSynchedData this)
    (.define (.getEntityData this) (shared/id-loyalty) (byte 0))
    (.define (.getEntityData this) (shared/id-foil) false)))

(defn -getPickupItem [this]
  (.copy (getfield this :item-stack)))