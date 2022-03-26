(ns com.github.lukebemish.pitchforks.entity.thrownpitchfork
  (:import (net.minecraft.world.entity LivingEntity Entity EntityType LightningBolt)
           (net.minecraft.world.level Level ItemLike)
           (net.minecraft.world.item ItemStack)
           (net.minecraft.world.item.enchantment EnchantmentHelper)
           (net.minecraft.server.level ServerPlayer ServerLevel)
           (net.minecraft.world.entity.player Player)
           (net.minecraft.sounds SoundEvents)
           (net.minecraft.nbt CompoundTag)
           (net.minecraft.world.entity.projectile AbstractArrow$Pickup AbstractArrow)
           (net.minecraft.world.phys EntityHitResult Vec3)
           (net.minecraft.world.damagesource DamageSource IndirectEntityDamageSource)
           (com.github.lukebemish.pitchforks.entity ThrownPitchfork)
           (net.minecraft.network.syncher EntityDataSerializers SynchedEntityData))
  (:require [com.github.lukebemish.pitchforks.item :as item]
            [com.github.lukebemish.pitchforks.entity.setup :as setup])
  (:gen-class
    :name com.github.lukebemish.pitchforks.entity.ThrownPitchfork
    :extends net.minecraft.world.entity.projectile.AbstractArrow
    :state state
    :init init
    :post-init post-init
    :exposes-methods {defineSynchedData pdefineSynchedData
                      findHitEntity pfindHitEntity
                      tryPickup ptryPickup
                      playerTouch pplayerTouch
                      addAdditionalSaveData paddAdditionalSaveData
                      tickDespawn ptickDespawn
                      tick ptick}
    :exposes {inGroundTime {:get getInGroundTime :set setInGroundTime}}
    :methods [[setup [Object] com.github.lukebemish.pitchforks.entity.ThrownPitchfork]]
    :prefix "-"
    :main false))

(def id-loyalty (memoize (fn [] (SynchedEntityData/defineId ThrownPitchfork EntityDataSerializers/BYTE))))
(def id-foil (memoize (fn [] (SynchedEntityData/defineId ThrownPitchfork EntityDataSerializers/BOOLEAN))))

(defn setfield
  [this key value]
  (swap! (.state this) into {key value}))

(defn getfield
  [this key]
  (@(.state this) key))

(defn default-state [] {:item-stack nil :dealt-damage false :client-return-ticks 0})

(defn -init [& args] [args (atom (default-state))])

(defn -setup [this item-stack]
  (if (instance? ItemStack item-stack)
    (do
      (.set (.getEntityData this) (id-loyalty) (byte (EnchantmentHelper/getLoyalty item-stack)))
      (.set (.getEntityData this) (id-foil) (.hasFoil item-stack))
      (setfield this :item-stack (.copy item-stack))
      this)))

(defn -post-init [this & args]
  (setfield this :item-stack (new ItemStack ^ItemLike (item/pitchfork-item))))

(defn -defineSynchedData [this]
  (do
    (.pdefineSynchedData this)
    (.define (.getEntityData this) (id-loyalty) (byte 0))
    (.define (.getEntityData this) (id-foil) false)))

(defn -getPickupItem [this]
  (.copy (getfield this :item-stack)))

(defn isAcceptibleReturnOwner [this]
  (let [entity (.getOwner this)]
    (and (not (nil? entity)) (.isAlive entity)
         (or (not (.isSpectator entity)) (instance? ServerPlayer entity)))))

(defn isFoil [this]
  (boolean (.get (.getEntityData this) (id-foil))))

(defn -findHitEntity [this vec31 vec32]
  (if (getfield this :dealt-damage) nil (.pfindHitEntity this vec31 vec32)))

(defn isChanneling [this]
  (EnchantmentHelper/hasChanneling (getfield this :item-stack)))

(defn -tryPickup [this ^Player player]
  (or (.ptryPickup this player)
      (and (.isNoPhysics this)
           (.ownedBy this player)
           (.add (.getInventory player) (.getPickupItem this)))))

(defn -getDefaultHitGroundSoundEvent [this]
  SoundEvents/TRIDENT_HIT_GROUND)

(defn -playerTouch [this player]
  (if (or (.ownedBy this player) (nil? (.getOwner this)))
    (.pplayerTouch this player)))

(defn -readAdditionalSaveData [this ^CompoundTag tag]
  (do
    (if (.contains tag "Pitchfork" 10)
      (setfield this :item-stack (ItemStack/of (.getCompound tag "Pitchfork"))))
    (setfield this :dealt-damage (.getBoolean tag "DealtDamage"))
    (.set (.getEntityData this) (id-loyalty) (byte (EnchantmentHelper/getLoyalty (getfield this :item-stack))))))

