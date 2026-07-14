package dev.rebby.largestructlib.worldgen.structure;

import com.mojang.serialization.MapCodec;
import dev.rebby.largestructlib.LargeStructureLib;
import dev.rebby.largestructlib.worldgen.structure.placement.GridStructurePlacement;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModStructureTypes {

    public static final DeferredRegister<StructureType<?>> TYPES =
            DeferredRegister.create(BuiltInRegistries.STRUCTURE_TYPE, LargeStructureLib.MODID);

    public static final DeferredHolder<StructureType<?>, StructureType<LargeJigsawStructure>> LARGE_JIGSAW_STRUCTURE =
            TYPES.register("large_jigsaw", () -> () -> LargeJigsawStructure.CODEC);

    public interface Placement{
        DeferredRegister<StructurePlacementType<?>> TYPES =
                DeferredRegister.create(BuiltInRegistries.STRUCTURE_PLACEMENT, LargeStructureLib.MODID);

        DeferredHolder<StructurePlacementType<?>, StructurePlacementType<GridStructurePlacement>> GRID =
                TYPES.register("grid", () -> () -> GridStructurePlacement.CODEC);
    }

}
