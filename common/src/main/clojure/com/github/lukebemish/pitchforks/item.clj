(ns com.github.lukebemish.pitchforks.item
  (:require [com.github.lukebemish.clojurewrapper.api.item :as item]
            [com.github.lukebemish.clojurewrapper.api.util.functional :as functional])
  (:import (net.minecraft.world.item CreativeModeTab Item Vanishable ItemStack UseAnim)
           (com.google.common.collect ImmutableMultimap ImmutableMultimap$Builder)
           (net.minecraft.world.entity.ai.attributes Attributes AttributeModifier AttributeModifier$Operation)
           (net.minecraft.world.entity EquipmentSlot LivingEntity)
           (net.minecraft.world.level.block.state BlockState)
           (net.minecraft.core BlockPos)
           (net.minecraft.world.level Level)
           (net.minecraft.world InteractionHand InteractionResultHolder)
           (net.minecraft.world.entity.player Player)))

(def pitchfork-atts (memoize (fn [] (let [^ImmutableMultimap$Builder att-builder (ImmutableMultimap/builder)]
                                      (do
                                        (.put att-builder Attributes/ATTACK_DAMAGE (AttributeModifier. Item/BASE_ATTACK_DAMAGE_UUID "Tool modifier" 4.0 AttributeModifier$Operation/ADDITION))
                                        (.put att-builder Attributes/ATTACK_SPEED (AttributeModifier. Item/BASE_ATTACK_SPEED_UUID "Tool modifier" -2.9 AttributeModifier$Operation/ADDITION))
                                        (.build att-builder))))))

(def pitchfork-item (memoize (fn []
                               (proxy [Item Vanishable] [(item/item-properties {:tab        CreativeModeTab/TAB_COMBAT
                                                                                ;:max-damage (int 150)
                                                                                :stack-size (int 1)})]
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
                                          (InteractionResultHolder/consume itemStack))))
                                 (getUseDuration [itemStack] 96000)
                                 (getUseAnimation [itemStack] UseAnim/SPEAR)
                                 (canAttackBlock [blockState level blockPos player] (.isCreative ^Player player))
                                 ))))