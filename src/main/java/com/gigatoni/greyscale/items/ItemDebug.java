package com.gigatoni.greyscale.items;

import com.gigatoni.greyscale.Castle_Mini;
import com.gigatoni.greyscale.Greyscale;
import com.gigatoni.greyscale.handler.ConfigHandler;
import com.gigatoni.greyscale.reference.Reference;
import com.gigatoni.greyscale.utility.SchematicUtil;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;

import java.util.List;
import java.util.Random;

public class ItemDebug extends Item {

    private SchematicUtil schematicUtil;
    private int selectedSchem = 0;
    private int yOffset;
    private String selectedSchemName = "";

    public ItemDebug(){
        super();
        setCreativeTab(Reference.genericRPGItems);
        setUnlocalizedName("debug");
        setMaxStackSize(1);
        setTextureName(Reference.MOD_ID + ":debug");
        schematicUtil = new SchematicUtil();
        yOffset = (int)Math.floor(Reference.schematicOffsets[selectedSchem].yCoord);
        selectedSchemName = Reference.schematics[selectedSchem];
    }

    public int getYOffset(){
        return 0;
    }

    public int getXShift() {
        return 0;
    }

    // Offsets:
    /*
    * Blacksmith: y - 5
    * Bakery: y - 1
    * Box T1: y -1
    * Box T2: y -1
    * Carpenter: y - 5
    * Butcher: y - 1
    * */

