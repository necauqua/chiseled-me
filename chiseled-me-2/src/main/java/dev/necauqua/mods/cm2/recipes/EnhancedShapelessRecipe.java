package dev.necauqua.mods.cm2.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

public final class EnhancedShapelessRecipe extends ShapelessRecipe {

    @Nullable
    private final ICondition condition;
    private final boolean special;
    @Nullable
    private final ItemStack[] remainingItems;

    public EnhancedShapelessRecipe(ResourceLocation id, String group, ItemStack result, NonNullList<Ingredient> recipeItems, @Nullable ICondition condition, boolean special, @Nullable ItemStack[] remainingItems) {
        super(id, group, result, recipeItems);
        this.condition = condition;
        this.special = special;
        this.remainingItems = remainingItems;
    }

    @Override
    public boolean matches(CraftingInventory inventory, World world) {
        return (condition == null || condition.test()) && super.matches(inventory, world);
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInventory inventory) {
        NonNullList<ItemStack> items = super.getRemainingItems(inventory);
        if (remainingItems == null) {
            return items;
        }
        NonNullList<Ingredient> ingredients = getIngredients();
        for (int i = 0; i < ingredients.size(); i++) {
            ItemStack remainingItem = remainingItems[i];
            if (remainingItem == null || remainingItem.isEmpty()) {
                continue;
            }
            Ingredient ingredient = ingredients.get(i);
            for (int j = 0; j < items.size(); ++j) {
                if (ingredient.test(inventory.getItem(j))) {
                    items.set(j, remainingItem.copy());
                }
            }
        }
        return items;
    }

    // test the condition to also hide it from the recipe book - this part requires a relogin, so not 100% dynamic :(
    @Override
    public boolean isSpecial() {
        return super.isSpecial() || special || condition != null && !condition.test();
    }

    public static class Serializer extends ShapelessRecipe.Serializer {

        @Override
        public ShapelessRecipe fromJson(ResourceLocation id, JsonObject json) {
            ShapelessRecipe recipe = super.fromJson(id, json);
            ICondition condition = json.has("condition") ?
                CraftingHelper.getCondition(JSONUtils.getAsJsonObject(json, "condition")) :
                null;
            boolean special = json.has("special") && json.get("special").getAsBoolean();

            ItemStack[] remainingItems = new ItemStack[recipe.getIngredients().size()];
            if (json.has("remaining_items")) {
                JsonArray array = JSONUtils.getAsJsonArray(json, "remaining_items");

                for (int i = 0; i < array.size(); ++i) {
                    JsonElement element = array.get(i);
                    if (element.isJsonNull()) {
                        continue;
                    }
                    remainingItems[i] = element.isJsonPrimitive() ?
                        new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(element.getAsString()))) :
                        CraftingHelper.getItemStack(element.getAsJsonObject(), true);
                }
            }

            return new EnhancedShapelessRecipe(id, recipe.getGroup(), recipe.getResultItem(), recipe.getIngredients(), condition, special, remainingItems);
        }

        @SuppressWarnings("ConstantConditions") // recipe is never null
        @Override
        public ShapelessRecipe fromNetwork(ResourceLocation id, PacketBuffer payload) {
            ShapelessRecipe recipe = super.fromNetwork(id, payload);
            String conditionJson = payload.readUtf();
            ICondition condition = conditionJson.isEmpty() ?
                null :
                CraftingHelper.getCondition(new JsonParser().parse(conditionJson).getAsJsonObject());
            boolean special = payload.readBoolean();
            ItemStack[] remainingItems = new ItemStack[payload.readVarInt()];
            for (int i = 0; i < remainingItems.length; i++) {
                remainingItems[i] = payload.readItem();
            }
            return new EnhancedShapelessRecipe(id, recipe.getGroup(), recipe.getResultItem(), recipe.getIngredients(), condition, special, remainingItems);
        }

        @Override
        public void toNetwork(PacketBuffer buffer, ShapelessRecipe recipe) {
            super.toNetwork(buffer, recipe);
            EnhancedShapelessRecipe enhanced = (EnhancedShapelessRecipe) recipe;
            buffer.writeUtf(enhanced.condition != null ? CraftingHelper.serialize(enhanced.condition).toString() : "");
            buffer.writeBoolean(enhanced.special);
            if (enhanced.remainingItems == null) {
                buffer.writeVarInt(0);
                return;
            }
            buffer.writeVarInt(enhanced.remainingItems.length);
            for (ItemStack remainingItem : enhanced.remainingItems) {
                buffer.writeItemStack(remainingItem != null ? remainingItem : ItemStack.EMPTY, false);
            }
        }
    }
}
