package dev.rebby.largestructlib.worldgen.feature;

import dev.rebby.largestructlib.LargeStructureLib;
import dev.rebby.largestructlib.worldgen.feature.configuration.DensitySamplerConfiguration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public interface Features {

    DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(BuiltInRegistries.FEATURE, LargeStructureLib.MODID);

    DeferredHolder<Feature<?>, DensitySampler> DENSITY_SAMPLER = FEATURES.register(
            "density_sampler",
            () -> new DensitySampler(DensitySamplerConfiguration.CODEC));
}
