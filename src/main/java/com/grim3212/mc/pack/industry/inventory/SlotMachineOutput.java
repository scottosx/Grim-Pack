package com.grim3212.mc.pack.industry.inventory;

import javax.annotation.Nullable;

import com.grim3212.mc.pack.industry.util.MachineRecipes;
import com.grim3212.mc.pack.industry.util.MachineRecipes.MachineType;

import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.AchievementList;
import net.minecraft.util.math.MathHelper;

public class SlotMachineOutput extends Slot {

	private final EntityPlayer thePlayer;
	private int removeCount;
	private MachineType type;

	public SlotMachineOutput(EntityPlayer player, IInventory inventoryIn, int slotIndex, int xPosition, int yPosition, MachineType type) {
		super(inventoryIn, slotIndex, xPosition, yPosition);
		this.thePlayer = player;
		this.type = type;
	}

	@Override
	public boolean isItemValid(@Nullable ItemStack stack) {
		return false;
	}

	@Override
	public ItemStack decrStackSize(int amount) {
		if (this.getHasStack()) {
			this.removeCount += Math.min(amount, this.getStack().stackSize);
		}

		return super.decrStackSize(amount);
	}

	@Override
	public void onPickupFromSlot(EntityPlayer playerIn, ItemStack stack) {
		this.onCrafting(stack);
		super.onPickupFromSlot(playerIn, stack);
	}

	@Override
	protected void onCrafting(ItemStack stack, int amount) {
		this.removeCount += amount;
		this.onCrafting(stack);
	}

	@Override
	protected void onCrafting(ItemStack stack) {
		stack.onCrafting(this.thePlayer.worldObj, this.thePlayer, this.removeCount);

		if (!this.thePlayer.worldObj.isRemote) {
			int i = this.removeCount;
			float f = MachineRecipes.INSTANCE.getSmeltingExperience(stack, this.type);

			if (f == 0.0F) {
				i = 0;
			} else if (f < 1.0F) {
				int j = MathHelper.floor_float((float) i * f);

				if (j < MathHelper.ceiling_float_int((float) i * f) && Math.random() < (double) ((float) i * f - (float) j)) {
					++j;
				}

				i = j;
			}

			while (i > 0) {
				int k = EntityXPOrb.getXPSplit(i);
				i -= k;
				this.thePlayer.worldObj.spawnEntityInWorld(new EntityXPOrb(this.thePlayer.worldObj, this.thePlayer.posX, this.thePlayer.posY + 0.5D, this.thePlayer.posZ + 0.5D, k));
			}
		}

		this.removeCount = 0;

		if (this.type == MachineType.MODERN_FURNACE) {
			net.minecraftforge.fml.common.FMLCommonHandler.instance().firePlayerSmeltedEvent(thePlayer, stack);

			if (stack.getItem() == Items.IRON_INGOT) {
				this.thePlayer.addStat(AchievementList.ACQUIRE_IRON);
			}

			if (stack.getItem() == Items.COOKED_FISH) {
				this.thePlayer.addStat(AchievementList.COOK_FISH);
			}
		}
	}
}
