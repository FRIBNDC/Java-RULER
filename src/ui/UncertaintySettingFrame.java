package ui;
import javax.swing.JLabel;
import javax.swing.JOptionPane;


import javax.swing.GroupLayout;

import javax.swing.GroupLayout.Alignment;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;

import calc.UncertaintyParser;

import javax.swing.JTextField;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class UncertaintySettingFrame extends javax.swing.JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JLabel topLabel;
	private JTextField emptyRITextField;
	private JTextField LTTextField;
	private JTextField GTTextField;
	private JTextField APTextField;
	private JTextField CATextField;
	private JTextField SYTextField;
	private JLabel lblEmpty;
	private JLabel lblAssumeddri;
	private JLabel DRItitle;
	private JLabel lblLT;
	private JLabel lblGT;
	private JLabel lblAP;
	private JLabel lblCA;
	private JLabel lblSY;
	private JPanel panel;
	
    public UncertaintySettingFrame() {
   	
        initComponents();        
    }
    
	private void initComponents() {
		setTitle("Uncertainty Settings");
		
    	topLabel = new JLabel("<HTML>assumption for<br>non-numerical uncertainty</HTML>");
    	
    	panel = new JPanel();
    	GroupLayout groupLayout = new GroupLayout(getContentPane());
    	groupLayout.setHorizontalGroup(
    		groupLayout.createParallelGroup(Alignment.LEADING)
    			.addGroup(groupLayout.createSequentialGroup()
    				.addGap(20)
    				.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
    					.addComponent(panel, GroupLayout.PREFERRED_SIZE, 207, GroupLayout.PREFERRED_SIZE)
    					.addComponent(topLabel, GroupLayout.PREFERRED_SIZE, 191, GroupLayout.PREFERRED_SIZE))
    				.addGap(16))
    	);
    	groupLayout.setVerticalGroup(
    		groupLayout.createParallelGroup(Alignment.LEADING)
    			.addGroup(groupLayout.createSequentialGroup()
    				.addGap(22)
    				.addComponent(topLabel)
    				.addPreferredGap(ComponentPlacement.UNRELATED)
    				.addComponent(panel, GroupLayout.PREFERRED_SIZE, 251, GroupLayout.PREFERRED_SIZE)
    				.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    	);
    	
    	DRItitle = new JLabel("DRI");
    	
    	lblAssumeddri = new JLabel("assumed %DRI");
    	
    	lblEmpty = new JLabel("empty");
    	lblEmpty.setToolTipText("no uncertainty given");
    	
    	emptyRITextField = new JTextField();
    	emptyRITextField.setText(Integer.toString((int)(100*UncertaintyParser.getDRIfactorForEmpty())));
    	emptyRITextField.addKeyListener(new KeyAdapter() {
    		@Override
    		public void keyReleased(KeyEvent arg0) {
    			RITextFieldKeyReleased(arg0);
    		}
    	});
    	emptyRITextField.setColumns(10);
    	
    	lblLT = new JLabel("LT or LE");
    	lblLT.setToolTipText("<HTML>less than; assumed uncertainty is for upper one and assumed<br>"
    			                 + "value as well as lower uncertainty will be original value minus<br>"
    			                 + "assumed upper uncertainty</HTML>");
    	
    	LTTextField = new JTextField();
    	LTTextField.setColumns(10);
    	LTTextField.setText(Integer.toString((int)(100*UncertaintyParser.getDRIfactorForLT())));
    	LTTextField.addKeyListener(new KeyAdapter() {
    		@Override
    		public void keyReleased(KeyEvent arg0) {
    			RITextFieldKeyReleased(arg0);
    		}
    	});
    	
    	lblGT = new JLabel("GT or GE");
    	lblGT.setToolTipText("<HTML>greater than; assumed uncertainty is for upper one and assumed<br>"
    			           + "lower uncertainty is set to be zero</HTML>");
    	
    	GTTextField = new JTextField();
    	GTTextField.setColumns(10);
    	GTTextField.setText(Integer.toString((int)(100*UncertaintyParser.getDRIfactorForGT())));
    	GTTextField.addKeyListener(new KeyAdapter() {
    		@Override
    		public void keyReleased(KeyEvent arg0) {
    			RITextFieldKeyReleased(arg0);
    		}
    	});
    	
    	lblAP = new JLabel("AP");
    	lblAP.setToolTipText("value is approximate");
    	
    	APTextField = new JTextField();
    	APTextField.setColumns(10);
    	APTextField.setText(Integer.toString((int)(100*UncertaintyParser.getDRIfactorForAP())));
    	APTextField.addKeyListener(new KeyAdapter() {
    		@Override
    		public void keyReleased(KeyEvent arg0) {
    			RITextFieldKeyReleased(arg0);
    		}
    	});
    	
    	lblCA = new JLabel("CA");
    	lblCA.setToolTipText("value is from theoretical calculation");
    	
    	CATextField = new JTextField();
    	CATextField.setColumns(10);
    	CATextField.setText(Integer.toString((int)(100*UncertaintyParser.getDRIfactorForCA())));
    	CATextField.addKeyListener(new KeyAdapter() {
    		@Override
    		public void keyReleased(KeyEvent arg0) {
    			RITextFieldKeyReleased(arg0);
    		}
    	});
    	
    	lblSY = new JLabel("SY");
    	lblSY.setToolTipText("value is from systematics");
    	
    	SYTextField = new JTextField();
    	SYTextField.setColumns(10);
    	SYTextField.setText(Integer.toString((int)(100*UncertaintyParser.getDRIfactorForSY())));
    	SYTextField.addKeyListener(new KeyAdapter() {
    		@Override
    		public void keyReleased(KeyEvent arg0) {
    			RITextFieldKeyReleased(arg0);
    		}
    	});
    	
    	
    	GroupLayout gl_panel = new GroupLayout(panel);
    	gl_panel.setHorizontalGroup(
    		gl_panel.createParallelGroup(Alignment.LEADING)
    			.addGroup(gl_panel.createSequentialGroup()
    				.addGap(34)
    				.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
    					.addGroup(gl_panel.createSequentialGroup()
    						.addGroup(gl_panel.createParallelGroup(Alignment.TRAILING)
    							.addComponent(DRItitle, GroupLayout.PREFERRED_SIZE, 54, GroupLayout.PREFERRED_SIZE)
    							.addComponent(lblEmpty, GroupLayout.PREFERRED_SIZE, 54, GroupLayout.PREFERRED_SIZE))
    						.addGap(23)
    						.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
    							.addGroup(gl_panel.createSequentialGroup()
    								.addComponent(lblAssumeddri, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
    								.addGap(24))
    							.addGroup(gl_panel.createSequentialGroup()
    								.addComponent(emptyRITextField, GroupLayout.PREFERRED_SIZE, 68, GroupLayout.PREFERRED_SIZE)
    								.addContainerGap())))
    					.addGroup(gl_panel.createSequentialGroup()
    						.addPreferredGap(ComponentPlacement.RELATED)
    						.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
    							.addGroup(gl_panel.createSequentialGroup()
    								.addComponent(lblGT, GroupLayout.PREFERRED_SIZE, 54, GroupLayout.PREFERRED_SIZE)
    								.addGap(23)
    								.addComponent(GTTextField, GroupLayout.PREFERRED_SIZE, 68, GroupLayout.PREFERRED_SIZE))
    							.addGroup(gl_panel.createSequentialGroup()
    								.addComponent(lblLT, GroupLayout.PREFERRED_SIZE, 54, GroupLayout.PREFERRED_SIZE)
    								.addGap(23)
    								.addComponent(LTTextField, GroupLayout.PREFERRED_SIZE, 68, GroupLayout.PREFERRED_SIZE))
    							.addGroup(gl_panel.createSequentialGroup()
    								.addComponent(lblAP, GroupLayout.PREFERRED_SIZE, 54, GroupLayout.PREFERRED_SIZE)
    								.addGap(23)
    								.addComponent(APTextField, GroupLayout.PREFERRED_SIZE, 68, GroupLayout.PREFERRED_SIZE))
    							.addGroup(gl_panel.createSequentialGroup()
    								.addComponent(lblCA, GroupLayout.PREFERRED_SIZE, 54, GroupLayout.PREFERRED_SIZE)
    								.addGap(23)
    								.addComponent(CATextField, GroupLayout.PREFERRED_SIZE, 68, GroupLayout.PREFERRED_SIZE))
    							.addGroup(gl_panel.createSequentialGroup()
    								.addComponent(lblSY, GroupLayout.PREFERRED_SIZE, 54, GroupLayout.PREFERRED_SIZE)
    								.addGap(23)
    								.addComponent(SYTextField, GroupLayout.PREFERRED_SIZE, 68, GroupLayout.PREFERRED_SIZE))))))
    	);
    	gl_panel.setVerticalGroup(
    		gl_panel.createParallelGroup(Alignment.LEADING)
    			.addGroup(gl_panel.createSequentialGroup()
    				.addContainerGap()
    				.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
    					.addComponent(DRItitle)
    					.addComponent(lblAssumeddri))
    				.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
    					.addGroup(gl_panel.createSequentialGroup()
    						.addGap(14)
    						.addComponent(lblEmpty))
    					.addGroup(gl_panel.createSequentialGroup()
    						.addPreferredGap(ComponentPlacement.UNRELATED)
    						.addComponent(emptyRITextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
    				.addPreferredGap(ComponentPlacement.RELATED)
    				.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
    					.addGroup(gl_panel.createSequentialGroup()
    						.addPreferredGap(ComponentPlacement.RELATED)
    						.addComponent(LTTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
    					.addGroup(gl_panel.createSequentialGroup()
    						.addGap(9)
    						.addComponent(lblLT)))
    				.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
    					.addGroup(gl_panel.createSequentialGroup()
    						.addGap(6)
    						.addComponent(GTTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
    					.addGroup(gl_panel.createSequentialGroup()
    						.addGap(9)
    						.addComponent(lblGT)))
    				.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
    					.addGroup(gl_panel.createSequentialGroup()
    						.addPreferredGap(ComponentPlacement.RELATED)
    						.addComponent(APTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
    					.addGroup(gl_panel.createSequentialGroup()
    						.addGap(9)
    						.addComponent(lblAP)))
    				.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
    					.addGroup(gl_panel.createSequentialGroup()
    						.addPreferredGap(ComponentPlacement.RELATED)
    						.addComponent(CATextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
    					.addGroup(gl_panel.createSequentialGroup()
    						.addGap(9)
    						.addComponent(lblCA)))
    				.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
    					.addGroup(gl_panel.createSequentialGroup()
    						.addPreferredGap(ComponentPlacement.RELATED)
    						.addComponent(SYTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
    					.addGroup(gl_panel.createSequentialGroup()
    						.addGap(9)
    						.addComponent(lblSY)))
    				.addContainerGap(19, Short.MAX_VALUE))
    	);
    	panel.setLayout(gl_panel);
    	getContentPane().setLayout(groupLayout);
    	
    	pack();
	}
	
	private void RITextFieldKeyReleased(KeyEvent arg0){
		JTextField tf=(JTextField)arg0.getSource();
        float x=0;
        try{
        	x=Float.parseFloat(tf.getText());
        	if(tf==this.emptyRITextField){
        		UncertaintyParser.setDRIfactorForEmpty(x);
        	}else if(tf==this.LTTextField){
        		UncertaintyParser.setDRIfactorForLT(x);
        	}else if(tf==this.GTTextField){
        		UncertaintyParser.setDRIfactorForGT(x);
        	}else if(tf==this.APTextField){
        		UncertaintyParser.setDRIfactorForAP(x);
        	}else if(tf==this.CATextField){
        		UncertaintyParser.setDRIfactorForCA(x);
        	}else if(tf==this.SYTextField){
        		UncertaintyParser.setDRIfactorForSY(x);
        	}		
        }catch(NumberFormatException e){
			JOptionPane.showMessageDialog(this,"Invalid input for %DRI !\nPlease try again!");
        }
		

	}
}
