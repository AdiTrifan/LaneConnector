package org.openstreetmap.josm.plugins.laneconnector;

import java.awt.Component;
import java.util.Map;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;


public class SignListRenderer extends DefaultListCellRenderer {
	private final Map<String, ImageIcon> imageMap;
	
	public SignListRenderer(Map<String, ImageIcon> imageMap) {
		super();
		this.imageMap = imageMap;
		
	}	

	@SuppressWarnings("rawtypes")

	@Override

	public Component getListCellRendererComponent(

	JList list, Object value, int index,

	boolean isSelected, boolean cellHasFocus) {


	JLabel label = (JLabel) super.getListCellRendererComponent(

	list, value, index, isSelected, cellHasFocus);

	label.setIcon(imageMap.get((String) value));

	label.setHorizontalTextPosition(JLabel.RIGHT);


	return label;

	}

	
	
//	public Component getListCellRendererComponent(
//            JList list, Object value, int index,
//            boolean isSelected, boolean cellHasFocus) {
//		
//
//        JLabel label = (JLabel) super.getListCellRendererComponent(
//                list, value, index, isSelected, cellHasFocus);
//        
//        label.setIcon(imageMap.get((String) value));
//        label.setHorizontalTextPosition(JLabel.RIGHT);
//     
//        return label;
//        
//    }
}
