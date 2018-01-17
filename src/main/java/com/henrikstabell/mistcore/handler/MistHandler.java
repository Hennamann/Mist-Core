package com.henrikstabell.mistcore.handler;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import com.henrikstabell.mistcore.api.IBiomeMist;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.client.event.EntityViewRenderEvent.FogColors;
import net.minecraftforge.client.event.EntityViewRenderEvent.RenderFogEvent;
import net.minecraftforge.common.ForgeModContainer;
import org.lwjgl.opengl.GL11;

public class MistHandler {
   private static double MistX;
   private static double MistZ;
   private static boolean MistInit;
   private static float MistFarPlaneDistance;

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void onGetMistColour(FogColors event) {
      if(event.entity instanceof EntityPlayer) {
         EntityPlayer player = (EntityPlayer)event.entity;
         World world = player.worldObj;
         int x = MathHelper.floor_double(player.posX);
         int y = MathHelper.floor_double(player.posY);
         int z = MathHelper.floor_double(player.posZ);
         Block blockAtEyes = ActiveRenderInfo.getBlockAtEntityViewpoint(world, event.entity, (float)event.renderPartialTicks);
         if(blockAtEyes.getMaterial() == Material.lava) {
            return;
         }

         Vec3 mixedColor;
         if(blockAtEyes.getMaterial() == Material.water) {
            mixedColor = getMistBlendColorWater(world, player, x, y, z, event.renderPartialTicks);
         } else {
            mixedColor = getMistBlendColour(world, player, x, y, z, event.red, event.green, event.blue, event.renderPartialTicks);
         }

         event.red = (float)mixedColor.xCoord;
         event.green = (float)mixedColor.yCoord;
         event.blue = (float)mixedColor.zCoord;
      }

   }

   @SideOnly(Side.CLIENT)
   @SubscribeEvent
   public void onRenderMist(RenderFogEvent event) {
      EntityLivingBase entity = event.entity;
      World world = entity.worldObj;
      int playerX = MathHelper.floor_double(entity.posX);
      int playerY = MathHelper.floor_double(entity.posY);
      int playerZ = MathHelper.floor_double(entity.posZ);
      if((double)playerX == MistX && (double)playerZ == MistZ && MistInit) {
         renderMist(event.fogMode, MistFarPlaneDistance, 0.75F);
      } else {
         MistInit = true;
         byte distance = 20;
         float fpDistanceBiomeMist = 0.0F;
         float weightBiomeMist = 0.0F;

         float farPlaneDistance;
         float farPlaneDistanceScaleBiome;
         for(int weightMixed = -distance; weightMixed <= distance; ++weightMixed) {
            for(int weightDefault = -distance; weightDefault <= distance; ++weightDefault) {
               BiomeGenBase fpDistanceBiomeMistAvg = world.getBiomeGenForCoords(playerX + weightMixed, playerZ + weightDefault);
               if(fpDistanceBiomeMistAvg instanceof IBiomeMist) {
                  farPlaneDistance = ((IBiomeMist)fpDistanceBiomeMistAvg).getMistDensity(playerX + weightMixed, playerY, playerZ + weightDefault);
                  farPlaneDistanceScaleBiome = 1.0F;
                  double farPlaneDistanceScale;
                  if(weightMixed == -distance) {
                     farPlaneDistanceScale = 1.0D - (entity.posX - (double)playerX);
                     farPlaneDistance = (float)((double)farPlaneDistance * farPlaneDistanceScale);
                     farPlaneDistanceScaleBiome = (float)((double)farPlaneDistanceScaleBiome * farPlaneDistanceScale);
                  } else if(weightMixed == distance) {
                     farPlaneDistanceScale = entity.posX - (double)playerX;
                     farPlaneDistance = (float)((double)farPlaneDistance * farPlaneDistanceScale);
                     farPlaneDistanceScaleBiome = (float)((double)farPlaneDistanceScaleBiome * farPlaneDistanceScale);
                  }

                  if(weightDefault == -distance) {
                     farPlaneDistanceScale = 1.0D - (entity.posZ - (double)playerZ);
                     farPlaneDistance = (float)((double)farPlaneDistance * farPlaneDistanceScale);
                     farPlaneDistanceScaleBiome = (float)((double)farPlaneDistanceScaleBiome * farPlaneDistanceScale);
                  } else if(weightDefault == distance) {
                     farPlaneDistanceScale = entity.posZ - (double)playerZ;
                     farPlaneDistance = (float)((double)farPlaneDistance * farPlaneDistanceScale);
                     farPlaneDistanceScaleBiome = (float)((double)farPlaneDistanceScaleBiome * farPlaneDistanceScale);
                  }

                  fpDistanceBiomeMist += farPlaneDistance;
                  weightBiomeMist += farPlaneDistanceScaleBiome;
               }
            }
         }

         float var17 = (float)(distance * 2 * distance * 2);
         float var18 = var17 - weightBiomeMist;
         float var19 = weightBiomeMist == 0.0F?0.0F:fpDistanceBiomeMist / weightBiomeMist;
         farPlaneDistance = (fpDistanceBiomeMist * 240.0F + event.farPlaneDistance * var18) / var17;
         farPlaneDistanceScaleBiome = 0.1F * (1.0F - var19) + 0.75F * var19;
         float var20 = (farPlaneDistanceScaleBiome * weightBiomeMist + 0.75F * var18) / var17;
         MistX = entity.posX;
         MistZ = entity.posZ;
         MistFarPlaneDistance = Math.min(farPlaneDistance, event.farPlaneDistance);
         renderMist(event.fogMode, MistFarPlaneDistance, var20);
      }
   }

