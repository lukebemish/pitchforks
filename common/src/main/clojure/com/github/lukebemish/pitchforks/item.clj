(ns com.github.lukebemish.pitchforks.item
  (:require [com.github.lukebemish.clojurewrapper.api.util.functional :as functional]
            [com.github.lukebemish.pitchforks.entity.setup]
            [com.github.lukebemish.pitchforks.entity :as entity]
            [com.github.lukebemish.pitchforks.shared :as shared])
  (:import (net.minecraft.world.item CreativeModeTab Item Vanishable ItemStack UseAnim Item$Properties)
           (com.google.common.collect ImmutableMultimap ImmutableMultimap$Builder)
           (net.minecraft.world.entity.ai.attributes Attributes AttributeModifier AttributeModifier$Operation)
           (net.minecraft.world.entity EquipmentSlot LivingEntity)
           (net.minecraft.world.level.block.state BlockState)
           (net.minecraft.core BlockPos)
           (net.minecraft.world.level Level)
           (net.minecraft.world InteractionHand InteractionResultHolder)
           (net.minecraft.world.entity.player Player Inventory)
           (net.minecraft.world.item.enchantment EnchantmentHelper)
           (com.github.lukebemish.pitchforks.entity ThrownPitchfork)
           (net.minecraft.world.entity.projectile AbstractArrow$Pickup)
           (net.minecraft.sounds SoundEvents SoundSource)
           (net.minecraft.stats Stats)
           (org.apache.logging.log4j LogManager Logger)))

(def pitchfork-atts (memoize (fn [] (let [^ImmutableMultimap$Builder att-builder (ImmutableMultimap/builder)]
                                      (do
                                        (.put att-builder Attributes/ATTACK_DAMAGE (AttributeModifier. Item/BASE_ATTACK_DAMAGE_UUID "Tool modifier" 4.0 AttributeModifier$Operation/ADDITION))
                                        (.put att-builder Attributes/ATTACK_SPEED (AttributeModifier. Item/BASE_ATTACK_SPEED_UUID "Tool modifier" -2.9 AttributeModifier$Operation/ADDITION))
                                        (.build att-builder))))))

(def pitchfork-item (memoize (fn []
                               (proxy [Item Vanishable] [(doto (Item$Properties.)
                                                           (.tab CreativeModeTab/TAB_COMBAT)
                                                           (.durability (int 150)))]
                                 (getDefaultAttributeModifiers [slot] (if (= slot EquipmentSlot/MAINHAND)
                                                                        (pitchfork-atts)
                                                                        (proxy-super getDefaultAttributeModifiers slot)))
                                 (getEnchantmentValue [] 1)
                                 (mineBlock [itemStack ^Level level ^BlockState blockState ^BlockPos blockPos livingEntity]
                                   (if (not (= 0 (.getDestroySpeed blockState level blockPos)))
                                     (.hurtAndBreak ^ItemStack itemStack 2 livingEntity (functional/consumer (fn [^LivingEntity le] (.broadcastBreakEvent le EquipmentSlot/MAINHAND)))))
                                   true)
                                 (hurtEnemy [itemStack livingEntity livingEntity2]
                                   (.hurtAndBreak ^ItemStack itemStack 1 livingEntity2 (functional/consumer (fn [^LivingEntity le] (.broadcastBreakEvent le EquipmentSlot/MAINHAND))))
                                   true)
                                 (use [level player interactionHand]
                                   (let [^ItemStack itemStack (.getItemInHand ^Player player ^InteractionHand interactionHand)]
                                        (if (>= (.getDamageValue itemStack) (.getMaxDamage itemStack))
                                          (InteractionResultHolder/fail itemStack)
                                          (do
                                            (.startUsingItem player interactionHand)
                                            (InteractionResultHolder/consume itemStack)))))
                                 (getUseDuration [itemStack] 72000)
                                 (getUseAnimation [itemStack] UseAnim/SPEAR)
                                 (canAttackBlock [blockState level blockPos player] (.isCreative ^Player player))
                                 (releaseUsing [^ItemStack item-stack ^Level level ^LivingEntity entity i]
                                   (if (instance? Player entity)
                                     (let [held (- (.getUseDuration this item-stack) i)]
                                       (if (>= held 15)
                                         (let [riptide (EnchantmentHelper/getRiptide item-stack)]
                                           (if (not (.isClientSide level))
                                             (do
                                               (.debug shared/logger "Running on-release at time {} with riptide {}" held riptide)
                                               (.awardStat ^Player entity (.get Stats/ITEM_USED this))
                                               (.hurtAndBreak item-stack 1 entity (functional/consumer #(.broadcastBreakEvent ^Player % (.getUsedItemHand entity))))
                                               (if (<= riptide 0)
                                                 (let [thrown (.setup (new ThrownPitchfork (entity/thrown-pitchfork) ^LivingEntity entity ^Level level) ^ItemStack item-stack)]
                                                   (do
                                                     (.shootFromRotation thrown entity (.getXRot entity) (.getYRot entity) (float 0) (float 1) (float 1))
                                                     (if (. (.getAbilities ^Player entity) -instabuild)
                                                       (set! (. thrown -pickup) AbstractArrow$Pickup/CREATIVE_ONLY)
                                                       (.removeItem ^Inventory (.getInventory ^Player entity) item-stack))
                                                     (.addFreshEntity level thrown)
                                                     (.playSound level nil thrown SoundEvents/TRIDENT_THROW SoundSource/PLAYERS (float 1) (float 1))))
                                                 (
                                                   ;do riptide stuff here
                                                   (.debug shared/logger "Riptide would be called here")
                                                   )))))))))
                                 ))))