package dev.rebby.largestructlib.worldgen.structure.check_value;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.rebby.largestructlib.worldgen.structure.util.MultiNoiseValueHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.DensityFunction;

public record CheckPlaneForSingleValue(double min, double max, String axis) implements CheckValue {

    public static final MapCodec<CheckPlaneForSingleValue> PLANE_SINGLE_VALUE_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                            Codec.DOUBLE.fieldOf("minimum").forGetter(CheckPlaneForSingleValue::min),
                            Codec.DOUBLE.fieldOf("maximum").forGetter(CheckPlaneForSingleValue::max),
                            Codec.STRING.fieldOf("axis").forGetter(CheckPlaneForSingleValue::axis))
                    .apply(instance, CheckPlaneForSingleValue::new));

    @Override
    public boolean check(Holder<DensityFunction> check, BlockPos loc, BlockPos minLoc, BlockPos maxLoc, int scale) {
        switch (axis) {
            case "x" -> {
                for (int j = minLoc.getY(); j < maxLoc.getY(); j += scale) {
                    for (int k = minLoc.getZ(); k < maxLoc.getZ(); k += scale) {
                        double density = MultiNoiseValueHolder.getInstance()
                                .getValue(check, new BlockPos(loc.getX(), j, k));
                        if (density >= min && density < max) {
                            return true;
                        }
                    }
                }
            }
            case "y" -> {
                for (int i = minLoc.getX(); i < maxLoc.getX(); i += scale) {
                    for (int k = minLoc.getZ(); k < maxLoc.getZ(); k += scale) {
                        double density = MultiNoiseValueHolder.getInstance()
                                .getValue(check, new BlockPos(i, loc.getY(), k));
                        if (density >= min && density < max) {
                            return true;
                        }
                    }
                }
            }
            case "z" -> {
                for (int i = minLoc.getX(); i < maxLoc.getX(); i += scale) {
                    for (int j = minLoc.getY(); j < maxLoc.getY(); j += scale) {
                        double density = MultiNoiseValueHolder.getInstance()
                                .getValue(check, new BlockPos(i, j, loc.getZ()));
                        if (density >= min && density < max) {
                            return true;
                        }
                    }
                }
            }
            default -> throw new IllegalArgumentException("Error: axis does not equal any true axis of location");
        }

        return false;
    }

    @Override
    public boolean doesOverrideIfTrue() {
        return false;
    }

    @Override
    public boolean doesOverrideIfFalse() {
        return true;
    }

    @Override
    public CheckValueType<?> getType() {
        return CheckValueTypes.PLANE_FOR_SINGLE_VALUE.get();
    }
}
