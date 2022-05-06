package cn.nukkit.entity.mob;

import cn.nukkit.entity.EntityPhysical;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;

/**
 * @author MagicDroidX (Nukkit Project)
 */
public abstract class EntityMob extends EntityPhysical {
    public EntityMob(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

}
