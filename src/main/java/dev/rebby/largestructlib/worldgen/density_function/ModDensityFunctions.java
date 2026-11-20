package dev.rebby.largestructlib.worldgen.density_function;

import com.mojang.serialization.MapCodec;
import dev.rebby.largestructlib.LargeStructureLib;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public interface ModDensityFunctions {

    DeferredRegister<MapCodec<? extends DensityFunction>> FUNCTION_TYPES =
            DeferredRegister.create(BuiltInRegistries.DENSITY_FUNCTION_TYPE, LargeStructureLib.MODID);

    DeferredHolder<MapCodec<? extends DensityFunction>, MapCodec<Interpolate>> INTERPOLATE =
            FUNCTION_TYPES.register("interpolate", () -> Interpolate.CODEC);
}
