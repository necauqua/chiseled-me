package dev.necauqua.mods.cm2.recipes;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition;

public final class EnhancedShapedRecipe extends ShapedRecipe {

    private final ICondition condition;
    private final boolean special;

    public EnhancedShapedRecipe(ResourceLocation id, String group, int width, int height, NonNullList<Ingredient> recipeItems, ItemStack result, ICondition condition, boolean special) {
        super(id, group, width, height, recipeItems, result);
        this.condition = condition;
        this.special = special;
    }

    @Override
    public boolean matches(CraftingInventory inventory, World world) {
        return (condition == null || condition.test()) && super.matches(inventory, world);
    }

    // test the condition to also hide it from the recipe book - this part requires a relogin, so not 100% dynamic :(
    @Override
    public boolean isSpecial() {
        return super.isSpecial() || special || !condition.test();
    }

    public static class Serializer extends ShapedRecipe.Serializer {

        @Override
        public ShapedRecipe fromJson(ResourceLocation id, JsonObject json) {
            ShapedRecipe recipe = super.fromJson(id, json);
            ICondition condition = json.has("condition") ?
                CraftingHelper.getCondition(JSONUtils.getAsJsonObject(json, "condition")) :
                null;
            boolean special = json.has("special") && json.get("special").getAsBoolean();
            return new EnhancedShapedRecipe(id, recipe.getGroup(), recipe.getWidth(), recipe.getHeight(), recipe.getIngredients(), recipe.getResultItem(), condition, special);
        }

        @SuppressWarnings("ConstantConditions") // recipe is never null
        @Override
        public ShapedRecipe fromNetwork(ResourceLocation id, PacketBuffer payload) {
            ShapedRecipe recipe = super.fromNetwork(id, payload);
            String conditionJson = payload.readUtf();
            ICondition condition = conditionJson.isEmpty() ?
                null :
                CraftingHelper.getCondition(new JsonParser().parse(conditionJson).getAsJsonObject());
            boolean special = payload.readBoolean();
            return new EnhancedShapedRecipe(id, recipe.getGroup(), recipe.getWidth(), recipe.getHeight(), recipe.getIngredients(), recipe.getResultItem(), condition, special);
        }

        @Override
        public void toNetwork(PacketBuffer buffer, ShapedRecipe recipe) {
            super.toNetwork(buffer, recipe);
            EnhancedShapedRecipe enhanced = (EnhancedShapedRecipe) recipe;
            buffer.writeUtf(enhanced.condition != null ? CraftingHelper.serialize(enhanced.condition).toString() : "");
            buffer.writeBoolean(enhanced.special);
        }
    }
}
