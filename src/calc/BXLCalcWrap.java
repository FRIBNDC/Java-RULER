package calc;

import java.util.HashMap;
import java.util.Vector;



import ensdf.Gamma;
import ensdf.Level;
import ensdf.SDS2XDX;
import ensdf.XDX2SDS;
import nds.util.Str;

/*
 * wrap of BXLCalculator for initializing the BXLCalculator from a level in an ENSDF file and
 * for printing out the calculated results of a gamma branch from the level
 */
public class BXLCalcWrap {
	

	private BXLCalculator BXLCalc;
	private boolean isGoodBriccs=true;
	private boolean isPrintRULComparison=false;
	private boolean isSuppressBXLWLimit=false;//suppress BXLW limit values in new output
	
	private float theoryDCC=0.014f;
	private String theoryDCCType="B";
	
	private int uncertaintyLimitBXL=50;//uncertaint limit for BXL values, note that ENSDF dafault uncertainty limit=25
	private int nProblems=0;
	
	private String warningPrefix=">>>>>>>";
	
	
	private HashMap<Integer,HashMap<String,String>> newBXLWEntryMapsV;;
	
	public BXLCalcWrap(BXLCalculator BXLCalc){				 
		setBXLCalculator(BXLCalc);
	}
	
	public void setBXLCalculator(BXLCalculator BXLCalc){
		nProblems=0;
		newBXLWEntryMapsV=new HashMap<Integer,HashMap<String,String>>();
		
		this.BXLCalc=BXLCalc;
	}
	
	
	public void setIsGoodBriccs(boolean b){isGoodBriccs=b;}
	public void setIsPrintRULComparison(boolean b){isPrintRULComparison=b;}
	public void setIsSuppressBXLWLimit(boolean b){isSuppressBXLWLimit=b;}
	public void setTheoryDCC(float v){theoryDCC=v;}
	public void setTheoryDCCType(String type){theoryDCCType=type;}
    public void setUncertaintyLimit(int limit){uncertaintyLimitBXL=limit;}
    
	public BXLCalculator getBXLCalc(){return BXLCalc;}
	public boolean isGoodBriccs(){return isGoodBriccs;}
	public boolean isPrintRULComparison(){return isPrintRULComparison;}
	public boolean isSuppressBXLWLimit(){return isSuppressBXLWLimit;}
	
	public float getTheoryDCC(){return theoryDCC;}
	public String getTheoryDCCType(){return theoryDCCType;}
	public int getUncertaintyLimit(){return uncertaintyLimitBXL;}
	public int nProblems(){return nProblems;}
	
	public String RULS(int A,String mult){
		String RULs="";
		double RUL=BXLCalculator.RUL(A, mult);
		if(RUL>=1f || RUL==0)
			RULs=String.format("%.0f", RUL);
		else if(RUL>=0.1f)
			RULs=String.format("%.1f", RUL);
		else if(RUL>=0.01f)
			RULs=String.format("%.2f", RUL);
		else
			RULs=String.format("%.3f", RUL);
		
		return RULs;
	}
	
	/*
	 * if x==dxl, set x<(x+dxu)
	 * suppress=true to suppress values of limit in new output
	 */
	public String printEntryBXL(XDX2SDS xs,String name){
		return printEntryBXL(xs,name,false);
	}
	
	public String printEntryBXL(double x,double dxu,double dxl,String name){
		return printEntryBXL(new XDX2SDS(x,dxu,dxl,uncertaintyLimitBXL),name);
	}
	
	public String printEntryBXL(double x,double dxu,double dxl,String name,boolean suppress){
		return printEntryBXL(new XDX2SDS(x,dxu,dxl,uncertaintyLimitBXL),name,suppress);
	}
	
	/*
	 * if x==dxl, set x<(x+dxu)
	 */
	public String printEntryBXL(XDX2SDS xs,String name,boolean suppress){

		String s="";
		String ds=xs.ds().toUpperCase().trim();
		
		double dxu=xs.dxu();
		double dxl=xs.dxl();
		boolean isLimit=false;
		
		String name0=name.trim();
		String name1=name.trim();
		if(!name1.isEmpty())
			name1=name1+"=";
		
		//System.out.println(" s="+xs.s()+" dsu="+xs.dsu()+" dsl="+xs.dsl()+" ds="+xs.ds()+" dxu="+dxu+" dxl="+dxl+" xs.islimit="+xs.isLimits()+
		//		" baseS="+xs.bases()+" exps="+xs.exps()+" basex="+xs.baseX()+" suppress="+suppress);

		if((dxu<0&&dxl<0) || (dxu==0&&dxl==0)){//including xu==xl==0 for f==0		
			if(!Str.isNumeric(ds) && !ds.isEmpty()){
				String temp="LGA";//first letters of LT,LE,GT,GA,AP
				if(temp.indexOf(ds.charAt(0))>=0)
					s=name0+" "+ds+" "+xs.s();
				else
					s=name1+xs.s()+" "+ds;
				
				isLimit=true;
			}else
				s=name1+xs.s();			
		}else if(!xs.isLimits()){
			Integer ix=0,idx=0;
			try{
				ix=Integer.parseInt(xs.baseS().replace(".", ""));
				idx=Integer.parseInt(xs.dsl());
				ix=Math.abs(ix);
				
				//System.out.println(" s="+xs.s()+" dsu="+xs.dsu()+" dsl="+xs.dsl()+" ds="+xs.ds()+" ix="+ix+" idx="+idx+" baseS="+xs.bases()+" exps="+xs.exps()+" basex="+xs.baseX());
				
				if(ix<=idx){//e.g., BXLW=6E-3 6 or 6E-3 +6-7
					if(ds.equals("LE"))
						s=name0+" LE "+xs.su();
					else
						s=name0+" LT "+xs.su();
					
					isLimit=true;
				}else if(xs.dsu().equals(xs.dsl()))
		    		s=name1+xs.s()+" "+xs.ds();
		    	else
		    		s=name1+xs.s()+" +"+xs.dsu()+"-"+xs.dsl();  
			}catch(Exception e){
				s=name1+xs.s();
			}

		}else{//isLimit=true
			if(suppress)
				return "";
			else
				return printEntry(xs,name);
		}
		
		if(suppress && isLimit)
			return "";
		
		return s;
	}
	
	
	public String printEntry(XDX2SDS xs){
		return printEntry(xs,"");
	}
	
