package dev.rebby.largestructlib.worldgen.structure.piece;

import com.mojang.serialization.Codec;
import dev.rebby.largestructlib.LargeStructureLib;
import dev.rebby.largestructlib.worldgen.structure.DensityBasedStructure;
import dev.rebby.largestructlib.worldgen.structure.check_value.CheckValue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pools.EmptyPoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public interface DensityStructurePiece {

    ResourceKey<Registry<DensityStructurePiece>> REGISTRY_KEY = ResourceKey.createRegistryKey(LargeStructureLib.ofPath(DensityBasedStructure.STRUCTURE_PATH +"/pieces"));
    Codec<DensityStructurePiece> DIRECT_CODEC = StructurePieceType.REGISTRY.byNameCodec().dispatch("type",
            DensityStructurePiece::getType, StructurePieceType::codec);
    Codec<Holder<DensityStructurePiece>> CODEC = RegistryFileCodec.create(REGISTRY_KEY, DIRECT_CODEC);

    static boolean checkStructure(BlockPos pos, long seed, StructureTemplatePool dataPool) {
        LegacyRandomSource r = new LegacyRandomSource(seed);
        LegacyRandomSource r1 = new LegacyRandomSource(pos.asLong());

        LegacyRandomSource random = new LegacyRandomSource(r.nextLong() + r1.nextLong());



        return dataPool.getRandomTemplate(random) != EmptyPoolElement.INSTANCE;
    }

    static StructurePoolElement getStructure(BlockPos pos, long seed, StructureTemplatePool dataPool) {
        LegacyRandomSource r = new LegacyRandomSource(seed);
        LegacyRandomSource r1 = new LegacyRandomSource(pos.asLong());

        LegacyRandomSource random = new LegacyRandomSource(r.nextLong() + r1.nextLong());

        return dataPool.getRandomTemplate(random);
    }

    default boolean checkPlace(BlockPos pos, int scale, Map<String, CheckValue> values, Holder<DensityFunction> densityFunction
            , BlockPos location, BlockPos boundingSize, List<String> grid3D) {
        BlockPos min = pos.subtract(location.multiply(scale));
        BlockPos max = min.offset(boundingSize.multiply(scale));

        int index = 0;
        for (int j = 0; j < boundingSize.getY(); j++) {
            for (int i = 0; i < boundingSize.getX(); i++) {
                for (int k = 0; k < boundingSize.getZ(); k++) {
                    CheckValue value = values.get(grid3D.get(index));
                    if (value != null) {
                        BlockPos gridLoc = location.multiply(-1).offset(i, j, k).multiply(scale);
                        BlockPos loc = pos.offset(gridLoc);
                        boolean result = value.check(densityFunction, loc, min, max, scale);
                        if (!result && value.doesOverrideIfFalse()) {
                            return false;
                        }

                        if (result && value.doesOverrideIfTrue()) {
                            return true;
                        }


                    }

                    index++;
                }
            }
        }
        return true;
    }

    boolean place(List<PoolElementStructurePiece> pieces, int scale, BlockPos pos, Holder<DensityFunction> densityFunction,
                  StructureTemplateManager structureTemplateManager, Structure.GenerationContext context,
                  Map<String, CheckValue> values, PoolAliasLookup aliasLookup,
                  LiquidSettings liquidSettings, int top, int bottom);

    String getName();

    @NotNull
    BlockPos getSize();

    StructurePieceType<?> getType();
}