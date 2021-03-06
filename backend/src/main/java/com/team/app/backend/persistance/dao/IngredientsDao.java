package com.team.app.backend.persistance.dao;

import com.team.app.backend.dto.IngredientDto;
import com.team.app.backend.persistance.model.Ingredient;
import com.team.app.backend.persistance.model.IngredientToRecipe;
import com.team.app.backend.persistance.model.Recipe;

import java.util.List;

public interface IngredientsDao {

    List<Ingredient> getAll();

    List<IngredientToRecipe> getRecipes(long id);

    void addRecipeIngred(long rec_id, List<IngredientDto>ingredients );

}
