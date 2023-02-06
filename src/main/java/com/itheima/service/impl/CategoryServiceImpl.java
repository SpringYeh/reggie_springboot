package com.itheima.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.common.CustomException;
import com.itheima.entity.Category;
import com.itheima.entity.Dish;
import com.itheima.entity.Setmeal;
import com.itheima.mapper.CategoryMapper;
import com.itheima.service.CategoryService;
import com.itheima.service.DishService;
import com.itheima.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;

    /**
     * 根据id删除分类，删除之前先判断是否有关联
     * @param id
     */
    @Override
    public void remove(Long id) {
        // 查询当前分类是否关联了菜品
        LambdaQueryWrapper<Dish> dishqw = new LambdaQueryWrapper<>();
        dishqw.eq(Dish::getCategoryId,id);
        int count1 = dishService.count(dishqw);
        if(count1>0){   // 已经关联，抛出一个业务异常
            throw new CustomException("当前分类下关联了菜品，无法删除");
        }

        // 查询当前分类是否关联了套餐
        LambdaQueryWrapper<Setmeal> setmealqw = new LambdaQueryWrapper<>();
        setmealqw.eq(Setmeal::getCategoryId,id);
        int count2 = setmealService.count(setmealqw);
        if(count2>0){   // 已经关联，抛出一个业务异常
            throw new CustomException("当前分类下关联了套餐，无法删除");
        }

        // 正常删除分类（super或this都可以吧？？！）
        super.removeById(id);
    }
}
