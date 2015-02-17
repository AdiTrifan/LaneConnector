package org.openstreetmap.josm.plugins.laneconnector;

import static org.openstreetmap.josm.tools.I18n.tr;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Shape;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.DefaultListCellRenderer;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.Scrollable;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.omg.CORBA.PRIVATE_MEMBER;
import org.omg.CORBA.portable.IndirectionException;
import org.openstreetmap.josm.Main;
import org.openstreetmap.josm.command.ChangePropertyCommand;
import org.openstreetmap.josm.command.Command;
import org.openstreetmap.josm.command.SequenceCommand;
import org.openstreetmap.josm.data.osm.DataSet;
import org.openstreetmap.josm.data.osm.Node;
import org.openstreetmap.josm.data.osm.OsmPrimitive;
import org.openstreetmap.josm.data.osm.Relation;
import org.openstreetmap.josm.data.osm.Tag;
import org.openstreetmap.josm.data.osm.Way;
import org.openstreetmap.josm.gui.ExtendedDialog;
import org.openstreetmap.josm.gui.tagging.TagEditorModel;
import org.openstreetmap.josm.gui.tagging.ac.AutoCompletionListItem;
import org.openstreetmap.josm.gui.widgets.JosmTextField;
import org.openstreetmap.josm.tools.GBC;

import com.sun.prism.Image;
//import com.sun.xml.internal.ws.api.Component;


import sun.nio.cs.ext.IBM037;

@SuppressWarnings("serial")



public class LaneRelationsDialog extends ExtendedDialog {
	private  Map<String, ImageIcon> imageMap;
	private static final String[] BUTTON_TEXTS = new String[] { tr("OK") };
	private static final String[] BUTTON_ICONS = new String[] { "ok.png" };
	

	String[] labels = { "Street name: ", "Number of lanes: ",
			"Lanes forward: ", "Lanes backward: " };
	int numPairs = labels.length;
	List<String> values = Arrays.asList("pedestrian", "footway", "cycleway", "bridleway", "steps", "path", "track", "bus_stop","crossing", "services", "service");

	private JLabel strtname = new JLabel(tr("Street name: "));
	private JLabel lanesnr = new JLabel(tr("Number of lanes: "));
	private JLabel lanesfrwd = new JLabel(tr("Lanes forward: "));
	private JLabel lanesbckwd = new JLabel(tr("Lanes backward: "));
	private JButton btnAdd, btnEdit;
	JosmTextField txtData = new JosmTextField();
	JosmTextField sname = new JosmTextField();
	private static boolean hasChanged = false;
	
	

	
	//modificare
	JFrame newFrame = new JFrame("intersection info");
	JPanel infoPanel = new JPanel();
	JTextArea infoArea = new JTextArea();
	JLabel labelArea = new JLabel();
	JButton infoBtn = new JButton("Info");
	JScrollPane infoScroll = new JScrollPane();
	
	ImageIcon[] images;
	String[] signStrings = {"a","b","c","d","e"};
	
	
	public static boolean getHasChanged() {
		return hasChanged;
	}

	public void setHasChanged(boolean hasChanged) {
		this.hasChanged = hasChanged;
	}

	JosmTextField snameTextFields[];
	JosmTextField lnrTextFields[];
	JosmTextField lfrwdTextFields[];
	JosmTextField lbckwdTextFields[];
	DataSet d;
	ArrayList<Way> drum = new ArrayList<Way>();
	int row = 0;

	TagEditorModel model = new TagEditorModel();
	JTextField lan = new JTextField();

	// Graphics
	BufferedImage img;
	Graphics g2d, g, g1, g2;
	Point startDrag, endDrag;
	Shape r = null;
	MyGraphics mg = new MyGraphics();
	MyGraphicsRestrictions mgr = new MyGraphicsRestrictions();

//	 File statText = new File("E:" + "\\" + "statsTest.txt");
//	 File stateText = new File(System.getProperty("user.home"), "oana.txt");
 
	String filename = "";
	public static Map<String, String> starting_point = new HashMap<String, String>();
	public static Map<String, String> ending_point = new HashMap<String, String>();

