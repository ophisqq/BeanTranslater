package com.ophis.beantranslater.commonsqlmapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExtBaseMapper<T> extends BaseMapper<T>
{
	/**
	 * 聚合查询
	 * @param queryWrapper
	 * @return
	 */
	//public List<Map<String, Object>> selectTogetherMapByWrapper(@Param(Constants.WRAPPER) TogetherWrapper<T> queryWrapper);
	
	/**
	 * 聚合查询
	 * @param queryWrapper
	 * @return
	 */
//	public List<T> selectTogetherListByWrapper(@Param(Constants.WRAPPER) TogetherWrapper<T> queryWrapper);
	 
	/**
     * 根据 updateWrapper 条件，条件更新记录
     *
     * @param entity        实体对象 (set 条件值,可以为 null)
     * @param updateWrapper 实体对象封装操作类（可以为 null,里面的 entity 用于生成 where 语句）
     */
	public int updateSelective(@Param(Constants.ENTITY) T entity, @Param(Constants.WRAPPER) Wrapper<T> updateWrapper);
    
    public int updateByIdSelective(@Param(Constants.ENTITY) T entity);


	public int updateAllColumns(@Param(Constants.ENTITY) T entity, @Param(Constants.WRAPPER) Wrapper<T> updateWrapper);

	public int updateByIdAllColumns(@Param(Constants.ENTITY) T entity);
    
    public int batchInsert(@Param("list") List<T> list);
    
    
}
