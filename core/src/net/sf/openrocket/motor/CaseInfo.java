package net.sf.openrocket.motor;

import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration of reloadable hardware which OpenRocket supports for substitution.
 *
 */
public enum CaseInfo {
	
	RMS29_100("RMS-29/100"), RMS29_120("RMS-29/120"), RMS29_180("RMS-29/180"), RMS29_240("RMS-29/240"), RMS29_360("RMS-29/360"),
	
	RMS38_120("RMS-38/120"), RMS38_240("RMS-38/240"), RMS38_360("RMS-38/360"), RMS38_480("RMS-38/480"), RMS38_600("RMS-38/600"), RMS38_720("RMS-38/720"),
	
	RMS54_426("RMS-54/426"), RMS54_852("RMS-54/852"), RMS54_1280("RMS-54/1280"), RMS54_1706("RMS-54/1706"), RMS54_2560("RMS-54/2560"), RMS54_2800("RMS-54/2800"),
	
	PRO29_1("Pro29-1G"), PRO29_2("Pro29-2G"), PRO29_3("Pro29-3G"), PRO29_4("Pro29-4G"), PRO29_5("Pro29-5G"), PRO29_6("Pro29-6G"), PRO29_6XL("Pro29-6GXL"),
	
	PRO38_1("Pro38-1G"), PRO38_2("Pro38-2G"), PRO38_3("Pro38-4G"), PRO38_4("Pro38-4G"), PRO38_5("Pro38-5G"), PRO38_6("Pro38-6G"), PRO38_6XL("Pro38-6GXL"),
	
	PRO54_1("Pro54-1G"), PRO54_2("Pro54-2G"), PRO54_3("Pro54-3G"), PRO54_4("Pro54-4G"), PRO54_5("Pro54-5G"), PRO54_6("Pro54-6G"), PRO54_6XL("Pro54-6GXL");
	
	private String label;
	
	private CaseInfo(String label) {
		this.label = label;
	}
	
	public static CaseInfo parse(String label) {
		return labelMapping.get(label);
	}
	
	@Override
	public String toString() {
		return label;
	}
	
	public CaseInfo[] getCompatibleCases() {
		return compatibleCases.get(this);
	}
	
	private static Map<String, CaseInfo> labelMapping;
	private static Map<CaseInfo, CaseInfo[]> compatibleCases;
	
	static {
		labelMapping = new HashMap<>();
		for (CaseInfo ci : CaseInfo.values()) {
			labelMapping.put(ci.label, ci);
		}
		
		compatibleCases = new HashMap<>();
		
		compatibleCases.put(RMS29_100, new CaseInfo[] { RMS29_100, RMS29_120, RMS29_180 });
		compatibleCases.put(RMS29_120, new CaseInfo[] { RMS29_120, RMS29_180, RMS29_240 });
		compatibleCases.put(RMS29_180, new CaseInfo[] { RMS29_180, RMS29_240, RMS29_360 });
		compatibleCases.put(RMS29_240, new CaseInfo[] { RMS29_240, RMS29_360 });
		compatibleCases.put(RMS29_360, new CaseInfo[] { RMS29_360 });
		compatibleCases.put(RMS38_120, new CaseInfo[] { RMS38_120, RMS38_240, RMS38_360 });
		compatibleCases.put(RMS38_240, new CaseInfo[] { RMS38_240, RMS38_360, RMS38_480 });
		compatibleCases.put(RMS38_360, new CaseInfo[] { RMS38_360, RMS38_480, RMS38_600 });
		compatibleCases.put(RMS38_480, new CaseInfo[] { RMS38_480, RMS38_600, RMS38_720 });
		compatibleCases.put(RMS38_600, new CaseInfo[] { RMS38_600, RMS38_720 });
		compatibleCases.put(RMS38_720, new CaseInfo[] { RMS38_720 });
		compatibleCases.put(RMS54_426, new CaseInfo[] { RMS54_426, RMS54_852, RMS54_1280 });
		compatibleCases.put(RMS54_852, new CaseInfo[] { RMS54_852, RMS54_1280, RMS54_1706 });
		compatibleCases.put(RMS54_1280, new CaseInfo[] { RMS54_1280, RMS54_1706, RMS54_2560 });
		compatibleCases.put(RMS54_1706, new CaseInfo[] { RMS54_1706, RMS54_2560, RMS54_2800 });
		compatibleCases.put(RMS54_2560, new CaseInfo[] { RMS54_2560, RMS54_2800 });
		compatibleCases.put(RMS54_2800, new CaseInfo[] { RMS54_2800 });
		compatibleCases.put(PRO29_1, new CaseInfo[] { PRO29_1, PRO29_2, PRO29_3 });
		compatibleCases.put(PRO29_2, new CaseInfo[] { PRO29_2, PRO29_3, PRO29_4 });
		compatibleCases.put(PRO29_3, new CaseInfo[] { PRO29_3, PRO29_4, PRO29_5 });
		compatibleCases.put(PRO29_4, new CaseInfo[] { PRO29_4, PRO29_5, PRO29_6 });
		compatibleCases.put(PRO29_5, new CaseInfo[] { PRO29_5, PRO29_6, PRO29_6XL });
		compatibleCases.put(PRO29_6, new CaseInfo[] { PRO29_6, PRO29_6XL });
		compatibleCases.put(PRO29_6XL, new CaseInfo[] { PRO29_6XL });
		compatibleCases.put(PRO38_1, new CaseInfo[] { PRO38_1, PRO38_2, PRO38_3 });
		compatibleCases.put(PRO38_2, new CaseInfo[] { PRO38_2, PRO38_3, PRO38_4 });
		compatibleCases.put(PRO38_3, new CaseInfo[] { PRO38_3, PRO38_4, PRO38_5 });
		compatibleCases.put(PRO38_4, new CaseInfo[] { PRO38_4, PRO38_5, PRO38_6 });
		compatibleCases.put(PRO38_5, new CaseInfo[] { PRO38_5, PRO38_6, PRO38_6XL });
		compatibleCases.put(PRO38_6, new CaseInfo[] { PRO38_6, PRO38_6XL });
		compatibleCases.put(PRO38_6XL, new CaseInfo[] { PRO38_6XL });
		compatibleCases.put(PRO54_1, new CaseInfo[] { PRO54_1, PRO54_2, PRO54_3 });
		compatibleCases.put(PRO54_2, new CaseInfo[] { PRO54_2, PRO54_3, PRO54_4 });
		compatibleCases.put(PRO54_3, new CaseInfo[] { PRO54_3, PRO54_4, PRO54_5 });
		compatibleCases.put(PRO54_4, new CaseInfo[] { PRO54_4, PRO54_5, PRO54_6 });
		compatibleCases.put(PRO54_5, new CaseInfo[] { PRO54_5, PRO54_6, PRO54_6XL });
		compatibleCases.put(PRO54_6, new CaseInfo[] { PRO54_6, PRO54_6XL });
		compatibleCases.put(PRO54_6XL, new CaseInfo[] { PRO54_6XL });
	}
	
}
