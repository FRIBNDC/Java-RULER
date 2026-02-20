package main;

import ensdf.*;
import nds.ensdf.MassChain;
import nds.latex.Translator;
import nds.util.Str;


import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import calc.BriccsWrap;


@SuppressWarnings("unused")
public class Main {

	public static void main(String[] args) throws Exception {
		Setup.load();
	    Translator.init();
	        
		//test();	
	    //test1();
		run();
		

	}
	
	
	static void run(){

        //launch master control panel
        try{
       	
        	UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        	//UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        	//UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        }catch(Exception e1){
            try{
                for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                	
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                    else{
                    	UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
                    }
                }
                
            }catch(Exception e2){
            	
            }        	
        }
        
        
        ui.MasterFrame frame=new ui.MasterFrame();
        frame.setTitle("Calculation of gamma transition strengths");
        frame.setVisible(true);
        frame.setResizable(false);
	}
	
	static void test1() throws Exception{
		
		String home="",wd="",briccs="";
		boolean isOffice=false;
		
		if(isOffice){
			boolean isLinux=false;
			if(isLinux){
				home="/user/chenj";
				wd=home+"/work/evaluation/mytools/myRULER/newRULER/bin/";	
				briccs=wd+"briccs";
			}else{
				home="H:";	
				wd=home+"\\work\\evaluation\\mytools\\myRULER\\newRULER\\bin\\";
				briccs=wd+"BrIccS.exe";		
			}

		}else{
			home="/home/junchen";
			wd=home+"/work/evaluation/mytools/myRULER/newRULER/bin/";	
			briccs=wd+"briccs";
		}

		
		//BriccsWrap.runCommand("export BrIccHome=/home/junchen/work/evaluation/ENSDF/tools/BRICC", null);
		String briccEnvName="BrIccHome";
		String[] envp={briccEnvName+"="+wd};
       
		System.out.println(" System="+System.getProperty("os.name").toLowerCase());

		//String command=briccs+" -Z 82 -g 1063 -e +3-2 -L M1+E2 -w BrIccFO";
        //String command=briccs+" -Z 44 -g 652.4 -e 0 -L M1 -w BrIccFO";
		
		//Vector<String> msgs=BriccsWrap.runCommand(command,envp,wd);
		//for(int i=0;i<msgs.size();i++)
		//	System.out.println(msgs.get(i));
		
		boolean isBriccsGood=BriccsWrap.checkInternalBrIccs(wd);
		
		//String[] out=BriccsWrap.getICCSByCommand(command);
		
		String[] out=BriccsWrap.getICCS(44, "652.4", "", "M1", "", "");
		System.out.println(" isBriccsGood="+isBriccsGood+" cc="+out[0]+"  dcc="+out[1]);
	}
	
	
	static void test(){
	
		
		/*
		BranchingCalculator calc=new BranchingCalculator();
		calc.addBranch("15", "1");
		calc.addBranch("12", "1");
		calc.addBranch("2", "0.3");
		calc.calculate();
		
		calc.printBranch(0);
		calc.printBranch(1);
		calc.printBranch(2);
		*/
		
		try{
			Run run=new Run();
			MassChain data=new MassChain();
			
			boolean isOffice=false;
			if(isOffice){
				Setup.outdir="H:\\work\\evaluation\\mytools\\myRULER\\newRULER\\test";
				data.load(new File(Setup.outdir+"\\test.ens"));	
			}else{
				Setup.outdir="/home/junchen/work/evaluation/mytools/myRULER/newRULER/test";
				data.load(new File(Setup.outdir+"/test.ens"));
			}
	
			
			System.out.println(" number of dataset="+data.nENSDF());
			
			run.setIsUseInputCC(false);
			run.setIsUseSymmetrized(false);
			//run.setIsSuppressBXLWLimit(true);
			
			
			run.calculateAndWriteBXLW(data, "ruler.out");	
			
		}catch(Exception e){
			e.printStackTrace();
		}
		

	}

}