	public String printEntry(XDX2SDS xs,String name){

		String ds=xs.ds().toUpperCase().trim();
	
		double dxu=xs.dxu();
		double dxl=xs.dxl();
		double xu=xs.x()+dxu;
		double xl=xs.x()-dxl;
		double f=xu*xl;
		
		String name0=name.trim();
		String name1=name.trim();
		if(!name1.isEmpty())
			name1=name1+"=";
		
		if((dxu<0&&dxl<0) || (dxu==0&&dxl==0)){//including xu==xl==0 for f==0		
			if(!Str.isNumeric(ds) && !ds.isEmpty()){
				String temp="LGA";//first letters of LT,LE,GT,GA,AP
				if(temp.indexOf(ds.charAt(0))>=0)
					return name0+" "+ds+" "+xs.s();
				else
					return name1+" "+ds;
			}else
				return name1+xs.s();			
		}
		
		if(!xs.isLimits()){
			if(xs.dsu().equals(xs.dsl()))
	    		return name1+xs.s()+" "+xs.ds();
	    	else
	    		return name1+xs.s()+" +"+xs.dsu()+"-"+xs.dsl();    	
		}else if(f<0){//not including xu==xl==0 for f==0	
			if(!name0.isEmpty())
				return xs.sl()+"<"+name+"<"+xs.su();	
			else
				return xs.sl()+"<value<"+xs.su();	
		}else if(xl==0){//f==0, xu>0
			if(ds.equals("LE"))
				return name0+" LE "+xs.s();
			else
				return name0+" LT "+xs.s();
		}else if(xu==0){//f==0, xl<0
			if(ds.equals("GE"))
				return name0+" GE "+xs.s();
			else
				return name0+" GT "+xs.s();
		}
		
		//here, xs.isLimit()=true, f>0 (xl!=0 and xu!=0), (dxu==0&&dxl!=0) or (dxu!=0&&dxl==0)
		// dxl < x < dxu
        //Now find out if one side of the limits can be removed.
		
		
		//System.out.println(" printEntry="+x+"  dxu="+dxu+" dxl="+dxl+" s="+xs.dsu()+" "+xs.dsl()+"  *"+xs.ds()+"*"+" xs.isLimits()="+xs.isLimits());
		
		double r=0;
		if(xu>0)//xl>0 also
			r=xu/xl;
		else//xu<0,xl<0
			r=Math.abs(xl/xu);
		
		if(r>1E5){//condition for one side of limit being able to be removed
			//then check which side the x value is closer to
			//note that xs.isLimit()=ture, means dxu and dxl differ significantly
			if(dxu>dxl){//x closer to xl
				if(ds.equals("GE"))
					return name0+" GE "+xs.s();
				else
					return name0+" GT "+xs.s();
			}else{//x closer to xu
				if(ds.equals("LE"))
					return name0+" LE "+xs.s();
				else
					return name0+" LT "+xs.s();
			}
				
		}else{
			if(!name0.isEmpty())
				return xs.sl()+"<"+name+"<"+xs.su();	
			else
				return xs.sl()+"<value<"+xs.su();	
		}		
	}
	
	public String printEntry(double x,double dxu,double dxl,String name){
		return printEntry(new XDX2SDS(x,dxu,dxl),name);		
	}
	
	public String printEntry(double x,double dxu,double dxl){
		return printEntry(x,dxu,dxl,"");
	}
	
	
	public String printEntryAsIs(double x,double dxu,double dxl){
		return printEntryAsIs(x,dxu,dxl,"");
	}
	
	public String printEntryAsIs(double x,double dxu,double dxl,String name){
		return printEntryAsIs(x,dxu,dxl,name,0);//default=25 will be set in XDX2SDS
	}
	public String printEntryAsIs(double x,double dxu,double dxl,String name,int uncertaintyLimit){
	
		String s="";
		XDX2SDS xs=new XDX2SDS(x,dxu,dxl,uncertaintyLimit);
		
		String name0=name.trim();
		String name1=name.trim();
		if(!name1.isEmpty())
			name1=name1+"=";
		
		if((dxu<0&&dxl<0) || (dxu==0&&dxl==0))//including xu==xl==0 for f==0		
			return name1+xs.s();			
		
		if(xs.isLimits()){                      		
			if(!name0.isEmpty())
				s=xs.sl()+"<"+name+"<"+xs.su();	
			else
				s=xs.sl()+"<value<"+xs.su();	
    	}else if(xs.dsu().equals(xs.dsl())){
    		s=name1+xs.s()+" "+xs.ds();
    	}else{
    		s=name1+xs.s()+" +"+xs.dsu()+"-"+xs.dsl();
    	}
		
		//System.out.println(" printEntry="+x+"  dxu="+dxu+" dxl="+dxl+" ds="+xs.dsu()+" "+xs.dsl()+"  *"+xs.ds()+"*"+" xs.isLimits()="+xs.isLimits()+" s="+s);
		
		s=s.trim();
		if(dxu>0)
			return s;
		else
			return xs.s().trim();
		
	}
	
	public Vector<String> printBranchReportLines(GammaBranch gb,Level level,Gamma gamma,Vector<String> oldBXLWlinesV,Vector<String> newBXLWlinesV){
		Vector<String> out=new Vector<String>();
       	
		out.add("   ");
		out.addAll(printGammaInfo(gb,level,gamma));
		out.add("   ");
		out.addAll(printWeisskopfEstimate(gamma.EF()));
		if(this.isPrintRULComparison){
			out.add("   ");
			out.addAll(printRULComparisons(gb));
		}

		out.add("   ");
		out.addAll(printBXLWResults(gb,level,gamma,oldBXLWlinesV,newBXLWlinesV));
		
		//debug
		//for(int i=0;i<out.size();i++)
		//	System.out.println(out.get(i));   	

		return out;
	}
	
