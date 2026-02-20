package calc;

import java.util.HashMap;
import java.util.Vector;

class GammaBranch{

    //input variables
	public double e,deu,del,ri,driu,dril,mr,dmru,dmrl,cc,dccu,dccl,ti,dtiu,dtil;//deu for upper de, ded for lower de

	//deduced variables
	public double br,dbru_ep,dbrl_ep,dbru_ex,dbrl_ex;//percentage branching for gamma, set in calculateBranchings(): 
	                                                 //dbru_ep,dbrl_ep from normal error propagation,
	                                                 //dbru_ex & dbrl_ex from extremes (min and max) of each variable 
	public double tbr,dtbru_ep,dtbrl_ep,dtbru_ex,dtbrl_ex;//percentage branching for (gamma+ce), set in calculateBranchings()
	public double pt,dptu_ep,dptl_ep,dptu_ex,dptl_ex;//partial T1/2, set in calculateBranchings()
	
	
	public double ji,jf;//ji is spin of parent level, jf for daughter level
	public String mult;
	public String[] EMs;
			
	public XDX calcICC;//store calculated cc values using briccs
	
	public HashMap<String,BXLWResult> BXLWmap=new HashMap<String,BXLWResult>();
	
	public GammaBranch(){
		reset();
	}
	
	public void setJiJf(double ji,double jf){this.ji=Math.abs(ji);this.jf=Math.abs(jf);}
	public void setJi(double ji){this.ji=Math.abs(ji);}
	public void setJf(double jf){this.jf=Math.abs(jf);}
		
	public void setEnergy(double e,double de){this.e=e;this.deu=de;this.del=de;}
	public void setIntensity(double ri,double dri){this.ri=ri;this.driu=dri;this.dril=dri;}
	public void setBranching(double br,double dbr){this.br=br;this.dbru_ex=dbr;this.dbrl_ex=dbr;}
	public void setTotalBranching(double tbr,double dtbr){this.tbr=tbr;this.dtbru_ex=dtbr;this.dtbrl_ex=dtbr;}
	public void setTotalIntensity(double ti,double dti){this.ti=ti;this.dtiu=dti;this.dtil=dti;}
	public void setMixingRatio(double mr,double dmr){this.mr=mr;this.dmru=dmr;this.dmrl=dmr;}
	public void setConversionCoeff(double cc,double dcc){this.cc=cc;this.dccu=dcc;this.dccl=dcc;}
	public void setMultipolarity(String mult){
		this.mult=mult.toUpperCase().trim();
		EMs=parseMUL(this.mult);
	}
	
	public void setEnergy(double e,double deu,double del){this.e=e;this.deu=deu;this.del=del;}
	public void setIntensity(double ri,double driu,double dril){this.ri=ri;this.driu=driu;this.dril=dril;}
	public void setBranching(double br,double dbru,double dbrl){this.br=br;this.dbru_ex=dbru;this.dbrl_ex=dbrl;}
	public void setTotalBranching(double tbr,double dtbru,double dtbrl){this.tbr=tbr;this.dtbru_ex=dtbru;this.dtbrl_ex=dtbrl;}
	public void setTotalIntensity(double ti,double dtiu,double dtil){this.ti=ti;this.dtiu=dtiu;this.dtil=dtil;}
	public void setMixingRatio(double mr,double dmru,double dmrl){this.mr=mr;this.dmru=dmru;this.dmrl=dmrl;}
	public void setConversionCoeff(double cc,double dccu,double dccl){this.cc=cc;this.dccu=dccu;this.dccl=dccl;}
	public void setCalcICC(XDX icc){calcICC=icc;}
	
