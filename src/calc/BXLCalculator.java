package calc;

import java.util.Vector;

public class BXLCalculator {
	
	private int z;
	private double t,dtu,dtl;
	private double mass;
		
	
	//if false, the external BrIcc program will be used to calculate cc in fly
	private boolean isUseInputCC=true;
	
	private boolean isOnlyCalculateLargeCC=true;//only re-calculate large CC (>0.01) if true,
	                                            //otherwise, re-calculate all,
	                                            //when is UseInputCC is set to be false (use CC from Bricc)
	
	private float errorPropagationLimit=0.1f;//10%, if all relative uncertainty<10%, use normal error propation
	private float largeCCLimit=0.01f;

	
    private GammaBranchingCalculator BRCalc=new GammaBranchingCalculator();
	private Vector<GammaBranch> branches=BRCalc.branchsV();//set the branches here to point to the branches from BranchingCalculator

	private GammaBranch currentBranch=new GammaBranch();
	
    
	private final double[] constantBESP={0,6.446E-4,5.940E-6,5.940E-8,6.285E-10,6.929E-12};
	private final double[] constantBELW={0,6.764E-6,9.527E6,2.045E19,6.503E31,2.895E44};
	
	private final double[] constantBMSP={0,1.791,1.650E-2,1.650E-4,1.746E-6,1.925E-8};
	private final double[] constantBMLW={0,2.202E-5,3.102E7,6.659E19,2.117E32,9.426E44};

	//Recommended Upper Limit of BXLW, from ENSDF manual
	//                           A=6-44    45-150   >150 
	/*
	//Isoscalar
	private final double[] RULE1IS={0.003,    0,    0};
	private final double[] RULE2IS={  100,  300, 1000};
	private final double[] RULE3IS={  100,  100,  100};
	private final double[] RULE4IS={  100,  100,    0};
	private final double[] RULM1IS={ 0.03,    0,    0};
	private final double[] RULM2IS={  0.1,    0,    0};
	private final double[] RULM3IS={    0,    0,    0};
	private final double[] RULM4IS={    0,   30,   10};
	
	//Isovector
	private final double[] RULE1IV={  0.3, 0.01, 0.01};
	private final double[] RULE2IV={   10,    0,    0};
	private final double[] RULE3IV={    0,    0,    0};
	private final double[] RULE4IV={    0,    0,    0};
	private final double[] RULM1IV={   10,    3,    2};
	private final double[] RULM2IV={    3,    1,    1};
	private final double[] RULM3IV={   10,   10,   10};
	private final double[] RULM4IV={    0,    0,    0};
	*/
	
	
	private static final double[][] RULIS={
			{0.003,    0,    0},	 //RUL E1 IS
			{  100,  300, 1000},	 //RUL E2 IS
			{  100,  100,  100},	 //RUL E3 IS
			{  100,  100,    0},	 //RUL E4 IS
			{ 0.03,    0,    0},	 //RUL M1 IS
			{  0.1,    0,    0},	 //RUL M2 IS
			{    0,    0,    0},	 //RUL M3 IS
			{    0,   30,   10} 	 //RUL M4 IS			
	};
	private static final double[][] RULIV={
			{  0.3, 0.01, 0.01},	 //RUL E1 IV
			{   10,    0,    0},	 //RUL E2 IV
			{    0,    0,    0},	 //RUL E3 IV
			{    0,    0,    0},	 //RUL E4 IV
			{   10,    3,    2},	 //RUL M1 IV
			{    3,    1,    1},	 //RUL M2 IV
			{   10,   10,   10},	 //RUL M3 IV
			{    0,   10,    0} 	 //RUL M4 IV		
	};
	
	
	public BXLCalculator(){
		reset();
	}
		
	public void setZ(int z){this.z=z;BRCalc.setZ(z);}
	public void setMass(double mass){this.mass=mass;BRCalc.setMass(mass);}
	public void setT12(double t,double dt){this.t=t;this.dtu=dt;this.dtl=dt;BRCalc.setT12(t, dt);}
	public void setT12(double t,double dtu,double dtl){this.t=t;this.dtu=dtu;this.dtl=dtl;BRCalc.setT12(t, dtu, dtl);}
	
	public int Z(){return z;}
	public double mass(){return mass;}
	public double t12(){return t;}
	public double dtu(){return dtu;}
	public double dtl(){return dtl;}
	
	
    public void setCurrentBranch(GammaBranch branch){currentBranch=branch;}
	public void setIsUseInputCC(boolean b){isUseInputCC=b;}
	public void setIsOnlyCalculateLargeCC(boolean b){isOnlyCalculateLargeCC=b;}
	public void setErrorPropationLimit(float f){errorPropagationLimit=f;}
	public void setLargeCCLimit(float f){largeCCLimit=f;}
	
	public boolean isUseInputCC(){return isUseInputCC;}
	public boolean isOnlyCalculateLargeCC(){return isOnlyCalculateLargeCC;}
	public float errorPropagtinLimit(){return errorPropagationLimit;}
	
	public Vector<GammaBranch> branchsV(){return branches;}
	public int nBranches(){return branches.size();}
	public GammaBranch branchAt(int i){return branches.get(i);}
	public BXLWResult getBXLW(int nb,String EM){return branches.get(nb).getBXLW(EM);}
	
	public void addGammaBranch(double e,double de){
        BRCalc.addBranch(e, de);//new branch is added into branches in BRCalc, which is the same Vector as the branches here
		setCurrentBranch(branches.lastElement());
	}
	
	public void addGammaBranch(double e,double de,double ri,double driu,double dril,
			String mult,double mr,double dmru,double dmrl,double cc,double dccu,double dccl){
		BRCalc.addBranch(e, de, ri, driu,dril, mult, mr, dmru, dmrl, cc, dccu, dccl);//new branch is added into branches in BRCalc, which is the same Vector as the branches here
		setCurrentBranch(branches.lastElement());
	}
	
	public void reset(){
		setMass(0);
		setT12(0,0);

		largeCCLimit=0.01f;
		errorPropagationLimit=0.1f;
		
		isUseInputCC=true;
		isOnlyCalculateLargeCC=true;
		
		BRCalc=new GammaBranchingCalculator();
		branches=BRCalc.branchsV();//set the branches here to point to the branches from BranchingCalculator
	}
	
	
	public void calculateAll(){
		if(branches.size()==0)
			return;
		
		if(branches.size()==1){
			GammaBranch gb=branches.get(0);
			if(gb.ri<=0f){
				gb.setIntensity(100, 0);
			}
		}
			
		//calculateBranchingsFromRI();
		
		for(int i=0;i<branches.size();i++){
			GammaBranch gb=branches.get(i);
			
			setCurrentBranch(gb);
			
			int count=0;
			for(int j=0;j<gb.EMs.length;j++){
				String type=gb.EMs[j];
				
				//debug
				//System.out.println(" eg="+gb.e+" *"+type+"*"+" j="+j+" size="+gb.EMs.length+" gb.mr="+gb.mr+" isGoodEM(type)="+gb.isGoodEM(type)+" pt="+gb.pt);
				
				if(!gb.isGoodEM(type))
					continue;
				
				String Loption="L";
				int L=Integer.parseInt(type.substring(type.length()-1));
				if(count>0)
					Loption="L+1";
				
				if(L>0){
					BXLWResult bx=calculateBXLW(gb,t,L,Loption,type);
					                    
					gb.BXLWmap.put(type, bx);
					

					count++;
				}
			}
		}
		
		calculateBranchingsFromRI();
			
		//debug
		//for(int i=0;i<branches.size();i++){
		//	GammaBranch gb=branches.get(i);
		//	System.out.println("In BXLCalculator 181: eg="+gb.e+" pt="+gb.pt());
		//}
		
	}
	