	public Vector<String> printGammaInfo(GammaBranch gb,Level level,Gamma gamma){
		Vector<String> out=new Vector<String>();
		int ig=BXLCalc.branchsV().indexOf(gb);
		int nb=BXLCalc.nBranches();
		
		String line="";
		String head="--->gamma#"+nb+"-"+(ig+1)+": ";
		String indent=Str.repeat(" ",head.length());
		Vector<String> recordLines=printGammaRecords(gb,level,gamma);
		if(recordLines.size()==0)
			return out;
		
		line=head+recordLines.get(0);
		out.add(line);
		for(int i=1;i<recordLines.size();i++){
			line=indent+recordLines.get(i);
			out.add(line);
		}
		
		if(gamma.IS().isEmpty()){
			if(level.nGammas()==1)
				line=indent+"intensity (IG) is not given. IG=100 is assumed.";
			else if(level.nGammas()>1)
				line=indent+"intensity (IG) is not given. IG=0 is assumed.";
			
			out.add(line);
		}
		
		return out;
	}
	
	public Vector<String> printGammaRecords(GammaBranch gb,Level level,Gamma gamma){
		return printGammaRecords(gb,level,gamma,"EXTREME");
	}
	public Vector<String> printGammaRecords(GammaBranch gb,Level level,Gamma gamma,String option){
		Vector<String> out=new Vector<String>();
	
		String es=gamma.ES();
		String des=gamma.DES();
		String mult=gamma.MS();
		String mrs=gamma.MRS()+" "+gamma.DMRS();
		String ccs=gamma.CCS();
		String dccs=gamma.DCCS();
		String ris=gamma.IS();
		String dris=gamma.DIS();
		
		String brs="",tbrs="";
		String riInfo="";
		
		boolean printIS=false;
		boolean useNormalError=option.toUpperCase().contains("NORMAL");
		
		//energy
		if(Str.isNumeric(des))
			es=es+" "+des;
		else if(gb.deu()>0){
			XDX2SDS xs=new XDX2SDS(gb.e(),gb.deu());
			es=xs.s()+" "+xs.ds()+"(assumed)";
		}

		HashMap<String,BXLWResult> BXLWmap=gb.getBXLWmap();
		XDX br=new XDX(gb.br,gb.dbru_ex,gb.dbrl_ex);	
		XDX tbr=new XDX(gb.tbr,gb.dtbru_ex,gb.dtbrl_ex);

		if(BXLWmap.size()>0){
			BXLWResult result0=BXLWmap.values().iterator().next();
			
			if(useNormalError){
				br=result0.normalErrorBranching;
				tbr=result0.normalErrorTotalBranching;
			}else{
				br=result0.extremeErrorBranching;
				tbr=result0.extremeErrorTotalBranching;
			}
		}
		
		if(gb.br>0) 
			brs=printEntry(br.x,br.dxu,br.dxl);
		
		if(gb.tbr>0){
			tbrs=printEntry(tbr.x,tbr.dxu,tbr.dxl);
			if(gamma.IS().isEmpty())
				tbrs=tbrs.trim()+"(assumed)";
		}
		
		if(Str.isNumeric(dris) || (dris.isEmpty()&&gb.driu()==0)){//the later is for the case where there is only one branch and RI=100 DRI=0
			printIS=false;
		}else{
			printIS=true;
			int r=0;
			String assumedS=printEntryAsIs(gb.ri(),gb.driu(),gb.dril(),"RI",99);
		
			if(dris.isEmpty() || dris.charAt(0)!='G')
				r=(int)(gb.driu()/gb.ri()*100+0.1);
			riInfo="original RI="+ris+" "+dris+"    assumed "+assumedS;
			
			if(r>0)
				riInfo+=" (%DRI="+r+")";
		}
		
		//CC
		if(!dccs.trim().isEmpty())
			ccs=ccs+" "+dccs;
		else if(gb.dccu()>0){
			XDX2SDS xs=new XDX2SDS(gb.cc(),gb.dccu());
			ccs=xs.s()+" "+xs.ds()+"(assumed)";
		}
		
		if(gamma.MRS().isEmpty() && gb.mr()>0 && gb.getEMs().length>=2)//e.g., MULT=M1+E2, MR not given, mr=1, range[0.001,1000] assumed when adding branch
			mrs=String.format("%.1f", gb.mr())+" (assumed)";
			
		String line="EG="+es;
		if(brs.length()>0) line+="  BR(%g)="+brs;
		if(tbrs.length()>0) line+="  BR(%g+%ce)="+tbrs;
		if(mult.length()>0) line+="  Mult="+mult;
		if(mrs.trim().length()>0) line+="  MR="+mrs;
		if(ccs.trim().length()>0) line+=" CC="+ccs;
		
		out.add(line);

		if(printIS && riInfo.length()>0)
			out.add(riInfo);
		
		return out;
	}
	
