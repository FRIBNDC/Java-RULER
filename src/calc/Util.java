package calc;

public class Util {
	/*
	 * calculate total conversion coefficient using BrIcc program
	 * NOTE that: even without giving any input uncertainties,
	 *            BrIcc will still generate uncertainty for 
	 *            calculated icc value
	 */
	public static XDX icc(int Z,double eg,String mult,double mr){
		XDX cc=new XDX();
		
		String es=Double.toString(eg);
		String des="0";
		String mrs=Double.toString(mr);
		String dmrs="0";
				
		float[] out=BriccsWrap.getICC(Z, es, des,mult, mrs, dmrs);
		
		cc.x=out[0];
		cc.dxl=out[1];
		cc.dxu=out[1];
			
		return cc;
	}
	
	public static XDX icc(int Z,double eg,String mult){
		return icc(Z,eg,mult,0);
	}
	
	public static XDX icc(int Z,double eg,double deg,String mult,double mr,double dmru,double dmrl){
		XDX cc=new XDX();
		
		float[] out=BriccsWrap.getICC(Z, eg,deg,mult,mr,dmru,dmrl);
		
		cc.x=out[0];
		cc.dxl=out[1];
		cc.dxu=out[1];
			
		return cc;
	}
	
	public static XDX icc(int Z,double eg,double deg,String mult,double mr,double dmru){
		return icc(Z,eg,deg,mult,mr,dmru,dmru);
	}
}
