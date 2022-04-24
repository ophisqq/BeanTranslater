package com.ophis.beantranslater.commonsqlmapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import com.ophis.beantranslater.commonsqlmapper.exception.CommonSQLException;
import com.ophis.beantranslater.commonsqlmapper.exception.MapperNotFoundException;
import com.ophis.beantranslater.commonsqlmapper.method.*;
import com.ophis.beantranslater.tools.BeanTools;
import com.ophis.beantranslater.tools.utils.TypeUtil;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionUtils;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@SuppressWarnings({"rawtypes","unchecked"})
/**
 * 通用mapper。
 * 无须使用特定的mapper执行mybatis plus的通用方法，减少过多的mapper引入，并支持动态实体操作
 * 通过传入的实体类自行查找对应mapper执行方法，若未找到对应mapper则自动生成sql执行
 * @author ophis
 *
 */
public final class CommonSqlMapper
{
	private static final long serialVersionUID = 1L;


	/**
	 * 根据 condition 条件，决定是否进行查询，false的话直接返回elseValue
	 * @param tableClass 实体类型（表）
	 * @param queryWrapper 实体对象封装操作类（可以为 null）
	 */
	public <T> List<T> conditionSelectList(Boolean condition,List<T> elseValue,Class<T> tableClass,Wrapper<T> queryWrapper)
	{
		if(condition)
		{
			return selectList(tableClass,queryWrapper);
		}
		else
		{
			return elseValue;
		}
	}
    /**
     * 根据 Wrapper 条件，查询全部记录
     * @param tableClass 实体类型（表）
     * @param queryWrapper 实体对象封装操作类（可以为 null）
     */
	public <T> List<T> selectList(Class<T> tableClass,Wrapper<T> queryWrapper)
	{
		try
		{
			return runMapperFunctionByTableClass(tableClass,mapper->
			{
				return mapper.selectList(queryWrapper);
			});
		}
		catch(MapperNotFoundException e)
		{
			String msId = createStatement(tableClass,new SelectListCommon(),SqlMethod.SELECT_LIST);
			Map param=new HashMap();
			param.put(Constants.WRAPPER, queryWrapper);
			List<T> result=runSqlSession(sqlSession->{
				return sqlSession.selectList(msId, param);
			});
			return result;
		}
	}
	
