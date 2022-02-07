package com.henrikstabell.mistcore.api;

import net.minecraftforge.registries.ForgeRegistryEntry;

/**
 * Register an instance of this class for each of your biomes using the Forge Registry event.
 * Make sure the ResourceLocation is the same as an existing Biome in the normal Biome Registry, otherwise it will not work!
 */
public class MistBiome extends ForgeRegistryEntry<MistBiome> {

    private final float mistDensity;
    private final int mistMultiplier;
    private final MistBiomeCategories mistCategory;
    private final boolean snowy;

    public MistBiome(float mistDensity, int mistMultiplier, MistBiomeCategories mistCategory, boolean snowy) {
        this.mistDensity = mistDensity;
        this.mistMultiplier = mistMultiplier;
        this.mistCategory = mistCategory;
        this.snowy = snowy;
    }

    /**
     * Returns the mistDensity for the biome. Used by Mist Core when rendering the mist.
     * @return float mistDensity
     */
    public float getMistDensity() {
        return mistDensity;
    }

    /**
     * Returns the mistMultiplier for the biome, this is used for calculating power generation in Mist Power.
     * @return int mistMultiplier
     */
    public int getMistMultiplier() {
        return mistMultiplier;
    }

    /**
     * Returns the mistCategory for the biome, this is used to determine which mobs should spawn in
     * the biome.
     * @return {@link MistBiomeCategories} mistCategory
     */
    public MistBiomeCategories getMistCategory() {
        return mistCategory;
    }

    /**
     * Returns whether or not the biome is snowy.
     * @return boolean snowy
     */
    public boolean isSnowy() {
        return snowy;
    }
}
