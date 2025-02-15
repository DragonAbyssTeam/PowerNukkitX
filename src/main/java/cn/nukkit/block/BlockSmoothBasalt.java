package cn.nukkit.block;

import cn.nukkit.api.PowerNukkitOnly;
import cn.nukkit.api.Since;
import cn.nukkit.blockproperty.BlockProperties;
import cn.nukkit.blockproperty.CommonBlockProperties;
import cn.nukkit.math.BlockFace;

import javax.annotation.Nonnull;

@PowerNukkitOnly
@Since("1.6.0.0-PNX")
public class BlockSmoothBasalt extends BlockBasalt {
    @PowerNukkitOnly
    @Since("1.6.0.0-PNX")
    public static final BlockProperties PROPERTIES = CommonBlockProperties.EMPTY_PROPERTIES;

    @Override
    public String getName() {
        return "Smooth Basalt";
    }

    @Override
    public int getId() {
        return 632;
    }

    @Since("1.6.0.0-PNX")
    @PowerNukkitOnly
    @Nonnull
    @Override
    public BlockProperties getProperties() {
        return PROPERTIES;
    }

    @PowerNukkitOnly
    @Since("1.6.0.0-PNX")
    public BlockFace.Axis getPillarAxis() {
        // ignore
        return null;
    }

    @PowerNukkitOnly
    @Since("1.6.0.0-PNX")
    public void setPillarAxis(BlockFace.Axis axis) {
        // ignore
    }
}
