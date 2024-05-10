package com.igrium.craftmesh.command;

import java.util.Arrays;
import java.util.Collection;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.argument.CoordinateArgument;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

public class DefaultClientPosArgument implements ClientPosArgument {

    public static final Collection<String> EXAMPLES = Arrays.asList("0 0 0", "~ ~ ~", "^ ^ ^", "^1 ^ ^-5", "0.1 -0.5 .9", "~0.5 ~1 ~-5");
    public static final SimpleCommandExceptionType INCOMPLETE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.pos3d.incomplete"));
    public static final SimpleCommandExceptionType MIXED_COORDINATE_EXCEPTION = new SimpleCommandExceptionType(Text.translatable("argument.pos.mixed"));

    private final CoordinateArgument x;
    private final CoordinateArgument y;
    private final CoordinateArgument z;

    public DefaultClientPosArgument(CoordinateArgument x, CoordinateArgument y, CoordinateArgument z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    @Override
    public Vec3d toAbsolutePos(FabricClientCommandSource source) {
        Vec3d pos = source.getPosition();
        return new Vec3d(this.x.toAbsoluteCoordinate(pos.x), this.y.toAbsoluteCoordinate(pos.y), this.z.toAbsoluteCoordinate(pos.z));
    }

    @Override
    public Vec2f toAbsoluteRotation(FabricClientCommandSource source) {
        Vec2f rot = source.getRotation();
        return new Vec2f((float) this.x.toAbsoluteCoordinate(rot.x), (float) this.y.toAbsoluteCoordinate(rot.y));
    }

    @Override
    public boolean isXRelative() {
        return x.isRelative();
    }

    @Override
    public boolean isYRelative() {
        return y.isRelative();
    }

    @Override
    public boolean isZRelative() {
        return z.isRelative();
    }

    public static DefaultClientPosArgument parse(StringReader reader) throws CommandSyntaxException {
        int cursor = reader.getCursor();

        CoordinateArgument x = CoordinateArgument.parse(reader);
        if (!reader.canRead() || reader.peek() != ' ') {
            reader.setCursor(cursor);
            throw INCOMPLETE_EXCEPTION.createWithContext(reader);
        }

        reader.skip();
        CoordinateArgument y = CoordinateArgument.parse(reader);
        if (!reader.canRead() || reader.peek() != ' ') {
            reader.setCursor(cursor);
            throw INCOMPLETE_EXCEPTION.createWithContext(reader);
        }

        reader.skip();
        CoordinateArgument z = CoordinateArgument.parse(reader);
        return new DefaultClientPosArgument(x, y, z);
    }

    public static DefaultClientPosArgument parse(StringReader reader, boolean centerIntegers) throws CommandSyntaxException {
        int cursor = reader.getCursor();

        CoordinateArgument x = CoordinateArgument.parse(reader, centerIntegers);
        if (!reader.canRead() || reader.peek() != ' ') {
            reader.setCursor(cursor);
            throw INCOMPLETE_EXCEPTION.createWithContext(reader);
        }

        reader.skip();
        CoordinateArgument y = CoordinateArgument.parse(reader, centerIntegers);
        if (!reader.canRead() || reader.peek() != ' ') {
            reader.setCursor(cursor);
            throw INCOMPLETE_EXCEPTION.createWithContext(reader);
        }

        reader.skip();
        CoordinateArgument z = CoordinateArgument.parse(reader, centerIntegers);
        return new DefaultClientPosArgument(x, y, z);
    }

    public static DefaultClientPosArgument absolute(double x, double y, double z) {
        return new DefaultClientPosArgument(new CoordinateArgument(false, x),
                new CoordinateArgument(false, y),
                new CoordinateArgument(false, z));
    }

    public static DefaultClientPosArgument absolute(Vec2f vec) {
        return new DefaultClientPosArgument(new CoordinateArgument(false, vec.x),
                new CoordinateArgument(false, vec.y),
                new CoordinateArgument(false, 0));
    }

    public static DefaultClientPosArgument zero() {
        return new DefaultClientPosArgument(new CoordinateArgument(false, 0),
                new CoordinateArgument(false, 0),
                new CoordinateArgument(false, 0));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof DefaultClientPosArgument other)) {
            return false;
        }

        return (this.x.equals(other.x)
                && this.y.equals(other.y)
                && this.z.equals(other.z));
    }

    @Override
    public int hashCode() {
        int i = x.hashCode();
        i = 31 * i + y.hashCode();
        i = 31 * i + z.hashCode();
        return i;
    }
}