	/**
     * 根据 Wrapper 条件，查询全部记录
     * @param tableClass 实体类型（表）
     * @param queryWrapper 实体对象封装操作类（可以为 null）
     */
	public List<Map<String, Object>> selectMaps(Class tableClass,Wrapper queryWrapper)
	{
		try
		{
			return runMapperFunctionByTableClass(tableClass,mapper->
			{
				return mapper.selectMaps(queryWrapper);
			});
		}
		catch(MapperNotFoundException e)
		{
			String msId = createStatement(tableClass,new SelectListCommon(),SqlMethod.SELECT_MAPS);
			Map param=new HashMap();
			param.put(Constants.WRAPPER, queryWrapper);
			List<Map<String, Object>> result=runSqlSession(sqlSession->{
				return sqlSession.selectList(msId, param);
			});
			return result;
		}
		
	}
	 /**
     * 根据 Wrapper 条件，查询第一条记录
     * @param tableClass 实体类型（表）
     * @param queryWrapper 实体对象封装操作类（可以为 null）
     */
	public <T> T selectOne(Class<T> tableClass,Wrapper<T> queryWrapper)
	{
		List<T> list = selectList(tableClass, queryWrapper);
		return list!=null&&list.size()>0?list.get(0):null;
	}
	/**
	 * 根据Wrapper 条件，togetherFields设定的聚合参数，groupbyField指定的group by（可为null）字段，查询聚合记录
	 * @param togetherFields 聚合参数字段 max min sum count groupconcat等,nothing为不添加函数,其中sum会自动添加空判断 例 TogetherFields.newInstance().count("id").sum("money").nothing("name")
	 * @param queryWrapper 实体对象封装操作类（可以为 null）
	 * @param groupbyField group by 字段（可以为 null）
	 * @param togetherClass 聚合查询的表对应实体类
	 * @return
	 */
//	public List<Map<String, Object>> selectMapTogetherByWrapper(TogetherFields togetherFields, QueryWrapper queryWrapper, String groupbyField, Class togetherClass)
//	{
//		if(queryWrapper==null)
//		{
//			queryWrapper=new QueryWrapper();
//		}
//		TogetherCreater.makeTogetherWrapper(queryWrapper, togetherFields,groupbyField,togetherClass);
//		return selectMaps(togetherClass,queryWrapper);
//	}
	/**
	 * 根据Wrapper 条件，togetherFields设定的聚合参数，groupbyField指定的group by（可为null）字段，查询聚合记录,聚合查询的表由queryWrapper的泛型决定
	 * @param togetherFields 聚合参数字段 max min sum count groupconcat等,nothing为不添加函数,其中sum会自动添加空判断 例 TogetherFields.newInstance().count("id").sum("money").nothing("name")
	 * @param queryWrapper 实体对象封装操作类（可以为 null）
	 * @param groupbyField group by 字段（可以为 null）
	 * @return
	 */
//	public <T> List<T> selectListTogetherByWrapper(TogetherFields togetherFields,QueryWrapper<T> queryWrapper,String groupbyField)
//	{
//		if(queryWrapper==null)
//		{
//			queryWrapper=new QueryWrapper<T>();
//		}
//		Class togetherClass;
//		togetherClass = TypeUtil.getObjectTypeArgument(queryWrapper);
//		if(togetherClass==null)
//		{
//			togetherClass= TypeUtil.getObjectTypeArgument(queryWrapper);
//		}
//		if(togetherClass==null)
//		{
//			throw new CommonSQLException("queryWrapper没有指定泛型");
//		}
//		TogetherCreater.makeTogetherWrapper(queryWrapper, togetherFields,groupbyField,togetherClass);
//		return selectList(togetherClass,queryWrapper);
//	}
	/**
	 * 根据Wrapper 条件，togetherFields设定的聚合参数，groupbyField指定的group by（可为null）字段，查询聚合记录
	 * @param togetherFields 聚合参数字段 max min sum count groupconcat等,nothing为不添加函数,其中sum会自动添加空判断 例 TogetherFields.newInstance().count("id").sum("money").nothing("name")
	 * @param queryWrapper 实体对象封装操作类（可以为 null）
	 * @param groupbyField group by 字段（可以为 null）
	 * @param togetherClass 聚合查询的表对应实体类
	 * @return
	 */
//	public Map<String, Object> selectOneMapTogetherByWrapper(TogetherFields togetherFields,QueryWrapper queryWrapper,String groupbyField,Class togetherClass)
//	{
//		List<Map<String, Object>> list = selectMapTogetherByWrapper(togetherFields, queryWrapper, groupbyField, togetherClass);
//		return list!=null&&list.size()>0?list.get(0):null;
//	}
	/**
	 * 根据Wrapper 条件，togetherFields设定的聚合参数，groupbyField指定的group by（可为null）字段，查询聚合记录,聚合查询的表由queryWrapper的泛型决定
	 * @param togetherFields 聚合参数字段 max min sum count groupconcat等,nothing为不添加函数,其中sum会自动添加空判断 例 TogetherFields.newInstance().count("id").sum("money").nothing("name")
	 * @param queryWrapper 实体对象封装操作类（可以为 null）
	 * @param groupbyField group by 字段（可以为 null）
	 * @return
	 */
//	public <T> T selectOneTogetherByWrapper(TogetherFields togetherFields,QueryWrapper<T> queryWrapper,String groupbyField)
//	{
//		List<T> list = selectListTogetherByWrapper(togetherFields, queryWrapper, groupbyField);
//		return list!=null&&list.size()>0?list.get(0):null;
//	}
	/**
     * 插入一条记录
     *
     * @param entity 实体对象
     */
	public <T> int insert(T entity)
	{
		Class tableClass=entity.getClass();
		try
		{
			return runMapperFunctionByTableClass(tableClass,mapper->
			{
				return mapper.insert(entity);
			});
		}
		catch(MapperNotFoundException e)
		{
			String msId = createStatement(tableClass,new InsertCommon(),SqlMethod.INSERT_ONE);
			return runSqlSession(sqlSession->{
				return sqlSession.insert(msId, entity);
			});
		}
	}
	/**
     * 批量插入整个list的实体对象
     *
     * @param list 实体对象
     */
	public <T> int batchInsert(List<T> list)
	{
		return batchInsert(list, 200);
	}
	/**
     * 批量插入整个list的实体对象，可指定每次插入的数量
     *
     * @param list 实体对象
     * @param size 分多次插入，每次插入数量
     */
	public <T> int batchInsert(List<T> list,int size)
	{
		if(list==null||list.size()==0)
		{
			return 0;
		}
		if(size<2)
		{
			throw new CommonSQLException("批量插入缓存大小不能小于2");
		}
		Class tableClass=list.get(0).getClass();
		
		Stack<T> stack=new Stack<T>();
		for(int i=list.size()-1;i>=0;i--)
		{
			stack.push(list.get(i));
		}
		
		int result=0;
		while(stack.size()!=0)
		{
			List<T> caches=new ArrayList<T>();
			for(int i=0;i<size;i++)
			{
				if(stack.size()==0)
				{
					break;
				}
				caches.add(stack.pop());
			}
			if(caches.size()!=0)
			{
				try
				{
					
					result+=runMapperFunctionByTableClass(tableClass, mapper->
					{
						
						if(mapper!=null&&ExtBaseMapper.class.isAssignableFrom(mapper.getClass()))
				    	{
							return ((ExtBaseMapper)mapper).batchInsert(caches);
				    	}
						else
						{
							throw new MapperNotFoundException("非ExtBaseMapper");
						}
					});
					
				}
				catch(MapperNotFoundException e)
				{
					String msId = createStatement(tableClass,new BatchInsertCommon(),SqlMethod.INSERT_ONE);
					Map param=new HashMap();
					param.put("list", caches);
					result+=runSqlSession(sqlSession->{
						return sqlSession.insert(msId, param);
					});
				}
			}
		}
		return result;
	}
	 /**
     * 根据 wrapper 条件，删除记录
     * @param tableClass 实体类型(表)
     * @param wrapper 实体对象封装操作类（可以为 null,里面的 entity 用于生成 where 语句）
     */
	public <T> int delete(Class<T> tableClass,Wrapper<T> wrapper)
	{
		try
		{
			return runMapperFunctionByTableClass(tableClass,mapper->
			{
				return mapper.delete(wrapper);
			});
		}
		catch(MapperNotFoundException e)
		{
			TableInfo tableInfo=getTableInfo(tableClass);
			String msId = createStatement(tableClass,new DeleteCommon(),tableInfo.isLogicDelete()?SqlMethod.LOGIC_DELETE:SqlMethod.DELETE);
			Map param=new HashMap();
			param.put(Constants.WRAPPER, wrapper);
			int result=0;
			if(tableInfo.isLogicDelete())
			{
				result=runSqlSession(sqlSession->{
					return sqlSession.update(msId, param);
				});
			}
			else
			{
				result=runSqlSession(sqlSession->{
					return sqlSession.delete(msId, param);
				});
			}
			return result;
		}
	}
	
	
	  /**
     * 根据 ID 删除
     *
     * @param id 主键ID
     */
    public <T> int deleteById(Class<T> tableClass,Serializable id)
    {
    	try
		{
			return runMapperFunctionByTableClass(tableClass,mapper->
			{
				return mapper.deleteById(id);
			});
		}
		catch(MapperNotFoundException e)
		{
	    	UpdateWrapper<T> wrapper = new UpdateWrapper<T>();
	    	TableInfo tableInfo=getTableInfo(tableClass);
	    	wrapper.eq(tableInfo.getKeyColumn(), id);
	    	return delete(tableClass, wrapper);
		}
    }
    
