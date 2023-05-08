package rhila.lib.http;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.Map.Entry;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import rhila.RhilaException;
import rhila.lib.ArrayMap;
import rhila.lib.NumberUtil;
import rhila.lib.ObjectUtil;
import rhila.lib.http.HttpCookieValue.HttpCookieValueScriptable;
import rhila.scriptable.AbstractRhinoFunction;
import rhila.scriptable.AbstractRhinoFunctionInstance;
import rhila.scriptable.LowerKeyMapScriptable;

/**
 * Httpヘッダ.
 */
public final class HttpHeader {
    // lambda snapStart CRaC用.
    protected static final HttpHeader LOAD_CRAC = new HttpHeader();
    
	// キーなし.
	private static final Object[] ZERO_KEYS = new Object[0];
    
	// instance可能なScriptable.
	private static final ArrayMap<String, Scriptable> instanceList;
    
    // [js]functions.
	private static final String[] FUNCTION_NAMES = new String[] {
		"clear"
		,"clearCookie"
		,"clearHeader"
		,"getContentLength"
		,"getContentType"
		,"getCookie"
		,"getCookieKeys"
		,"getCookieSize"
		,"getHeader"
		,"getHeaderKeys"
		,"getHeaderSize"
		,"removeCookie"
		,"removeHeader"
		,"setContentLength"
		,"setContentType"
		,"setCookie"
		,"setHeader"
		,"toString"
	};
	
	static {
		// 配列で直接追加.
		final int len = FUNCTION_NAMES.length * 2;
		Object[] list = new Object[len];
		for(int i = 0, j = 0; i < len; i += 2, j ++) {
			list[i] = FUNCTION_NAMES[j];
			list[i + 1] = new HttpHeaderFunctions(j);
		}
		instanceList = new ArrayMap<String, Scriptable>(list);
	}
	
	// objectインスタンスリスト.
	private final ArrayMap<String, Object> objInsList =
		new ArrayMap<String, Object>();
        
	// headerValue.
	private LowerKeyMapScriptable headers = null;
	// cookieValue.
	private LowerKeyMapScriptable cookies = null;
	
	// header情報を取得.
	private final LowerKeyMapScriptable headers() {
		if(headers == null) {
			headers = new LowerKeyMapScriptable();
		}
		return headers;
	}
	
	// cookie情報を取得.
	private final LowerKeyMapScriptable cookies() {
		if(cookies == null) {
			cookies = new LowerKeyMapScriptable();
		}
		return cookies;
	}
	
	// クリアー.
	public void clear() {
		clearHeader();
		clearCookie();
	}
	
	// cookieをすべて削除.
	public void clearHeader() {
		headers = null;
	}
	
	// cookieをすべて削除.
	public void clearCookie() {
		cookies = null;
	}
	
	// ヘッダ取得.
	public String getHeader(String key) {
		if(headers == null) {
			return null;
		}
		return (String)headers.get(key);
	}
	
	// ヘッダ設定.
	public boolean setHeader(String key, String value) {
		headers().put(key, value);
		return true;
	}
	
	// ヘッダ削除.
	public boolean removeHeader(String key) {
		if(headers == null) {
			return false;
		}
		headers.remove(key);
		return true;
	}
	
	// ヘッダキー名群を取得.
	public Object[] getHeaderKeys() {
		if(headers == null) {
			return ZERO_KEYS;
		}
		return headers.getIds();
	}
	
	// ヘッダーキー登録数を取得.
	public int getHeaderSize() {
		if(headers == null) {
			return 0;
		}
		return headers.size();
	}
	
	// コンテンツタイプを取得.
	public String getContentType() {
		return (String)headers.get("content-type");
	}
	
	// コンテンツタイプを設定.
	public HttpHeader setContentType(String value) {
		if(value == null) {
			headers().remove("content-type");
			return this;
		}
		headers().put("content-type", value);
		return this;
	}
	
	// コンテンツ長を取得.
	public long getContentLength() {
		if(headers == null) {
			return -1L;
		}
		String s = (String)headers.get("content-length");
		if(s == null || !NumberUtil.isNumeric(s)) {
			return -1L;
		}
		return NumberUtil.parseLong(s);
	}
	
	// コンテンツ長を設定.
	public HttpHeader setContentLength(Object o) {
		if(o == null) {
			headers().remove("content-length");
			return this;
		}
		headers().put("content-length", String.valueOf(o));
		return this;
	}
	
	// 1つのCookie要素を取得.
	public HttpCookieValueScriptable getCookie(String key) {
		if(cookies == null) {
			return null;
		}
		return (HttpCookieValueScriptable)cookies.get(key);
	}
	
