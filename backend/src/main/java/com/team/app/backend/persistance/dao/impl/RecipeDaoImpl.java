package com.team.app.backend.persistance.dao.impl;

import com.team.app.backend.dto.RecipeFilterDto;
import com.team.app.backend.persistance.dao.RecipeDao;
import com.team.app.backend.persistance.dao.mappers.RecipeRowMapper;
import com.team.app.backend.persistance.model.Recipe;
import com.team.app.backend.persistance.model.RecipeWithContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

@Repository
public class RecipeDaoImpl implements RecipeDao {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    private RecipeRowMapper recipeRowMapper;


    public RecipeDaoImpl(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }


    @Override
    public long add(Recipe recipe, Long user_id) {
        String sql="INSERT INTO recipes( title, description, image, author_id, approved) VALUES (?, ?, ?, ?, ?);";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(sql, new String[] {"id"});
                    ps.setString(1, recipe.getTitle());
                    ps.setString(2, recipe.getDescription());
                    ps.setString(3, recipe.getImageUrl());
                    ps.setLong(4, user_id);
                    ps.setBoolean(5, false);
                    return ps;
                },
                keyHolder);
        return  keyHolder.getKey().longValue();
    }

    @Override
    public void update(RecipeWithContent recipe) {

    }

    @Override
    public void delete(Long id) {
        jdbcTemplate.update(
                "DELETE from recipes where id = ?",
                id
        );
    }

    @Override
    public Recipe get(Long id) {
        return jdbcTemplate.queryForObject("SELECT r.id,r.title,r.description,r.image,u.username author, COALESCE(likes_count,0) - COALESCE(dislikes_count,0) as rating FROM recipes r INNER JOIN users u ON r.author_id = u.id " +
                        "full outer join (SELECT REC_ID, COUNT(is_liked) as likes_count FROM user_to_rec WHERE is_liked = true GROUP BY REC_ID, is_Liked) as likes on likes.rec_id = r.id\n" +
                        "full outer join (SELECT REC_ID, COUNT(is_liked) as dislikes_count FROM user_to_rec WHERE is_liked = false GROUP BY REC_ID, is_Liked) as dislike on r.id = dislike.rec_id " +
                        "WHERE r.id = ?",
                new Object[]{id},
                recipeRowMapper);
    }

    @Override
    public List<Recipe> getAll() {
        return jdbcTemplate.query("SELECT r.id,r.title,r.description,r.image,u.username author , COALESCE(likes_count,0) - COALESCE(dislikes_count,0) as rating\n" +
                        "FROM recipes r INNER JOIN users u ON r.author_id = u.id\n" +
                        "full outer join (SELECT REC_ID, COUNT(is_liked) as likes_count FROM user_to_rec WHERE is_liked = true GROUP BY REC_ID, is_Liked) as likes on likes.rec_id = r.id\n" +
                        "full outer join (SELECT REC_ID, COUNT(is_liked) as dislikes_count FROM user_to_rec WHERE is_liked = false GROUP BY REC_ID, is_Liked) as dislike on r.id = dislike.rec_id " +
                        "WHERE r.approved = true",
                recipeRowMapper);
    }

    @Override
    public List<Recipe> getNotApproved() {
        return jdbcTemplate.query("SELECT r.id,r.title,r.description,r.image,u.username author ,0 as rating FROM recipes r INNER JOIN users u ON r.author_id = u.id WHERE r.approved = false",
                recipeRowMapper);
    }

    @Override
    public List<Recipe> getRecipesByCategory(String category) {
        return jdbcTemplate.query("SELECT DISTINCT(r.id),r.title,r.description,r.image,u.username author FROM recipes r INNER JOIN users u ON r.author_id = u.id INNER JOIN rec_to_categ rtc ON r.id = rtc.rec_id INNER JOIN recipe_categories cat ON cat.id = rtc.cat_id where cat.name = ? and r.approved = true",
                new Object[] { category },
                recipeRowMapper);    }

    @Override
    public List<Recipe> getRecipesBySearchStr(String searchStr) {

        searchStr="%"+searchStr+"%";

        return jdbcTemplate.query("SELECT DISTINCT(r.id),r.title,r.description,r.image,u.username author FROM recipes r INNER JOIN users u ON r.author_id = u.id WHERE LOWER(r.title) like ? or r.description like ? and r.approved = true;",
                new Object[] { searchStr,searchStr },
                recipeRowMapper);
    }

    @Override
    public List<Recipe> findFilteredRecipe(RecipeFilterDto recipeFilterDto) {
        System.out.println();
        String sql = "SELECT DISTINCT(r.id),r.title,r.description,r.image,u.username author, COALESCE(likes_count,0) - COALESCE(dislikes_count,0) as rating \n" +
                "FROM recipes r INNER JOIN users u ON r.author_id = u.id \n" +
                "full outer join (SELECT REC_ID, COUNT(is_liked) as likes_count FROM user_to_rec WHERE is_liked = true GROUP BY REC_ID, is_Liked) as likes on likes.rec_id = r.id\n" +
                "full outer join (SELECT REC_ID, COUNT(is_liked) as dislikes_count FROM user_to_rec WHERE is_liked = false GROUP BY REC_ID, is_Liked) as dislike on r.id = dislike.rec_id " +
                "INNER JOIN ingred_to_rec itc ON itc.rec_id = r.id\n" +
                "INNER JOIN rec_to_categ rtc ON rtc.rec_id = r.id\n" +
                "WHERE r.approved = true and (LOWER(r.title) like :query or LOWER(r.description) like :query) \n";
        MapSqlParameterSource parameters = new MapSqlParameterSource();

        if (recipeFilterDto.getQuery() != null) {
            parameters.addValue("query", "%" + recipeFilterDto.getQuery().toLowerCase() + "%");
        } else {
            parameters.addValue("query", "%%");
        }
        List<Integer> catList = new ArrayList<>();
        if (recipeFilterDto.getCategory() != null) {
            for (String s : recipeFilterDto.getCategory()) catList.add(Integer.valueOf(s));
            sql += "AND rtc.cat_id IN (:categories)";
        }
        parameters.addValue("categories", catList);
        List<Integer> ingrList = new ArrayList<>();
        if (recipeFilterDto.getIngredients() != null) {
            for (String s : recipeFilterDto.getIngredients()) ingrList.add(Integer.valueOf(s));
            sql += "AND itc.ingr_id IN (:ingredients)";

        }
        parameters.addValue("ingredients", ingrList);
        NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(jdbcTemplate.getDataSource());

        return template.query(sql,
                parameters,
                recipeRowMapper);

    }

    @Override
    public void approve(Long id) {
        jdbcTemplate.update(
                "UPDATE recipes SET approved = ? WHERE id = ?",
                true,
                id
        );
    }


    @Override
    public void likeRecipe(Long rec_id, Long user_id, boolean is_liked) {

        String sql="INSERT INTO user_to_rec( rec_id, user_id, is_liked) VALUES (?, ?, ?) ON CONFLICT (rec_id, user_id) DO UPDATE SET is_liked = excluded.is_liked;";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(sql);
                    ps.setLong(1, rec_id);
                    ps.setLong(2, user_id);
                    ps.setBoolean(3, is_liked);
                    return ps;
                },
                keyHolder);
    }
}
