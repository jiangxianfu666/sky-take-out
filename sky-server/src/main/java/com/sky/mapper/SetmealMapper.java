package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SetmealMapper {
    /**
     * 根据分类id查询套餐的数量
     * @param id
     * @return
     */
    // 改法1：sql里面改成 #{id}
    @Select("select count(id) from setmeal where category_id = #{id}")
    Integer countByCategoryId(Long id);

    // 改法2（推荐）：参数上加@Param("categoryId")
    //@Select("select count(id) from setmeal where category_id = #{categoryId}")
    //Integer countByCategoryId(@Param("categoryId") Long id);
}