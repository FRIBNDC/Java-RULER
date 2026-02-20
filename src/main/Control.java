package main;

public class Control {

	public static float theoryDCC=0.014f;//1.4%
	public static float uncertaintyLimitBXL=50;//uncertainty limit for BXLW values in new output, ENSDF default limit=25
	
	public static float errorPropagationLimit=0.10f;//10%, only if all variables have relative uncertainty will normal error propagation be used,
	                                                //otherwise, calculate uncertainty from min and max of each variable
	
	public static String theoryDCCType="B";//B for Bricc, H for Hsicc, O for others
	
	public static boolean isUseInputCC=true;
	public static boolean isUseSymmetrized=false;
	
	public static boolean isPrintRULComparison=false;
	
	public static boolean isGoodBriccs=false;
	
	public static boolean isNewOutpath=true;
	
	public static boolean isSuppressBXLWLimit=true;
	
	public static boolean isOnlyCalculateLargeCC=true;
	public static float largeCCLimit=0.01f;
	
	
}