	Collection<OsmPrimitive> sel;
	// private static List<OsmPrimitive> lop = new ArrayList<OsmPrimitive>();
	private static int panel_index;

	private Relation junction = null;
	private static Node selectedNode = null;
	private static ArrayList<Way> junctionWays = null;

	public static ArrayList<Way> getJunctionWays() {
		return junctionWays;
	}

	public static Node getSelectedNode() {
		return selectedNode;
	}

	Comparator<AutoCompletionListItem> defaultACItemComparator = new Comparator<AutoCompletionListItem>() {
		@Override
		public int compare(AutoCompletionListItem o1, AutoCompletionListItem o2) {
			return String.CASE_INSENSITIVE_ORDER.compare(o1.getValue(),
					o2.getValue());
		}
	};

	public static final int DEFAULT_LRU_TAGS_NUMBER = 5;
	public static final int MAX_LRU_TAGS_NUMBER = 30;

	// LRU cache for recently added tags
	// (http://java-planet.blogspot.com/2005/08/how-to-set-up-simple-lru-cache-using.html)
	private final Map<Tag, Void> recentTags = new LinkedHashMap<Tag, Void>(
			MAX_LRU_TAGS_NUMBER + 1, 1.1f, true) {
		@Override
		protected boolean removeEldestEntry(Map.Entry<Tag, Void> eldest) {
			return size() > MAX_LRU_TAGS_NUMBER;
		}
	};
	

	public LaneRelationsDialog() {
		
		
		
		
		super(Main.parent, tr("Edit lanes"), BUTTON_TEXTS);
		if (findAndSaveJunction()) {
			setButtonIcons(BUTTON_ICONS);
			JPanel fr = new JPanel();
			final JTabbedPane tabs = new JTabbedPane();

			snameTextFields = new JosmTextField[junctionWays.size()];
			lnrTextFields = new JosmTextField[junctionWays.size()];
			lfrwdTextFields = new JosmTextField[junctionWays.size()];
			lbckwdTextFields = new JosmTextField[junctionWays.size()];
			
	
			for (panel_index = 0; panel_index < junctionWays.size(); panel_index++) {
				// JComponent panel = makePanel("Panel " + (panel_index + 1));
				JPanel panel = new JPanel();
				panel.setLayout(new GridBagLayout());
				final int i = panel_index;

				// Street name
				snameTextFields[i] = new JosmTextField();
				strtname = new JLabel(tr("Street name: "), JLabel.TRAILING);
				if ((junctionWays.get(i)).hasKey("name")) {
					btnEdit = new JButton("Edit");
					btnEdit.setEnabled(false);
					panel.add(btnEdit);
					panel.add(strtname);
					strtname.setLabelFor(snameTextFields[i]);
					snameTextFields[i].setText(junctionWays.get(i).getName());
					snameTextFields[i].setEnabled(false);
					panel.add(snameTextFields[i], GBC.eol()
							.fill(GBC.HORIZONTAL));
				} else {
					btnAdd = new JButton("Add");
					btnAdd.setEnabled(true);
					btnAdd.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent arg0) {
							String values = snameTextFields[i].getText();
							addTag(junctionWays, i, "name", values);
						}
					});
					panel.add(btnAdd);
					panel.add(strtname);
					strtname.setLabelFor(snameTextFields[i]);
					panel.add(snameTextFields[i], GBC.eol()
							.fill(GBC.HORIZONTAL));
				}

				// Lanes nr
				lnrTextFields[i] = new JosmTextField();
				lanesnr = new JLabel(tr("Number of lanes: "), JLabel.TRAILING);

