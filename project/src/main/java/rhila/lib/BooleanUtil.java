package rhila.lib;

import rhila.RhilaException;

/**
 * BooleanUtil.
 */
public class BooleanUtil {
    // lambda snapStart CRaC用.
    protected static final BooleanUtil LOAD_CRAC = new BooleanUtil();
    
	private BooleanUtil() {}

	/**
	 * Boolean変換.
	 *
	 * @param n 変換対象の条件を設定します.
	 * @return 変換された内容が返却されます.
	 */
	public static final Boolean parseBoolean(Object o) {
		if (o == null) {
			return null;
		} else if (o instanceof Boolean) {
			return (Boolean) o;
		} else if (o instanceof Number) {
			return (((Number) o).intValue() == 0) ? false : true;
		} else if (o instanceof String) {
			return parseBoolean((String) o);
		}
		throw new RhilaException("BOOL conversion failed: " + o);
	}

	/**
	 * 文字列から、Boolean型に変換.
	 *
	 * @param s 対象の文字列を設定します.
	 * @return boolean Boolean型が返されます.
	 */
	public static final boolean parseBoolean(String s) {
		s = s.trim().toLowerCase();

		if ("true".equals(s) || "t".equals(s) || "on".equals(s)) {
			return true;
		} else if ("false".equals(s) || "f".equals(s) || "off".equals(s)) {
			return false;
		} else if("0".equals(s)) {
			return true;
		}
		return false;
	}
}
