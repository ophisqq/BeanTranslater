package com.ophis.beantranslater.commonsqlmapper.method;

import java.util.ArrayList;
import java.util.Map;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.MappedStatement.Builder;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;

import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;


/**
 *
 * [功能描述]：通用commonsqlmapper的方法
 * @author	ophis
 * @version	1.0, 2021年3月31日下午5:27:30
 */
public abstract class AbstractCommonMethod extends AbstractMethod
{
	public abstract String getSql(TableInfo tableInfo);
	
	public abstract SqlCommandType getSqlCommandType();
	
	public void setPrimaryKeys(Builder builder, TableInfo tableInfo)
	{
		
	}
	
	public String getStatementName(SqlMethod sqlMethod)
	{
		return sqlMethod.getMethod();
	}
	
	public String addStatement(TableInfo tableInfo,SqlSource sqlSource,SqlMethod sqlMethod)
	{
		configuration=tableInfo.getConfiguration();
		Class tableClass=tableInfo.getEntityType();
		String msId=tableInfo.getCurrentNamespace()+"."+getStatementName(sqlMethod)+"Common"+tableClass.getName()+"";
		Class resultType=getSqlCommandType()==SqlCommandType.SELECT?tableClass:int.class;
		if(sqlMethod==SqlMethod.SELECT_MAPS)
		{
			resultType=Map.class;
		}
		final Class type=resultType;
		Builder builder = new Builder(configuration, msId, sqlSource, getSqlCommandType())
        .resultMaps(new ArrayList<ResultMap>() {
            {
                add(new ResultMap.Builder(configuration, "defaultResultMap",type, new ArrayList<ResultMapping>(0)).build());
            }
        });
		setPrimaryKeys(builder,tableInfo);
		MappedStatement ms =builder.build();
		//缓存
		if(!configuration.hasStatement(ms.getId()))
		{
			configuration.addMappedStatement(ms);
		}
		return ms.getId();
	}
	
	@Override
	public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
		return null;
	}
	
}
