package dev.rebby.largestructlib.worldgen.structure.piece;

import dev.rebby.largestructlib.LargeStructureLib;
import dev.rebby.largestructlib.worldgen.structure.LargeJigsawStructure;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public interface StructurePieceTypes {
    DeferredRegister<StructurePieceType<?>> TYPES =
            DeferredRegister.create(StructurePieceType.REGISTRY, LargeStructureLib.MODID);

    DeferredHolder<StructurePieceType<?>, StructurePieceType<DefaultDensityStructure>> DEFAULT =
            TYPES.register("default", () -> () -> DefaultDensityStructure.DEFAULT_STRUCTURE_CODEC);

    DeferredHolder<StructurePieceType<?>, StructurePieceType<AlternatingDensityStructure>> ALTERNATING =
            TYPES.register("alternating", () -> () -> AlternatingDensityStructure.ALTERNATING_STRUCTURE_CODEC);

    DeferredHolder<StructurePieceType<?>, StructurePieceType<LayeredGridDensityStructure>> LAYERED_GRID =
            TYPES.register("layered_grid", () -> () -> LayeredGridDensityStructure.LAYERED_GRID_STRUCTURE_CODEC);

}
