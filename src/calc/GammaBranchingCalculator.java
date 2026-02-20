package calc;

import java.util.Vector;

public class GammaBranchingCalculator {

	private int z;
	private double mass,t,dtu,dtl;
	
	//if false, the external BrIcc program will be used to calculate cc in fly
	private boolean isUseInputCC=true;

	private boolean isOnlyCalculateLargeCC=true;//only re-calculate large CC (>0.01) if true,
                                                //otherwise, re-calculate all,
                                                //when is UseInputCC is set to be false (use CC from Bricc)

	private float largeCCLimit=0.01f;

	private Vector<GammaBranch> branches=new Vector<GammaBranch>();
	
	public GammaBranchingCalculator(){
		reset();
	}
		
	
	public void setIsUseInputCC(boolean b){isUseInputCC=b;}
	public void setIsOnlyCalculateLargeCC(boolean b){isOnlyCalculateLargeCC=b;}
	public void setLargeCCLimit(float f){largeCCLimit=f;}
	
	public void setMass(double mass){this.mass=mass;}
	public void setZ(int z){this.z=z;}
	public void setT12(double t,double dt){this.t=t;this.dtu=dt;this.dtl=dt;}
	public void setT12(double t,double dtu,double dtl){this.t=t;this.dtu=dtu;this.dtl=dtl;}

	public int Z(){return z;}
	public double mass(){return mass;}
	public double t12(){return t;}
	public double dtu(){return dtu;}
	public double dtl(){return dtl;}
	
	public boolean isUseInputCC(){return isUseInputCC;}
	public boolean isOnlyCalculateLargeCC(){return isOnlyCalculateLargeCC;}
	
	public Vector<GammaBranch> branchsV(){return branches;}
	public int nBranches(){return branches.size();}
	public GammaBranch branchAt(int i){return branches.get(i);}
	public BXLWResult getBXLW(int nb,String EM){return branches.get(nb).getBXLW(EM);}
	
	
	public void addBranch(double e,double de){

		GammaBranch gb=new GammaBranch();	
		gb.setEnergy(e,de);
		branches.add(gb);
	}
	
	public void addBranch(double e,double de,double ri,double driu,double dril,
			String mult,double mr,double dmru,double dmrl,double cc,double dccu,double dccl){
		GammaBranch gb=new GammaBranch();
		gb.setEnergy(e,de);
		gb.setIntensity(ri,driu,dril);
		gb.setMixingRatio(mr,dmru,dmrl);
		gb.setConversionCoeff(cc,dccu,dccl);
		gb.setMultipolarity(mult);
		
		//debug
		//System.out.println(" eg="+e+" gb.mr="+gb.mr+" mr="+mr+" dmru="+dmru+" dmrl="+dmrl);
		
		branches.add(gb);
	}
	
	public void reset(){
		setT12(0,0);
		
		largeCCLimit=0.01f;
		
		isUseInputCC=true;
		isOnlyCalculateLargeCC=true;
		
		branches.clear();
	}
	
	
	public float sumRI(){
		float out=0;
		for(int i=0;i<branches.size();i++){
			GammaBranch b=branches.get(i);
			if(b.ri<=0)
				continue;
			
			out+=b.ri;
		}
		
		return out;
	}
	
	public float sumTI(){
		float out=0;
		for(int i=0;i<branches.size();i++){
			GammaBranch b=branches.get(i);
			if(b.ri<=0)
				continue;
		
			double cc=b.cc;
			if(cc<0)
				cc=0;
			
			out+=b.ri*(1.0f+cc);
		}
		
		return out;
	}
	
