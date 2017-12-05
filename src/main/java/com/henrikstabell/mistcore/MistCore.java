package com.henrikstabell.mistcore;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import com.henrikstabell.mistcore.api.MistHandler;
import net.minecraftforge.common.MinecraftForge;

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
