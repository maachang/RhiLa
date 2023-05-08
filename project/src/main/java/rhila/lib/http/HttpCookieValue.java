package rhila.lib.http;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import rhila.RhilaException;
import rhila.lib.ArrayMap;
import rhila.lib.DateUtil;
import rhila.lib.ObjectUtil;
import rhila.scriptable.AbstractRhinoFunction;
import rhila.scriptable.AbstractRhinoFunctionInstance;
import rhila.scriptable.LowerKeyMapScriptable;

/**
 * HttpCookieValue.
 *  {value: value, "Max-Age": 2592000, Secure: true}的な情報の振る舞いを
 * 提供するオブジェクト.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class HttpCookieValue {
    // lambda snapStart CRaC用.
    protected static final HttpCookieValue LOAD_CRAC = new HttpCookieValue();
    
	// instance可能なScriptable.
	private static final ArrayMap<String, Scriptable> instanceList;
	
	// 呼び出しFunctionをセット.
	static {
		instanceList = new ArrayMap<String, Scriptable>(
			"getKey", GetKey.LOAD_CRAC
			,"getOptino", GetOption.LOAD_CRAC
			,"getValue", GetValue.LOAD_CRAC
			,"set", Set.LOAD_CRAC
			,"toString", ToString.LOAD_CRAC
		);
	}
	
	// objectインスタンスリスト.
	private final ArrayMap<String, Object> objInsList =
		new ArrayMap<String, Object>();
	
    private String key = null;
	private LowerKeyMapScriptable value = null;
	
	// コンストラクタ.
	public HttpCookieValue() {}
	
	// コンストラクタ.
	public HttpCookieValue(String key, Object value) {
		set(key, value);
	}
	
	// コンストラクタ.
	public HttpCookieValue(String key, Object value, Object... options) {
		set(key, value, options);
	}
	
	// valueの解釈.
	private static final String valueString(Object value) {
		if(value instanceof Date) {
			return DateUtil.toRfc822((Date)value);
		} else {
			return String.valueOf(value);
		}
	}
	
	// cookie内容 "key=value" の要素をパース.
	// out {"key": "value"} がセットされます.
	// value "key=value" のような条件を設定します.
	private static final void parseElement(
		LowerKeyMapScriptable out, String value) {
	    int p = value.indexOf("=");
	    if(p == -1) {
	    	out.put(value, true);
	    } else {
	        out.put(value.substring(0, p),
	        	value.substring(p + 1));
	    }
	}
	
	// cookieのvalue内容をパース.
	//   value="value; Max-Age=2592000; Secure;"
	// ※必ず先頭文字は "value;" 必須.
	// または
	//   value={value: value, "Max-Age": 2592000, Secure: true}
	// のような感じで設定します.
	protected static final LowerKeyMapScriptable parseValue(Object value) {
        // 文字の場合.
        if(value instanceof String) {
        	LowerKeyMapScriptable ret = new LowerKeyMapScriptable();
            // 文字列から LowerKeyMapScriptable に変換.
            String n;
            final String[] list = ((String)value).split(";");
            final int len = list.length;
            for(int i = 0; i < len; i ++) {
                n = list[i].trim();
                if(i == 0) {
                    ret.put("value", n);
                } else {
                	parseElement(ret, n);
                }
            }
            return ret;
        // Mapの場合.
        } else if(value instanceof Map) {
        	LowerKeyMapScriptable ret = new LowerKeyMapScriptable();
        	Entry e;
        	Map mp = (Map)value;
        	Iterator<Entry> it = mp.entrySet().iterator();
        	while(it.hasNext()) {
        		e = it.next();
        		ret.put(String.valueOf(e.getKey()),
        			valueString(e.getValue()));
        	}
        	return ret;
        }
        return null;
	}
		
	// cookieセット.
	public void set(String key, Object value) {
		if(!ObjectUtil.useString(key)) {
			throw new RhilaException("key does not exist.");
		}
		LowerKeyMapScriptable v = parseValue(value);
		if(v == null) {
			v = new LowerKeyMapScriptable();
			v.put("value", "");
		} else {
			v.put(key, valueString(value));
		}
		this.key = key;
		this.value = v;
	}
	
	// cookieセット.
	public void set(String key, Object value, Object... options) {
		if(!ObjectUtil.useString(key)) {
			throw new RhilaException("key does not exist.");
		}
		LowerKeyMapScriptable v = new LowerKeyMapScriptable();
		v.put("value", valueString(value));
		final int len = options == null ? 0 : options.length;
		for(int i = 0; i < len; i += 2) {
			v.put(String.valueOf(options[i]),
				valueString(options[i + 1]));
		}
		this.key = key;
		this.value = v;
	}
	
	// Object[] args でHttpCookieValue.setを呼び出す処理.
	// jsからの呼び出しで利用する.
	protected static final boolean setArgs(HttpCookieValue out, Object[] args) {
		if(args == null || args.length < 2 || !(args[0] instanceof String)) {
			return false;
		}
		String key = (String)args[0];
		Object value = args[1];
		if(args.length >= 3) {
			int len = args.length;
			Object[] params = new Object[len - 2];
			System.arraycopy(args, 2, params, 0, params.length);
			out.set(key, value, params);
		} else {
			out.set(key, value);
		}
		return true;
	}
	
	// key情報を取得.
	public String getKey() {
		return key;
	}
	
	// value情報を取得.
	public String getValue() {
		return (String)value.get("value");
	}
	
	// CookieのOptionを取得.
	public String getOption(String name) {
		if("value".equals(name)) {
			return null;
		}
		Object ret = value.get(name);
		if(ret != null) {
			if(ret instanceof Boolean && (Boolean)ret == true) {
				if("samesite".equals(name.toLowerCase())) {
					return "Lax";
				}
				return "";
			}
			return (String)ret;
		}
		return null;
	}
	
	// [cookie:]getCookieでの文字列化.
	private final void toStringByKeyValueString(StringBuilder out) {
        String v = value.get("value") == null ?
            	"" : (String)value.get("value");
        // key=value条件.
        out.append(ObjectUtil.encodeURIComponent(key))
            .append("=").append(ObjectUtil.encodeURIComponent(v));
	}
	
	// [cookie:]getCookieでの文字列化.
	protected void toStringByGetCookie(StringBuilder out) {
		toStringByKeyValueString(out);
	}
	
	// [set-cookie:]setCookieでの文字列化.
	protected void toStringBySetCookie(StringBuilder out) {
        Entry<String, Object> e;
        toStringByKeyValueString(out);
        // cookie要素を連結.
        Iterator<Entry<String, Object>> itn = value.entrySet().iterator();
        while(itn.hasNext()) {
        	e = itn.next();
    		// valueは無視.
        	if("value".equals(e.getKey())) {
        		continue;
        	} else if(e.getValue() instanceof Boolean) {
        		if("samesite".equals(e.getKey())) {
        			out.append(";SameSite=Lax");            			
        		} else {
        			out.append(";").append(
        				ObjectUtil.encodeURIComponent(e.getKey()));
        		}
        	} else {
        		out.append(";").append(
        				ObjectUtil.encodeURIComponent(e.getKey()))
        			.append("=").append(
        				ObjectUtil.encodeURIComponent((String)e.getValue()));
        	}
        }
        out.append("\r\n");
	}
	
	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		toStringBySetCookie(buf);
		return buf.toString();
	}
	
	// Key取得.
	private static final class GetKey extends AbstractRhinoFunctionInstance {
	    // lambda snapStart CRaC用.
	    protected static final GetKey LOAD_CRAC = new GetKey();
		private HttpCookieValue src;
		
		// 新しいインスタンスを生成.
		public final Scriptable getInstance(Object... args) {
			GetKey ret = new GetKey();
			ret.src = (HttpCookieValue)args[0];
			return ret;
		}
		
		protected GetKey() {}
		@Override
		public String getName() {
			return "getKey";
		}

		@Override
		public Object function(
			Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			return src.getKey();
		}
	}
	
	// Option取得.
	private static final class GetOption extends AbstractRhinoFunctionInstance {
	    // lambda snapStart CRaC用.
	    protected static final GetOption LOAD_CRAC = new GetOption();
		private HttpCookieValue src;
		
		// 新しいインスタンスを生成.
		public final Scriptable getInstance(Object... args) {
			GetOption ret = new GetOption();
			ret.src = (HttpCookieValue)args[0];
			return ret;
		}
		
		protected GetOption() {}
		@Override
		public String getName() {
			return "getValue";
		}

		@Override
		public Object function(
			Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			return src.getOption((String)args[0]);
		}
	}
	
	// Value取得.
	private static final class GetValue extends AbstractRhinoFunctionInstance {
	    // lambda snapStart CRaC用.
	    protected static final GetValue LOAD_CRAC = new GetValue();
		private HttpCookieValue src;
		
		// 新しいインスタンスを生成.
		public final Scriptable getInstance(Object... args) {
			GetValue ret = new GetValue();
			ret.src = (HttpCookieValue)args[0];
			return ret;
		}
		
		protected GetValue() {}
		@Override
		public String getName() {
			return "getValue";
		}

		@Override
		public Object function(
			Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			return src.getValue();
		}
	}
	
	// Set処理.
	private static final class Set extends AbstractRhinoFunctionInstance {
	    // lambda snapStart CRaC用.
	    protected static final Set LOAD_CRAC = new Set();
		private HttpCookieValue src;
		
		// 新しいインスタンスを生成.
		public final Scriptable getInstance(Object... args) {
			Set ret = new Set();
			ret.src = (HttpCookieValue)args[0];
			return ret;
		}
		
		protected Set() {}
		@Override
		public String getName() {
			return "set";
		}

		@Override
		public Object function(
			Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			return setArgs(src, args);
		}
	}

	// 文字列変換処理.
	private static final class ToString extends AbstractRhinoFunctionInstance {
	    // lambda snapStart CRaC用.
	    protected static final ToString LOAD_CRAC = new ToString();
		private HttpCookieValue src;
		
		// 新しいインスタンスを生成.
		public final Scriptable getInstance(Object... args) {
			ToString ret = new ToString();
			ret.src = (HttpCookieValue)args[0];
			return ret;
		}
		
		protected ToString() {}
		@Override
		public String getName() {
			return "toString";
		}

		@Override
		public Object function(
			Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			return src.toString();
		}
	}
	
	// [js]HttpCookieValue.
	public static final class HttpCookieValueScriptable extends AbstractRhinoFunction {
	    // lambda snapStart CRaC用.
	    protected static final HttpCookieValueScriptable LOAD_CRAC =
	    	new HttpCookieValueScriptable();
	    
		protected HttpCookieValueScriptable() {}
		protected HttpCookieValue src = null;
		private boolean staticFlag = true;
		
		@Override
		public String getClassName() {
			return "HttpCookieValueScriptable";
		}
		
		// 元のオブジェクトを取得.
		public HttpCookieValue getSrc() {
			return src;
		}
		
		// new HttpCookieValueScriptable();
		@Override
		public Scriptable newInstance(Context arg0, Scriptable arg1, Object[] arg2) {
			// 新しいScriptableオブジェクトを生成.
			HttpCookieValueScriptable ret = new HttpCookieValueScriptable();
			ret.src = new HttpCookieValue();
			setArgs(ret.src, arg2);
			ret.staticFlag = false;
			return ret;
		}
		
		// new HttpCookieValueScriptable();
		public static final HttpCookieValueScriptable getInstance(HttpCookieValue src) {
			HttpCookieValueScriptable ret = new HttpCookieValueScriptable();
			ret.src = src;
			ret.staticFlag = false;
			return ret;
		}

		@Override
		public String getName() {
			return "[HttpCookieValue]";
		}
		
		@Override
		public String toString() {
			return getName();
		}
		
		@Override
		public Object[] getIds() {
			return src.value.getIds();
		}
		
		@Override
		public Object get(String arg0, Scriptable arg1) {
			return getFunction(arg0);
		}
		
		// function取得.
		private final Object getFunction(String name) {
			// staticの場合.
			if(staticFlag) {
				return null;
			}
			// オブジェクト管理の生成Functionを取得.
			Object ret = src.objInsList.get(name);
			// 存在しない場合.
			if(ret == null) {
				// static管理のオブジェクトを取得.
				ret = instanceList.get(name);
				// 存在する場合.
				if(ret != null) {
					// オブジェクト管理の生成Functionとして管理.
					ret = ((AbstractRhinoFunctionInstance)ret)
						.getInstance(src);
					src.objInsList.put(name, ret);
				}
			}
			return ret;
		}
	}	
}
