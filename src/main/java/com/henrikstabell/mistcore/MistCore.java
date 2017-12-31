package com.henrikstabell.mistcore;

import com.henrikstabell.mistcore.handler.MistHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(
   modid = "mistcore",
   version = "1.0"
)
public class MistCore {
   public static final String MODID = "mistcore";
   public static final String VERSION = "1.0";

   @EventHandler
   public void preInit(FMLPreInitializationEvent event) {
      MinecraftForge.EVENT_BUS.register(new MistHandler());
   }
}