    /**
     * 删除（根据ID 批量删除）
     *
     * @param idList 主键ID列表(不能为 null 以及 empty)
     */
    public <T> int deleteBatchIds(Class<T> tableClass,Collection<? extends Serializable> idList)
    {
    	if(idList!=null&&idList.size()>0)
    	{
    		try
    		{
    			return runMapperFunctionByTableClass(tableClass,mapper->
    			{
    			 	UpdateWrapper<T> wrapper = new UpdateWrapper<T>();
        	    	TableInfo tableInfo=getTableInfo(tableClass);
        	    	wrapper.in(tableInfo.getKeyColumn(), idList);
    				return mapper.delete(wrapper);
    			});
    		}
    		catch(MapperNotFoundException e)
    		{
    		  	UpdateWrapper<T> wrapper = new UpdateWrapper<T>();
    	    	TableInfo tableInfo=getTableInfo(tableClass);
    	    	wrapper.in(tableInfo.getKeyColumn(), idList);
    	    	return delete(tableClass, wrapper);
			}  
    	}
    	return 0;
    }
    
    /**
     * 根据 ID 查询
     *
     * @param id 主键ID
     */
    public <T> T selectById(Class<T> tableClass,Serializable id)
    {
    	try
		{
			return runMapperFunctionByTableClass(tableClass,mapper->
			{
				return (T)mapper.selectById(id);
			});
		}
		catch(MapperNotFoundException e)
		{
			
		} 
    	QueryWrapper<T> wrapper = new QueryWrapper<T>();
    	TableInfo tableInfo=getTableInfo(tableClass);
    	wrapper.eq(tableInfo.getKeyColumn(), id);
    	return selectOne(tableClass, wrapper);
    }

