package org.openstreetmap.josm.plugins.laneconnector;

import javax.swing.Icon;
import javax.swing.UIManager;

public class Thumbnail {
	public final String name;
	  public final Icon icon;
	  public Thumbnail(String name) {
	    this.name = name;
	    this.icon = UIManager.getIcon("OptionPane."+name+"Icon");
	  }

}
