package rhila.lib.http.client;

import org.mozilla.javascript.Scriptable;

import rhila.lib.ArrayMap;
import rhila.scriptable.RhinoGetFunction;

/**
 * httpClient用function.
 */
public class HttpClientGetFunction implements RhinoGetFunction {
	protected HttpClientGetFunction() {}
    // lambda snapStart CRaC用.
	public static final HttpClientGetFunction LOAD_CRAC = new HttpClientGetFunction();
	// functionインスタンス管理用.
	private static final ArrayMap<String, Scriptable> instanceList;
	
	// 初期設定.
	static {
		instanceList = new ArrayMap<String, Scriptable>(
			"HttpClient", HttpClient.LOAD_CRAC
		);
		
		// lambda snapStart CRaC用.
		HttpClient.LOAD_CRAC.getClass();
	}
	
	// オブジェクトを取得.
	public static final HttpClientGetFunction getInstance() {
		return LOAD_CRAC;
	}
	
    // Functionを取得.
	public final Scriptable getFunction(String name) {
		return instanceList.get(name);
	}
}
