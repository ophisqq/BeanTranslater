package com.ophis.beantranslater.commonsqlmapper.method;

import org.apache.ibatis.mapping.SqlCommandType;

import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;

public class SelectListCommon extends AbstractCommonMethod
{
	@Override
	public String getSql(TableInfo tableInfo)
	{
		SqlMethod sqlMethod = SqlMethod.SELECT_LIST;
		String sql = String.format(sqlMethod.getSql(), sqlFirst(), sqlSelectColumns(tableInfo, true),
				tableInfo.getTableName(), sqlWhereEntityWrapper(true, tableInfo), sqlComment());
		return sql;
	}

	@Override
	public SqlCommandType getSqlCommandType() {
		return SqlCommandType.SELECT;
	}
}