	public double e(){return e;}
	public double deu(){return deu;}
	public double del(){return del;}
	public double ji(){return ji;}
	public double jf(){return jf;}		
	public double ri(){return ri;}
	public double driu(){return driu;}
	public double dril(){return dril;}
	public double ti(){return ti;}
	public double dtiu(){return dtiu;}
	public double dtil(){return dtil;}
	public double mr(){return mr;}
	public double dmru(){return dmru;}
	public double dmrl(){return dmrl;}
	public double cc(){return cc;}
	public double dccu(){return dccu;}
	public double dccl(){return dccl;} 
	public String mult(){return mult;}
	public double br(){return br;}
	public double dbr(){return dbru_ep;}
	public double dbru(){return dbru_ex;}
	public double dbrl(){return dbrl_ex;}
	public double tbr(){return tbr;}
	public double dtbr(){return dtbru_ep;}
	public double dtbru(){return dtbru_ex;}
	public double dtbrl(){return dtbrl_ex;}
	public double pt(){return pt;}
	public double dpt(){return dptu_ep;}
	public double dptu(){return dptu_ex;}
	public double dptl(){return dptl_ex;}
	
	public HashMap<String,BXLWResult> getBXLWmap(){return BXLWmap;}
	public BXLWResult getBXLW(String EM){return BXLWmap.get(EM.toUpperCase().trim());}
	public String[] getEMs(){return EMs;}
	
	public XDX getCalcICC(){
		return calcICC;
	}
	
	public void reset(){	
		calcICC=null;
		br=0;dbru_ep=0;dbru_ex=0;dbrl_ex=0;
		tbr=0;dtbru_ep=0;dtbru_ex=0;dtbrl_ex=0;
		pt=0;dptu_ep=0;dptu_ex=0;dptl_ex=0;
		
		setEnergy(0,0);
		setIntensity(0,0);
		setTotalIntensity(0,0);
		setMixingRatio(0,0);
		setConversionCoeff(0,0);
		setJiJf(0,0);
		setMultipolarity("");

	}
	
	
	public String[] parseMUL(String ms){
		String[] mv=new String[0];
		String s=ms.toUpperCase().trim();
        if(s.isEmpty())
           return mv;

        s=s.replace("(","").replace(")","").trim();
        s=s.replace("[","").replace("]","").trim();
        
        mv=s.split("[,/+]+");
        
        int[] numbers=new int[mv.length];
        Vector<String> tempV=new Vector<String>();
        for(int i=0;i<mv.length;i++){
        	if(mv[i].trim().length()>0)
        		tempV.add(mv[i].trim());
        }
        
        if(tempV.size()==0)
        	return new String[0];
        
        mv=new String[tempV.size()];
        tempV.toArray(mv);
        
        for(int i=0;i<tempV.size();i++){
        	numbers[i]=100;
        	if(mv[i].trim().length()>0){
        		s=mv[i].trim();
        		if(s.equals("D"))
        			numbers[i]=1;
        		else if(s.equals("Q"))
        			numbers[i]=2;
        		else if(s.equals("O"))
        			numbers[i]=3;
        		else{
        			try{
        				int len=s.length();
        				numbers[i]=Integer.parseInt(s.substring(len-1));
        			}catch(NumberFormatException e){}
        		}
        	}
        }
        
		
        //debug
		//for(int i=0;i<mv.length;i++)
		//	System.out.println(" @ i="+i+" "+mv[i]+" size="+mv.length+" ms="+ms);
		        
        for(int i=0;i<mv.length-1;i++){
        	for(int j=i+1;j<mv.length;j++){
        		if(numbers[i]>numbers[j]){
        			int tempNum=numbers[i];
        			numbers[i]=numbers[j];
        			numbers[j]=tempNum;
        			
        			String tempS=mv[i];
        			mv[i]=mv[j];
        			mv[j]=tempS;
        		}
        	}     	    	
        }
        
        //debug
		//for(int i=0;i<mv.length;i++)
		//	System.out.println(" # i="+i+" "+mv[i]+" size="+mv.length+" "+tempV.size());
		
        return mv; 
	}

	public boolean isGoodEM(String singlePol){
		String[] MS={"E","M"};
		String s=singlePol.toUpperCase().trim();
		if(s.length()!=2)
			return false;
		
		for(int i=0;i<MS.length;i++){
			for(int j=1;j<=6;j++){
				if(s.equals(MS[i]+j))
					return true;
			}
		}
		
		return false;
	}
}
