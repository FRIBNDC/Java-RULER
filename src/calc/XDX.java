package calc;

public class XDX{
	public double x=-1;
	public double dxu=0;
	public double dxl=0;
	
	public XDX(){};
	
	public XDX(double x,double dxu,double dxl){
		this.x=x;
		this.dxu=dxu;
		this.dxl=dxl;
	}
	
	public XDX(double x,double dx){
		this(x,dx,dx);		
	}
	
	public double x(){return x;}
	public double dxl(){return dxl;}
	public double dxu(){return dxu;}
}