				if ((junctionWays.get(i)).hasKey("lanes")) {
					btnEdit = new JButton("Edit");
					btnEdit.setEnabled(true);
					btnEdit.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent arg0) {
							String values = lnrTextFields[i].getText();
							editTag(junctionWays, i, "lanes", values);
						}
					});
					panel.add(btnEdit);
					panel.add(lanesnr);
					lanesnr.setLabelFor(lnrTextFields[i]);
					lnrTextFields[i].setText(junctionWays.get(i).get("lanes"));
					panel.add(lnrTextFields[i], GBC.eol().fill(GBC.HORIZONTAL));
				} else {
					btnAdd = new JButton("Add");
					btnAdd.setEnabled(true);
					btnAdd.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent arg0) {
							String values = lnrTextFields[i].getText();
							addTag(junctionWays, i, "lanes", values);
						}
					});
				
					panel.add(btnAdd);
					panel.add(lanesnr);
					lanesnr.setLabelFor(lnrTextFields[i]);
					panel.add(lnrTextFields[i], GBC.eol().fill(GBC.HORIZONTAL));
				}

				// Lanes forward
				lfrwdTextFields[i] = new JosmTextField();
				lanesfrwd = new JLabel(tr("Lanes forward: "), JLabel.TRAILING);
				if ((junctionWays.get(i)).hasKey("lanes:forward")) {
					btnEdit = new JButton("Edit");
					btnEdit.setEnabled(true);
					btnEdit.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent arg0) {
							String values = lfrwdTextFields[i].getText();
							editTag(junctionWays, i, "lanes:forward", values);
						}
					});
				
					panel.add(btnEdit);
					panel.add(lanesfrwd);
					lanesfrwd.setLabelFor(lfrwdTextFields[i]);
					lfrwdTextFields[i].setText(junctionWays.get(i).get("lanes:forward"));
					panel.add(lfrwdTextFields[i], GBC.eol()
							.fill(GBC.HORIZONTAL));
				} else {
					btnAdd = new JButton("Add");
					btnAdd.setEnabled(true);
					btnAdd.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent arg0) {
							String values = lfrwdTextFields[i].getText();
							addTag(junctionWays, i, "lanes:forward", values);
						}
					});
					
					panel.add(btnAdd);
					panel.add(lanesfrwd);
					lanesfrwd.setLabelFor(lfrwdTextFields[i]);
					panel.add(lfrwdTextFields[i], GBC.eol()
							.fill(GBC.HORIZONTAL));
				}

				// Lanes backward
				lbckwdTextFields[i] = new JosmTextField();
				lanesbckwd = new JLabel(tr("Lanes backward: "), JLabel.TRAILING);
				if ((junctionWays.get(i)).hasKey("lanes:backward")) {
					btnEdit = new JButton("Edit");
					btnEdit.setEnabled(true);
					btnEdit.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent arg0) {
							String values = lbckwdTextFields[i].getText();
							editTag(junctionWays, i, "lanes:backward", values);
						}
					});
					panel.add(btnEdit);
					panel.add(lanesbckwd);
					lanesbckwd.setLabelFor(lbckwdTextFields[i]);
					lbckwdTextFields[i].setText(junctionWays.get(i).get(
							"lanes:backward"));
					panel.add(lbckwdTextFields[i],
							GBC.eol().fill(GBC.HORIZONTAL));
				} else {
					btnAdd = new JButton("Add");
					btnAdd.setEnabled(true);
					btnAdd.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent arg0) {
							String values = lbckwdTextFields[i].getText();
							addTag(junctionWays, i, "lanes:backward", values);
						}
					});
					panel.add(btnAdd);
					panel.add(lanesbckwd);
					lanesbckwd.setLabelFor(lbckwdTextFields[i]);
					panel.add(lbckwdTextFields[i],
							GBC.eol().fill(GBC.HORIZONTAL));
				}

				panel.setPreferredSize(new Dimension(700, 500));
				tabs.addTab(tr((junctionWays.get(panel_index)).getName()),
						panel);
			}

			// graphical and interactive part
			JPanel panelview = new JPanel();
			panelview.setLayout(new GridBagLayout());
			panelview.add(mg = new MyGraphics());
			JButton btnSave = new JButton("Save");