   private static void renderMist(int MistMode, float farPlaneDistance, float farPlaneDistanceScale) {
      if(MistMode < 0) {
         GL11.glFogf(2915, 0.0F);
         GL11.glFogf(2916, farPlaneDistance);
      } else {
         GL11.glFogf(2915, farPlaneDistance * farPlaneDistanceScale);
         GL11.glFogf(2916, farPlaneDistance);
      }

   }

   private static Vec3 postProcessColor(World world, EntityLivingBase player, float r, float g, float b, double renderPartialTicks) {
      double darkScale = (player.lastTickPosY + (player.posY - player.lastTickPosY) * renderPartialTicks) * world.provider.getVoidFogYFactor();
      int aR;
      if(player.isPotionActive(Potion.blindness)) {
         aR = player.getActivePotionEffect(Potion.blindness).getDuration();
         darkScale *= aR < 20?(double)(1.0F - (float)aR / 20.0F):0.0D;
      }

      if(darkScale < 1.0D) {
         darkScale = darkScale < 0.0D?0.0D:darkScale * darkScale;
         r = (float)((double)r * darkScale);
         g = (float)((double)g * darkScale);
         b = (float)((double)b * darkScale);
      }

      float aG;
      float aB;
      if(player.isPotionActive(Potion.nightVision)) {
         aR = player.getActivePotionEffect(Potion.nightVision).getDuration();
         aG = aR > 200?1.0F:0.7F + MathHelper.sin((float)(((double)aR - renderPartialTicks) * 3.141592653589793D * 0.20000000298023224D)) * 0.3F;
         aB = 1.0F / r;
         aB = Math.min(aB, 1.0F / g);
         aB = Math.min(aB, 1.0F / b);
         r = r * (1.0F - aG) + r * aB * aG;
         g = g * (1.0F - aG) + g * aB * aG;
         b = b * (1.0F - aG) + b * aB * aG;
      }

      if(Minecraft.getMinecraft().gameSettings.anaglyph) {
         float aR1 = (r * 30.0F + g * 59.0F + b * 11.0F) / 100.0F;
         aG = (r * 30.0F + g * 70.0F) / 100.0F;
         aB = (r * 30.0F + b * 70.0F) / 100.0F;
         r = aR1;
         g = aG;
         b = aB;
      }

      return Vec3.createVectorHelper((double)r, (double)g, (double)b);
   }

