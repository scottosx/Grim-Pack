package com.grim3212.mc.tools.entity;

import java.util.List;

import com.grim3212.mc.core.entity.EntityProjectile;
import com.grim3212.mc.tools.items.ToolsItems;
import com.grim3212.mc.tools.util.EnumSpearType;

import net.minecraft.block.Block;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.network.play.server.S2BPacketChangeGameState;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class EntitySpear extends EntityProjectile {

	protected EnumSpearType type;

	public EntitySpear(World worldIn) {
		super(worldIn);
	}

	public EntitySpear(World world, double x, double y, double z, EnumSpearType type) {
		super(world, x, y, z);
		this.type = type;
		setDamage(this.type.getDamage());
	}

	public EntitySpear(World world, EntityLivingBase shooter, EntityLivingBase entityliving, float par4, float par5, EnumSpearType type) {
		super(world, shooter, entityliving, par4, par5);
		this.type = type;
		setDamage(this.type.getDamage());
	}

	public EntitySpear(World world, EntityLivingBase shooter, float par3, EnumSpearType type) {
		super(world, shooter, par3);
		this.type = type;
		setDamage(this.type.getDamage());
	}

	@Override
	public Item getItemPickup() {
		switch (type) {
		case STONE:
			return ToolsItems.spear;
		case IRON:
			return ToolsItems.iron_spear;
		case DIAMOND:
			return ToolsItems.diamond_spear;
		case EXPLOSIVE:
			return ToolsItems.iron_spear;
		case FIRE:
			return ToolsItems.spear;
		case SLIME:
			return ToolsItems.slime_spear;
		case LIGHT:
			return ToolsItems.spear;
		case LIGHTNING:
			return ToolsItems.iron_spear;
		default:
			return ToolsItems.spear;
		}
	}

	@Override
	public void onUpdate() {
		super.onEntityUpdate();

		if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
			float f = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
			this.prevRotationYaw = this.rotationYaw = (float) (Math.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI);
			this.prevRotationPitch = this.rotationPitch = (float) (Math.atan2(this.motionY, (double) f) * 180.0D / Math.PI);
		}

		BlockPos blockpos = new BlockPos(this.newXTile, this.newYTile, this.newZTile);
		IBlockState iblockstate = this.worldObj.getBlockState(blockpos);
		Block block = iblockstate.getBlock();

		if (block.getMaterial() != Material.air) {
			block.setBlockBoundsBasedOnState(this.worldObj, blockpos);
			AxisAlignedBB axisalignedbb = block.getCollisionBoundingBox(this.worldObj, blockpos, iblockstate);

			if (axisalignedbb != null && axisalignedbb.isVecInside(new Vec3(this.posX, this.posY, this.posZ))) {
				this.newInGround = true;
			}
		}

		if (this.arrowShake > 0) {
			--this.arrowShake;
		}

		if (this.newInGround) {
			int j = block.getMetaFromState(iblockstate);

			if (block == this.newInTile && j == this.newInData) {
				++this.numTicksInGround;

				if (this.numTicksInGround >= 1200) {
					this.setDead();
				}
			} else {
				this.newInGround = false;
				this.motionX *= (double) (this.rand.nextFloat() * 0.2F);
				this.motionY *= (double) (this.rand.nextFloat() * 0.2F);
				this.motionZ *= (double) (this.rand.nextFloat() * 0.2F);
				this.numTicksInGround = 0;
				this.numTicksInAir = 0;
			}
		} else {
			++this.numTicksInAir;
			Vec3 vec31 = new Vec3(this.posX, this.posY, this.posZ);
			Vec3 vec3 = new Vec3(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
			MovingObjectPosition movingobjectposition = this.worldObj.rayTraceBlocks(vec31, vec3, false, true, false);
			vec31 = new Vec3(this.posX, this.posY, this.posZ);
			vec3 = new Vec3(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);

			if (movingobjectposition != null) {
				vec3 = new Vec3(movingobjectposition.hitVec.xCoord, movingobjectposition.hitVec.yCoord, movingobjectposition.hitVec.zCoord);
			}

			Entity entity = null;
			List<Entity> list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().addCoord(this.motionX, this.motionY, this.motionZ).expand(1.0D, 1.0D, 1.0D));
			double d0 = 0.0D;
			int i;
			float f1;

			for (i = 0; i < list.size(); ++i) {
				Entity entity1 = (Entity) list.get(i);

				if (entity1.canBeCollidedWith() && (entity1 != this.shootingEntity || this.numTicksInAir >= 5)) {
					f1 = 0.3F;
					AxisAlignedBB axisalignedbb1 = entity1.getEntityBoundingBox().expand((double) f1, (double) f1, (double) f1);
					MovingObjectPosition movingobjectposition1 = axisalignedbb1.calculateIntercept(vec31, vec3);

					if (movingobjectposition1 != null) {
						double d1 = vec31.distanceTo(movingobjectposition1.hitVec);

						if (d1 < d0 || d0 == 0.0D) {
							entity = entity1;
							d0 = d1;
						}
					}
				}
			}

			if (entity != null) {
				movingobjectposition = new MovingObjectPosition(entity);
			}

			if (movingobjectposition != null && movingobjectposition.entityHit != null && movingobjectposition.entityHit instanceof EntityPlayer) {
				EntityPlayer entityplayer = (EntityPlayer) movingobjectposition.entityHit;

				if (entityplayer.capabilities.disableDamage || this.shootingEntity instanceof EntityPlayer && !((EntityPlayer) this.shootingEntity).canAttackPlayer(entityplayer)) {
					movingobjectposition = null;
				}
			}

			float f2;
			float f3;
			float f4;

			if (movingobjectposition != null) {
				if (movingobjectposition.entityHit != null) {
					f2 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
					int k = MathHelper.ceiling_double_int((double) f2 * this.throwableDamage);

					if (this.getIsCritical()) {
						k += this.rand.nextInt(k / 2 + 2);
					}

					DamageSource damagesource;

					if (this.shootingEntity == null) {
						damagesource = DamageSource.causeThrownDamage(this, this);
					} else {
						damagesource = DamageSource.causeThrownDamage(this, this.shootingEntity);
					}

					if (this.isBurning() && !(movingobjectposition.entityHit instanceof EntityEnderman)) {
						movingobjectposition.entityHit.setFire(5);
					}

					if (movingobjectposition.entityHit.attackEntityFrom(damagesource, (float) k)) {

						if (movingobjectposition.entityHit instanceof EntityLivingBase) {
							EntityLivingBase entitylivingbase = (EntityLivingBase) movingobjectposition.entityHit;

							if (this.shootingEntity instanceof EntityLivingBase) {
								EnchantmentHelper.applyThornEnchantments(entitylivingbase, this.shootingEntity);
								EnchantmentHelper.applyArthropodEnchantments((EntityLivingBase) this.shootingEntity, entitylivingbase);
							}

							if (this.shootingEntity != null && movingobjectposition.entityHit != this.shootingEntity && movingobjectposition.entityHit instanceof EntityPlayer && this.shootingEntity instanceof EntityPlayerMP) {
								((EntityPlayerMP) this.shootingEntity).playerNetServerHandler.sendPacket(new S2BPacketChangeGameState(6, 0.0F));
							}
						}

						this.playSound("random.bowhit", 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
						if (this.type == EnumSpearType.EXPLOSIVE) {
							if (!worldObj.isRemote)
								worldObj.createExplosion(null, posX, posY, posZ, 3F, true);
							setDead();
						}else if (this.type == EnumSpearType.LIGHTNING) {
							EntityLightningBolt entitylightningbolt1 = new EntityLightningBolt(worldObj, 1.0D, 1.0D, 1.0D);
							entitylightningbolt1.setLocationAndAngles(posX, posY, posZ, rotationYaw, 0.0F);
							worldObj.addWeatherEffect(entitylightningbolt1);
							this.newInGround = true;
						} else if (this.type == EnumSpearType.FIRE) {
							for (int fire = 0; fire < 6; ++fire) {
								BlockPos blockPos = this.getPosition().add(this.rand.nextInt(3) - 1, this.rand.nextInt(3) - 1, this.rand.nextInt(3) - 1);

								if (worldObj.getBlockState(blockPos).getBlock().getMaterial() == Material.air && Blocks.fire.canPlaceBlockAt(worldObj, blockPos)) {
									worldObj.setBlockState(blockPos, Blocks.fire.getDefaultState());
								}
							}
							setDead();
						}

						if (!(movingobjectposition.entityHit instanceof EntityEnderman)) {
							this.setDead();
						}
					} else {
						this.motionX *= -0.10000000149011612D;
						this.motionY *= -0.10000000149011612D;
						this.motionZ *= -0.10000000149011612D;
						this.rotationYaw += 180.0F;
						this.prevRotationYaw += 180.0F;
						this.numTicksInAir = 0;
					}
				} else {
					BlockPos blockpos1 = movingobjectposition.getBlockPos();
					this.newXTile = blockpos1.getX();
					this.newYTile = blockpos1.getY();
					this.newZTile = blockpos1.getZ();
					iblockstate = this.worldObj.getBlockState(blockpos1);
					this.newInTile = iblockstate.getBlock();
					this.newInData = this.newInTile.getMetaFromState(iblockstate);
					this.motionX = (double) ((float) (movingobjectposition.hitVec.xCoord - this.posX));
					this.motionY = (double) ((float) (movingobjectposition.hitVec.yCoord - this.posY));
					this.motionZ = (double) ((float) (movingobjectposition.hitVec.zCoord - this.posZ));
					f3 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
					this.posX -= this.motionX / (double) f3 * 0.05000000074505806D;
					this.posY -= this.motionY / (double) f3 * 0.05000000074505806D;
					this.posZ -= this.motionZ / (double) f3 * 0.05000000074505806D;
					this.playSound("random.bowhit", 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
					if (this.type == EnumSpearType.LIGHT) {
						if (movingobjectposition.sideHit == EnumFacing.UP) {
							if (!worldObj.isRemote && numTicksInGround == 0 && newInTile != Blocks.air && this.getEntityWorld().getBlockState(movingobjectposition.getBlockPos().up(2)).getBlock() == Blocks.air) {
								worldObj.setBlockState(movingobjectposition.getBlockPos().up(), Blocks.torch.getDefaultState());
							}
						} else {
							if (!worldObj.isRemote && numTicksInGround == 0 && newInTile != Blocks.air && this.getEntityWorld().getBlockState(movingobjectposition.getBlockPos().offset(movingobjectposition.sideHit, 2)).getBlock() == Blocks.air) {
								worldObj.setBlockState(movingobjectposition.getBlockPos().offset(movingobjectposition.sideHit), Blocks.torch.getDefaultState().withProperty(BlockTorch.FACING, movingobjectposition.sideHit));
							}
						}
						this.newInGround = true;
					} else if (this.type == EnumSpearType.LIGHTNING) {
						EntityLightningBolt entitylightningbolt1 = new EntityLightningBolt(worldObj, 1.0D, 1.0D, 1.0D);
						entitylightningbolt1.setLocationAndAngles(posX, posY, posZ, rotationYaw, 0.0F);
						worldObj.addWeatherEffect(entitylightningbolt1);
						this.newInGround = true;
					} else if (this.type == EnumSpearType.FIRE) {
						for (int fire = 0; fire < 6; ++fire) {
							BlockPos blockPos = this.getPosition().add(this.rand.nextInt(3) - 1, this.rand.nextInt(3) - 1, this.rand.nextInt(3) - 1);

							if (worldObj.getBlockState(blockPos).getBlock().getMaterial() == Material.air && Blocks.fire.canPlaceBlockAt(worldObj, blockPos)) {
								worldObj.setBlockState(blockPos, Blocks.fire.getDefaultState());
							}
						}
						setDead();
					} else if (this.type == EnumSpearType.EXPLOSIVE) {
						if (!worldObj.isRemote)
							worldObj.createExplosion(null, posX, posY, posZ, 3F, true);
						setDead();
					} else {
						this.newInGround = true;
					}

					this.arrowShake = 7;
					this.setIsCritical(false);

					if (this.newInTile.getMaterial() != Material.air) {
						this.newInTile.onEntityCollidedWithBlock(this.worldObj, blockpos1, iblockstate, this);
					}
				}
			}

			this.posX += this.motionX;
			this.posY += this.motionY;
			this.posZ += this.motionZ;
			f2 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
			this.rotationYaw = (float) (Math.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI);

			for (this.rotationPitch = (float) (Math.atan2(this.motionY, (double) f2) * 180.0D / Math.PI); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F) {
				;
			}

			while (this.rotationPitch - this.prevRotationPitch >= 180.0F) {
				this.prevRotationPitch += 360.0F;
			}

			while (this.rotationYaw - this.prevRotationYaw < -180.0F) {
				this.prevRotationYaw -= 360.0F;
			}

			while (this.rotationYaw - this.prevRotationYaw >= 180.0F) {
				this.prevRotationYaw += 360.0F;
			}

			this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
			this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;
			f3 = 0.99F;
			f1 = 0.05F;

			if (this.isInWater()) {
				for (int l = 0; l < 4; ++l) {
					f4 = 0.25F;
					this.worldObj.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX - this.motionX * (double) f4, this.posY - this.motionY * (double) f4, this.posZ - this.motionZ * (double) f4, this.motionX, this.motionY, this.motionZ, new int[0]);
				}

				f3 = 0.6F;
			}

			if (this.isWet()) {
				this.extinguish();
			}

			this.motionX *= (double) f3;
			this.motionY *= (double) f3;
			this.motionZ *= (double) f3;
			this.motionY -= (double) f1;
			this.setPosition(this.posX, this.posY, this.posZ);
			this.doBlockCollisions();
		}
	}
}