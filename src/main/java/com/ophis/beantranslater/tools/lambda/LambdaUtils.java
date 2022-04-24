package com.ophis.beantranslater.tools.lambda;

import com.baomidou.mybatisplus.annotation.TableField;
import com.ophis.beantranslater.tools.utils.TableFieldUtils;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;


public class LambdaUtils
{
    /**
     * SerializedLambda 反序列化缓存
     */
    private static final Map<String, WeakReference<SerializedLambda>> FUNC_CACHE = new ConcurrentHashMap<>();

	public static <T> SerializedLambda resolve(SerializedFunction<T, ?> func) {
        Class<?> clazz = func.getClass();
        String name = clazz.getName();
		return Optional.ofNullable(FUNC_CACHE.get(name))
				.map(WeakReference::get)
				.orElseGet(() -> {
					SerializedLambda lambda = SerializedLambda.resolve(func);
					FUNC_CACHE.put(name, new WeakReference<>(lambda));
					return lambda;
				});
    }
	
	public static <T> String resolveFieldName(SerializedFunction<T, ?> func) {
		SerializedLambda lambda=resolve(func);
		return methodToProperty(lambda.getImplMethodName());
	}

	public static <T> String property(SerializedFunction<T, ?> func) {
		if(func==null){
			return null;
		}
		SerializedLambda lambda=resolve(func);
		return methodToProperty(lambda.getImplMethodName());
	}

	public static <T> String column(SerializedFunction<T, ?> func) {
		if(func==null){
			return null;
		}
		SerializedLambda lambda=resolve(func);
		String property=property(func);
		Class<?> implClass = lambda.getImplClass();

		return TableFieldUtils.propertyToColumn(property,implClass);
	}

	public static String methodToProperty(String name)
	{
		if (name.startsWith("is"))
		{
			name = name.substring(2);
		}
		else if (name.startsWith("get") || name.startsWith("set"))
		{
			name = name.substring(3);
		}
		else
		{
			throw new LambdaException("Error parsing property name '" + name + "'.  Didn't start with 'is', 'get' or 'set'.");
		}

		if (name.length() == 1 || (name.length() > 1 && !Character.isUpperCase(name.charAt(1))))
		{
			name = name.substring(0, 1).toLowerCase(Locale.ENGLISH) + name.substring(1);
		}

		return name;
	}

}