   private static Vec3 getMistBlendColorWater(World world, EntityLivingBase playerEntity, int playerX, int playerY, int playerZ, double renderPartialTicks) {
      byte distance = 2;
      float rBiomeMist = 0.0F;
      float gBiomeMist = 0.0F;
      float bBiomeMist = 0.0F;

      float bMixed;
      for(int weight = -distance; weight <= distance; ++weight) {
         for(int respirationLevel = -distance; respirationLevel <= distance; ++respirationLevel) {
            BiomeGenBase rMixed = world.getBiomeGenForCoords(playerX + weight, playerZ + respirationLevel);
            int gMixed = rMixed.waterColorMultiplier;
            bMixed = (float)((gMixed & 16711680) >> 16);
            float gPart = (float)((gMixed & '\uff00') >> 8);
            float bPart = (float)(gMixed & 255);
            double zDiff;
            if(weight == -distance) {
               zDiff = 1.0D - (playerEntity.posX - (double)playerX);
               bMixed = (float)((double)bMixed * zDiff);
               gPart = (float)((double)gPart * zDiff);
               bPart = (float)((double)bPart * zDiff);
            } else if(weight == distance) {
               zDiff = playerEntity.posX - (double)playerX;
               bMixed = (float)((double)bMixed * zDiff);
               gPart = (float)((double)gPart * zDiff);
               bPart = (float)((double)bPart * zDiff);
            }

            if(respirationLevel == -distance) {
               zDiff = 1.0D - (playerEntity.posZ - (double)playerZ);
               bMixed = (float)((double)bMixed * zDiff);
               gPart = (float)((double)gPart * zDiff);
               bPart = (float)((double)bPart * zDiff);
            } else if(respirationLevel == distance) {
               zDiff = playerEntity.posZ - (double)playerZ;
               bMixed = (float)((double)bMixed * zDiff);
               gPart = (float)((double)gPart * zDiff);
               bPart = (float)((double)bPart * zDiff);
            }

            rBiomeMist += bMixed;
            gBiomeMist += gPart;
            bBiomeMist += bPart;
         }
      }

      rBiomeMist /= 255.0F;
      gBiomeMist /= 255.0F;
      bBiomeMist /= 255.0F;
      float var20 = (float)(distance * 2 * distance * 2);
      float var21 = (float)EnchantmentHelper.getRespiration(playerEntity) * 0.2F;
      float var22 = (rBiomeMist * 0.02F + var21) / var20;
      float var23 = (gBiomeMist * 0.02F + var21) / var20;
      bMixed = (bBiomeMist * 0.2F + var21) / var20;
      return postProcessColor(world, playerEntity, var22, var23, bMixed, renderPartialTicks);
   }

