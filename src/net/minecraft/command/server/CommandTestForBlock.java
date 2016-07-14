package net.minecraft.command.server;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.*;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import java.util.Iterator;
import java.util.List;

public class CommandTestForBlock extends CommandBase {

    /**
     * Gets the name of the command
     */
    public String getCommandName() {
        return "testforblock";
    }

    /**
     * Return the required permission level for this command.
     */
    public int getRequiredPermissionLevel() {
        return 2;
    }

    /**
     * Gets the usage string for the command.
     *
     * @param sender The {@link ICommandSender} who is requesting usage details.
     */
    public String getCommandUsage(ICommandSender sender) {
        return "commands.testforblock.usage";
    }

    /**
     * Callback when the command is invoked
     *
     * @param sender The {@link ICommandSender sender} who executed the command
     * @param args   The arguments that were passed with the command
     */
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 4) {
            throw new WrongUsageException("commands.testforblock.usage", new Object[0]);
        } else {
            sender.setCommandStat(CommandResultStats.Type.AFFECTED_BLOCKS, 0);
            BlockPos var3 = parseBlockPos(sender, args, 0, false);
            Block var4 = Block.getBlockFromName(args[3]);

            if (var4 == null) {
                throw new NumberInvalidException("commands.setblock.notFound", new Object[]{args[3]});
            } else {
                int var5 = -1;

                if (args.length >= 5) {
                    var5 = parseInt(args[4], -1, 15);
                }

                World var6 = sender.getEntityWorld();

                if (!var6.isBlockLoaded(var3)) {
                    throw new CommandException("commands.testforblock.outOfWorld", new Object[0]);
                } else {
                    NBTTagCompound var7 = new NBTTagCompound();
                    boolean var8 = false;

                    if (args.length >= 6 && var4.hasTileEntity()) {
                        String var9 = getChatComponentFromNthArg(sender, args, 5).getUnformattedText();

                        try {
                            var7 = JsonToNBT.getTagFromJson(var9);
                            var8 = true;
                        } catch (NBTException var13) {
                            throw new CommandException("commands.setblock.tagError", new Object[]{var13.getMessage()});
                        }
                    }

                    IBlockState var14 = var6.getBlockState(var3);
                    Block var10 = var14.getBlock();

                    if (var10 != var4) {
                        throw new CommandException("commands.testforblock.failed.tile", new Object[]{Integer.valueOf(var3.getX()), Integer.valueOf(var3.getY()), Integer.valueOf(var3.getZ()), var10.getLocalizedName(), var4.getLocalizedName()});
                    } else {
                        if (var5 > -1) {
                            int var11 = var14.getBlock().getMetaFromState(var14);

                            if (var11 != var5) {
                                throw new CommandException("commands.testforblock.failed.data", new Object[]{Integer.valueOf(var3.getX()), Integer.valueOf(var3.getY()), Integer.valueOf(var3.getZ()), Integer.valueOf(var11), Integer.valueOf(var5)});
                            }
                        }

                        if (var8) {
                            TileEntity var15 = var6.getTileEntity(var3);

                            if (var15 == null) {
                                throw new CommandException("commands.testforblock.failed.tileEntity", new Object[]{Integer.valueOf(var3.getX()), Integer.valueOf(var3.getY()), Integer.valueOf(var3.getZ())});
                            }

                            NBTTagCompound var12 = new NBTTagCompound();
                            var15.writeToNBT(var12);

                            if (!func_175775_a(var7, var12, true)) {
                                throw new CommandException("commands.testforblock.failed.nbt", new Object[]{Integer.valueOf(var3.getX()), Integer.valueOf(var3.getY()), Integer.valueOf(var3.getZ())});
                            }
                        }

                        sender.setCommandStat(CommandResultStats.Type.AFFECTED_BLOCKS, 1);
                        notifyOperators(sender, this, "commands.testforblock.success", new Object[]{Integer.valueOf(var3.getX()), Integer.valueOf(var3.getY()), Integer.valueOf(var3.getZ())});
                    }
                }
            }
        }
    }

    public static boolean func_175775_a(NBTBase p_175775_0_, NBTBase p_175775_1_, boolean p_175775_2_) {
        if (p_175775_0_ == p_175775_1_) {
            return true;
        } else if (p_175775_0_ == null) {
            return true;
        } else if (p_175775_1_ == null) {
            return false;
        } else if (!p_175775_0_.getClass().equals(p_175775_1_.getClass())) {
            return false;
        } else if (p_175775_0_ instanceof NBTTagCompound) {
            NBTTagCompound var9 = (NBTTagCompound) p_175775_0_;
            NBTTagCompound var10 = (NBTTagCompound) p_175775_1_;
            Iterator var11 = var9.getKeySet().iterator();
            String var12;
            NBTBase var13;

            do {
                if (!var11.hasNext()) {
                    return true;
                }

                var12 = (String) var11.next();
                var13 = var9.getTag(var12);
            }
            while (func_175775_a(var13, var10.getTag(var12), p_175775_2_));

            return false;
        } else if (p_175775_0_ instanceof NBTTagList && p_175775_2_) {
            NBTTagList var3 = (NBTTagList) p_175775_0_;
            NBTTagList var4 = (NBTTagList) p_175775_1_;

            if (var3.tagCount() == 0) {
                return var4.tagCount() == 0;
            } else {
                int var5 = 0;

                while (var5 < var3.tagCount()) {
                    NBTBase var6 = var3.get(var5);
                    boolean var7 = false;
                    int var8 = 0;

                    while (true) {
                        if (var8 < var4.tagCount()) {
                            if (!func_175775_a(var6, var4.get(var8), p_175775_2_)) {
                                ++var8;
                                continue;
                            }

                            var7 = true;
                        }

                        if (!var7) {
                            return false;
                        }

                        ++var5;
                        break;
                    }
                }

                return true;
            }
        } else {
            return p_175775_0_.equals(p_175775_1_);
        }
    }

    /**
     * Return a list of options when the user types TAB
     *
     * @param sender The {@link ICommandSender sender} who pressed TAB
     * @param args   The arguments that were present when TAB was pressed
     * @param pos    The block that the player is targeting, <b>May be {@code null}</b>
     */
    public List addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return args.length > 0 && args.length <= 3 ? func_175771_a(args, 0, pos) : (args.length == 4 ? getListOfStringsMatchingLastWord(args, Block.blockRegistry.getKeys()) : null);
    }
}
