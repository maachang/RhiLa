package rhila.lib;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;

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
		return encodeURIComponent(s, null);
	}
	
	// urlエンコード.
	public static final String encodeURIComponent(String s, String c) {
		c = c == null ? "UTF8": c;
		try {
			return URLEncoder.encode(s, c);
		} catch(Exception e) {
			throw new RhilaException(e);
		}
	}
	
	// urlデコード.
	public static final String decodeURIComponent(String s) {
		return decodeURIComponent(s, null);
	}
	
	// urlデコード.
	public static final String decodeURIComponent(String s, String c) {
		c = c == null ? "UTF8": c;
		try {
			return URLDecoder.decode(s, c);
		} catch(Exception e) {
			throw new RhilaException(e);
		}
	}
	
	/**
	 * チェック情報単位で情報を区切ります.
	 *
	 * @param out   区切られた情報が格納されます.
	 * @param mode  区切られた時の文字列が無い場合に、無視するかチェックします.
	 *              [true]の場合は、無視しません.
	 *              [false]の場合は、無視します.
	 * @param str   区切り対象の情報を設置します.
	 * @param check 区切り対象の文字情報をセットします.
	 *              区切り対象文字を複数設定する事により、それらに対応した区切りとなります.
	 */
	public static final void cutString(
		List<String> out, boolean mode, String str, String check) {
		int i, j;
		int len;
		int lenJ;
		int s = -1;
		char strCode;
		char[] checkCode = null;
		String tmp = null;
		if (out == null || str == null || (len = str.length()) <= 0
			|| check == null || check.length() <= 0) {
			throw new IllegalArgumentException();
		}
		lenJ = check.length();
		checkCode = new char[lenJ];
		check.getChars(0, lenJ, checkCode, 0);
		if (lenJ == 1) {
			for (i = 0, s = -1; i < len; i++) {
				strCode = str.charAt(i);
				s = (s == -1) ? i : s;
				if (strCode == checkCode[0]) {
					if (s < i) {
						tmp = str.substring(s, i);
						out.add(tmp);
						tmp = null;
						s = -1;
					} else if (mode == true) {
						out.add("");
						s = -1;
					} else {
						s = -1;
					}
				}
			}
		} else {
			for (i = 0, s = -1; i < len; i++) {
				strCode = str.charAt(i);
				s = (s == -1) ? i : s;
				for (j = 0; j < lenJ; j++) {
					if (strCode == checkCode[j]) {
						if (s < i) {
							tmp = str.substring(s, i);
							out.add(tmp);
							tmp = null;
							s = -1;
						} else if (mode == true) {
							out.add("");
							s = -1;
						} else {
							s = -1;
						}
						break;
					}
				}
			}
		}
		if (s != -1) {
			tmp = str.substring(s, len);
			out.add(tmp);
			tmp = null;
		}
		checkCode = null;
		tmp = null;
	}

	/**
	 * チェック情報単位で情報を区切ります。
	 *
	 * @param out      区切られた情報が格納されます.
	 * @param quote    クォーテーション対応であるか設定します.
	 *                 [true]を設定した場合、各クォーテーション ( ",' ) で囲った情報内は
	 *                 区切り文字と判別しません. [false]を設定した場合、クォーテーション対応を行いません.
	 * @param quoteFlg クォーテーションが入っている場合に、クォーテーションを範囲に含むか否かを 設定します.
	 *                 [true]を設定した場合、クォーテーション情報も範囲に含みます.
	 *                 [false]を設定した場合、クォーテーション情報を範囲としません.
	 * @param str      区切り対象の情報を設置します.
	 * @param check    区切り対象の文字情報をセットします.
	 *                 区切り対象文字を複数設定する事により、それらに対応した区切りとなります.
	 */
	public static final void cutString(
		List<String> out, boolean quote, boolean quoteFlg,
		String str, String check) {
		int i, j;
		int len;
		int lenJ;
		int s = -1;
		char quoteChr;
		char nowChr;
		char strCode;
		char[] checkCode = null;
		String tmp = null;
		if (!quote) {
			cutString(out, false, str, check);
		} else {
			if (out == null || str == null || (len = str.length()) <= 0
				|| check == null || check.length() <= 0) {
				throw new IllegalArgumentException();
			}
			lenJ = check.length();
			checkCode = new char[lenJ];
			check.getChars(0, lenJ, checkCode, 0);
			if (lenJ == 1) {
				int befCode = -1;
				boolean yenFlag = false;
				for (i = 0, s = -1, quoteChr = 0; i < len; i++) {
					strCode = str.charAt(i);
					nowChr = strCode;
					s = (s == -1) ? i : s;
					if (quoteChr == 0) {
						if (nowChr == '\'' || nowChr == '\"') {
							quoteChr = nowChr;
							if (s < i) {
								tmp = str.substring(s, i);
								out.add(tmp);
								tmp = null;
								s = -1;
							} else {
								s = -1;
							}
						} else if (strCode == checkCode[0]) {
							if (s < i) {
								tmp = str.substring(s, i);
								out.add(tmp);
								tmp = null;
								s = -1;
							} else {
								s = -1;
							}
						}
					} else {
						if (befCode != '\\' && quoteChr == nowChr) {
							yenFlag = false;
							quoteChr = 0;
							if (s == i && quoteFlg == true) {
								out.add(new StringBuilder()
									.append(strCode).append(strCode).toString());
								s = -1;
							} else if (s < i) {
								if (quoteFlg == true) {
									tmp = str.substring(s - 1, i + 1);
								} else {
									tmp = str.substring(s, i);
								}
								out.add(tmp);
								tmp = null;
								s = -1;
							} else {
								s = -1;
							}
						} else if (strCode == '\\' && befCode == '\\') {
							yenFlag = true;
						} else {
							yenFlag = false;
						}
					}
					if (yenFlag) {
						yenFlag = false;
						befCode = -1;
					} else {
						befCode = strCode;
					}
				}
			} else {
				int befCode = -1;
				boolean yenFlag = false;
				for (i = 0, s = -1, quoteChr = 0; i < len; i++) {
					strCode = str.charAt(i);
					nowChr = strCode;
					s = (s == -1) ? i : s;
					if (quoteChr == 0) {
						if (nowChr == '\'' || nowChr == '\"') {
							quoteChr = nowChr;
							if (s < i) {
								tmp = str.substring(s, i);
								out.add(tmp);
								tmp = null;
								s = -1;
							} else {
								s = -1;
							}
						} else {
							for (j = 0; j < lenJ; j++) {
								if (strCode == checkCode[j]) {
									if (s < i) {
										tmp = str.substring(s, i);
										out.add(tmp);
										tmp = null;
										s = -1;
									} else {
										s = -1;
									}
									break;
								}
							}
						}
					} else {
						if (befCode != '\\' && quoteChr == nowChr) {
							quoteChr = 0;
							yenFlag = false;
							if (s == i && quoteFlg == true) {
								out.add(new StringBuilder().append(strCode)
									.append(strCode).toString());
								s = -1;
							} else if (s < i) {
								if (quoteFlg == true) {
									tmp = str.substring(s - 1, i + 1);
								} else {
									tmp = str.substring(s, i);
								}

								out.add(tmp);
								tmp = null;
								s = -1;
							} else {
								s = -1;
							}
						} else if (strCode == '\\' && befCode == '\\') {
							yenFlag = true;
						} else {
							yenFlag = false;
						}
					}
					if (yenFlag) {
						yenFlag = false;
						befCode = -1;
					} else {
						befCode = strCode;
					}
				}
			}
			if (s != -1) {
				if (quoteChr != 0 && quoteFlg == true) {
					tmp = str.substring(s - 1, len) + (char) quoteChr;
				} else {
					tmp = str.substring(s, len);
				}
				out.add(tmp);
				tmp = null;
			}
			checkCode = null;
			tmp = null;
		}
	}
}
