package rhila.lib;

import java.math.BigDecimal;

import org.mozilla.javascript.Undefined;

import rhila.RhilaException;

/**
 * 数値ユーティリティ.
 */
public final class NumberUtil {
	private NumberUtil() {}
	
	// hexかチェック.
	private static final boolean isHex(String s) {
		return (s.length() > 2 && s.charAt(0) == '0' && (s.charAt(1) == 'x'
			|| s.charAt(1) == 'X'));
	}

	/**
	 * 文字列内容が数値かチェック.
	 *
	 * @param num 対象のオブジェクトを設定します.
	 * @return boolean [true]の場合、文字列内は数値が格納されています.
	 */
	public static final boolean isNumeric(Object num) {
		if (num == null || num instanceof Undefined) {
			return false;
		} else if (num instanceof Number) {
			return true;
		} else if (!(num instanceof String)) {
			num = num.toString().trim();
		}
		String s = (String) num;
		if(isHex(s)) {
			// 16進数文字列の場合.
			try {
				Long.parseLong(s.substring(2), 16);
			} catch(Exception e) {
				return false;
			}
			return true;
		}
		try {
			Double.parseDouble(s);
		} catch(Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * 対象文字列内容が小数点かチェック.
	 *
	 * @param n 対象のオブジェクトを設定します.
	 * @return boolean [true]の場合は、数値内容です.
	 */
	public static final boolean isFloat(Object n) {
		if (NumberUtil.isNumeric(n)) {
			String s;
			if (n instanceof Float || n instanceof Double || n instanceof BigDecimal) {
				return true;
			} else if (n instanceof String) {
				s = (String)n;
			} else {
				s = n.toString().trim();
			}
			if(isHex(s)) {
				return true;
			}
			return s.indexOf(".") != -1;
		}
		return false;
	}

	/**
	 * Byte変換.
	 *
	 * @param n 変換対象の条件を設定します.
	 * @return 変換された内容が返却されます.
	 */
	public static final Byte parseByte(Object o) {
		if (o == null) {
			return null;
		} else if (o instanceof Number) {
			return ((Number) o).byteValue();
		} else if (o instanceof String) {
			String s = ((String)o).trim();
			if(isHex(s)) {
				return Byte.parseByte(s.substring(2), 16);
			}
			return Byte.parseByte(s);
		} else if (o instanceof Boolean) {
			return (byte)(((Boolean) o).booleanValue() ? 1 : 0);
		}
		throw new RhilaException("Byte conversion failed: " + o);
	}

	/**
	 * Short変換.
	 *
	 * @param n 変換対象の条件を設定します.
	 * @return 変換された内容が返却されます.
	 */
	public static final Short parseShort(Object o) {
		if (o == null) {
			return null;
		} else if (o instanceof Number) {
			return ((Number) o).shortValue();
		} else if (o instanceof String) {
			String s = ((String)o).trim();
			if(isHex(s)) {
				return Short.parseShort(s.substring(2), 16);
			}
			return Short.parseShort(s);
		} else if (o instanceof Boolean) {
			return (short)(((Boolean) o).booleanValue() ? 1 : 0);
		}
		throw new RhilaException("Short conversion failed: " + o);
	}

	/**
	 * Integer変換.
	 *
	 * @param n 変換対象の条件を設定します.
	 * @return 変換された内容が返却されます.
	 */
	public static final Integer parseInt(Object o) {
		if (o == null) {
			return null;
		} else if (o instanceof Integer) {
			return (Integer) o;
		} else if (o instanceof Number) {
			return ((Number) o).intValue();
		} else if (o instanceof String) {
			String s = ((String)o).trim();
			if(isHex(s)) {
				return Integer.parseInt(s.substring(2), 16);
			}
			return Integer.parseInt(s);
		} else if (o instanceof Boolean) {
			return ((Boolean) o).booleanValue() ? 1 : 0;
		}
		throw new RhilaException("Int conversion failed: " + o);
	}

	/**
	 * Long変換.
	 *
	 * @param n 変換対象の条件を設定します.
	 * @return 変換された内容が返却されます.
	 */
	public static final Long parseLong(Object o) {
		if (o == null) {
			return null;
		} else if (o instanceof Long) {
			return (Long) o;
		} else if (o instanceof Number) {
			return ((Number) o).longValue();
		} else if (o instanceof String) {
			String s = ((String)o).trim();
			if(isHex(s)) {
				return Long.parseLong(s.substring(2), 16);
			}
			return Long.parseLong(s);
		} else if (o instanceof Boolean) {
			return ((Boolean) o).booleanValue() ? 1L : 0L;
		}
		throw new RhilaException("Long conversion failed: " + o);
	}

	/**
	 * Float変換.
	 *
	 * @param n 変換対象の条件を設定します.
	 * @return 変換された内容が返却されます.
	 */
	public static final Float parseFloat(final Object o) {
		if (o == null) {
			return null;
		} else if (o instanceof Float) {
			return (Float) o;
		} else if (o instanceof Number) {
			return ((Number) o).floatValue();
		} else if (o instanceof String && isNumeric(o)) {
			String s = ((String)o).trim();
			if(isHex(s)) {
				return (float)Integer.parseInt(s.substring(2), 16);
			}
			return Float.parseFloat(s);
		} else if (o instanceof Boolean) {
			return ((Boolean) o).booleanValue() ? 1F : 0F;
		}
		throw new RhilaException("Float conversion failed: " + o);
	}

	/**
	 * Double変換.
	 *
	 * @param n 変換対象の条件を設定します.
	 * @return 変換された内容が返却されます.
	 */
	public static final Double parseDouble(Object o) {
		if (o == null) {
			return null;
		} else if (o instanceof Double) {
			return (Double) o;
		} else if (o instanceof Number) {
			return ((Number) o).doubleValue();
		} else if (o instanceof String) {
			String s = ((String)o).trim();
			if(isHex(s)) {
				return (double)Long.parseLong(s.substring(2), 16);
			}
			return Double.parseDouble(s);
		} else if (o instanceof Boolean) {
			return ((Boolean) o).booleanValue() ? 1D : 0D;
		}
		throw new RhilaException("Double conversion failed: " + o);
	}
	
	/**
	 * 容量を指定する文字列であるかチェック.
	 * @param num 対象のサイズの文字列を設定します.
	 * @return boolean parseCapacityByLong で変換が可能です.
	 */
	public static final boolean isCapacityString(String num) {
		// 最後の情報が数字の単位のアルファベットの場合.
		boolean dotFlag = false;
		int lastPos = num.length() - 1;
		char c = num.charAt(lastPos);
		if('k' == c || 'K' == c || 'm' == c || 'M' == c
			|| 'g' == c || 'G' == c || 't' == c || 'T' == c
			|| 'p' == c || 'P' == c) {
			// 最後の情報以外が数字の場合.
			final int len = num.length() - 1;
			for(int i = num.charAt(0) == '-' ? 1 : 0;
				i < len; i ++) {
				c = num.charAt(i);
				if(c == '.') {
					if(dotFlag || i == 0 || i + 1 >= len) {
						return false;
					}
					dotFlag = true;
				} else if(!(c >= '0' && c <= '9')) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	
	// 小数点表記の場合は小数点で計算して返却.
	private static final long convertLongOrDouble(
		String num, long value, int len) {
		char c;
		boolean dotFlag = false;
		boolean minusFlag = num.charAt(0) == '-';
		for(int i = minusFlag ? 1 : 0; i < len; i ++) {
			c = num.charAt(i);
			if(c == '.') {
				if(dotFlag || i == 0 || i + 1 >= len) {
					throw new RhilaException("Long conversion failed: " + num);
				}
				dotFlag = true;
			} else if(!(c >= '0' && c <= '9')) {
				throw new RhilaException("Long conversion failed: " + num);
			}
		}
		if(dotFlag) {
			if(minusFlag) {
				return ((long)(parseDouble(
					num.substring(1, len)) * (double)value)) * -1L;
			}
			return (long)(parseDouble(
				num.substring(0, len)) * (double)value);
		} else if(minusFlag) {
			return (parseLong(num.substring(1, len)) * value) * -1L;
		}
		return parseLong(num.substring(0, len)) * value;
	}
	
	/**
	 * 容量を指定する文字列からlong値に変換.
	 * 以下のように キロ,メガ,ギガ,テラ のような単位を
	 * long値に変換します.
	 * 
	 * "1024" = 1,024.
	 * "1k" = 1,024.
	 * "1m" = 1,048,576.
	 * "1g" = 1,073,741,824.
	 * "1t" = 1,099,511,627,776.
	 * "1p" = 1,125,899,906,842,624.
	 * 
	 * @param num 対象のサイズの文字列を設定します.
	 * @return long 変換されたLong値が返却されます.
	 */
	public static final long parseCapacityByLong(String num) {
		int lastPos = num.length() - 1;
		char c = num.charAt(lastPos);
		if(c == 'k' || c == 'K') {
			return convertLongOrDouble(num, 1024L, lastPos);
		} else if(c == 'm' || c == 'M') {
			return convertLongOrDouble(num, 1048576L, lastPos);
		} else if(c == 'g' || c == 'G') {
			return convertLongOrDouble(num, 1073741824L, lastPos);
		} else if(c == 't' || c == 'T') {
			return convertLongOrDouble(num, 1099511627776L, lastPos);
		} else if(c == 'p' || c == 'P') {
			return convertLongOrDouble(num, 1125899906842624L, lastPos);
		}
		return convertLongOrDouble(num, 1L, lastPos + 1);
	}
}