	public BXLWResult calculateBXLW(GammaBranch gb,double t,int L,String Loption,String type){
		
		setCurrentBranch(gb);		
		
		//BXLWResult bx=BXLW(L, Loption, type);
		
		BXLWResult bx=BXLW(L, Loption, type);

		//debug
        //System.out.println("  gamma="+gb.e+" type="+type+" x="+bx.x+" dxu="+bx.dxu+" dxl="+bx.dxl+" max="+max+" min="+min);
		
		return bx;
	}
	

	/*
	 * called after calculateAll() in order to use updated CC values
	 */
	private void calculateBranchingsFromRI(){
		BRCalc.setIsUseInputCC(isUseInputCC);
		BRCalc.setIsOnlyCalculateLargeCC(isOnlyCalculateLargeCC);
		BRCalc.setLargeCCLimit(largeCCLimit);
		
		BRCalc.calculateBranchingsFromRI();
	}
	
	@SuppressWarnings("unused")
	private void calculateBranchingsFromTI(){
		BRCalc.setIsUseInputCC(isUseInputCC);
		BRCalc.setIsOnlyCalculateLargeCC(isOnlyCalculateLargeCC);
		BRCalc.setLargeCCLimit(largeCCLimit);
		
		BRCalc.calculateBranchingsFromTI();
	}
	
	/////////////////////////////
	// calculating functions
	////////////////////////////
	
	public double BELDOWN_SP(int L){
		try{
			if(mass<=0)
				return -1;
			
			return constantBESP[L]*Math.pow(mass,2.0*L/3.0);
		}catch(Exception e){}
			
		return -1;
	}
    
	
    public double BE1DOWN_SP(){return BELDOWN_SP(1);}
	public double BE2DOWN_SP(){return BELDOWN_SP(2);}
	public double BE3DOWN_SP(){return BELDOWN_SP(3);}
	public double BE4DOWN_SP(){return BELDOWN_SP(4);}
	public double BE5DOWN_SP(){return BELDOWN_SP(5);}

	public double BMLDOWN_SP(int L){
		try{
			if(mass<=0)
				return -1;
			
			return constantBMSP[L]*Math.pow(mass,2.0*(L-1)/3.0);
		}catch(Exception e){}
			
		return -1;
	}
	
	public double BXLDOWN_SP(int L,String option){
		if(option.toUpperCase().trim().indexOf("M")==0)
			return BMLDOWN_SP(L);
		else
			return BELDOWN_SP(L);
	}
	
	public double BM1DOWN_SP(){return BMLDOWN_SP(1);}
	public double BM2DOWN_SP(){return BMLDOWN_SP(1);}
	public double BM3DOWN_SP(){return BMLDOWN_SP(1);}
	public double BM4DOWN_SP(){return BMLDOWN_SP(1);}
	public double BM5DOWN_SP(){return BMLDOWN_SP(1);}

	public double T12EL_SP(double eg,int L){
		try{
			if(mass<=0 || eg<=0)
				return -1;
			
			return constantBELW[L]/Math.pow(mass,2.0*L/3.0)/Math.pow(eg,2*L+1);
		}catch(Exception e){}
			
		return -1;
	}

	public double T12ML_SP(double eg,int L){
		try{		
			if(mass<=0 || eg<=0)
				return -1;
			
			return constantBMLW[L]/Math.pow(mass,2.0*(L-1)/3.0)/Math.pow(eg,2*L+1);
		}catch(Exception e){}
			
		return -1;
	}

	/*
	 * option="E" or "M"
	 * 
	 * option="E" by default, if empty or incorrect
	 */
	public double T12XL_SP(double eg,int L,String option){
		if(option.toUpperCase().trim().indexOf("M")==0)
			return T12ML_SP(eg,L);
		else
			return T12EL_SP(eg,L);
	}
	
	
	/*
	 *  Loption="L" or "L+1" for BX(L) and BX(L+1) component, respectively,
	 *   if mixed multipolarity
	 *  Loption="L" by default, if empty or incorrect
	 */
	public double BELW(double eg,double t12,int L,String Loption){return BXLW(eg,t12,L,Loption,"E");}
	public double BMLW(double eg,double t12,int L,String Loption){return BXLW(eg,t12,L,Loption,"M");}
	
	public double BE1W(double eg,double t12){return BELW(eg,t12,1,"L");}
	public double BE2W(double eg,double t12,String Loption){return BELW(eg,t12,2,Loption);}
	public double BE3W(double eg,double t12,String Loption){return BELW(eg,t12,3,Loption);}
	public double BE4W(double eg,double t12,String Loption){return BELW(eg,t12,4,Loption);}
	public double BE5W(double eg,double t12,String Loption){return BELW(eg,t12,5,Loption);}

	public double BM1W(double eg,double t12){return BMLW(eg,t12,1,"L");}
	public double BM2W(double eg,double t12,String Loption){return BMLW(eg,t12,2,Loption);}
	public double BM3W(double eg,double t12,String Loption){return BMLW(eg,t12,3,Loption);}
	public double BM4W(double eg,double t12,String Loption){return BMLW(eg,t12,4,Loption);}
	public double BM5W(double eg,double t12,String Loption){return BMLW(eg,t12,5,Loption);}

	
	public double BELDOWN(double eg,double t12,int L,String Loption){return BELW(eg,t12,L,Loption)*BELDOWN_SP(L);}
	public double BMLDOWN(double eg,double t12,int L,String Loption){return BMLW(eg,t12,L,Loption)*BMLDOWN_SP(L);}
	
	public double BE1DOWN(double eg,double t12){return BELDOWN(eg,t12,1,"L");}
	public double BE2DOWN(double eg,double t12,String Loption){return BELDOWN(eg,t12,2,Loption);}
	public double BE3DOWN(double eg,double t12,String Loption){return BELDOWN(eg,t12,3,Loption);}
	public double BE4DOWN(double eg,double t12,String Loption){return BELDOWN(eg,t12,4,Loption);}
	public double BE5DOWN(double eg,double t12,String Loption){return BELDOWN(eg,t12,5,Loption);}	
	