    /**
     * 查询（根据ID 批量查询）
     *
     * @param idList 主键ID列表(不能为 null 以及 empty)
     */
    public <T> List<T> selectBatchIds(Class<T> tableClass,Collection<? extends Serializable> idList)
	{
    	if(idList!=null&&idList.size()>0)
    	{
    		try
    		{
    			return runMapperFunctionByTableClass(tableClass,mapper->
    			{
    				return mapper.selectBatchIds(idList);
    			});
    		}
    		catch(MapperNotFoundException e)
    		{
    			QueryWrapper<T> wrapper = new QueryWrapper<T>();
    	    	TableInfo tableInfo=getTableInfo(tableClass);
    	    	wrapper.in(tableInfo.getKeyColumn(), idList);
    	    	return selectList(tableClass, wrapper);
			}  
    	}
    	return new ArrayList<T>();
    }
    
    /**
     * 根据 whereEntity 条件，更新记录
     *
     * @param entity        实体对象 (set 条件值,可以为 null)
     * @param updateWrapper 实体对象封装操作类（可以为 null,里面的 entity 用于生成 where 语句）
     */
    public <T> int update(T entity,Wrapper<T> updateWrapper)
    {
		Class tableClass=null;
//    	if(updateWrapper instanceof ExtUpdateWrapper){
//			ExtUpdateWrapper wrapper=(ExtUpdateWrapper)updateWrapper;
//			tableClass=wrapper.getEntityClass();
//		}
//    	else{
			if(entity==null){
				return 0;
			}
			tableClass=entity.getClass();
//    	}
    	try
		{
			return runMapperFunctionByTableClass(tableClass,mapper->
			{
				return mapper.update(entity, updateWrapper);
			});
		}
		catch(MapperNotFoundException e)
		{
			String msId = createStatement(tableClass,new UpdateCommon(),SqlMethod.UPDATE);
			Map param=new HashMap();
			param.put(Constants.ENTITY, entity);
			param.put(Constants.WRAPPER, updateWrapper);
			return runSqlSession(sqlSession->{
				return sqlSession.update(msId, param);
			});
		}
	
    }
    
