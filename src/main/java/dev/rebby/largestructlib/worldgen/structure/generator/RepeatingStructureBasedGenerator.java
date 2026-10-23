package dev.rebby.largestructlib.worldgen.structure.generator;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pools.EmptyPoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;

import java.util.Optional;

public class RepeatingStructureBasedGenerator {

    public static Optional<Structure.GenerationStub> generate(Structure.GenerationContext context, BlockPos pos,
                                                              Holder<StructureTemplatePool> startPool,
                                                              PoolAliasLookup aliasLookup,
                                                              LiquidSettings liquidSettings,
                                                              RandomSource random){

        RegistryAccess dynamicRegistryManager = context.registryAccess();
        Registry<StructureTemplatePool> registry = dynamicRegistryManager.registryOrThrow(Registries.TEMPLATE_POOL);
        StructureTemplatePool structuretemplatepool = startPool.unwrapKey().flatMap((key) -> registry.getOptional(aliasLookup.lookup(key))).orElse(startPool.value());
        StructurePoolElement structurePoolElement = structuretemplatepool.getRandomTemplate(random);

        if (structurePoolElement == EmptyPoolElement.INSTANCE){
            return Optional.empty();
        }

        PoolElementStructurePiece structurePiece = new PoolElementStructurePiece(
                context.structureTemplateManager(),
                structurePoolElement,
                pos,
                structurePoolElement.getGroundLevelDelta(),
                Rotation.NONE,
                structurePoolElement.getBoundingBox(context.structureTemplateManager(), pos, Rotation.NONE),
                liquidSettings);

        return Optional.of(new Structure.GenerationStub(pos, collector -> collector.addPiece(
                structurePiece
        )));
    }
}