	public double BM1DOWN(double eg,double t12){return BMLDOWN(eg,t12,1,"L");}
	public double BM2DOWN(double eg,double t12,String Loption){return BMLDOWN(eg,t12,2,Loption);}
	public double BM3DOWN(double eg,double t12,String Loption){return BMLDOWN(eg,t12,3,Loption);}
	public double BM4DOWN(double eg,double t12,String Loption){return BMLDOWN(eg,t12,4,Loption);}
	public double BM5DOWN(double eg,double t12,String Loption){return BMLDOWN(eg,t12,5,Loption);}	
	



	public double BELUP(double eg,double t12,int L,String Loption){return BXLUP(eg,t12,L,Loption,"E");}
	public double BMLUP(double eg,double t12,int L,String Loption){return BXLUP(eg,t12,L,Loption,"M");}
	
	public double BE1UP(double eg,double t12){return BELUP(eg,t12,1,"L");}
	public double BE2UP(double eg,double t12,String Loption){return BELUP(eg,t12,2,Loption);}
	public double BE3UP(double eg,double t12,String Loption){return BELUP(eg,t12,3,Loption);}
	public double BE4UP(double eg,double t12,String Loption){return BELUP(eg,t12,4,Loption);}
	public double BE5UP(double eg,double t12,String Loption){return BELUP(eg,t12,5,Loption);}	
	
	public double BM1UP(double eg,double t12){return BMLUP(eg,t12,1,"L");}
	public double BM2UP(double eg,double t12,String Loption){return BMLUP(eg,t12,2,Loption);}
	public double BM3UP(double eg,double t12,String Loption){return BMLUP(eg,t12,3,Loption);}
	public double BM4UP(double eg,double t12,String Loption){return BMLUP(eg,t12,4,Loption);}
	public double BM5UP(double eg,double t12,String Loption){return BMLUP(eg,t12,5,Loption);}

	
	public double[] BXLWs(double eg,double t12,String mult){
		String[] types={"E","M"};
		
		String s=mult.toUpperCase().trim();
		if(s.isEmpty())
			return new double[0];
		
		double[] temp=new double[10];
		int count=0;
		
		for(int i=0;i<2;i++){
			String type=types[i];
			for(int L=1;L<=5;L++){
				if(s.indexOf(type+L)>=0){
					if(count==0)
						temp[count]=this.BXLW(eg,t12,L,"L", type);
					else
						temp[count]=this.BXLW(eg,t12,L,"L+1", type);
					
					count++;
				}
			}
		}
		
		if(count>2)
			count=2;
		
		double[] out=new double[count];
		for(int i=0;i<count;i++)
			out[i]=temp[i];
		
		return out;		
	}
	
