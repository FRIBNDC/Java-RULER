package calc;

public class BXLWResult {

	public XDX normalErrorBXLW;//result with uncertainty from normal error propagation
	public XDX extremeErrorBXLW;//result with uncertainty from min and max of each variable
	public boolean canUseNormalError=false;
	
	public XDX normalErrorBranching;//gamma branching ratio
	public XDX extremeErrorBranching;
	
	public XDX normalErrorTotalBranching;//gamma+ce branching ratio
	public XDX extremeErrorTotalBranching;
	
	public XDX normalErrorPartialT;//partial halflife
	public XDX extremeErrorPartialT;
	
	public BXLWResult(){
		normalErrorBXLW=new XDX(0,0);
		extremeErrorBXLW=new XDX(0,0);
		canUseNormalError=false;
		
		normalErrorBranching=new XDX(0,0);
		extremeErrorBranching=new XDX(0,0);
		
		normalErrorTotalBranching=new XDX(0,0);
		extremeErrorTotalBranching=new XDX(0,0);
		
		normalErrorPartialT=new XDX(0,0);
		extremeErrorPartialT=new XDX(0,0);
	}
}
