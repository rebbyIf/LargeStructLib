package dev.rebby.largestructlib.worldgen.structure.piece;

import com.mojang.serialization.MapCodec;
import dev.rebby.largestructlib.LargeStructureLib;
import dev.rebby.largestructlib.worldgen.structure.DensityBasedStructure;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegistryBuilder;

public interface StructurePieceType<T extends DensityStructurePiece> {

    ResourceKey<Registry<StructurePieceType<?>>> REGISTRY_KEY = ResourceKey.createRegistryKey(LargeStructureLib.ofPath(DensityBasedStructure.STRUCTURE_PATH +"/pieces_type"));
    Registry<StructurePieceType<?>> REGISTRY =
            new RegistryBuilder<>(REGISTRY_KEY)
                    .create();

    MapCodec<T> codec();
}
