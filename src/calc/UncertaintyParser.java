package calc;

/*
 * parse non-numerical uncertainty string, like "LT","AP","CA","SY"
 * and assign an estimated numerical uncertainty by using the numerical 
 * value multiplied by a factor that can be user-defined. 
 */
public class UncertaintyParser {

	/*
	 *    ds          dri
	 *    empty       20%*ri
	 *    AP,SY,CA    50%*ri
	 *    LT or LE    50%*ri, ri changed to ri*50
	 *    GT or GE    if ri/sum(ri)<10%, set dri=0
	 *                if ri/sum(ri)>10%, set dri=ri*100
	 */
	
	//factors for non-numerical uncertainty string
	private static float factorForEmpty=0.2f;
	private static float factorForAP=0.5f;
	private static float factorForCA=0.5f;
	private static float factorForSY=0.5f;
	private static float factorForLT=0.5f;
	private static float factorForGT=10000;
	
	private static XDX originalXDX=null;
	private static XDX parsedXDX=null;
	
	private static boolean toParse=true;
	private static boolean isGoodX=false;
		
	/*
	 * drius: string of upper uncertainty
	 * drils: string of lower uncertainty
	 * NOTE: uncertainty string is for real values
	 */
	public static void parse(String s0,String dsu0,String dsl0){
		
		float x0=-1,dxu0=-1,dxl0=-1;//store original value of s,ds
		float x=-1,dxu=-1,dxl=-1;
		
		String s=s0.trim().toUpperCase();
		String dsu=dsu0.trim().toUpperCase();
		String dsl=dsl0.trim().toUpperCase();
		
		isGoodX=false;
		toParse=true;
		
		try{
			x=Float.parseFloat(s);
			x0=x;
			
			isGoodX=true;	
			dxu=Float.parseFloat(dsu);
			dxl=Float.parseFloat(dsl);
		    
			dxu0=dxu;
			dxl0=dxl;
			
			toParse=false;
			
		}catch(NumberFormatException e){
			if(!isGoodX || (!dsu.equals(dsl)&&!dsu.isEmpty()&&!dsl.isEmpty()))
				toParse=false;
		}
		
		if(toParse){
			String ds=dsu;
			
			//if reaching here, ds is non-numerical, eg, AP,LT or LE, GT or GE, SY, CA, or empty
		    if(ds.isEmpty()){
		    	dxu=factorForEmpty*x;
		    }else if(ds.equals("AP")){
		    	dxu=factorForAP*x;
		    }else if(ds.equals("LT") || ds.equals("LE")){
		    	if(factorForLT>=1)//if factor>1, force to set it to be 0.5
		    		factorForLT=0.5f;
		    	
		    	dxu=factorForLT*x;
		    	x=x-dxu;
		    	dxl=x*0.999f;
		    }else if(ds.equals("GT") || ds.equals("GE")){
		    	dxu=factorForGT*x;
		    	dxl=0;
		    }else if(ds.equals("SY"))
		    	dxu=factorForSY*x;
		    else if(ds.equals("CA"))
		    	dxu=factorForCA*x;		    
		    
		    if(dxl<0)
		    	dxl=dxu;
		    else if(dxl>0 && dxl==x)
		    	dxl=x*0.999f;
		}

	   
	    originalXDX=new XDX(x0,dxu0,dxl0);
	    parsedXDX=new XDX(x,dxu,dxl);
	}
	   	
	public static void parse(String s,String ds){
		parse(s,ds,ds);
	}
	
	public static XDX findOriginalXDX(String s,String dsu,String dsl){
		parse(s,dsu,dsl);
		return originalXDX;
	}
	
	public static XDX findParsedXDX(String s,String dsu,String dsl){
		parse(s,dsu,dsl);
		return parsedXDX;		
	}
	
	public static XDX getOriginalXDX(){return originalXDX;}
	public static XDX getParsedXDX(){return originalXDX;}	
		
	public static void setDRIfactorForEmpty(float x){factorForEmpty=x;}
	public static void setDRIfactorForAP(float x){factorForAP=x;}
	public static void setDRIfactorForLT(float x){factorForLT=x;}
	public static void setDRIfactorForGT(float x){factorForGT=x;}
	public static void setDRIfactorForCA(float x){factorForCA=x;}
	public static void setDRIfactorForSY(float x){factorForSY=x;}
	
	public static float getDRIfactorForEmpty(){return factorForEmpty;}
	public static float getDRIfactorForAP(){return factorForAP;}
	public static float getDRIfactorForLT(){return factorForLT;}
	public static float getDRIfactorForGT(){return factorForGT;}
	public static float getDRIfactorForCA(){return factorForCA;}
	public static float getDRIfactorForSY(){return factorForSY;}
}
