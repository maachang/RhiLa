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
		"change","clear",
		"getDate","getDay",
		"getFullYear","getHours",
		"getMilliseconds", "getMinutes","getMonth",
		"getSeconds","getTime",
		"getTimezoneOffset","getYear",
		"nano","now",
		"range",
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
		try {
			this.date = DateUtil.stringToYmdHms(ymd);			
		} catch(Exception e) {}
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
	
	public String toString(String model) {
		return DateUtil.toUTC(date, model);
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
		return date.getTimezoneOffset();
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
	public DateScriptable setFullYear(int y) {
		date.setYear(y + 1900);
		return this;
	}
	public int getFullYear() {
		return date.getYear() + 1900;
	}
	
	// ミリ秒設定.
	public DateScriptable setMilliseconds(int sss) {
		// 1000の単位で丸めてセット.
		date = new Date(
			((long)(date.getTime() / 1000L) * 1000L)
			+ (sss % 1000));
		return this;
	}
	
	// ミリ秒を取得.
	public long getMilliseconds() {
		long t = date.getTime();
		return (t - ((long)(t / 1000L)) * 1000L);
	}
	
	// 年月日や時分秒の増減を設定.
	// mode "year": 西暦を変更します.
	//      "month": 月を変更します.
	//      "date": 日を変更します.
	//      "week": 週を変更します.
	//      "hours": 時間を変更します.
	//      "minutes": 分を変更します.
	//      "seconds": 秒を変更します.
	// value 増減設定を行います.
	// 戻り値: このオブジェクトが返却されます.
	public DateScriptable change(String mode, int value) {
		if(mode == null) {
			throw new RhilaException("mode is not set.");
		}
		mode = mode.toLowerCase();
		if("year".equals(mode)) {
			date.setYear(date.getYear() + value);
		} else if("month".equals(mode)) {
			date.setMonth(date.getMonth() + value);
		} else if("date".equals(mode)) {
			date.setDate(date.getDate() + value);
		} else if("week".equals(mode)) {
			date.setDate(date.getDate() + (value * 7));
		} else if("hours".equals(mode)) {
			date.setHours(date.getHours() + value);
		} else if("minutes".equals(mode)) {
			date.setMinutes(date.getMinutes() + value);
		} else if("seconds".equals(mode)) {
			date.setSeconds(date.getSeconds() + value);
		} else {
			throw new RhilaException(
				"Condition for mode does not apply: " +
				mode);
		}
		return this;
	}
	
	// 年月日の開始、終了条件を取得.
	// mode "year": 年以下の開始終了日を取得.
	//      "month": 月以下の開始終了日を取得.
	//      "date": 日以下の開始終了日を取得.
	//      "week": 週の開始終了日を取得.
	// 戻り値: [0]開始日, [1]終了日.
	public DateScriptable[] range(String mode) {
		if(mode == null) {
			throw new RhilaException("mode is not set.");
		}
		Date start, end; 
		mode = mode.toLowerCase();
		if("year".equals(mode)) {
	        start = new Date(date.getYear(), 0, 1);
	        end = new Date(date.getYear() + 1, 0, 1);
	        end = new Date(end.getTime() - 1);
		} else if("month".equals(mode)) {
	        start = new Date(date.getYear(),
	        	date.getMonth(), 1);
	        end = new Date(date.getYear(),
	        	date.getMonth() + 1, 1);
	        end = new Date(end.getTime() - 1);
		} else if("date".equals(mode)) {
	        start = new Date(date.getYear(),
	        	date.getMonth(), date.getDate());
	        end = new Date(date.getYear(),
	        	date.getMonth(), date.getDate() + 1);
	        end = new Date(end.getTime() - 1);
		} else if("week".equals(mode)) {
	        int d = date.getDay();
	        DateScriptable s = new DateScriptable(date)
	        	.change("date", (d * -1));
	        DateScriptable e = new DateScriptable(date)
	        	.change("date", (6 - d));
	        return new DateScriptable[] {s, e};
		} else {
			throw new RhilaException(
				"Condition for mode does not apply: " +
				mode);
		}
        return new DateScriptable[] {
        	new DateScriptable(start),
        	new DateScriptable(end)
        };
	}
	
	// 年月日や時分秒の内容をクリア
	// mode "month": 月以下を0にします.
	//      "date": 日以下を0にします.
	//      "week": 週以下を0にします.
	//      "hours": 時間以下を0にします.
	//      "minutes": 分以下を0にします.
	//      "seconds": 秒以下を0にします.
	//      "milliseconds": 秒以下を0にします.
	// value 増減設定を行います.
	// 戻り値: このオブジェクトが返却されます.
	public DateScriptable clear(String mode) {
		if(mode == null) {
			throw new RhilaException("mode is not set.");
		}
		mode = mode.toLowerCase();
		if("month".equals(mode)) {
            this.setMonth(0);
            this.setDate(1);
            this.setHours(0);
            this.setMinutes(0);
            this.setSeconds(0);
            this.setMilliseconds(0);
		} else if("date".equals(mode)) {
            this.setDate(1);
            this.setHours(0);
            this.setMinutes(0);
            this.setSeconds(0);
            this.setMilliseconds(0);
		} else if("hours".equals(mode)) {
            this.setHours(0);
            this.setMinutes(0);
            this.setSeconds(0);
            this.setMilliseconds(0);
		} else if("minutes".equals(mode)) {
            this.setMinutes(0);
            this.setSeconds(0);
            this.setMilliseconds(0);
		} else if("seconds".equals(mode)) {
            this.setSeconds(0);
            this.setMilliseconds(0);
		} else if("milli".equals(mode) ||
			"milliseconds".equals(mode)) {
            this.setMilliseconds(0);
		} else {
			throw new RhilaException(
				"Condition for mode does not apply: " +
				mode);
		}
		return this;

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
			switch(type) {
			case 0: // "change":
				checkArgs(args, 0, "Date", "change");
				checkArgs(args, 1, "Date", "change");
				src.change(
					(String)args[0], NumberUtil.parseInt(args[1]));
				return src; 
			case 1: //"clear":
				checkArgs(args, 0, "Date", "clear");
				src.clear((String)args[0]);
				return src;
			case 2: //"getDate":
				return src.getDate();
			case 3: //"getDay":
				return src.getDay();
			case 4: //"getFullYear":
				return src.getYear() + 1900;
			case 5: //"getHours":
				return src.getHours();
			case 6: // "getMilliseconds"
				return src.getMilliseconds();
			case 7: //"getMinutes":
				return src.getMinutes();
			case 8: //"getMonth":
				return src.getMonth();
			case 9: //"getSeconds":
				return src.getSeconds();
			case 10: //"getTime":
				return src.getTime();
			case 11: //"getTimezoneOffset":
				return src.getTimezoneOffset();
			case 12: //"getYear":
				return src.getYear();
			case 13: //"nano":
				return System.nanoTime();
			case 14: //"now":
				return System.currentTimeMillis();
			case 15: //"range":
				checkArgs(args, 0, "Date", "range");
				DateScriptable[] list = src.range((String)args[0]);
				return new MapScriptable(
					"start", list[0], "end", list[1]);
			case 16: //"setDate":
				checkArgs(args, 0, "Date", "setDate");
				src.setDate(NumberUtil.parseInt(args[0]));
				return src;
			case 17: //"setFullYear":
				checkArgs(args, 0, "Date", "setFullYear");
				src.setYear(NumberUtil.parseInt(args[0]) - 1900);
				return src;
			case 18: //"setHours":
				checkArgs(args, 0, "Date", "setHours");
				src.setHours(NumberUtil.parseInt(args[0]));
				return src;
			case 19: //"setMilliseconds"
				checkArgs(args, 0, "Date", "setMilliseconds");
				src.setMilliseconds(NumberUtil.parseInt(args[0]));
				return src;
			case 20: //"setMinutes":
				checkArgs(args, 0, "Date", "setMinutes");
				src.setMinutes(NumberUtil.parseInt(args[0]));
				return src;
			case 21: //"setMonth":
				checkArgs(args, 0, "Date", "setMonth");
				src.setMonth(NumberUtil.parseInt(args[0]));
				return src;
			case 22: //"setSeconds":
				checkArgs(args, 0, "Date", "setSeconds");
				src.setSeconds(NumberUtil.parseInt(args[0]));
				return src;
			case 23: //"setTime":
				checkArgs(args, 0, "Date", "setTime");
				src.setTime(NumberUtil.parseLong(args[0]));
				return src;
			case 24: //"setYear":
				checkArgs(args, 0, "Date", "setYear");
				src.setYear(NumberUtil.parseInt(args[0]));
				return src;
			case 25: //"toLocaleString":
				return src.toLocaleString();
			case 26: //"toString":
				if(args.length > 0) {
					return DateUtil.toUTC(src, args[0]);
				}
				return DateUtil.toUTC(src);
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
