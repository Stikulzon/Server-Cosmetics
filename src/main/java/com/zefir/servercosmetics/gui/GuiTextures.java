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
//    public static final Supplier<GuiElementBuilder> EMPTY_BUILDER = icon16("empty");
//    public static final GuiElement EMPTY = EMPTY_BUILDER.get().hideTooltip().build();
    public static final Function<Text, Text> COLOR_PICKER_MENU = background("color_picker_menu");
    public static final Function<Text, Text> COSMETICS_MENU = background("cosmetics_menu");
    public static final Function<Text, Text> ITEM_SKINS_MENU = background("item_skins_menu");

    public static void register(){
    }

    public record Progress(GuiElement[] elements, ItemStack[] withTooltip) {
        public GuiElement get(float progress) {
            return elements[Math.min((int) (progress * elements.length), elements.length - 1)];
        }
//        public static Progress createHorizontal32Right(String path, int start, int stop, boolean reverse) {
//            var size = stop - start;
//            var function = horizontalProgress32Right(path, start, stop, reverse);
//
//            return create(size, function);
//        }

//        private static Progress create(int size, IntFunction<GuiElementBuilder> function) {
//            var elements = new GuiElement[size + 1];
//            var withTooltip = new ItemStack[size + 1];
//
//            elements[0] = EMPTY;
//            withTooltip[0] = EMPTY.getItemStack().copy();
//
//            for (var i = 1; i <= size; i++) {
//                elements[i] = function.apply(i - 1).hideTooltip().build();
//                withTooltip[i] = function.apply(i - 1).asStack();
//            }
//            return new Progress(elements, withTooltip);
//        }
//
//        public static Progress createVertical(String path, int start, int stop, boolean reverse) {
//            var size = stop - start;
//            var function = verticalProgress16(path, start, stop, reverse);
//
//            return create(size, function);
//        }
    }
}
