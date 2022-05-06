package cn.nukkit.entity.passive;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityAgeable;
import cn.nukkit.entity.EntityPhysical;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;

/**
 * @author MagicDroidX (Nukkit Project)
 */
public abstract class EntityAnimal extends EntityPhysical implements EntityAgeable {
    public EntityAnimal(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public boolean isBaby() {
        return this.getDataFlag(DATA_FLAGS, Entity.DATA_FLAG_BABY);
    }

    public boolean isBreedingItem(Item item) {
        return item.getId() == Item.WHEAT; //default
    }

    @Override
    protected double getStepHeight() {
        return 0.5;
    }
}
