package rhila.lib.http;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import rhila.RhilaConstants;
import rhila.RhilaException;
import rhila.lib.ArrayMap;
import rhila.lib.JsValidate;
import rhila.lib.NumberUtil;
import rhila.lib.ObjectUtil;
import rhila.scriptable.AbstractRhinoFunctionInstance;
import rhila.scriptable.MapScriptable;

/**
 * HttpRequest.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class HttpRequest extends AbstractReqRes<HttpResponse> {
    // lambda snapStart CRaC用.
    protected static final HttpRequest LOAD_CRAC = new HttpRequest(true);
    
	// instance可能なScriptable.
	private static final ArrayMap<String, Scriptable> instanceList;
	
	// メソッド名群(sort済み).
	private static final String[] FUNCTION_NAMES = new String[] {
		"clearBody"
		,"getBody"
		,"getBodyToJSON"
		,"getBodyToString"
		,"getHeader"
		,"getHost"
		,"getHttpVersion"
		,"getMethod"
		,"getPath"
		,"getPort"
		,"getProtocol"
		,"getQuery"
		,"getQueryString"
		,"getURL"
		,"setBody"
		,"setBodyToBase64"
		,"setBodyToJSON"
		,"setHttpVersion"
		,"setMethod"
		,"setQueryString"
		,"setURL"
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
	
	// [URL]host名.
	private String host = null;
	
	// [URL]path名.
	private String path = null;
	
	// [URL]protocol.
	private String protocol = "https";
	
	// [URL]port番号.
	private Integer port = null;
	
	// queryパラメータ.
	private MapScriptable query = null;
	
	// method.
	private String method = "GET";
	
	// コンストラクタ.
	public HttpRequest() {
		staticFlag = false;
	}
	
	// コンストラクタ.
	protected HttpRequest(boolean staticFlag) {
		this.staticFlag = staticFlag;
	}
	
	// URLを取得.
	public String getURL() {
		if(protocol == null || host == null || path == null) {
			return null;
		}
		if(port != null) {
			return protocol + "://" + host + ":" + port + path;
		}
		return protocol + "://" + host + path;
	}
	
	// url + queryStringを取得.
	public String getFullURL() {
		String url = getURL();
		if(url == null) {
			return null;
		}
		String queryString = getQueryString();
		if(queryString != null) {
			return url + (queryString.indexOf("?") == -1 ? "?" : "&")
				+ queryString;
		}
		return url;
	}
	
	// [URL]protocol名を取得.
	public String getProtocol() {
		return protocol;
	}
	
	// [URL]port番号を取得.
	public Integer getPort() {
		if(port == null) {
			if("http".equals(protocol)) {
				return 80;
			} else if("https".equals(protocol)) {
				return 443;
			}
		}
		return port;
	}
	
	// [URL]host名を取得.
	public String getHost() {
		return host;
	}
	
	// [URL]path名を取得.
	public String getPath() {
		return path;
	}
	
	// [URL]queryStringを取得.
	public String getQueryString() {
		if(query == null) {
			return null;
		}
		StringBuilder buf = new StringBuilder();
		Entry<String, Object> e;
		Iterator<Entry<String, Object>> it = query.entrySet().iterator();
		while(it.hasNext()) {
			if(buf.length() > 0) {
				buf.append("&");
			}
			e = it.next();
			buf.append(ObjectUtil.encodeURIComponent(e.getKey()))
				.append("=")
				.append(ObjectUtil.encodeURIComponent(
					(String)e.getValue()));
		}
		return buf.toString();
	}
	
	// queryParamを取得.
	public MapScriptable getQuery() {
		return query;
	}
	
	// methodを取得.
	public String getMethod() {
		return method;
	}
	
	// URLを設定.
	public HttpRequest setURL(String url) {
		setUrlToHostPathQueryString(url);
		return this;
	}
	
	// URLを設定.
	public HttpRequest setURL(String host, String path) {
		return setURL(host, path, null);
	}
	
	// URLを設定.
	public HttpRequest setURL(String host, String path, String query) {
		if(query != null) {
			if(path.indexOf("?") == -1) {
				path += "?" + query;
			} else {
				path += "&" + query;
			}
		}
		setUrlToHostPathQueryString(HttpUtil.createURL(host, path));
		return this;
	}
	
	// url指定からhost, path, queryStringをそれぞれ設定.
	private final void setUrlToHostPathQueryString(String url) {
		if(ObjectUtil.isNull(url) || url.isEmpty()) {
			this.protocol = null;
			this.host = null;
			this.path = null;
			this.port = null;
			return;
		}
		String queryString = null;
		// queryStringが存在する場合分離.
		int p = url.indexOf("?");
		if(p != -1) {
			queryString = url.substring(p + 1);
			url = url.substring(0, p);
		}
		// protocol, host, port, path に分離.
		String[] urls = HttpUtil.parseUrl(url);
		String protocol = urls[0];
		String host = urls[1];
		// protocolに対して独自ポート指定の場合.
		Integer port = null;
		if(("http".equals(protocol) && !"80".equals(urls[2]))
			|| ("https".equals(protocol) && !"443".equals(urls[2]))) {
			port = NumberUtil.parseInt(urls[2]);
		} else {
		}
		this.protocol = protocol;
		this.host = host;
		this.path = urls[3].startsWith("/") ? urls[3] : "/" + urls[3];
		this.port = port;
		if(queryString != null) {
			setQueryString(queryString);
		}
		// headerにHost名をセット.
		getHeader().setHeader("host", host);
	}
	
	// methodをセット.
	public HttpRequest setMethod(String method) {
		this.method = HttpUtil.trimMethod(method);
		return this;
	}
	
	// queryStringをセット.
	public HttpRequest setQueryString(String queryString) {
		if(queryString == null || queryString.isEmpty()) {
			this.query = null;
		} else {
			this.query = HttpUtil.decodeHttpUrlParams(queryString, null);
		}
		return this;
	}
	
	// queryStringをセット.
	public HttpRequest setQueryStringToMap(Map map) {
		if(map == null || map.isEmpty()) {
			this.query = null;
		} else {
			this.query = new MapScriptable(map);
		}
		return this;
	}

			
	@Override
	public String getName() {
		return "HttpRequest";
	}
	
	@Override
	public String toString() {
		if(staticFlag) {
			return "[HttpRequest]";
		}
		StringBuilder buf = new StringBuilder();
		getHeaderToString(buf);
		return buf.toString();
	}
	
	// HTTPヘッダの文字変換.
	public void getHeaderToString(StringBuilder out) {
		// GET /sample_page.html HTTP/2.0
		out.append(method).append(" ");
		if(path == null) {
			out.append("/");
		} else {
			out.append(path);
		}
		String queryString = getQueryString();
		if(queryString != null) {
			out.append("?").append(queryString);
			queryString = null;
		}
		out.append(" ").append(HTTP_VERSION).append(httpVersion)
			.append("\r\n");
		// [header]User-Agentがセットされていない場合.
		this.setDefaultHeader("user-agent", RhilaConstants.USER_AGENT);
		// [header]connectionがセットされていない場合.
		this.setDefaultHeader("connection", "keep-alive");
		// [header]accept-encodingがセットされていない場合.
		this.setDefaultHeader("accept-encoding", "gzip,deflate");
		
		// ヘッダを出力.
		header.toString(false, out);
	}
	
	@Override
	public Object get(String arg0, Scriptable arg1) {
		// staticな場合.
		if(staticFlag) {
			return null;
		}
		// staticでない場合.
		Object ret = getFunction(arg0);
		if(ret != null) {
			return ret;
		}
		return null;
	}
	
	// function取得.
	private final Object getFunction(String name) {
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
	
	// new HttpRequest.
	public Scriptable newInstance(Context arg0, Scriptable arg1, Object[] arg2) {
		return new HttpRequest();
	}
	
	// functionリストを生成.
	private static final class FunctionList extends AbstractRhinoFunctionInstance {
	    // lambda snapStart CRaC用.
	    @SuppressWarnings("unused")
		protected static final FunctionList LOAD_CRAC = new FunctionList();
	    
		private HttpRequest src;
		private int type;
		private String typeString;
		
		// 新しいインスタンスを生成.
		public final Scriptable getInstance(Object... args) {
			FunctionList ret = new FunctionList(type);
			ret.src = (HttpRequest)args[0];
			return ret;
		}
		
		protected FunctionList() {}
		
		// コンストラクタ.
		protected FunctionList(int type) {
			this.type = type;
			this.typeString = FUNCTION_NAMES[type];
		}
		
		@Override
		public String getName() {
			return typeString;
		}
		
		// メソッド実行.
		public Object function(
			Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			switch(type) {
			case 0: //"clearBody"
				return src.clearBody();
			case 1: //"getBody"
				return src.getBody();
			case 2: //"getBodyToJSON"
				return src.getBodyToJSON();
			case 3: //"getBodyToString"
				return src.getBodyToString();
			case 4: //"getHeader"
				return src.header;
			case 5: //"getHost"
				return src.getHost();
			case 6: //"getHttpVersion"
				return src.getHttpVersion();
			case 7: //"getMethod"
				return src.getMethod();
			case 8: //"getPath"
				return src.getPath();
			case 9: //"getPort"
				return src.getPort();
			case 10: //"getProtocol"
				return src.getProtocol();
			case 11: //"getQuery"
				return src.getQuery();
			case 12: //"getQueryString"
				return src.getQueryString();
			case 13: //"getURL"
				return src.getURL();
			case 14: //"setBody"
				setBodyByArgs(TYPE_PLAIN, src, args);
				return src;
			case 15: //"setBodyToBase64"
				setBodyByArgs(TYPE_BASE64, src, args);
				return src;
			case 16: //"setBodyToJSON"
				setBodyByArgs(TYPE_JSON, src, args);
				return src;
			case 17: //"setHttpVersion"
				JsValidate.noArgsStringToError(0, args);
				src.setHttpVersion((String)args[0]);
				return src;
			case 18: //"setMethod"
				JsValidate.noArgsStringToError(0, args);
				src.setMethod((String)args[0]);
				return src;
			case 19: //"setQueryString"
				JsValidate.noArgsStringToError(0, args);
				src.setQueryString((String)args[0]);
				return src;
			case 20: //"setURL"
				JsValidate.noArgsToError(args);
				if(args.length == 1) {
					JsValidate.noArgsStringToError(0, args);
					src.setURL((String)args[0]);
				} else {
					JsValidate.noArgsStringToError(0, args);
					JsValidate.noArgsStringToError(1, args);
					src.setURL((String)args[0], (String)args[1]);
				}
				return src;
			case 21: //"toString"
				return src.toString();
			}
			// プログラムの不具合以外にここに来ることは無い.
			throw new RhilaException(
				"An unspecified error (type: " + type + ") occurred");
		}
	}
}
