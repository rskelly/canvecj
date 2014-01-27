package ca.dijital.canvec.gui;

import java.awt.EventQueue;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JToolBar;

import java.awt.BorderLayout;
import javax.swing.JTabbedPane;
import javax.swing.JPanel;
import java.awt.GridBagLayout;
import javax.swing.JButton;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JList;
import javax.swing.JLabel;

import ca.dijital.canvec.ExtractorJob;
import ca.dijital.canvec.gui.JobDialog.JobDialogListener;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CanvecJ implements JobDialogListener {

    private JFrame frame;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
	EventQueue.invokeLater(new Runnable() {
	    public void run() {
		try {
		    CanvecJ window = new CanvecJ();
		    window.frame.setVisible(true);
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }
	});
    }

    /**
     * Create the application.
     */
    public CanvecJ() {
	extractorJobs = new DefaultListModel<ExtractorJob>();
	initialize();
    }

    DefaultListModel<ExtractorJob> extractorJobs;
    JobDialog dlgJob;
    JList<ExtractorJob> lstJobs;
    
    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {

	    dlgJob = new JobDialog();
	    dlgJob.setJobDialogListener(this);

	frame = new JFrame();
	frame.setBounds(100, 100, 450, 300);
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	JToolBar toolBar = new JToolBar();
	frame.getContentPane().add(toolBar, BorderLayout.NORTH);

	JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
	frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);

	JPanel panel = new JPanel();
	tabbedPane.addTab("Jobs", null, panel, null);
	GridBagLayout gbl_panel = new GridBagLayout();
	gbl_panel.columnWidths = new int[] { 0 };
	gbl_panel.rowHeights = new int[] { 0, 0, 0 };
	gbl_panel.columnWeights = new double[] { 1.0 };
	gbl_panel.rowWeights = new double[] { 0.0, 1.0, 0.0 };
	panel.setLayout(gbl_panel);

	JLabel lblJobs = new JLabel("Jobs");
	GridBagConstraints gbc_lblJobs = new GridBagConstraints();
	gbc_lblJobs.anchor = GridBagConstraints.WEST;
	gbc_lblJobs.insets = new Insets(0, 0, 5, 0);
	gbc_lblJobs.gridx = 0;
	gbc_lblJobs.gridy = 0;
	panel.add(lblJobs, gbc_lblJobs);

	lstJobs = new JList<ExtractorJob>();
	lstJobs.addMouseListener(new MouseAdapter() {
		@Override
		public void mouseClicked(MouseEvent e) {
		    doJobListClick();
		}
	});
	GridBagConstraints gbc_lstJobs = new GridBagConstraints();
	gbc_lstJobs.weighty = 1.0;
	gbc_lstJobs.insets = new Insets(0, 0, 5, 0);
	gbc_lstJobs.fill = GridBagConstraints.BOTH;
	gbc_lstJobs.gridx = 0;
	gbc_lstJobs.gridy = 1;
	panel.add(lstJobs, gbc_lstJobs);

	lstJobs.setModel(extractorJobs);

	JButton btnAddJob = new JButton("Add Job");
	btnAddJob.addMouseListener(new MouseAdapter() {
	    @Override
	    public void mouseClicked(MouseEvent e) {
		doAddJob();
	    }
	});
	GridBagConstraints gbc_btnAddJob = new GridBagConstraints();
	gbc_btnAddJob.insets = new Insets(0, 0, 5, 0);
	gbc_btnAddJob.gridx = 0;
	gbc_btnAddJob.gridy = 2;
	panel.add(btnAddJob, gbc_btnAddJob);
    }

    private void doJobListClick() {
	int idx = lstJobs.getSelectedIndex();
	ExtractorJob job = lstJobs.getModel().getElementAt(idx);
	doEditJob(job);
    }
    
    private void doEditJob(ExtractorJob job) {
	dlgJob.setExtractorJob(job);
	dlgJob.setVisible(true);
    }
    
    private void doAddJob() {
	dlgJob.setExtractorJob(new ExtractorJob());
	dlgJob.setVisible(true);
    }

    @Override
    public void jobDialogOk(JobDialog dlg) {
	ExtractorJob job = dlg.getExtractorJob();
	dlg.setVisible(false);
	extractorJobs.add(0, job);
    }

    @Override
    public void jobDialogCancel(JobDialog dlg) {
	dlg.setVisible(false);
    }

}
