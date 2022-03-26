package com.github.lukebemish.pitchforks.mixin;

import clojure.lang.RT;
import clojure.lang.Var;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {
    private static final Var pitchforks$get_item = (Var) RT.var("com.github.lukebemish.pitchforks.item", "pitchfork-item");
    @Shadow
    @Final
    private ItemModelShaper itemModelShaper;

    @ModifyVariable(method="render",at=@At("HEAD"),ordinal = 0,argsOnly = true)
    private BakedModel pitchforks$render(BakedModel model, ItemStack is, ItemTransforms.TransformType type, boolean bl, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j, BakedModel bakedModel) {
        if (type == ItemTransforms.TransformType.GUI || type == ItemTransforms.TransformType.GROUND || type == ItemTransforms.TransformType.FIXED) {
            if (!is.isEmpty() && is.is((Item)pitchforks$get_item.invoke())) {
                return this.itemModelShaper.getModelManager().getModel(new ModelResourceLocation("pitchforks:pitchfork#inventory"));
            }
        }
        return model;
    }

    @ModifyVariable(method="getModel",at=@At(value="INVOKE_ASSIGN",
        target="Lnet/minecraft/client/renderer/ItemModelShaper;getItemModel(Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/client/resources/model/BakedModel;",
        shift=At.Shift.AFTER), ordinal = 0)
    private BakedModel pitchforks$getModel(BakedModel model, ItemStack itemStack, @Nullable Level level, @Nullable LivingEntity livingEntity, int i) {
        if (itemStack.is((Item)pitchforks$get_item.invoke())) {
            return this.itemModelShaper.getModelManager().getModel(new ModelResourceLocation("pitchforks:pitchfork_in_hand#inventory"));
        }
        return model;
    }
}
