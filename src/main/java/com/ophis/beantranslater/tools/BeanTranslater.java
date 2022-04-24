package com.ophis.beantranslater.tools;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.ophis.beantranslater.commonsqlmapper.CommonSqlMapper;
import com.ophis.beantranslater.tools.lambda.LambdaUtils;
import com.ophis.beantranslater.tools.lambda.SerializedFunction;
import com.ophis.beantranslater.tools.utils.ListUtils;
import com.ophis.beantranslater.tools.utils.StrUtil;
import com.ophis.beantranslater.tools.utils.TableFieldUtils;
import org.springframework.util.LinkedMultiValueMap;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@SuppressWarnings({ "rawtypes", "unchecked"})
public class BeanTranslater {


	private CommonSqlMapper commonSqlMapper;


	private static Map<String,Map<String,String>> enumMap=new HashMap<>();

	public static BeanTranslater build(CommonSqlMapper commonSqlMapper){
		BeanTranslater beanTranslater = new BeanTranslater();
		beanTranslater.commonSqlMapper=commonSqlMapper;
		return beanTranslater;
	}

	/**
	 * 由字典自动转换属性(单一对象)
	 * @param object             源对象
	 * @param translateColumnStr  需要转换的属性
	 * @param translatedColumnStr 转换后的值放入的属性
	 * @param groupStr			  字典组名
	 * @return
	 */
    public <T> T translateSingleByDict(T object,String translateColumnStr,String translatedColumnStr,String groupStr)
	{
		if(object==null)return null;
		List<T> data=new ArrayList<T>();
    	data.add(object);
    	return translateByDict(data,translateColumnStr, translatedColumnStr, groupStr).get(0);
	}
	/**
	 * 根据字典自动转换属性
	 * @param data             源数据
	 * @param translateColumnStr  需要转换的属性
	 * @param translatedColumnStr 转换后的值放入的属性
	 * @param groupStr			  字典组名

	 * @return
	 */
    public <T> List<T> translateByDict(List<T> data,String translateColumnStr,String translatedColumnStr,String groupStr)
    {  
    	if(StrUtil.isBlank(translateColumnStr)||StrUtil.isBlank(translatedColumnStr)||StrUtil.isBlank(groupStr))
    	{
    		System.out.println("translateByI18n:转换参数不正确");
    		return data;
    	}
    	String[] translateColumn=translateColumnStr.split(",");
    	String[] translatedColumn=translatedColumnStr.split(",");
    	String [] group=groupStr.split(",");
    	if(data.size()<=0)return data;
		for(int d=0;d<data.size();d++)
		{
			Object entity=data.get(d);
			for(int i=0;i<translateColumn.length;i++)
    		{
    			String column=translateColumn[i];
    			Object object= BeanTools.getProperty(entity, column);
    			String value="";
    			if(object instanceof String)
    			{
    				value=(String) object;
    			}
    			else if(object instanceof Boolean)
    			{
    				value=((Boolean) object)?"1":"0";
    			}
    			else
    			{
    				value=String.valueOf(object);
    			}
    			if(StrUtil.isBlank(value))
    			{
    				BeanTools.setProperty(entity,translatedColumn[i], "");
    			}
    			else if(StrUtil.isBlank(getDictName(value,group[i])))
    			{
    				BeanTools.setProperty(entity,translatedColumn[i], "");
    			}
    			else
    			{
    				BeanTools.setProperty(entity, translatedColumn[i],getDictName(value,group[i]));
    			}
    		}
		}
    	return data;
    }

    //TODO 实现字典转换
	private String getDictName(String value, String s) {
    	return null;
	}

