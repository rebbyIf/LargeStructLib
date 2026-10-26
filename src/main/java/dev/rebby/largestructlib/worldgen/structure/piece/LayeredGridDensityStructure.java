package dev.rebby.largestructlib.worldgen.structure.piece;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.rebby.largestructlib.worldgen.structure.check_value.CheckValue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public record LayeredGridDensityStructure(String name, Holder<StructureTemplatePool> basePool,
                                          Holder<StructureTemplatePool> middlePool,
                                          Holder<StructureTemplatePool> topPool, BlockPos boundingSize,
                                          BlockPos size, BlockPos location, int minY, int maxY, int yOffset,
                                          double rarity, List<String> grid3D) implements DensityStructurePiece {

    public static final MapCodec<LayeredGridDensityStructure> LAYERED_GRID_STRUCTURE_CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                            Codec.STRING.optionalFieldOf("name", "unnamed").forGetter(LayeredGridDensityStructure::name),
                            StructureTemplatePool.CODEC.fieldOf("base_elements").forGetter(structure -> structure.basePool),
                            StructureTemplatePool.CODEC.fieldOf("middle_elements").forGetter(structure -> structure.middlePool),
                            StructureTemplatePool.CODEC.fieldOf("top_elements").forGetter(structure -> structure.topPool),
                            BlockPos.CODEC.fieldOf("bounding_size").forGetter(LayeredGridDensityStructure::boundingSize),
                            BlockPos.CODEC.optionalFieldOf("size", new BlockPos(1, 1, 1)).forGetter(LayeredGridDensityStructure::size),
                            BlockPos.CODEC.fieldOf("location").forGetter(LayeredGridDensityStructure::location),
                            Codec.INT.optionalFieldOf("min_y", 0).forGetter(LayeredGridDensityStructure::minY),
                            Codec.INT.fieldOf("max_y").forGetter(LayeredGridDensityStructure::maxY),
                            Codec.INT.optionalFieldOf("y_offset", 0).forGetter(LayeredGridDensityStructure::yOffset),
                            Codec.doubleRange(0.0,1.0).optionalFieldOf("rarity", 1.0).forGetter(LayeredGridDensityStructure::rarity),
                            Codec.STRING.listOf().fieldOf("grid").forGetter(LayeredGridDensityStructure::grid3D))
                    .apply(instance, LayeredGridDensityStructure::new));


    @Override
    public boolean place(List<PoolElementStructurePiece> pieces, int scale, BlockPos pos, Holder<DensityFunction> densityFunction,
                         StructureTemplateManager structureTemplateManager, Structure.GenerationContext context,
                         Map<String, CheckValue> values, PoolAliasLookup aliasLookup,
                         LiquidSettings liquidSettings, int top, int bottom) {

        BlockPos boundingScale = size.multiply(scale);

        if (boundingSize.getX() * boundingSize.getY() * boundingSize.getZ() > grid3D.size() ||
                maxY < minY ||
                yOffset >= size.getY()) {
            throw new IndexOutOfBoundsException("Scaling out of bounds for structure " + getName());
        }

        BlockPos distanceFromOrigin = new BlockPos(
                ((pos.getX() % boundingScale.getX()) + boundingScale.getX()) % boundingScale.getX(),
                (((pos.getY() + yOffset*scale) % boundingScale.getY()) + boundingScale.getY()) % boundingScale.getY(),
                ((pos.getZ() % boundingScale.getZ()) + boundingScale.getZ()) % boundingScale.getZ());



        BlockPos origin = pos.subtract(distanceFromOrigin);

        BlockPos gridDistance = new BlockPos(Math.abs(origin.getX()) % (boundingScale.getX() * 2),
                0,
                Math.abs(origin.getZ()) % (boundingScale.getZ() * 2));

        if (!gridDistance.equals(boundingScale.offset(0, -boundingScale.getY(), 0))) {
            return false;
        }

        boolean hasBase = false;
        BlockPos basePos = new BlockPos(origin.getX(),
                0, origin.getZ());

        for (int y = origin.getY(); y >= bottom; y -= boundingScale.getY()) {
            basePos = basePos.atY(y);
            if (checkPlace(basePos, scale, values, densityFunction, location, boundingSize, grid3D)) {

                if (DensityStructurePiece.checkStructure(basePos, context.seed(),
                        basePool.value())) {
                    hasBase = true;
                    break;
                }
            }
        }

        if (!hasBase) {
            return false;
        }

        LegacyRandomSource r = new LegacyRandomSource(context.seed());
        LegacyRandomSource r1 = new LegacyRandomSource(basePos.asLong());
        LegacyRandomSource random = new LegacyRandomSource(r.nextLong() + r1.nextLong());
        if (random.nextDouble() > rarity) {
            return false;
        }

        int setY = random.nextInt(minY, maxY) * boundingScale.getY();

        if (origin.getY() > setY + basePos.getY() || origin.getY() < basePos.getY()) {
            return false;
        }

        StructureTemplatePool templatePool = middlePool.value();
        if (basePos.equals(origin)) {
            templatePool = basePool.value();
        } else if (setY + basePos.getY() == origin.getY()) {
            templatePool = topPool.value();
        }

        if (!distanceFromOrigin.equals(BlockPos.ZERO)) {

            return true;

//            return StructurePiece.checkStructure(origin, world.toServerWorld().getSeed(),
//                    random, templatePool);
        }

        StructurePoolElement structurePoolElement = DensityStructurePiece.getStructure(pos, context.seed(), templatePool);

        pieces.add(new PoolElementStructurePiece(
                context.structureTemplateManager(),
                structurePoolElement,
                pos,
                structurePoolElement.getGroundLevelDelta(),
                Rotation.NONE,
                structurePoolElement.getBoundingBox(context.structureTemplateManager(), pos, Rotation.NONE),
                liquidSettings));

        return true;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public @NotNull BlockPos getSize() {
        return size;
    }

    @Override
    public StructurePieceType<?> getType() {
        return StructurePieceTypes.LAYERED_GRID.get();
    }
}