package com.henrikstabell.mistcore;

import com.henrikstabell.mistcore.handler.MistHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = MistCore.MODID, version = MistCore.VERSION)
public class MistCore {
   public static final String MODID = "mistcore";
   public static final String VERSION = "1.2.1";

   @EventHandler
   public void preInit(FMLPreInitializationEvent event) {
      MinecraftForge.EVENT_BUS.register(new MistHandler());
   }
}