	public double[] BXLWs(double eg,double t12){
		String mult=currentBranch.mult;
		
		return BXLWs(eg,t12,mult);
		
	}
	public double[] BXLWs(double t12){
		double e=currentBranch.e;
		String mult=currentBranch.mult;
		return BXLWs(e,t12,mult);
		
	}	
	
	
	/*
	 * Loption="L" or "L+1" for BX(L) and BX(L+1) component, respectively,
	 *   if mixed multipolarity
	 * type="E" or "M"
	 * 
	 * Loption="L" and type="E" by default, if empty or incorrect
	 */
	public double BXLW(double eg,double t12,int L,String Loption,String type){
		try{
			double sumTI=0;
			double MR_factor=1;
			double mr=currentBranch.mr;
			double ri=currentBranch.ri;
			double cc=currentBranch.cc;
			
			if(Loption.toUpperCase().trim().equals("L+1"))
				MR_factor=mr*mr/(mr*mr+1);
			else
				MR_factor=1.0/(mr*mr+1);
			
			if(isUseInputCC){
				sumTI=0;
				for(int i=0;i<branches.size();i++){
					GammaBranch gb=branches.get(i);
					cc=gb.cc;
					if(cc<0)
						cc=0;
					
					if(gb.ri>0)
						sumTI+=gb.ri*(1+cc);
					else
						sumTI+=gb.ti;
					
					//debug
					//System.out.println("EG="+gb.e+" MULT="+gb.mult+" RI= "+gb.ri+" TI="+gb.ti);
				}
			}else{
				sumTI=0;
				for(int i=0;i<branches.size();i++){
					GammaBranch gb=branches.get(i);
					cc=gb.cc;//input cc
					
					if(!isOnlyCalculateLargeCC || cc<0 || cc>largeCCLimit){
				        gb.calcICC=Util.icc(z, gb.e,gb.deu, gb.mult,gb.mr,gb.dmru,gb.dmrl);
						cc=gb.calcICC.x;
					}

					if(cc<0)
						cc=0;

					if(gb.ri>0)
						sumTI+=gb.ri*(1+cc);
					else
						sumTI+=gb.ti;
				}
			}
			
			
            double result=(T12XL_SP(eg,L,type)/t12)*MR_factor*ri/sumTI;
            
	        //debug
            //System.out.println("  gamma="+eg+" type="+type+" L="+L+" Loption="+Loption+" T12XL_SP(eg,L,type)="+T12XL_SP(eg,L,type));
            //System.out.println("     t12="+t12+" mr="+mr+" cc="+cc+" MR_factor="+MR_factor+" ri="+ri+" sumTI="+sumTI);
            //System.out.println("  curentBranch.cc="+currentBranch.cc+" isUseInputCC="+isUseInputCC+" result="+result);

			return result;
			
		}catch(Exception e){
			return -1;
		}
	}
	
	
	public BXLWResult BXLW_new(int L,String Loption,String type){
		double x=-1,dxu=0,dxl=0,rel_dxu=0,rel_dxl=0;
        double rel_driu=0,rel_dril=0,rel_dtu=0,rel_dtl=0,rel_deg=0,rel_dmru=0,rel_dmrl=0;
        double rel_dmrfu=0,rel_dmrfl=0;//MR factor
        double br=0,rel_dbru=0,rel_dbrl=0;
        
		BXLWResult result=new BXLWResult();
		boolean canUseNormalError=true;
		
		try{
			double sumTI=0;
			double MR_factor=1;
			double mr=currentBranch.mr;//note that mr is already set as positive value when adding branching
			double ri=currentBranch.ri;
			double eg=currentBranch.e;
			
			if(eg>0){
				rel_deg=currentBranch.deu/eg;
				if(rel_deg<0 || rel_deg>=errorPropagationLimit)
					canUseNormalError=false;
			}
				
			if(ri>0){
				rel_driu=currentBranch.driu/ri;
				rel_dril=currentBranch.dril/ri;
				if(rel_driu<0 || rel_dril<0 || rel_driu>=errorPropagationLimit || rel_dril>=errorPropagationLimit)
					canUseNormalError=false;
			}

			if(t>0){
				rel_dtu=dtu/t;
				rel_dtl=dtl/t;
				if(branches.size()>1)
					if(rel_dtu<0 || rel_dtl<0 || rel_dtu>=errorPropagationLimit || rel_dtl>=errorPropagationLimit)
						canUseNormalError=false;
			}

			if(mr>0){
				rel_dmru=currentBranch.dmru/mr;
				rel_dmrl=currentBranch.dmrl/mr;
				if(rel_dmru<0 || rel_dmrl<0 || rel_dmru>=errorPropagationLimit || rel_dmrl>=errorPropagationLimit)
					canUseNormalError=false;
			}
			
			br=currentBranch.br;
			if(br>0){
			    //uncertainty form normal error propagation
				rel_dbru=currentBranch.dbru_ep/br;
				rel_dbrl=currentBranch.dbrl_ep/br;
			}
			
			if(Loption.toUpperCase().trim().equals("L+1")){
				MR_factor=mr*mr/(mr*mr+1);
				if(mr>0){
					rel_dmrfu=2/(mr*mr+1)*rel_dmru;
					rel_dmrfl=2/(mr*mr+1)*rel_dmrl;
				}
			}else{
				MR_factor=1.0/(mr*mr+1);
				if(mr>0){
					rel_dmrfu=2*mr*mr/(mr*mr+1)*rel_dmrl;
					rel_dmrfl=2*mr*mr/(mr*mr+1)*rel_dmru;
				}
			}
			
			double cc=0,dccu=0,dccl=0;
			
			sumTI=0;
			for(int i=0;i<branches.size();i++){
				GammaBranch gb=branches.get(i);
				cc=gb.cc;//input cc
				dccu=gb.dccu;
				dccl=gb.dccl;
							
				
		        if(!isUseInputCC && gb.calcICC!=null){
		        	//NOTE that calcICC is calculated in calculateBRFromRI() that must be called before BXLW()
		        	cc=gb.calcICC.x;
		        	dccu=gb.calcICC.dxu;
		        	dccl=gb.calcICC.dxl;
		        }

				if(cc<0) cc=0;
				if(dccu<0) dccu=0;
				if(dccl<0) dccl=0;
				
				//System.out.println("##### e="+eg+"  cc="+cc+" dccu="+dccu+" dccl="+dccl);
				
				if(gb.ri>0){
					sumTI+=gb.ri*(1+cc);					
				}else{
					sumTI+=gb.ti;	
				}					
			}		
			
            x=(T12XL_SP(currentBranch.e,L,type)/t)*MR_factor*ri/sumTI;
                          
            rel_dxu=Math.pow((2*L+1)*rel_deg,2)+rel_dtl*rel_dtl+rel_dmrfu*rel_dmrfu+rel_dbru*rel_dbru;
            rel_dxu=Math.sqrt(rel_dxu);
          
            rel_dxl=Math.pow((2*L+1)*rel_deg,2)+rel_dtu*rel_dtu+rel_dmrfl*rel_dmrfl+rel_dbrl*rel_dbrl;
            rel_dxl=Math.sqrt(rel_dxl);
            
            dxu=x*rel_dxu;
            dxl=x*rel_dxl;
            
            result.normalErrorBXLW.x=x;
            result.normalErrorBXLW.dxu=dxu;
            result.normalErrorBXLW.dxl=dxl;
			
			result.normalErrorBranching=new XDX(br,rel_dbru*br,rel_dbrl*br);
			result.normalErrorTotalBranching=new XDX(currentBranch.tbr,currentBranch.dtbru_ep,currentBranch.dtbrl_ep);
			result.normalErrorPartialT=new XDX(currentBranch.pt,currentBranch.dptu_ep,currentBranch.dptl_ep);
			
            result.canUseNormalError=canUseNormalError;
            
			//System.out.println("### eg="+eg+"  br="+br+" dbru="+rel_dbru*br+" dbrl="+rel_dbrl*br+" dcc="+rel_dcciu+" "+rel_dccil);
            //System.out.println("### eg="+eg+"rel_dtl="+rel_dtl+" rel_dtu="+rel_dtu+" rel_dmrfl="+rel_dmrfl+" rel_dmrfu="+rel_dmrfu+" rel_dbrl="+rel_dbrl+" rel_dbru="+rel_dbru);
            //System.out.println("              BXLW="+x+" rel_dxu="+rel_dxu+" rel_dxl="+rel_dxl+" canUseNormalError="+canUseNormalError);
            
    		float max=(float)maxBXLW(L, Loption, type);//currentBranch.dbru is set in this call
    		float min=(float)minBXLW(L, Loption, type);//currentBranch.dbrl is set in this call
    		if(x>0){
    			if(max>0 && max>=x) 
    				dxu=max-x;
    			if(min>=0 && min<=x)
    				dxl=x-min;
    			   			
                result.extremeErrorBXLW.x=x;
    			result.extremeErrorBXLW.dxu=dxu;
    			result.extremeErrorBXLW.dxl=dxl;  		
    			
    			result.extremeErrorBranching=new XDX(br,currentBranch.dbru_ex,currentBranch.dbrl_ex);
    			result.extremeErrorTotalBranching=new XDX(currentBranch.tbr,currentBranch.dtbru_ex,currentBranch.dtbrl_ex);
    			result.extremeErrorPartialT=new XDX(currentBranch.pt,currentBranch.dptu_ex,currentBranch.dptl_ex);
    			
    			//System.out.println("### eg="+eg+"  br="+br+" cci="+cci+" tbr="+tbr+"  "+currentBranch.dtbru_ex+"  "+currentBranch.dtbrl_ex);
    		}
    		
	        //debug
            //System.out.println("  gamma="+eg+" type="+type+" L="+L+" Loption="+Loption+" T12XL_SP(eg,L,type)="+T12XL_SP(eg,L,type));
            //System.out.println("     t12="+t12+" mr="+mr+" cc="+cc+" MR_factor="+MR_factor+" ri="+ri+" sumTI="+sumTI);
            //System.out.println("  curentBranch.cc="+currentBranch.cc+" isUseInputCC="+isUseInputCC+" result="+result);
            
			return result;
			
		}catch(Exception e){
			return result;
		}
	}
	