    /**
     * 根据 whereEntity 条件，选择更新记录 值为null的字段不会更新
     * @param entity  实体对象 (set 条件值,可以为 null)
     * @param updateWrapper 实体对象封装操作类（可以为 null,里面的 entity 用于生成 where 语句）
     * @return
     */
    public <T> int updateSelective(T entity,Wrapper<T> updateWrapper)
    {
    	if(entity==null)
    	{
    		return 0;
    	}
    	Class tableClass=entity.getClass();
    	try
		{
			return runMapperFunctionByTableClass(tableClass,mapper->
			{
				if(mapper!=null&&ExtBaseMapper.class.isAssignableFrom(mapper.getClass()))
		    	{
		    		return ((ExtBaseMapper)mapper).updateSelective(entity, updateWrapper);
		    	}
				else
				{
					throw new MapperNotFoundException("非ExtBaseMapper");
				}
			});
		}
		catch(MapperNotFoundException e)
		{
			String msId = createStatement(tableClass,new UpdateSelectiveCommon(),SqlMethod.UPDATE);
    		Map param=new HashMap();
    		param.put(Constants.ENTITY, entity);
    		param.put(Constants.WRAPPER, updateWrapper);
    		return runSqlSession(sqlSession->{
    			return sqlSession.update(msId, param);
    		});
		}
    }
    /**
     * 根据 主键，选择更新记录 值为null的字段不会更新
     * @param entity  实体对象 (set 条件值,可以为 null)
     * @return
     */
    public <T> int updateByIdSelective(T entity)
    {
    	if(entity==null)
    	{
    		return 0;
    	}
    	Class tableClass=entity.getClass();
    	try
		{
			return runMapperFunctionByTableClass(tableClass,mapper->
			{
				if(mapper!=null&&ExtBaseMapper.class.isAssignableFrom(mapper.getClass()))
		    	{
					return ((ExtBaseMapper)mapper).updateByIdSelective(entity);
		    	}
				else
				{
					throw new MapperNotFoundException("非ExtBaseMapper");
				}
			});
		}
		catch(MapperNotFoundException e)
		{
			UpdateWrapper updateWrapper=new UpdateWrapper<T>();
    		TableInfo tableInfo=getTableInfo(tableClass);
    		Object id= BeanTools.getProperty(entity, tableInfo.getKeyProperty());
    		updateWrapper.eq(tableInfo.getKeyColumn(), id);
			return updateSelective(entity, updateWrapper);
    	}
    }
    /**
     * 根据 主键，更新记录 值为null的字段也会更新
     * @param entity  实体对象 (set 条件值,可以为 null)
     * @return
     */
    public <T> int updateById(T entity)
    {
    	if(entity==null)
    	{
    		return 0;
    	}
    	Class tableClass=entity.getClass();
    	try
		{
			return runMapperFunctionByTableClass(tableClass,mapper->
			{
				if(mapper!=null&&ExtBaseMapper.class.isAssignableFrom(mapper.getClass()))
				{
					return ((ExtBaseMapper)mapper).updateByIdAllColumns(entity);
				}
				else
				{
					throw new MapperNotFoundException("非ExtBaseMapper");
				}
			});
		}
		catch(MapperNotFoundException e)
		{
			UpdateWrapper updateWrapper=new UpdateWrapper<T>();
    		TableInfo tableInfo=getTableInfo(tableClass);
    		Object id=BeanTools.getProperty(entity, tableInfo.getKeyProperty());
    		updateWrapper.eq(tableInfo.getKeyColumn(), id);
			return update(entity, updateWrapper);
    	}
    }
    
//    /**
//     * 根据 entity 条件，查询全部记录（并翻页）
//     *
//     * @param page         分页查询条件（可以为 RowBounds.DEFAULT）
//     * @param queryWrapper 实体对象封装操作类（可以为 null）
//     */
//    public <E extends IPage,T> E selectPage(E page, ExtQueryWrapper<T> queryWrapper)
//    {
//    	if(queryWrapper==null)
//		{
//    		throw new CommonSQLException("queryWrapper没有指定泛型");
//		}
//		Class<T> tableClass=TypeUtil.getObjectTypeArgument(queryWrapper);
//		if(tableClass==null||tableClass==Object.class)
//		{
//			throw new CommonSQLException("queryWrapper没有指定泛型");
//		}
//
//		try
//		{
//			return runMapperFunctionByTableClass(tableClass,mapper->
//			{
//				return (E) mapper.selectPage(page, queryWrapper);
//			});
//		}
//		catch(MapperNotFoundException e)
//		{
//			String msId = createStatement(tableClass,new SelectListCommon(),SqlMethod.SELECT_PAGE);
//    		Map param=new HashMap();
//    		param.put(Constants.WRAPPER, queryWrapper);
//    		param.put("page", page);
//    		List<T> list=runSqlSession(sqlSession->{
//    			return sqlSession.selectList(msId, param);
//    		});
//    		page.setRecords(list);
//    		return page;
//    	}
//
//    }
    
