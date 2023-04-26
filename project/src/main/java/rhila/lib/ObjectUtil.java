package rhila.lib;

import org.mozilla.javascript.Undefined;

// Object系判別Util.
public final class ObjectUtil {
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
}
