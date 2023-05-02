package rhila.lib.http;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import rhila.lib.ArrayMap;
import rhila.scriptable.AbstractRhinoFunction;
import rhila.scriptable.MapScriptable;

/**
 * MimeType管理.
 */
public class MimeType {
    // lambda snapStart CRaC用.
    protected static final MimeType LOAD_CRAC = new MimeType();
    
	// instance可能なScriptable.
	private static final ArrayMap<String, Scriptable> instanceList;
	
	// 基本MimeType.
	private static final ArrayMap<String, String> DEF_MIMETYPE;
	
	// [mimeType]octet-stream.
	public static final String OCTET_STREAM = "application/octet-stream";

	// [mimeType]form-data.
	public static final String FORM_DATA = "application/x-www-form-urlencoded";

	// [mimeType]json.
	public static final String JSON = "application/json";

	// [mimeType]gz.
	public static final String GZ = "application/gzip";
	
	static {
		DEF_MIMETYPE = new ArrayMap<String, String>(
			/** プレーンテキスト. **/
			"txt", "text/plain"
			/** HTML. **/
			,"htm", "text/html"
			,"html", "text/html"
			/** XHTML. **/
			,"xhtml", "application/xhtml+xml"
			/** XML. **/
			,"xml", "text/xml"
			/** JSON. */
			,"json", "application/json"
			/** RSS. */
			,"rss", "application/rss+xm"
			/** stylesheet. */
			,"css", "text/css"
			/** javascript. */
			,"js", "text/javascript"
			,"mjs", "text/javascript"
			/** csv. */
			,"csv", "text/csv"
			/** gif. */
			,"gif", "image/gif"
			/** jpeg. */
			,"jpg", "image/jpeg"
			,"jpeg", "image/jpeg"
			/** png. */
			,"png", "image/png"
			/** ico. */
			,"ico", "image/vnd.microsoft.icon"
		);
		
		instanceList = new ArrayMap<String, Scriptable>(
			"FORM_DATA", FORM_DATA
			,"GZ", GZ
			,"JSON", JSON
			,"OCTET_STREAM", OCTET_STREAM
			,"get", Get.LOAD_CRAC
			,"loadJSON", LoadJSON.LOAD_CRAC
		);
	}
		
	// オブジェクトを取得.
	public static final MimeType getInstance() {
		return LOAD_CRAC;
	}
	
	// 拡張MimeType.
	private Map<String, String> originMime = null;
	
	// コンストラクタ.
	private MimeType() {}
	
	// 新しいMimeTypeをロード.
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void loadJSON(Map json) {
		// jsonは以下の形をローディングする.
		// {
		//   extension: mimeType
		// }
		// extension は拡張子
		// mimeType は拡張子に対するMimeType.
		Entry e;
		Map<String, String> o = new HashMap<String, String>();
		Iterator<Entry> it = json.entrySet().iterator();
		while(it.hasNext()) {
			e = it.next();
			o.put(String.valueOf(e.getKey()),
				String.valueOf(e.getValue()));
		}
		originMime = o;
	}
	
	// 指定拡張子からMimeTypeを取得.
	public String get(String extension) {
		String ret = null;
		extension = extension.trim();
		if(originMime != null) {
			ret = originMime.get(extension);
			if(ret != null) {
				return ret;
			}
		}
		return DEF_MIMETYPE.get(extension);
	}
	
	// 新しいMimeTypeのロード.
	private static final class LoadJSON extends AbstractRhinoFunction {
	    // lambda snapStart CRaC用.
	    protected static final LoadJSON LOAD_CRAC = new LoadJSON();
		
		@Override
		public String getName() {
			return "loadJSON";
		}

		@Override
		public Object function(
			Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if(args != null && args.length >= 1 && args[0] instanceof MapScriptable) {
				MimeType.LOAD_CRAC.loadJSON((MapScriptable)args[0]);
				return true;
			}
			return false;
		}
	}
	
	// 拡張子を指定してMimeTypeを取得する.
	private static final class Get extends AbstractRhinoFunction {
	    // lambda snapStart CRaC用.
	    protected static final Get LOAD_CRAC = new Get();
	    
		@Override
		public String getName() {
			return "get";
		}

		@Override
		public Object function(
			Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if(args != null && args.length >= 1 && args[0] instanceof String) {
				return MimeType.LOAD_CRAC.get((String)args[0]);
			}
			return null;
		}
	}
	
	// [js]HttpCookieValue.
	public static final class MimeTypeScriptable extends AbstractRhinoFunction {
	    // lambda snapStart CRaC用.
	    protected static final MimeTypeScriptable LOAD_CRAC =
	    	new MimeTypeScriptable();
	    
		protected MimeTypeScriptable() {}
		
		@Override
		public String getClassName() {
			return "MimeTypeScriptable";
		}
		
		@Override
		public String getName() {
			return "[MimeType]";
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
			// static管理のオブジェクトを取得.
			return instanceList.get(name);
		}
	}
}
