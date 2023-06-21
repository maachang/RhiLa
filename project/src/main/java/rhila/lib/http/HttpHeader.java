package rhila.lib.http;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import rhila.RhilaException;
import rhila.lib.ArrayMap;
import rhila.lib.JsValidate;
import rhila.lib.NumberUtil;
import rhila.lib.ObjectUtil;
import rhila.scriptable.AbstractRhinoFunction;
import rhila.scriptable.AbstractRhinoFunctionInstance;
import rhila.scriptable.LowerKeyMapScriptable;

/**
 * Httpヘッダ.
 */
public final class HttpHeader extends AbstractRhinoFunction {
    // lambda snapStart CRaC用.
    protected static final HttpHeader LOAD_CRAC = new HttpHeader(true);
    
	// キーなし.
	private static final Object[] ZERO_KEYS = new Object[0];
    
	// instance可能なScriptable.
	private static final ArrayMap<String, Scriptable> instanceList;
    
    // [js]functions.
	private static final String[] FUNCTION_NAMES = new String[] {
		"clear"
		,"clearCookie"
		,"clearHeader"
		,"getCharset"
		,"getContentLength"
		,"getContentType"
		,"getCookie"
		,"getCookieKeys"
		,"getCookieSize"
		,"getHeader"
		,"getHeaderKeys"
		,"getHeaderSize"
		,"getMimeType"
		,"isContentType"
		,"isGzip"
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
			list[i + 1] = new FunctionList(j);
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
	// staticフラグ.
	private boolean staticFlag = false;
	
	// コンストラクタ.
	public HttpHeader() {
		this(false);
	}
	
	// staticFlag付きコンストラクタ.
	protected HttpHeader(boolean staticFlag) {
		this.staticFlag = staticFlag;
	}
	
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
	public HttpHeader clear() {
		clearHeader();
		clearCookie();
		return this;
	}
	
	// cookieをすべて削除.
	public HttpHeader clearHeader() {
		headers = null;
		return this;
	}
	
	// cookieをすべて削除.
	public HttpHeader clearCookie() {
		cookies = null;
		return this;
	}
	
	// ヘッダ内容をコピー.
	public HttpHeader setHttpHeader(HttpHeader value) {
		setHeaders(value);
		setCookies(value);
		return this;
	}
	
	// ヘッダが存在するかチェック.
	public boolean isHeader(String key) {
		if(headers == null) {
			return false;
		}
		return headers.containsKey(key);
	}
	
	// ヘッダ取得.
	public String getHeader(String key) {
		if(headers == null) {
			return null;
		}
		return (String)headers.get(key);
	}
	
	// header群をセット.
	@SuppressWarnings("rawtypes")
	public int setHeaders(Map header) {
		int ret = 0;
		Iterator it = header.entrySet().iterator();
		Entry e;
		while(it.hasNext()) {
			e = (Entry)it.next();
			setHeader(
				e.getKey().toString(),
				e.getValue().toString());
			ret ++;
		}
		return ret;
	}
	
	// header群をセット.
	public int setHeaders(HttpHeader header) {
		// headerをセット.
		return setHeaders(header.headers().getRaw());
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
	
	// コンテンツタイプが存在するかチェック.
	public boolean isContentType() {
		return isHeader("content-type");
	}
	
	// コンテンツタイプを取得.
	public String getContentType() {
		if(headers == null) {
			return null;
		}
		return (String)headers.get("content-type");
	}
	
	// コンテンツタイプを設定.
	public HttpHeader setContentType(String value) {
		return setContentType(value, null);
	}
	
	// コンテンツタイプを設定.
	public HttpHeader setContentType(String value, String charset) {
		if(value == null) {
			headers().remove("content-type");
			return this;
		} else if(charset != null) {
			headers().put("content-type", value +
				";charset=" + charset);
		} else {
			headers().put("content-type", value);
		}
		return this;
	}
	
	// ContentTypeに設定されているMimeTypeを返却.
	public String getMimeType() {
		String contentType = getContentType();
		if(contentType == null) {
			return null;
		}
		int p = contentType.indexOf(";");
		if(p != -1) {
			return contentType.substring(0, p).trim();
		}
		return null;
	}
	
	// ContentTypeに設定されているCharsetを返却.
	public String getCharset() {
		return HttpUtil.getContentTypeToCharset(getContentType());
	}
	
	// bodyがgzipの場合.
	public boolean isGzip() {
		if(headers == null) {
			return false;
		}
		String value = (String)headers.get("content-encoding");
		if (value != null && "gzip".equals(value.toLowerCase())) {
			return true;
		}
		return false;
	}
	
	// コンテンツ長を取得.
	public boolean isContentLength() {
		return isHeader("content-length");
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
	
	// LambdaURLFunction用のHttpHeader取得.
	protected Map<String, String> toHeaderMap() {
		Map<String, String> ret = new HashMap<String, String>();
		if(headers == null) {
			return ret; 
		}
		Entry<String, Object> e;
		Iterator<Entry<String, Object>> it =
			headers.entrySet().iterator();
		while(it.hasNext()) {
			e = it.next();
			ret.put(e.getKey(), (String)e.getValue());
		}
		return ret;
	}
	
	// 1つのCookie要素を取得.
	public boolean isCookie(String key) {
		if(cookies == null) {
			return false;
		}
		return cookies.containsKey(key);
	}
	
	// 1つのCookie要素を取得.
	public HttpCookieValue getCookie(String key) {
		if(cookies == null) {
			return null;
		}
		return (HttpCookieValue)cookies.get(key);
	}
	
	// cookie内容を文字列でセット.
	// cookieKeyMode == trueの場合
	//   set-cookie: key=value; Max-Age=2592000; Secure;
	//   のヘッダ条件に対して以下のvalueを設定します.
	//     stringValue="key=value; Max-Age=2592000; Secure;"
	// cookieKeyMode == falseの場合
	//   cookie: key=value; key=value; key=value;
	//   のヘッダ条件に対して以下のvalueを設定します.
	//     stringValue="key=value; key=value; key=value; ...."
	public boolean setCookie(boolean cookieKeyMode, String stringValue) {
		// (httpResponse)set-cookieの場合.
		if(cookieKeyMode) {
			// stringValue="key=value; Max-Age=2592000; Secure;"
			int p = stringValue.indexOf("=");
			if(p == -1) {
				throw new RhilaException(
					"Not a set-cookie header value condition: " +
						stringValue);
			}
			return setCookie(
				ObjectUtil.decodeURIComponent(
					stringValue.substring(0, p).trim()),
				ObjectUtil.decodeURIComponent(
					stringValue.substring(p + 1).trim()));
		// (httpRequest)cookieの場合.
		} else {
			// stringValue="key=value; key=value; key=value; ...."
			boolean ret = false;
			boolean end = false;
			int b = 0, p, pp;
			while(true) {
				// key=value条件の終端が存在しない場合..
				if((p = stringValue.indexOf(";", b)) == -1) {
					end = true;
					p = stringValue.length();
				}
				ret = true;
				// key=valueの分離条件を取得.
				if((pp = stringValue.indexOf("=", b)) == -1) {
					throw new RhilaException(
						"Failed to get key=value: " + stringValue);
				}
				// 条件セット.
				setCookie(
					ObjectUtil.decodeURIComponent(
						stringValue.substring(b, pp).trim()),
					ObjectUtil.decodeURIComponent(
						stringValue.substring(pp + 1, p).trim()));
				if(end) {
					return ret;
				}
				b = p + 1;
			}
		}
	}
	
	// cookie群をセット.
	@SuppressWarnings("rawtypes")
	public int setCookies(Map cookie) {
		int ret = 0;
		Iterator it = cookie.entrySet().iterator();
		Entry e;
		while(it.hasNext()) {
			e = (Entry)it.next();
			setCookie(
				e.getKey().toString(),
				e.getValue().toString());
			ret ++;
		}
		return ret;
	}
	
	// cookie群をセット.
	public int setCookies(HttpHeader cookie) {
		// cookieをセット.
		return setHeaders(cookie.cookies().getRaw());
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
		if(value instanceof HttpCookieValue) {
			cookies().put(key, value);
			return true;
		} else if(value != null) {
			Object[] args;
			if(value.getClass().isArray()) {
				final int len = Array.getLength(value);
				args = new Object[len + 1];
				args[0] = key;
				System.arraycopy(value, 0, args, 1, len);
			} else {
				args = new Object[] { key, value };
			}
			Object v = HttpCookieValue
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
	
	// LambdaURLFunction用のSetCookie用.
	public List<String> toSetCookieArray() {
		List<String> ret = new ArrayList<String>();
		if(cookies == null) {
			return ret;
		}
		Entry<String, Object> e;
		Iterator<Entry<String, Object>> it =
			cookies.entrySet().iterator();
		while(it.hasNext()) {
			e = it.next();
			ret.add(e.getValue().toString());
		}
		return ret;
	}
    
    @Override
    public String toString() {
    	if(staticFlag) {
    		return getName();
    	}
    	return toString(true);
    }
    
    public String toString(boolean response) {
    	StringBuilder buf = new StringBuilder();
    	toString(response, buf);
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
    			out.append(e.getKey()).append(":").append(e.getValue());
    			out.append("\r\n");
    		}
    	}
    	// cookie出力.
    	toStringByCookie(response, out);
    }
    
	// cookieヘッダの出力.
    public final void toStringByCookie(boolean response, StringBuilder out) {
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
    
    // [cookie情報をset-cookie形式で出力.
    private final void toStringByGetCookie(StringBuilder out) {
		if(cookies == null) {
			return;
		}
		boolean first = true;
		HttpCookieValue em;
        Entry<String, Object> e;
        Iterator<Entry<String, Object>> it = cookies.entrySet().iterator();
        // get-cookie開始.
    	out.append("cookie:");
        while(it.hasNext()) {
        	e = it.next();
        	em = (HttpCookieValue)e.getValue();
        	if(!first) {
        		// firstじゃない場合セット.
        		out.append(";");
        	}
        	// 1つのcookieのkey/valueを出力.
        	em.toStringByGetCookie(out);
        	// firstでないことをセット.
        	first = false;
        }
        // cookie: の終端をセット.
        out.append("\r\n");
		
    }
	
    // [cookie情報をset-cookie形式で出力.
    private final void toStringBySetCookie(StringBuilder out) {
		if(cookies == null) {
			return;
		}
		HttpCookieValue em;
        Entry<String, Object> e;
        Iterator<Entry<String, Object>> it = cookies.entrySet().iterator();
        while(it.hasNext()) {
        	e = it.next();
        	em = (HttpCookieValue)e.getValue();
        	out.append("set-cookie:");
        	em.toStringBySetCookie(out);
        }
    }
	
	// args配列からcookie設定.
	private static final void setCookieToArgs(HttpHeader obj, Object[] args) {
		JsValidate.noArgsKeyToTypeError("string", args);
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
		Object v = HttpCookieValue
			.LOAD_CRAC.newInstance(null, null, value);
		obj.cookies().put(key, v);		
	}
	
	@Override
	public String getClassName() {
		return "HttpHeader";
	}
	
	// new HttpHeaderScriptable();
	@Override
	public Scriptable newInstance(Context arg0, Scriptable arg1, Object[] arg2) {
		// 新しいScriptableオブジェクトを生成.
		return new HttpHeader();
	}
	
	@Override
	public String getName() {
		return "[HttpHeader]";
	}
	
	@Override
	public Object get(String arg0, Scriptable arg1) {
		return getFunction(arg0);
	}
	
	// function取得.
	private final Object getFunction(String name) {
		// staticの場合.
		if(staticFlag) {
			if("toString".equals(name)) {
				return toString();
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
	
	// [js]HttpStatusFunctions.
	private static final class FunctionList
		extends AbstractRhinoFunctionInstance {
	    // lambda snapStart CRaC用.
		@SuppressWarnings("unused")
		protected static final FunctionList LOAD_CRAC =
			new FunctionList();
		
		private int type;
		private String typeString;
		private HttpHeader object;
		
		// コンストラクタ.
		private FunctionList() {}
		
		// コンストラクタ.
		private FunctionList(int type) {
			this.type = type;
			this.typeString = FUNCTION_NAMES[type];
		}
		
		// 新しいインスタンスを生成.
		public final Scriptable getInstance(Object... args) {
			FunctionList ret = new FunctionList(type);
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
			case 3: //"getCharset"
				return object.getCharset();
			case 4: //"getContentLength"
				return object.getContentLength();
			case 5: //"getContentType"
				return object.getContentType();
			case 6: //"getCookie"
				JsValidate.noArgsKeyToTypeError("string", args);
				return object.getCookie((String)args[0]);
			case 7: //"getCookieKeys"
				return object.getCookieKeys();
			case 8: //"getCookieSize"
				return object.getCookieSize();
			case 9: //"getHeader"
				JsValidate.noArgsKeyToTypeError("string", args);
				return object.getHeader((String)args[0]);
			case 10: //"getHeaderKeys"
				return object.getHeaderKeys();
			case 11: //"getHeaderSize"
				return object.getHeaderSize();
			case 12: //"getMimeType"
				return object.getMimeType();
			case 13: //"isContentType"
				return object.isContentType();
			case 14: //"isGZip"
				return object.isGzip();
			case 15: //"removeCookie"
				JsValidate.noArgsKeyToTypeError("string", args);
				return object.removeHeader((String)args[0]);
			case 16: //"removeHeader"
				JsValidate.noArgsKeyToTypeError("string", args);
				return object.removeHeader((String)args[0]);
			case 17: //"setContentLength"
				object.setContentLength(args[0]);
				return null;
			case 18: //"setContentType"
				JsValidate.noArgsToError(args);
				if(args.length == 1) {
					JsValidate.noArgsStringToError(0, args);
					object.setContentType((String)args[0]);
				} else {
					JsValidate.noArgsStringToError(0, args);
					JsValidate.noArgsStringToError(1, args);
					object.setContentType((String)args[0], (String)args[1]);
				}
				return null;
			case 19: //"setCookie"
				setCookieToArgs(object, args);
				return null;
			case 20: //"setHeader"
				JsValidate.noArgsKeyToTypeError("string", args);
				JsValidate.noArgsValueToTypeError("string", args);
				object.setHeader((String)args[0], (String)args[1]);
				return null;
			case 21: //"toString"
				return object.toString();
			}
			// プログラムの不具合以外にここに来ることは無い.
			throw new RhilaException(
				"An unspecified error (type: " + type + ") occurred");
		}
	}
}
