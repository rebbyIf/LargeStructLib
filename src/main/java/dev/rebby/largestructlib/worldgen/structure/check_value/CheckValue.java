package dev.rebby.largestructlib.worldgen.structure.check_value;

import com.mojang.serialization.Codec;
import dev.rebby.largestructlib.LargeStructureLib;
import dev.rebby.largestructlib.worldgen.structure.DensityBasedStructure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.DensityFunction;

public interface CheckValue {
    ResourceKey<Registry<CheckValue>> REGISTRY_KEY = ResourceKey.createRegistryKey(LargeStructureLib.ofPath(DensityBasedStructure.STRUCTURE_PATH +"/check_value"));
    Codec<CheckValue> DIRECT_CODEC = CheckValueType.REGISTRY.byNameCodec().dispatch("type",
            CheckValue::getType, CheckValueType::codec);
    Codec<Holder<CheckValue>> CODEC = RegistryFileCodec.create(REGISTRY_KEY, DIRECT_CODEC);

    boolean check(Holder<DensityFunction> check, BlockPos loc, BlockPos minLoc, BlockPos maxLoc, int scale);

    boolean doesOverrideIfTrue();

    boolean doesOverrideIfFalse();

    CheckValueType<?> getType();
}