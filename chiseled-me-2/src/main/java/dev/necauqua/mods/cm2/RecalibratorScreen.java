package dev.necauqua.mods.cm2;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.necauqua.mods.mira.api.IRenderSized;
import dev.necauqua.mods.mira.api.ISized;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.function.Predicate;

import static dev.necauqua.mods.cm2.ChiseledMe2.ns;
import static dev.necauqua.mods.mira.command.SizeArgumentType.sizeArg;

public final class RecalibratorScreen extends Screen {

    public static final ResourceLocation TEXTURE = ns("textures/gui/recalibrator.png");
    private final RecalibratorConfig config;

    private int left, top;

    public RecalibratorScreen(ItemStack stack) {
        super(new StringTextComponent("M-V Recalibrator 2.0"));
        config = new RecalibratorConfig(stack);
    }

    @Override
    public void init() {
        left = (width - 176) / 2;
        top = (height - 86) / 2;

        assert minecraft != null && minecraft.player != null;

//        addButton(new Button(left + 60, top + 16, 32, 20, new StringTextComponent("<< 1"), b -> {
//            Network.guiEvent(minecraft.player, GuiEvent.MUL);
//        }));
//        addButton(new Button(left + 100, top + 16, 32, 20, new StringTextComponent(">> 1"), b -> {
//            Network.guiEvent(minecraft.player, GuiEvent.DIV);
//        }));

        class BetterTextFieldWidget extends TextFieldWidget {

            public BetterTextFieldWidget(FontRenderer font, int x, int y, int w, int h, ITextComponent message) {
                super(font, x, y, w, h, message);
            }

            @Override
            public boolean mouseClicked(double mx, double my, int button) {
                boolean s = super.mouseClicked(mx, my, button);
                if (s) {
                    return true;
                }
                if (button == 1 && mx >= (double) x && mx < (double) (x + width) && my >= (double) y && my < (double) (y + height)) {
                    setValue("");
                    return true;
                }
                children.forEach(ch -> ch.changeFocus(false));
                return false;
            }
        }

        BetterTextFieldWidget sizeA = new BetterTextFieldWidget(font, left + 57, top + 13, 40, 10, new StringTextComponent(""));
        sizeA.setValue(String.valueOf(config.getSizeA()));
        Predicate<String> filterLol = s -> {
            try {
                sizeArg().parse(new StringReader(s));
                return true;
            } catch (CommandSyntaxException e) {
                return false;
            }
        };
        sizeA.setFilter(filterLol);
        sizeA.setResponder(s -> {
            try {
                config.setSizeA(Double.parseDouble(s));
            } catch (NumberFormatException ignored) {
            }
        });
        addWidget(sizeA);

        BetterTextFieldWidget sizeB = new BetterTextFieldWidget(font, left + 57, top + 26, 40, 10, new StringTextComponent(""));
        sizeB.setValue(String.valueOf(config.getSizeB()));
        sizeB.setFilter(filterLol);
        sizeB.setResponder(s -> {
            try {
                config.setSizeB(Double.parseDouble(s));
            } catch (NumberFormatException ignored) {
            }
        });
        addWidget(sizeB);
    }

    @Override
    public void tick() {
        children.forEach(b -> ((TextFieldWidget) b).tick());
    }

    @Override
    public void render(MatrixStack poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);

        assert minecraft != null; // intellij shut up
        ClientPlayerEntity player = minecraft.player;
        assert player != null;

        //noinspection deprecation
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);

        minecraft.getTextureManager().bind(TEXTURE);
        blit(poseStack, left, top, 0, 0, 176, 86);

        InventoryScreen.renderEntityInInventory(left + 30, top + 72, 30, (float) (left + 30) - mouseX, (float) (top + 72 - 50) - mouseY, player);

        minecraft.getTextureManager().bind(TEXTURE);
        blit(poseStack, left + 36, top + 8, 176, 0, 18, 70);

        MatrixStack fontStack = new MatrixStack();
        fontStack.last().pose().set(poseStack.last().pose());
        fontStack.scale(0.5f, 0.5f, 1.0f);

        float height = (float) (player.getBbHeight() / ((ISized) player).getSizeCM() * ((IRenderSized) player).getSizeCM(partialTick));
        font.draw(fontStack, String.valueOf(height), (left + 55) * 2, (top + 41) * 2, 0x404040);

        font.draw(fontStack, title, (left + 54) * 2, (top + 5) * 2, 0x404040);

        children.forEach(b -> ((TextFieldWidget) b).render(poseStack, mouseX, mouseY, partialTick));

        super.render(poseStack, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
