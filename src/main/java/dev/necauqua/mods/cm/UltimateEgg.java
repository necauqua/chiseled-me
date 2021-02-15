/*
 * Copyright (c) 2017-2021 Anton Bulakh <self@necauqua.dev>
 * Licensed under MIT, see the LICENSE file for details.
 */

package dev.necauqua.mods.cm;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import static dev.necauqua.mods.cm.ChiseledMe.MODID;
import static net.minecraft.util.text.event.ClickEvent.Action.SUGGEST_COMMAND;
import static net.minecraft.util.text.event.HoverEvent.Action.SHOW_ENTITY;
import static net.minecraft.util.text.event.HoverEvent.Action.SHOW_TEXT;

@EventBusSubscriber(modid = MODID, value = Side.CLIENT)
@SideOnly(Side.CLIENT)
public final class UltimateEgg {

    private static final String USERNAME = "necauqua";
    private static final String USERNAME_COLORED = "§d" + USERNAME + "§r";

    private static boolean incoming = false;
    private static final Set<ChatLine> handled = Collections.newSetFromMap(new WeakHashMap<>());
    private static final Set<ITextComponent> names = Collections.newSetFromMap(new WeakHashMap<>());

    // this whole thing exist so I can track (the `names` set) exact ITextComponent instances being drawn
    // and modify them each tick to make the animation, MC recreates (serde) the instances several times
    // even on the way from ClientChatReceivedEvent to actual rendering code, so have to go with those hacks
    //
    // could've done everything smoother yet much hackier by replacing drawnChatLines with a wrapper to track List#add
    // but if for Cleanse it kind of makes sense, I feel that it is not really plausible for something like
    // a dumb mod author privilege thing

    @SubscribeEvent
    public static void on(ClientChatReceivedEvent e) {
        // 'run code on the next tick'
        // so that drawnChatLines are populated
        // with recreated split text components
        incoming = true;
    }

    @SubscribeEvent
    public static void on(ClientTickEvent e) {
        if (e.phase == Phase.START) {
            return;
        }

        // update our names
        names.forEach(name -> name.getSiblings().set(0, generateName()));

        // and then short circuit out unless we got
        // scheduled from that chat received event
        if (!incoming) {
            return;
        }
        incoming = false;

        GuiIngame ingameGUI = Minecraft.getMinecraft().ingameGUI;
        if (ingameGUI == null) {
            return;
        }
        List<ChatLine> drawnChatLines = ingameGUI.getChatGUI().drawnChatLines;
        for (ChatLine chatLine : drawnChatLines) {
            if (!handled.add(chatLine)) {
                continue;
            }

            List<ITextComponent> siblings = chatLine.getChatComponent().getSiblings();
            for (int i = 0; i < siblings.size(); i++) {
                ITextComponent sibling = siblings.get(i);
                HoverEvent event = sibling.getStyle().getHoverEvent();
                if (event == null
                        || event.getAction() != SHOW_ENTITY
                        || !USERNAME_COLORED.equals(sibling.getUnformattedComponentText())
                        || !event.getValue().getUnformattedComponentText().contains("f98e9365-2c52-48c5-8647-6662f70b7e3d")) {
                    continue;
                }

                ITextComponent text = new TextComponentString("") {
                    @Override
                    public int hashCode() {
                        // their hashCode is literally broken and throws NPEs lol
                        return System.identityHashCode(this);
                    }
                };
                text.appendSibling(generateName());
                names.add(text);
                siblings.set(i, text);
                break;
            }
        }
    }

    @SubscribeEvent
    public static void on(PlayerEvent.NameFormat e) {
        // this is to prevent it from being white for half a tick and to appear purple in other places
        UUID id = e.getEntityPlayer().getGameProfile().getId();
        if (id.getMostSignificantBits() == 0xf98e93652c5248c5L
                && id.getLeastSignificantBits() == 0x86476662f70b7e3dL) {
            e.setDisplayname(USERNAME_COLORED);
        }
    }

    private static ITextComponent generateName() {
        ThreadLocalRandom rng = ThreadLocalRandom.current();

        ITextComponent text;

        // add a possibility for username with no obf letters to show up
        if (rng.nextInt(8) != 0) {

            // up to 4 characters are obf
            int[] indices = new int[4];
            for (int i = 0; i < indices.length; i++) {
                indices[i] = rng.nextInt(USERNAME.length());
            }

            int ptr = indices.length - 1;

            StringBuilder sb = new StringBuilder("§d");

            char[] charArray = USERNAME.toCharArray();
            for (int i = 0; i < charArray.length; i++) {
                // either we ended with obf indices or current index != next obf index
                if (ptr == 0 || indices[ptr] != i) {
                    sb.append(charArray[i]);
                    continue;
                }
                // else if we hit an obf index, consume it and add an obf letter
                --ptr;
                sb.append("§k").append(charArray[i]).append("§r§d");
            }

            text = new TextComponentString(sb.toString());
        } else {
            text = new TextComponentString(USERNAME_COLORED);
        }

        // copying default vanilla stuff
        text.getStyle()
                .setInsertion(USERNAME)
                .setClickEvent(new ClickEvent(SUGGEST_COMMAND, "/msg " + USERNAME + " "));

        // except an easter egg inside of an easter egg, custom dump hover text
        EntityPlayer observer = Minecraft.getMinecraft().player;
        if (observer == null) {
            return text;
        }
        text.getStyle().setHoverEvent(
                new HoverEvent(SHOW_TEXT, new TextComponentString("")
                        .appendText("Trying to peep at\nmy entity, ")
                        .appendSibling(observer.getDisplayName())
                        .appendText("?\n\n   ")
                        .appendSibling(new TextComponentString("Naughty.").setStyle(new Style().setItalic(true)))));
        return text;
    }
}