	public Vector<String> printBXLWResults(GammaBranch gb,Level level,Gamma gamma,Vector<String> oldBXLWlinesV,Vector<String> newBXLWlinesV){
		Vector<String> out=new Vector<String>();
		
		String BXLWline=newBXLWlinesV.get(0);
    	String symBXLWline=newBXLWlinesV.get(1);
    	String BXLWline1=newBXLWlinesV.get(2);
    	

    	out.add("* Use uncertainties from normal ERROR PROPAGATION:\n");
    	out.addAll(printBXLWResults0(gb,level,gamma,"NORMAL"));
    	
    	out.add("  ");
    	
    	out.add("* Use uncertainties from MINIMUM and MAXIMUM of each variable:\n");
    	out.addAll(printBXLWResults0(gb,level,gamma,"EXTREME"));
    	
		out.add("  ");
		
		if(BXLWline.isEmpty())
			out.add(String.format("%-15s%s", "**** new line:","(limit value is suppressed)"));
		else{
	    	//out.add("*** new line:\n"+BXLWline);
			if(BXLWline.equals(BXLWline1))
				out.add(String.format("%-15s%s", "**** new line:",BXLWline));
			else{
				out.add(String.format("%-15s%s", "**** new line:",BXLWline1));
				out.add(String.format("%-15s%s", " asymmetrized:",BXLWline));	
			}
			
	    	if(!symBXLWline.equals(BXLWline))
	    		//out.add("*** new line (symmetrized):\n"+symBXLWline);
	    		out.add(String.format("%-15s%s", "  symmetrized:",symBXLWline));			
		}

		String line="";
    	//out.add("*** old line:");
    	for(int c=0;c<oldBXLWlinesV.size();c++){
    		line=oldBXLWlinesV.get(c);
    		if(!line.isEmpty()){
        		//out.add(line);
        		out.add(String.format("%-15s%s", "**** old line:",line));
    		}

    	}   	
    	
		return out;
	}
	
	/*
	 * option="NORMALERROR" for using uncertainty from normal error propagation
	 * otherwise, use uncertainty deduced from min and max
	 */
	public Vector<String> printBXLWResults0(GammaBranch gb,Level level,Gamma gamma,String option){
		Vector<String> out=new Vector<String>();
		
		Vector<String> warnings=new Vector<String>();//warning messages
    	int A=level.nucleus().A();
	
		boolean useNormalError=option.toUpperCase().contains("NORMAL");		
		
    	String indent=" ";
    		
		String gammaRecord=printGammaRecords(gb,level,gamma,option).get(0);	
		out.add(indent+gammaRecord);

		HashMap<String,BXLWResult> BXLWmap=gb.getBXLWmap();
		
		indent+="   ";
		
		String line="";
		XDX2SDS xs=new XDX2SDS(gb.pt(),gb.dptu(),gb.dptl());//partial T1/2
        String ts="T1/2(partial)="+xs.s()+" "+xs.ds()+" (sec)";
        ts=ts.trim();
        
        line=indent+ts;
        
    	XDX icc=gb.getCalcICC();    	
		if(icc!=null){
			double cc=icc.x();
			double dcc=icc.dxu();
			xs=new XDX2SDS(cc,dcc);
			
			if(cc>0){
				String ccs="Re-calculated CC="+xs.s()+" "+xs.ds();
				ccs=ccs.trim();
				line+="     "+ccs;
			}

		}

    	out.add(line);
    	
    	line="";
    	
    	for(String EM: BXLWmap.keySet()){
    		BXLWResult bxResult=BXLWmap.get(EM);
    		XDX bx;
    		if(useNormalError)
    			bx=bxResult.normalErrorBXLW;
    		else
    			bx=bxResult.extremeErrorBXLW;
    		
    		double RUL=BXLCalculator.RUL(A, EM);
    		String RULs=RULS(A,EM);
    		
    		double min=0;
    		if(bx.x()>0 && bx.dxl()>0)
    			min=bx.x()-bx.dxl();
    		
    		if(min>0 && min>RUL){
    			int igb=BXLCalc.branchsV().indexOf(gb);
    			String s=newBXLWEntryMapsV.get(igb).get(EM);
    			if(s==null){
    				xs=new XDX2SDS(bx.x(),bx.dxu(),bx.dxl(),uncertaintyLimitBXL);
    				if(xs.dsu().equals(xs.dsl()))
    					s=xs.s()+" "+xs.ds();
    				else
    					s=xs.s()+" +"+xs.dsu()+"-"+xs.dsl();
    				
    				s="B"+EM+"W="+s.trim();
    			}
    			line=warningPrefix+" Warning: "+s+" exceeds RUL="+RULs;
    				
    			warnings.add(line);
    			
    			nProblems++;
    		}
    		
    		//write BXL(DOWN)
    		int L=-1;
    		try{
    			L=Integer.parseInt(EM.charAt(1)+"");
    		}catch(NumberFormatException e){}
    		
    		if(L>0){
        		double x=BXLCalc.BXLW2BXLDOWN(bx.x(), L, EM);
        		double dxu=BXLCalc.BXLW2BXLDOWN(bx.dxu(), L, EM);
        		double dxl=BXLCalc.BXLW2BXLDOWN(bx.dxl(), L, EM);
        		String name="B"+EM+"(DOWN)";
        		String name1="B"+EM+"W";
        		
        		//String s=printEntryBXL(x,dxu,dxl,name);
        		String s=printEntryAsIs(x,dxu,dxl,name,500);		
        		String s1=printEntryAsIs(bx.x(),bx.dxu(),bx.dxl(),name1,500);
        		s1+=" (RUL="+RULS(A,EM)+")";
        		if(s.length()>0){
        			out.add(indent+String.format("%-32s", s)+String.format("%s", s1));    			
        			//System.out.println("######### eg="+gb.e()+"  bx="+x+" "+dxu+" "+dxl);
        			//System.out.println("          eg="+gb.e()+"  bx="+bx.x()+" "+bx.dxu()+" "+bx.dxl());
        		}
    		}

    	}
    	
    	
    	if(warnings.size()>0){
    		out.add("  ");
    		out.addAll(warnings);
    	}
    	
    	return out;
	}
	
	public Vector<String> printWeisskopfEstimate(float eg){
		return printWeisskopfEstimate(eg,0);
	}

