package com.ophis.beantranslater.commonsqlmapper.method;

import org.apache.ibatis.mapping.SqlCommandType;

import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;

public class UpdateCommon extends AbstractCommonMethod
{
	@Override
	public String getSql(TableInfo tableInfo)
	{
		SqlMethod sqlMethod = SqlMethod.UPDATE;
		String sql = String.format(sqlMethod.getSql(), tableInfo.getTableName(),
				sqlSet(true, true, tableInfo, true, ENTITY, ENTITY_DOT), sqlWhereEntityWrapper(true, tableInfo),
				sqlComment());
		return sql;
	}
	

	
	@Override
	public SqlCommandType getSqlCommandType() {
		return SqlCommandType.UPDATE;
	}
	
	
}
