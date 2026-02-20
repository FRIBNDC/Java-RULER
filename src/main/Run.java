/*
 * Created by Jun Chen, April 2018
*/
package main;


import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

import calc.BXLCalcWrap;
import calc.BXLCalculator;
import calc.BriccsWrap;
import calc.UncertaintyParser;
import ensdf.ENSDF;
import ensdf.Gamma;
import ensdf.Level;
import main.Setup;
import nds.ensdf.EnsdfUtil;
import nds.ensdf.MassChain;
import nds.util.Str;

public class Run {
    
    private JTextArea messenger;
    FileOutputStream outputStream;
	static private final String newline = "\n";
    
	private boolean redirectToMessenger=false;
	
	private String os;
	
	private String dirSeparator="\\";//"\\" for Windows, "/" for Linux
	
	
	private String infilepath="";
	private String outfilepath="";
	
	private Vector<String> infileNamesV=new Vector<String>();
	
	public Run(){
        //find out what operating system is being used, and set the right filename for the script
        String temp=System.getProperty("os.name");
        
        if(temp.toLowerCase().contains("linux")||temp.toLowerCase().contains("mac")) 
        	os="linux";
        else if(temp.toLowerCase().contains("windows")) 
        	os="windows";
        else 
        	os="other";
        
        dirSeparator="\\";
        if(!os.equals("windows"))
        	dirSeparator="/";
        
        infileNamesV=new Vector<String>();
        
        Control.isGoodBriccs=false;

	}
	
	public void debug(){
        //debug
        printMessage(" briccs name="+BriccsWrap.briccsExecName()+" path="+BriccsWrap.briccsExecPath()+" home="+BriccsWrap.briccsExecHome());
	}
		
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//functions specific for RULER calculations
	
	public void setIsUseSymmetrized(boolean b){Control.isUseSymmetrized=b;}
	public void setIsUseInputCC(boolean b){Control.isUseInputCC=b;}
	public void setIsSuppressBXLWLimit(boolean b){Control.isSuppressBXLWLimit=b;}
	
	
	public boolean isUseSymmetrized(){return Control.isUseSymmetrized;}
	public boolean isUseInputCC(){return Control.isUseInputCC;}
	public boolean isSuppressBXLWLimit(){return Control.isSuppressBXLWLimit;}
	
	public Vector<String> infileNamesV(){return infileNamesV;}
	
	
	public String checkBriccs(){
        
		String msg="";
		String msg1="";
		//copy the bricss packed inside the JAR file if exists and check if it works
		if(!Control.isGoodBriccs && !Control.isUseInputCC){
			
			msg+="Checking if BrIccs exists and works...\n";
			
			printMessage(msg);
			
			Control.isGoodBriccs=BriccsWrap.checkInternalBrIccs(Setup.outdir);
			
			String briccsName=BriccsWrap.briccsExecName();
			
			if(!Control.isGoodBriccs){
				msg1="";
				msg1+="BrIccs=<"+briccsName+"> in package is not copied into output folder or is not working.\n";
				msg1+="*** BrIccs (32-bit) is not working probably because of the following reasons:\n";
				//msg1+="        Inputs from gamma records are not valid for BrIccs calculation;\n";
				msg1+="        BrIccs executable is not permitted to run (try changing permission manually);\n";
				msg1+="        Running system (Linux/MacOS) is 64-bit (try installing 32-bit loader).\n";
				msg1+="*** So no CC values can be calculated and input CC values in dataset are used instead.\n";
				
				printMessage(msg1);
				
				msg+=msg1;
				Control.isUseInputCC=true;
			}else{
				msg1="BrIccs=<"+briccsName+"> is working ok.\n";
				printMessage(msg1);
				
				msg+=msg1;
			}
		}
		
		return msg;
		
        //printMessage(" isGoodBriccs="+isGoodBriccs);
	}
	