	// cookie内容を設定.
	//   value="value; Max-Age=2592000; Secure;"
	// ※必ず先頭文字は "value;" 必須.
	// または
	//   value={value: value, "Max-Age": 2592000, Secure: true}
	// のような感じで設定します.
	public boolean setCookie(String key, Object value) {
		if(key == null || (key = key.trim()).isEmpty()) {
			return false;
		}
		if(value instanceof HttpCookieValueScriptable) {
			cookies().put(key, value);
			return true;
		} else if(value instanceof HttpCookieValue) {
			cookies().put(key,
				HttpCookieValueScriptable.getInstance(
					(HttpCookieValue)value));
		} else if(value != null) {
			Object[] args;
			if(value.getClass().isArray()) {
				final int len = Array.getLength(value);
				args = new Object[len];
				System.arraycopy(value, 0, args, 0, len);
			} else {
				args = new Object[] { value };
			}
			Object v = HttpCookieValueScriptable
				.LOAD_CRAC.newInstance(null, null, args);
			cookies().put(key, v);
			return true;
		}
		return false;
	}
		
	// 指定cookieを削除.
	public boolean removeCookie(String key) {
		if(cookies == null) {
			return false;
		}
		cookies.remove(key);
		return true;
	}
	
	// Cookieキー名群を取得.
	public Object[] getCookieKeys() {
		if(cookies == null) {
			return ZERO_KEYS;
		}
		return cookies.getIds();
	}
	
	// Cookieキー登録数を取得.
	public int getCookieSize() {
		if(cookies == null) {
			return 0;
		}
		return cookies.size();
	}
	
    // [cookie情報をset-cookie形式で出力.
    protected final void toStringByGetCookie(StringBuilder out) {
		if(cookies == null) {
			return;
		}
		boolean first = true;
		HttpCookieValueScriptable em;
        Entry<String, Object> e;
        Iterator<Entry<String, Object>> it = cookies.entrySet().iterator();
        // get-cookie開始.
    	out.append("cookie:");
        while(it.hasNext()) {
        	e = it.next();
        	em = (HttpCookieValueScriptable)e.getValue();
        	if(!first) {
        		// firstじゃない場合セット.
        		out.append(";");
        	}
        	// 1つのcookieのkey/valueを出力.
        	em.getSrc().toStringByGetCookie(out);
        	// firstでないことをセット.
        	first = false;
        }
        // cookie: の終端をセット.
        out.append("\r\n");
		
    }
	
    // [cookie情報をset-cookie形式で出力.
    protected final void toStringBySetCookie(StringBuilder out) {
		if(cookies == null) {
			return;
		}
		HttpCookieValueScriptable em;
        Entry<String, Object> e;
        Iterator<Entry<String, Object>> it = cookies.entrySet().iterator();
        while(it.hasNext()) {
        	e = it.next();
        	em = (HttpCookieValueScriptable)e.getValue();
        	out.append("set-cookie:");
        	em.getSrc().toStringBySetCookie(out);
        }
    }
    
    @Override
    public String toString() {
    	StringBuilder buf = new StringBuilder();
    	toString(true, buf);
    	return buf.toString();
    }
    
    // ヘッダ情報を文字列取得.
    // response = trueの場合は、HttpResponse返却としてヘッダ出力します.
    public void toString(boolean response, StringBuilder out) {
		// ヘッダを展開.
    	if(headers != null) {
    		Entry<String, Object> e;
    		Iterator<Entry<String, Object>> it =
    			headers.entrySet().iterator();
    		while(it.hasNext()) {
    			e = it.next();
    			out.append(ObjectUtil.encodeURIComponent(e.getKey()))
    				.append(":").append(
    					ObjectUtil.encodeURIComponent((String)e.getValue()))
    				.append("\r\n");
    		}
    	}
    	// cookieヘッダ.
    	if(cookies != null) {
    		// HttpResponseで出力.
    		if(response) {
    			// set-cookieで出力.
    			toStringBySetCookie(out);
    		// HttpRequestで出力.
    		} else {
    			// cookieでkey/value出力.
    			toStringByGetCookie(out);
    		}
    	}
    }
	
	// keyチェック.
	private static final void checkArgsKey(Object[] args) {
		if(args == null || args.length < 1) {
			throw new RhilaException("key is not set.");
		}
		if(!(args[0] instanceof String)) {
			throw new RhilaException("key must be a string.");
		}
	}
	
	// key, Valueチェック.
	private static final void checkArgsValue(Object[] args) {
		if(args == null || args.length < 2) {
			throw new RhilaException("value is not set.");
		}
		if(!(args[1] instanceof String)) {
			throw new RhilaException("value must be a string.");
		}
	}
	
