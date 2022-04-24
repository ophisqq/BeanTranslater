package com.ophis.beantranslater.commonsqlmapper.method;

import org.apache.ibatis.mapping.SqlCommandType;

import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;

public class DeleteCommon extends AbstractCommonMethod
{
	@Override
	public String getSql(TableInfo tableInfo)
	{
		String sql;
        SqlMethod sqlMethod = SqlMethod.LOGIC_DELETE;
        if (tableInfo.isLogicDelete())
        {
            sql = String.format(sqlMethod.getSql(), tableInfo.getTableName(), sqlLogicSet(tableInfo),
                sqlWhereEntityWrapper(true, tableInfo),sqlComment());
        }
        else
        {
            sqlMethod = SqlMethod.DELETE;
            sql = String.format(sqlMethod.getSql(), tableInfo.getTableName(),
                sqlWhereEntityWrapper(true, tableInfo), sqlComment());
        }
        return sql;
	}
	

	
	@Override
	public SqlCommandType getSqlCommandType() {
		return SqlCommandType.DELETE;
	}
	
	
}
