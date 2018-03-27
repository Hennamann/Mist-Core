package com.henrikstabell.mistcore.handler;

import com.henrikstabell.mistcore.api.IBiomeMist;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.client.event.EntityViewRenderEvent.FogColors;
import net.minecraftforge.client.event.EntityViewRenderEvent.RenderFogEvent;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

public class MistHandler {
    private static double MistX;
    private static double MistZ;
    private static boolean MistInit;
    private static float MistFarPlaneDistance;

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onGetMistColour(FogColors event) {
        if (event.getEntity() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.getEntity();
            World world = player.world;
            int x = MathHelper.floor(player.posX);
            int y = MathHelper.floor(player.posY);
            int z = MathHelper.floor(player.posZ);
            IBlockState blockStateAtEyes = ActiveRenderInfo.getBlockStateAtEntityViewpoint(world, event.getEntity(), (float) event.getRenderPartialTicks());
            if (blockStateAtEyes.getMaterial() == Material.LAVA) {
                return;
            }

            Vec3d mixedColor;
            if (blockStateAtEyes.getMaterial() == Material.WATER) {
                mixedColor = getMistBlendColorWater(world, player, x, y, z, event.getRenderPartialTicks());
            } else {
                mixedColor = getMistBlendColour(world, player, x, y, z, event.getRed(), event.getGreen(), event.getBlue(), event.getRenderPartialTicks());
            }
            event.setRed((float) mixedColor.x);
            event.setGreen((float) mixedColor.y);
            event.setBlue((float) mixedColor.z);
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onRenderMist(RenderFogEvent event) {
        Entity entity = event.getEntity();
        World world = entity.world;
        int playerX = MathHelper.floor(entity.posX);
        int playerY = MathHelper.floor(entity.posY);
        int playerZ = MathHelper.floor(entity.posZ);
        if ((double) playerX == MistX && (double) playerZ == MistZ && MistInit) {
            renderMist(event.getFogMode(), MistFarPlaneDistance, 0.75F);
        } else {
            MistInit = true;
            byte distance = 20;
            float fpDistanceBiomeMist = 0.0F;
            float weightBiomeMist = 0.0F;

            float farPlaneDistance;
            float farPlaneDistanceScaleBiome;
            for (int weightMixed = -distance; weightMixed <= distance; ++weightMixed) {
                for (int weightDefault = -distance; weightDefault <= distance; ++weightDefault) {
                    Biome fpDistanceBiomeMistAvg = world.getBiomeForCoordsBody(new BlockPos(playerX + weightMixed, playerZ + weightDefault, playerY + weightDefault));
                    if (fpDistanceBiomeMistAvg instanceof IBiomeMist) {
                        farPlaneDistance = ((IBiomeMist) fpDistanceBiomeMistAvg).getMistDensity(playerX + weightMixed, playerY, playerZ + weightDefault);
                        farPlaneDistanceScaleBiome = 1.0F;
                        double farPlaneDistanceScale;
                        if (weightMixed == -distance) {
                            farPlaneDistanceScale = 1.0D - (entity.posX - (double) playerX);
                            farPlaneDistance = (float) ((double) farPlaneDistance * farPlaneDistanceScale);
                            farPlaneDistanceScaleBiome = (float) ((double) farPlaneDistanceScaleBiome * farPlaneDistanceScale);
                        } else if (weightMixed == distance) {
                            farPlaneDistanceScale = entity.posX - (double) playerX;
                            farPlaneDistance = (float) ((double) farPlaneDistance * farPlaneDistanceScale);
                            farPlaneDistanceScaleBiome = (float) ((double) farPlaneDistanceScaleBiome * farPlaneDistanceScale);
                        }

                        if (weightDefault == -distance) {
                            farPlaneDistanceScale = 1.0D - (entity.posZ - (double) playerZ);
                            farPlaneDistance = (float) ((double) farPlaneDistance * farPlaneDistanceScale);
                            farPlaneDistanceScaleBiome = (float) ((double) farPlaneDistanceScaleBiome * farPlaneDistanceScale);
                        } else if (weightDefault == distance) {
                            farPlaneDistanceScale = entity.posZ - (double) playerZ;
                            farPlaneDistance = (float) ((double) farPlaneDistance * farPlaneDistanceScale);
                            farPlaneDistanceScaleBiome = (float) ((double) farPlaneDistanceScaleBiome * farPlaneDistanceScale);
                        }

                        fpDistanceBiomeMist += farPlaneDistance;
                        weightBiomeMist += farPlaneDistanceScaleBiome;
                    }
                }
            }

            float var17 = (float) (distance * 2 * distance * 2);
            float var18 = var17 - weightBiomeMist;
            float var19 = weightBiomeMist == 0.0F ? 0.0F : fpDistanceBiomeMist / weightBiomeMist;
            farPlaneDistance = (fpDistanceBiomeMist * 240.0F + event.getFarPlaneDistance() * var18) / var17;
            farPlaneDistanceScaleBiome = 0.1F * (1.0F - var19) + 0.75F * var19;
            float var20 = (farPlaneDistanceScaleBiome * weightBiomeMist + 0.75F * var18) / var17;
            MistX = entity.posX;
            MistZ = entity.posZ;
            MistFarPlaneDistance = Math.min(farPlaneDistance, event.getFarPlaneDistance());
            renderMist(event.getFogMode(), MistFarPlaneDistance, var20);
        }
    }

    private static void renderMist(int mistMode, float farPlaneDistance, float farPlaneDistanceScale) {
        if (mistMode < 0) {
            GL11.glFogf(GL11.GL_FOG_START, 0.0F);
            GL11.glFogf(GL11.GL_FOG_END, farPlaneDistance);
        } else {
            GL11.glFogf(GL11.GL_FOG_START, farPlaneDistance * farPlaneDistanceScale);
            GL11.glFogf(GL11.GL_FOG_END, farPlaneDistance);
        }
    }

    private static Vec3d postProcessColor(World world, EntityLivingBase player, double r, double g, double b, double renderPartialTicks) {
        double darkScale = (player.lastTickPosY + (player.posY - player.lastTickPosY) * renderPartialTicks) * world.provider.getVoidFogYFactor();
        if (player.isPotionActive(MobEffects.BLINDNESS)) {
            int duration = player.getActivePotionEffect(MobEffects.BLINDNESS).getDuration();
            darkScale *= (duration < 20) ? (1 - duration / 20f) : 0;
        }

        if (darkScale < 1) {
            darkScale = (darkScale < 0) ? 0 : darkScale * darkScale;
            r *= darkScale;
            g *= darkScale;
            b *= darkScale;
        }

        if (player.isPotionActive(MobEffects.NIGHT_VISION)) {
            int duration = player.getActivePotionEffect(MobEffects.NIGHT_VISION).getDuration();
            float brightness = (duration > 200) ? 1 : 0.7f + MathHelper.sin((float) ((duration - renderPartialTicks) * Math.PI * 0.2f)) * 0.3f;

            double scale = 1 / r;
            scale = Math.min(scale, 1 / g);
            scale = Math.min(scale, 1 / b);

            r = r * (1 - brightness) + r * scale * brightness;
            g = g * (1 - brightness) + g * scale * brightness;
            b = b * (1 - brightness) + b * scale * brightness;
        }


        if (Minecraft.getMinecraft().gameSettings.anaglyph) {
            double aR = (r * 30 + g * 59 + b * 11) / 100;
            double aG = (r * 30 + g * 70) / 100;
            double aB = (r * 30 + b * 70) / 100;

            r = aR;
            g = aG;
            b = aB;
        }

        return new Vec3d(r, g, b);
    }

    private static Vec3d getMistBlendColorWater(World world, EntityLivingBase playerEntity, int playerX, int playerY, int playerZ, double renderPartialTicks) {
        byte distance = 2;
        float rBiomeMist = 0.0F;
        float gBiomeMist = 0.0F;
        float bBiomeMist = 0.0F;

        float bMixed;
        for (int weight = -distance; weight <= distance; ++weight) {
            for (int respirationLevel = -distance; respirationLevel <= distance; ++respirationLevel) {
                Biome rMixed = world.getBiomeForCoordsBody(new BlockPos(playerX + weight, playerY + weight, playerZ + respirationLevel));
                int gMixed = rMixed.getWaterColorMultiplier();
                bMixed = (float) ((gMixed & 16711680) >> 16);
                float gPart = (float) ((gMixed & '\uff00') >> 8);
                float bPart = (float) (gMixed & 255);
                double zDiff;
                if (weight == -distance) {
                    zDiff = 1.0D - (playerEntity.posX - (double) playerX);
                    bMixed = (float) ((double) bMixed * zDiff);
                    gPart = (float) ((double) gPart * zDiff);
                    bPart = (float) ((double) bPart * zDiff);
                } else if (weight == distance) {
                    zDiff = playerEntity.posX - (double) playerX;
                    bMixed = (float) ((double) bMixed * zDiff);
                    gPart = (float) ((double) gPart * zDiff);
                    bPart = (float) ((double) bPart * zDiff);
                }

                if (respirationLevel == -distance) {
                    zDiff = 1.0D - (playerEntity.posZ - (double) playerZ);
                    bMixed = (float) ((double) bMixed * zDiff);
                    gPart = (float) ((double) gPart * zDiff);
                    bPart = (float) ((double) bPart * zDiff);
                } else if (respirationLevel == distance) {
                    zDiff = playerEntity.posZ - (double) playerZ;
                    bMixed = (float) ((double) bMixed * zDiff);
                    gPart = (float) ((double) gPart * zDiff);
                    bPart = (float) ((double) bPart * zDiff);
                }

                rBiomeMist += bMixed;
                gBiomeMist += gPart;
                bBiomeMist += bPart;
            }
        }

        rBiomeMist /= 255.0F;
        gBiomeMist /= 255.0F;
        bBiomeMist /= 255.0F;
        float var20 = (float) (distance * 2 * distance * 2);
        float var21 = (float) EnchantmentHelper.getRespirationModifier(playerEntity) * 0.2F;
        float var22 = (rBiomeMist * 0.02F + var21) / var20;
        float var23 = (gBiomeMist * 0.02F + var21) / var20;
        bMixed = (bBiomeMist * 0.2F + var21) / var20;
        return postProcessColor(world, playerEntity, var22, var23, bMixed, renderPartialTicks);
    }

    private static Vec3d getMistBlendColour(World world, EntityLivingBase playerEntity, int playerX, int playerY, int playerZ, float defR, float defG, float defB, double renderPartialTicks) {
        GameSettings settings = Minecraft.getMinecraft().gameSettings;
        int[] ranges = ForgeModContainer.blendRanges;
        int distance = 0;
        if (settings.fancyGraphics && settings.renderDistanceChunks >= 0 && settings.renderDistanceChunks < ranges.length) {
            distance = ranges[settings.renderDistanceChunks];
        }

        float rBiomeMist = 0.0F;
        float gBiomeMist = 0.0F;
        float bBiomeMist = 0.0F;
        float weightBiomeMist = 0.0F;

        float rainStrength;
        float thunderStrength;
        float weightMixed;
        for (int celestialAngle = -distance; celestialAngle <= distance; ++celestialAngle) {
            for (int baseScale = -distance; baseScale <= distance; ++baseScale) {
                Biome rScale = world.getBiomeForCoordsBody(new BlockPos(playerX + celestialAngle, playerY + celestialAngle, playerZ + baseScale));
                if (rScale instanceof IBiomeMist) {
                    IBiomeMist gScale = (IBiomeMist) rScale;
                    int bScale = gScale.getMistColour(playerX + celestialAngle, playerY, playerZ + baseScale);
                    rainStrength = (float) ((bScale & 16711680) >> 16);
                    thunderStrength = (float) ((bScale & '\uff00') >> 8);
                    float processedColor = (float) (bScale & 255);
                    weightMixed = 1.0F;
                    double weightDefault;
                    if (celestialAngle == -distance) {
                        weightDefault = 1.0D - (playerEntity.posX - (double) playerX);
                        rainStrength = (float) ((double) rainStrength * weightDefault);
                        thunderStrength = (float) ((double) thunderStrength * weightDefault);
                        processedColor = (float) ((double) processedColor * weightDefault);
                        weightMixed = (float) ((double) weightMixed * weightDefault);
                    } else if (celestialAngle == distance) {
                        weightDefault = playerEntity.posX - (double) playerX;
                        rainStrength = (float) ((double) rainStrength * weightDefault);
                        thunderStrength = (float) ((double) thunderStrength * weightDefault);
                        processedColor = (float) ((double) processedColor * weightDefault);
                        weightMixed = (float) ((double) weightMixed * weightDefault);
                    }

                    if (baseScale == -distance) {
                        weightDefault = 1.0D - (playerEntity.posZ - (double) playerZ);
                        rainStrength = (float) ((double) rainStrength * weightDefault);
                        thunderStrength = (float) ((double) thunderStrength * weightDefault);
                        processedColor = (float) ((double) processedColor * weightDefault);
                        weightMixed = (float) ((double) weightMixed * weightDefault);
                    } else if (baseScale == distance) {
                        weightDefault = playerEntity.posZ - (double) playerZ;
                        rainStrength = (float) ((double) rainStrength * weightDefault);
                        thunderStrength = (float) ((double) thunderStrength * weightDefault);
                        processedColor = (float) ((double) processedColor * weightDefault);
                        weightMixed = (float) ((double) weightMixed * weightDefault);
                    }

                    rBiomeMist += rainStrength;
                    gBiomeMist += thunderStrength;
                    bBiomeMist += processedColor;
                    weightBiomeMist += weightMixed;
                }
            }
        }

        if (weightBiomeMist == 0.0F) {
            return new Vec3d((double) defR, (double) defG, (double) defB);
        } else {
            rBiomeMist /= 255.0F;
            gBiomeMist /= 255.0F;
            bBiomeMist /= 255.0F;
            float var28 = world.getCelestialAngle((float) renderPartialTicks);
            float var29 = MathHelper.clamp(MathHelper.cos(var28 * 3.1415927F * 2.0F) * 2.0F + 0.5F, 0.0F, 1.0F);
            float var30 = var29 * 0.94F + 0.06F;
            float var31 = var29 * 0.94F + 0.06F;
            float var32 = var29 * 0.91F + 0.09F;
            rainStrength = world.getRainStrength((float) renderPartialTicks);
            if (rainStrength > 0.0F) {
                var30 *= 1.0F - rainStrength * 0.5F;
                var31 *= 1.0F - rainStrength * 0.5F;
                var32 *= 1.0F - rainStrength * 0.4F;
            }

            thunderStrength = world.getThunderStrength((float) renderPartialTicks);
            if (thunderStrength > 0.0F) {
                var30 *= 1.0F - thunderStrength * 0.5F;
                var31 *= 1.0F - thunderStrength * 0.5F;
                var32 *= 1.0F - thunderStrength * 0.5F;
            }

            rBiomeMist *= var30 / weightBiomeMist;
            gBiomeMist *= var31 / weightBiomeMist;
            bBiomeMist *= var32 / weightBiomeMist;
            Vec3d var33 = postProcessColor(world, playerEntity, rBiomeMist, gBiomeMist, bBiomeMist, renderPartialTicks);
            rBiomeMist = (float) var33.x;
            gBiomeMist = (float) var33.y;
            bBiomeMist = (float) var33.z;
            weightMixed = (float) (distance * 2 * distance * 2);

            float var34 = weightMixed - weightBiomeMist;
            double rFinal = (double) ((rBiomeMist * weightBiomeMist + defR * var34) / weightMixed);
            double gFinal = (double) ((gBiomeMist * weightBiomeMist + defG * var34) / weightMixed);
            double bFinal = (double) ((bBiomeMist * weightBiomeMist + defB * var34) / weightMixed);

            return new Vec3d(rFinal, gFinal, bFinal);
        }
    }
}