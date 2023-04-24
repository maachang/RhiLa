package rhila.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Date;

import org.mozilla.javascript.IdScriptableObject;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.Wrapper;

import rhila.lib.NumberUtil;

/**
 * javascriptからのjavaオブジェクトのラップ・アンラップ支援.
 */
@SuppressWarnings("rawtypes")
public class WrapUtil {
	protected WrapUtil() {}
	
	/**
	 * Javaのパラメータとしてアンラップ.
	 */
	public static final Object[] unwrapArgs(final Object[] args) {
		final int length = args == null ? 0 : args.length;
		if(length == 0) {
			return args;
		}
		for(int i = 0; i < length; i ++) {
			args[i] = unwrap(args[i]);
		}
		return args;
	}
	
	/**
	 * Javaのパラメータとしてアンラップ.
	 */
	public static final Object[] unwrapArgs(final int off, final Object[] args) {
		final int length = args == null ? 0 : args.length;
		if(length == 0) {
			return args;
		}
		if(off == 0) {
			for(int i = 0; i < length; i ++) {
				args[i] = unwrap(args[i]);
			}
			return args;
		}
		int cnt = 0;
		final Object[] pms = new Object[length - off];
		for(int i = off; i < length; i ++) {
			pms[cnt ++] = unwrap(args[i]);
		}
		return pms;
	}
	
	/**
	 * Javaのオブジェクトとしてアンラップ.
	 */
	@SuppressWarnings("unchecked")
	public static final Object unwrap(Object value) {
		// 空系の場合.
		if(value == null || value instanceof Undefined) {
			return null;
		// wrapperされている場合.
		} else if(value instanceof Wrapper) {
			return ((Wrapper)value).unwrap();
		// NativeObjectの場合.
		} else if (value instanceof NativeObject) {
			return new MapScriptable((java.util.Map)value);
		// NativeArrayの場合.
		} else if (value instanceof NativeArray) {
			return new ListScriptable((java.util.List)value);
		}
		// NativeDateの場合.
		Long unixTime = convertRhinoNativeDateByLong(value);
		if(unixTime != null) {
			return new Date(unixTime);
		}
		return value;
	}
	
	/**
	 * Javaオブジェクトからrhino向けにラップ.
	 */
	public static final Object wrap(Object value) {
		return wrap(null, value);
	}
	
	/**
	 * Javaオブジェクトからrhino向けにラップ.
	 */
	@SuppressWarnings("unchecked")
	public static final Object wrap(boolean[] result, Object value) {
		// 正常処理条件をセット.
		if(result != null) {
			result[0] = true;
		}
		Class c;
		// 空系の場合.
		if(value == null || value instanceof Undefined) {
			return value;
		// wrapper系の場合.
		} else if(value instanceof Wrapper) {
			value = ((Wrapper)value).unwrap();
		// scriptableの場合.
		} else if(value instanceof Scriptable) {
			return value;
		}
		// primitive系かmozilla系の場合.
		c = value.getClass();
		if(c.isPrimitive() ||
			value instanceof Exception ||
			value instanceof String || value instanceof Boolean || value instanceof Number ||
			c.getPackage().getName().startsWith("org.mozilla.javascript")) {
			return value;
		// Map系(NativeObjectを除く)の場合.
		} else if (!(value instanceof NativeObject) &&
			value instanceof java.util.Map) {
			return new MapScriptable((java.util.Map)value);
		// Array系(NativeArrayを除く)の場合.
		} else if (!(value instanceof NativeArray) &&
			value instanceof java.util.List) {
			return new ListScriptable((java.util.List)value);
		// Date系の場合.
		} else if(value instanceof java.util.Date) {
			return createNativeDate(((java.util.Date)value).getTime());
		}
		// 処理が正常でない場合.
		if(result != null) {
			result[0] = false;
		}
		return value;
	}
	
	/**
	 * rhinoのNativeDateオブジェクトの場合は、java.util.Dateに変換.
	 * @param o 対象のオブジェクトを設定します.
	 * @return Long 
	 */
	public static final Long convertRhinoNativeDateByLong(Object o) {
		if (o instanceof IdScriptableObject &&
			"Date".equals(((IdScriptableObject)o).getClassName())) {
			// NativeDate.
			try {
				// リフレクションで直接取得するようにする.
				final Method md = o.getClass().getDeclaredMethod("getJSTimeValue");
				md.setAccessible(true);
				return NumberUtil.parseLong(md.invoke(o));
			} catch (Exception e) {
				// エラーの場合は処理しない.
			}
		}
		return null;
	}
	
	// nativeDateを生成.
	private static final Object createNativeDate(long time) {
		try {
			// リフレクションで作成する.
			Class<?> c = Class.forName("org.mozilla.javascript.NativeDate");
			Field f = c.getDeclaredField("date");
			f.setAccessible(true); // private date
			Constructor<?> cs = c.getDeclaredConstructor();
			cs.setAccessible(true); // private NativeDate
			Object o = cs.newInstance();
			f.set(o, (double)time);
			return o;
		} catch(Exception e) {
			throw new RhilaException(e);
		}
	}
	
	
}
