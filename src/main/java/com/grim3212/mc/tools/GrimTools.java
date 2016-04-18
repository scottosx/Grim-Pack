package com.grim3212.mc.tools;

import com.grim3212.mc.core.config.GrimConfig;
import com.grim3212.mc.core.manual.ManualRegistry;
import com.grim3212.mc.core.manual.ModSection;
import com.grim3212.mc.core.manual.pages.PageCrafting;
import com.grim3212.mc.core.manual.pages.PageFurnace;
import com.grim3212.mc.core.manual.pages.PageImageText;
import com.grim3212.mc.core.part.GrimPart;
import com.grim3212.mc.core.proxy.CommonProxy;
import com.grim3212.mc.tools.blocks.ToolsBlocks;
import com.grim3212.mc.tools.client.gui.ToolsGuiHandler;
import com.grim3212.mc.tools.config.ToolsConfig;
import com.grim3212.mc.tools.entity.ToolsEntities;
import com.grim3212.mc.tools.items.ToolsItems;
import com.grim3212.mc.tools.util.MilkingEvent;
import com.grim3212.mc.tools.world.ToolsGenerate;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod(modid = GrimTools.modID, name = GrimTools.modName, version = GrimTools.modVersion, dependencies = "required-after:grimcore")
public class GrimTools extends GrimPart {

	@SidedProxy(clientSide = "com.grim3212.mc.tools.ToolsClientProxy", serverSide = COMMON_PROXY)
	public static CommonProxy proxy;

	@Instance(GrimTools.modID)
	public static GrimTools INSTANCE;

	public static final String modID = "grimtools";
	public static final String modName = "Grim Tools";
	public static final String modVersion = "1.0.0";

	public GrimTools() {
		super(GrimTools.modID, GrimTools.modName, GrimTools.modVersion);
		addItem(new ToolsItems());
		addItem(new ToolsBlocks());
		addEntity(new ToolsEntities());
	}

	@Override
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		super.preInit(event);
		ModMetadata data = event.getModMetadata();
		data.description = "Grim Tools adds in a bunch of tools and weapons.";
		data.url = "http://mods.grim3212.com/mc/" + "my-mods/grim-tools/";
		data.credits = "Thanks to the following authors. Leesgowest, LFalch, mattop101, Nandonalt.";

		NetworkRegistry.INSTANCE.registerGuiHandler(INSTANCE, new ToolsGuiHandler());
		MinecraftForge.EVENT_BUS.register(new MilkingEvent());

		GameRegistry.registerWorldGenerator(new ToolsGenerate(), 25);

		proxy.registerModels();
	}

	@Override
	protected Item getCreativeTabIcon() {
		return ToolsItems.backpack;
	}

	@Override
	protected GrimConfig setConfig() {
		return new ToolsConfig();
	}

	@Override
	protected void setupManualPages(ModSection modSection) {
		ManualRegistry.addSection("backpacks", modSection).addSubSectionPages(new PageCrafting("backpacks", new ItemStack(ToolsItems.backpack)), new PageImageText("colors", "colorsImage.png"));
		ManualRegistry.addSection("portable", modSection).addSubSectionPages(new PageCrafting("portableworkbench", new ItemStack(ToolsItems.portable_workbench)));
		ManualRegistry.addSection("parts", modSection).addSubSectionPages(new PageCrafting("part1", new ItemStack(ToolsItems.casing_part)), new PageCrafting("part2", new ItemStack(ToolsItems.spring_part)), new PageCrafting("part3", new ItemStack(ToolsItems.button_part)), new PageCrafting("part4", new ItemStack(ToolsItems.rod_part)));
		ManualRegistry.addSection("ballistic", modSection).addSubSectionPages(new PageCrafting("ballistic", new ItemStack(ToolsItems.unloaded_knife)), new PageCrafting("knives", new ItemStack(ToolsItems.ammo_part)));
		ManualRegistry.addSection("black", modSection).addSubSectionPages(new PageImageText("blackore", "blackore.png"), new PageFurnace("cookOre", new ItemStack(ToolsBlocks.black_diamond_ore)), new PageCrafting("craftblack", ToolsBlocks.black, 25));
		ManualRegistry.addSection("tools", modSection).addSubSectionPages(new PageCrafting("tools", ToolsItems.blackTools, 18));
		ManualRegistry.addSection("armor", modSection).addSubSectionPages(new PageCrafting("armor", ToolsItems.blackArmor, 18));
	}
}