	/**
     * 根据自定义转换器转换属性
	 * @param object		   源数据
     * @param translateColumn  需要转换的属性
     * @param translatedColumn 转换后的值放入的属性
     * @param transer          转换器
     * @return
     * @throws Exception
     */
    public static <T> T translateSingleByPropertyTranser(T object,String translateColumn,String translatedColumn,PropertyTranser transer) throws Exception
    {  
    	if(object==null)return null;
		List<T> data=new ArrayList<T>();
    	data.add(object);
    	return translateByPropertyTranser(data,translateColumn, translatedColumn, transer).get(0); 
    }  
    /**
     * 根据自定义转换器转换属性
	 * @param data			   源数据
     * @param translateColumn  需要转换的属性
     * @param translatedColumn 转换后的值放入的属性
     * @param transer          转换器
     * @return
     * @throws Exception
     */
    public static <T> List<T> translateByPropertyTranser(List<T> data,String translateColumn,String translatedColumn,PropertyTranser transer)
    {
		for(Object entity:data)
		{
			String[] columns=translateColumn.split(",");
			Object[] values=new Object[columns.length];
			int i=0;
			for(String column:columns)
			{
				values[i++]=getProperty(entity, column);
			}
			BeanTools.setProperty(entity,translatedColumn,transer.trans(values));
		}
    	return data;
    }  
    

    
    public <T> T translateByChildTable(T object,String keyColumn,String valueColumn,String childTableKey,
			String childTableName,Class childClass)
    {
    	return translateByChildTable(object, keyColumn, valueColumn, childTableKey, childTableName, childClass,null);
    }