	public void calculateBranchingsFromRI_new(){
		int nb=branches.size();
		double minSum=0;
		double maxSum=0;
		double sum=0;
		
		//used for relative uncertainties for normal error propagation
		double dbruSum=0,dbrlSum=0;
		double[] dbrus=new double[nb];
		double[] dbrls=new double[nb];
		
		//relative uncertainties from normal error propagation
        double rel_driu=0,rel_dril=0,rel_dbru=0,rel_dbrl=0;
        double rel_dtiu=0,rel_dtil=0,rel_dtbru=0,rel_dtbrl=0;
        double rel_drku=0,rel_drkl=0,rel_dccku=0,rel_dcckl=0;
        double rel_dtu=0,rel_dtl=0;
        
		double[] minTIs=new double[nb];
		double[] maxTIs=new double[nb];
		double[] TIs=new double[nb];
		double[] minCCs=new double[nb];
		double[] maxCCs=new double[nb];
		
		double[] ccs=new double[nb];
		double[] dccus=new double[nb];
		double[] dccls=new double[nb];
		if(t>0){
			rel_dtu=dtu/t;
			rel_dtl=dtl/t;
		}
		
		for(int i=0;i<nb;i++){
			GammaBranch b=branches.get(i);
			double cc=b.cc;
	        double dccu=b.dccu;
	        double dccl=b.dccl;
	        
			if(!isUseInputCC){
				if(!isOnlyCalculateLargeCC || cc<0 || cc>largeCCLimit){
			        b.calcICC=Util.icc(z,b.e,b.deu, b.mult,b.mr,b.dmru,b.dmrl);
					cc=b.calcICC.x;
					dccu=b.calcICC.dxu;
					dccl=b.calcICC.dxl;
				}
			}
	        
			if(cc<0) cc=0;
			if(dccu<0) dccu=0;
			if(dccl<0) dccl=0;
			
			ccs[i]=cc;
			dccus[i]=dccu;
			dccls[i]=dccl;
			
			TIs[i]=0;
            minTIs[i]=0;
            maxTIs[i]=0;
			if(b.ri>0){
				TIs[i]=b.ri*(1.0+cc);
				minTIs[i]=(b.ri-b.dril)*(1.0+cc-dccl);
				maxTIs[i]=(b.ri+b.driu)*(1.0+cc+dccu);
				
				sum+=TIs[i];
				minSum+=minTIs[i];
				maxSum+=maxTIs[i];	
				
				rel_drku=b.driu/b.ri;
				rel_drkl=b.dril/b.ri;
				if(cc>0){
					rel_dccku=dccu/cc;
					rel_dcckl=dccl/cc;
				}else{
					rel_dccku=0;
					rel_dcckl=0;
				}
				
				//for normal error propagation
				dbrus[i]=Math.pow(b.ri*(1+cc)*rel_drkl,2)+Math.pow(b.ri*cc*rel_dcckl,2);
				dbruSum+=dbrus[i];
	
				dbrls[i]=Math.pow(b.ri*(1+cc)*rel_drku,2)+Math.pow(b.ri*cc*rel_dccku,2);
				dbrlSum+=dbrls[i];
			}else if(b.ti>0){
				TIs[i]=b.ti;
				minTIs[i]=b.ti-b.dtil;
				maxTIs[i]=b.ti+b.dtiu;
				
				sum+=TIs[i];
				minSum+=minTIs[i];
				maxSum+=maxTIs[i];
				
				//for normal error propagation
				dbrus[i]=b.dtil*b.dtil;
				dbruSum+=dbrus[i];		
				
				dbrls[i]=b.dtiu*b.dtiu;
				dbrlSum+=dbrls[i];
			}
			
			minCCs[i]=cc-dccl;
			maxCCs[i]=cc+dccu;
			
			//if(Math.abs(b.e-415)<1)
			//System.out.println("i="+i+" eg="+b.e+" ri="+b.ri+"  "+b.driu+"  "+b.dril+" cc="+cc+" dccl="+dccl+" dccu="+dccu+" minSum"+minSum+" maxSum"+maxSum);
		}
		
		/*
		 * bri=ri/sum[rk*(1+cck)],k=1-n,          it increases as ri increases and as rk (k!=i) and cck (k=1-n) decreases
		 * tbri=ri*(1+cci)/sum[rk*(1+cck], k=1-n, it increases as ri and cci increases and as rk (k!=i) and cck (k!=i) decreases 
		 * pti=t/bri
		 */
		if(sum>0 && minSum>0 && maxSum>0){
			for(int i=0;i<nb;i++){
				GammaBranch b=branches.get(i);
				if(b.ri<=0)
					continue;
				
				double min=minTIs[i];
				double max=maxTIs[i];
				
				b.tbr=TIs[i]/sum*100;
				b.dtbrl_ex=b.tbr-min/(min+maxSum-max)*100;
				b.dtbru_ex=max/(max+minSum-min)*100-b.tbr;	
				if(b.dtbrl_ex<0) b.dtbrl_ex=0;
				if(b.dtbru_ex<0) b.dtbru_ex=0;
				
				double min1=(b.ri-b.dril)*(1.0+maxCCs[i]);
				double max1=(b.ri+b.driu)*(1.0+minCCs[i]);
				
				b.br=b.ri/sum*100;				
				b.dbrl_ex=b.br-(b.ri-b.dril)/(min1+maxSum-max)*100;
				b.dbru_ex=(b.ri+b.driu)/(max1+minSum-min)*100-b.br;	
				if(b.dbrl_ex<0) b.dbrl_ex=0;
				if(b.dbru_ex<0) b.dbru_ex=0;
				
				b.pt=t/b.br*100;
				b.dptl_ex=b.pt-(t-dtl)/(b.br+b.dbru_ex)*100;
				if(b.br>b.dbrl_ex)
					b.dptu_ex=(t+dtu)/(b.br-b.dbrl_ex)*100-b.pt;
				else
					b.dptu_ex=1000000;
				
				
				//get uncertainty from normal error propagation
				double cc=ccs[i];
				double dccu=dccus[i];
				double dccl=dccls[i];
				
				rel_dccku=dccu/cc;
				rel_dcckl=dccl/cc;
				
				if(b.br>0){					
					rel_driu=b.driu/b.ri;
					rel_dril=b.dril/b.ri;
					
					rel_dbru=dbruSum-dbrus[i]+Math.pow(sum-b.ri*(1+cc),2)*rel_driu*rel_driu+Math.pow(b.ri*cc*rel_dcckl,2);
					rel_dbru=Math.sqrt(rel_dbru)/sum;
					b.dbru_ep=rel_dbru*b.br;
					
					rel_dbrl=dbrlSum-dbrls[i]+Math.pow(sum-b.ri*(1+cc),2)*rel_dril*rel_dril+Math.pow(b.ri*cc*rel_dccku,2);
					rel_dbrl=Math.sqrt(rel_dbrl)/sum;
					b.dbrl_ep=rel_dbrl*b.br;
					
					
					rel_dtbru=dbruSum-dbrus[i]+Math.pow(sum-b.ri*(1+cc),2)*(rel_driu*rel_driu+Math.pow(dccu/(1+cc),2));
					rel_dtbru=Math.sqrt(rel_dtbru)/sum;
					b.dtbru_ep=rel_dtbru*b.tbr;
					
					rel_dtbrl=dbrlSum-dbrls[i]+Math.pow(sum-b.ri*(1+cc),2)*(rel_dril*rel_dril+Math.pow(dccl/(1+cc),2));
					rel_dtbrl=Math.sqrt(rel_dtbrl)/sum;
					b.dtbrl_ep=rel_dtbrl*b.tbr;
					
					b.dptu_ep=Math.sqrt(rel_dtu*rel_dtu+rel_dbrl*rel_dbrl);
					b.dptl_ep=Math.sqrt(rel_dtl*rel_dtl+rel_dbru*rel_dbru);

				}else if(b.ti>0){
					rel_dtiu=b.dtiu/b.ti;
					rel_dtil=b.dtil/b.ti;
					
					rel_dtbru=dbruSum-dbrus[i]+Math.pow(sum-b.ti,2)*rel_dtiu*rel_dtiu;
					rel_dtbru=Math.sqrt(rel_dtbru)/sum;
					b.dtbru_ep=rel_dtbru*b.tbr;
					
					rel_dtbrl=dbrlSum-dbrls[i]+Math.pow(sum-b.ti,2)*rel_dtil*rel_dtil;
					rel_dtbrl=Math.sqrt(rel_dtbrl)/sum;
					b.dtbrl_ep=rel_dtbrl*b.tbr;
					
					rel_dbru=rel_dtbru*rel_dtbru+Math.pow(dccl/(1+cc),2);
					rel_dbru=Math.sqrt(rel_dbru)/sum;
					b.dbru_ep=rel_dbru*b.br;
					
					rel_dbrl=rel_dtbrl*rel_dtbrl+Math.pow(dccu/(1+cc),2);
					rel_dbrl=Math.sqrt(rel_dbrl)/sum;
					b.dbrl_ep=rel_dbrl*b.br;
					
					b.dptu_ep=Math.sqrt(rel_dtu*rel_dtu+rel_dbrl*rel_dbrl);
					b.dptl_ep=Math.sqrt(rel_dtl*rel_dtl+rel_dbru*rel_dbru);
				}	
				
				//if(Math.abs(b.e-32.5)<1){
					//System.out.println("***t="+t+" pt="+b.pt+"  br="+b.br+"  "+b.dbru_ep+"  "+b.dbrl_ep+" tbr="+b.tbr+"  "+b.dtbru_ep+"  "+b.dtbrl_ep+" min"+min+" max"+max+" minSum"+minSum+" maxSum"+maxSum);
				    //System.out.println("sum="+sum+" b.ri*(1+cc)="+b.ri*(1+cc)+" dbrlSum="+dbrlSum+"  dbrls[i]="+dbrls[i]);
					//System.out.println("  ri="+b.ri+" "+b.driu+" "+b.dril+" ti="+TIs[i]+" "+max1+" "+min1);
				//}
			}
		}	
	}
	
