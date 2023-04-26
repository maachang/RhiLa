package rhila.lib;

import rhila.RhilaException;

/**
 * BooleanUtil.
 */
public class BooleanUtil {
	private BooleanUtil() {}

	/**
	 * 対象のオブジェクトがBooleanとして解釈できるかチェック.
	 *
	 * @param n 変換対象の条件を設定します.
	 * @return boolean [true]の場合、Booleanで解釈が可能です.
	 */
	public static final Boolean isBoolean(Object o) {
		if (o == null) {
			return false;
		} else if (o instanceof Boolean) {
			return true;
		} else if (o instanceof Number) {
			return true;
		} else if (o instanceof String) {
			String s = ((String)o).toLowerCase();
			if (NumberUtil.isNumeric(s) || "true".equals(s) || "t".equals(s)
				|| "on".equals(s) || "false".equals(s) || "f".equals(s)
				|| "off".equals(s)) {
				return true;
			}
		}
		return false;
	}

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

		if (NumberUtil.isNumeric(s)) {
			return "0".equals(s) ? false : true;
		} else if ("true".equals(s) || "t".equals(s) || "on".equals(s)) {
			return true;
		} else if ("false".equals(s) || "f".equals(s) || "off".equals(s)) {
			return false;
		}
		throw new RhilaException("Boolean conversion failed: " + s);
	}
}