	// args配列からcookie設定.
	private static final void setCookieToArgs(HttpHeader obj, Object[] args) {
		checkArgsKey(args);
		if(args.length < 2) {
			throw new RhilaException("key, value setting is required.");
		}
		String key = (String)args[0];
		Object[] value;
		if(args.length == 2) {
			value = new Object[] {args[1] };
		} else {
			final int len = args.length;
			value = new Object[len - 1];
			System.arraycopy(args, 1, value, 0, len - 1);
		}
		Object v = HttpCookieValueScriptable
			.LOAD_CRAC.newInstance(null, null, value);
		obj.cookies().put(key, v);		
	}
	
	// [js]HttpStatusFunctions.
	private static final class HttpHeaderFunctions
		extends AbstractRhinoFunctionInstance {
	    // lambda snapStart CRaC用.
		@SuppressWarnings("unused")
		protected static final HttpHeaderFunctions LOAD_CRAC = new HttpHeaderFunctions();
		
		private int type;
		private String typeString;
		private HttpHeader object;
		
		// コンストラクタ.
		private HttpHeaderFunctions() {}
		
		// コンストラクタ.
		private HttpHeaderFunctions(int type) {
			this.type = type;
			this.typeString = FUNCTION_NAMES[type];
		}
		
		// 新しいインスタンスを生成.
		public final Scriptable getInstance(Object... args) {
			HttpHeaderFunctions ret = new HttpHeaderFunctions(type);
			ret.object = (HttpHeader)args[0];
			return ret;
		}
		
		@Override
		public String getName() {
			return typeString;
		}
		
		// メソッド実行.
		public Object function(
			Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			switch(type) {
			case 0: //"clear"
				object.clear();
				return null;
			case 1: //"clearCookie"
				object.clearCookie();
				return null;
			case 2: //"clearHeader"
				object.clearHeader();
				return null;
			case 3: //"getContentLength"
				return object.getContentLength();
			case 4: //"getContentType"
				return object.getContentType();
			case 5: //"getCookie"
				checkArgsKey(args);
				return object.getCookie((String)args[0]);
			case 6: //"getCookieKeys"
				return object.getCookieKeys();
			case 7: //"getCookieSize"
				return object.getCookieSize();
			case 8: //"getHeader"
				checkArgsKey(args);
				return object.getHeader((String)args[0]);
			case 9: //"getHeaderKeys"
				return object.getHeaderKeys();
			case 10: //"getHeaderSize"
				return object.getHeaderSize();
			case 11: //"removeCookie"
				checkArgsKey(args);
				return object.removeHeader((String)args[0]);
			case 12: //"removeHeader"
				checkArgsKey(args);
				return object.removeHeader((String)args[0]);
			case 13: //"setContentLength"
				object.setContentLength(args[0]);
				return null;
			case 14: //"setContentType"
				object.setContentType((String)args[0]);
				return null;
			case 15: //"setCookie"
				setCookieToArgs(object, args);
				return null;
			case 16: //"setHeader"
				checkArgsKey(args);
				checkArgsValue(args);
				object.setHeader((String)args[0], (String)args[1]);
				return null;
			case 17: //"toString"
				return object.toString();
			}
			// プログラムの不具合以外にここに来ることは無い.
			throw new RhilaException(
				"An unspecified error (type: " + type + ") occurred");
		}
	}
	
	// [js]HttpStatus.
	public static final class HttpHeaderScriptable extends AbstractRhinoFunction {
	    // lambda snapStart CRaC用.
	    protected static final HttpHeaderScriptable LOAD_CRAC = new HttpHeaderScriptable();
	    
		protected HttpHeaderScriptable() {}
		protected HttpHeader src = null;
		private boolean staticFlag = true;
		
		@Override
		public String getClassName() {
			return "HttpHeaderScriptable";
		}
		
		// 元のオブジェクトを取得.
		public HttpHeader getSrc() {
			return src;
		}
		
		// new HttpHeaderScriptable();
		@Override
		public Scriptable newInstance(Context arg0, Scriptable arg1, Object[] arg2) {
			return newInstance(new HttpHeader());
		}
		
		// new HttpHeaderScriptable();
		public static final HttpHeaderScriptable newInstance(HttpHeader src) {
			// 新しいScriptableオブジェクトを生成.
			HttpHeaderScriptable ret = new HttpHeaderScriptable();
			// 元のオブジェクトを設定.
			ret.src = src;
			ret.staticFlag = false;
			return ret;
		}

		@Override
		public String getName() {
			return "[HttpHeader]";
		}
		
		@Override
		public String toString() {
			return getName();
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
