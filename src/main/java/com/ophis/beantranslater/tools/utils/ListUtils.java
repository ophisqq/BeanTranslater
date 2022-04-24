package com.ophis.beantranslater.tools.utils;


import com.ophis.beantranslater.tools.BeanTools;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;



/**
 * 
 * <p>[功能描述]：list工具类</p>
 * @author	ophis
 * @version	1.0, 2021年3月31日下午5:27:30
 */
public class ListUtils
{
	/**
	 * 
	 * <p>[功能描述]：把字符串的list转换成逗号分割的字符串</p>
	 * @param arr 待转换的list
	 * @return
	 */
	public static String list2Dot(List<String> arr)
	{
		String result = null;
		for (String obj : arr)
		{
			if (result == null)
			{
				result = obj;
			}
			else
			{
				result += "," + obj;
			}
		}
		if (result == null)
		{
			return "";
		}
		return result;
	}

	/**
	 *
	 * <p>[功能描述]：把字符串的list转换成逗号分割的字符串</p>
	 * @param arr 待转换的list
	 * @return
	 */
	public static <T> String list2DotAny(List<T> arr)
	{
		String result = null;
		for (T obj : arr)
		{
			if(obj==null)
			{
				continue;
			}
			if (result == null)
			{
				result = obj.toString();
			}
			else
			{
				result += "," + obj.toString();
			}
		}
		if (result == null)
		{
			return "";
		}
		return result;
	}

	/**
	 * 
	 * <p>[功能描述]：把逗号分割的字符串转换成字符串的list</p>
	 * @param arrstr 逗号分割的字符串
	 * @return
	 */
	public static List<String> dot2List(String arrstr)
	{
		if (StrUtil.isBlank(arrstr))
		{
			return new ArrayList<String>();
		}
		List<String> result = new ArrayList<String>();
		for (String obj : arrstr.split(","))
		{
			result.add(obj);
		}
		return result;
	}

	/**
	 *
	 * <p>[功能描述]：数组转list</p>
	 * @param array 待转化数组
	 * @return
	 */
	public static <T> List<T> array2List(T[] array)
	{
		if (array == null)
		{
			return new ArrayList<T>();
		}
		List<T> result = new ArrayList<T>();
		for (int i = 0; i < array.length; i++)
		{
			result.add(array[i]);
		}
		return result;
	}

	/**
	 *
	 * <p>[功能描述]：数组转list</p>
	 * @param array 待转化数组
	 * @return
	 */
	public static <T> List<T> arrayToList(T... array)
	{
		if (array == null)
		{
			return new ArrayList<T>();
		}
		List<T> result = new ArrayList<T>();
		for (int i = 0; i < array.length; i++)
		{
			result.add(array[i]);
		}
		return result;
	}


	/**
	 * 
	 * <p>[功能描述]：把list转为map</p>
	 * @param list 待转换list
	 * @param propKey 哪个属性作为map的key
	 * @return
	 */
	public static Map<String, Object> list2Map(List list, String propKey)
	{
		return list2Map(list, propKey, null);
	}
	
	/**
	 * 
	 * <p>[功能描述]：把list转为map</p>
	 * @param list 待转换list
	 * @param propKey 哪个属性作为map的key
	 * @param propValue 哪个属性作为map的value(传null把对象自己放进去)
	 * @return
	 */
	public static Map<String, Object> list2Map(List list, String propKey, String propValue)
	{
		if (list == null || list.size() == 0)
		{
			return new HashMap<String, Object>();
		}
		Map<String, Object> result = new HashMap<String, Object>();
		for (Object obj : list)
		{
			if (obj == null)
				continue;
			Object key = BeanTools.getProperty(obj, propKey);
			if (key == null)
				continue;
			Object value;
			if (propValue == null)
			{
				value = obj;
			}
			else
			{
				value = BeanTools.getProperty(obj, propValue);
			}

			result.put(String.valueOf(key), value);
		}
		return result;
	}
	public static <T> Map<String, T> list2MapT(List<T> list, String propKey)
	{
		if (list == null || list.size() == 0)
		{
			return new HashMap<String, T>();
		}
		Map<String, T> result = new HashMap<String, T>();
		for (T obj : list)
		{
			if (obj == null)
				continue;
			Object key = BeanTools.getProperty(obj, propKey);
			if (key == null)
				continue;
			result.put(String.valueOf(key), obj);
		}
		return result;
	}
	/**
	 * 
	 * <p>[功能描述]：把list复制为一个新地址的list</p>
	 * @param list 待复制list
	 * @return
	 */
	public static <T> List<T> copyList(List<T> list)
	{
		if (list == null)
		{
			return null;
		}
		List<T> newlist = new ArrayList<T>();
		newlist.addAll(list);
		return newlist;
	}

