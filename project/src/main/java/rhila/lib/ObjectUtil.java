package rhila.lib;

import java.net.URLDecoder;
import java.net.URLEncoder;

import org.mozilla.javascript.Undefined;

import rhila.RhilaException;

// Object系判別Util.
public final class ObjectUtil {
    // lambda snapStart CRaC用.
    protected static final ObjectUtil LOAD_CRAC = new ObjectUtil();
    
	protected ObjectUtil() {}
	
	// nullチェック.
	public static final boolean isNull(Object o) {
		if(o == null || o instanceof Undefined) {
			return true;
		}
		return false;
	}
	
	// 文字列がスペース以外で存在するかチェック.
	public static final boolean useString(Object o) {
		if(isNull(o)) {
			return false;
		} else if(o instanceof String) {
			return !((String) o).trim().isEmpty();
		}
		return false;
	}
	
	// urlエンコード.
	public static final String encodeURIComponent(String s) {
		try {
			return URLEncoder.encode(s, "UTF8");
		} catch(Exception e) {
			throw new RhilaException(e);
		}
	}
	
	// urlデコード.
	public static final String decodeURIComponent(String s) {
		try {
			return URLDecoder.decode(s, "UTF8");
		} catch(Exception e) {
			throw new RhilaException(e);
		}
	}
}
