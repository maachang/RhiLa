package rhila.scriptable;

import java.util.Date;

import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.Wrapper;

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
		}
		// wrapperされている場合.
		if(value instanceof Wrapper) {
			value = ((Wrapper)value).unwrap();
		}
		// Dateの場合.
		if (value instanceof java.util.Date &&
			!(value instanceof DateScriptable)) {
			return new DateScriptable((Date)value);
		// NativeObjectの場合.
		} else if (value instanceof NativeObject) {
			return new MapScriptable((java.util.Map)value);
		// NativeArrayの場合.
		} else if (value instanceof NativeArray) {
			return new ListScriptable((java.util.List)value);
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
		// scriptableの場合.
		} else if(value instanceof Scriptable) {
			return value;
		// wrapper系の場合.
		} else if(value instanceof Wrapper) {
			value = ((Wrapper)value).unwrap();
		}
		// primitive系かmozilla系の場合.
		c = value.getClass();
		if(c.isPrimitive() ||
			value instanceof Exception
			|| value instanceof String || value instanceof Boolean
			|| value instanceof Number
			|| c.getPackage().getName().startsWith("org.mozilla.javascript")) {
			return value;
		// Map系の場合.
		} else if (value instanceof java.util.Map) {
			return new MapScriptable((java.util.Map)value);
		// Array系の場合.
		} else if (value instanceof java.util.List) {
			return new ListScriptable((java.util.List)value);
		// Date系の場合.
		} else if (value instanceof java.util.Date) {
			return new DateScriptable((Date)value);
		}
		// 処理が正常でない場合.
		if(result != null) {
			result[0] = false;
		}
		return value;
	}
	
}
