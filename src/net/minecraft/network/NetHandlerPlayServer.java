package net.minecraft.network;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import io.netty.buffer.Unpooled;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.block.material.Material;
import net.minecraft.command.server.CommandBlockLogic;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityMinecartCommandBlock;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemEditableBook;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemWritableBook;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.server.management.UserListBansEntry;
import net.minecraft.stats.AchievementList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.*;
import net.minecraft.world.WorldServer;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;

public class NetHandlerPlayServer implements INetHandlerPlayServer, IUpdatePlayerListBox {
    private static final Logger logger = LogManager.getLogger();
    public final NetworkManager netManager;
    private final MinecraftServer serverController;
    public EntityPlayerMP playerEntity;
    private int networkTickCount;
    private int field_175090_f;

    /**
     * Used to keep track of how the player is floating while gamerules should prevent that. Surpassing 80 ticks means
     * kick
     */
    private int floatingTickCount;
    private boolean field_147366_g;
    private int field_147378_h;
    private long lastPingTime;
    private long lastSentPingPacket;

    /**
     * Incremented by 20 each time a user sends a chat message, decreased by one every tick. Non-ops kicked when over
     * 200
     */
    private int chatSpamThresholdCount;
    private int itemDropThreshold;
    private IntHashMap field_147372_n = new IntHashMap();
    private double lastPosX;
    private double lastPosY;
    private double lastPosZ;
    private boolean hasMoved = true;

    public NetHandlerPlayServer(MinecraftServer server, NetworkManager networkManagerIn, EntityPlayerMP playerIn) {
        this.serverController = server;
        this.netManager = networkManagerIn;
        networkManagerIn.setNetHandler(this);
        this.playerEntity = playerIn;
        playerIn.playerNetServerHandler = this;
    }

    /**
     * Like the old updateEntity(), except more generic.
     */
    public void update() {
        this.field_147366_g = false;
        ++this.networkTickCount;
        this.serverController.theProfiler.startSection("keepAlive");

        if ((long) this.networkTickCount - this.lastSentPingPacket > 40L) {
            this.lastSentPingPacket = (long) this.networkTickCount;
            this.lastPingTime = this.currentTimeMillis();
            this.field_147378_h = (int) this.lastPingTime;
            this.sendPacket(new S00PacketKeepAlive(this.field_147378_h));
        }

        this.serverController.theProfiler.endSection();

        if (this.chatSpamThresholdCount > 0) {
            --this.chatSpamThresholdCount;
        }

        if (this.itemDropThreshold > 0) {
            --this.itemDropThreshold;
        }

        if (this.playerEntity.getLastActiveTime() > 0L && this.serverController.getMaxPlayerIdleMinutes() > 0 && MinecraftServer.getCurrentTimeMillis() - this.playerEntity.getLastActiveTime() > (long) (this.serverController.getMaxPlayerIdleMinutes() * 1000 * 60)) {
            this.kickPlayerFromServer("You have been idle for too long!");
        }
    }

    public NetworkManager getNetworkManager() {
        return this.netManager;
    }

    /**
     * Kick a player from the server with a reason
     */
    public void kickPlayerFromServer(String reason) {
        final ChatComponentText var2 = new ChatComponentText(reason);
        this.netManager.sendPacket(new S40PacketDisconnect(var2), new GenericFutureListener() {
            public void operationComplete(Future p_operationComplete_1_) {
                NetHandlerPlayServer.this.netManager.closeChannel(var2);
            }
        }, new GenericFutureListener[0]);
        this.netManager.disableAutoRead();
        Futures.getUnchecked(this.serverController.addScheduledTask(new Runnable() {
            public void run() {
                NetHandlerPlayServer.this.netManager.checkDisconnected();
            }
        }));
    }