	public Vector<String> printWeisskopfEstimate(float eg,int printOption){
		Vector<String> out=new Vector<String>();
		if(printOption==1){
			out.addAll(printWeisskopfT(eg));
			out.add(" ");
		    out.addAll(printWeisskopfBXL());
		    return out;
		}
		

		String indent="        ";
		out.add(indent+"Weisskopf single-particle B(XL)(s.p.)(down) and half-lives T1/2(s.p.) in second:");
		out.add(indent+String.format("  %-3s   %-15s   %-15s    %-3s   %-15s   %-15s","L"," B(EL)sp"," B(ML)sp","L", "T1/2(EL)sp","T1/2(ML)sp"));
		
		String formatStr="  %-3d   %-15.4E   %-15.4E    %-3d   %-15.4E   %-15.4E";
		double te=0,tm=0,be=0,bm=0;
			
		String line="";
		for(int L=1;L<=4;L++){
			te=BXLCalc.T12EL_SP(eg,L);
			tm=BXLCalc.T12ML_SP(eg,L);
			be=BXLCalc.BELDOWN_SP(L);
			bm=BXLCalc.BMLDOWN_SP(L);
			
			//int n1=(int)Math.floor(Math.log10(99.9/te));
			//int n2=(int)Math.floor(Math.log10(99.9/te));
			
			line=indent+String.format(formatStr,L,be,bm,L,te,tm);
			out.add(line);
		}		
		

		//debug
		//for(int i=0;i<out.size();i++)
		//	System.out.println(" line="+eg+"  "+out.get(i));
		
		return out;
	}
	
	public Vector<String> printWeisskopfT(float eg){
		Vector<String> out=new Vector<String>();
		
		String indent="        ";
		out.add(indent+"Weisskopf single-particle half-lives T1/2(s.p.) in second:");
		out.add(indent+String.format("  %-3s   %-15s   %-15s","L", " T1/2(EL)"," T1/2(ML)"));
		
		String formatStr="  %-3d   %-15.4E   %-15.4E";
		double te=0,tm=0;
			
		String line="";
		for(int L=1;L<=5;L++){
			te=BXLCalc.T12EL_SP(eg,L);
			tm=BXLCalc.T12ML_SP(eg,L);
			
			//int n1=(int)Math.floor(Math.log10(99.9/te));
			//int n2=(int)Math.floor(Math.log10(99.9/te));
			
			line=indent+String.format(formatStr, L,te,tm);
			out.add(line);
		}	
		return out;
	}
	
	public Vector<String> printWeisskopfBXL(){
		Vector<String> out=new Vector<String>();
		String indent="        ";
		out.add(indent+"Weisskopf single-particle B(XL)(down):");
		out.add(indent+String.format("  %-3s   %-15s   %-15s","L","  B(EL)","  B(ML)"));
		
		String formatStr="  %-3d   %-15.4E   %-15.4E";
		double be=0,bm=0;
			
		String line="";
		for(int L=1;L<=5;L++){
			be=BXLCalc.BELDOWN_SP(L);
			bm=BXLCalc.BMLDOWN_SP(L);
			
			//int n1=(int)Math.floor(Math.log10(99.9/te));
			//int n2=(int)Math.floor(Math.log10(99.9/te));
			
			line=indent+String.format(formatStr,L,be,bm);
			out.add(line);
		}	
		
		return out;
	}
	
	public Vector<String> printRULComparisons(GammaBranch gb){
		Vector<String> out=new Vector<String>();
		String indent="        ";
		String formatStr="";
		boolean isUseInputCC=BXLCalc.isUseInputCC();
		
		out.add(indent+"Recommended Upper Limits (RUL) comparisons with calculated B(XL)(W.u.) if pure E or M");

		if(!isGoodBriccs){
			out.add(indent+"    (Note: ICC can't be caculated. Input ICC is assumed in all calculations");
			out.add(indent+String.format("  %-2s %-4s  %-17s   %-4s    %-2s %-4s  %-17s   %-4s","","", " B(EL)(W.u.)","RUL","",""," B(ML)(W.u.)","RUL"));
			formatStr="%-2s %-4s  %-10s %-6s   %-4s    ";
			BXLCalc.setIsUseInputCC(true);
		}else{
			out.add(indent+"    (Note: ICC is re-calculated using BrIcc for each pure E or M)");
			out.add(indent+String.format("  %-2s %-4s %-8s  %-17s   %-4s    %-2s %-4s %-8s  %-17s   %-4s","","","ICC","  B(EL)(W.u.)","RUL","","","ICC","  B(ML)(W.u.)","RUL"));
			formatStr="%-2s %-4s %-8s  %-10s %-6s   %-4s    ";
			BXLCalc.setIsUseInputCC(false);
		}
		
		
		String mult="";
		String label="";
		String line="";
		String bs="",dbs="",RULs="";
		String[] types={"E","M"};
		
		int A=(int)(BXLCalc.mass()+0.2);
	
		for(int L=1;L<=4;L++){
			line=indent+"  ";
			
			for(int i=0;i<types.length;i++){			
				
				//for pure E or M
				label="";
				mult=types[i]+L;
				RULs=RULS(A,mult);
				
				if(L==1)
					label="(IV)";
				else if(L==2){
					if(i==0)
						label="(IS)";
					else
						label="(IV)";
				}
					
				
				double mr=gb.mr();
				double dmrl=gb.dmrl();
				double dmru=gb.dmru();
                String tempMult=gb.mult();
                XDX calcICC=gb.getCalcICC();
                
				gb.setMixingRatio(0, 0);
				gb.setMultipolarity(mult);
				
				BXLWResult result=BXLCalc.calculateBXLW(gb, BXLCalc.t12(), L, "L",mult);
			    XDX bx;
			    
			    if(result.canUseNormalError)
			    	bx=result.normalErrorBXLW;
			    else
			    	bx=result.extremeErrorBXLW;
			    
				XDX2SDS x2s=new XDX2SDS(bx.x(),bx.dxu(),bx.dxl(),this.uncertaintyLimitBXL);
				bs=x2s.S();
				if(!x2s.dsu().equals(x2s.dsl()))
					dbs="+"+x2s.dsu()+"-"+x2s.dsl();
				else
					dbs=x2s.ds();
				
				if(isGoodBriccs){
					XDX icc=gb.getCalcICC();
					x2s=new XDX2SDS(icc.x(),icc.dxu(),icc.dxl());
					line+=String.format(formatStr,mult,label,x2s.s(),bs,dbs,RULs);
				}else{
					line+=String.format(formatStr,mult,label,bs,dbs,RULs);
				}
				
				gb.setMixingRatio(mr, dmru, dmrl);
				gb.setMultipolarity(tempMult);
				gb.setCalcICC(calcICC);

			}
			
			out.add(line);
		}
		
		BXLCalc.setIsUseInputCC(isUseInputCC);
		
		return out;
	}
	
