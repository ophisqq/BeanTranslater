package com.ophis.beantranslater.commonsqlmapper.method.utils;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;

import java.util.List;

public class SelectiveFieldsUtils 
{
	public static String getAllSqlSet(TableInfo tableInfo,boolean ignoreLogicDelFiled, final String prefix) 
	{
		final String newPrefix = prefix == null ? TableInfo.EMPTY : prefix;
		List<TableFieldInfo> fieldList = tableInfo.getFieldList();
		String sql="";
		for(TableFieldInfo field:fieldList)
		{
			 if (ignoreLogicDelFiled)
			 {
				 if(tableInfo.isLogicDelete() && field.isLogicDelete())
				 {
					 continue;
				 }
             }
			 sql+=getSqlSet(field,newPrefix)+TableInfo.NEWLINE;
			 
		}
		return sql;
	 }
	
	private static String getSqlSet(TableFieldInfo tableFieldInfo, final String prefix)
	{
		final boolean ignoreIf=false;
        final String newPrefix = prefix == null ? TableFieldInfo.EMPTY : prefix;
        // 默认: column=
        String sqlSet = tableFieldInfo.getColumn() + TableFieldInfo.EQUALS;
        if (StringUtils.isNotBlank(tableFieldInfo.getUpdate())) {
            sqlSet += String.format(tableFieldInfo.getUpdate(), tableFieldInfo.getColumn());
        } else {
            sqlSet += SqlScriptUtils.safeParam(newPrefix + tableFieldInfo.getEl());
        }
        sqlSet += tableFieldInfo.COMMA;
        if (ignoreIf) {
            return sqlSet;
        }
        if (tableFieldInfo.isWithUpdateFill()) {
            // 不进行 if 包裹
            return sqlSet;
        }
        return convertIf(false,tableFieldInfo.isCharSequence(),sqlSet,
        		convertIfProperty(newPrefix, tableFieldInfo.getProperty()), FieldStrategy.NOT_NULL);
    }
	 /**
     * 转换成 if 标签的脚本片段
     *
     * @param sqlScript     sql 脚本片段
     * @param property      字段名
     * @param fieldStrategy 验证策略
     * @return if 脚本片段
     */
    private static String convertIf(final boolean isPrimitive,final boolean isCharSequence,final String sqlScript, final String property, final FieldStrategy fieldStrategy)
    {
        if (fieldStrategy == FieldStrategy.NEVER) {
            return null;
        }
        if (isPrimitive || fieldStrategy == FieldStrategy.IGNORED) {
            return sqlScript;
        }
        if (fieldStrategy == FieldStrategy.NOT_EMPTY && isCharSequence) {
            return SqlScriptUtils.convertIf(sqlScript, String.format("%s != null and %s != ''", property, property),false);
        }
        return SqlScriptUtils.convertIf(sqlScript, String.format("%s != null", property), false);
    }
	private static String convertIfProperty(String prefix, String property) 
	{
		return StringUtils.isNotBlank(prefix) ? prefix.substring(0,prefix.length() - 1)+ "['" + property + "']" : property;
	}
}
