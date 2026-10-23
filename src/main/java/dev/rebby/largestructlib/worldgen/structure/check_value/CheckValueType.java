package dev.rebby.largestructlib.worldgen.structure.check_value;

import com.mojang.serialization.MapCodec;
import dev.rebby.largestructlib.LargeStructureLib;
import dev.rebby.largestructlib.worldgen.structure.DensityBasedStructure;
import dev.rebby.largestructlib.worldgen.structure.piece.StructurePieceType;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.RegistryBuilder;

public interface CheckValueType<T extends CheckValue> {

    ResourceKey<Registry<CheckValueType<?>>> REGISTRY_KEY = ResourceKey.createRegistryKey(LargeStructureLib.ofPath(DensityBasedStructure.STRUCTURE_PATH +"/check_value_type"));
    Registry<CheckValueType<?>> REGISTRY =
            new RegistryBuilder<>(REGISTRY_KEY)
                    .create();

    MapCodec<T> codec();
}