	/*
	 * calculate BXLW uncertainty using normal error propagation, when all variables have relative uncertainty dx/x<=10
	 * Loption="L" or "L+1" for BX(L) and BX(L+1) component, respectively,
	 *   if mixed multipolarity
	 * type="E" or "M"
	 * 
	 * Loption="L" and type="E" by default, if empty or incorrect
	 */
	public BXLWResult BXLW(int L,String Loption,String type){
		double x=-1,dxu=0,dxl=0,rel_dxu=0,rel_dxl=0;
        double rel_driu=0,rel_dril=0,rel_dtu=0,rel_dtl=0,rel_deg=0,rel_dmru=0,rel_dmrl=0;
        double rel_dmrfu=0,rel_dmrfl=0;//MR factor
        double br=0,rel_dbru=0,rel_dbrl=0;
        double tbr=0,rel_dtbru=0,rel_dtbrl=0,rel_dtiu=0,rel_dtil=0;
        double rel_drku=0,rel_drkl=0,rel_dccku=0,rel_dcckl=0,rel_dcciu=0,rel_dccil=0;
        double pt=0,rel_dptu=0,rel_dptl=0;//partial halflife
        
		BXLWResult result=new BXLWResult();
		boolean canUseNormalError=true;
		
		try{
			double sumTI=0;
			double MR_factor=1;
			double mr=currentBranch.mr;//note that mr is already set as positive value when adding branching
			double ri=currentBranch.ri;
			double ti=currentBranch.ti;
			double cci=currentBranch.cc;
			double eg=currentBranch.e;
			
			if(eg>0){
				rel_deg=currentBranch.deu/eg;
				if(rel_deg<0 || rel_deg>=errorPropagationLimit)
					canUseNormalError=false;
			}
				
			if(ri>0){
				rel_driu=currentBranch.driu/ri;
				rel_dril=currentBranch.dril/ri;
				if(rel_driu<0 || rel_dril<0 || rel_driu>=errorPropagationLimit || rel_dril>=errorPropagationLimit)
					canUseNormalError=false;
			}

			if(t>0){
				rel_dtu=dtu/t;
				rel_dtl=dtl/t;
				if(branches.size()>1)
					if(rel_dtu<0 || rel_dtl<0 || rel_dtu>=errorPropagationLimit || rel_dtl>=errorPropagationLimit)
						canUseNormalError=false;
			}

			if(mr>0){
				rel_dmru=currentBranch.dmru/mr;
				rel_dmrl=currentBranch.dmrl/mr;
				if(rel_dmru<0 || rel_dmrl<0 || rel_dmru>=errorPropagationLimit || rel_dmrl>=errorPropagationLimit)
					canUseNormalError=false;
			}
			
			if(Loption.toUpperCase().trim().equals("L+1")){
				MR_factor=mr*mr/(mr*mr+1);
				if(mr>0){
					rel_dmrfu=2/(mr*mr+1)*rel_dmru;
					rel_dmrfl=2/(mr*mr+1)*rel_dmrl;
				}
			}else{
				MR_factor=1.0/(mr*mr+1);
				if(mr>0){
					rel_dmrfu=2*mr*mr/(mr*mr+1)*rel_dmrl;
					rel_dmrfl=2*mr*mr/(mr*mr+1)*rel_dmru;
				}
			}
			
			
			boolean isUseRI=true;
			double cc=0,dccu=0,dccl=0;
			
			sumTI=0;
			for(int i=0;i<branches.size();i++){
				GammaBranch gb=branches.get(i);
				cc=gb.cc;//input cc
				dccu=gb.dccu;
				dccl=gb.dccl;
				
				if(!isUseInputCC){
					if(!isOnlyCalculateLargeCC || cc<0 || cc>largeCCLimit){
				        gb.calcICC=Util.icc(z, gb.e,gb.deu, gb.mult,gb.mr,gb.dmru,gb.dmrl);
						cc=gb.calcICC.x;
						dccu=gb.calcICC.dxu;
						dccl=gb.calcICC.dxl;
					}
				}

				if(cc<0) cc=0;
				if(dccu<0) dccu=0;
				if(dccl<0) dccl=0;
				
				//System.out.println("##### e="+eg+"  cc="+cc+" dccu="+dccu+" dccl="+dccl);
				
				if(cc>0){
					rel_dccku=dccu/cc;
					rel_dcckl=dccl/cc;
				}else{
					rel_dccku=0;
					rel_dcckl=0;
				}
				
				if(gb.ri>0){
					sumTI+=gb.ri*(1+cc);
					if(i!=branches.indexOf(currentBranch)){
						rel_drku=gb.driu/gb.ri;
						rel_drkl=gb.dril/gb.ri;
						
						//br increases as rk and cck decreases (k!=i)
						rel_dbru+=Math.pow(gb.ri*(1+cc)*rel_drkl,2)+Math.pow(gb.ri*cc*rel_dcckl,2);
						rel_dbrl+=Math.pow(gb.ri*(1+cc)*rel_drku,2)+Math.pow(gb.ri*cc*rel_dccku,2);
					}else{
						cci=cc;
						rel_dcciu=rel_dccku;
						rel_dccil=rel_dcckl;
						
						rel_dbru+=Math.pow(gb.ri*cc*rel_dcckl,2);
						rel_dbrl+=Math.pow(gb.ri*cc*rel_dccku,2);
					}
						
				}else{
					sumTI+=gb.ti;	
					if(i!=branches.indexOf(currentBranch)){
						//br increases as tik decreases (k!=i)
						rel_dbru+=gb.dtil*gb.dtil;
						rel_dbrl+=gb.dtiu*gb.dtiu;
					}else{
						cci=cc;
						rel_dcciu=rel_dccku;
						rel_dccil=rel_dcckl;
						isUseRI=false;
					}
				}					
			}		
			
			if(isUseRI){
				//br increases as ri increases
				rel_dbru+=Math.pow(sumTI-ri*(1+cci),2)*rel_driu*rel_driu;
				rel_dbrl+=Math.pow(sumTI-ri*(1+cci),2)*rel_dril*rel_dril;
			}else{
				rel_dbru+=Math.pow(rel_driu*sumTI,2);
				rel_dbrl+=Math.pow(rel_dril*sumTI,2);
			}
			
			if(sumTI>0){
				rel_dbru=Math.sqrt(rel_dbru)/sumTI;
				rel_dbrl=Math.sqrt(rel_dbrl)/sumTI;
			}
			
			br=ri/sumTI*100;
			currentBranch.br=br;
			currentBranch.dbru_ep=br*rel_dbru;//uncertainty from normal error propagation
			currentBranch.dbrl_ep=br*rel_dbrl;
			
			if(isUseRI){
				if(branches.size()>1){
					rel_dtbru=Math.sqrt(rel_dbru*rel_dbru+Math.pow(cci*rel_dcciu/(1+cci),2));
					rel_dtbrl=Math.sqrt(rel_dbrl*rel_dbrl+Math.pow(cci*rel_dccil/(1+cci),2));
				}
				ti=ri*(1+cci);
			}else{
				rel_dtbru=rel_dbru*rel_dbru-rel_driu*rel_driu+Math.pow(currentBranch.dtiu/currentBranch.ti,2);
				rel_dtbrl=rel_dbrl*rel_dbrl-rel_dril*rel_dril+Math.pow(currentBranch.dtil/currentBranch.ti,2);
				rel_dtbru=Math.sqrt(rel_dtiu);
				rel_dtbrl=Math.sqrt(rel_dtil);
			}

			tbr=ti/sumTI*100;
			currentBranch.tbr=tbr;
			currentBranch.dtbru_ep=tbr*rel_dtbru;
			currentBranch.dtbrl_ep=tbr*rel_dtbrl;
					
			pt=t/br*100;
			rel_dptu=Math.sqrt(rel_dtu*rel_dtu+rel_dbrl*rel_dbrl);
			rel_dptl=Math.sqrt(rel_dtl*rel_dtl+rel_dbru*rel_dbru);
			
			currentBranch.pt=pt;
			currentBranch.dptu_ep=pt*rel_dptu;
			currentBranch.dptl_ep=pt*rel_dptl;
			
            x=(T12XL_SP(currentBranch.e,L,type)/t)*MR_factor*ri/sumTI;
                     
            rel_dxu=Math.pow((2*L+1)*rel_deg,2)+rel_dtl*rel_dtl+rel_dmrfu*rel_dmrfu+rel_dbru*rel_dbru;
            rel_dxu=Math.sqrt(rel_dxu);
          
            rel_dxl=Math.pow((2*L+1)*rel_deg,2)+rel_dtu*rel_dtu+rel_dmrfl*rel_dmrfl+rel_dbrl*rel_dbrl;
            rel_dxl=Math.sqrt(rel_dxl);
            
            dxu=x*rel_dxu;
            dxl=x*rel_dxl;
            
            result.normalErrorBXLW.x=x;
            result.normalErrorBXLW.dxu=dxu;
            result.normalErrorBXLW.dxl=dxl;
			result.normalErrorBranching=new XDX(br,rel_dbru*br,rel_dbrl*br);
			result.normalErrorTotalBranching=new XDX(tbr,rel_dtbru*tbr,rel_dtbrl*tbr);
			result.normalErrorPartialT=new XDX(pt,rel_dptu*pt,rel_dptl*pt);
			
            result.canUseNormalError=canUseNormalError;
            
			//System.out.println("### eg="+eg+"  br="+br+" dbru="+rel_dbru*br+" dbrl="+rel_dbrl*br+" dcc="+rel_dcciu+" "+rel_dccil);
            //System.out.println("### eg="+eg+"rel_dtl="+rel_dtl+" rel_dtu="+rel_dtu+" rel_dmrfl="+rel_dmrfl+" rel_dmrfu="+rel_dmrfu+" rel_dbrl="+rel_dbrl+" rel_dbru="+rel_dbru);
            //System.out.println("              BXLW="+x+" rel_dxu="+rel_dxu+" rel_dxl="+rel_dxl+" canUseNormalError="+canUseNormalError);
            
    		float max=(float)maxBXLW(L, Loption, type);//currentBranch.dbru is set in this call
    		float min=(float)minBXLW(L, Loption, type);//currentBranch.dbrl is set in this call
    		if(x>0){
    			if(max>0 && max>=x) 
    				dxu=max-x;
    			if(min>=0 && min<=x)
    				dxl=x-min;
    			   			
                result.extremeErrorBXLW.x=x;
    			result.extremeErrorBXLW.dxu=dxu;
    			result.extremeErrorBXLW.dxl=dxl;  		
    			result.extremeErrorBranching=new XDX(br,currentBranch.dbru_ex,currentBranch.dbrl_ex);
    			result.extremeErrorTotalBranching=new XDX(tbr,currentBranch.dtbru_ex,currentBranch.dtbrl_ex);
    			result.extremeErrorPartialT=new XDX(pt,currentBranch.dptu_ex,currentBranch.dptl_ex);
    			
    			//System.out.println("### eg="+eg+"  br="+br+" cci="+cci+" tbr="+tbr+"  "+currentBranch.dtbru_ex+"  "+currentBranch.dtbrl_ex);
    		}
    		
	        //debug
            //System.out.println("  gamma="+eg+" type="+type+" L="+L+" Loption="+Loption+" T12XL_SP(eg,L,type)="+T12XL_SP(eg,L,type));
            //System.out.println("     t12="+t12+" mr="+mr+" cc="+cc+" MR_factor="+MR_factor+" ri="+ri+" sumTI="+sumTI);
            //System.out.println("  curentBranch.cc="+currentBranch.cc+" isUseInputCC="+isUseInputCC+" result="+result);
            
			return result;
			
		}catch(Exception e){
			return result;
		}
	}
	