	public void addGammaBranches(Level level){
				
		BXLCalc.setMass(level.nucleus().m());
		BXLCalc.setZ(level.nucleus().Z());
		
		double t=level.T12VD();
		double dtu=level.T12UVD()-t;
		double dtl=t-level.T12LVD();
		
		if(dtl<=0) dtl=0;
		if(dtl>=t) dtl=t*0.99999999;
		
		BXLCalc.setT12(t,dtu,dtl);
		
		//if(level.ES().contains("3229"))
		//System.out.println(" T="+t+" DTU="+dtu+" DTL="+dtl+" DTS="+level.DT12S());    		
		
		int ng=level.nGammas();
		
		//add gamma branches
		double e=0,de=0,mr=0,dmru=0,dmrl=0,ri=0,driu=0,dril=0,cc=0,dccu=0,dccl=0;
		double ti=0,dtil=0,dtiu=0;
		
		for(int k=0;k<ng;k++){
			
			e=0;
			de=0;
			mr=0;
			dmru=0;
			dmrl=0;
			ri=0;
			driu=0;
			dril=0;
			cc=0;
			dccu=0;
			dccl=0;
			
			ti=0;
			dtiu=0;
			dtil=0;
			
			Gamma g=level.gammaAt(k);
	
			
			//asymmetric uncertainty can be given in MR field in ENSDF file
			if(!g.MRS().isEmpty()){
				SDS2XDX MR=new SDS2XDX(g.MRS(),g.DMRS());
				mr=MR.x();
				double min=MR.XLV();
				double max=MR.XUV();
				double f=(max-mr)*(mr-min);
				if(max>min && f>=0){
					if(mr>0){
		    			dmru=MR.XUV()-mr;
		    			dmrl=mr-MR.XLV();	
					}else{
						mr=-mr;
		    			dmrl=MR.XUV()-mr;
		    			dmru=mr-MR.XLV();
					}    			
				}else{//for uncertainty=empty,LT,GT,AP,SY, CA
					XDX parsedMR=UncertaintyParser.findParsedXDX(g.MRS(),g.DMRS(),g.DMRS());
					if(parsedMR.dxl>=0){
						dmru=parsedMR.dxu();
						dmrl=parsedMR.dxl();
					}else{//should not happen
						System.out.println("*** something wrong in parsing non-numerical MR uncertainty:"+g.MRS()+" "+g.DMRS());
						dmru=Math.abs(mr*0.5);
						dmrl=dmru;
					}

				}
				//System.out.println("k="+k+" eg="+g.EF()+" mrs="+g.MRS()+" dmrs="+g.DMRS()+"  MR.XUV=="+MR.XUV()+" MR.XLV="+MR.XLV()+" line="+g.recordLine());

			}
			
			
			//for case, DRI=0 & DRI_S=empty, use DRI=0, this is the case for 
			//ENSDF RI=100 with DRI=empty when PN=6
			if(Str.isNumeric(g.DIS()) || (g.DIS().isEmpty()&&g.DID()==0)){
				ri=g.ID();
				driu=g.DID();
				dril=driu;
			}else{
				XDX parsedXDX=UncertaintyParser.findParsedXDX(g.IS(), g.DIS(),g.DIS());
				ri=parsedXDX.x();
				driu=parsedXDX.dxu();
	            dril=parsedXDX.dxl();
			}

			
			//ri=g.ID();
			//driu=g.DID();
			//dril=g.DID();
			
			e=g.EF();
			de=g.DEF();
			if(g.DES().isEmpty()){
				String es=g.ES().trim().toUpperCase();
				if(es.indexOf(".")>0 && es.indexOf("E")<0){
					de=0.5;
					if(de>e*0.01)
						de=e*0.01;
				}else
					de=1.0;//1 keV
			}else if(de<=0)//ds=AP, CA, LT, ...
				de=20.0;//

		    if(de>e)
				de=e*0.99999;
			
		    //only symmetric uncertainty (len<=2) can be given in ENSDF file
		    
			cc=g.CCF();
			dccu=g.DCCF();
			dccl=dccu;
			
			float r=theoryDCC;
			if(cc>0){
				if(g.DCCS().isEmpty()){
					dccu=r*cc;
					dccl=dccu;
				}else if(dccu>=0){
					if(theoryDCCType.equals("H"))
						dccu=Math.sqrt(dccu*dccu+r*r*cc*cc);
					else if(!theoryDCCType.equals("B"))
						dccu=r*cc;
					
					dccl=dccu;
				}else{//dccs=LT,GT,AP, ...
					SDS2XDX CC=new SDS2XDX(g.CCS(),g.DCCS());
					double min=CC.XLV();
					double max=CC.XUV();
					double f=(max-cc)*(mr-cc);
					if(max>min && f>=0){
		    			dccu=CC.XUV()-cc;
		    			dccl=cc-CC.XLV();
					}else{//if uncertainty not given or AP, SY, CA
						XDX parsedCC=UncertaintyParser.findParsedXDX(g.CCS(),g.DCCS(),g.DCCS());
						if(parsedCC.dxl>=0){
							dccu=parsedCC.dxu();
							dccl=parsedCC.dxl();
						}else{//should not happen
							System.out.println("*** something wrong in parsing non-numerical CC uncertainty:"+g.CCS()+" "+g.DCCS());
							dccu=Math.abs(cc*0.5);
							dccl=dccu;
						}

					}
				}
			}
				
			if(e<0) {e=0;de=0;}
			if(de<0) de=0;
			if(ri<0) ri=0;
			if(driu<0) driu=0;
			if(dril<0) dril=0;
			if(dmru<0) dmrl=0;
			if(dmrl<0) dmrl=0;
			if(cc<0) cc=0;
			if(dccu<0) dccu=0;
			if(dccl<0) dccl=0;
			
			BXLCalc.addGammaBranch(e, de, ri, driu,dril,g.MS(),mr, dmru, dmrl, cc, dccu, dccl);

			GammaBranch gb=BXLCalc.branchsV().lastElement();
			String[] EMs=gb.getEMs();
			int nGoodEM=0;
			for(int j=0;j<EMs.length;j++){
				if(gb.isGoodEM(EMs[j]))
					nGoodEM++;
			}
			
			if(nGoodEM==2 && g.MRS().isEmpty()){//e.g., MULT=M1+E2, but MR is not given
				gb.setMixingRatio(1, 0.999, 9999);//assuming MR=1.0, MR range=[0.0001,10000]
			}

			
			if(g.IS().isEmpty() && !g.TIS().isEmpty()){
				if(Str.isNumeric(g.DTIS()) || (g.DTIS().isEmpty()&&g.DTISD()==0)){
					ti=g.TISD();
					dtiu=g.DTISD();
					dtil=dtiu;
				}else{
					XDX parsedXDX=UncertaintyParser.findParsedXDX(g.TIS(), g.DTIS(),g.DTIS());
					ti=parsedXDX.x();
					dtiu=parsedXDX.dxu();
		            dtil=parsedXDX.dxl();
				}
				
				gb.setTotalIntensity(ti, dtiu, dtil);
			}
			
			//debug
			//System.out.println(" gamma="+g.ES()+" IS="+g.IS()+" ri="+ri+" mult="+g.MS()+" MR="+g.MRS()+" "+g.DMRS());
			//System.out.println("    mr="+mr+" dmru="+dmru+" dmrl="+dmrl+" cc="+g.CCF()+" dcc="+g.DCCF()+" ti="+gb.ti+" g.TIS="+g.TIS()+" g.ID="+g.ID());
		}
		
		if(ng==1){
			BXLCalc.branchAt(0).setIntensity(100, 0);//NOTE: force to set ri=100 if there is only one branch
		}
		
		//debug
		//for(GammaBranch gb:BXLCalc.branchsV())
		//	System.out.println(" eg="+gb.e()+" ri="+gb.ri());

		//calculate absolute and relative gamma branchings
		//No need for this branching calculation since only
		//input gamma intensities are needed for calculating 
		//BXL. Here BranchingCalculator is just used for 
		//re-setting the uncertainties for those intensities 
		//with non-numerical uncertainties, e.g., "AP", "LT"
		//bc.calculate();
	}
	