   private static Vec3 getMistBlendColour(World world, EntityLivingBase playerEntity, int playerX, int playerY, int playerZ, float defR, float defG, float defB, double renderPartialTicks) {
      GameSettings settings = Minecraft.getMinecraft().gameSettings;
      int[] ranges = ForgeModContainer.blendRanges;
      int distance = 0;
      if(settings.fancyGraphics && settings.renderDistanceChunks >= 0 && settings.renderDistanceChunks < ranges.length) {
         distance = ranges[settings.renderDistanceChunks];
      }

      float rBiomeMist = 0.0F;
      float gBiomeMist = 0.0F;
      float bBiomeMist = 0.0F;
      float weightBiomeMist = 0.0F;

      float rainStrength;
      float thunderStrength;
      float weightMixed;
      for(int celestialAngle = -distance; celestialAngle <= distance; ++celestialAngle) {
         for(int baseScale = -distance; baseScale <= distance; ++baseScale) {
            BiomeGenBase rScale = world.getBiomeGenForCoords(playerX + celestialAngle, playerZ + baseScale);
            if(rScale instanceof IBiomeMist) {
               IBiomeMist gScale = (IBiomeMist)rScale;
               int bScale = gScale.getMistColour(playerX + celestialAngle, playerY, playerZ + baseScale);
               rainStrength = (float)((bScale & 16711680) >> 16);
               thunderStrength = (float)((bScale & '\uff00') >> 8);
               float processedColor = (float)(bScale & 255);
               weightMixed = 1.0F;
               double weightDefault;
               if(celestialAngle == -distance) {
                  weightDefault = 1.0D - (playerEntity.posX - (double)playerX);
                  rainStrength = (float)((double)rainStrength * weightDefault);
                  thunderStrength = (float)((double)thunderStrength * weightDefault);
                  processedColor = (float)((double)processedColor * weightDefault);
                  weightMixed = (float)((double)weightMixed * weightDefault);
               } else if(celestialAngle == distance) {
                  weightDefault = playerEntity.posX - (double)playerX;
                  rainStrength = (float)((double)rainStrength * weightDefault);
                  thunderStrength = (float)((double)thunderStrength * weightDefault);
                  processedColor = (float)((double)processedColor * weightDefault);
                  weightMixed = (float)((double)weightMixed * weightDefault);
               }

               if(baseScale == -distance) {
                  weightDefault = 1.0D - (playerEntity.posZ - (double)playerZ);
                  rainStrength = (float)((double)rainStrength * weightDefault);
                  thunderStrength = (float)((double)thunderStrength * weightDefault);
                  processedColor = (float)((double)processedColor * weightDefault);
                  weightMixed = (float)((double)weightMixed * weightDefault);
               } else if(baseScale == distance) {
                  weightDefault = playerEntity.posZ - (double)playerZ;
                  rainStrength = (float)((double)rainStrength * weightDefault);
                  thunderStrength = (float)((double)thunderStrength * weightDefault);
                  processedColor = (float)((double)processedColor * weightDefault);
                  weightMixed = (float)((double)weightMixed * weightDefault);
               }

               rBiomeMist += rainStrength;
               gBiomeMist += thunderStrength;
               bBiomeMist += processedColor;
               weightBiomeMist += weightMixed;
            }
         }
      }

      if(weightBiomeMist == 0.0F) {
         return Vec3.createVectorHelper((double)defR, (double)defG, (double)defB);
      } else {
         rBiomeMist /= 255.0F;
         gBiomeMist /= 255.0F;
         bBiomeMist /= 255.0F;
         float var28 = world.getCelestialAngle((float)renderPartialTicks);
         float var29 = MathHelper.clamp_float(MathHelper.cos(var28 * 3.1415927F * 2.0F) * 2.0F + 0.5F, 0.0F, 1.0F);
         float var30 = var29 * 0.94F + 0.06F;
         float var31 = var29 * 0.94F + 0.06F;
         float var32 = var29 * 0.91F + 0.09F;
         rainStrength = world.getRainStrength((float)renderPartialTicks);
         if(rainStrength > 0.0F) {
            var30 *= 1.0F - rainStrength * 0.5F;
            var31 *= 1.0F - rainStrength * 0.5F;
            var32 *= 1.0F - rainStrength * 0.4F;
         }

         thunderStrength = world.getWeightedThunderStrength((float)renderPartialTicks);
         if(thunderStrength > 0.0F) {
            var30 *= 1.0F - thunderStrength * 0.5F;
            var31 *= 1.0F - thunderStrength * 0.5F;
            var32 *= 1.0F - thunderStrength * 0.5F;
         }

         rBiomeMist *= var30 / weightBiomeMist;
         gBiomeMist *= var31 / weightBiomeMist;
         bBiomeMist *= var32 / weightBiomeMist;
         Vec3 var33 = postProcessColor(world, playerEntity, rBiomeMist, gBiomeMist, bBiomeMist, renderPartialTicks);
         rBiomeMist = (float)var33.xCoord;
         gBiomeMist = (float)var33.yCoord;
         bBiomeMist = (float)var33.zCoord;
         weightMixed = (float)(distance * 2 * distance * 2);
         float var34 = weightMixed - weightBiomeMist;
         var33.xCoord = (double)((rBiomeMist * weightBiomeMist + defR * var34) / weightMixed);
         var33.yCoord = (double)((gBiomeMist * weightBiomeMist + defG * var34) / weightMixed);
         var33.zCoord = (double)((bBiomeMist * weightBiomeMist + defB * var34) / weightMixed);
         return var33;
      }
   }
}
