package com.henrikstabell.mistcore;

import com.henrikstabell.mistcore.api.MistBiome;
import com.henrikstabell.mistcore.api.MistCoreApi;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.henrikstabell.mistcore.MistCore.MODID;

@Mod(MODID)
public class MistCore {

    public static final String MODID = "mistcore";
    public static final Logger LOGGER = LogManager.getLogger();

    public MistCore() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::registerRegistry);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::loadComplete);
    }

    private void registerRegistry(final RegistryEvent.NewRegistry event) {
        final IForgeRegistry<MistBiome> MIST_BIOME_REGISTRY = new RegistryBuilder<MistBiome>()
                .setIDRange(0, 0x0FFFFF)
                .setName(new ResourceLocation(MODID, "mist_biomes"))
                .setType(MistBiome.class)
                .create();
    }
    
    private void loadComplete(final FMLLoadCompleteEvent event) {}
}