	/**
	 * 
	 * <p>[功能描述]：把一个对象放入list返回</p>
	 * @param t 待转换对象
	 * @return
	 */
	public static <T> List<T> object2List(T t)
	{
		List<T> newlist = new ArrayList<T>();
		newlist.add(t);
		return newlist;
	}
	


	/**
	 * 
	 * <p>[功能描述]：合并两个数组/p>
	 * @param array1 数组1
	 * @param array2 数组2
	 * @return
	 */
	public static <T> T[] mergeArray(T[] array1, T[] array2)
	{
		if (array1 == null)
		{
			return array2;
		}
		else if (array2 == null)
		{
			return array1;
		}
		T[] joinedArray = (T[]) Array.newInstance(array1.getClass().getComponentType(), array1.length + array2.length);
		System.arraycopy(array1, 0, joinedArray, 0, array1.length);
		try
		{
			System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
		}
		catch (ArrayStoreException ase)
		{
			final Class type1 = array1.getClass().getComponentType();
			final Class type2 = array2.getClass().getComponentType();
			if (!type1.isAssignableFrom(type2))
			{
				throw new IllegalArgumentException(
						"Cannot store " + type2.getName() + " in an array of " + type1.getName());
			}
			throw ase;
		}
		return joinedArray;
	}
	
	/**
	 * 
	 * <p>[功能描述]：将数组中的一个属性提取出来成为新数组</p>
	 * @param list 待提取数组
	 * @param property 提取的属性名
	 * @return
	 */
	public static List<String> property2Array(List list, String property)
	{
		List<String> result = new ArrayList<String>();
		if (list == null || list.size() == 0)
		{
			return result;
		}
		for (Object obj : list)
		{
			if (obj != null && BeanTools.getProperty(obj, property) != null)
			{
				Object value = BeanTools.getProperty(obj, property);
				if (value.getClass() == String.class)
					result.add((String) value);
				else
					result.add(String.valueOf(value));

			}
		}
		return result;
	}

	/**
	 * 
	 * <p>[功能描述]：将数组中的一个属性提取出来成为新数组</p>
	 * @param list 待提取数组
	 * @param property 提取的属性名
	 * @return
	 */
	public static <T> List<T> property2ArrayT(List list, String property)
	{
		List<T> result = new ArrayList<T>();
		for (Object obj : list)
		{
			if (obj != null && BeanTools.getProperty(obj, property) != null)
			{
				T value = (T) BeanTools.getProperty(obj, property);
				result.add(value);
			}
		}
		return result;
	}


	public static Function<Object,Integer> TRANSER_STRING_INTEGER = new Function<Object,Integer>()
	{
		@Override
		public Integer apply(Object object)
		{
			String type = (String) object;
			try
			{
				return Integer.parseInt(type);
			}
			catch (Exception e)
			{
				return null;
			}
		}
	};

	public static Function<Object,String> TRANSER_ANYOBJECT_STRING = new Function<Object,String>()
	{
		@Override
		public String apply(Object object)
		{
			if(object==null)
			{
				return null;
			}
			return String.valueOf(object);
		}
	};

	/**
	 * 
	 * <p>[功能描述]：转换list为指定类型的list，相同的属性名会复制到新list的对象里</p>
	 * @param list    待转换list
	 * @param resultType 新list的类型
	 * @return
	 */
	public static <T> List<T> toClassListWithSameFields(List list, Class<T> resultType)
	{
		List<T> result = new ArrayList<T>();
		for (Object obj : list)
		{
			try
			{
				T t = resultType.newInstance();
				BeanTools.copyProperties(obj, t);
				result.add(t);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				result.add(null);
			}
		}
		return result;
	}
	
	/**
	 * 
	 * <p>[功能描述]：通过转换器转换list为指定类型的list</p>
	 * @param list 待转换list
	 * @param transer 转换器
	 * @return
	 */
	public static <T> List<T> toClassList(List list, Function<Object,T> transer)
	{
		List<T> result = new ArrayList<T>();
		for (Object obj : list)
		{
			T t = (T) transer.apply(obj);
			result.add(t);
		}
		return result;
	}

	public static <T> List<T> emptyList(Class<T> type)
	{
		return new ArrayList<T>();
	}


}
