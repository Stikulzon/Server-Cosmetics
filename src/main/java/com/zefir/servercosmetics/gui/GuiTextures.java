package com.zefir.servercosmetics.gui;

import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;

import static com.zefir.servercosmetics.gui.UiResourceCreator.*;

public class GuiTextures {
    public static final Function<Text, Text> COLOR_PICKER_MENU = background("color_picker_menu");
    public static final Function<Text, Text> COSMETICS_MENU = background("cosmetics_menu");
    public static final Function<Text, Text> ITEM_SKINS_MENU = background("item_skins_menu");

    public static void register(){
    }
}
