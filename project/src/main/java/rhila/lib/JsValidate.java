package rhila.lib;

import java.util.Date;

import rhila.RhilaException;

// jsで設定されたvalidate処理.
public class JsValidate {
	// lambda snapStart CRaC用.
	protected static final JsValidate LOAD_CRAC = new JsValidate();

	// コンストラクタ.
	private JsValidate() {}
	

	// argsが存在しない場合エラー.
	public static final boolean isNoArgs(Object... args) {
		return args == null || args.length == 0;
	}
	
	// 指定位置のパラメータが設定されていない場合エラー.
	public static final boolean isNoArgsToLength(
		int no, Object... args) {
		return args == null || args.length < no;
	}
	
	// argsの設定位置が文字情報ではない場合.
	public static final boolean isNoArgsString(
		int no, Object... args) {
		return !(args[no] instanceof String);
	}
	
	// argsの設定位置が数字情報ではない場合.
	public static final boolean isNoArgsNumber(
		int no, Object... args) {
		return !(args[no] instanceof Number);
	}
	
	// argsの設定位置がBoolean情報ではない場合.
	public static final boolean isNoArgsBoolean(
		int no, Object... args) {
		return !(args[no] instanceof Boolean);
	}
	
	// argsの設定位置がDate情報ではない場合.
	public static final boolean isNoArgsDate(
		int no, Object... args) {
		return !(args[no] instanceof Date);
	}
	
	// argsが存在しない場合エラー.
	public static final void noArgsToError(Object... args) {
		if(isNoArgs(args)) {
			throw new RhilaException("Argument not set.");
		}
	}
	
	// 指定位置のパラメータが設定されていない場合エラー.
	public static final void noArgsToLengthToError(
		int no, Object... args) {
		if(isNoArgsToLength(no, args)) {
			throw new RhilaException(
				"The " + no + "th argument is not.");
		}
	}
	
	// argsの設定位置が文字情報ではない場合.
	public static final void noArgsStringToError(
		int no, Object... args) {
		noArgsToLengthToError(no, args);
		if(isNoArgsString(no, args)) {
			throw new RhilaException(
				"The " + no + "th argument is not a string.");
		}
	}
	
	// argsの設定位置が数字情報ではない場合.
	public static final void noArgsNumberToError(
		int no, Object... args) {
		noArgsToLengthToError(no, args);
		if(isNoArgsNumber(no, args)) {
			throw new RhilaException(
				"The " + no + "th argument is not a number.");
		}
	}
	
	// argsの設定位置がBoolean情報ではない場合.
	public static final void noArgsBooleanToError(
		int no, Object... args) {
		noArgsToLengthToError(no, args);
		if(isNoArgsBoolean(no, args)) {
			throw new RhilaException(
				"The " + no + "th argument is not a boolean.");
		}
	}
	
	// argsの設定位置がDate情報ではない場合.
	public static final void noArgsDateToError(
		int no, Object... args) {
		noArgsToLengthToError(no, args);
		if(isNoArgsDate(no, args)) {
			throw new RhilaException(
				"The " + no + "th argument is not a date.");
		}
	}
	
	// keyチェック.
	public static final void noArgsKeyToError(
		Object... args) {
		if(isNoArgsToLength(1, args)) {
			throw new RhilaException("key is not set.");
		}
	}
	
	// type別keyチェック.
	public static final void noArgsKeyToTypeError(
		String type, Object... args) {
		noArgsKeyToError(args);
		if(type == null) {
			return;
		}
		boolean res = false;
		switch(type) {
		case "string":
			res = isNoArgsString(0, args);
			break;
		case "boolean":
			res = isNoArgsBoolean(0, args);
			break;
		case "number":
			res = isNoArgsNumber(0, args);
			break;
		case "date":
			res = isNoArgsDate(0, args);
			break;
		}
		if(res) {
			throw new RhilaException(
				"key is not a " + type + ".");
		}
	}
	
	// key, Valueチェック.
	public static final void noArgsValueToError(
		Object... args) {
		if(isNoArgsToLength(2, args)) {
			throw new RhilaException("value is not set.");
		}
	}
	
	// key, Valueチェック.
	public static final void noArgsValueToTypeError(
		String type, Object... args) {
		noArgsValueToError(args);
		boolean res = false;
		switch(type) {
		case "string":
			res = isNoArgsString(1, args);
			break;
		case "boolean":
			res = isNoArgsBoolean(1, args);
			break;
		case "number":
			res = isNoArgsNumber(1, args);
			break;
		case "date":
			res = isNoArgsDate(1, args);
			break;
		}
		if(res) {
			throw new RhilaException(
				"value is not a " + type + ".");
		}
	}
}
