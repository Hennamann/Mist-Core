package com.henrikstabell.mistcore;

import com.henrikstabell.mistcore.handler.MistHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod(modid = this.MODID, version = this.VERSION)
public class MistCore {
   public static final String MODID = "mistcore";
   public static final String VERSION = "1.2.0";

   @EventHandler
   public void preInit(FMLPreInitializationEvent event) {
      RegisterClientHandlers();
   }

   @SideOnly(Side.CLIENT)
   public static void RegisterClientHandlers() {
      MinecraftForge.EVENT_BUS.register(new MistHandler());
   }
}