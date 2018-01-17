package com.henrikstabell.mistcore;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import com.henrikstabell.mistcore.handler.MistHandler;
import net.minecraftforge.common.MinecraftForge;

@Mod(modid = MistCore.MODID, version = MistCore.VERSION)
public class MistCore {
   public static final String MODID = "mistcore";
   public static final String VERSION = "1.2.1";

   @EventHandler
   public void preInit(FMLPreInitializationEvent event) {
      MinecraftForge.EVENT_BUS.register(new MistHandler());
   }
}
