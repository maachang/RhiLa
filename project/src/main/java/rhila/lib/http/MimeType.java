package rhila.lib.http;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

import rhila.RhilaException;
import rhila.lib.ArrayMap;
import rhila.lib.BooleanUtil;
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
	
	// 基本MimeTypeに対するバイナリ属性.
	private static final ArrayMap<String, Boolean> DEF_MIME_BIN;
	
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
			/** gz. */
			,"gz", "application/gzip"
			/** zip. */
			,"zip", "application/zip"
		);
		
		DEF_MIME_BIN = new ArrayMap<String, Boolean>(
			"text/plain", false
			,"text/html", false
			,"text/html", false
			,"application/xhtml+xml", false
			,"text/xml", false
			,"application/json", false
			,"application/rss+xm", false
			,"text/css", false
			,"text/javascript", false
			,"text/javascript", false
			,"text/csv", false
			,"image/gif", true
			,"image/jpeg", true
			,"image/jpeg", true
			,"image/png", true
			,"image/vnd.microsoft.icon", true
			,"application/octet-stream", true
			,"application/x-www-form-urlencoded", false
			,"application/gzip", true
			,"application/zip", true
		);
		
		instanceList = new ArrayMap<String, Scriptable>(
			"FORM_DATA", FORM_DATA
			,"GZ", GZ
			,"JSON", JSON
			,"OCTET_STREAM", OCTET_STREAM
			,"get", Get.LOAD_CRAC
			,"isBinary", IsBinary.LOAD_CRAC
			,"loadJSON", LoadJSON.LOAD_CRAC
		);
	}
		
	// オブジェクトを取得.
	public static final MimeType getInstance() {
		return LOAD_CRAC;
	}
	
	// 拡張MimeType.
	private Map<String, String> originMime = null;
	
	// 拡張MimeTypeのバイナリ判別用.
	private Map<String, Boolean> originMimeBin = null;
	
	
	// コンストラクタ.
	private MimeType() {}
	
	// 新しいMimeTypeをロード.
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void loadJSON(Map json) {
		// jsonは以下の形をローディングする.
		// {
		//   extension: mimeType
		//      or 
		//   extension: {mimeType: string, binary: boolean}
		// }
		// extension は拡張子
		// mimeType は拡張子に対するMimeType.
		// binary は対象MimeTypeがバイナリ設定の内容かのフラグ.
		Entry e;
		Object v;
		String m;
		boolean f;
		Map p;
		Map<String, String> o = new HashMap<String, String>();
		Map<String, Boolean> oo = new HashMap<String, Boolean>();
		Iterator<Entry> it = json.entrySet().iterator();
		while(it.hasNext()) {
			e = it.next();
			v = e.getValue();
			// value = {mimeType: string, binary: boolean}の場合.
			if(v instanceof Map) {
				p = (Map)v;
				if(!p.containsKey("mimeType")) {
					throw new RhilaException(
						"Failed to load MimeType: malformed key=" +
							e.getKey() + " value.");
				}
				m = String.valueOf(p.get("mimeType"));
				f = false;
				if(p.containsKey("binary")) {
					f = BooleanUtil.parseBoolean(p.get("binary"));
				}
			// value = mimeTypeの場合.
			} else {
				m = String.valueOf(e.getValue());
				f = false;
			}
			// 拡張MimeType.
			o.put(String.valueOf(e.getKey()), m);
			// 拡張MimeType拡張子.
			oo.put(m, f);
		}
		originMime = o;
		originMimeBin = oo;
	}
	
	// 指定拡張子からMimeTypeを取得.
	// nullの場合はMimeTypeが存在しない.
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
	
	// 対象MimeTypeがバイナリ形式かチェック.
	// nullの場合はMimeTypeが存在しない.
	public boolean isBinary(String mimeType) {
		// mimeTypeのみを抽出.
		int p = mimeType.indexOf(";");
		if(p != -1) {
			mimeType = mimeType.substring(0, p).trim();
		}
		// オリジナルMimeTypeバイナリ判別条件が存在する場合.
		if(originMimeBin != null) {
			if(originMimeBin.containsKey(mimeType)) {
				return originMimeBin.get(mimeType);
			}
		}
		// オリジナルMimeTypeバイナリ判別条件が存在する場合返却.
		if(DEF_MIME_BIN.containsKey(mimeType)) {
			return DEF_MIME_BIN.get(mimeType);
		}
		return false;
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
	
	// 拡張子を指定してMimeTypeを取得する.
	private static final class IsBinary extends AbstractRhinoFunction {
	    // lambda snapStart CRaC用.
	    protected static final IsBinary LOAD_CRAC = new IsBinary();
	    
		@Override
		public String getName() {
			return "isBinary";
		}

		@Override
		public Object function(
			Context ctx, Scriptable scope, Scriptable thisObj, Object[] args) {
			if(args != null && args.length >= 1 && args[0] instanceof String) {
				return MimeType.LOAD_CRAC.isBinary((String)args[0]);
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