	/*
	 * called after calculateAll() in order to use updated CC values
	 */
	public void calculateBranchingsFromRI(){
		int nb=branches.size();
		double minSum=0;
		double maxSum=0;
		double sum=0;
		
		//used for relative uncertainties for normal error propagation
		double dbruSum=0,dbrlSum=0;
		double[] dbrus=new double[nb];
		double[] dbrls=new double[nb];
		
		//relative uncertainties from normal error propagation
        double rel_driu=0,rel_dril=0,rel_dbru=0,rel_dbrl=0;
        double rel_dtiu=0,rel_dtil=0,rel_dtbru=0,rel_dtbrl=0;
        double rel_drku=0,rel_drkl=0,rel_dccku=0,rel_dcckl=0;
        double rel_dtu=0,rel_dtl=0;
        
		double[] minTIs=new double[nb];
		double[] maxTIs=new double[nb];
		double[] TIs=new double[nb];
		double[] minCCs=new double[nb];
		double[] maxCCs=new double[nb];
		
		if(t>0){
			rel_dtu=dtu/t;
			rel_dtl=dtl/t;
		}
		
		for(int i=0;i<nb;i++){
			GammaBranch b=branches.get(i);
			double cc=b.cc;
	        double dccu=b.dccu;
	        double dccl=b.dccl;
	        if(!isUseInputCC && b.calcICC!=null){
	        	cc=b.calcICC.x;
	        	dccu=b.calcICC.dxu;
	        	dccl=b.calcICC.dxl;
	        }
	        
			if(cc<0) cc=0;
			if(dccu<0) dccu=0;
			if(dccl<0) dccl=0;
			
			TIs[i]=0;
            minTIs[i]=0;
            maxTIs[i]=0;
			if(b.ri>0){
				TIs[i]=b.ri*(1.0+cc);
				minTIs[i]=(b.ri-b.dril)*(1.0+cc-dccl);
				maxTIs[i]=(b.ri+b.driu)*(1.0+cc+dccu);
				
				sum+=TIs[i];
				minSum+=minTIs[i];
				maxSum+=maxTIs[i];	
				
				rel_drku=b.driu/b.ri;
				rel_drkl=b.dril/b.ri;
				if(cc>0){
					rel_dccku=dccu/cc;
					rel_dcckl=dccl/cc;
				}else{
					rel_dccku=0;
					rel_dcckl=0;
				}
				
				//for normal error propagation
				dbrus[i]=Math.pow(b.ri*(1+cc)*rel_drkl,2)+Math.pow(b.ri*cc*rel_dcckl,2);
				dbruSum+=dbrus[i];
	
				dbrls[i]=Math.pow(b.ri*(1+cc)*rel_drku,2)+Math.pow(b.ri*cc*rel_dccku,2);
				dbrlSum+=dbrls[i];
			}else if(b.ti>0){
				TIs[i]=b.ti;
				minTIs[i]=b.ti-b.dtil;
				maxTIs[i]=b.ti+b.dtiu;
				
				sum+=TIs[i];
				minSum+=minTIs[i];
				maxSum+=maxTIs[i];
				
				//for normal error propagation
				dbrus[i]=b.dtil*b.dtil;
				dbruSum+=dbrus[i];		
				
				dbrls[i]=b.dtiu*b.dtiu;
				dbrlSum+=dbrls[i];
			}
			
			minCCs[i]=cc-dccl;
			maxCCs[i]=cc+dccu;
			
			//if(Math.abs(b.e-415)<1)
			//System.out.println("i="+i+" eg="+b.e+" ri="+b.ri+"  "+b.driu+"  "+b.dril+" cc="+cc+" dccl="+dccl+" dccu="+dccu+" minSum"+minSum+" maxSum"+maxSum);
		}
		
		/*
		 * bri=ri/sum[rk*(1+cck)],k=1-n,          it increases as ri increases and as rk (k!=i) and cck (k=1-n) decreases
		 * tbri=ri*(1+cci)/sum[rk*(1+cck], k=1-n, it increases as ri and cci increases and as rk (k!=i) and cck (k!=i) decreases 
		 * pti=t/bri
		 */
		if(sum>0 && minSum>0 && maxSum>0){
			for(int i=0;i<nb;i++){
				GammaBranch b=branches.get(i);
				if(b.ri<=0)
					continue;
				
				double min=minTIs[i];
				double max=maxTIs[i];
				
				b.tbr=TIs[i]/sum*100;
				b.dtbrl_ex=b.tbr-min/(min+maxSum-max)*100;
				b.dtbru_ex=max/(max+minSum-min)*100-b.tbr;	
				if(b.dtbrl_ex<0) b.dtbrl_ex=0;
				if(b.dtbru_ex<0) b.dtbru_ex=0;
				
				double min1=(b.ri-b.dril)*(1.0+maxCCs[i]);
				double max1=(b.ri+b.driu)*(1.0+minCCs[i]);
				
				b.br=b.ri/sum*100;				
				b.dbrl_ex=b.br-(b.ri-b.dril)/(min1+maxSum-max)*100;
				b.dbru_ex=(b.ri+b.driu)/(max1+minSum-min)*100-b.br;	
				if(b.dbrl_ex<0) b.dbrl_ex=0;
				if(b.dbru_ex<0) b.dbru_ex=0;
				
				b.pt=t/b.br*100;
				b.dptl_ex=b.pt-(t-dtl)/(b.br+b.dbru_ex)*100;
				if(b.br>b.dbrl_ex)
					b.dptu_ex=(t+dtu)/(b.br-b.dbrl_ex)*100-b.pt;
				else
					b.dptu_ex=1000000;
				
				
				//get uncertainty from normal error propagation
				double cc=b.cc;
				double dccu=b.dccu;
				double dccl=b.dccl;
				if(!isUseInputCC && b.calcICC!=null){
		        	cc=b.calcICC.x;			
		        	dccu=b.calcICC.dxu;
		        	dccl=b.calcICC.dxl;
				}
				
				if(cc<0) cc=0;
				if(dccu<0) dccu=0;
				if(dccl<0) dccl=0;
				
				if(b.br>0){					
					rel_driu=b.driu/b.ri;
					rel_dril=b.dril/b.ri;
					
					rel_dbru=dbruSum-dbrus[i]+Math.pow(sum-b.ri*(1+cc),2)*rel_driu*rel_driu;
					rel_dbru=Math.sqrt(rel_dbru)/sum;
					b.dbru_ep=rel_dbru*b.br;
					
					rel_dbrl=dbrlSum-dbrls[i]+Math.pow(sum-b.ri*(1+cc),2)*rel_dril*rel_dril;
					rel_dbrl=Math.sqrt(rel_dbrl)/sum;
					b.dbrl_ep=rel_dbrl*b.br;
					
					
					rel_dtbru=dbruSum-dbrus[i]+Math.pow(sum-b.ri*(1+cc),2)*(rel_driu*rel_driu+Math.pow(dccu/(1+cc),2));
					rel_dtbru=Math.sqrt(rel_dtbru)/sum;
					b.dtbru_ep=rel_dtbru*b.tbr;
					
					rel_dtbrl=dbrlSum-dbrls[i]+Math.pow(sum-b.ri*(1+cc),2)*(rel_dril*rel_dril+Math.pow(dccl/(1+cc),2));
					rel_dtbrl=Math.sqrt(rel_dtbrl)/sum;
					b.dtbrl_ep=rel_dtbrl*b.tbr;
					
					b.dptu_ep=Math.sqrt(rel_dtu*rel_dtu+rel_dbrl*rel_dbrl);
					b.dptl_ep=Math.sqrt(rel_dtl*rel_dtl+rel_dbru*rel_dbru);

				}else if(b.ti>0){
					rel_dtiu=b.dtiu/b.ti;
					rel_dtil=b.dtil/b.ti;
					
					rel_dtbru=dbruSum-dbrus[i]+Math.pow(sum-b.ti,2)*rel_dtiu*rel_dtiu;
					rel_dtbru=Math.sqrt(rel_dtbru)/sum;
					b.dtbru_ep=rel_dtbru*b.tbr;
					
					rel_dtbrl=dbrlSum-dbrls[i]+Math.pow(sum-b.ti,2)*rel_dtil*rel_dtil;
					rel_dtbrl=Math.sqrt(rel_dtbrl)/sum;
					b.dtbrl_ep=rel_dtbrl*b.tbr;
					
					rel_dbru=rel_dtbru*rel_dtbru+Math.pow(dccl/(1+cc),2);
					rel_dbru=Math.sqrt(rel_dbru)/sum;
					b.dbru_ep=rel_dbru*b.br;
					
					rel_dbrl=rel_dtbrl*rel_dtbrl+Math.pow(dccu/(1+cc),2);
					rel_dbrl=Math.sqrt(rel_dbrl)/sum;
					b.dbrl_ep=rel_dbrl*b.br;
					
					b.dptu_ep=Math.sqrt(rel_dtu*rel_dtu+rel_dbrl*rel_dbrl);
					b.dptl_ep=Math.sqrt(rel_dtl*rel_dtl+rel_dbru*rel_dbru);
				}	
				//if(Math.abs(b.e-415)<1){
				//	System.out.println("***t="+t+" pt="+b.pt+"  br="+b.br+"  "+b.dbru+"  "+b.dbrl+" tbr="+b.tbr+"  "+b.dtbru+"  "+b.dtbrl+" min"+min+" max"+max+" minSum"+minSum+" maxSum"+maxSum);
				//	System.out.println("  ri="+b.ri+" "+b.driu+" "+b.dril+" ti="+TIs[i]+" "+max1+" "+min1);
				//}
			}
		}	
	}
	
