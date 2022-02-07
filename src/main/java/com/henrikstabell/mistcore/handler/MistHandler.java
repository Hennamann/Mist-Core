package com.henrikstabell.mistcore.handler;

import com.henrikstabell.mistcore.MistCore;
import com.henrikstabell.mistcore.api.MistCoreApi;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FogType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.awt.*;
import java.util.Objects;

@Mod.EventBusSubscriber(modid = MistCore.MODID, value = Dist.CLIENT)
public class MistHandler {

    @SubscribeEvent
    public static void onRenderMistColors(EntityViewRenderEvent.FogColors event) {
        Color fogColor = Color.decode("#FFFFFF");

        float red = fogColor.getRed();
        float green = fogColor.getGreen();
        float blue = fogColor.getBlue();

        final float[] fogColors = {red / 255F, green / 255F, blue / 255F};

        event.setRed(fogColors[0]);
        event.setGreen(fogColors[1]);
        event.setBlue(fogColors[2]);
    }

    @SubscribeEvent
    public static void onRenderMist(EntityViewRenderEvent.RenderFogEvent event) {
        FogType fogtype = event.getCamera().getFluidInCamera();
        Entity entity = event.getCamera().getEntity();
        Level world = event.getCamera().getEntity().level;
        BlockPos pos = event.getCamera().getBlockPosition();
        ResourceLocation biome = world.getBiome(pos).getRegistryName();

        if (MistCoreApi.MIST_BIOMES.containsKey(biome)) {
            float mistDensity = Objects.requireNonNull(MistCoreApi.MIST_BIOMES.getValue(biome)).getMistDensity();

            float f2;
            float f3;
            if (fogtype == FogType.LAVA) {
                if (entity.isSpectator()) {
                    f2 = -8.0F;
                    f3 = mistDensity * 0.5F;
                } else if (entity instanceof LivingEntity && ((LivingEntity) entity).hasEffect(MobEffects.FIRE_RESISTANCE)) {
                    f2 = 0.0F;
                    f3 = 3.0F;
                } else {
                    f2 = 0.25F;
                    f3 = 1.0F;
                }
            } else if (entity instanceof LivingEntity && ((LivingEntity) entity).hasEffect(MobEffects.BLINDNESS)) {
                int i = Objects.requireNonNull(((LivingEntity) entity).getEffect(MobEffects.BLINDNESS)).getDuration();
                float f1 = Mth.lerp(Math.min(1.0F, (float) i / 20.0F), mistDensity, 5.0F);
                if (event.getMode() == FogRenderer.FogMode.FOG_SKY) {
                    f2 = 0.0F;
                    f3 = f1 * 0.8F;
                } else {
                    f2 = f1 * 0.25F;
                    f3 = f1;
                }
            } else if (fogtype == FogType.POWDER_SNOW) {
                if (entity.isSpectator()) {
                    f2 = -8.0F;
                    f3 = mistDensity * 0.5F;
                } else {
                    f2 = 0.0F;
                    f3 = 2.0F;
                }
            } else if (event.getMode() == FogRenderer.FogMode.FOG_SKY) {
                f2 = 0.0F;
                f3 = mistDensity;
            } else {
                float f4 = Mth.clamp(mistDensity / 10.0F, 4.0F, 64.0F);
                f2 = mistDensity - f4;
                f3 = mistDensity;
            }
            RenderSystem.setShaderTexture(0,0);
            RenderSystem.setShaderFogStart(f2);
            RenderSystem.setShaderFogEnd(f3);
        }
    }
}