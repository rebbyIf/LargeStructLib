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
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public record LayeredGridDensityStructure(String name, Holder<StructureTemplatePool> basePool,
                                          Holder<StructureTemplatePool> middlePool,
                                          Holder<StructureTemplatePool> topPool, BlockPos boundingSize,
                                          BlockPos size, BlockPos location, int minY, int maxY, int yOffset,
                                          List<String> grid3D) implements DensityStructurePiece {

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
                            Codec.STRING.listOf().fieldOf("grid").forGetter(LayeredGridDensityStructure::grid3D))
                    .apply(instance, LayeredGridDensityStructure::new));





    private boolean checkPlace(BlockPos pos, int scale, Map<String, CheckValue> values, DensityFunction densityFunction) {
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

    @Override
    public boolean place(List<PoolElementStructurePiece> pieces, int scale, BlockPos pos, DensityFunction densityFunction,
                         StructureTemplateManager structureTemplateManager, Structure.GenerationContext context,
                         Map<String, CheckValue> values, PoolAliasLookup aliasLookup,
                         LiquidSettings liquidSettings) {

        BlockPos boundingScale = size.multiply(scale);

        if (boundingSize.getX() * boundingSize.getY() * boundingSize.getZ() > grid3D.size() ||
                maxY % boundingScale.getY() != 0 || minY % boundingScale.getY() != 0 ||
                maxY - boundingScale.getY() * 3 - yOffset * scale <= minY + yOffset * scale ||
                yOffset >= size.getY()) {
            throw new IndexOutOfBoundsException("Scaling out of bounds for structure " + getName());
        }

        if (pos.getY() > maxY - boundingScale.getY() - yOffset*scale || pos.getY() < minY + yOffset*scale) {
            return false;
        }

        BlockPos distanceFromOrigin = new BlockPos(Math.abs(pos.getX()) % boundingScale.getX(),
                Math.abs(pos.getY() + yOffset*scale) % boundingScale.getY(),
                Math.abs(pos.getZ()) % boundingScale.getZ());



        BlockPos origin = pos.subtract(distanceFromOrigin);

        BlockPos gridDistance = new BlockPos(Math.abs(origin.getX()) % (boundingScale.getX() * 2),
                0,
                Math.abs(origin.getZ()) % (boundingScale.getZ() * 2));

        if (!gridDistance.equals(BlockPos.ZERO) && !gridDistance.equals(boundingScale.offset(0, -boundingScale.getY(), 0))) {
            return false;
        }

        boolean hasBase = false;
        BlockPos.MutableBlockPos basePos = new BlockPos.MutableBlockPos(origin.getX(),
                maxY - boundingScale.getY() * 3 - yOffset*scale, origin.getZ());

        for (int y = maxY - boundingScale.getY() * 3 - yOffset*scale; y >= minY + yOffset*scale; y -= boundingScale.getY()) {
            basePos.setY(y);
            if (checkPlace(basePos.immutable(), scale, values, densityFunction)) {

                if (DensityStructurePiece.checkStructure(basePos.immutable(), context.seed(),
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
        LegacyRandomSource r1 = new LegacyRandomSource(pos.atY(0).asLong());
        LegacyRandomSource random = new LegacyRandomSource(r.nextLong() + r1.nextLong());
        int setY = (random.nextInt((maxY - basePos.getY() - yOffset*scale) / boundingScale.getY())+3) * boundingScale.getY();

        if (origin.getY() > setY + basePos.getY() || origin.getY() < basePos.getY()) {
            return false;
        }

        StructureTemplatePool templatePool = middlePool.value();
        if (basePos.equals(origin)) {
            templatePool = basePool.value();
        } else if (setY + basePos.getY() == origin.getY() || origin.getY() == maxY - boundingScale.getY() - yOffset*scale) {
            templatePool = topPool.value();
        }


        if (!distanceFromOrigin.equals(BlockPos.ZERO)) {

            return false;

//            return StructurePiece.checkStructure(origin, world.toServerWorld().getSeed(),
//                    random, templatePool);
        }

        if (!DensityStructurePiece.checkStructure(basePos.immutable(), context.seed(),
                templatePool)) {
            return false;
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