    private void NextSchem()
    {
        if((ConfigHandler.schematics.size()-1) > selectedSchem)
        {
            selectedSchem += 1;
            //yOffset = (int)Math.floor(Reference.schematicOffsets[selectedSchem].yCoord);
            selectedSchemName = ConfigHandler.schematics.get(selectedSchem);
        }else{
            selectedSchem = 0;
            //yOffset = (int)Math.floor(Reference.schematicOffsets[selectedSchem].yCoord);
            selectedSchemName = ConfigHandler.schematics.get(selectedSchem);
        }

        Reference.loadedSchematic = null;
        Reference.loadedSchematic = schematicUtil.get(ConfigHandler.schematics.get(selectedSchem));
        if (Reference.loadedSchematic == null) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Can't load schematic '" + selectedSchemName + "'"));
        }else{
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("Loaded Schematic '" + selectedSchemName + "'"));
        }
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4)
    {
        if(!selectedSchemName.equals("") && selectedSchemName.lastIndexOf('.') > 0)
            list.add(selectedSchemName.substring(0, selectedSchemName.lastIndexOf('.')));
    }

    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (stack.getItem() == this && isSelected) {

        }
    }

    /*
    @Override
    public ItemStack onItemRightClick(ItemStack item, World world, EntityPlayer player)
    {
        int x = (int)Math.floor(player.posX);
        int y = (int)Math.floor(player.posY);
        int z = (int)Math.floor(player.posZ);

        if(player.isSneaking()) {
            NextSchem();
        }else if(Reference.schematicPosition != null){
            for (int pX = 0; pX < 16; pX++)
                for (int pY = 0; pY < 20; pY++)
                    for (int pZ = 0; pZ < 16; pZ++) {
                        int rx = schematicUtil.blockCoordsRotation(pX - this.getXShift(), pZ, Reference.schematicRotation)[0];
                        int rz = schematicUtil.blockCoordsRotation(pX - this.getXShift(), pZ, Reference.schematicRotation)[1];
                        world.setBlock(x + rx, y + pY, z + rz, Blocks.air, 0, 3);
                        world.setBlock((int)Reference.schematicPosition.xCoord + rx, (int)Reference.schematicPosition.yCoord + pY, (int)Reference.schematicPosition.zCoord +rz, Blocks.air, 0, 3);
                    }

            for (int pX = 0; pX < 16; pX++)
                for (int pZ = 0; pZ < 16; pZ++) {
                    int rx = schematicUtil.blockCoordsRotation(pX - this.getXShift(), pZ, Reference.schematicRotation)[0];
                    int rz = schematicUtil.blockCoordsRotation(pX - this.getXShift(), pZ, Reference.schematicRotation)[1];
                    world.setBlock(x + rx, y, z + rz, Blocks.grass, 0, 3);
                    world.setBlock((int)Reference.schematicPosition.xCoord + rx, (int)Reference.schematicPosition.yCoord, (int)Reference.schematicPosition.zCoord +rz, Blocks.grass, 0, 3);
                }
        }
        return item;
    }*/

    @Override
    public boolean onItemUse(ItemStack is, EntityPlayer player, World world, int x, int y, int z, int side, float px, float py, float pz) {
        if(!player.capabilities.isCreativeMode){
            if(!world.isRemote)
                player.addChatMessage(new ChatComponentText("You don't have permission to use this!"));
            return false;
        }

        if (!world.isRemote) {
            if (player.isSneaking()) {
                NextSchem();
            } else {
                if ((ConfigHandler.schematics.size()-1) > selectedSchem && Reference.loadedSchematic != null) {
                    int rotation = MathHelper.floor_double((double) (player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;

                    player.addChatMessage(new ChatComponentText("Building started."));

                    int i = 0;
                    for (int sy = 0; sy < Reference.loadedSchematic.height; sy++)
                        for (int sz = 0; sz < Reference.loadedSchematic.length; sz++)
                            for (int sx = 0; sx < Reference.loadedSchematic.width; sx++) {

                                Block b = Block.getBlockById(Reference.loadedSchematic.blocks[i]);
                                int rx = schematicUtil.blockCoordsRotation(sx - this.getXShift(), sz, rotation)[0];
                                int rz = schematicUtil.blockCoordsRotation(sx - this.getXShift(), sz, rotation)[1];
                                if (b != Blocks.air) {
                                    if(b != Blocks.torch && b != Blocks.redstone_torch && b != Blocks.redstone_wire)
                                        world.setBlock(x + rx, y + getYOffset() + sy, z + rz, b, schematicUtil.rotateMeta(Reference.loadedSchematic.blocks[i], Reference.loadedSchematic.data[i], rotation), 2);
                                }
                                else
                                    world.setBlockToAir(x + rx, y + getYOffset() + sy, z + rz);
                                i++;
                            }

                    i = 0;
                    for (int sy = 0; sy < Reference.loadedSchematic.height; sy++)
                        for (int sz = 0; sz < Reference.loadedSchematic.length; sz++)
                            for (int sx = 0; sx < Reference.loadedSchematic.width; sx++) {

                                Block b = Block.getBlockById(Reference.loadedSchematic.blocks[i]);
                                int rx = schematicUtil.blockCoordsRotation(sx - this.getXShift(), sz, rotation)[0];
                                int rz = schematicUtil.blockCoordsRotation(sx - this.getXShift(), sz, rotation)[1];
                                if (b == Blocks.torch || b == Blocks.redstone_torch || b == Blocks.redstone_wire) {
                                    world.setBlock(x + rx, y + getYOffset() + sy, z + rz, b, schematicUtil.rotateMeta(Reference.loadedSchematic.blocks[i], Reference.loadedSchematic.data[i], rotation), 2);
                                }
                                i++;
                            }


                    if (Reference.loadedSchematic.tileentities != null) {
                        for (int i1 = 0; i1 < Reference.loadedSchematic.tileentities.tagCount(); ++i1) {
                            NBTTagCompound nbttagcompound4 = Reference.loadedSchematic.tileentities.getCompoundTagAt(i1);
                            TileEntity tileentity = TileEntity.createAndLoadEntity(nbttagcompound4);

                            if (tileentity != null) {
                                int[] conv2 = schematicUtil.blockCoordsRotation(tileentity.xCoord - this.getXShift(), tileentity.zCoord, rotation);
                                tileentity.xCoord = x + conv2[0];
                                tileentity.yCoord += y + getYOffset();
                                tileentity.zCoord = z + conv2[1];
                                world.setTileEntity(tileentity.xCoord, tileentity.yCoord, tileentity.zCoord, tileentity);
                            }
                        }
                    }

                    player.addChatMessage(new ChatComponentText("Building finished."));
                }
                Reference.schematicPosition = Vec3.createVectorHelper(x, y, z);
                Reference.schematicRotation = MathHelper.floor_double((double) (player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
            }
        }
        return true;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack item, World world, EntityPlayer player)
    {
        if(!player.capabilities.isCreativeMode){
            if(!world.isRemote)
                player.addChatMessage(new ChatComponentText("You don't have permission to use this!"));
            return item;
        }

        if(player.isSneaking())
            player.openGui(Greyscale.INSTANCE, Reference.SKILL_UI_ID, world, (int)player.posX, (int)player.posY, (int)player.posZ);
        else
            player.openGui(Greyscale.INSTANCE, Reference.QUESTBOOK_UI_ID, world, (int)player.posX, (int)player.posY, (int)player.posZ);
        return item;
    }
}