	/*
	 * Loption="L" or "L+1" for BX(L) and BX(L+1) component, respectively,
	 *   if mixed multipolarity
	 * type="E" or "M"
	 * 
	 * Loption="L" and type="E" by default, if empty or incorrect
	 */
	public double BXLUP(double eg,double t12,int L,String Loption,String type){
		try{
			double ji=currentBranch.ji;
			double jf=currentBranch.jf;
			
			if(type.toUpperCase().trim().indexOf("M")==0)
				return BMLDOWN(eg,t12,L,Loption)*(2*ji+1)/(2*jf+1);
			else
				return BELDOWN(eg,t12,L,Loption)*(2*ji+1)/(2*jf+1);
			
		}catch(Exception e){}
		return -1;
	}
	
	
	/*
	 * BXL increases as:
	 *   all EG decrease;
	 *   T1/2 decreases;
	 *   RI(current branch) increases;
	 *   RI(other branches) decreases;
	 *   MR(current branch) decreases, L component;
	 *                      increases, L+1 component;
	 *   MR(other branches) decreases, alpha(L)<alpha(L+1);
	 *                      increases, alpha(L)>alpha(L+1)
	 */
	public double maxBXLW(int L,String Loption,String type){
		try{
			double MR_factor=1;
			double mr=currentBranch.mr;
			double dmru=currentBranch.dmru;
			double dmrl=currentBranch.dmrl;
			double e=currentBranch.e;
			double del=currentBranch.del;
			double ri=currentBranch.ri;
			double driu=currentBranch.driu;
			double ti=currentBranch.ti;
			double dtiu=currentBranch.dtiu;
            String mult=currentBranch.mult;
			
			if(dmru<0) dmru=0;
			if(dmrl<0) dmrl=0;
			if(driu<0) driu=0;
			if(dtiu<0) dtiu=0;
			
            double factor1=(mr+dmru)*(mr+dmru);
            double factor2=(mr-dmrl)*(mr-dmrl);
            
			if(Loption.toUpperCase().trim().equals("L+1")){
				if(factor1>factor2)
					MR_factor=factor1/(factor1+1);//increases as mr*mr increases
				else
					MR_factor=factor2/(factor2+1);
			}else{
				if((mr+dmru)*(mr-dmrl)<=0)
					MR_factor=1;
				else if(factor1>factor2)
					MR_factor=1.0/(factor2+1);//decreases as mr*mr increases
				else
					MR_factor=1.0/(factor1+1);
			}
				
			double minT=t-dtl;
			if(t>0 && minT<=0)
				minT=t*1.0E-10;
			
			//System.out.println("  eg="+e+"   t="+t+" dtl="+dtl+" minT="+minT+" mr="+mr+" e="+e+" de="+del+" MR_factor="+MR_factor+" ri="+ri);
		
			e=e-del;
			ri=ri+driu;
			ti=ti+dtiu;
			
			if(e<0)
				return -1;
			
			double sumTI=0;
			double cc=0;
			if(isUseInputCC){
				sumTI=0;
				for(int i=0;i<branches.size();i++){
					GammaBranch gb=branches.get(i);
					double rk=gb.ri-gb.dril;
					cc=gb.cc-gb.dccl;
					if(cc<0)
						cc=0;
					if(rk<0)
						rk=0;
					
					if(gb.ri<=0){
						double tk=gb.ti-gb.dtil;
						if(tk<0)
							tk=0;
						sumTI+=tk;
					}else if(gb!=currentBranch){
						sumTI+=rk*(1+cc);		
					}else{
						sumTI+=ri*(1+cc);
						if(ti<=0)
							ti=ri*(1+cc);
					}
				}
			}else{
				sumTI=0;
				
				for(int i=0;i<branches.size();i++){
					GammaBranch gb=branches.get(i);
					double ek=gb.e-gb.del;
					double rk=gb.ri-gb.dril;
					double mrk=gb.mr;
					
					cc=gb.cc;
					
					if(gb==currentBranch)
						rk=ri;
					
					//System.out.println(" i="+i+" e="+gb.e+" "+(gb!=currentBranch)+" mv.length="+gb.EMs.length+" t="+t+" dtl="+dtl+" minT="+minT+" mr="+mr+" e="+e+" de="+del+" MR_factor="+MR_factor+" ri="+ri);
					if(gb.ri>0){
						if(!isOnlyCalculateLargeCC || cc<0 || cc>largeCCLimit){
							if(gb!=currentBranch){
								String[] mv=gb.EMs;
								int nGoodEM=0;
								for(int j=0;j<mv.length;j++){
									if(gb.isGoodEM(mv[j]))
										nGoodEM++;
								}
								
								//System.out.println("i="+i+" e="+gb.e+" mv.length="+mv.length+" type="+type+" nGoodEM="+nGoodEM+" t="+t+" dtl="+dtl+" minT="+minT+" mult="+mult+" mr="+mr+" e="+e+" de="+del+" MR_factor="+MR_factor+" ri="+ri);
								
								if(mv.length==nGoodEM && nGoodEM==2){//e.g., M1+E2
									XDX icc1=Util.icc(z,ek,mv[0]);
									XDX icc2=Util.icc(z,ek,mv[1]);
									
									double mrk1=gb.mr-gb.dmrl;
									double mrk2=gb.mr+gb.dmru;
									
									if(icc1.x<icc2.x){     //alpha(L)<alpha(L+1),BXLM increases as MR(k) decreases
									    if(mrk1*mrk2<=0)
									    	mrk=0.0;
									    else if(mrk1*mrk1<mrk2*mrk2)
											mrk=mrk1;
										else
											mrk=mrk2;
									}else if(icc1.x>icc2.x){//alpha(L)>alpha(L+1),BXLM increases as MR(k) increases
										if(mrk1*mrk1>mrk2*mrk2)
											mrk=mrk1;
										else
											mrk=mrk2;
									}
										
									XDX icc;
									if(icc1.x==icc2.x)
										icc=icc1;
									else
										icc=Util.icc(z,ek,gb.mult,mrk);
									
									cc=icc.x-icc.dxl;
								}else if(mv.length==nGoodEM && nGoodEM==1){//e.g., M1
									XDX icc=Util.icc(z,ek,mv[0]);
									cc=icc.x-icc.dxl;
								}		
							}else{
								XDX icc=Util.icc(z,e,mult,mr);
								cc=icc.x-icc.dxl;						
								//System.out.println("     @@@@@@@@@ z="+z+" cc="+cc+" MR_factor="+MR_factor+" icc.x="+icc.x+" dxl="+icc.dxl+" dxu="+icc.dxu+" mult="+mult+" mr="+mr);
							}
						}	
						
						if(cc<0)
							cc=0;
						
						sumTI+=rk*(1+cc);
						if(gb==currentBranch && ti<=0)
							ti=rk*(1+cc);
					}else{
						double tk=gb.ti-gb.dtil;
						if(tk<0)
							tk=0;
						sumTI+=tk;
					}
					

				}
			}
			
	        //debug
            //System.out.println("  gamma="+e+" type="+type+" L="+L+" Loption="+Loption+" T12XL_SP(eg,L,type)="+T12XL_SP(e,L,type));
            //System.out.println("     minT="+minT+" mr="+mr+" cc="+cc+" MR_factor="+MR_factor+" ri="+ri+" sumTI="+sumTI);
			double maxBR=ri/sumTI*100;
			double maxTBR=ti/sumTI*100;
			double minPT=minT/maxBR*100;
			currentBranch.dbru_ex=maxBR-currentBranch.br;
			currentBranch.dptl_ex=currentBranch.pt-minPT;
			currentBranch.dtbru_ex=maxTBR-currentBranch.tbr;
						
			return (T12XL_SP(e,L,type)/minT	)*MR_factor*ri/sumTI;
			
			
		}catch(Exception e){}
		
		return -1;
	}
	
