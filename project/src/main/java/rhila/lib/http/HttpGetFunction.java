package rhila.lib.http;

import org.mozilla.javascript.Scriptable;

import rhila.lib.ArrayMap;
import rhila.lib.http.HttpCookieValue.HttpCookieValueScriptable;
import rhila.lib.http.HttpHeader.HttpHeaderScriptable;
import rhila.lib.http.HttpStatus.HttpStatusScriptable;
import rhila.lib.http.MimeType.MimeTypeScriptable;
import rhila.scriptable.RhinoGetFunction;

/**
 * http用function.
 */
public class HttpGetFunction implements RhinoGetFunction {
	protected HttpGetFunction() {}
    // lambda snapStart CRaC用.
	protected static final HttpGetFunction LOAD_CRAC = new HttpGetFunction();
	// functionインスタンス管理用.
	private static final ArrayMap<String, Scriptable> instanceList;
	
	// 初期設定.
	static {
		instanceList = new ArrayMap<String, Scriptable>(
			"HttpCookieValue", HttpCookieValueScriptable.LOAD_CRAC
			,"HttpHeader", HttpHeaderScriptable.LOAD_CRAC
			,"HttpStatus", HttpStatusScriptable.LOAD_CRAC
			,"MimeType", MimeTypeScriptable.LOAD_CRAC
		);
	}
	
	// オブジェクトを取得.
	public static final HttpGetFunction getInstance() {
		return LOAD_CRAC;
	}
	
    // Functionを取得.
	public final Scriptable getFunction(String name) {
		return instanceList.get(name);
	}
}
