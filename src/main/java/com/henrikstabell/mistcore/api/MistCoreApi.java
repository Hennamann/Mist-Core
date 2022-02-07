package com.henrikstabell.mistcore.api;

import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

/**
 * Main MistCore API class.
 * The Mist Core API is not finalized, changes are bound to happen between versions.
 * You have been warned.
 */
public class MistCoreApi {

    public static final IForgeRegistry<MistBiome> MIST_BIOMES = RegistryManager.ACTIVE.getRegistry(MistBiome.class);

}