	/*
	 * first  element: line with new BXLW values (not symmetrized if asymmetric)
	 * second element: line with new BXLW values after symmetrization
	 * third  element: line with new BXLW values (not symmetrized if asymmetric unless uncertainty difference=1 in ENSDF format)
	 *            eg., for 101.4 +12-11, use symmetrized value=101.4 11 
	 */
	public Vector<String> makeNewBXLWlines(GammaBranch gb,Level level,Gamma gamma){
		Vector<String> linesV=new Vector<String>();
		
    	String[] EMs=gb.getEMs();
    	HashMap<String,String> newBXLWEntryMap=this.makeNewBXLWEntryMap(gb, level, gamma);
    	
    	String BXLWline=gamma.recordLine().substring(0,9);
    	BXLWline=BXLWline.substring(0, 5)+"B"+BXLWline.substring(6);
    	
    	String bs="",sbs="",bs1="";//sbs for symmetrized BXLW 
    	String symBXLWline=BXLWline;//line with symmetrized BXLW values
    	String BXLWline1=BXLWline;
    	
    	for(int m=0;m<EMs.length;m++){
        //for(String key:BXLWmap.keySet()){
    		//System.out.println(" e="+gb.e()+" EM="+EMs[m]+" entry="+newBXLWEntryMap.get(EMs[m]));
    		
    		String key=EMs[m];
    		String entry=newBXLWEntryMap.get(key);
    		String sentry=newBXLWEntryMap.get(key+"S");//asymmetrized
    		if(entry==null || entry.isEmpty())
    			continue;
    		if(sentry==null)
    			sentry="";
    		        	
        	if(bs.length()>0){
        		bs+=" $";
        		bs1+=" $";
        		sbs+=" $";
        	}
        	
        	bs+=entry;
        	if(!sentry.isEmpty()){
        		sbs+=sentry;
        		boolean processed=false;
        		if(entry.contains("+") && entry.contains("-")){
        			try{
        				String s=entry.substring(entry.indexOf("+")+1).trim();
            			String[] vals=s.split("-");
            			int u1=Integer.parseInt(vals[0]);
            			int u2=Integer.parseInt(vals[1]);
            			if(Math.abs(u1-u2)==1){
            				bs1+=sentry;
            				processed=true;
            			}
        			}catch(Exception e){}      			
        		}
        		
        		if(!processed)
        			bs1+=entry;
        	}else{
        		bs1+=entry;
        		sbs+=entry;
        	}
        }
        
        //debug
        //System.out.println("##       level="+level.ES()+" gamma="+gb.e()+" line="+BXLWline+" bs="+bs);
        
        if(bs.length()>0){
        	BXLWline+=bs;
        	symBXLWline+=sbs;
        	BXLWline1+=bs1;
        	
        	BXLWline=Str.fixLineLength(BXLWline,80);
        	symBXLWline=Str.fixLineLength(symBXLWline,80);
        	BXLWline1=Str.fixLineLength(BXLWline1,80);
        }else{
        	BXLWline="";
        	symBXLWline="";
        	BXLWline1="";
        }
        
        linesV.add(BXLWline);
        linesV.add(symBXLWline);
        linesV.addElement(BXLWline1);
        
        return linesV;
	}
	
	
	/*
	 * make a map of new BXLW entries for the branch gb to be printed in output ENSDF file
	 * (both asymmetric and symmetrized ones)
	 * key=EL or ML for original entry and ELS or MLS for symmetrized one, L=1,2,3,4
	 * value=new BXLW entry of value and uncertainty in ENSDF style
	 *    e.g., key="M1", value="BM1W=12 +3-2"
	 *          key="M1S"
	 */
	private HashMap<String,String> makeNewBXLWEntryMap(GammaBranch gb,Level level,Gamma gamma){
		HashMap<String,String> newBXLWEntryMap=new HashMap<String,String>();
		
    	String[] EMs=gb.getEMs();
    	HashMap<String,BXLWResult> BXLWmap=gb.getBXLWmap();
    	
    	String BXLWline=gamma.recordLine().substring(0,9);
    	BXLWline=BXLWline.substring(0, 5)+"B"+BXLWline.substring(6);
    	
    	for(int m=0;m<EMs.length;m++){
        //for(String key:BXLWmap.keySet()){
    		String key=EMs[m];
        	BXLWResult bxResult=BXLWmap.get(key);
        	if(bxResult==null)
        		continue;
		    
        	XDX bx;
		    if(bxResult.canUseNormalError)
		    	bx=bxResult.normalErrorBXLW;
		    else
		    	bx=bxResult.extremeErrorBXLW;
		    
        	float x=(float)bx.x();
        	float dxu=(float)bx.dxu();
        	float dxl=(float)bx.dxl();
        	
            //debug
            //System.out.println(" 0      level="+level.ES()+" gamma="+gb.e()+" key="+key+" x="+x+" dxu="+dxu+" dxl="+dxl);
            
        	if(x<0 || dxu<0 || dxl<0)
        		continue;
        	        	
        	
        	String name="B"+key+"W";
        	String entry="";
        	String sentry="";
        	String dts=level.DT12S().trim();
        	
        	XDX2SDS xs=new XDX2SDS(x,dxu,dxl,this.uncertaintyLimitBXL);
    		double xu=xs.x()+xs.dxu();
    		double xl=xs.x()-xs.dxl();
    		double r=0;
    		if(xl>0)
    			r=xu/xl;
    		
    		boolean done=false;
    		
        	if(!isSuppressBXLWLimit && dts.length()>0 && !level.dt12IsNumber() && dts.indexOf("+")<0 && dts.indexOf("-")<0){
        		
        		done=true;
        		char c=dts.trim().charAt(0);
        		if(c=='L'){//LT,LE
        			if(xl>0)
        				entry=name+">"+xs.sl();
        		}else if(c=='G'){//GT,GE
        			if(xu>0 && xu<1000 && r<1E4)//1000 is the largest RUL for BXWL
        				entry=name+"<"+xs.su();
        		}else if(dts.equals("AP") && xs.x()>0)
        			entry=name+" AP "+xs.s();
          		else
          			done=false;
        	}
        	
        	if(!done){
        		if(dts.isEmpty())
        			entry=name+"="+xs.s();
        		else{
        			entry=printEntryBXL(xs,name,isSuppressBXLWLimit);
        			if(!xs.isLimits() && !xs.dsu().equals(xs.dsl()))
        				sentry=name+"="+xs.ss()+" "+xs.ds();      					
        		}
        	}

        	entry=entry.trim();
        	sentry=sentry.trim();
        	
            //debug
            //System.out.println(" 1     level="+level.ES()+" gamma="+gb.e()+" key="+key+" x="+x+" dxu="+dxu+" dxl="+dxl+" s="+xs.s()+" dsu="+xs.dsu()+" dsl="+xs.dsl()+" "+xs.isLimits()+" entry="+entry+" dts="+dts);
            
        	if(entry.isEmpty())
        		continue;
        	
        	newBXLWEntryMap.put(key, entry);
        	if(!sentry.isEmpty())
        		newBXLWEntryMap.put(key+"S", sentry);
        	
            //debug
            //System.out.println(" 1     level="+level.ES()+" gamma="+gb.e()+" key="+key+" x="+x+" dxu="+dxu+" dxl="+dxl+" dsu="+xs.dsu()+" dsl="+xs.dsl());

        }
        
    	
    	int nb=BXLCalc.branchsV().indexOf(gb);
    	if(nb>=0 && !newBXLWEntryMapsV.containsKey(nb))
    		newBXLWEntryMapsV.put(nb, newBXLWEntryMap);
    	
        return newBXLWEntryMap;
	}
	
	public static String removeBXLW(String line){
		String[] types={"E","M"};
		if(line.length()<=10 || !line.substring(6,8).equals(" G") || line.charAt(5)==' ' || line.substring(9).trim().isEmpty())
			return line;
		
		String s=line;
		String bs="";
		String s1="",s2="";
		int p1=-1,p2=-1;
		for(int i=0;i<types.length;i++){
			for(int j=1;j<=6;j++){
				bs="B"+types[i]+j+"W";
				p1=s.indexOf(bs);
				if(p1<0)
					continue;
				
				s1="";s2="";
				
				s1=s.substring(0,p1);
				if(s1.length()<9)
					return line;
				
				s1=s1.substring(0, 9);
				
				p2=s.indexOf("$",p1+bs.length());
				if(p2>0)
					s2=s.substring(p2+1).trim();
				
				s=s1+s2;
				
				//debug
    			//System.out.println("type="+bs+" s="+s+" len="+s.length()+" s1="+s1+" s2="+s2+" line="+line);
			}
		}
		
		if(s.length()<=10 || s.substring(9).trim().isEmpty())
			return "";
		
		return s;
		
	}
}