    /**
     * Processes player movement input. Includes walking, strafing, jumping, sneaking; excludes riding and toggling
     * flying/sprinting
     */
    public void processInput(C0CPacketInput packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerForPlayer());
        this.playerEntity.setEntityActionState(packetIn.getStrafeSpeed(), packetIn.getForwardSpeed(), packetIn.isJumping(), packetIn.isSneaking());
    }

    /**
     * Processes clients perspective on player positioning and/or orientation
     */
    public void processPlayer(C03PacketPlayer packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerForPlayer());
        WorldServer var2 = this.serverController.worldServerForDimension(this.playerEntity.dimension);
        this.field_147366_g = true;

        if (!this.playerEntity.playerConqueredTheEnd) {
            double var3 = this.playerEntity.posX;
            double var5 = this.playerEntity.posY;
            double var7 = this.playerEntity.posZ;
            double var9 = 0.0D;
            double var11 = packetIn.getPositionX() - this.lastPosX;
            double var13 = packetIn.getPositionY() - this.lastPosY;
            double var15 = packetIn.getPositionZ() - this.lastPosZ;

            if (packetIn.isMoving()) {
                var9 = var11 * var11 + var13 * var13 + var15 * var15;

                if (!this.hasMoved && var9 < 0.25D) {
                    this.hasMoved = true;
                }
            }

            if (this.hasMoved) {
                this.field_175090_f = this.networkTickCount;
                double var19;
                double var21;
                double var23;

                if (this.playerEntity.ridingEntity != null) {
                    float var47 = this.playerEntity.rotationYaw;
                    float var18 = this.playerEntity.rotationPitch;
                    this.playerEntity.ridingEntity.updateRiderPosition();
                    var19 = this.playerEntity.posX;
                    var21 = this.playerEntity.posY;
                    var23 = this.playerEntity.posZ;

                    if (packetIn.getRotating()) {
                        var47 = packetIn.getYaw();
                        var18 = packetIn.getPitch();
                    }

                    this.playerEntity.onGround = packetIn.isOnGround();
                    this.playerEntity.onUpdateEntity();
                    this.playerEntity.setPositionAndRotation(var19, var21, var23, var47, var18);

                    if (this.playerEntity.ridingEntity != null) {
                        this.playerEntity.ridingEntity.updateRiderPosition();
                    }

                    this.serverController.getConfigurationManager().serverUpdateMountedMovingPlayer(this.playerEntity);

                    if (this.playerEntity.ridingEntity != null) {
                        if (var9 > 4.0D) {
                            Entity var48 = this.playerEntity.ridingEntity;
                            this.playerEntity.playerNetServerHandler.sendPacket(new S18PacketEntityTeleport(var48));
                            this.setPlayerLocation(this.playerEntity.posX, this.playerEntity.posY, this.playerEntity.posZ, this.playerEntity.rotationYaw, this.playerEntity.rotationPitch);
                        }

                        this.playerEntity.ridingEntity.isAirBorne = true;
                    }

                    if (this.hasMoved) {
                        this.lastPosX = this.playerEntity.posX;
                        this.lastPosY = this.playerEntity.posY;
                        this.lastPosZ = this.playerEntity.posZ;
                    }

                    var2.updateEntity(this.playerEntity);
                    return;
                }

                if (this.playerEntity.isPlayerSleeping()) {
                    this.playerEntity.onUpdateEntity();
                    this.playerEntity.setPositionAndRotation(this.lastPosX, this.lastPosY, this.lastPosZ, this.playerEntity.rotationYaw, this.playerEntity.rotationPitch);
                    var2.updateEntity(this.playerEntity);
                    return;
                }

                double var17 = this.playerEntity.posY;
                this.lastPosX = this.playerEntity.posX;
                this.lastPosY = this.playerEntity.posY;
                this.lastPosZ = this.playerEntity.posZ;
                var19 = this.playerEntity.posX;
                var21 = this.playerEntity.posY;
                var23 = this.playerEntity.posZ;
                float var25 = this.playerEntity.rotationYaw;
                float var26 = this.playerEntity.rotationPitch;

                if (packetIn.isMoving() && packetIn.getPositionY() == -999.0D) {
                    packetIn.setMoving(false);
                }

                if (packetIn.isMoving()) {
                    var19 = packetIn.getPositionX();
                    var21 = packetIn.getPositionY();
                    var23 = packetIn.getPositionZ();

                    if (Math.abs(packetIn.getPositionX()) > 3.0E7D || Math.abs(packetIn.getPositionZ()) > 3.0E7D) {
                        this.kickPlayerFromServer("Illegal position");
                        return;
                    }
                }

                if (packetIn.getRotating()) {
                    var25 = packetIn.getYaw();
                    var26 = packetIn.getPitch();
                }

                this.playerEntity.onUpdateEntity();
                this.playerEntity.setPositionAndRotation(this.lastPosX, this.lastPosY, this.lastPosZ, var25, var26);

                if (!this.hasMoved) {
                    return;
                }

                double var27 = var19 - this.playerEntity.posX;
                double var29 = var21 - this.playerEntity.posY;
                double var31 = var23 - this.playerEntity.posZ;
                double var33 = Math.min(Math.abs(var27), Math.abs(this.playerEntity.motionX));
                double var35 = Math.min(Math.abs(var29), Math.abs(this.playerEntity.motionY));
                double var37 = Math.min(Math.abs(var31), Math.abs(this.playerEntity.motionZ));
                double var39 = var33 * var33 + var35 * var35 + var37 * var37;

                if (var39 > 100.0D && (!this.serverController.isSinglePlayer() || !this.serverController.getServerOwner().equals(this.playerEntity.getCommandSenderName()))) {
                    logger.warn(this.playerEntity.getCommandSenderName() + " moved too quickly! " + var27 + "," + var29 + "," + var31 + " (" + var33 + ", " + var35 + ", " + var37 + ")");
                    this.setPlayerLocation(this.lastPosX, this.lastPosY, this.lastPosZ, this.playerEntity.rotationYaw, this.playerEntity.rotationPitch);
                    return;
                }

                float var41 = 0.0625F;
                boolean var42 = var2.getCollidingBoundingBoxes(this.playerEntity, this.playerEntity.getEntityBoundingBox().contract((double) var41, (double) var41, (double) var41)).isEmpty();

                if (this.playerEntity.onGround && !packetIn.isOnGround() && var29 > 0.0D) {
                    this.playerEntity.jump();
                }

                this.playerEntity.moveEntity(var27, var29, var31);
                this.playerEntity.onGround = packetIn.isOnGround();
                double var43 = var29;
                var27 = var19 - this.playerEntity.posX;
                var29 = var21 - this.playerEntity.posY;

                if (var29 > -0.5D || var29 < 0.5D) {
                    var29 = 0.0D;
                }

                var31 = var23 - this.playerEntity.posZ;
                var39 = var27 * var27 + var29 * var29 + var31 * var31;
                boolean var45 = false;

                if (var39 > 0.0625D && !this.playerEntity.isPlayerSleeping() && !this.playerEntity.theItemInWorldManager.isCreative()) {
                    var45 = true;
                    logger.warn(this.playerEntity.getCommandSenderName() + " moved wrongly!");
                }

                this.playerEntity.setPositionAndRotation(var19, var21, var23, var25, var26);
                this.playerEntity.addMovementStat(this.playerEntity.posX - var3, this.playerEntity.posY - var5, this.playerEntity.posZ - var7);

                if (!this.playerEntity.noClip) {
                    boolean var46 = var2.getCollidingBoundingBoxes(this.playerEntity, this.playerEntity.getEntityBoundingBox().contract((double) var41, (double) var41, (double) var41)).isEmpty();

                    if (var42 && (var45 || !var46) && !this.playerEntity.isPlayerSleeping()) {
                        this.setPlayerLocation(this.lastPosX, this.lastPosY, this.lastPosZ, var25, var26);
                        return;
                    }
                }

                AxisAlignedBB var49 = this.playerEntity.getEntityBoundingBox().expand((double) var41, (double) var41, (double) var41).addCoord(0.0D, -0.55D, 0.0D);

                if (!this.serverController.isFlightAllowed() && !this.playerEntity.capabilities.allowFlying && !var2.checkBlockCollision(var49)) {
                    if (var43 >= -0.03125D) {
                        ++this.floatingTickCount;

                        if (this.floatingTickCount > 80) {
                            logger.warn(this.playerEntity.getCommandSenderName() + " was kicked for floating too long!");
                            this.kickPlayerFromServer("Flying is not enabled on this server");
                            return;
                        }
                    }
                } else {
                    this.floatingTickCount = 0;
                }

                this.playerEntity.onGround = packetIn.isOnGround();
                this.serverController.getConfigurationManager().serverUpdateMountedMovingPlayer(this.playerEntity);
                this.playerEntity.handleFalling(this.playerEntity.posY - var17, packetIn.isOnGround());
            } else if (this.networkTickCount - this.field_175090_f > 20) {
                this.setPlayerLocation(this.lastPosX, this.lastPosY, this.lastPosZ, this.playerEntity.rotationYaw, this.playerEntity.rotationPitch);
            }
        }
    }

    public void setPlayerLocation(double x, double y, double z, float yaw, float pitch) {
        this.setPlayerLocation(x, y, z, yaw, pitch, Collections.emptySet());
    }

    public void setPlayerLocation(double x, double y, double z, float yaw, float pitch, Set relativeSet) {
        this.hasMoved = false;
        this.lastPosX = x;
        this.lastPosY = y;
        this.lastPosZ = z;

        if (relativeSet.contains(S08PacketPlayerPosLook.EnumFlags.X)) {
            this.lastPosX += this.playerEntity.posX;
        }

        if (relativeSet.contains(S08PacketPlayerPosLook.EnumFlags.Y)) {
            this.lastPosY += this.playerEntity.posY;
        }

        if (relativeSet.contains(S08PacketPlayerPosLook.EnumFlags.Z)) {
            this.lastPosZ += this.playerEntity.posZ;
        }

        float var10 = yaw;
        float var11 = pitch;

        if (relativeSet.contains(S08PacketPlayerPosLook.EnumFlags.Y_ROT)) {
            var10 = yaw + this.playerEntity.rotationYaw;
        }

        if (relativeSet.contains(S08PacketPlayerPosLook.EnumFlags.X_ROT)) {
            var11 = pitch + this.playerEntity.rotationPitch;
        }

        this.playerEntity.setPositionAndRotation(this.lastPosX, this.lastPosY, this.lastPosZ, var10, var11);
        this.playerEntity.playerNetServerHandler.sendPacket(new S08PacketPlayerPosLook(x, y, z, yaw, pitch, relativeSet));
    }

    /**
     * Processes the player initiating/stopping digging on a particular spot, as well as a player dropping items?. (0:
     * initiated, 1: reinitiated, 2? , 3-4 drop item (respectively without or with player control), 5: stopped; x,y,z,
     * side clicked on;)
     */
    public void processPlayerDigging(C07PacketPlayerDigging packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerForPlayer());
        WorldServer var2 = this.serverController.worldServerForDimension(this.playerEntity.dimension);
        BlockPos var3 = packetIn.getPosition();
        this.playerEntity.markPlayerActive();

        switch (NetHandlerPlayServer.SwitchAction.field_180224_a[packetIn.getStatus().ordinal()]) {
            case 1:
                if (!this.playerEntity.isSpectator()) {
                    this.playerEntity.dropOneItem(false);
                }

                return;

            case 2:
                if (!this.playerEntity.isSpectator()) {
                    this.playerEntity.dropOneItem(true);
                }

                return;

            case 3:
                this.playerEntity.stopUsingItem();
                return;

            case 4:
            case 5:
            case 6:
                double var4 = this.playerEntity.posX - ((double) var3.getX() + 0.5D);
                double var6 = this.playerEntity.posY - ((double) var3.getY() + 0.5D) + 1.5D;
                double var8 = this.playerEntity.posZ - ((double) var3.getZ() + 0.5D);
                double var10 = var4 * var4 + var6 * var6 + var8 * var8;

                if (var10 > 36.0D) {
                    return;
                } else if (var3.getY() >= this.serverController.getBuildLimit()) {
                    return;
                } else {
                    if (packetIn.getStatus() == C07PacketPlayerDigging.Action.START_DESTROY_BLOCK) {
                        if (!this.serverController.isBlockProtected(var2, var3, this.playerEntity) && var2.getWorldBorder().contains(var3)) {
                            this.playerEntity.theItemInWorldManager.onBlockClicked(var3, packetIn.getFacing());
                        } else {
                            this.playerEntity.playerNetServerHandler.sendPacket(new S23PacketBlockChange(var2, var3));
                        }
                    } else {
                        if (packetIn.getStatus() == C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK) {
                            this.playerEntity.theItemInWorldManager.blockRemoving(var3);
                        } else if (packetIn.getStatus() == C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK) {
                            this.playerEntity.theItemInWorldManager.cancelDestroyingBlock();
                        }

                        if (var2.getBlockState(var3).getBlock().getMaterial() != Material.air) {
                            this.playerEntity.playerNetServerHandler.sendPacket(new S23PacketBlockChange(var2, var3));
                        }
                    }

                    return;
                }

            default:
                throw new IllegalArgumentException("Invalid player action");
        }
    }

    /**
     * Processes block placement and block activation (anvil, furnace, etc.)
     */
    public void processPlayerBlockPlacement(C08PacketPlayerBlockPlacement packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerForPlayer());
        WorldServer var2 = this.serverController.worldServerForDimension(this.playerEntity.dimension);
        ItemStack var3 = this.playerEntity.inventory.getCurrentItem();
        boolean var4 = false;
        BlockPos var5 = packetIn.getPosition();
        EnumFacing var6 = EnumFacing.getFront(packetIn.getPlacedBlockDirection());
        this.playerEntity.markPlayerActive();

        if (packetIn.getPlacedBlockDirection() == 255) {
            if (var3 == null) {
                return;
            }

            this.playerEntity.theItemInWorldManager.tryUseItem(this.playerEntity, var2, var3);
        } else if (var5.getY() >= this.serverController.getBuildLimit() - 1 && (var6 == EnumFacing.UP || var5.getY() >= this.serverController.getBuildLimit())) {
            ChatComponentTranslation var7 = new ChatComponentTranslation("build.tooHigh", new Object[]{Integer.valueOf(this.serverController.getBuildLimit())});
            var7.getChatStyle().setColor(EnumChatFormatting.RED);
            this.playerEntity.playerNetServerHandler.sendPacket(new S02PacketChat(var7));
            var4 = true;
        } else {
            if (this.hasMoved && this.playerEntity.getDistanceSq((double) var5.getX() + 0.5D, (double) var5.getY() + 0.5D, (double) var5.getZ() + 0.5D) < 64.0D && !this.serverController.isBlockProtected(var2, var5, this.playerEntity) && var2.getWorldBorder().contains(var5)) {
                this.playerEntity.theItemInWorldManager.activateBlockOrUseItem(this.playerEntity, var2, var3, var5, var6, packetIn.getPlacedBlockOffsetX(), packetIn.getPlacedBlockOffsetY(), packetIn.getPlacedBlockOffsetZ());
            }

            var4 = true;
        }

        if (var4) {
            this.playerEntity.playerNetServerHandler.sendPacket(new S23PacketBlockChange(var2, var5));
            this.playerEntity.playerNetServerHandler.sendPacket(new S23PacketBlockChange(var2, var5.offset(var6)));
        }

        var3 = this.playerEntity.inventory.getCurrentItem();

        if (var3 != null && var3.stackSize == 0) {
            this.playerEntity.inventory.mainInventory[this.playerEntity.inventory.currentItem] = null;
            var3 = null;
        }

        if (var3 == null || var3.getMaxItemUseDuration() == 0) {
            this.playerEntity.isChangingQuantityOnly = true;
            this.playerEntity.inventory.mainInventory[this.playerEntity.inventory.currentItem] = ItemStack.copyItemStack(this.playerEntity.inventory.mainInventory[this.playerEntity.inventory.currentItem]);
            Slot var8 = this.playerEntity.openContainer.getSlotFromInventory(this.playerEntity.inventory, this.playerEntity.inventory.currentItem);
            this.playerEntity.openContainer.detectAndSendChanges();
            this.playerEntity.isChangingQuantityOnly = false;

            if (!ItemStack.areItemStacksEqual(this.playerEntity.inventory.getCurrentItem(), packetIn.getStack())) {
                this.sendPacket(new S2FPacketSetSlot(this.playerEntity.openContainer.windowId, var8.slotNumber, this.playerEntity.inventory.getCurrentItem()));
            }
        }
    }

    public void handleSpectate(C18PacketSpectate packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerForPlayer());

        if (this.playerEntity.isSpectator()) {
            Entity var2 = null;
            WorldServer[] var3 = this.serverController.worldServers;
            int var4 = var3.length;

            for (int var5 = 0; var5 < var4; ++var5) {
                WorldServer var6 = var3[var5];

                if (var6 != null) {
                    var2 = packetIn.getEntity(var6);

                    if (var2 != null) {
                        break;
                    }
                }
            }

            if (var2 != null) {
                this.playerEntity.setSpectatingEntity(this.playerEntity);
                this.playerEntity.mountEntity((Entity) null);

                if (var2.worldObj != this.playerEntity.worldObj) {
                    WorldServer var7 = this.playerEntity.getServerForPlayer();
                    WorldServer var8 = (WorldServer) var2.worldObj;
                    this.playerEntity.dimension = var2.dimension;
                    this.sendPacket(new S07PacketRespawn(this.playerEntity.dimension, var7.getDifficulty(), var7.getWorldInfo().getTerrainType(), this.playerEntity.theItemInWorldManager.getGameType()));
                    var7.removePlayerEntityDangerously(this.playerEntity);
                    this.playerEntity.isDead = false;
                    this.playerEntity.setLocationAndAngles(var2.posX, var2.posY, var2.posZ, var2.rotationYaw, var2.rotationPitch);

                    if (this.playerEntity.isEntityAlive()) {
                        var7.updateEntityWithOptionalForce(this.playerEntity, false);
                        var8.spawnEntityInWorld(this.playerEntity);
                        var8.updateEntityWithOptionalForce(this.playerEntity, false);
                    }

                    this.playerEntity.setWorld(var8);
                    this.serverController.getConfigurationManager().preparePlayer(this.playerEntity, var7);
                    this.playerEntity.setPositionAndUpdate(var2.posX, var2.posY, var2.posZ);
                    this.playerEntity.theItemInWorldManager.setWorld(var8);
                    this.serverController.getConfigurationManager().updateTimeAndWeatherForPlayer(this.playerEntity, var8);
                    this.serverController.getConfigurationManager().syncPlayerInventory(this.playerEntity);
                } else {
                    this.playerEntity.setPositionAndUpdate(var2.posX, var2.posY, var2.posZ);
                }
            }
        }
    }

    public void handleResourcePackStatus(C19PacketResourcePackStatus packetIn) {
    }

    /**
     * Invoked when disconnecting, the parameter is a ChatComponent describing the reason for termination
     */
    public void onDisconnect(IChatComponent reason) {
        logger.info(this.playerEntity.getCommandSenderName() + " lost connection: " + reason);
        this.serverController.refreshStatusNextTick();
        ChatComponentTranslation var2 = new ChatComponentTranslation("multiplayer.player.left", new Object[]{this.playerEntity.getDisplayName()});
        var2.getChatStyle().setColor(EnumChatFormatting.YELLOW);
        this.serverController.getConfigurationManager().sendChatMsg(var2);
        this.playerEntity.mountEntityAndWakeUp();
        this.serverController.getConfigurationManager().playerLoggedOut(this.playerEntity);

        if (this.serverController.isSinglePlayer() && this.playerEntity.getCommandSenderName().equals(this.serverController.getServerOwner())) {
            logger.info("Stopping singleplayer server as player logged out");
            this.serverController.initiateShutdown();
        }
    }

    public void sendPacket(final Packet packetIn) {
        if (packetIn instanceof S02PacketChat) {
            S02PacketChat var2 = (S02PacketChat) packetIn;
            EntityPlayer.EnumChatVisibility var3 = this.playerEntity.getChatVisibility();

            if (var3 == EntityPlayer.EnumChatVisibility.HIDDEN) {
                return;
            }

            if (var3 == EntityPlayer.EnumChatVisibility.SYSTEM && !var2.isChat()) {
                return;
            }
        }

        try {
            this.netManager.sendPacket(packetIn);
        } catch (Throwable var5) {
            CrashReport var6 = CrashReport.makeCrashReport(var5, "Sending packet");
            CrashReportCategory var4 = var6.makeCategory("Packet being sent");
            var4.addCrashSectionCallable("Packet class", new Callable() {
                public String func_180225_a() {
                    return packetIn.getClass().getCanonicalName();
                }

                public Object call() {
                    return this.func_180225_a();
                }
            });
            throw new ReportedException(var6);
        }
    }

    /**
     * Updates which quickbar slot is selected
     */
    public void processHeldItemChange(C09PacketHeldItemChange packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerForPlayer());

        if (packetIn.getSlotId() >= 0 && packetIn.getSlotId() < InventoryPlayer.getHotbarSize()) {
            this.playerEntity.inventory.currentItem = packetIn.getSlotId();
            this.playerEntity.markPlayerActive();
        } else {
            logger.warn(this.playerEntity.getCommandSenderName() + " tried to set an invalid carried item");
        }
    }

    /**
     * Process chat messages (broadcast back to clients) and commands (executes)
     */
    public void processChatMessage(C01PacketChatMessage packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerForPlayer());

        if (this.playerEntity.getChatVisibility() == EntityPlayer.EnumChatVisibility.HIDDEN) {
            ChatComponentTranslation var4 = new ChatComponentTranslation("chat.cannotSend", new Object[0]);
            var4.getChatStyle().setColor(EnumChatFormatting.RED);
            this.sendPacket(new S02PacketChat(var4));
        } else {
            this.playerEntity.markPlayerActive();
            String var2 = packetIn.getMessage();
            var2 = StringUtils.normalizeSpace(var2);

            for (int var3 = 0; var3 < var2.length(); ++var3) {
                if (!ChatAllowedCharacters.isAllowedCharacter(var2.charAt(var3))) {
                    this.kickPlayerFromServer("Illegal characters in chat");
                    return;
                }
            }

            if (var2.startsWith("/")) {
                this.handleSlashCommand(var2);
            } else {
                ChatComponentTranslation var5 = new ChatComponentTranslation("chat.type.text", new Object[]{this.playerEntity.getDisplayName(), var2});
                this.serverController.getConfigurationManager().sendChatMsgImpl(var5, false);
            }

            this.chatSpamThresholdCount += 20;

            if (this.chatSpamThresholdCount > 200 && !this.serverController.getConfigurationManager().canSendCommands(this.playerEntity.getGameProfile())) {
                this.kickPlayerFromServer("disconnect.spam");
            }
        }
    }

    /**
     * Handle commands that start with a /
     */
    private void handleSlashCommand(String command) {
        this.serverController.getCommandManager().executeCommand(this.playerEntity, command);
    }

    public void handleAnimation(C0APacketAnimation packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerForPlayer());
        this.playerEntity.markPlayerActive();
        this.playerEntity.swingItem();
    }

    /**
     * Processes a range of action-types: sneaking, sprinting, waking from sleep, opening the inventory or setting jump
     * height of the horse the player is riding
     */
    public void processEntityAction(C0BPacketEntityAction packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerForPlayer());
        this.playerEntity.markPlayerActive();

        switch (NetHandlerPlayServer.SwitchAction.field_180222_b[packetIn.getAction().ordinal()]) {
            case 1:
                this.playerEntity.setSneaking(true);
                break;

            case 2:
                this.playerEntity.setSneaking(false);
                break;

            case 3:
                this.playerEntity.setSprinting(true);
                break;

            case 4:
                this.playerEntity.setSprinting(false);
                break;

            case 5:
                this.playerEntity.wakeUpPlayer(false, true, true);
                this.hasMoved = false;
                break;

            case 6:
                if (this.playerEntity.ridingEntity instanceof EntityHorse) {
                    ((EntityHorse) this.playerEntity.ridingEntity).setJumpPower(packetIn.getAuxData());
                }

                break;

            case 7:
                if (this.playerEntity.ridingEntity instanceof EntityHorse) {
                    ((EntityHorse) this.playerEntity.ridingEntity).openGUI(this.playerEntity);
                }

                break;

            default:
                throw new IllegalArgumentException("Invalid client command!");
        }
    }

    /**
     * Processes interactions ((un)leashing, opening command block GUI) and attacks on an entity with players currently
     * equipped item
     */
    public void processUseEntity(C02PacketUseEntity packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerForPlayer());
        WorldServer var2 = this.serverController.worldServerForDimension(this.playerEntity.dimension);
        Entity var3 = packetIn.getEntityFromWorld(var2);
        this.playerEntity.markPlayerActive();

        if (var3 != null) {
            boolean var4 = this.playerEntity.canEntityBeSeen(var3);
            double var5 = 36.0D;

            if (!var4) {
                var5 = 9.0D;
            }

            if (this.playerEntity.getDistanceSqToEntity(var3) < var5) {
                if (packetIn.getAction() == C02PacketUseEntity.Action.INTERACT) {
                    this.playerEntity.interactWith(var3);
                } else if (packetIn.getAction() == C02PacketUseEntity.Action.INTERACT_AT) {
                    var3.interactAt(this.playerEntity, packetIn.getHitVec());
                } else if (packetIn.getAction() == C02PacketUseEntity.Action.ATTACK) {
                    if (var3 instanceof EntityItem || var3 instanceof EntityXPOrb || var3 instanceof EntityArrow || var3 == this.playerEntity) {
                        this.kickPlayerFromServer("Attempting to attack an invalid entity");
                        this.serverController.logWarning("Player " + this.playerEntity.getCommandSenderName() + " tried to attack an invalid entity");
                        return;
                    }

                    this.playerEntity.attackTargetEntityWithCurrentItem(var3);
                }
            }
        }
    }

    /**
     * Processes the client status updates: respawn attempt from player, opening statistics or achievements, or
     * acquiring 'open inventory' achievement
     */
    public void processClientStatus(C16PacketClientStatus packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerForPlayer());
        this.playerEntity.markPlayerActive();
        C16PacketClientStatus.EnumState var2 = packetIn.getStatus();

        switch (NetHandlerPlayServer.SwitchAction.field_180223_c[var2.ordinal()]) {
            case 1:
                if (this.playerEntity.playerConqueredTheEnd) {
                    this.playerEntity = this.serverController.getConfigurationManager().recreatePlayerEntity(this.playerEntity, 0, true);
                } else if (this.playerEntity.getServerForPlayer().getWorldInfo().isHardcoreModeEnabled()) {
                    if (this.serverController.isSinglePlayer() && this.playerEntity.getCommandSenderName().equals(this.serverController.getServerOwner())) {
                        this.playerEntity.playerNetServerHandler.kickPlayerFromServer("You have died. Game over, man, it\'s game over!");
                        this.serverController.deleteWorldAndStopServer();
                    } else {
                        UserListBansEntry var3 = new UserListBansEntry(this.playerEntity.getGameProfile(), (Date) null, "(You just lost the game)", (Date) null, "Death in Hardcore");
                        this.serverController.getConfigurationManager().getBannedPlayers().addEntry(var3);
                        this.playerEntity.playerNetServerHandler.kickPlayerFromServer("You have died. Game over, man, it\'s game over!");
                    }
                } else {
                    if (this.playerEntity.getHealth() > 0.0F) {
                        return;
                    }

                    this.playerEntity = this.serverController.getConfigurationManager().recreatePlayerEntity(this.playerEntity, 0, false);
                }

                break;

            case 2:
                this.playerEntity.getStatFile().func_150876_a(this.playerEntity);
                break;

            case 3:
                this.playerEntity.triggerAchievement(AchievementList.openInventory);
        }
    }

    /**
     * Processes the client closing windows (container)
     */
    public void processCloseWindow(C0DPacketCloseWindow packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerForPlayer());
        this.playerEntity.closeContainer();
    }

    /**
     * Executes a container/inventory slot manipulation as indicated by the packet. Sends the serverside result if they
     * didn't match the indicated result and prevents further manipulation by the player until he confirms that it has
     * the same open container/inventory
     */
    public void processClickWindow(C0EPacketClickWindow packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerForPlayer());
        this.playerEntity.markPlayerActive();

        if (this.playerEntity.openContainer.windowId == packetIn.getWindowId() && this.playerEntity.openContainer.getCanCraft(this.playerEntity)) {
            if (this.playerEntity.isSpectator()) {
                ArrayList var2 = Lists.newArrayList();

                for (int var3 = 0; var3 < this.playerEntity.openContainer.inventorySlots.size(); ++var3) {
                    var2.add(((Slot) this.playerEntity.openContainer.inventorySlots.get(var3)).getStack());
                }

                this.playerEntity.updateCraftingInventory(this.playerEntity.openContainer, var2);
            } else {
                ItemStack var5 = this.playerEntity.openContainer.slotClick(packetIn.getSlotId(), packetIn.getUsedButton(), packetIn.getMode(), this.playerEntity);

                if (ItemStack.areItemStacksEqual(packetIn.getClickedItem(), var5)) {
                    this.playerEntity.playerNetServerHandler.sendPacket(new S32PacketConfirmTransaction(packetIn.getWindowId(), packetIn.getActionNumber(), true));
                    this.playerEntity.isChangingQuantityOnly = true;
                    this.playerEntity.openContainer.detectAndSendChanges();
                    this.playerEntity.updateHeldItem();
                    this.playerEntity.isChangingQuantityOnly = false;
                } else {
                    this.field_147372_n.addKey(this.playerEntity.openContainer.windowId, Short.valueOf(packetIn.getActionNumber()));
                    this.playerEntity.playerNetServerHandler.sendPacket(new S32PacketConfirmTransaction(packetIn.getWindowId(), packetIn.getActionNumber(), false));
                    this.playerEntity.openContainer.setCanCraft(this.playerEntity, false);
                    ArrayList var6 = Lists.newArrayList();

                    for (int var4 = 0; var4 < this.playerEntity.openContainer.inventorySlots.size(); ++var4) {
                        var6.add(((Slot) this.playerEntity.openContainer.inventorySlots.get(var4)).getStack());
                    }

                    this.playerEntity.updateCraftingInventory(this.playerEntity.openContainer, var6);
                }
            }
        }
    }

    /**
     * Enchants the item identified by the packet given some convoluted conditions (matching window, which
     * should/shouldn't be in use?)
     */
    public void processEnchantItem(C11PacketEnchantItem packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerForPlayer());
        this.playerEntity.markPlayerActive();

        if (this.playerEntity.openContainer.windowId == packetIn.getWindowId() && this.playerEntity.openContainer.getCanCraft(this.playerEntity) && !this.playerEntity.isSpectator()) {
            this.playerEntity.openContainer.enchantItem(this.playerEntity, packetIn.getButton());
            this.playerEntity.openContainer.detectAndSendChanges();
        }
    }

    /**
     * Update the server with an ItemStack in a slot.
     */
    public void processCreativeInventoryAction(C10PacketCreativeInventoryAction packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerForPlayer());

        if (this.playerEntity.theItemInWorldManager.isCreative()) {
            boolean var2 = packetIn.getSlotId() < 0;
            ItemStack var3 = packetIn.getStack();

            if (var3 != null && var3.hasTagCompound() && var3.getTagCompound().hasKey("BlockEntityTag", 10)) {
                NBTTagCompound var4 = var3.getTagCompound().getCompoundTag("BlockEntityTag");

                if (var4.hasKey("x") && var4.hasKey("y") && var4.hasKey("z")) {
                    BlockPos var5 = new BlockPos(var4.getInteger("x"), var4.getInteger("y"), var4.getInteger("z"));
                    TileEntity var6 = this.playerEntity.worldObj.getTileEntity(var5);

                    if (var6 != null) {
                        NBTTagCompound var7 = new NBTTagCompound();
                        var6.writeToNBT(var7);
                        var7.removeTag("x");
                        var7.removeTag("y");
                        var7.removeTag("z");
                        var3.setTagInfo("BlockEntityTag", var7);
                    }
                }
            }

            boolean var8 = packetIn.getSlotId() >= 1 && packetIn.getSlotId() < 36 + InventoryPlayer.getHotbarSize();
            boolean var9 = var3 == null || var3.getItem() != null;
            boolean var10 = var3 == null || var3.getMetadata() >= 0 && var3.stackSize <= 64 && var3.stackSize > 0;

            if (var8 && var9 && var10) {
                if (var3 == null) {
                    this.playerEntity.inventoryContainer.putStackInSlot(packetIn.getSlotId(), (ItemStack) null);
                } else {
                    this.playerEntity.inventoryContainer.putStackInSlot(packetIn.getSlotId(), var3);
                }

                this.playerEntity.inventoryContainer.setCanCraft(this.playerEntity, true);
            } else if (var2 && var9 && var10 && this.itemDropThreshold < 200) {
                this.itemDropThreshold += 20;
                EntityItem var11 = this.playerEntity.dropPlayerItemWithRandomChoice(var3, true);

                if (var11 != null) {
                    var11.setAgeToCreativeDespawnTime();
                }
            }
        }
    }

    /**
     * Received in response to the server requesting to confirm that the client-side open container matches the servers'
     * after a mismatched container-slot manipulation. It will unlock the player's ability to manipulate the container
     * contents
     */
    public void processConfirmTransaction(C0FPacketConfirmTransaction packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerForPlayer());
        Short var2 = (Short) this.field_147372_n.lookup(this.playerEntity.openContainer.windowId);

        if (var2 != null && packetIn.getUid() == var2.shortValue() && this.playerEntity.openContainer.windowId == packetIn.getWindowId() && !this.playerEntity.openContainer.getCanCraft(this.playerEntity) && !this.playerEntity.isSpectator()) {
            this.playerEntity.openContainer.setCanCraft(this.playerEntity, true);
        }
    }

    public void processUpdateSign(C12PacketUpdateSign packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerForPlayer());
        this.playerEntity.markPlayerActive();
        WorldServer var2 = this.serverController.worldServerForDimension(this.playerEntity.dimension);
        BlockPos var3 = packetIn.getPosition();

        if (var2.isBlockLoaded(var3)) {
            TileEntity var4 = var2.getTileEntity(var3);

            if (!(var4 instanceof TileEntitySign)) {
                return;
            }

            TileEntitySign var5 = (TileEntitySign) var4;

            if (!var5.getIsEditable() || var5.getPlayer() != this.playerEntity) {
                this.serverController.logWarning("Player " + this.playerEntity.getCommandSenderName() + " just tried to change non-editable sign");
                return;
            }

            System.arraycopy(packetIn.getLines(), 0, var5.signText, 0, 4);
            var5.markDirty();
            var2.markBlockForUpdate(var3);
        }
    }

    /**
     * Updates a players' ping statistics
     */
    public void processKeepAlive(C00PacketKeepAlive packetIn) {
        if (packetIn.getKey() == this.field_147378_h) {
            int var2 = (int) (this.currentTimeMillis() - this.lastPingTime);
            this.playerEntity.ping = (this.playerEntity.ping * 3 + var2) / 4;
        }
    }

    private long currentTimeMillis() {
        return System.nanoTime() / 1000000L;
    }

    /**
     * Processes a player starting/stopping flying
     */
    public void processPlayerAbilities(C13PacketPlayerAbilities packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerForPlayer());
        this.playerEntity.capabilities.isFlying = packetIn.isFlying() && this.playerEntity.capabilities.allowFlying;
    }

    /**
     * Retrieves possible tab completions for the requested command string and sends them to the client
     */
    public void processTabComplete(C14PacketTabComplete packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerForPlayer());
        ArrayList var2 = Lists.newArrayList();
        Iterator var3 = this.serverController.getTabCompletions(this.playerEntity, packetIn.getMessage(), packetIn.getTargetBlock()).iterator();

        while (var3.hasNext()) {
            String var4 = (String) var3.next();
            var2.add(var4);
        }

        this.playerEntity.playerNetServerHandler.sendPacket(new S3APacketTabComplete((String[]) var2.toArray(new String[var2.size()])));
    }

    /**
     * Updates serverside copy of client settings: language, render distance, chat visibility, chat colours, difficulty,
     * and whether to show the cape
     */
    public void processClientSettings(C15PacketClientSettings packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerForPlayer());
        this.playerEntity.handleClientSettings(packetIn);
    }

    /**
     * Synchronizes serverside and clientside book contents and signing
     */
    public void processVanilla250Packet(C17PacketCustomPayload packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.playerEntity.getServerForPlayer());
        PacketBuffer var2;
        ItemStack var3;
        ItemStack var4;

        if ("MC|BEdit".equals(packetIn.getChannelName())) {
            var2 = new PacketBuffer(Unpooled.wrappedBuffer(packetIn.getBufferData()));

            try {
                var3 = var2.readItemStackFromBuffer();

                if (var3 == null) {
                    return;
                }

                if (!ItemWritableBook.isNBTValid(var3.getTagCompound())) {
                    throw new IOException("Invalid book tag!");
                }

                var4 = this.playerEntity.inventory.getCurrentItem();

                if (var4 != null) {
                    if (var3.getItem() == Items.writable_book && var3.getItem() == var4.getItem()) {
                        var4.setTagInfo("pages", var3.getTagCompound().getTagList("pages", 8));
                    }

                    return;
                }
            } catch (Exception var38) {
                logger.error("Couldn\'t handle book info", var38);
                return;
            } finally {
                var2.release();
            }

            return;
        } else if ("MC|BSign".equals(packetIn.getChannelName())) {
            var2 = new PacketBuffer(Unpooled.wrappedBuffer(packetIn.getBufferData()));

            try {
                var3 = var2.readItemStackFromBuffer();

                if (var3 == null) {
                    return;
                }

                if (!ItemEditableBook.validBookTagContents(var3.getTagCompound())) {
                    throw new IOException("Invalid book tag!");
                }

                var4 = this.playerEntity.inventory.getCurrentItem();

                if (var4 != null) {
                    if (var3.getItem() == Items.written_book && var4.getItem() == Items.writable_book) {
                        var4.setTagInfo("author", new NBTTagString(this.playerEntity.getCommandSenderName()));
                        var4.setTagInfo("title", new NBTTagString(var3.getTagCompound().getString("title")));
                        var4.setTagInfo("pages", var3.getTagCompound().getTagList("pages", 8));
                        var4.setItem(Items.written_book);
                    }

                    return;
                }
            } catch (Exception var36) {
                logger.error("Couldn\'t sign book", var36);
                return;
            } finally {
                var2.release();
            }

            return;
        } else if ("MC|TrSel".equals(packetIn.getChannelName())) {
            try {
                int var40 = packetIn.getBufferData().readInt();
                Container var42 = this.playerEntity.openContainer;

                if (var42 instanceof ContainerMerchant) {
                    ((ContainerMerchant) var42).setCurrentRecipeIndex(var40);
                }
            } catch (Exception var35) {
                logger.error("Couldn\'t select trade", var35);
            }
        } else if ("MC|AdvCdm".equals(packetIn.getChannelName())) {
            if (!this.serverController.isCommandBlockEnabled()) {
                this.playerEntity.addChatMessage(new ChatComponentTranslation("advMode.notEnabled", new Object[0]));
            } else if (this.playerEntity.canCommandSenderUseCommand(2, "") && this.playerEntity.capabilities.isCreativeMode) {
                var2 = packetIn.getBufferData();

                try {
                    byte var43 = var2.readByte();
                    CommandBlockLogic var46 = null;

                    if (var43 == 0) {
                        TileEntity var5 = this.playerEntity.worldObj.getTileEntity(new BlockPos(var2.readInt(), var2.readInt(), var2.readInt()));

                        if (var5 instanceof TileEntityCommandBlock) {
                            var46 = ((TileEntityCommandBlock) var5).getCommandBlockLogic();
                        }
                    } else if (var43 == 1) {
                        Entity var48 = this.playerEntity.worldObj.getEntityByID(var2.readInt());

                        if (var48 instanceof EntityMinecartCommandBlock) {
                            var46 = ((EntityMinecartCommandBlock) var48).getCommandBlockLogic();
                        }
                    }

                    String var49 = var2.readStringFromBuffer(var2.readableBytes());
                    boolean var6 = var2.readBoolean();

                    if (var46 != null) {
                        var46.setCommand(var49);
                        var46.setTrackOutput(var6);

                        if (!var6) {
                            var46.setLastOutput((IChatComponent) null);
                        }

                        var46.updateCommand();
                        this.playerEntity.addChatMessage(new ChatComponentTranslation("advMode.setCommand.success", new Object[]{var49}));
                    }
                } catch (Exception var33) {
                    logger.error("Couldn\'t set command block", var33);
                } finally {
                    var2.release();
                }
            } else {
                this.playerEntity.addChatMessage(new ChatComponentTranslation("advMode.notAllowed", new Object[0]));
            }
        } else if ("MC|Beacon".equals(packetIn.getChannelName())) {
            if (this.playerEntity.openContainer instanceof ContainerBeacon) {
                try {
                    var2 = packetIn.getBufferData();
                    int var44 = var2.readInt();
                    int var47 = var2.readInt();
                    ContainerBeacon var50 = (ContainerBeacon) this.playerEntity.openContainer;
                    Slot var51 = var50.getSlot(0);

                    if (var51.getHasStack()) {
                        var51.decrStackSize(1);
                        IInventory var7 = var50.func_180611_e();
                        var7.setField(1, var44);
                        var7.setField(2, var47);
                        var7.markDirty();
                    }
                } catch (Exception var32) {
                    logger.error("Couldn\'t set beacon", var32);
                }
            }
        } else if ("MC|ItemName".equals(packetIn.getChannelName()) && this.playerEntity.openContainer instanceof ContainerRepair) {
            ContainerRepair var41 = (ContainerRepair) this.playerEntity.openContainer;

            if (packetIn.getBufferData() != null && packetIn.getBufferData().readableBytes() >= 1) {
                String var45 = ChatAllowedCharacters.filterAllowedCharacters(packetIn.getBufferData().readStringFromBuffer(32767));

                if (var45.length() <= 30) {
                    var41.updateItemName(var45);
                }
            } else {
                var41.updateItemName("");
            }
        }
    }

    static final class SwitchAction {
        static final int[] field_180224_a;

        static final int[] field_180222_b;

        static final int[] field_180223_c = new int[C16PacketClientStatus.EnumState.values().length];

        static {
            try {
                field_180223_c[C16PacketClientStatus.EnumState.PERFORM_RESPAWN.ordinal()] = 1;
            } catch (NoSuchFieldError var16) {
                ;
            }

            try {
                field_180223_c[C16PacketClientStatus.EnumState.REQUEST_STATS.ordinal()] = 2;
            } catch (NoSuchFieldError var15) {
                ;
            }

            try {
                field_180223_c[C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT.ordinal()] = 3;
            } catch (NoSuchFieldError var14) {
                ;
            }

            field_180222_b = new int[C0BPacketEntityAction.Action.values().length];

            try {
                field_180222_b[C0BPacketEntityAction.Action.START_SNEAKING.ordinal()] = 1;
            } catch (NoSuchFieldError var13) {
                ;
            }

            try {
                field_180222_b[C0BPacketEntityAction.Action.STOP_SNEAKING.ordinal()] = 2;
            } catch (NoSuchFieldError var12) {
                ;
            }

            try {
                field_180222_b[C0BPacketEntityAction.Action.START_SPRINTING.ordinal()] = 3;
            } catch (NoSuchFieldError var11) {
                ;
            }

            try {
                field_180222_b[C0BPacketEntityAction.Action.STOP_SPRINTING.ordinal()] = 4;
            } catch (NoSuchFieldError var10) {
                ;
            }

            try {
                field_180222_b[C0BPacketEntityAction.Action.STOP_SLEEPING.ordinal()] = 5;
            } catch (NoSuchFieldError var9) {
                ;
            }

            try {
                field_180222_b[C0BPacketEntityAction.Action.RIDING_JUMP.ordinal()] = 6;
            } catch (NoSuchFieldError var8) {
                ;
            }

            try {
                field_180222_b[C0BPacketEntityAction.Action.OPEN_INVENTORY.ordinal()] = 7;
            } catch (NoSuchFieldError var7) {
                ;
            }

            field_180224_a = new int[C07PacketPlayerDigging.Action.values().length];

            try {
                field_180224_a[C07PacketPlayerDigging.Action.DROP_ITEM.ordinal()] = 1;
            } catch (NoSuchFieldError var6) {
                ;
            }

            try {
                field_180224_a[C07PacketPlayerDigging.Action.DROP_ALL_ITEMS.ordinal()] = 2;
            } catch (NoSuchFieldError var5) {
                ;
            }

            try {
                field_180224_a[C07PacketPlayerDigging.Action.RELEASE_USE_ITEM.ordinal()] = 3;
            } catch (NoSuchFieldError var4) {
                ;
            }

            try {
                field_180224_a[C07PacketPlayerDigging.Action.START_DESTROY_BLOCK.ordinal()] = 4;
            } catch (NoSuchFieldError var3) {
                ;
            }

            try {
                field_180224_a[C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK.ordinal()] = 5;
            } catch (NoSuchFieldError var2) {
                ;
            }

            try {
                field_180224_a[C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK.ordinal()] = 6;
            } catch (NoSuchFieldError var1) {
                ;
            }
        }
    }
}