	public void calculateAndWriteBXLW(MassChain data,String outfilename) throws Exception{
		     
		String filename=Setup.outdir+dirSeparator+Str.fileNamePrefix(outfilename);
		
	    PrintWriter reportFile=new PrintWriter(new File(filename+".rpt"));
	    PrintWriter newFile=new PrintWriter(new File(filename+".out"));
	    
	    int nProblems=0;
	    
	    writeReportHead(reportFile);
	    
	    printMessage("Start calculation...\n");
	    
	    //debug
	    //debug();
	    
	    String msg="";
		msg+="\n";
		
		int nfiles=infileNamesV.size();
		if(nfiles==1)
			msg+="   input file : "+infileNamesV.get(0)+"\n";
		else if(nfiles>1)
			msg+="   input files: "+infileNamesV.get(0)+"\n";
		for(int i=1;i<nfiles;i++)
			msg+="                "+infileNamesV.get(i)+"\n";
		
        msg+="   output dir : "+Setup.outdir+"\n";
        msg+="   output file: "+filename+".out\n";
        msg+="   report file: "+filename+".rpt\n";	
        
        
	    if(Control.isNewOutpath)
	    	printMessage(msg+"\n");
	    else
	    	printMessage("\n");
	    
		reportFile.write(msg+"\n");
        
		int nBlocks=data.nBlocks();
		int nENSDF=data.nENSDF();
		int nENSDFtoBeProcessed=0;
		for(int i=0;i<nENSDF;i++){
			ENSDF ens=data.getENSDF(i);
			if(ens.nGamWM()>0 && ens.nLevWT()>0)
				nENSDFtoBeProcessed++;
		}

		String noCalcMsg="No BXLW to be calculated for input file(s).";
		String prefix="    ";
		String datasetSeparator=Str.repeat("*", 100);
		String levelSeparator=Str.repeat("-", 100);
		msg="";
		if(nBlocks==0 || nENSDF==0 || nENSDFtoBeProcessed==0){
			msg+=noCalcMsg+"\n";
			if(nBlocks==0)
				msg+=prefix+"Empty file\n";
			else if(nENSDF==0)
				msg+=prefix+"No ENSDF databset.\n";
			else if(nENSDFtoBeProcessed==0)
				msg+=prefix+"No calculable gamma (no T1/2, Mult).\n";
			
			printMessage(msg+"\n");
			reportFile.write(msg+"\n");
			
			newFile.close();
			reportFile.close();
			return;
		}
		
	
			    
		msg="";
		msg+="   Calculation settings:\n";
		if(Control.isUseInputCC){
			msg+="     use input CC in dataset\n";

			if(Control.theoryDCCType.equals("H"))
				msg+="     DCC theory="+String.format("%.1f", Control.theoryDCC*100)+"% added in quadrature to DCC\n";
			else
				msg+="     DCC theory="+String.format("%.1f", Control.theoryDCC*100)+"% assumed if DCC not given\n";	
				
			msg+="     relative uncertainty limit="+String.format("%.0f", Control.errorPropagationLimit*100)+"% for normal error propagation\n";
			msg+="           ** if any of the variables, E, T1/2, MR and RI, have relative uncertainty greater than this limit,\n";
			msg+="           ** uncertainty will be deduced from mininum and maximum value of each variable.\n";
		}else{
			msg+="     re-calculate CC using BrIcc";
			if(Control.isOnlyCalculateLargeCC)
				msg+=": only CC>"+Control.largeCCLimit+" will be re-calculated.";
			msg+="\n";
			msg+="     (note: it may take a while for BrIcc to calculate CC if many)\n";
		}
		
		if(Control.isUseSymmetrized)
			msg+="     symmetrize uncertainty\n";

		String indent="     ";
		msg+=printUncertaintySettings(indent);
		
		msg+="\nnumber of data blocks="+nBlocks+"\n\n";
		
		reportFile.write(msg);	
		printMessage(msg);		

		
		//printMessage(" isUseInputCC="+Control.isUseInputCC+" isGoodBriccs="+isGoodBriccs);
		//System.out.println("**"+msg);
		
		//check if briccs is working
		msg=checkBriccs();
		if(msg.length()>0){
			printMessage("\n");
			
			msg+="\n"+checkBriccs();
			reportFile.write(msg);
		}
		
		msg="";
	    for(int iblock=0;iblock<nBlocks;iblock++){
	    	Vector<String> lines=data.getBlockAt(iblock);
	    	String blockType=data.getBlockTypeAt(iblock);
	    	String firstLine=lines.get(0);
	    	String blockID=firstLine.substring(9,38).trim();
	    	String NUCID=firstLine.substring(0,5).trim();
	    	blockID=NUCID+": "+blockID;
	    	
	    	boolean canBeCalculated=false;
	    	
	    	ENSDF ens = null;
	    	if(blockType.equals("ENSDF")){
	    		ens=data.getETDByBlockAt(iblock).getENSDF();
	    		if(ens.nGamWM()>0 && ens.nLevWT()>0)
	    			canBeCalculated=true;
	    	}
	    	
	    	msg="Dataset=<"+blockID.trim()+">";
	    	printMessage("------>"+msg+"\n");
	    	
	    	int len=msg.length();
	    	int p=20;
	    	msg=datasetSeparator.substring(0,p)+msg+datasetSeparator.substring(p+len);
	    	reportFile.write("\n"+msg+"\n\n");
	    	
	    	noCalcMsg="    No BXLW to be calculated for dataset:"+blockID+"\n";
	    	if(!canBeCalculated){
	    	  		
	    		String s=prefix+"No calculable gamma (no T1/2, Mult).\n";
	    		reportFile.write(noCalcMsg+s);
	    		printMessage(noCalcMsg+s);
	    		
	    		Str.write(newFile,lines);
	    		newFile.write("\n");
	    		    		
	    		continue;
	    	}
	    	
	    	
	    	int nLevels=ens.levelsV().size();
	    	lines=ens.lines();
	    	
	    	Vector<String> newLines=new Vector<String>();
	    	Vector<String> levelLines=new Vector<String>();
	    	Vector<String> gammaLines=new Vector<String>();
	    	
	    	int firstLevelPos=ens.firstLevelLineNo();
	    	
	    	newLines.addAll(lines.subList(0, firstLevelPos));  
	    	
	    	int ngCalc=0;//count total number of gammas that have BXLW being calculated in a dataset .
	    	@SuppressWarnings("unused")
			int nlCalc=0;//count total number of level that have BXLW of its any gamma being calculated in a dataset .
	    	for(int ilevel=0;ilevel<nLevels;ilevel++){
	    		Level level=ens.levelAt(ilevel);
	    		
	    	    int li=((Integer) ens.poslev().elementAt(ilevel)).intValue(); 
	    	    int lf=lines.size()-1; 
	    	    if(ilevel<(nLevels-1 )) 
	    	    	lf=((Integer) ens.poslev().elementAt(ilevel+1)).intValue()-1;
	    	      
	    	    int ng=level.nGammas();
	    	    String ts=level.T12S().trim()+level.T12US().trim();
	    	    boolean hasHalflife=true;
	    	    boolean hasRIandMult=false;
	    	    
	    	    if(ts.isEmpty() || level.T12US().toUpperCase().indexOf("EV")>=0)
	    	    	hasHalflife=false;
	    	    
	    	    for(int k=0;k<ng;k++){
	    	    	Gamma gamma=level.gammaAt(k);
	    	    	String MS=gamma.MS();
	    	    	boolean hasRI=false;
	    	    	if(!gamma.IS().isEmpty() || ng==1)//assuming RI=100 if IS empty and only one branch
	    	    		hasRI=true;
	    	    	
	    	    	if(hasRI && (MS.contains("E")||MS.contains("M"))){
	    	    		hasRIandMult=true;
	    	    		break;
	    	    	}
	    	    }
	    	    
	    	    if(ng==0 || !hasHalflife || !hasRIandMult){
	    	    	newLines.addAll(lines.subList(li, lf+1));
	    	    	continue;
	    	    }
	    	    
	    	    levelLines.clear();    	     
	    	    
	    	    for( int k=li; k<=lf; k++){
	    	    	String line= (String) lines.elementAt(k);
	    	    	if(line.charAt(7)!='G'){
	    	    		newLines.add(line);
	    	    		continue;
	    	    	}
	    	    	
	    	    	levelLines.addElement(line);
	    	    }
	    	    //System.out.println(" level="+level.ES()+" ng="+level.nGammas()+" levelLine="+levelLines.get(0));
	    	    
	    	  	    	    
	    	    Vector<Vector<String>> gammaLinesV=EnsdfUtil.findRecordLines(levelLines, "G");
	    	    
	    	    //System.out.println("    gammaLinesV.size="+gammaLinesV.size());
	    	    
	    	    //debug
	    	    //if(ng!=gammaLinesV.size()) {System.out.println("In Run line 126: something wrong here!");System.exit(0);};
	    	    
	    		
	    	    //////////////////////////////////////////////////////////////
	    	    //calculate BXLW values 
	    	    /////////////////////////////////////////////////////////////
	    	    BXLCalculator BXLCalc=new BXLCalculator();
	    	    BXLCalcWrap calcWrap=new BXLCalcWrap(BXLCalc);
	    			    		
	    		//initialize BXLW calculator
	    		BXLCalc.setIsUseInputCC(Control.isUseInputCC);
	    		
	    		calcWrap.setIsPrintRULComparison(Control.isPrintRULComparison);
	    		calcWrap.setIsSuppressBXLWLimit(Control.isSuppressBXLWLimit);
	    		    		
                calcWrap.addGammaBranches(level);
	    		
	    		//calculate BXLW values for all gamma branches if available
	    		BXLCalc.calculateAll();
	    		//////////////////////////////////////////////////////////////
	    		
	    		
	    		//write new ENSDF lines with new BXLW values for each gamma branch if applicable 
	    		Vector<String> reportLines=new Vector<String>();//report fo BXLW calculations for all gammas from a level 
	    		
	    	    for(int igamma=0;igamma<ng;igamma++){
	    	    	gammaLines=gammaLinesV.get(igamma);
	    	    	
	    	    	//System.out.println("     gammaLines.size="+gammaLines.size());
	    	    	Gamma gamma=level.gammaAt(igamma);
	    	    	String MS=gamma.MS();
	    	    	    	    
	    	    	boolean hasRI=false;
	    	    	if(!gamma.IS().isEmpty() || ng==1)//assuming RI=100 if IS empty and only one branch
	    	    		hasRI=true;
	    	    	
	    	    	if(!hasRI || (!MS.contains("E")&&!MS.contains("M"))){
	    	    		newLines.addAll(gammaLines);
	    	    		reportLines.add("   ");
	    	    		reportLines.addAll(calcWrap.printGammaInfo(BXLCalc.branchAt(igamma), level, gamma));
	    	    		continue;
	    	    	}
	    	    	
	    	    	//make new ENSDF lines with new BXLW values
	    	    	Vector<String> newBXLWlinesV=calcWrap.makeNewBXLWlines(BXLCalc.branchAt(igamma),level,gamma);   
	    	    	
	    	    	String BXLWline=newBXLWlinesV.get(0);
	    	    	String symBXLWline=newBXLWlinesV.get(1);
	    	    	String BXLWline1=newBXLWlinesV.get(2);//use asymmetric uncertainty unless uncertainty difference==1, like 1234.5 +11-12 
	    	    	
	    	    	
                    Vector<String> oldBXLWlinesV=new Vector<String>();
	    	    	
                    int firstBXLWlineIndex=-1;
                   
	    	    	for(int m=0;m<gammaLines.size();m++){
	    	    		String gammaLine=gammaLines.get(m);
	    	    		
    	    			//debug
    	    			//System.out.println(" -1      m="+m+" level="+level.ES()+" gamma#="+igamma+" line="+gammaLine+" gammaLines.size()="+gammaLines.size()+" "+(gammaLine.charAt(5)!=' ')+" "+ (gammaLine.charAt(6)==' '));
	    	    		
	    	    		if(gammaLine.charAt(5)!=' ' && gammaLine.charAt(6)==' '){
	    	    			String s="";
	    	    			
	    	    			//s=EnsdfUtil.removeBXLW(gammaLine);
	    	    			s=BXLCalcWrap.removeBXLW(gammaLine);
	    	    			
	    	    			//debug
	    	    			//System.out.println(" 0      level="+level.ES()+" gamma#="+igamma+" line="+gammaLine+" gammaLines.size()="+gammaLines.size());
	    	    			
	    	    			if(!s.trim().equals(gammaLine.trim())){
	    	    				oldBXLWlinesV.add(gammaLine);
	    	    				gammaLine=s;
	    	    				if(firstBXLWlineIndex<0)
	    	    					firstBXLWlineIndex=newLines.size();
	    	    			}
	    	    			
	    	    			//debug
	    	    			//System.out.println(" 1      level="+level.ES()+" gamma#="+igamma+" line="+gammaLine);
	    	    		}
	    	    		
	    	    		if(gammaLine.length()>0){
	    	    			newLines.add(gammaLine);
	    	    			continue;
	    	    		}   	    			    	    		
	    	    		
	    	    	}
	    	    		    	    	
	    	    	//debug
	    	    	//System.out.println("       level="+level.ES()+" gamma#="+igamma+" line="+BXLWline+" firstBXLWlineIndex="+firstBXLWlineIndex);
    	    		    	    	
	    	    	if(BXLWline.length()>0){
	    	    		ngCalc+=1;
	    	    		
	    	    		if(firstBXLWlineIndex<0)
	    					firstBXLWlineIndex=newLines.size();
	    	    		
	    	    		if(Control.isUseSymmetrized)
	    	    			newLines.add(firstBXLWlineIndex,symBXLWline);
	    	    		else if(BXLWline.equals(BXLWline1))
	    	    			newLines.add(firstBXLWlineIndex,BXLWline);
	    	    		else
	    	    			newLines.add(firstBXLWlineIndex,BXLWline1);
	    	    	}
	    	    	
	    	    	reportLines.addAll(calcWrap.printBranchReportLines(BXLCalc.branchAt(igamma),level,gamma,oldBXLWlinesV,newBXLWlinesV));

	    	    }//end loop gamma    
	    	    
	    	    
	    	    nProblems+=calcWrap.nProblems();
	    	    
	    	    //print level information before gamma report lines in report file
	    	    
	    	    if(reportLines.size()>0){
	    	    	Vector<String> temp=new Vector<String>();
	    	    	
	    	    	temp.add(levelSeparator);
	    	    	
	    	    	temp.add(level.recordLine());
	    	    	for(int m=0;m<level.nGammas();m++){
	    	    		temp.add(level.gammaAt(m).recordLine());
	    	    	}
	    	    	
	    	    	reportLines.addAll(0,temp);
	    	    	reportLines.add("\n\n\n");
	    	    	
	    	    	Str.write(reportFile, reportLines);
	    	    	
	    	    	nlCalc++;
	    	    }
	    	    
	    	}//end loop level    
	    	
	    	if(ngCalc==0){
	    		printMessage(noCalcMsg);
	    		reportFile.write(noCalcMsg);
	    	}else{
	    		printMessage("Done");
	    	}
	    	
	    	Str.write(newFile, newLines);
	    }//end loop data block
	    
	    msg="\nDone calculations!\n";
	    
	    if(nProblems<=1)
	    	msg+=nProblems+" problem found.";
	    else
	    	msg+=nProblems+" problems found.";
	    

	    
	    reportFile.write(msg+"\n");
	    if(nProblems>0)
	    	msg+=" See report file for details.\n";
	    
	    printMessage(msg);
        printMessage("\n\n");
	    
	    newFile.close();
	    reportFile.close();

	}
	