//			//adding jlist witn icons
			String[] nameList = {"dreapta_sus" ,"inainte_dreapta","intoarcere_stanga_jos", "stanga_sus", "turn_left", "turn_right", "uturn_left", "uturn_right", "y_intersection", "y_intersection_intors" };//trebuie puse numele pozelor!!
	        imageMap = createImageMap(nameList);
	        JList list = new JList(nameList);
	        list.setCellRenderer(new SignListRenderer(imageMap));
	        JScrollPane scroll = new JScrollPane(list);
	        scroll.setPreferredSize(new Dimension(300, 400));
	        panelview.add(scroll);
	        panelview.setVisible(true);
	        
	     
//	 	   DefaultListModel<Thumbnail> m = new DefaultListModel<>();
//
//		   for(String s: Arrays.asList("dreapta_sus" ,"inainte_dreapta","intoarcere_stanga_jos", "stanga_sus", "turn_left", "turn_right", "uturn_left", "uturn_right", "y_intersection", "y_intersection_intors")){
//			   m.addElement(new Thumbnail(s));
//		   }
		   //list = namelist
//		   JList<Thumbnail> namelist = new JList<>(m);
//		   namelist.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
//		   namelist.setTransferHandler(new ListItemTransferHandler());
//		   namelist.setDropMode(DropMode.INSERT);
//		   namelist.setDragEnabled(true);
//		    
//		    namelist.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		   
		    
		
			
			
			panelview.add(infoBtn);
			infoBtn.setVisible(true);
			infoArea.setBounds(200,400,1000,1000);
			infoArea.setColumns(20);
			infoArea.setLineWrap(true);
			infoArea.setRows(5);
			infoArea.setWrapStyleWord(true);
			
			infoScroll = new JScrollPane(infoArea);
			

			
			infoBtn.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					// TODO Auto-generated method stub
					System.out.println("Info button pressed");
					
			
					newFrame.add(infoArea);
					newFrame.add(labelArea);
					newFrame.add(infoPanel);
					newFrame.setVisible(true);
					infoArea.setVisible(true);
					labelArea.setVisible(true);
					infoArea.setLineWrap(true);
					infoArea.setBorder(BorderFactory.createEmptyBorder(100,100,100,100));
			
					
				}
			});
			
		
			
				btnSave.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					String fname = String.valueOf(selectedNode.getId());
					mg.doSaveToFile(fname);
					JOptionPane.showMessageDialog(Main.parent,
							tr("Connectors successfully saved!"),
							tr("Information"), JOptionPane.INFORMATION_MESSAGE);
				}
			});
			panelview.add(btnSave);
			JButton btnUndo = new JButton("Undo");
			btnUndo.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					mg.doUndo();
				}
			});
			panelview.add(btnUndo);
			JButton btnClear = new JButton("Clear");
			btnClear.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					mg.doClear();
				}
			});
			panelview.add(btnClear);
			tabs.addTab(tr("Junction View"), panelview);

			tabs.getModel().addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent e) {
				}
			});

			// graphical and interactive part
			JPanel panelviewRestrictions = new JPanel();
			panelviewRestrictions.setLayout(new GridBagLayout());
			panelviewRestrictions.add(mgr = new MyGraphicsRestrictions());
			JButton btnSaveR = new JButton("Save");
			

			newFrame.setSize(1000,500);
			panelview.add(infoBtn);
			
			
			
			btnSaveR.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					String fname = String.valueOf(selectedNode.getId());
					mgr.doSaveToFile(fname);
					JOptionPane.showMessageDialog(Main.parent,
							tr("Connectors successfully saved!"),
							tr("Information"), JOptionPane.INFORMATION_MESSAGE);
				}
			});
			panelviewRestrictions.add(btnSaveR);
			JButton btnUndoR = new JButton("Undo");
			btnUndoR.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					mgr.doUndo();
				}
			});
			panelviewRestrictions.add(btnUndoR);
			JButton btnClearR = new JButton("Clear");
			btnClearR.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					mgr.doClear();
				}
			});
			panelviewRestrictions.add(btnClearR);
			tabs.addTab(tr("Junction View Restrictions"), panelviewRestrictions);

			tabs.getModel().addChangeListener(new ChangeListener() {

				@Override
				public void stateChanged(ChangeEvent e) {
				}
			});

			fr.add(tabs);
			setContent(fr);
			setupDialog();
			setResizable(false);
			showDialog();
		}
		
	
	}
	
	//here is the magic Jlist with icons elements
	 private Map<String, ImageIcon> createImageMap(String[] list) {
	        Map<String, ImageIcon> map = new HashMap<>();
	        	for (String s : list) {
//	        		InputStream stream = getClass().getResourceAsStream("/images/signs/" +  s + ".png");
//	        		InputStream stream = getClass().getResourceAsStream("/");
//	        		System.out.println(getClass().getResource("/images"));
//	        		System.out.println("Current Stream : " + stream);
//	     System.out.println( getClass().getResource("images/dreapta_sus.png"));
	     
	        	System.out.println("s =" );
	        	System.out.println(s);
	      

	            map.put(s, new ImageIcon(
            		 getClass().getResource("images/" +  s + ".png")));
	          
	        }
	        return map;
	    }

	public boolean findAndSaveJunction() {

		boolean validJunction = false;

		DataSet currentDataSet = Main.main.getCurrentDataSet();
		if (currentDataSet != null) {
			for (OsmPrimitive osm : currentDataSet.getSelectedNodes()) {
				junction = new Relation();
				junction.removeAll();
				Node node = (Node) osm;
				List<Way> drum = new ArrayList<Way>();
				ArrayList<Way> drum_bun = new ArrayList<Way>();
				drum.addAll(currentDataSet.getWays());
				for (Way way : drum) {
					if (way instanceof Way && way.isArea() == false
							&& way.containsNode(node)
							&& way.hasTag("highway", values) == false
							&& way.hasKey("building") == false) {
						drum_bun.add(way);
					}
				}
				if (drum_bun.size() >= 2) {
					this.selectedNode = (Node) osm;
					this.junctionWays = drum_bun;
					validJunction = true;
					String.valueOf(node.getId());

				}
			}
		}
		return validJunction;
	}

	protected JComponent makePanel(String text) {
		JPanel panel = new JPanel(false);
		JLabel filler = new JLabel(text);
		filler.setHorizontalAlignment(JLabel.CENTER);
		panel.setLayout(new GridBagLayout());
		// panel.add(filler);
		return panel;
	}

	public static boolean isNumeric(String str) {
		for (char c : str.toCharArray()) {
			if (!Character.isDigit(c))
				return false;
		}
		return true;
	}
	
	
	
	
	

	public void addTag(ArrayList<Way> w, int index, String tagKey,
			String valueKey) {
		if (w.get(index) == null)
			return;
		String key = Tag.removeWhiteSpaces(tagKey);
		String value = Tag.removeWhiteSpaces(valueKey);
			
		
		if (key.isEmpty() || value.isEmpty())
			return;

		if (!isNumeric(value)) {
			JOptionPane.showMessageDialog(Main.parent,
					tr("Please insert a valid number!"), tr("Error"),
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		if (tagKey.equals("lanes:forward")) {
			
			int nrlanes = 0, nrlanesbk = 0;
			if (w.get(index).hasKey("lanes")) {
				nrlanes = Integer.parseInt(w.get(index).get("lanes"));
				System.out.println("total number of lanes" + nrlanes);
				
			}
			if (w.get(index).hasKey("lanes:backward")) {
				nrlanesbk = Integer
						.parseInt(w.get(index).get("lanes:backward"));
				System.out.println("======lanes backward " + nrlanesbk);
			}

			if (Integer.parseInt(valueKey) > nrlanes || nrlanesbk > nrlanes
					|| (Integer.parseInt(valueKey) + nrlanesbk) > nrlanes) {
				JOptionPane
				.showMessageDialog(
						Main.parent,
						tr("Please make sure that the number of lanes matches the number of lanes forward and/or backward!"),
						tr("Error"), JOptionPane.ERROR_MESSAGE);
				return;
			}
			
		
		}
		
		

		if (tagKey.equals("lanes:backward")) {
			int nrlanes = 0, nrlanesfr = 0;
			if (w.get(index).hasKey("lanes")) {
				nrlanes = Integer.parseInt(w.get(index).get("lanes"));
				System.out.println("index " + index);
		}
			if (w.get(index).hasKey("lanes:forward")) {
				nrlanesfr = Integer.parseInt(w.get(index).get("lanes:forward"));
				
			}
			
			//display number of lanes backward -> we don't have a variable 
			int lanebktest= nrlanes - nrlanesfr;
		
		
			
			if (Integer.parseInt(valueKey) > nrlanes || nrlanesfr > nrlanes
					|| (Integer.parseInt(valueKey) + nrlanesfr) != nrlanes) {
				JOptionPane
				.showMessageDialog(
						Main.parent,
						tr("Please make sure that the number of lanes matches the number of lanes forward and/or backward!"),
						tr("Error"), JOptionPane.ERROR_MESSAGE);
				return;
			}
			//infoPanel
			for(int i= index; i<=index;i++)
			{
				String strArray[] = new String[10];
				String[] valueArray = new String[10];
				String[] lanesArray = {"lanes forward ","lanes backward" };
				int lanesback = 0;
				
				valueArray[i]= String.valueOf(value);
				strArray[i] = junctionWays.get(i).getName();
				
				
				infoArea.append("total number of lanes for street " + strArray[i] + " is " + nrlanes);
				infoArea.append("\n");
				infoArea.append("for street " +strArray[i]+ " we have " + nrlanesfr + " lanes forward");
				infoArea.append("\n");
		
				lanesback = nrlanes - nrlanesfr;
				infoArea.append("for street "+strArray[i]+ " we have " + lanesback + " lanes backward");
				infoArea.append("\n");
			
			}
		}
		
	

			
				


		//}

		/*
		 * if (tagKey.equals("lanes")) { int nrlanesbk = 0, nrlanesfr = 0; if
		 * (w.get(index).hasKey("lanes:backward")) { nrlanesbk = Integer
		 * .parseInt(w.get(index).get("lanes:backward")); } if
		 * (w.get(index).hasKey("lanes:forward")) { nrlanesfr =
		 * Integer.parseInt(w.get(index).get("lanes:forward")); }
		 * 
		 * if (Integer.parseInt(valueKey) < nrlanesbk ||
		 * Integer.parseInt(valueKey) < nrlanesfr || Integer.parseInt(valueKey)
		 * != (nrlanesfr + nrlanesbk)) { JOptionPane .showMessageDialog(
		 * Main.parent, tr(
		 * "Please make sure that the number of lanes matches the number of lanes forward and/or backward!"
		 * ), tr("Error"), JOptionPane.ERROR_MESSAGE); return; } }
		 */

		recentTags.put(new Tag(key, value), null);
		
	

		
		Command c = new ChangePropertyCommand(w.get(index), key, value);
		c.executeCommand();
		mg.doClear();
		mgr.doClear();
	}


	public void editTag(ArrayList<Way> w, int index, String tagKey,
			String valueKey) {
		String key = Tag.removeWhiteSpaces(tagKey);
		String value = Tag.removeWhiteSpaces(valueKey);
		value = Normalizer.normalize(value, java.text.Normalizer.Form.NFC);
		if (value.isEmpty()) {
			return;
		}

		if (!isNumeric(value)) {
			JOptionPane.showMessageDialog(Main.parent,
					tr("Please insert a valid number!"), tr("Error"),
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		if (tagKey.equals("lanes:forward")) {
			int nrlanes = 0, nrlanesbk = 0;
			if (w.get(index).hasKey("lanes")) {
				nrlanes = Integer.parseInt(w.get(index).get("lanes"));
			}
			if (w.get(index).hasKey("lanes:backward")) {
				nrlanesbk = Integer
						.parseInt(w.get(index).get("lanes:backward"));
			}

			if (Integer.parseInt(valueKey) > nrlanes || nrlanesbk > nrlanes
					|| (Integer.parseInt(valueKey) + nrlanesbk) > nrlanes) {
				JOptionPane
				.showMessageDialog(
						Main.parent,
						tr("Please make sure that the number of lanes matches the number of lanes forward and/or backward!"),
						tr("Error"), JOptionPane.ERROR_MESSAGE);
				return;
			}
		}

		if (tagKey.equals("lanes:backward")) {
			int nrlanes = 0, nrlanesfr = 0;
			if (w.get(index).hasKey("lanes")) {
				nrlanes = Integer.parseInt(w.get(index).get("lanes"));
			
				infoArea.setVisible(true);
			}
			if (w.get(index).hasKey("lanes:forward")) {
				nrlanesfr = Integer.parseInt(w.get(index).get("lanes:forward"));
				
			}

			if (Integer.parseInt(valueKey) > nrlanes || nrlanesfr > nrlanes
					|| (Integer.parseInt(valueKey) + nrlanesfr) != nrlanes) {
				JOptionPane
				.showMessageDialog(
						Main.parent,
						tr("Please make sure that the number of lanes matches the number of lanes forward and/or backward!"),
						tr("Error"), JOptionPane.ERROR_MESSAGE);
				return;
			}
		}

		/*
		 * if (tagKey.equals("lanes")) { int nrlanesbk = 0, nrlanesfr = 0; if
		 * (w.get(index).hasKey("lanes:backward")) { nrlanesbk = Integer
		 * .parseInt(w.get(index).get("lanes:backward")); } if
		 * (w.get(index).hasKey("lanes:forward")) { nrlanesfr =
		 * Integer.parseInt(w.get(index).get("lanes:forward")); }
		 * 
		 * if (Integer.parseInt(valueKey) < nrlanesbk ||
		 * Integer.parseInt(valueKey) < nrlanesfr || Integer.parseInt(valueKey)
		 * != (nrlanesfr + nrlanesbk)) { JOptionPane .showMessageDialog(
		 * Main.parent, tr(
		 * "Please make sure that the number of lanes matches the number of lanes forward and/or backward!"
		 * ), tr("Error"), JOptionPane.ERROR_MESSAGE); return; } }
		 */

		if (value == null) {
			Command c = new ChangePropertyCommand(w.get(index), key, value);
			c.executeCommand();
		} else {

			if ((w.get(index)).get(key) != null) {
				ExtendedDialog ed = new ExtendedDialog(Main.parent,
						tr("Overwrite key"), new String[] { tr("Replace"),
					tr("Cancel") });
				ed.setButtonIcons(new String[] { "purge", "cancel" });
				ed.setContent(tr(
						"You changed the key from ''{0}'' to ''{1}''.\n"
								+ "The new key is already used, overwrite values?",
								key, key));
				ed.setCancelButton(2);
				ed.toggleEnable("overwriteEditKey");
				ed.showDialog();

				if (ed.getValue() != 1)
					return;
			}

			
			
			Collection<Command> commands = new ArrayList<Command>();
			commands.add(new ChangePropertyCommand(w.get(index), key, null));
			if (value.equals(tr("<different>"))) {
				Map<String, List<OsmPrimitive>> map = new HashMap<String, List<OsmPrimitive>>();

				String val = (w.get(index)).get(key);
				if (val != null) {
					if (map.containsKey(val)) {
						map.get(val).add(w.get(index));
					} else {
						List<OsmPrimitive> v = new ArrayList<OsmPrimitive>();
						v.add(w.get(index));
						map.put(val, v);
					}
				}

				for (Map.Entry<String, List<OsmPrimitive>> e : map.entrySet()) {
					commands.add(new ChangePropertyCommand(e.getValue(), key, e
							.getKey()));
				}
			} else {
				commands.add(new ChangePropertyCommand(w.get(index), key, value));
			}

			Command sc = new SequenceCommand(tr(
					"Change properties of up to {0} object",
					"Change properties of up to {0} objects", 1, 1), commands);
			sc.executeCommand();
		}

		mg.doClear();
		mgr.doClear();

	}

}
