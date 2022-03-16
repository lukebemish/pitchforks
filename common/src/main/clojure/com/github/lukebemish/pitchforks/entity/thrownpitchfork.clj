(ns com.github.lukebemish.pitchforks.entity.thrownpitchfork
  (:import (net.minecraft.world.entity LivingEntity Entity EntityType)
           (net.minecraft.world.level Level ItemLike)
           (net.minecraft.world.item ItemStack)
           (net.minecraft.world.item.enchantment EnchantmentHelper)
           (net.minecraft.server.level ServerPlayer)
           (net.minecraft.world.entity.player Player)
           (net.minecraft.sounds SoundEvents SoundEvent)
           (net.minecraft.nbt CompoundTag)
           (net.minecraft.world.entity.projectile AbstractArrow$Pickup)
           (net.minecraft.world.phys EntityHitResult)
           (net.minecraft.world.damagesource DamageSource IndirectEntityDamageSource))
  (:require [com.github.lukebemish.pitchforks.item :as item]
            [com.github.lukebemish.pitchforks.entity.shared :as shared])
  (:gen-class
    :extends net.minecraft.world.entity.projectile.AbstractArrow
    :state state
    :init init
    :post-init post-init
    :exposes-methods {defineSynchedData p-defineSynchedData
                      findHitEntity p-findHitEntity
                      tryPickup p-tryPickup
                      playerTouch p-playerTouch
                      addAdditionalSaveData p-addAdditionalSaveData
                      tickDespawn p-tickDespawn}
    :prefix "-"
    :main false))

(defn setfield
  [this key value]
  (swap! (.state this) into {key value}))

(defn getfield
  [this key]
  (@(.state this) key))

(defn default-state [] {:item-stack nil :dealt-damage false})

(defn -init
  ([type ^Level level]
   [[type level]
    (atom (default-state))])
  ([type ^LivingEntity entity ^Level level ^ItemStack item-stack]
   [[type entity level]
    (atom (default-state))]))

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

(defn -isAcceptibleReturnOwner [this]
  (let [entity (.getOwner this)]
    (and (not (nil? entity)) (.isAlive entity)
         (or (not (.isSpectator entity)) (instance? ServerPlayer entity)))))

(defn -isFoil [this]
  (boolean (.get (.getEntityData this) shared/id-foil)))

(defn -findHitEntity [this vec31 vec32]
  (if (getfield this :dealt-damage) nil (.p-findHitEntity this vec31 vec32)))

(defn -isChanneling [this]
  (EnchantmentHelper/hasChanneling (getfield this :item-stack)))

(defn -tryPickup [this ^Player player]
  (or (.p-tryPickup this player)
      (and (.isNoPhysics this)
           (.ownedBy this player)
           (.add (.getInventory player) (.getPickupItem this)))))

(defn -getDefaultHitGroundSoundEvent [this]
  SoundEvents/TRIDENT_HIT_GROUND)

(defn -playerTouch [this player]
  (if (or (.ownedBy player) (nil? (.getOwner this)))
    (.p-playerTouch this player)))

(defn -readAdditionalSaveData [this ^CompoundTag tag]
  (do
    (if (.contains tag "Pitchfork" 10)
      (setfield this :item-stack (ItemStack/of (.getCompound tag "Pitchfork"))))
    (setfield this :dealt-damage (.getBoolean tag "DealtDamage"))
    (.set (.getEntityData this) (shared/id-loyalty) (EnchantmentHelper/getLoyalty (getfield this :item-stack)))))

(defn -addAdditionalSaveData [this ^CompoundTag tag]
  (do
    (.p-addAdditionalSaveData this tag)
    (.put tag "Pitchfork" (.save ^ItemStack (getfield this :item-stack) (CompoundTag.)))
    (.putBoolean tag "DealtDamage" (getfield this :dealt-damage))))

(defn -tickDespawn [this]
  (let [loyal (int ^byte (.get (.getEntityData this) (shared/id-loyalty)))]
    (if (or (not (= (.pickup this) AbstractArrow$Pickup/ALLOWED))
            (<= loyal 0))
      (.p-tickDespawn this))))

(defn -shouldRender [this] true)

(defn -onHitEntity [this ^EntityHitResult entity-hit]
  (let [^Entity entity (.getEntity entity-hit)
        ^Float damage (+ 8.0 (if (instance? LivingEntity entity)
                               (EnchantmentHelper/getDamageBonus (getfield this :item-stack)
                                                                 (.getMobType ^LivingEntity entity)) 0))
        ^Entity owner (.getOwner this)
        ^DamageSource source (.setProjectile (IndirectEntityDamageSource. "pitchfork" entity
                                                                          (if (nil? owner) this owner)))
        ^SoundEvent sound-event SoundEvents/TRIDENT_HIT
        after (fn [] ())]
    (if (.hurt entity source damage)
      (if (= (.getType entity) EntityType/ENDERMAN)
        () (do

             (after))))))