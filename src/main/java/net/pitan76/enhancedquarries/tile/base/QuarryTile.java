package net.pitan76.enhancedquarries.tile.base;

import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.pitan76.enhancedquarries.block.Frame;
import net.pitan76.enhancedquarries.block.base.Quarry;
import net.pitan76.mcpitanlib.api.event.block.TileCreateEvent;
import net.pitan76.mcpitanlib.api.event.nbt.ReadNbtArgs;
import net.pitan76.mcpitanlib.api.event.nbt.WriteNbtArgs;
import net.pitan76.mcpitanlib.api.gui.inventory.IInventory;
import net.pitan76.mcpitanlib.api.util.BlockStateUtil;
import net.pitan76.mcpitanlib.api.util.InventoryUtil;
import net.pitan76.mcpitanlib.api.util.ItemStackUtil;
import net.pitan76.mcpitanlib.api.util.WorldUtil;
import net.pitan76.mcpitanlib.api.util.math.BoxUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class QuarryTile extends BaseEnergyTile implements IInventory, SidedInventory {
    // Container
    public DefaultedList<ItemStack> invItems = DefaultedList.ofSize(27, ItemStack.EMPTY);

    public IInventory inventory = this;

    // Fluid
    /*
    SingleVariantStorage<FluidVariant> fluidStorage = new SingleVariantStorage<>() {
        @Override
        protected FluidVariant getBlankVariant() {
            return FluidVariant.blank();
        }

        @Override
        protected long getCapacity(FluidVariant variant) {
            return getMaxStoredExp();
        }

        @Override
        protected boolean canInsert(FluidVariant variant) {
            return false;
        }

        @Override
        protected void onFinalCommit() {
            super.onFinalCommit();
            markDirty();
        }
    };

    public SingleVariantStorage<FluidVariant> getFluidStorage() {
        return fluidStorage;
    }

     */

    // 経験値の量
    private int storedExp = 0;

    // 経験値の最大量
    public int getMaxStoredExp() {
        return 10_000;
    }

    public int getStoredExp() {
        return storedExp;
    }

    public void setStoredExp(int storedExp) {
        this.storedExp = storedExp;
    }

    public void addStoredExp(int storedExp) {
        this.storedExp += storedExp;
    }

    public void removeStoredExp(int storedExp) {
        this.storedExp -= storedExp;
    }

    // モジュール

    // - 岩盤破壊モジュール
    private boolean canBedrockBreak = false;

    public boolean canBedrockBreak() {
        return canBedrockBreak;
    }

    public void setBedrockBreak(boolean canBedrockBreak) {
        this.canBedrockBreak = canBedrockBreak;
    }

    // - シルクタッチモジュール
    private boolean isSetSilkTouch = false;

    public boolean isSetSilkTouch() {
        return isSetSilkTouch;
    }

    public void setSilkTouchModule(boolean isSetSilkTouch) {
        this.isSetSilkTouch = isSetSilkTouch;
    }

    // - 経験値採取モジュール
    private boolean isSetExpCollect = false;

    public boolean isSetExpCollect() {
        return isSetExpCollect;
    }

    public void setExpCollectModule(boolean isSetExpCollect) {
        this.isSetExpCollect = isSetExpCollect;
    }

    // - 幸運モジュール
    private boolean isSetLuck = false;

    public boolean isSetLuck() {
        return isSetLuck;
    }

    public void setLuckModule(boolean isSetLuck) {
        this.isSetLuck = isSetLuck;
    }

    // - モブキルモジュール
    private boolean isSetMobKill = false;

    public boolean isSetMobKill() {
        return isSetMobKill;
    }

    public void setMobKillModule(boolean isSetMobKill) {
        this.isSetMobKill = isSetMobKill;
    }

    // - モブ消去モジュール
    private boolean isSetMobDelete = false;

    public boolean isSetMobDelete() {
        return isSetMobDelete;
    }

    public void setMobDeleteModule(boolean isSetMobDelete) {
        this.isSetMobDelete = isSetMobDelete;
    }

    // ----

    // 液体を置き換えるか？
    public boolean canReplaceFluid() {
        return false;
    }

    // 液体を何に置き換えるか？
    public Block getReplaceFluidWithBlock() {
        return Blocks.GLASS;
    }

    // ブロック1回破壊分に対するエネルギーのコスト
    public long getEnergyCost() {
        return 30;
    }

    // フレーム設置に必要なエネルギーのコスト
    public long getPlaceFrameEnergyCost() {
        return 40;
    }

    // 液体をガラスに置き換えるエネルギーのコスト
    public long getReplaceFluidEnergyCost() {
        return 120;
    }

    // エネルギーの容量
    public long getMaxEnergy() {
        return 5000;
    }

    // エネルギーの最大出力
    public long getMaxOutput() {
        return 0;
    }

    // エネルギーの最大入力
    public long getMaxInput() {
        return 500;
    }
    // ----

    // NBT

    public void writeNbt(WriteNbtArgs args) {
        NbtCompound tag = args.getNbt();
        InventoryUtil.writeNbt(args, getItems());

        tag.putDouble("coolTime", coolTime);
        if (canBedrockBreak)
            tag.putBoolean("module_bedrock_break", true);
        if (isSetMobKill)
            tag.putBoolean("module_mob_kill", true);
        if (isSetLuck)
            tag.putBoolean("module_luck", true);
        if (isSetSilkTouch)
            tag.putBoolean("module_silk_touch", true);
        if (isSetMobDelete)
            tag.putBoolean("module_mob_delete", true);
        if (isSetExpCollect)
            tag.putBoolean("module_exp_collect", true);

        if (pos1 != null) {
            tag.putInt("rangePos1X", getPos1().getX());
            tag.putInt("rangePos1Y", getPos1().getY());
            tag.putInt("rangePos1Z", getPos1().getZ());
        }
        if (pos2 != null) {
            tag.putInt("rangePos2X", getPos2().getX());
            tag.putInt("rangePos2Y", getPos2().getY());
            tag.putInt("rangePos2Z", getPos2().getZ());
        }

        tag.putInt("storedExp", getStoredExp());

        /*
        tag.put("variant", fluidStorage.variant.toNbt());
        tag.putLong("amount", fluidStorage.amount);
         */
    }

    public void readNbt(ReadNbtArgs args) {
        NbtCompound tag = args.getNbt();
        if (tag.contains("Items")) {
            InventoryUtil.readNbt(args, getItems());
        }

        if (tag.contains("coolTime")) coolTime = tag.getDouble("coolTime");
        if (tag.contains("module_bedrock_break")) canBedrockBreak = tag.getBoolean("module_bedrock_break");
        if (tag.contains("module_mob_delete")) isSetMobDelete = tag.getBoolean("module_mob_delete");
        if (tag.contains("module_mob_kill")) isSetMobKill = tag.getBoolean("module_mob_kill");
        if (tag.contains("module_luck")) isSetLuck = tag.getBoolean("module_luck");
        if (tag.contains("module_silk_touch")) isSetSilkTouch = tag.getBoolean("module_silk_touch");
        if (tag.contains("module_exp_collect")) isSetExpCollect = tag.getBoolean("module_exp_collect");
        if (tag.contains("rangePos1X")
                && tag.contains("rangePos1Y")
                && tag.contains("rangePos1Z")
                && tag.contains("rangePos2X")
                && tag.contains("rangePos2Y")
                && tag.contains("rangePos2Z")) {
            setPos1(new BlockPos(tag.getInt("rangePos1X"), tag.getInt("rangePos1Y"), tag.getInt("rangePos1Z")));
            setPos2(new BlockPos(tag.getInt("rangePos2X"), tag.getInt("rangePos2Y"), tag.getInt("rangePos2Z")));
        }

        if (tag.contains("storedExp")) setStoredExp(tag.getInt("storedExp"));

        /*
        if (tag.contains("variant"))
            fluidStorage.variant = FluidVariant.fromNbt(tag.getCompound("variant"));
        if (tag.contains("amount"))
            fluidStorage.amount = tag.getLong("amount");

         */
    }

    // ----

    // 基準の速度
    public double getBasicSpeed() {
        return 5;
    }

    // クールダウンの基準
    public double getSettingCoolTime() {
        return 300;
    }

    public double coolTime = getSettingCoolTime();

    public void tick(World world, BlockPos pos, BlockState state, BaseEnergyTile blockEntity) {
        super.tick(world, pos, state, blockEntity);
        if (world.isClient()) return;

        // レッドストーン受信で無効
        if (WorldUtil.isReceivingRedstonePower(getWorld(), getPos())) {
            if (isActive())
                Quarry.setActive(false, getWorld(), getPos());
            return;
        }

        if (getEnergy() > getEnergyCost()) {
            // ここに処理を記入
            if (coolTime <= 0) {
                coolTime = getSettingCoolTime();
                if (tryQuarrying()) {
                    useEnergy(getEnergyCost());
                }
            }
            // エネルギーが多いほどクールダウンが早くなる
            coolTimeBonus();
            coolTime = coolTime - getBasicSpeed();
            if (!isActive()) {
                Quarry.setActive(true, getWorld(), getPos());
            }
            if (getWorld().getTime() % 2L == 0L)
                insertChest();
        } else if (isActive()) {
            Quarry.setActive(false, getWorld(), getPos());
        }
    }

    public int limit = 5;

    public void insertChest() {
        // チェスト自動挿入
        if (getItems().isEmpty()) return;
        List<Direction> dirs = getDirsOfAnyContainerBlock();
        if (!dirs.isEmpty()) {
            int time = 0;
            for (int i = 0; i < getItems().size(); i++) {
                if (time > limit) break;
                for (Direction dir : dirs) {
                    ItemStack stack = getItems().get(i);
                    if (stack.isEmpty()) continue;

                    long amount = StorageUtil.move(InventoryStorage.of(this, null).getSlot(i), ItemStorage.SIDED.find(getWorld(), getPos().offset(dir), dir.getOpposite()), (iv) -> true, Long.MAX_VALUE, null);
                    if (amount < stack.getCount()) continue;
                    ++time;
                    break;
                }
            }
        }
    }

    public List<Direction> getDirsOfAnyContainerBlock() {
        Direction[] dirs = new Direction[]{Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};
        List<Direction> usableDirs = new ArrayList<>();

        for (Direction dir : dirs) {
            BlockPos pos = getPos().offset(dir);
            if (getWorld().getBlockEntity(pos) == null) continue;
            usableDirs.add(dir);
        }
        return usableDirs;
    }

    // クールダウンのエネルギー量による追加ボーナス
    public void coolTimeBonus() {
        if (getMaxEnergy() / 1.125 < getEnergy()) {
            coolTime = coolTime - getBasicSpeed() * 5;
        }
        if (getMaxEnergy() / 1.25 < getEnergy()) {
            coolTime = coolTime - getBasicSpeed() * 3;
        }
        if (getMaxEnergy() / 3 < getEnergy()) {
            coolTime = coolTime - getBasicSpeed() * 2;
        }
        if (getMaxEnergy() / 5 < getEnergy()) {
            coolTime = coolTime - getBasicSpeed();
        }
        if (getMaxEnergy() / 7 < getEnergy()) {
            coolTime = coolTime - getBasicSpeed();
        }
        if (getMaxEnergy() / 10 < getEnergy()) {
            coolTime = coolTime - getBasicSpeed();
        }
        if (getMaxEnergy() / 12 < getEnergy()) {
            coolTime = coolTime - getBasicSpeed();
        }
        if (getMaxEnergy() / 15 < getEnergy()) {
            coolTime = coolTime - getBasicSpeed();
        }
        if (getMaxEnergy() / 16 < getEnergy()) {
            coolTime = coolTime - getBasicSpeed();
        }
        if (getMaxEnergy() / 20 < getEnergy()) {
            coolTime = coolTime - getBasicSpeed();
        }
        if (getMaxEnergy() / 30 < getEnergy()) {
            coolTime = coolTime - getBasicSpeed();
        }
        if (getMaxEnergy() / 40 < getEnergy()) {
            coolTime = coolTime - getBasicSpeed();
        }
    }

    public BlockPos getDefaultRangePos1() {
        // default
        switch (getFacing()) {
            case NORTH:
                return new BlockPos(getPos().getX() - 5, getPos().getY(), getPos().getZ() + 1);
            case SOUTH:
                return new BlockPos(getPos().getX() - 5, getPos().getY(), getPos().getZ() - 11);
            case WEST:
                return new BlockPos(getPos().getX() + 1, getPos().getY(), getPos().getZ() - 5);
            case EAST:
                return new BlockPos(getPos().getX() - 11, getPos().getY(), getPos().getZ() - 5);
            default:
                return null;
        }
    }

    public BlockPos getDefaultRangePos2() {
        // default
        switch (getFacing()) {
            case NORTH:
                return new BlockPos(getPos().getX() + 6, getPos().getY() + 4, getPos().getZ() + 12);
            case SOUTH:
                return new BlockPos(getPos().getX() + 6, getPos().getY() + 4, getPos().getZ());

            case WEST:
                return new BlockPos(getPos().getX() + 12, getPos().getY() + 4, getPos().getZ() + 6);
            case EAST:
                return new BlockPos(getPos().getX(), getPos().getY() + 4, getPos().getZ() + 6);
            default:
                return null;
        }
    }

    // マーカーによる範囲指定を許可するか？
    public boolean canSetPosByMarker() {
        return true;
    }

    private BlockPos pos1 = null;
    private BlockPos pos2 = null;

    public BlockPos getPos1() {
        return pos1;
    }

    public BlockPos getPos2() {
        return pos2;
    }

    public void setPos1(BlockPos pos1) {
        this.pos1 = pos1;
    }

    public void setPos2(BlockPos pos2) {
        this.pos2 = pos2;
    }

    public void breakBlock(BlockPos pos, boolean drop) {
        if (getWorld().isClient()) return;

        if (isSetSilkTouch || isSetLuck) {
            if (drop) {
                List<ItemStack> drops = Block.getDroppedStacks(getWorld().getBlockState(pos), (ServerWorld) getWorld(), pos, getWorld().getBlockEntity(pos), null, getQuarryStack());
                drops.forEach(this::addStack);
            }
            WorldUtil.breakBlock(getWorld(), pos, false);
        } else {
            WorldUtil.breakBlock(getWorld(), pos, drop);
        }
    }

    private ItemStack getQuarryStack() {
        ItemStack stack = new ItemStack(Items.DIAMOND_PICKAXE);
        if (isSetSilkTouch)
            stack.addEnchantment(Enchantments.SILK_TOUCH, 3);
        if (isSetLuck)
            stack.addEnchantment(Enchantments.FORTUNE, 3);
        return stack;
    }

    public void tryDeleteMob(BlockPos blockPos) {
        if (getWorld() == null || getWorld().isClient()) return;
        List<MobEntity> mobs = getWorld().getEntitiesByClass(MobEntity.class, BoxUtil.createBox(blockPos.add(-2, -2, -2), blockPos.add(2, 2, 2)), EntityPredicates.VALID_ENTITY);
        mobs.forEach(Entity::discard);
    }

    public void tryKillMob(BlockPos blockPos) {
        if (getWorld() == null || getWorld().isClient()) return;
        List<MobEntity> mobs = getWorld().getEntitiesByClass(MobEntity.class, BoxUtil.createBox(blockPos.add(-2, -2, -2), blockPos.add(2, 2, 2)), EntityPredicates.VALID_ENTITY);
        mobs.forEach(LivingEntity::kill);
    }

    public void tryCollectExp(BlockPos blockPos) {
        if (getWorld() == null || getWorld().isClient()) return;
        List<ExperienceOrbEntity> entities = getWorld().getEntitiesByClass(ExperienceOrbEntity.class, BoxUtil.createBox(blockPos.add(-2, -2, -2), blockPos.add(2, 2, 2)), EntityPredicates.VALID_ENTITY);
        entities.forEach((entity) -> {
            if (getStoredExp() + entity.getExperienceAmount() > getMaxStoredExp()) return;
            addStoredExp(entity.getExperienceAmount());
            entity.discard();
        });
    }

    public double tryFluidReplace(BlockPos blockPos) {
        double time = 0;
        if(getEnergy() < getReplaceFluidEnergyCost()) {
            return 0;
        }

        for (Direction value : Direction.values()) {
            BlockPos offsetBlockPos = blockPos.offset(value);

            if (getWorld().getBlockState(offsetBlockPos).getBlock() instanceof FluidBlock) {
                // replace fluid block
                getWorld().setBlockState(offsetBlockPos, getReplaceFluidWithBlock().getDefaultState());
                time += 0.1;
            }

            if(!getWorld().getFluidState(offsetBlockPos).isEmpty() || getWorld().getFluidState(offsetBlockPos).isStill()) {
                breakBlock(offsetBlockPos, true);

                List<ItemEntity> entities = getWorld().getEntitiesByType(EntityType.ITEM, BoxUtil.createBox(blockPos.add(-1, -1, -1), blockPos.add( 1,  1, 1)), EntityPredicates.VALID_ENTITY);
                for(ItemEntity itemEntity : entities) {
                    addStack(itemEntity.getStack());
                    itemEntity.kill();
                }

                getWorld().setBlockState(offsetBlockPos, getReplaceFluidWithBlock().getDefaultState());
                time += 0.1;
            }
        }

        return time;
    }

    public boolean tryPlaceFrame(BlockPos blockPos) {
        if (getEnergy() > getPlaceFrameEnergyCost()) {
            Block block = getWorld().getBlockState(blockPos).getBlock();
            if (block instanceof Frame) return false;
            if (
                    ((blockPos.getX() == pos1.getX()
                    || blockPos.getZ() == pos1.getZ()
                    || blockPos.getX() + 1 == pos2.getX()
                    || blockPos.getZ() + 1 == pos2.getZ())
                            && (blockPos.getY() == pos1.getY()
                            || blockPos.getY() == pos2.getY())
                    )
                    ||
                    (
                            (blockPos.getX() == pos1.getX() && blockPos.getZ() == pos1.getZ())
                                    || (blockPos.getX() + 1 == pos2.getX() && blockPos.getZ() + 1 == pos2.getZ())
                                    || (blockPos.getX() == pos1.getX() && blockPos.getZ() + 1 == pos2.getZ())
                                    || (blockPos.getX() + 1 == pos2.getX() && blockPos.getZ() == pos1.getZ())
                    )
            ) {
                getWorld().setBlockState(blockPos, Frame.getPlacementStateByTile(getWorld(), blockPos));
                return true;
            }

        }
        return false;
    }

    public boolean tryQuarrying() {
        if (getWorld() == null || getWorld().isClient()) return false;
        if (pos1 == null)
            pos1 = getDefaultRangePos1();
        if (pos2 == null)
            pos2 = getDefaultRangePos2();
        int procX;
        int procY;
        int procZ;
        for (procY = pos2.getY(); procY > WorldUtil.getBottomY(getWorld()); procY--) {
            if (pos1.getY() - 1 >= procY) {
                for (procX = pos1.getX() + 1; procX < pos2.getX() - 1; procX++) {
                    for (procZ = pos1.getZ() + 1; procZ < pos2.getZ() - 1; procZ++) {
                        BlockPos procPos = new BlockPos(procX, procY, procZ);
                        if (getWorld().getBlockState(procPos) == null) continue;
                        if (getWorld().getBlockEntity(procPos) instanceof QuarryTile && getWorld().getBlockEntity(procPos) == this) continue;

                        Block procBlock = getWorld().getBlockState(procPos).getBlock();
                        if (procBlock instanceof AirBlock || (procBlock.equals(Blocks.BEDROCK) && !canBedrockBreak)) {
                            if (canReplaceFluid()) {
                                double time = tryFluidReplace(procPos);
                                if (time != 0) {
                                    useEnergy((long) time * getReplaceFluidEnergyCost());
                                }
                            }
                            continue;
                        }
                        if (pos1.getY() - 1 >= procY) {
                            if ( procBlock instanceof FluidBlock) {
                                if (canReplaceFluid()
                                        && getWorld().getFluidState(procPos).isStill()
                                        && getEnergy() > getReplaceFluidEnergyCost()) {
                                    getWorld().setBlockState(procPos, BlockStateUtil.getDefaultState(getReplaceFluidWithBlock()));
                                    useEnergy(getReplaceFluidEnergyCost());
                                } else {
                                    continue;
                                }
                            }
                            if (canReplaceFluid()) {
                                double time = tryFluidReplace(procPos);
                                if (time != 0) {
                                    useEnergy((long) time * getReplaceFluidEnergyCost());
                                }
                            }
                            if (isSetMobDelete) {
                                tryDeleteMob(procPos);
                            }
                            if (isSetMobKill) {
                                tryKillMob(procPos);
                            }
                            if (isSetExpCollect) {
                                tryCollectExp(procPos);
                            }
                            breakBlock(procPos, true);
                            List<ItemEntity> entities = getWorld().getEntitiesByType(EntityType.ITEM, BoxUtil.createBox(new BlockPos(procX - 1, procY - 1, procZ - 1), new BlockPos(procX + 1, procY + 1, procZ + 1)), EntityPredicates.VALID_ENTITY);
                            if (entities.isEmpty()) return true;
                            for(ItemEntity itemEntity : entities) {
                                addStack(itemEntity.getStack());
                                itemEntity.kill();
                            }
                            return true;
                        }
                    }
                }
            }
            else if (pos1.getY() <= procY && pos2.getY() >= procY) {
                // procX < pos2.getX()を=<するとposのずれ問題は修正可能だが、別の方法で対処しているので、時間があればこっちで修正したい。
                for (procX = pos1.getX(); procX < pos2.getX(); procX++) {
                    for (procZ = pos1.getZ(); procZ < pos2.getZ(); procZ++) {
                        BlockPos procPos = new BlockPos(procX, procY, procZ);
                        if (getWorld().getBlockState(procPos) == null) continue;
                        if (getWorld().getBlockEntity(procPos) instanceof QuarryTile && getWorld().getBlockEntity(procPos) == this) continue;

                        Block procBlock = getWorld().getBlockState(procPos).getBlock();
                        if (procBlock instanceof AirBlock || (procBlock.equals(Blocks.BEDROCK) && !canBedrockBreak)) {
                            if (tryPlaceFrame(procPos)) {
                                useEnergy(getPlaceFrameEnergyCost());
                                return false;
                            }
                            if (canReplaceFluid()) {
                                double time = tryFluidReplace(procPos);
                                if (time != 0) {
                                    useEnergy((long) time * getReplaceFluidEnergyCost());
                                }
                            }
                            continue;
                        }
                        if (canReplaceFluid()) {
                            double time = tryFluidReplace(procPos);
                            if (time != 0) {
                                useEnergy((long) time * getReplaceFluidEnergyCost());
                            }
                        }
                        if (isSetMobDelete) {
                            tryDeleteMob(procPos);
                        }
                        if (isSetMobKill) {
                            tryKillMob(procPos);
                        }
                        if (isSetExpCollect) {
                            tryCollectExp(procPos);
                        }
                        if (procBlock instanceof FluidBlock && !getWorld().getFluidState(procPos).isStill()) {
                            continue;
                        }
                        if (procBlock instanceof Frame) continue;
                        breakBlock(procPos, false);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public Integer getEmptyOrCanInsertIndex(Inventory inventory, ItemStack stack) {
        int index = 0;
        for (; index < getItems().size(); index++) {
            if (inventory.getStack(index).isEmpty()) {
                return index;
            }
            ItemStack inStack = getItems().get(index);
            if (stack.getItem().equals(inStack.getItem()) && (ItemStackUtil.areNbtOrComponentEqual(stack, inStack) || !ItemStackUtil.hasNbtOrComponent(stack) == !ItemStackUtil.hasNbtOrComponent(inStack)) && inStack.getItem().getMaxCount() != 1) {
                int originInCount = getItems().get(index).getCount();
                getItems().get(index).setCount(Math.min(stack.getMaxCount(), stack.getCount() + originInCount));
                if (stack.getMaxCount() >= stack.getCount() + originInCount) {
                    return index;
                }
            }
        }

        return null;
    }

    public void addStack(ItemStack stack) {
        if (getWorld() == null || getWorld().isClient()) return;
        int index = 0;
        for (; index < getItems().size(); index++) {
            if (stack.isEmpty() || stack.getCount() == 0) return;
            if (getItems().get(index).isEmpty()) {
                getItems().set(index, stack);
                return;
            }
            ItemStack inStack = getItems().get(index);
            if (stack.getItem().equals(inStack.getItem()) && (ItemStackUtil.areNbtOrComponentEqual(stack, inStack) || !ItemStackUtil.hasNbtOrComponent(stack) == !ItemStackUtil.hasNbtOrComponent(inStack)) && inStack.getItem().getMaxCount() != 1) {
                int originInCount = getItems().get(index).getCount();
                getItems().get(index).setCount(Math.min(stack.getMaxCount(), stack.getCount() + originInCount));
                if (stack.getMaxCount() >= stack.getCount() + originInCount) {
                    return;
                }
                stack.setCount(stack.getCount() + originInCount - stack.getMaxCount());
            }
        }
        getWorld().spawnEntity(new ItemEntity(getWorld(), getPos().getX(), getPos().getY(), getPos().getZ(), stack));
    }

    public QuarryTile(BlockEntityType<?> type, TileCreateEvent event) {
        super(type, event);
    }

    public void init() {

    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return invItems;
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        int[] result = new int[getItems().size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = i;
        }
        return result;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return false;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return true;
    }
}
