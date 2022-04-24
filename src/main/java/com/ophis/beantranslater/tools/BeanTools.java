package com.ophis.beantranslater.tools;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.ophis.beantranslater.tools.utils.DateUtil;
import com.ophis.beantranslater.tools.utils.ListUtils;
import com.ophis.beantranslater.tools.utils.TypeUtil;
import org.springframework.cglib.beans.BeanGenerator;
import org.springframework.cglib.beans.BeanMap;

/**
 *
 * [功能描述]：Bean工具类
 * @author	ophis
 * @version	1.0, 2021年3月31日下午5:27:30
 */
public class BeanTools
{
	public static <T> void setProperty(T entity,String property,Object value)
	{
		if(entity!=null)
		{
			try
			{
				if(property.contains("$cglib_prop_"))
				{
					property=property.replace("$cglib_prop_", "");
				}
				Field field=getClassField(entity.getClass(), property);
				if(field!=null)
				{
					if(value instanceof String)
					{
						if(field.getType()!=String.class&&("null".equals(value)||"".equals(value)))
						{
							value=null;
						}
						else
						{
							if(field.getType()==Double.class)
							{
								value=Double.parseDouble((String)value);
							}
							else if(field.getType()==Integer.class)
							{
								value=Integer.parseInt((String) value);
							}
							else if(field.getType()==Boolean.class)
	                        {
	                            value=Boolean.parseBoolean((String) value);
	                        }
							else if(field.getType()==Date.class)
	                        {
								value= DateUtil.parse(value);
	                        }
							else if(field.getType()==Long.class)
							{
								value=Long.parseLong((String)value);
							}
							else if(field.getType()==BigDecimal.class)
							{
								value=new BigDecimal((String)value);
							}
						}
					}
					else
					{
						if(value!=null)
						{
							if(field.getType()==Double.class)
							{
								value=Double.parseDouble(String.valueOf(value));
							}
							else if(field.getType()==Long.class)
							{
								value=Long.parseLong(String.valueOf(value));
							}
							else if(field.getType()==Short.class)
							{
								value=Short.parseShort(String.valueOf(value));
							}
							else if(field.getType()==Integer.class)
							{
								if(value instanceof Double)
								{
									value=((Double)value).intValue();
								}
								if(value instanceof Float)
								{
									value=((Float)value).intValue();
								}
							}
							else if(field.getType()==BigDecimal.class)
							{
								value=new BigDecimal(String.valueOf(value));
							}
							else if(field.getType()==Boolean.class)
	                        {
								if(value instanceof Integer)
								{
									value=((Integer)value!=0);
								}
								else
								{
									value=Boolean.parseBoolean(String.valueOf(value));
								}
	                        }
							else if(field.getType()==Date.class)
							{
								if(value instanceof Long)
								{
									value=new Date((Long)value);
								}
							}
							else if(field.getType()==String.class)
	                        {
	                        	if(value instanceof Date){
									value=new SimpleDateFormat("yyyy-MM-dd").format(value);
								}
	                        	else{
									value=String.valueOf(value);
								}
	                        }
							else if(List.class.isAssignableFrom(value.getClass())&&List.class.isAssignableFrom(field.getType()))
							{
								Class argClass= TypeUtil.getClass(field.getType());
								if(argClass==Integer.class)
								{
									value= ListUtils.toClassList((List<String>)value,ListUtils.TRANSER_STRING_INTEGER);
								}
							}
						}
					}
				}
				if(Map.class.isAssignableFrom(entity.getClass()))
				{
					((Map)entity).put(property, value);
				}
				else
				{
					String finalProperty = property;
					Method setter=Optional.ofNullable(field).map(f->getSetter(entity.getClass(), finalProperty,f.getType())).orElse(null);
					if(setter!=null&&setter.getReturnType()==Void.TYPE)
					{
						BeanMap bm=BeanMap.create(entity);
						bm.put(entity,property, value);
					}
					else
					{
						if (field != null) {
							field.setAccessible(true);
							field.set(entity, value);
						}
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}



	public static <T> Object getProperty(T entity,String property)
	{
		if(property.contains("$cglib_prop_"))
		{
			property=property.replace("$cglib_prop_", "");
		}
		Object o=null;
		if(entity!=null)
		{
			if(entity instanceof Map)
			{
				return ((Map) entity).get(property);
			}
			try
			{
		        o =BeanMap.create(entity).get(property);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		return o;
	}
	
	public static <T> T getNumberProperty(Object entity,String property,Class<T> clazz)
	{
		Object object=getProperty(entity, property);
		if(object==null)
		{
			return null;
		}
		String str=String.valueOf(object);
		T t=null;
		if(StringUtils.isBlank(str))
		{
			return null;
		}
		if(clazz==Integer.class)
		{
			t=(T)(Integer)Integer.parseInt(str);
		}
		else if(clazz==Long.class)
		{
			t=(T)(Long)Long.parseLong(str);
		}
		else if(clazz==Double.class)
		{
			t=(T)(Double)Double.parseDouble(str);
		}
		else if(clazz==Float.class)
		{
			t=(T)(Float)Float.parseFloat(str);
		}
		else if(clazz==BigDecimal.class)
		{
			t=(T)(BigDecimal)new BigDecimal(str);
		}
		else
		{
			throw new RuntimeException("不支持的数值类型");
		}
		return t;
	}
	/**
	 * 把d的属性复制到s上
	 * @param s
	 * @param d
	 */
	public static void copyProperties(Object s, Object d) {
        if(s == null || d == null){
        	return;
        }
        BeanMap bd=BeanMap.create(d);
        BeanMap bs=BeanMap.create(s);
        for(Object key:bd.keySet())
        {
        	if(bs.containsKey(key))
        	{
        	    try
        	    {
        	        setProperty(s,(String)key,bd.get(key));
        	    }
        	    catch(Exception e)
        	    {
        	        e.printStackTrace();
        	    }
        	}
        }
    }
	/**
	 * 用from的值覆盖to的值并返回两个是否所有值都一样
	 * @param from
	 * @param to
	 * @return
	 */
	public static <T> boolean copyAndCompareBean(T from,T to)
	{
		if(from==null||to==null)
		{
			return false;
		}
		boolean result=true;
		Field[] fields=getClassFields(from.getClass());
		for(Field field:fields)
		{
			Object value1=getProperty(from, field.getName());
			Object value2=getProperty(to, field.getName());
			if(value1==null&&value2==null)
			{
				continue;
			}
			if(value1==null)
			{
				result=false;
				continue;
			}
			if(value1.equals(value2))
			{
				continue;
			}
			setProperty(to, field.getName(), value1);
		}
		return result;
	}
	public static List<Field> getClassFieldsWithAnnotation(Class clazz,Class annotation)
	{
		Field[] fields=getClassFieldsWithOutCglib(clazz);
		List<Field> list = ListUtils.array2List(fields);
		List<Field> result = list.stream().filter(new Predicate<Field>() {
			@Override
			public boolean test(Field t) {
				return t.getAnnotation(annotation) != null;
			}
		}).collect(Collectors.toList());
		return result;
	}
	public static Field[] getClassFields(Class clazz)
	{
		if(clazz.getName().contains("$$BeanGeneratorByCGLIB"))
		{
			Class parent=clazz.getSuperclass();
			Field[] fields=clazz.getDeclaredFields();
			while(parent.getName().contains("$$BeanGeneratorByCGLIB"))
			{
				Field[] superfields=parent.getDeclaredFields();
				fields=ListUtils.mergeArray(superfields, fields);
				parent=parent.getSuperclass();
			}
			Field[] superfields=parent.getDeclaredFields();
			fields=ListUtils.mergeArray(superfields, fields);
			return fields;
		}
		else
		{
			if(clazz.getSuperclass()!=null&&clazz.getSuperclass()!=Object.class)
			{
				return ListUtils.mergeArray(clazz.getSuperclass().getDeclaredFields(), clazz.getDeclaredFields());
			}
			return clazz.getDeclaredFields();
		}
	}
	public static Field getClassField(Class clazz,String fieldName)
	{
		Field[] fields=getClassFields(clazz);
		for(Field field:fields)
		{
			if(field.getName().replace("$cglib_prop_","").equals(fieldName))
			{
				return field;
			}
		}
		Class parent=clazz;
		while(parent.getName().contains("$$BeanGeneratorByCGLIB")||parent!=Object.class)
		{
			Field[] parentFields=parent.getDeclaredFields();
			for(Field field:parentFields)
			{
				if(field.getName().equals(fieldName))
				{
					return field;
				}
			}
			parent=parent.getSuperclass();
		}
			
		return null;
	}
	
	public static Field[] getClassFieldsWithOutCglib(Class clazz)
	{
		
		if(clazz.getName().contains("$$BeanGeneratorByCGLIB"))
		{
			clazz=clazz.getSuperclass();
		}
		
		if(clazz.getSuperclass()!=Object.class)
		{
			return ListUtils.mergeArray(clazz.getSuperclass().getDeclaredFields(), clazz.getDeclaredFields());
		}
		return clazz.getDeclaredFields();
	}
	
	public static Field getClassFieldWithOutCglib(Class clazz,String fieldName)
	{
		Field[] fields=null;
		if(clazz.getName().contains("$$BeanGeneratorByCGLIB"))
		{
			clazz=clazz.getSuperclass();
		}
		if(clazz.getSuperclass()!=Object.class)
		{
			fields=ListUtils.mergeArray(clazz.getSuperclass()!=null?clazz.getSuperclass().getDeclaredFields():new Field[0], clazz.getDeclaredFields());
		}
		else
		{
			fields=clazz.getDeclaredFields();
		}
		for(Field field:fields)
		{
			if(field.getName().equals(fieldName))
			{
				return field;
			}
		}
		return null;
	}
	/**
	 * 清洗掉临时字段
	 * @return
	 */
	public static <T> T clearBean(T t,T realT) 
	{
		if(t==null)
		{
			return null;
		}
		try 
		{
			copyAndCompareBean(t, realT);
			return realT;
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * 为某一对象创建子对象，添加一个指定类型的属性
	 * @param object
	 * @param property
	 * @param clazz
	 * @return
	 */
	public static <T> T createNewPropertyChildWithClass(T object,String property,Class clazz)
    {
		if(object==null)return null;
		List<T> data=new ArrayList<T>();
    	data.add(object);
    	return createNewPropertyChildWithClass(data, property, clazz).get(0);
    }
	/**
	 * 为某一list创建子list，添加一个指定类型的属性
	 * @param property
	 * @param clazz
	 * @return
	 */
	public static <T> List<T> createNewPropertyChildWithClass(List<T> data,String property,Class clazz)
    {
		if(data==null||data.size()<=0)
		{
			return data;
		}
    	List result=new ArrayList<>();
    	Map map=new HashMap<>();
		List<String> properties=ListUtils.dot2List(property);
		for (String p : properties) {
			map.put(p, clazz);
		}
    	try
    	{
    		for(int i=0;i<data.size();i++)
    		{
				if(data.get(i)==null)
				{
					continue;
				}
    			Object o=generateBean(map, data.get(i).getClass());
    			copyProperties(o, data.get(i));
    			data.set(i, (T)o);
    			result.add(o);
    		}
    		return result;
    	}
		catch(Exception e)
    	{
			e.printStackTrace();
    	}
		return data;
    }
	
	/**
	 * 为某一对象创建子对象，添加多个属性逗号分割
	 * @param object
	 * @param properties
	 * @return
	 */
	public static <T> T createNewPropertyChild(T object,String properties)
    {
		if(object==null)return null;
		List<T> data=new ArrayList<T>();
    	data.add(object);
    	return createNewPropertyChild(data, properties).get(0);
    }
	/**
	 * 为某一list创建子list，添加多个属性逗号分割
	 * @param properties
	 * @return
	 */
	public static <T> List<T> createNewPropertyChild(List<T> data,String properties)
    {
		if(data==null||data.size()<=0)
		{
			return data;
		}
    	List result=new ArrayList<>();
    	Map map=new HashMap<>();
    	String[] propertyarr=properties.split(",");
    	for(int i=0;i<propertyarr.length;i++)
    	{
    		map.put(propertyarr[i], String.class);
    	}
    	try
    	{
    		for(int i=0;i<data.size();i++)
    		{
    			if(data.get(i)==null)
				{
					continue;
				}
    			Object o=generateBean(map, data.get(i).getClass());
    			copyProperties(o, data.get(i));
    			data.set(i, (T)o);
    			result.add(o);
    		}
    		return result;
    	}
		catch(Exception e)
    	{
			e.printStackTrace();
    	}
		return data;
    }

    private static Method getGetter(Class clazz,String fieldName)
	{
		StringBuffer sb=new StringBuffer();
		sb.append("get").append(fieldName.substring(0,1).toUpperCase()).append(fieldName.substring(1));
		try {
			return clazz.getDeclaredMethod(sb.toString());
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static Method getSetter(Class clazz,String fieldName,Class type)
	{
		StringBuffer sb=new StringBuffer();
		sb.append("set").append(fieldName.substring(0,1).toUpperCase()).append(fieldName.substring(1));
		try {
			return clazz.getDeclaredMethod(sb.toString(),type);
		} catch (NoSuchMethodException e) {
			return null;
		}
	}
	
	public static <T> T createNewPropertyObjectWithClassMap(Map classMap,T t)
	{  
		T o=(T) generateBean(classMap,t.getClass());
		copyProperties(o,t);
		return o;
    }  
	
	private static Object generateBean(Map propertyMap,Class parent){  
        BeanGenerator generator=new BeanGenerator();  
        generator.setSuperclass(parent);//设置父类  
        Set keySet=propertyMap.keySet();  
        for(Iterator i=keySet.iterator();i.hasNext();){  
            String key=(String)i.next();  
            generator.addProperty(key, (Class)propertyMap.get(key));  
        }  
        return generator.create();  
    }

    public static <T>  T parseNumber(String numberStr,Class<T> clazz) {
		if(numberStr==null){return null;}
		try {
			if(clazz==Integer.class){
				return (T)(Integer)Integer.parseInt(numberStr);
			}
			else if(clazz==Long.class){
				return (T)(Long)Long.parseLong(numberStr);
			}
			else if(clazz==Double.class){
				return (T)(Double)Double.parseDouble(numberStr);
			}
			else if(clazz==Float.class){
				return (T)(Float)Float.parseFloat(numberStr);
			}
			else if(clazz==Byte.class){
				return (T)(Byte)Byte.parseByte(numberStr);
			}
			return null;
		}
		catch (Exception e){
			return null;
		}
    }

}
