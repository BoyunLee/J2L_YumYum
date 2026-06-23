package com.ssafy.yumyum.domain.refrigerator.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.yumyum.domain.refrigerator.entity.RefrigeratorItem;

@Mapper
public interface RefrigeratorItemDao {
    void createDefaultRefrigeratorIfAbsent(@Param("userId") Long userId);
    Long findRefrigeratorIdByUserId(@Param("userId") Long userId);
    void insertManual(RefrigeratorItem item);
    RefrigeratorItem findById(@Param("id") Long id);
    List<RefrigeratorItem> findAllByUserId(@Param("userId") Long userId);
    RefrigeratorItem findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
    int updateByIdAndUserId(@Param("item") RefrigeratorItem item, @Param("userId") Long userId);
    void deleteLogsByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