	public double minBXLW(int L,String Loption,String type){
		try{
			double MR_factor=1;
			
			double mr=currentBranch.mr;
			double dmru=currentBranch.dmru;
			double dmrl=currentBranch.dmrl;
			double e=currentBranch.e;
			double deu=currentBranch.del;
			double ri=currentBranch.ri;
			double dril=currentBranch.driu;
			double ti=currentBranch.ti;
			double dtil=currentBranch.dtil;
			String mult=currentBranch.mult;
			
			if(dmru<0) dmru=0;
			if(dmrl<0) dmrl=0;
			if(dril<0) dril=0;
			if(dtil<0) dtil=0;
			
            double factor1=(mr+dmru)*(mr+dmru);
            double factor2=(mr-dmrl)*(mr-dmrl);
            
			if(Loption.toUpperCase().trim().equals("L+1")){
				if((mr+dmru)*(mr-dmrl)<=0)
					MR_factor=0.0;
				else if(factor1<factor2)
					MR_factor=factor1/(factor1+1);//increases as mr*mr increases
				else
					MR_factor=factor2/(factor2+1);
			}else{
				if(factor1>factor2)
					MR_factor=1.0/(factor1+1);//decreases as mr*mr increases
				else
					MR_factor=1.0/(factor2+1);
			}
					
          
			double maxT=t+dtu;
			e=e+deu;
			ri=ri-dril;
			ti=ti-dtil;
			
			if(ri<=0)
				ri=0;
			
			double sumTI=0;
			double cc=0;
			if(isUseInputCC){
				sumTI=0;
				for(int i=0;i<branches.size();i++){
					GammaBranch gb=branches.get(i);
					double rk=gb.ri+gb.driu;
					cc=gb.cc+gb.dccu;
					
					if(gb.ri<=0)
						sumTI+=gb.ti+gb.dtiu;
					else if(gb!=currentBranch)
						sumTI+=rk*(1+cc);
					else{
						sumTI+=ri*(1+cc);
						if(ti<=0)
							ti=ri*(1+cc);
					}
				}
			}else{
				sumTI=0;
				
				for(int i=0;i<branches.size();i++){
					GammaBranch gb=branches.get(i);
					double ek=gb.e+gb.deu;
					double rk=gb.ri+gb.driu;
					double mrk=gb.mr;
					
					cc=gb.cc;
					
					if(gb==currentBranch)
						rk=ri;
					
					if(gb.ri>0){
						if(!isOnlyCalculateLargeCC || cc<0 || cc>largeCCLimit){
							if(gb!=currentBranch){
								String[] mv=gb.EMs;
								int nGoodEM=0;
								for(int j=0;j<mv.length;j++){
									if(gb.isGoodEM(mv[j]))
										nGoodEM++;
								}
								
								if(mv.length==nGoodEM && nGoodEM==2){//e.g., M1+E2
									XDX icc1=Util.icc(z,ek,mv[0]);
									XDX icc2=Util.icc(z,ek,mv[1]);
									
									if(icc1.x<icc2.x)     //alpha(L)<alpha(L+1),BXLM increases as MR(k) decreases
										mrk=gb.mr+gb.dmru;
									else if(icc1.x>icc2.x)//alpha(L)>alpha(L+1),BXLM increases as MR(k) increases
										mrk=gb.mr-gb.dmrl;
									
									
									double mrk1=gb.mr-gb.dmrl;
									double mrk2=gb.mr+gb.dmru;
									
									if(icc1.x<icc2.x){     //alpha(L)<alpha(L+1),BXLM increases as MR(k) decreases
										if(mrk1*mrk1>mrk2*mrk2)
											mrk=mrk1;
										else
											mrk=mrk2;
									}else if(icc1.x>icc2.x){//alpha(L)>alpha(L+1),BXLM increases as MR(k) increases
										if(mrk1*mrk2<=0)
									    	mrk=0.0;
									    else if(mrk1*mrk1<mrk2*mrk2)
											mrk=mrk1;
										else
											mrk=mrk2;
									}
									
									XDX icc;
									if(icc1.x==icc2.x)
										icc=icc1;
									else
										icc=Util.icc(z,ek,gb.mult,mrk);
									
									cc=icc.x+icc.dxu;
								}else if(mv.length==nGoodEM && nGoodEM==1){//e.g., M1
									XDX icc=Util.icc(z,ek,mv[0]);
									cc=icc.x+icc.dxu;
								}		
							}else{
								XDX icc=Util.icc(z,e,mult,mr);
								cc=icc.x+icc.dxl;
							}
						}

						if(cc<0)
							cc=0;
						
						sumTI+=rk*(1+cc);
						if(gb==currentBranch && ti<=0)
							ti=rk*(1+cc);
					}else
						sumTI+=gb.ti+gb.dtiu;
					

				}
			}
			

			double minBR=ri/sumTI*100;
			double minTBR=ti/sumTI*100;
			double maxPT=maxT/minBR*100;
			currentBranch.dbrl_ex=currentBranch.br-minBR;
			currentBranch.dptu_ex=maxPT-currentBranch.pt;
			currentBranch.dtbrl_ex=currentBranch.tbr-minTBR;
			
			//System.out.println("**** eg="+e+"ri="+ri+" ti="+ti+" sumTI="+sumTI+"  br="+currentBranch.br+" minBR="+minBR+" tbr="+currentBranch.tbr+" mintbr="+minTBR+" "+currentBranch.dtbrl_ex);

			
			return (T12XL_SP(e,L,type)/maxT)*MR_factor*ri/sumTI;

			
		}catch(Exception e){}
		
		return -1;
	}
		