	public void calculateBranchingsFromTI(){
		int nb=branches.size();
		double minSum=0;
		double maxSum=0;
		double sum=0;
		
		//used for relative uncertainties for normal error propagation
		double dbrSum=0;
		double[] dbrs=new double[nb];
		
		//relative uncertainties from normal error propagation
        double rel_dri=0,rel_dbr=0;
        
		double[] minTIs=new double[nb];
		double[] maxTIs=new double[nb];
		double[] TIs=new double[nb];
		double[] minCCs=new double[nb];
		double[] maxCCs=new double[nb];
		
        boolean hasAsymmetricUnc=false;
        
		for(int i=0;i<nb;i++){
			GammaBranch b=branches.get(i);
			double cc=b.cc;
	        double dccu=b.dccu;
	        double dccl=b.dccl;
	        if(!isUseInputCC && b.calcICC!=null){
	        	cc=b.calcICC.x;
	        	dccu=b.calcICC.dxu;
	        	dccl=b.calcICC.dxl;
	        }
	        
			if(cc<0) cc=0;
			if(dccu<0) dccu=0;
			if(dccl<0) dccl=0;

			if(!hasAsymmetricUnc && b.dtiu!=b.dtil)
				hasAsymmetricUnc=true;
			
			TIs[i]=0;
            minTIs[i]=0;
            maxTIs[i]=0;
			if(b.ti>0){
				b.ri=b.ti/(1.0+cc);
				b.driu=(b.ti+b.dtiu)/(1.0+cc-dccl)-b.ri;
				b.dril=b.ri-(b.ti-b.dtil)/(1.0+cc+dccu);
				
				if(b.driu<0) b.driu=0;
				if(b.dril<0) b.dril=0;
				
				TIs[i]=b.ti;
				minTIs[i]=b.ti-b.dtil;
				maxTIs[i]=b.ti+b.dtiu;
				
				sum+=TIs[i];
				minSum+=minTIs[i];
				maxSum+=maxTIs[i];	
				
				if(!hasAsymmetricUnc){
					dbrs[i]=b.dtiu*b.dtiu;
					dbrSum+=dbrs[i];
				}
			}
			
			minCCs[i]=cc-dccl;
			maxCCs[i]=cc+dccu;
			
			//System.out.println("i="+i+" eg="+b.e+" ri="+b.ri+"  "+b.driu+"  "+b.dril+" cc="+cc+" dccl="+dccl+" dccu="+dccu+" minSum"+minSum+" maxSum"+maxSum);
		}
		
		/*
		 * bri=ri/sum[rk*(1+cck)],k=1-n,          it increases as ri increases and as rk (k!=i) and cck (k=1-n) decreases
		 * tbri=ri*(1+cci)/sum[rk*(1+cck], k=1-n, it increases as ri and cci increases and as rk (k!=i) and cck (k!=i) decreases 
		 * pti=t/bri
		 */
		if(sum>0 && minSum>0 && maxSum>0){
			for(int i=0;i<nb;i++){
				GammaBranch b=branches.get(i);
				if(b.ri<=0)
					continue;
				
				double min=minTIs[i];
				double max=maxTIs[i];
				
				b.tbr=TIs[i]/sum*100;
				b.dtbrl_ex=b.tbr-min/(min+maxSum-max)*100;
				b.dtbru_ex=max/(max+minSum-min)*100-b.tbr;	
				if(b.dtbrl_ex<0) b.dtbrl_ex=0;
				if(b.dtbru_ex<0) b.dtbru_ex=0;
				
				double min1=(b.ri-b.dbrl_ex)*(1.0+maxCCs[i]);
				double max1=(b.ri+b.dbru_ex)*(1.0+minCCs[i]);
				
				b.br=b.ri/sum*100;				
				b.dbrl_ex=b.br-(b.ri-b.dril)/(min1+maxSum-max)*100;
				b.dbru_ex=(b.ri+b.driu)/(max1+minSum-min)*100-b.br;	
				if(b.dbrl_ex<0) b.dbrl_ex=0;
				if(b.dbru_ex<0) b.dbru_ex=0;
				
				b.pt=t/b.br;
				b.dptl_ex=b.pt-(t-dtl)/(b.br+b.dbru_ex);
				if(b.br>b.dbrl_ex)
					b.dptu_ex=(t+dtu)/(b.br-b.dbrl_ex)-b.pt;
				else
					b.dptu_ex=1000000;
				
				if(!hasAsymmetricUnc){
					double cc=b.cc;
			        double dccu=b.dccu;
			        double dccl=b.dccl;
			        if(!isUseInputCC && b.calcICC!=null){
			        	cc=b.calcICC.x;
			        	dccu=b.calcICC.dxu;
			        	dccl=b.calcICC.dxl;
			        }
					
					if(cc<0) cc=0;
					if(dccu<0) dccu=0;
					if(dccl<0) dccl=0;
					
					if(b.ti>0){
						rel_dri=b.dtiu/b.ti;
						rel_dbr+=dbrSum-dbrs[i]+Math.pow(sum-b.ti,2)*rel_dri*rel_dri;
						rel_dbr=Math.sqrt(rel_dbr)/sum;
						b.dtbru_ep=rel_dbr*b.tbr;
						
						if(dccu==dccl){
							rel_dbr=rel_dbr*rel_dbr+Math.pow(dccu/(1+cc),2);
							rel_dbr=Math.sqrt(rel_dbr)/sum;
							b.dbru_ep=rel_dbr*b.br;
						}
					}										
				}
				//System.out.println(b.br+"  "+b.dbru+"  "+b.dbrl+" min"+min+" max"+max+" minSum"+minSum+" maxSum"+maxSum);
			}
		}	
	}
}
