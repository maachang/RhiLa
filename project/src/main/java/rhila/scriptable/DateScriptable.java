package rhila.scriptable;

import java.time.Instant;
import java.util.Date;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import rhila.RhilaException;
import rhila.lib.ArrayMap;
import rhila.lib.DateUtil;
import rhila.lib.NumberUtil;

/**
 * Date用Scriptable.
 * rhinoのNativeDateはprivate finalなオブジェクトで扱いづらいので
 * この処理に置き換える.
 */
@SuppressWarnings("deprecation")
public class DateScriptable extends java.util.Date
	implements RhinoScriptable<Date>, RhinoFunction {
	private static final long serialVersionUID = -1516184127284167794L;
	
    // lambda snapStart CRaC用.
    protected static final DateScriptable LOAD_CRAC = new DateScriptable();
	
	// instance可能なScriptable.
	private static final ArrayMap<String, Scriptable> instanceList;
	
	// メソッド名群(sort済み).
	private static final String[] FUNCTION_NAMES = new String[] {
		"getDate","getDay",
		"getFullYear","getHours",
		"getMilliseconds", "getMinutes","getMonth",
		"getSeconds","getTime",
		"getTimezoneOffset","getYear",
		"nano",
		"now",
		"setDate","setFullYear",
		"setHours","setMilliseconds",
		"setMinutes",
		"setMonth","setSeconds",
		"setTime","setYear",
		"toLocaleString",
		"toString"
	};
	
	// 初期設定.
	static {
		// 配列で直接追加.
		final int len = FUNCTION_NAMES.length * 2;
		Object[] list = new Object[len];
		for(int i = 0, j = 0; i < len; i += 2, j ++) {
			list[i] = FUNCTION_NAMES[j];
			list[i + 1] = new FunctionList(j);
		}
		instanceList = new ArrayMap<String, Scriptable>(list);
	}
		
	// 汎用表示.
	private static final String SYMBOL = "[Date]";
	
	// 元のDateオブジェクト.
	private Date date;
	
	// objectインスタンスリスト.
	private final ArrayMap<String, Object> objInsList =
		new ArrayMap<String, Object>();
	
	// Date.now()用Cache.
	private Object NOW = null;
	// Date.nano()用Cache.
	private Object NANO = null;

	
	// これがtrueの場合Scriptableのコンストラクタ呼び出しではない.
	private boolean staticFlag = true;
	
	// コンストラクタ.
	public DateScriptable() {
		this.date = new Date();
	}
	
	// コンストラクタ.
	public DateScriptable(Date date) {
		if(date == null) {
			throw new RhilaException("Argument not valid");
		}
		this.date = new Date(date.getTime());
	}
	
	// コンストラクタ.
	public DateScriptable(DateScriptable date) {
		if(date == null) {
			throw new RhilaException("Argument not valid");
		}
		this.date = new Date(date.date.getTime());
	}
	
	// コンストラクタ.
	public DateScriptable(long time) {
		this.date = new Date(time);
	}
	
	// コンストラクタ.
	public DateScriptable(Number time) {
		this.date = new Date(((Number)time).longValue());
	}
	
	// コンストラクタ.
	public DateScriptable(String ymd) {
		this.date = DateUtil.parseDate(ymd);
	}
	
	// コンストラクタ.
	// 0: yyyy 年
	// 1: MM 月
	// 2: dd 日
	// 3: HH 時
	// 4: mm 分
	// 5: ss 秒
	public DateScriptable(Object... args) {
		int yyyy = convNumOrDef(args, 0, 1900);
		int MM = convNumOrDef(args, 1, -1);
		int dd = convNumOrDef(args, 2, 0);
		int hh = convNumOrDef(args, 3, 0);
		int mm = convNumOrDef(args, 4, 0);
		int ss = convNumOrDef(args, 5, 0);
		this.date = new Date(yyyy, MM, dd, hh, mm, ss);
	}
	
	// 複数パラメータが設定されていて、数字の場合はその数字を取得.
	// 一方そうでない場合はdef値を返却.
	private static final int convNumOrDef(Object[] args, int no, int def) {
		return args.length >= no ?
			NumberUtil.parseInt(args[no]) : def;
	}
	
	// 元情報を取得.
	public Date getRaw() {
		return date;
	}
	
	@Override
	public String toString() {
		return SYMBOL;
	}
	
	//////////////////////
	// java.util.Date実装.
	//////////////////////
	
	@Override
	public boolean after(Date when) {
		return date.after(when);
	}
	@Override
	public boolean before(Date when) {
		return date.before(when);
	}
	@Override
	public Object clone() {
		return new DateScriptable(date.getTime());
	}
	@Override
	public int compareTo(Date anotherDate) {
		return date.compareTo(anotherDate);
	}
	@Override
	public boolean equals(Object obj) {
		return date.equals(obj);
	}
	@Override
	public int getDate() {
		return date.getDate();
	}
	@Override
	public int getDay() {
		return date.getDay();
	}
	@Override
	public int getHours() {
		return date.getHours();
	}
	@Override
	public int getMinutes() {
		return date.getMinutes();
	}
	@Override
	public int getMonth() {
		return date.getMonth();
	}
	@Override
	public int getSeconds() {
		return date.getSeconds();
	}
	@Override
	public long getTime() {
		return date.getTime();
	}
	@Override
	public int getTimezoneOffset() {
		return 0;
	}
	@Override
	public int getYear() {
		return date.getYear();
	}
	@Override
	public int hashCode() {
		return date.hashCode();
	}
	@Override
	public void setDate(int d) {
		date.setDate(d);
	}
	@Override
	public void setHours(int hours) {
		date.setHours(hours);
	}
	@Override
	public void setMinutes(int minutes) {
		date.setMinutes(minutes);
	}
	@Override
	public void setMonth(int month) {
		date.setMonth(month);
	}
	@Override
	public void setSeconds(int seconds) {
		date.setSeconds(seconds);
	}
	@Override
	public void setTime(long time) {
		date.setTime(time);
	}
	@Override
	public void setYear(int year) {
		date.setYear(year);
	}
	@Override
	public Instant toInstant() {
		return date.toInstant();
	}
	@Override
	public String toLocaleString() {
		return date.toLocaleString();
	}
	// 拡張.
	public void setFullYear(int y) {
		date.setYear(y + 1900);
	}
	public int getFullYear() {
		return date.getYear() + 1900;
	}
	
	// ミリ秒設定.
	public void setMilliseconds(int sss) {
		// 1000の単位で丸めてセット.
		date = new Date(
			((long)(date.getTime() / 1000L) * 1000L)
			+ (sss % 1000));
	}
	
	// ミリ秒を取得.
	public long getMilliseconds() {
		long t = date.getTime();
		return (t - ((long)(t / 1000L)) * 1000L);
	}
	
	
	
	///////////
	// js実装.
	///////////
	
	@Override
	public String getName() {
		return "Date";
	}
	
	@Override
	public Object get(String arg0, Scriptable arg1) {
		return getFunction(arg0);
	}
		
	// function取得.
	private final Object getFunction(String name) {
		// コンストラクタ呼び出しでない場合.
		if(staticFlag) {
			if("now".equals(name)) {
				if(NOW == null) {
					NOW = instanceList.get(name);
				}
				return NOW;
			} else if("nano".equals(name)) {
				if(NANO == null) {
					NANO = instanceList.get(name);
				}
				return NANO;
			}
			return null;
		}

		// オブジェクト管理の生成Functionを取得.
		Object ret = objInsList.get(name);
		// 存在しない場合.
		if(ret == null) {
			// static管理のオブジェクトを取得.
			ret = instanceList.get(name);
			// 存在する場合.
			if(ret != null) {
				// オブジェクト管理の生成Functionとして管理.
				ret = ((AbstractRhinoFunctionInstance)ret)
					.getInstance(this);
				objInsList.put(name, ret);
			}
		}
		return ret;
	}
	
	// [js]Function.
	@Override
	public final Object function(Context arg0, Scriptable arg1, Scriptable arg2, Object[] arg3) {
		return SYMBOL;
	}
	
	// [js]コンストラクタ.
	@Override
	public final Scriptable newInstance(Context arg0, Scriptable arg1, Object[] arg2) {
		try {
			return newInstance(arg2);
		} catch(RhilaException rwe) {
			throw rwe;
		} catch(Throwable t) {
			throw new RhilaException(t);
		}
	}
	
	public static final DateScriptable newInstance(Object... args) {
		DateScriptable ret = null;
		if(args == null || args.length == 0) {
			// 空のDate.
			ret = new DateScriptable();
		} else if(args.length == 1) {
			Object o = args[0];
			if(o instanceof Date) {
				// Date指定.
				ret = new DateScriptable((Date)o);
			} else if(o instanceof DateScriptable) {
				// DateScriptable指定.
				ret = new DateScriptable((DateScriptable)o);
			} else if(o instanceof Number) {
				// Number指定.
				ret = new DateScriptable(((Number)o).longValue());
			} else if(o instanceof String) {
				// 文字変換.
				ret = new DateScriptable((String)o);
			}
		}
		// 複数パラメータの場合.
		if(ret == null) {
			ret = new DateScriptable(args);
		}
		ret.staticFlag = false;
		return ret;
	}
	
	// functionリストを生成.
	private static final class FunctionList extends AbstractRhinoFunctionInstance {
	    // lambda snapStart CRaC用.
	    @SuppressWarnings("unused")
		protected static final FunctionList LOAD_CRAC = new FunctionList();
	    
		private DateScriptable src;
		private int type;
		private String typeString;
		
		// 新しいインスタンスを生成.
		public final Scriptable getInstance(Object... args) {
			FunctionList ret = new FunctionList(type);
			ret.src = (DateScriptable)args[0];
			return ret;
		}
		
		protected FunctionList() {}
		
		// コンストラクタ.
		protected FunctionList(int type) {
			this.type = type;
			this.typeString = FUNCTION_NAMES[type];
		}
		
		// メソッド実行.
		public Object function(
			Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if (args.length <= 0) {
				switch(type) {
				case 0: //"getDate":
					return src.getDate();
				case 1: //"getDay":
					return src.getDay();
				case 2: //"getFullYear":
					return src.getYear() + 1900;
				case 3: //"getHours":
					return src.getHours();
				case 4: // "getMilliseconds"
					return src.getMilliseconds();
				case 5: //"getMinutes":
					return src.getMinutes();
				case 6: //"getMonth":
					return src.getMonth();
				case 7: //"getSeconds":
					return src.getSeconds();
				case 8: //"getTime":
					return src.getTime();
				case 9: //"getTimezoneOffset":
					return src.getTimezoneOffset();
				case 10: //"getYear":
					return src.getYear();
				case 11: //"nano":
					return System.nanoTime();
				case 12: //"now":
					return System.currentTimeMillis();
				case 22: //"toLocaleString":
					return src.toLocaleString();
				case 23: //"toString":
					//return DateUtil.toISO8601(src);
					return DateUtil.toUTC(src);
				}
			} else {
				// 引数が必要な場合.
				Object o = args[0];
				switch (type) {
				case 13: //"setDate":
					src.setDate(NumberUtil.parseInt(o));
					return src;
				case 14: //"setFullYear":
					src.setYear(NumberUtil.parseInt(o) - 1900);
					return src;
				case 15: //"setHours":
					src.setHours(NumberUtil.parseInt(o));
					return src;
				case 16: //"setMilliseconds"
					src.setMilliseconds(NumberUtil.parseInt(o));
					return src;
				case 17: //"setMinutes":
					src.setMinutes(NumberUtil.parseInt(o));
					return src;
				case 18: //"setMonth":
					src.setMonth(NumberUtil.parseInt(o));
					return src;
				case 19: //"setSeconds":
					src.setSeconds(NumberUtil.parseInt(o));
					return src;
				case 20: //"setTime":
					src.setTime(NumberUtil.parseLong(o));
					return src;
				case 21: //"setYear":
					src.setYear(NumberUtil.parseInt(o));
					return src;
				}
			}
			// プログラムの不具合以外にここに来ることは無い.
			throw new RhilaException(
				"An unspecified error (type: " + type + ") occurred");
		}

		@Override
		public String getName() {
			return typeString;
		}		
	}
}