	public double BXLW2BXLDOWN(double BXLW,int L,String type){return BXLW*BXLDOWN_SP(L,type);}
	public double BXLW2BXLUP(double BXLW,int L,String type){return BXLW2BXLDOWN(BXLW,L,type)*(2*currentBranch.ji+1)/(2*currentBranch.jf+1);}
	
	public double BXLDOWN2BXLW(double BXLDOWN,int L,String type){return BXLDOWN/BXLDOWN_SP(L,type);}
	public double BXLUP2BXLW(double BXLUP,int L,String type){return BXLUP/BXLDOWN_SP(L,type)*(2*currentBranch.jf+1)/(2*currentBranch.ji+1);}
	
	
	/*
	//Recommended Upper Limit of BXLW, from ENSDF manual
	//                           A=6-44    45-150   >150 
	//Isoscalar
	private final double[] RULE1IS={0.003,    0,    0};
	private final double[] RULE2IS={  100,  300, 1000};
	private final double[] RULE3IS={  100,  100,  100};
	private final double[] RULE4IS={  100,  100,    0};
	private final double[] RULM1IS={ 0.03,    0,    0};
	private final double[] RULM2IS={  0.1,    0,    0};
	private final double[] RULM3IS={    0,    0,    0};
	private final double[] RULM4IS={    0,   30,    10};
	
	//Isovector
	private final double[] RULE1IV={  0.3, 0.01, 0.01};
	private final double[] RULE2IV={   10,    0,    0};
	private final double[] RULE3IV={    0,    0,    0};
	private final double[] RULE4IV={    0,    0,    0};
	private final double[] RULM1IV={   10,    3,    2};
	private final double[] RULM2IV={    3,    1,    1};
	private final double[] RULM3IV={   10,   10,   10};
	private final double[] RULM4IV={    0,    0,    0};
	 */
	public static float[] RULs(int A,String type){
		float[] out=new float[2];
		out[0]=0;
		out[1]=0;
		
		String s=type.trim().toUpperCase();
		if(A<6 || s.length()!=2 || (s.charAt(0)!='E'&&s.charAt(0)!='M') || !Character.isDigit(s.charAt(1)))
			return out;
		
		char c=s.charAt(0);
		int L=Integer.parseInt(s.charAt(1)+"");
		if(L>4 || L==0)
			return out;		
		
		//RULIS[i][j] and RULIV[i][j]:
		//i=0-3: for E1,E2,E3,E4
		//i=4-7: for M1,M2,M3,M4
		//j=0,1,2: for A=6-44, 45-150, >150	
		int i=L-1,j=0;
	    if(c=='M')
	    	i=i+4;
	    
	    float otherIS=0,otherIV=0;
		if(A<=44){
			j=0;
			if(A>=21 && i==0)//E1(IV)
				otherIV=0.1f;
		}else if(A<=150){
			j=1;
			if(A>=90 && i==3)//E4(IS)
				otherIS=30f;
		}else
			j=2;
		
		out[0]=(float)RULIS[i][j];
		out[1]=(float)RULIV[i][j];
		
		if(otherIS>0)
			out[0]=otherIS;
		if(otherIV>0)
			out[1]=otherIV;
				
		return out;		
	}
	
	public static float RUL(int A,String type){
		float[] out=RULs(A,type);
		float is=out[0];
		float iv=out[1];
		if(is>iv)
			return is;
		else
			return iv;
	}


}
