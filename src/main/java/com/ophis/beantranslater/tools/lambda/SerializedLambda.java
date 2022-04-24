package com.ophis.beantranslater.tools.lambda;

import javax.sql.rowset.serial.SerialArray;
import java.io.*;

/**
 * 这个类是从 {@link java.lang.invoke.SerializedLambda} 里面 copy 过来的， 字段信息完全一样
 * <p>
 * 负责将一个支持序列的 Function 序列化为 SerializedLambda
 * </p>
 */
@SuppressWarnings("unused")
public class SerializedLambda implements Serializable
{

	private static final long serialVersionUID = 8025925345765570181L;

	private Class<?> capturingClass;
	private String functionalInterfaceClass;
	private String functionalInterfaceMethodName;
	private String functionalInterfaceMethodSignature;
	private String implClass;
	private String implMethodName;
	private String implMethodSignature;
	private int implMethodKind;
	private String instantiatedMethodType;
	private Object[] capturedArgs;

	/**
	 * 通过反序列化转换 lambda 表达式，该方法只能序列化 lambda 表达式，不能序列化接口实现或者正常非 lambda 写法的对象
	 *
	 * @param lambda lambda对象
	 * @return 返回解析后的 SerializedLambda
	 */
	public static SerializedLambda resolve(SerializedFunction<?, ?> lambda)
	{
		if (!lambda.getClass().isSynthetic())
		{
			throw new LambdaException("该方法仅能传入 lambda 表达式产生的合成类");
		}
		try (ObjectInputStream objIn = new ObjectInputStream(
				new ByteArrayInputStream(serialize(lambda)))
		{
			@Override
			protected Class<?> resolveClass(ObjectStreamClass objectStreamClass)
					throws IOException, ClassNotFoundException
			{
				Class<?> clazz;
				try
				{
					clazz = toClassConfident(objectStreamClass.getName());
				}
				catch (Exception ex)
				{
					clazz = super.resolveClass(objectStreamClass);
				}
				return clazz == java.lang.invoke.SerializedLambda.class ? SerializedLambda.class : clazz;
			}
		})
		{
			return (SerializedLambda) objIn.readObject();
		}
		catch (ClassNotFoundException | IOException e)
		{
			throw new LambdaException("This is impossible to happen");
		}
	}

	/**
	 * 获取接口 class
	 *
	 * @return 返回 class 名称
	 */
	public String getFunctionalInterfaceClassName()
	{
		return normalizedName(functionalInterfaceClass);
	}

	/**
	 * 获取实现的 class
	 *
	 * @return 实现类
	 */
	public Class<?> getImplClass()
	{
		return toClassConfident(getImplClassName());
	}

	/**
	 * 获取 class 的名称
	 *
	 * @return 类名
	 */
	public String getImplClassName()
	{
		return normalizedName(implClass);
	}

	/**
	 * 获取实现者的方法名称
	 *
	 * @return 方法名称
	 */
	public String getImplMethodName()
	{
		return implMethodName;
	}

	/**
	 * 正常化类名称，将类名称中的 / 替换为 .
	 *
	 * @param name 名称
	 * @return 正常的类名
	 */
	private String normalizedName(String name)
	{
		return name.replace('/', '.');
	}

	/**
	 * @return 获取实例化方法的类型
	 */
	public Class<?> getInstantiatedType()
	{
		String instantiatedTypeName = normalizedName(
				instantiatedMethodType.substring(2, instantiatedMethodType.indexOf(';')));
		return toClassConfident(instantiatedTypeName);
	}

	/**
	 * @return 字符串形式
	 */
	@Override
	public String toString()
	{
		String interfaceName = getFunctionalInterfaceClassName();
		String implName = getImplClassName();
		return String.format("%s -> %s::%s", interfaceName.substring(interfaceName.lastIndexOf('.') + 1),
				implName.substring(implName.lastIndexOf('.') + 1), implMethodName);
	}

	public static byte[] serialize(Object object)
	{
		if (object == null)
		{
			return null;
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
		try (ObjectOutputStream oos = new ObjectOutputStream(baos))
		{
			oos.writeObject(object);
			oos.flush();
		}
		catch (IOException ex)
		{
			throw new IllegalArgumentException("Failed to serialize object of type: " + object.getClass(), ex);
		}
		return baos.toByteArray();
	}
	public static Class<?> toClassConfident(String name)
	{
		try
		{
			return classForName(name,getClassLoaders(null));
		}
		catch (ClassNotFoundException e)
		{
			try
			{
				return Class.forName(name);
			}
			catch (ClassNotFoundException ex)
			{
				throw new LambdaException("找不到指定的class！请仅在明确确定会有 class 的时候，调用该方法");
			}
		}
	}

	private static Class<?> classForName(String name, ClassLoader[] classLoader) throws ClassNotFoundException
	{
		for (ClassLoader cl : classLoader)
		{
			if (null != cl)
			{
				try
				{
					return Class.forName(name, true, cl);
				}
				catch (ClassNotFoundException e)
				{
					// we'll ignore this until all classloaders fail to locate the class
				}
			}
		}
		throw new ClassNotFoundException("Cannot find class: " + name);
	}

	private static ClassLoader[] getClassLoaders(ClassLoader classLoader)
	{
		return new ClassLoader[] { classLoader, null, Thread.currentThread().getContextClassLoader(),
				SerialArray.class.getClassLoader(),  ClassLoader.getSystemClassLoader() };
	}

}