    public <T> List<T> selectListWithXml(String xml,Map<String,Object> params,Class<T> resultType)
	{
    	TableInfo table =getAnyTableInfo();
    	Configuration configuration = table.getConfiguration();
		LanguageDriver languageDriver = configuration.getDefaultScriptingLanguageInstance();
		xml="<script>"+xml+"</script>";
		SqlSource sqlSource = languageDriver.createSqlSource(configuration, xml, Map.class);
		final Class type=resultType;
		String sqlId=SqlMethod.SELECT_LIST.getMethod()+xml.hashCode();
		MappedStatement.Builder builder = new MappedStatement.Builder(configuration,sqlId, sqlSource,SqlCommandType.SELECT)
        .resultMaps(new ArrayList<ResultMap>() {
            {
                add(new ResultMap.Builder(configuration, "defaultResultMap",type, new ArrayList<ResultMapping>(0)).build());
            }
        });
		MappedStatement ms =builder.build();
		//缓存
		if(!configuration.hasStatement(ms.getId()))
		{
			configuration.addMappedStatement(ms);
		}
		
		List<T> result=runSqlSession(sqlSession->{
			return (List<T>)sqlSession.selectList(ms.getId(), params);
		});
		return result;
	}

	private String createStatement(Class tableClass,AbstractCommonMethod commonMethod,SqlMethod sqlMethod)
	{
		TableInfo table = getTableInfo(tableClass);
		Configuration configuration = table.getConfiguration();
		LanguageDriver languageDriver = configuration.getDefaultScriptingLanguageInstance();
		SqlSource sqlSource = languageDriver.createSqlSource(configuration, commonMethod.getSql(table), tableClass);
		String msId = commonMethod.addStatement(table, sqlSource, sqlMethod);
		
		return msId;
	}

	private <R> R runMapperFunctionByTableClass(Class tableClass,Function<BaseMapper, R> runFunction)
	{
		if(tableClass==null)
		{
			throw new CommonSQLException("参数tableClass为null");
		}
		TableInfo table=getTableInfo(tableClass);
		SqlSessionFactory sqlSessionFactory=GlobalConfigUtils.getGlobalConfig(table.getConfiguration()).getSqlSessionFactory();
		SqlSession sqlSession=SqlSessionUtils.getSqlSession(sqlSessionFactory);
		BaseMapper baseMapper=null;
		try
		{
			List<Class<?>> mappers = sqlSession.getConfiguration().getMapperRegistry().getMappers().stream()
				.filter(mapper->BaseMapper.class.isAssignableFrom(mapper)).collect(Collectors.toList());
			mappers=mappers.stream().filter(mapper-> TypeUtil.getClassArgument(mapper,0)==tableClass).collect(Collectors.toList());
			
			if(mappers!=null&&mappers.size()==1)
			{
				baseMapper=(BaseMapper) sqlSession.getMapper(mappers.get(0));
			}
			if(baseMapper!=null)
			{
				return runFunction.apply(baseMapper);
			}
			else
			{
				throw new MapperNotFoundException("没有找到该类型对应的mapper");
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
		finally
		{
			SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
		}
	}
	private TableInfo getTableInfo(Class tableClass)
	{
		TableInfo table=null;
		table=TableInfoHelper.getTableInfo(tableClass);
		if(table==null)
		{
//			if(TableInfoHelper.getTableInfos()!=null&&TableInfoHelper.getTableInfos().size()>0)
//			{
//				table=TableInfoMaker.getTempTableInfo(TableInfoHelper.getTableInfos().get(0).getConfiguration(), tableClass);
//			}
			if(table==null)
			{
				throw new CommonSQLException("获取类型"+tableClass.getName()+"的表信息失败");
			}
		}
		return table;
	}
	private TableInfo getAnyTableInfo()
	{
		if(TableInfoHelper.getTableInfos()!=null&&TableInfoHelper.getTableInfos().size()>0)
		{
			return TableInfoHelper.getTableInfos().get(0);
		}
		return null;
	}
	private <R> R runSqlSession(Function<SqlSession, R> function)
	{
		TableInfo table=getAnyTableInfo();
		SqlSessionFactory sqlSessionFactory=GlobalConfigUtils.getGlobalConfig(table.getConfiguration()).getSqlSessionFactory();
		SqlSession sqlSession=SqlSessionUtils.getSqlSession(sqlSessionFactory);
		try
		{
			return function.apply(sqlSession);
		}
		finally 
		{
			SqlSessionUtils.closeSqlSession(sqlSession, sqlSessionFactory);
		}
		
	}
	
	
}
