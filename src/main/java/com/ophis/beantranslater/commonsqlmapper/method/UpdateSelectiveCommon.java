package com.ophis.beantranslater.commonsqlmapper.method;

import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;
import com.ophis.beantranslater.commonsqlmapper.method.utils.SelectiveFieldsUtils;
import org.apache.ibatis.mapping.SqlCommandType;

public class UpdateSelectiveCommon extends AbstractCommonMethod
{
	@Override
	public String getSql(TableInfo tableInfo) 
	{
		SqlMethod sqlMethod = SqlMethod.UPDATE;
		String sql = String.format(sqlMethod.getSql(), tableInfo.getTableName(),
				sqlSetSelective(true, true, tableInfo, true, ENTITY, ENTITY_DOT),
				sqlWhereEntityWrapper(true, tableInfo), sqlComment());
		return sql;
	}
	
	@Override
	public String getStatementName(SqlMethod sqlMethod)
	{
		return "updateSelective";
	}
	
	protected String sqlSetSelective(boolean logic, boolean ew, TableInfo table,
			boolean judgeAliasNull, String alias, String prefix) 
	{
		String sqlScript = SelectiveFieldsUtils.getAllSqlSet(table,logic, prefix);
        if (judgeAliasNull) {
            sqlScript = SqlScriptUtils.convertIf(sqlScript, String.format("%s != null", alias), true);
        }
        if (ew) {
            sqlScript += NEWLINE;
            sqlScript += SqlScriptUtils.convertIf(SqlScriptUtils.unSafeParam(U_WRAPPER_SQL_SET),
                String.format("%s != null and %s != null", WRAPPER, U_WRAPPER_SQL_SET), false);
        }
        sqlScript = SqlScriptUtils.convertSet(sqlScript);
        return sqlScript;
	}

	@Override
	public SqlCommandType getSqlCommandType() 
	{
		return SqlCommandType.UPDATE;
	}
	
	
}