(defn -addAdditionalSaveData [this ^CompoundTag tag]
  (do
    (.paddAdditionalSaveData this tag)
    (.put tag "Pitchfork" (.save ^ItemStack (getfield this :item-stack) (CompoundTag.)))
    (.putBoolean tag "DealtDamage" (getfield this :dealt-damage))))

(defn -tickDespawn [this]
  (let [loyal (int (byte (.get (.getEntityData this) (id-loyalty))))]
    (if (or (not (= (.pickup this) AbstractArrow$Pickup/ALLOWED))
            (<= loyal 0))
      (.ptickDespawn this))))

(defn -shouldRender [this d e f] true)

(defn -onHitEntity [this ^EntityHitResult entity-hit]
  (let [^Entity entity (.getEntity entity-hit)
        damage (float (+ 8.0 (if (instance? LivingEntity entity)
                               (EnchantmentHelper/getDamageBonus (getfield this :item-stack)
                                                                 (.getMobType ^LivingEntity entity)) 0)))
        ^Entity owner (.getOwner this)
        ^DamageSource source (.setProjectile (IndirectEntityDamageSource. "pitchfork" entity
                                                                          (if (nil? owner) this owner)))
        play-def (fn [] (.playSound this SoundEvents/TRIDENT_HIT (float 1.0) (float 1.0)))
        after (fn [] (do
                       (.setDeltaMovement this (.multiply (.getDeltaMovement this) -0.01 -0.1 -0.01))
                       (if (and (instance? ServerLevel (.getLevel this)) (.isThundering (.getLevel this)) (isChanneling this))
                         (let [block-pos (.blockPosition entity)]
                           (if (.canSeeSky (.getLevel this) block-pos)
                             (let [^LightningBolt bolt (.create EntityType/LIGHTNING_BOLT (.getLevel this))]
                               (do
                                 (.moveTo bolt (Vec3/atBottomCenterOf block-pos))
                                 (.setCause bolt (if (instance? ServerPlayer owner) ^ServerPlayer owner nil))
                                 (.addFreshEntity (.getLevel this) bolt)
                                 (.playSound this SoundEvents/TRIDENT_THUNDER (float 5.0) (float 1.0))))
                             (play-def)))
                         (play-def))))]
    (if (.hurt entity source damage)
      (if (= (.getType entity) EntityType/ENDERMAN)
        () (do
             (if (instance? LivingEntity entity)
               (do
                 (if (instance? LivingEntity owner)
                   (do
                     (EnchantmentHelper/doPostHurtEffects ^LivingEntity entity owner)
                     (EnchantmentHelper/doPostDamageEffects ^LivingEntity owner ^LivingEntity entity)))
                 (.doPostHurtEffects this ^LivingEntity entity)))
             (after)))
      (if (not (= (.getType entity) EntityType/ENDERMAN))
        (after)))))

(defn -tick [this]
  (do
    (if (> (.getInGroundTime this) 4)
      (setfield this :dealt-damage true))
    (let [owner (.getOwner this)
          loyalty (byte (.get (.getEntityData this) (id-loyalty)))]
      (if (and (> loyalty 0) (or (getfield this :dealt-damage) (.isNoPhysics this)) (not (nil? owner)))
        (if (isAcceptibleReturnOwner this)
          (let [vec3 (.subtract (.getEyePosition owner) (.position this))
                d (* 0.05 loyalty)]
            (do (.setNoPhysics this true)
                (.setPosRaw this (.getX this) (+ (.getY this) (* (.y vec3) 0.015 loyalty)) (.getZ this))
                (if (.isClientSide (.getLevel this)) (set! (. this -yOld) (.getY this)))
                (.setDeltaMovement this (.add (.scale (.getDeltaMovement this) 0.95) (.scale (.normalize vec3) d)))
                (if (= (getfield this :client-return-ticks) 0)
                  (.playSound this SoundEvents/TRIDENT_RETURN (float 10.0) (float 1.0)))
                (setfield this :client-return-ticks (+ 1 (getfield this :client-return-ticks)))))
          (do (if (and (not (.isClientSide (.getLevel this))) (= (.pickup this) (AbstractArrow$Pickup/ALLOWED)))
                (.spawnAtLocation this (.getPickupItem this) (float 0.1)))
              (.discard this)))))
    (.ptick this)))

(defn getitem [thrown]
  (getfield thrown :item-stack))