	private void writeReportHead(PrintWriter out){
		String msg="Java RULER "+version()+" calculation report        generated at: "+date()+"\n";
		out.write(msg);
	}
	

	private String printUncertaintySettings(String indent){
		String out="\n";
		
		out+=indent+"assumptions for non-numerical uncertainties in RI:\n";
		out+=indent+"   "+String.format("%-15s    %-20s\n","DRI","assumed %DRI");
		out+=indent+"   "+String.format("%-15s    %-20s\n","-----------","------------");
		out+=indent+"   "+String.format("%-15s    %.0f\n","Empty",   100*UncertaintyParser.getDRIfactorForEmpty());
		out+=indent+"   "+String.format("%-15s    %.0f","LT or LE",100*UncertaintyParser.getDRIfactorForLT());
		out+=" for DRI(+); assumed RI=DRI(-)=%(100-this value)\n";
		out+=indent+"   "+String.format("%-15s    %.0f","GT or GE",100*UncertaintyParser.getDRIfactorForGT());
		out+=" for DRI(+); assumed DRI(-)=0\n";
		out+=indent+"   "+String.format("%-15s    %.0f\n","AP",      100*UncertaintyParser.getDRIfactorForAP());
		out+=indent+"   "+String.format("%-15s    %.0f\n","CA",      100*UncertaintyParser.getDRIfactorForCA());
		out+=indent+"   "+String.format("%-15s    %.0f\n","SY",      100*UncertaintyParser.getDRIfactorForSY());
		out+=indent+"   "+"(same for MR and CC except for empty DCC where theory DCC assumed)\n";
		return out;
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //general functions

	public String getOutfilePath(){return outfilepath;}
	public String getInfilePath(){return infilepath;}
	
	public void setOutfilePath(String s){outfilepath=s;}
	public void setInfilePath(String s){infilepath=s;}
	
    public void setMessenger(JTextArea m){
        messenger=m;    
        
        if(redirectToMessenger)
        	redirectOutput(true);
    }
    
    
    public void redirectOutput(boolean redirect){
    	if(!redirect){
    		System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
    		return;
    	}
    	
        PrintStream ps=new PrintStream(new OutputStream(){
			@Override
			public void write(int b) throws IOException {
				messenger.append(String.valueOf((char)b));
				
			}
        });
        
        System.setOut(ps);
    }
    
    
    public void printMessage(String message){

    	if(message==null)
    		return;
    	int len=newline.length();
    	if(!message.contains(newline) || message.lastIndexOf(newline)<message.length()-len)
    		message+=newline;
    			
		//System.out.println(" message="+message+" "+message.contains(newline)+" "+(message.indexOf(newline)<message.length()-1)+" newline="+newline.length());
		
    	printMessageAsIs(message);	
    }
    
    public void printMessageAsIs(String message){

    	if(message==null)
    		return;
    	  	
    	System.out.print(message);
    	   			
    	if(messenger!=null){	
    		   		
    		messenger.append(message);
    		messenger.setCaretPosition(messenger.getDocument().getLength());
    		messenger.update(messenger.getGraphics());
    		
    		try {
				messenger.scrollRectToVisible(messenger.modelToView(messenger.getDocument().getLength()));
			} catch (BadLocationException e) {
				System.out.println("Error");
				e.printStackTrace();
			}
    		

    		//THIS DOES NOT AUTOMATICALLY SCROLL TEXT 
    		//DefaultCaret caret = (DefaultCaret)messenger.getCaret();
    		//caret.setUpdatePolicy(DefaultCaret.OUT_BOTTOM);
    		//caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);    		
    		//messenger.setCaretPosition(messenger.getDocument().getLength());

    	}
    }
    
    public void clear() throws IOException{
  
        if(messenger!=null){
            messenger.setText("");
    		messenger.setCaretPosition(messenger.getDocument().getLength());
    		messenger.update(messenger.getGraphics());
        }

		
        if(outputStream!=null)
        	outputStream.close();

    }
        
    
    public boolean isWindows(){
        if(os.equals("windows"))
        	return true;
        
        return false;
    }
    
    public boolean isLinux(){
    	String name=System.getProperty("os.name").toLowerCase();
        if(name.contains("linux"))
        	return true;
        
        return false;
    }
    
    public boolean isMacOS(){
    	String name=System.getProperty("os.name").toLowerCase();
        if(name.contains("max"))
        	return true;
        
        return false;
    }
    
    public String date(){
        Date date=new Date();
        SimpleDateFormat sdf=new SimpleDateFormat("E MM/dd/yyyy 'at' hh:mm:ss a zzz");
        return sdf.format(date);
    }
    
    public String title(){
    	String out="";
    	out="**Calculation of BXLW in ENSDF datasets "+version()+"**";
    	return out;
    }
    public String version(){
        //Date date=new Date();
        //SimpleDateFormat sdf=new SimpleDateFormat("MM/dd/yyyy");
    	//return "Version 1.5: last update on "+sdf.format(date);
    	
    	return "(update 07/23/2018)";
    }      
    

}