    /**
	 * 通过子查询转换属性(单一对象)
	 * @param object        源数据
	 * @param keyColumn   需要转换的属性名
	 * @param valueColumn   转换后的属性名
	 * @param childTableKey 对应的关联对象的主键属性名
	 * @param childTableName 对应的关联对象的需要取出的属性名
	 * @param queryWrapper      查询需要的queryWrapper
	 * @return
	 */
    public <T> T translateByChildTable(T object, String keyColumn, String valueColumn, String childTableKey,
											  String childTableName, Class childClass, QueryWrapper queryWrapper)
	{
    	if(object==null)return null;
		List<T> data=new ArrayList<T>();
    	data.add(object);
    	return translateByChildTable(data, keyColumn, valueColumn, childTableKey, childTableName, childClass, queryWrapper).get(0); 
	}
    
    
    public <T> List<T> translateByChildTable(List<T> data,String keyColumn,String valueColumn,String childTableKey,
			String childTableName,Class childClass)
    {
    	return translateByChildTable(data, keyColumn, valueColumn, childTableKey, childTableName, childClass,null);
    }
    /**
	 * 通过子查询转换属性
	 * @param data        源数据
	 * @param keyColumn   转换使用的属性名（和子表关联的主键名）
	 * @param valueColumn   转换后的属性名
	 * @param childTableKey 对应的关联对象的主键属性名
	 * @param childTableName 对应的关联对象的需要取出的属性名
	 * @param queryWrapper      查询需要的example 必须实现泛型!!!!!
 	 * @return
	 */
	public <T> List<T> translateByChildTable(List<T> data, String keyColumn, String valueColumn, String childTableKey,
                                             String childTableName, Class childClass, QueryWrapper queryWrapper)
	{
		if(data==null||data.size()<=0)
		{
			return data;
		}
		try 
		{
			if(queryWrapper==null)
			{
				queryWrapper=new QueryWrapper();
			}
			String[] valueColumns=valueColumn.split(",");
			String[] childTableNames=childTableName.split(",");
			if(valueColumns.length!=childTableNames.length)
			{
				throw new RuntimeException("翻译前的值(valueColumns)数量与翻译后的值(childTableNames)数量不一致");
			}
			
			Set ids=new HashSet();
			List lsid=new ArrayList<>();
			for(int i=0;i<data.size();i++)
			{
				ids.add(getProperty(data.get(i), keyColumn));
			}
			lsid.addAll(ids);
			
			queryWrapper.in(true, TableFieldUtils.propertyToColumn(childTableKey, childClass), lsid);
	
			
			List<String> columnNames = Stream.of(childTableName.split(",")).map(name->(TableFieldUtils.propertyToColumn(name, childClass)+" as "+name)).collect(Collectors.toList());
			columnNames.add(TableFieldUtils.propertyToColumn(childTableKey, childClass)+" as "+childTableKey);
			queryWrapper.select(columnNames.toArray(new String[columnNames.size()]));
			
			List childdata=commonSqlMapper.selectList(childClass,queryWrapper);
			
	
			
			for(int x=0;x<valueColumns.length;x++)
			{
				valueColumn=valueColumns[x];
				childTableName=childTableNames[x];
				Map<Object, Object> map=new HashMap<>();
				for(int i=0;i<childdata.size();i++)
				{
					map.put(String.valueOf(getProperty(childdata.get(i), childTableKey)), getProperty(childdata.get(i), childTableName));
				}
				for(int i=0;i<data.size();i++)
				{
					Object result=map.get(String.valueOf(getProperty(data.get(i), keyColumn)));
				    if (result!=null){
				        BeanTools.setProperty(data.get(i), valueColumn, result);
				    }
				}
			}
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return data;
	}
	
	
//	public <T> List<T> translateByTogether(List<T> data,
//												  String keyColumn, String translatedColumns, String togetherKey, TogetherFields togetherFields,
//												  Class togetherClass)
//	 {
//		return translateByTogether(data, keyColumn, translatedColumns, togetherKey, togetherFields, togetherClass,null);
//	 }
//	/**
//	 *
//	 * @param data list数据
//	 * @param keyColumn 主表的键   例如：id
//	 * @param translatedColumns 翻译后放入哪个字段 例如num1,num2,num3
//	 * @param togetherKey 子表关联到主表的键 例如mainId
//	 * @param togetherFields 聚合函数字段，也可使用普通字段
//	 * @param togetherClass 子表的类
//	 * @param queryWrapper
//	 * @return
//	 */
//	public <T> List<T> translateByTogether(List<T> data,
//                                           String keyColumn, String translatedColumns, String togetherKey, TogetherFields togetherFields,
//                                           Class togetherClass, QueryWrapper queryWrapper)
//	{
//		if(queryWrapper==null)
//		{
//			queryWrapper=new QueryWrapper();
//		}
//		if(data==null||data.size()==0)
//		{
//			return data;
//		}
//		queryWrapper.in(true,TableFieldUtils.propertyToColumn(togetherKey, togetherClass), data.stream().map(t->BeanTools.getProperty(t,keyColumn)).collect(Collectors.toList()));
//			List<Map<String, Object>> list=commonSqlMapper.selectMapTogetherByWrapper(togetherFields, queryWrapper, togetherKey,togetherClass);
//
//			Map<String, Map<String, Object>> result = new HashMap<String, Map<String, Object>>();
//			if(list==null)
//			{
//				return data;
//			}
//			for(int i=0;i<list.size();i++)
//			{
//			Map<String, Object> entity = list.get(i);
//			result.put(String.valueOf(entity.get(togetherKey)), entity);
//		}
//
//
//		List<String> translatedColumnList=dot2List(translatedColumns);
//		List<String> sqlvalueList=togetherFields.getFields().stream().map(t->(String)BeanTools.getProperty(t,"asFieldOrField")).collect(Collectors.toList());
//		if(sqlvalueList.size()!=translatedColumnList.size())
//		{
//			throw new RuntimeException("翻译前的值(sqlvalues)数量与翻译后的值(translatedColumns)数量不一致");
//		}
//		for(Object entity:data)
//		{
//			Object key=getProperty(entity, keyColumn);
//			for(int i=0;i<sqlvalueList.size();i++)
//			{
//				String sqlvalue=sqlvalueList.get(i);
//				String translatedColumn=translatedColumnList.get(i);
//				Map<String, Object> maptodeal = result.get(key);
//				if(maptodeal!=null)
//
//				{
//					Object value=maptodeal.get(sqlvalue);
//					Field field=BeanTools.getClassField(entity.getClass(), translatedColumn);
//					if(field!=null&&field.getType()==String.class&&value!=null)
//					{
//						value=String.valueOf(value);
//					}
//					BeanTools.setProperty(entity,translatedColumn,value);
//				}
//			}
//		}
//		return data;
//	}
	

	private static Object getProperty(Object entity,String property)
	{
		Object object=null;
		try 
		{
			object=BeanTools.getProperty(entity, property);
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return object;
	}
	
	private static String getGetterStr(String property)
	{
		return property.substring(0,1).toUpperCase()+property.substring(1);
	}

	public static void transAllStringFieldEmpty2Null(List list)
	{
		for(Object object:list)
		{
			Field[] fields = BeanTools.getClassFieldsWithOutCglib(object.getClass());
			for(Field field:fields)
			{
				if(field.getType()==String.class)
				{
					if("".equals(BeanTools.getProperty(object, field.getName())))
					{
						BeanTools.setProperty(object, field.getName(),null);
					}
				}
			}
		}
	}
	public static void translateNullToEmpty(List data,String... propertys) {
		if(propertys!=null)
		{
			for(int i=0;i<data.size();i++)
			{
				for(String property:propertys)
				{
					if(BeanTools.getProperty(data.get(i), property)==null
					  ||"null".equals(BeanTools.getProperty(data.get(i), property))
					)
					{
						BeanTools.setProperty(data.get(i), property, "");
					}
				}
			}
		}
	}
	public <T> List<T> translateByDictDotList(List<T> data,String translateColumnStr,String translatedColumnStr,String groupStr)
		{
	    	if(StrUtil.isBlank(translateColumnStr)||StrUtil.isBlank(translatedColumnStr)||StrUtil.isBlank(groupStr))
	    	{
	    		System.out.println("translateByI18n:转换参数不正确");
	    		return data;
	    	}
	    	String[] translateColumn=translateColumnStr.split(",");
	    	String[] translatedColumn=translatedColumnStr.split(",");
	    	String [] group=groupStr.split(",");
	    	if(data.size()<=0)return data;
			for(int d=0;d<data.size();d++)
			{
				Object entity=data.get(d);
				for(int i=0;i<translateColumn.length;i++)
	    		{
	    			String column=translateColumn[i];
	    			Object object= BeanTools.getProperty(entity, column);
	    			String value="";
	    			if(object instanceof String)
	    			{
	    				value=(String) object;
	    			}
	    			else if(object instanceof Boolean)
	    			{
	    				value=((Boolean) object)?"1":"0";
	    			}
	    			else
	    			{
	    				value=String.valueOf(object);
	    			}
	    			
	    			if(StrUtil.isBlank(value))
	    			{
	    				BeanTools.setProperty(entity,translatedColumn[i], "");
	    			}
	    			else
	    			{
	    				List<String> list=new ArrayList<String>();
	    				for(String key:value.split(","))
	    				{
	    					String label=getDictName(key,group[i]);
	        				if(label==null)
	        	    		{
	        					label="";
	        	    		}
	        				list.add(label);
	    				}
	    				BeanTools.setProperty(entity, translatedColumn[i],list2Dot(list));
	    			}
	    		}
			}
			return data;
		}

//	/**
//	 * 翻译逗号分割的字段为逗号分割的字段
//	 * @param data
//	 * @param keyColumn
//	 * @param valueColumn
//	 * @param childTableKey
//	 * @param childTableName
//	 * @return
//	 */
//	public List translateByChildTableDotList(List data, String keyColumn, String valueColumn, String childTableKey,
//											 String childTableName, Class childClass){
//		return 	translateByChildTableDotList(data,keyColumn,valueColumn,childTableKey,childTableName,childClass,ExtQueryWrapper.newInstance(childClass));
//	}

//	/**
//	 * 翻译逗号分割的字段为逗号分割的字段
//	 * @param data
//	 * @param keyColumn
//	 * @param valueColumn
//	 * @param childTableKey
//	 * @param childTableName
//	 * @param queryWrapper
//	 * @return
//	 */
//	public List translateByChildTableDotList(List data, String keyColumn, String valueColumn, String childTableKey,
//											 String childTableName, Class childClass, QueryWrapper queryWrapper){
//		return 	translateByChildTableDotList(data,keyColumn,valueColumn,childTableKey,childTableName,childClass,queryWrapper,",");
//	}

	/**
	 * 嵌套list的翻译
	 * @param data
	 * @param innerListProperty
	 * @param function
	 * @return
	 */
	public List translateListInnerList(List data,String innerListProperty,
									   String newProperties,
									   BiFunction<BeanTranslater,List,List> function)
	{
		List allInner=new ArrayList();
		for (Object obj : data) {
			List inner= (List) BeanTools.getProperty(obj,innerListProperty);
			if(StrUtil.isNotBlank(newProperties)){
				BeanTools.createNewPropertyChild(inner,newProperties);
			}
			if(Objects.isNull(inner)){
				continue;
			}
			allInner.addAll(inner);
		}
		function.apply(this,allInner);
		return data;
	}

	/**
	 * 翻译逗号分割的字段为逗号分割的字段
	 * @param data
	 * @param keyColumn
	 * @param valueColumn
	 * @param childTableKey
	 * @param childTableName
	 * @param queryWrapper
	 * @return
	 */
	public List translateByChildTableDotList(List data, String keyColumn, String valueColumn, String childTableKey,
                                             String childTableName, Class childClass, QueryWrapper queryWrapper,String splitSymbol)
	{
		if(data==null||data.size()<=0)
		{
			return data;
		}
		try 
		{
			if(queryWrapper==null)
			{
				queryWrapper=new QueryWrapper();
			}
			
			Set<String> ids=new HashSet<String>();
			List lsid=new ArrayList<>();
			for(int i=0;i<data.size();i++)
			{
				if(getProperty(data.get(i), keyColumn)==null){
					continue;
				}
				String dot=String.valueOf(getProperty(data.get(i), keyColumn));
				ids.addAll(dot2List(dot));
			}
			lsid.addAll(ids);
			if(lsid!=null&&lsid.size()>0)
			{
				queryWrapper.in(true, TableFieldUtils.propertyToColumn(childTableKey, childClass), lsid);
			}
			else
			{
				queryWrapper.apply(true,"1=0");
			}

			List<String> columnNames = Stream.of(childTableName.split(",")).map(name->(TableFieldUtils.propertyToColumn(name, childClass)+" as "+name)).collect(Collectors.toList());
			columnNames.add(TableFieldUtils.propertyToColumn(childTableKey, childClass)+" as "+childTableKey);
			queryWrapper.select(columnNames.toArray(new String[columnNames.size()]));
			List childdata=commonSqlMapper.selectList(childClass,queryWrapper);

			String[] valueColumns=valueColumn.split(",");
			String[] childTableNames=childTableName.split(",");
			if(valueColumns.length!=childTableNames.length)
			{
				throw new RuntimeException("翻译前的值(valueColumns)数量与翻译后的值(childTableNames)数量不一致");
			}
			for(int x=0;x<valueColumns.length;x++)
			{
				valueColumn=valueColumns[x];
				childTableName=childTableNames[x];
				LinkedMultiValueMap<String, String> map=new LinkedMultiValueMap<>();
				for(int i=0;i<childdata.size();i++)
				{
					map.add(String.valueOf(getProperty(childdata.get(i), childTableKey)), String.valueOf(getProperty(childdata.get(i), childTableName)));
				}
				for(int i=0;i<data.size();i++)
				{
					if(getProperty(data.get(i), keyColumn)==null){
						continue;
					}
					String dot=String.valueOf(getProperty(data.get(i), keyColumn));
					List<String> keysList=dot2List(dot);
					List<String> valueList=new ArrayList<String>();
					for(String key:keysList)
					{
						 if (map.get(key)!=null)
						 {
							 valueList.addAll(map.get(key));
						 }
						 else
						 {
							 valueList.add("");
						 }
					}
				    if (valueList.size()>0){
				        BeanTools.setProperty(data.get(i), valueColumn, list2Dot(valueList,splitSymbol));
				    }
				}
			}
		} 
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return data;
	}
//
//	/**
//	 *
//	 * @param data          源数据
//	 * @param keyColumn     主键
//	 * @param valueColumn   翻译后属性
//	 * @param childTableKey 子表关联主键
//	 * @param childTableName 翻译用到的子表属性 与valueColumn一一对应
//	 * @param queryFunction  执行函数 手写数据库操作方法，传入List<String>类型的id，返回List属性结果
//	 * @return
//	 */
//	public List translateByDiyQueryDotList(List data, String keyColumn,String valueColumn,String childTableKey,
//			String childTableName,Function<List<String>,List> queryFunction)
//	{
//		if(data==null||data.size()<=0)
//		{
//			return data;
//		}
//		try
//		{
//
//			Set<String> ids=new HashSet<String>();
//			List lsid=new ArrayList<>();
//			for(int i=0;i<data.size();i++)
//			{
//				String dot=(String) getProperty(data.get(i), keyColumn);
//				ids.addAll(dot2List(dot));
//			}
//			lsid.addAll(ids);
//			if(lsid!=null&&lsid.size()>0)
//			{
//
//			}
//			else
//			{
//				return data;
//			}
//
//			List childdata=queryFunction.apply(lsid);
//			if(childdata==null||childdata.size()==0)
//			{
//				return data;
//			}
//			String[] valueColumns=valueColumn.split(",");
//			String[] childTableNames=childTableName.split(",");
//			if(valueColumns.length!=childTableNames.length)
//			{
//				throw new RuntimeException("翻译前的值(valueColumns)数量与翻译后的值(childTableNames)数量不一致");
//			}
//			for(int x=0;x<valueColumns.length;x++)
//			{
//				valueColumn=valueColumns[x];
//				childTableName=childTableNames[x];
//				Map<Object, String> map=new HashMap<>();
//				for(int i=0;i<childdata.size();i++)
//				{
//					String mapChildKey=childTableKey;
//					String mapChildValue=childTableName;
//					if(getProperty(childdata.get(i), mapChildKey)==null)
//					{
//						mapChildKey=StrUtil.toUnderlineCase(mapChildKey);
//					}
//					if(getProperty(childdata.get(i), mapChildValue)==null)
//					{
//						mapChildValue=StrUtil.toUnderlineCase(mapChildValue);
//					}
//					map.put(getProperty(childdata.get(i), mapChildKey), String.valueOf(getProperty(childdata.get(i), mapChildValue)));
//				}
//				for(int i=0;i<data.size();i++)
//				{
//					String dot=(String) getProperty(data.get(i), keyColumn);
//					List<String> keysList=dot2List(dot);
//					List<String> valueList=new ArrayList<String>();
//					for(String key:keysList)
//					{
//						 if (map.get(key)!=null)
//						 {
//							 valueList.add(map.get(key));
//						 }
//						 else
//						 {
//							 valueList.add("");
//						 }
//					}
//				    if (valueList.size()>0){
//				        BeanTools.setProperty(data.get(i), valueColumn, list2Dot(valueList,","));
//				    }
//				}
//			}
//		}
//		catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return data;
//	}




	public static <T>  List<T> translateByEnum(List<T> data, SerializedFunction<T,Object> translateField, SerializedFunction<T,Object> translatedField, Class... enumClazz)
	{
		return translateByEnum(data, LambdaUtils.resolveFieldName(translateField), LambdaUtils.resolveFieldName(translatedField),enumClazz);
	}
	/**
	 * 根据枚举自动转换属性
	 * @param data             源数据
	 * @param translateColumnStr  需要转换的属性
	 * @param translatedColumnStr 转换后的值放入的属性
	 * @param enumClazz			  字典组名
	 * @return
	 */
	public static <T>  List<T> translateByEnum(List<T> data,String translateColumnStr,String translatedColumnStr,Class... enumClazz)
	{
		if(StrUtil.isBlank(translateColumnStr)||StrUtil.isBlank(translatedColumnStr))
		{
			System.out.println("translateByI18n:转换参数不正确");
			return data;
		}
		String[] translateColumn=translateColumnStr.split(",");
		String[] translatedColumn=translatedColumnStr.split(",");
		if(data.size()<=0)return data;
		for(int d=0;d<data.size();d++)
		{
			Object entity=data.get(d);
			for(int i=0;i<translateColumn.length;i++)
			{
				String column=translateColumn[i];
				Object object= BeanTools.getProperty(entity, column);
				String value="";
				if(object instanceof String)
				{
					value=(String) object;
				}
				else if(object instanceof Boolean)
				{
					value=((Boolean) object)?"1":"0";
				}
				else
				{
					value=String.valueOf(object);
				}
				if(StrUtil.isBlank(value))
				{
					BeanTools.setProperty(entity,translatedColumn[i], "");
				}
				else if(StrUtil.isBlank(getEnumName(value,enumClazz[i])))
				{
					BeanTools.setProperty(entity,translatedColumn[i], "");
				}
				else
				{
					BeanTools.setProperty(entity, translatedColumn[i],getEnumName(value,enumClazz[i]));
				}
			}
		}
		return data;
	}

	public static Map<String,String> getEnumMap(Class enumClazz,Integer type){
		makeEnumMaps(enumClazz,type);
		return enumMap.get(makeEnumKey(enumClazz,type));
	}
	private static String makeEnumKey(Class enumClazz,Integer type){
		String key=enumClazz.getName();
		if(type!=null){
			key=key+"<=>"+type;
		}
		return key;
	}
	private static void makeEnumMaps(Class enumClazz){
		makeEnumMaps(enumClazz,null);
	}
	private static void makeEnumMaps(Class enumClazz,Integer type){
		synchronized (enumMap){
			String key=makeEnumKey(enumClazz,type);
			if(enumMap.get(key)==null||enumMap.get(key).isEmpty()){
				enumMap.put(key,new TreeMap<String,String>());
				EnumSet.allOf(enumClazz).stream()
						.filter(t->
								{
									if(Objects.equals(1,BeanTools.getProperty(t, "unuse"))){
										return false;
									}
									if(type!=null){
										//如果type为空或者type不等于需要的type则过滤掉即返回false
										if(ListUtils.array2List((Integer[])BeanTools.getProperty(t, "types")).contains(type)
										){
											return true;
										}
										else{
											return false;
										}
									}
									return true;
								}
						)
						.forEach(t->
						enumMap.get(key).put(String.valueOf(BeanTools.getProperty(t, "flag"))
								,String.valueOf(BeanTools.getProperty(t,"description")))
				);
			}
		}
	}

	public static String getEnumName(String key, Class enumClazz) {

		makeEnumMaps(enumClazz);
		if(enumMap.get(makeEnumKey(enumClazz,null)) ==null||enumMap.get(makeEnumKey(enumClazz,null)).get(key)==null)
		{
			return "";
		}
		return enumMap.get(makeEnumKey(enumClazz,null)).get(key);
	}

	private static List<String> dot2List(String strdot)
	{
		return Stream.of(strdot.split(",")).collect(Collectors.toList());
	}
	private static String list2Dot(List<String> list)
	{
		return list2Dot(list,",");
	}
	private static String list2Dot(List<String> list,String symbol)
	{
		if(list==null)
		{
			return null;
		}
		return list.stream().collect(Collectors.joining(symbol));
	}

    public static String JACKSON_CLEAR_FIELD="jacksonClearFields";

	public static  <T> void clearJacksonAttributes(List<T> list,String attributes){
		if(list==null){
			return ;
		}
		BeanTools.createNewPropertyChild(list,JACKSON_CLEAR_FIELD);
		for(T t:list){
			BeanTools.setProperty(t,JACKSON_CLEAR_FIELD,attributes);
		}
	}

//	/**
//	 * 将子表对象整个放到某字段
//	 * @param data
//	 * @param keyColumn
//	 * @param valueColumn
//	 * @param childTableKey
//	 * @param queryWrapper
//	 * @param <T>
//	 * @return
//	 */
//    public <T> List<T> translateListByChildTable(List<T> data, String keyColumn, String valueColumn, String childTableKey, ExtQueryWrapper queryWrapper) {
//		if(data==null||data.size()<=0)
//		{
//			return data;
//		}
//		try {
//			if (queryWrapper == null) {
//				throw new RuntimeException("未传入querywrapper");
//			}
//
//			Set ids = new HashSet();
//			List lsid = new ArrayList<>();
//			for (int i = 0; i < data.size(); i++) {
//				ids.add(getProperty(data.get(i), keyColumn));
//			}
//			lsid.addAll(ids);
//
//			Class childClass = queryWrapper.getEntityClass();
//
//			queryWrapper.in(true, DBTransUtil.propertyToColumn(childTableKey, childClass), lsid);
//
//
//			List childdata = commonSqlMapper.selectList(childClass, queryWrapper);
//
//			LinkedMultiValueMap<Object, Object> map = new LinkedMultiValueMap<>();
//			for (int i = 0; i < childdata.size(); i++) {
//				map.add(String.valueOf(getProperty(childdata.get(i), childTableKey)), childdata.get(i));
//			}
//			for (int i = 0; i < data.size(); i++) {
//				Object result = map.get(String.valueOf(getProperty(data.get(i), keyColumn)));
//				if (result != null) {
//					BeanTools.setProperty(data.get(i), valueColumn,result );
//				}
//			}
//		}
//		catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return data;
//
//	}

	/**
	 *
	 * @param data          源数据
	 * @param keyColumn     主键
	 * @param valueColumn   翻译后属性
	 * @param childTableKey 子表关联主键
	 * @param queryFunction  执行函数 手写数据库操作方法，传入List<String>类型的id，返回List属性结果
	 * @return
	 */
	public <T> List translateListByDiySQL(List data, String keyColumn,String valueColumn,String childTableKey
			,Function<List<T>,List> queryFunction)
	{
		if(data==null||data.size()<=0)
		{
			return data;
		}
		try
		{

			Set<T> ids=new HashSet<T>();
			List lsid=new ArrayList<>();
			for(int i=0;i<data.size();i++)
			{
				T key=(T) getProperty(data.get(i), keyColumn);
				ids.add(key);
			}
			lsid.addAll(ids);
			if(lsid!=null&&lsid.size()>0)
			{

			}
			else
			{
				return data;
			}

			List childdata=queryFunction.apply(lsid);
			if(childdata==null||childdata.size()==0)
			{
				return data;
			}

			LinkedMultiValueMap<Object, Object> map = new LinkedMultiValueMap<>();
			for (int i = 0; i < childdata.size(); i++) {
				map.add(String.valueOf(getProperty(childdata.get(i), childTableKey)), childdata.get(i));
			}
			for (int i = 0; i < data.size(); i++) {
				Object result = map.get(String.valueOf(getProperty(data.get(i), keyColumn)));
				if (result != null) {
					BeanTools.setProperty(data.get(i), valueColumn,result);
				}
			}
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return data;
	}


	public interface PropertyTranser {
		public String trans(Object... object);
	}

}
