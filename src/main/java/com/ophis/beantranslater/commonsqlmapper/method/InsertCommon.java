package com.ophis.beantranslater.commonsqlmapper.method;

import java.util.ArrayList;
import java.util.Collections;

import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.executor.keygen.SelectKeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.MappedStatement.Builder;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.Configuration;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.incrementer.IKeyGenerator;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.toolkit.GlobalConfigUtils;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;

public class InsertCommon extends AbstractCommonMethod
{
	@Override
	public String getSql(TableInfo tableInfo)
	{
		SqlMethod sqlMethod = SqlMethod.INSERT_ONE;
		String columnScript = SqlScriptUtils.convertTrim(tableInfo.getAllInsertSqlColumnMaybeIf(null), LEFT_BRACKET,
				RIGHT_BRACKET, null, COMMA);
		String valuesScript = SqlScriptUtils.convertTrim(tableInfo.getAllInsertSqlPropertyMaybeIf(null), LEFT_BRACKET,
				RIGHT_BRACKET, null, COMMA);
		String sql = String.format(sqlMethod.getSql(), tableInfo.getTableName(), columnScript, valuesScript);
		return sql;
	}
	@Override
	public void setPrimaryKeys(Builder builder, TableInfo tableInfo) 
	{
		KeyGenerator keyGenerator = new NoKeyGenerator();
        String keyProperty = null;
        String keyColumn = null;
        // 表包含主键处理逻辑,如果不包含主键当普通字段处理
        if (StringUtils.isNotBlank(tableInfo.getKeyProperty())) {
            if (tableInfo.getIdType() == IdType.AUTO) {
                /** 自增主键 */
                keyGenerator = new Jdbc3KeyGenerator();
                keyProperty = tableInfo.getKeyProperty();
                keyColumn = tableInfo.getKeyColumn();
            } else {
                if (null != tableInfo.getKeySequence()) {
                    keyGenerator = InsertCommon.genKeyGenerator(getMethod(SqlMethod.INSERT_ONE), tableInfo);
                    keyProperty = tableInfo.getKeyProperty();
                    keyColumn = tableInfo.getKeyColumn();
                }
            }
        }
        builder.keyColumn(keyColumn).keyProperty(keyProperty).keyGenerator(keyGenerator);
	}
	
	public static KeyGenerator genKeyGenerator(String baseStatementId, TableInfo tableInfo) {
		Configuration configuration=tableInfo.getConfiguration();
        IKeyGenerator keyGenerator = GlobalConfigUtils.getKeyGenerator(configuration);
        if (null == keyGenerator) {
            throw new IllegalArgumentException("not configure IKeyGenerator implementation class.");
        }
        String id = tableInfo.getCurrentNamespace() + StringPool.DOT + baseStatementId + SelectKeyGenerator.SELECT_KEY_SUFFIX;
        ResultMap resultMap = new ResultMap.Builder(configuration, id, tableInfo.getKeyType(), new ArrayList<>()).build();
        MappedStatement mappedStatement = new Builder(configuration, id,
            new StaticSqlSource(configuration, keyGenerator.executeSql(tableInfo.getKeySequence().value())), SqlCommandType.SELECT)
            .keyProperty(tableInfo.getKeyProperty())
            .resultMaps(Collections.singletonList(resultMap))
            .build();
        configuration.addMappedStatement(mappedStatement);
        return new SelectKeyGenerator(mappedStatement, true);
    }

	
	@Override
	public SqlCommandType getSqlCommandType() {
		return SqlCommandType.INSERT;
	}
	
	
}
