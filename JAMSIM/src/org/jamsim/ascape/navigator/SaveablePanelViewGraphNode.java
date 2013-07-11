package org.jamsim.ascape.navigator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.File;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.ascape.runtime.swing.navigator.PanelViewNode;
import org.ascape.runtime.swing.navigator.PanelViewProvider;
import org.ascape.runtime.swing.navigator.PopupMenuProvider;
import org.jamsim.ascape.MicroSimScape;
import org.jamsim.ascape.r.PanelViewJGraphicsDevice;
import org.jamsim.ascape.r.ScapeRInterface;
import org.jamsim.ascape.ui.BringToFrontPanelViewNode;
import org.omancode.util.io.FileUtil;

/**
 * A {@link PanelViewNode} that takes a {@link PanelViewJGraphicsDevice} and displays the graph
 * which it provides. Provides a {@link JPopupMenu} that that saves the provided graph to the 
 * user's preferred format.
 * @author bmac055
 *
 */

public class SaveablePanelViewGraphNode extends BringToFrontPanelViewNode implements PopupMenuProvider {

	private PanelViewJGraphicsDevice provider;
	private MicroSimScape<?> scape;
	private ScapeRInterface rInterface;
	private String rPlotCmd;
	private String nodeName;
	
	private JMenuItem pdfItem;
	private JMenuItem jpegItem;
	private JMenuItem pngItem;
	private JMenuItem wmfItem;
	
	private final SaverAction saverAction;

	/**
	 * Creates a {@link SaveablePanelViewGraphNode}.
	 * 
	 * @param provider
	 * 			A {@link PanelViewJGraphicsDevice}.
	 * @param scape
	 * 			The {@link MicroSimScape}.
	 */
	public SaveablePanelViewGraphNode(PanelViewJGraphicsDevice provider, MicroSimScape scape) {
		super(provider);
		this.scape = scape;
		rInterface = provider.getRInterface();
		rPlotCmd = provider.getRPlotCmd();
		nodeName = provider.getName();
		saverAction  = new SaverAction();
	}
	
	/**
	 * Uses a {@link ScapeRInterface} and the rPlotCommand to create an R graphics device
	 * and then saves it to the desktop in the desired format.
	 * @param fileFormat
	 * 			The format specified by the user (eg. pdf, jpeg...)
	 * @param fileSuffix
	 * 			The suffix corresponding to the specified format.
	 * @throws IOException
	 */
	private void saveToDesktop(String fileFormat, String fileSuffix) throws IOException {
		String workingDirectory = rInterface.evalReturnString("getwd()");
		System.out.println(workingDirectory);
		rInterface.eval("setwd('"+ getDesktopPath() + "')");
		//System.out.println(getDesktopPath());
		rInterface.eval(fileFormat + "('" + nodeName + "." + fileSuffix + "')");
		rInterface.eval(rPlotCmd);
		rInterface.eval("dev.off()");
		rInterface.eval("setwd('"+ workingDirectory + "')");
	}
	
	/**
	 * @return
	 * 		A String representation of the absolute path to the Desktop 
	 * 		on the current system.
	 */
	private String getDesktopPath(){
		FileSystemView filesys = FileSystemView.getFileSystemView();
		String path = filesys.getHomeDirectory().toString();
		path = path.replace("\\", "/");
		return path;
	}
	
	/**
	 * Right click popup menu that allows saving of this node.
	 * 
	 * @return popup menu.
	 */
	public JPopupMenu getPopupMenu() {
		String desktopLabel = "Save to Desktop:";
		String pdfLabel = "Save as PDF";
		String jpegLabel = "Save as JPEG";
		String pngLabel = "Save as PNG";
		String wmfLabel = "Save as WMF (Windows only)";
		
		JPopupMenu popup = new JPopupMenu();
		
		JMenuItem desktopItem = new JMenuItem(desktopLabel);
		pdfItem = new JMenuItem(pdfLabel);
		jpegItem = new JMenuItem(jpegLabel);
		pngItem = new JMenuItem(pngLabel);
		wmfItem = new JMenuItem(wmfLabel);
		
		pdfItem.addActionListener(saverAction);
		jpegItem.addActionListener(saverAction);
		pngItem.addActionListener(saverAction);
		wmfItem.addActionListener(saverAction);

		popup.add(desktopItem);
		popup.add(pdfItem);
		popup.add(jpegItem);
		popup.add(pngItem);
		popup.add(wmfItem);
		
		return popup;
	}

	/**
	 * Action that saves this node to a file. 
	 */
	private class SaverAction implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			try {
				if (e.getSource().equals(pdfItem)){
					saveToDesktop("pdf", "pdf");
				} else if (e.getSource().equals(jpegItem)){
					saveToDesktop("jpeg", "jpeg");
				} else if (e.getSource().equals(pngItem)){
					saveToDesktop("png", "png");
				} else if (e.getSource().equals(wmfItem)){
					saveToDesktop("win.metafile", "wmf");
				}
			} catch (IOException e1) {
				throw new RuntimeException(e1);
			}
		}

	}
}
