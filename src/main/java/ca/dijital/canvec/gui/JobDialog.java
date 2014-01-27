package ca.dijital.canvec.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JCheckBox;
import javax.swing.JTextField;

import ca.dijital.canvec.ExtractorJob;
import ca.dijital.canvec.gui.FeatureIDDialog.FeatureIDDialogListener;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;

public class JobDialog extends JDialog implements FeatureIDDialogListener {

    /**
     * 
     */
    private static final long serialVersionUID = -8480688160666161052L;
    private final JPanel contentPanel = new JPanel();
    private JTextField txtQueryString;
    private JTextField txtTargetFolder;
    private JTextField txtSQLFile;
    private JTextField txtDatabaseName;
    private JTextField txtSchemaName;
    private JTextField txtTableName;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
	try {
	    JobDialog dialog = new JobDialog();
	    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	    dialog.setVisible(true);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    /**
     * Create the dialog.
     */
    public JobDialog() {
	setTitle("Add/Edit an Extractor Job");
	setSize(550, 400);
	getContentPane().setLayout(new BorderLayout());
	contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
	getContentPane().add(contentPanel, BorderLayout.CENTER);
	GridBagLayout gbl_contentPanel = new GridBagLayout();
	gbl_contentPanel.columnWeights = new double[] { 0.0, 1.0, 0.0 };
	gbl_contentPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0,
		0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
	contentPanel.setLayout(gbl_contentPanel);
	{
	    JLabel lblQueryString = new JLabel("Query String");
	    GridBagConstraints gbc_lblQueryString = new GridBagConstraints();
	    gbc_lblQueryString.insets = new Insets(0, 0, 5, 5);
	    gbc_lblQueryString.anchor = GridBagConstraints.EAST;
	    gbc_lblQueryString.gridx = 0;
	    gbc_lblQueryString.gridy = 1;
	    contentPanel.add(lblQueryString, gbc_lblQueryString);
	}
	{
	    txtQueryString = new JTextField();
	    GridBagConstraints gbc_txtQueryString = new GridBagConstraints();
	    gbc_txtQueryString.insets = new Insets(0, 0, 5, 5);
	    gbc_txtQueryString.fill = GridBagConstraints.HORIZONTAL;
	    gbc_txtQueryString.gridx = 1;
	    gbc_txtQueryString.gridy = 1;
	    contentPanel.add(txtQueryString, gbc_txtQueryString);
	    txtQueryString.setColumns(10);
	}
	{
	    JButton btnFeatureId = new JButton("Feature IDs...");
	    btnFeatureId.addMouseListener(new MouseAdapter() {
		@Override
		public void mouseClicked(MouseEvent e) {
		    doSelectFeatureIDs();
		}
	    });
	    GridBagConstraints gbc_btnFeatureId = new GridBagConstraints();
	    gbc_btnFeatureId.anchor = GridBagConstraints.WEST;
	    gbc_btnFeatureId.insets = new Insets(0, 0, 5, 0);
	    gbc_btnFeatureId.gridx = 2;
	    gbc_btnFeatureId.gridy = 1;
	    contentPanel.add(btnFeatureId, gbc_btnFeatureId);
	}
	{
	    JLabel lblTargetFolder = new JLabel("Target Folder");
	    GridBagConstraints gbc_lblTargetFolder = new GridBagConstraints();
	    gbc_lblTargetFolder.insets = new Insets(0, 0, 5, 5);
	    gbc_lblTargetFolder.anchor = GridBagConstraints.EAST;
	    gbc_lblTargetFolder.gridx = 0;
	    gbc_lblTargetFolder.gridy = 2;
	    contentPanel.add(lblTargetFolder, gbc_lblTargetFolder);
	}
	{
	    txtTargetFolder = new JTextField();
	    GridBagConstraints gbc_txtTargetFolder = new GridBagConstraints();
	    gbc_txtTargetFolder.insets = new Insets(0, 0, 5, 5);
	    gbc_txtTargetFolder.fill = GridBagConstraints.HORIZONTAL;
	    gbc_txtTargetFolder.gridx = 1;
	    gbc_txtTargetFolder.gridy = 2;
	    contentPanel.add(txtTargetFolder, gbc_txtTargetFolder);
	    txtTargetFolder.setColumns(10);
	}
	{
	    JButton btnChoose = new JButton("Choose...");
	    btnChoose.addMouseListener(new MouseAdapter() {
		@Override
		public void mouseClicked(MouseEvent e) {
		    doFolderChooser();
		}
	    });
	    GridBagConstraints gbc_btnChoose = new GridBagConstraints();
	    gbc_btnChoose.anchor = GridBagConstraints.WEST;
	    gbc_btnChoose.insets = new Insets(0, 0, 5, 0);
	    gbc_btnChoose.gridx = 2;
	    gbc_btnChoose.gridy = 2;
	    contentPanel.add(btnChoose, gbc_btnChoose);
	}
	{
	    JLabel lblSqlFile = new JLabel("SQL File");
	    GridBagConstraints gbc_lblSqlFile = new GridBagConstraints();
	    gbc_lblSqlFile.insets = new Insets(0, 0, 5, 5);
	    gbc_lblSqlFile.anchor = GridBagConstraints.EAST;
	    gbc_lblSqlFile.gridx = 0;
	    gbc_lblSqlFile.gridy = 3;
	    contentPanel.add(lblSqlFile, gbc_lblSqlFile);
	}
	{
	    txtSQLFile = new JTextField();
	    GridBagConstraints gbc_txtSQLFile = new GridBagConstraints();
	    gbc_txtSQLFile.insets = new Insets(0, 0, 5, 5);
	    gbc_txtSQLFile.fill = GridBagConstraints.HORIZONTAL;
	    gbc_txtSQLFile.gridx = 1;
	    gbc_txtSQLFile.gridy = 3;
	    contentPanel.add(txtSQLFile, gbc_txtSQLFile);
	    txtSQLFile.setColumns(10);
	}
	{
	    JButton btnChoose_1 = new JButton("File...");
	    btnChoose_1.addMouseListener(new MouseAdapter() {
		@Override
		public void mouseClicked(MouseEvent e) {
		    doSqlFileChooser();
		}
	    });
	    GridBagConstraints gbc_btnChoose_1 = new GridBagConstraints();
	    gbc_btnChoose_1.anchor = GridBagConstraints.WEST;
	    gbc_btnChoose_1.insets = new Insets(0, 0, 5, 0);
	    gbc_btnChoose_1.gridx = 2;
	    gbc_btnChoose_1.gridy = 3;
	    contentPanel.add(btnChoose_1, gbc_btnChoose_1);
	}
	{
	    JLabel lblDatabaseName = new JLabel("Database Name");
	    GridBagConstraints gbc_lblDatabaseName = new GridBagConstraints();
	    gbc_lblDatabaseName.anchor = GridBagConstraints.EAST;
	    gbc_lblDatabaseName.insets = new Insets(0, 0, 5, 5);
	    gbc_lblDatabaseName.gridx = 0;
	    gbc_lblDatabaseName.gridy = 4;
	    contentPanel.add(lblDatabaseName, gbc_lblDatabaseName);
	}
	{
	    txtDatabaseName = new JTextField();
	    GridBagConstraints gbc_txtDatabaseName = new GridBagConstraints();
	    gbc_txtDatabaseName.insets = new Insets(0, 0, 5, 5);
	    gbc_txtDatabaseName.fill = GridBagConstraints.HORIZONTAL;
	    gbc_txtDatabaseName.gridx = 1;
	    gbc_txtDatabaseName.gridy = 4;
	    contentPanel.add(txtDatabaseName, gbc_txtDatabaseName);
	    txtDatabaseName.setColumns(10);
	}
	{
	    JLabel lblSchemaName = new JLabel("Schema Name");
	    GridBagConstraints gbc_lblSchemaName = new GridBagConstraints();
	    gbc_lblSchemaName.anchor = GridBagConstraints.EAST;
	    gbc_lblSchemaName.insets = new Insets(0, 0, 5, 5);
	    gbc_lblSchemaName.gridx = 0;
	    gbc_lblSchemaName.gridy = 5;
	    contentPanel.add(lblSchemaName, gbc_lblSchemaName);
	}
	{
	    txtSchemaName = new JTextField();
	    GridBagConstraints gbc_txtSchemaName = new GridBagConstraints();
	    gbc_txtSchemaName.insets = new Insets(0, 0, 5, 5);
	    gbc_txtSchemaName.fill = GridBagConstraints.HORIZONTAL;
	    gbc_txtSchemaName.gridx = 1;
	    gbc_txtSchemaName.gridy = 5;
	    contentPanel.add(txtSchemaName, gbc_txtSchemaName);
	    txtSchemaName.setColumns(10);
	}
	{
	    JLabel lblTableName = new JLabel("Table Name");
	    GridBagConstraints gbc_lblTableName = new GridBagConstraints();
	    gbc_lblTableName.anchor = GridBagConstraints.EAST;
	    gbc_lblTableName.insets = new Insets(0, 0, 5, 5);
	    gbc_lblTableName.gridx = 0;
	    gbc_lblTableName.gridy = 6;
	    contentPanel.add(lblTableName, gbc_lblTableName);
	}
	{
	    txtTableName = new JTextField();
	    GridBagConstraints gbc_txtTableName = new GridBagConstraints();
	    gbc_txtTableName.insets = new Insets(0, 0, 5, 5);
	    gbc_txtTableName.fill = GridBagConstraints.HORIZONTAL;
	    gbc_txtTableName.gridx = 1;
	    gbc_txtTableName.gridy = 6;
	    contentPanel.add(txtTableName, gbc_txtTableName);
	    txtTableName.setColumns(10);
	}
	{
	    JLabel lblSrid = new JLabel("SRID");
	    GridBagConstraints gbc_lblSrid = new GridBagConstraints();
	    gbc_lblSrid.anchor = GridBagConstraints.EAST;
	    gbc_lblSrid.insets = new Insets(0, 0, 5, 5);
	    gbc_lblSrid.gridx = 0;
	    gbc_lblSrid.gridy = 7;
	    contentPanel.add(lblSrid, gbc_lblSrid);
	}
	{
	    txtSRID = new JTextField();
	    txtSRID.setText("4326");
	    GridBagConstraints gbc_txtSRID = new GridBagConstraints();
	    gbc_txtSRID.anchor = GridBagConstraints.WEST;
	    gbc_txtSRID.insets = new Insets(0, 0, 5, 5);
	    gbc_txtSRID.gridx = 1;
	    gbc_txtSRID.gridy = 7;
	    contentPanel.add(txtSRID, gbc_txtSRID);
	    txtSRID.setColumns(10);
	}
	{
	    JCheckBox chkCompressOutput = new JCheckBox("Compress Output");
	    GridBagConstraints gbc_chkCompressOutput = new GridBagConstraints();
	    gbc_chkCompressOutput.anchor = GridBagConstraints.WEST;
	    gbc_chkCompressOutput.insets = new Insets(0, 0, 5, 5);
	    gbc_chkCompressOutput.gridx = 1;
	    gbc_chkCompressOutput.gridy = 8;
	    contentPanel.add(chkCompressOutput, gbc_chkCompressOutput);
	}
	{
	    JCheckBox chkDeleteTemp = new JCheckBox("Delete Temp Files");
	    GridBagConstraints gbc_chkDeleteTemp = new GridBagConstraints();
	    gbc_chkDeleteTemp.anchor = GridBagConstraints.WEST;
	    gbc_chkDeleteTemp.insets = new Insets(0, 0, 5, 5);
	    gbc_chkDeleteTemp.gridx = 1;
	    gbc_chkDeleteTemp.gridy = 9;
	    contentPanel.add(chkDeleteTemp, gbc_chkDeleteTemp);
	}
	{
	    JLabel lblNtsTiles = new JLabel("NTS Tiles");
	    GridBagConstraints gbc_lblNtsTiles = new GridBagConstraints();
	    gbc_lblNtsTiles.insets = new Insets(0, 0, 0, 5);
	    gbc_lblNtsTiles.anchor = GridBagConstraints.EAST;
	    gbc_lblNtsTiles.gridx = 0;
	    gbc_lblNtsTiles.gridy = 10;
	    contentPanel.add(lblNtsTiles, gbc_lblNtsTiles);
	}
	{
	    txtNTSTiles = new JTextField();
	    GridBagConstraints gbc_txtNTSTiles = new GridBagConstraints();
	    gbc_txtNTSTiles.insets = new Insets(0, 0, 0, 5);
	    gbc_txtNTSTiles.fill = GridBagConstraints.HORIZONTAL;
	    gbc_txtNTSTiles.gridx = 1;
	    gbc_txtNTSTiles.gridy = 10;
	    contentPanel.add(txtNTSTiles, gbc_txtNTSTiles);
	    txtNTSTiles.setColumns(10);
	}
	{
	    JButton btnChoose_2 = new JButton("Choose...");
	    btnChoose_2.addMouseListener(new MouseAdapter() {
		@Override
		public void mouseClicked(MouseEvent e) {
		    doNtsTileChooser();
		}
	    });
	    GridBagConstraints gbc_btnChoose_2 = new GridBagConstraints();
	    gbc_btnChoose_2.anchor = GridBagConstraints.WEST;
	    gbc_btnChoose_2.gridx = 2;
	    gbc_btnChoose_2.gridy = 10;
	    contentPanel.add(btnChoose_2, gbc_btnChoose_2);
	}
	{
	    JPanel buttonPane = new JPanel();
	    buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
	    getContentPane().add(buttonPane, BorderLayout.SOUTH);
	    {
		JButton okButton = new JButton("OK");
		okButton.addMouseListener(new MouseAdapter() {
		    @Override
		    public void mouseClicked(MouseEvent e) {
			doOk();
		    }
		});
		okButton.setActionCommand("OK");
		buttonPane.add(okButton);
		getRootPane().setDefaultButton(okButton);
	    }
	    {
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addMouseListener(new MouseAdapter() {
		    @Override
		    public void mouseClicked(MouseEvent e) {
			doCancel();
		    }
		});
		cancelButton.setActionCommand("Cancel");
		buttonPane.add(cancelButton);
	    }
	}
    }

    private void doFolderChooser() {
	JFileChooser jfc = new JFileChooser();
	jfc.setDialogType(JFileChooser.OPEN_DIALOG);
	jfc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	int ret = jfc.showDialog(this, "Choose");
	if (ret == JFileChooser.APPROVE_OPTION) {
	    File folder = jfc.getSelectedFile();
	    txtTargetFolder.setText(folder.getAbsolutePath());
	}
    }

    private void doNtsTileChooser() {

    }

    private void doSqlFileChooser() {
	JFileChooser jfc = new JFileChooser();
	jfc.setDialogType(JFileChooser.SAVE_DIALOG);
	jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
	int ret = jfc.showSaveDialog(this);
	if (ret == JFileChooser.APPROVE_OPTION) {
	    File folder = jfc.getSelectedFile();
	    txtSQLFile.setText(folder.getAbsolutePath());
	}
    }

    private void doSelectFeatureIDs() {
	FeatureIDDialog fid = new FeatureIDDialog();
	fid.setFeatureIDDialogListener(this);
	fid.setVisible(true);
    }

    /**
     * Returns the instance of the {@link ExtractorJob} managed by this dialog.
     * 
     * @return An {@link ExtractorJob}.
     */
    public ExtractorJob getExtractorJob() {
	return ed;
    }

    /**
     * Sets the values in the form to those contained in the
     * {@link ExtractorJob}.
     * 
     * @param job
     *            An {@link ExtractorJob}.
     */
    public void setExtractorJob(ExtractorJob job) {
	this.ed = job;
	txtQueryString.setText(ed.getPattern());
	txtSQLFile.setText(ed.getOutFile());
	txtSchemaName.setText(ed.getSchemaName());
	txtTableName.setText(ed.getTableName());
	txtDatabaseName.setText(ed.getDatabaseName());
	txtSRID.setText(Integer.toString(ed.getSrid()));
	txtTargetFolder.setText(ed.getTempDir());
    }

    private JobDialogListener listener;
    private JTextField txtNTSTiles;
    private JTextField txtSRID;

    public void setJobDialogListener(JobDialogListener listener) {
	this.listener = listener;
    }

    private ExtractorJob ed;
    
    public void doOk() {
	ed = new ExtractorJob();
	ed.setPattern(txtQueryString.getText());
	ed.setOutFile(txtSQLFile.getText());
	ed.setSchemaName(txtSchemaName.getText());
	ed.setTableName(txtTableName.getText());
	ed.setDatabaseName(txtDatabaseName.getText());
	ed.setSrid(Integer.parseInt(txtSRID.getText()));
	ed.setTempDir(txtTargetFolder.getText());
	try {
	    ed.validate();
	    if (listener != null)
		listener.jobDialogOk(this);
	} catch (Exception e) {
	    JOptionPane.showMessageDialog(this, e.getMessage(), "Error",
		    JOptionPane.ERROR_MESSAGE);
	}
    }

    public void doCancel() {
	if (listener != null)
	    listener.jobDialogCancel(this);
    }

    /**
     * Provides a listener for objects that manage a job dialog.
     * 
     * @author rob
     */
    public static interface JobDialogListener {

	void jobDialogOk(JobDialog dlg);

	void jobDialogCancel(JobDialog dlg);

    }

    @Override
    public void featureIDDialogOk(FeatureIDDialog dlg) {
	List<String> ids = dlg.getSelectedFeatureIds();
	if (ids.size() > 0) {
	    StringBuilder buf = new StringBuilder();
	    for (String id : ids)
		buf.append("|").append(id);
	    txtQueryString.setText("(" + buf.toString().substring(1) + ")");
	}
	featureIDDialogCancel(dlg);
    }

    @Override
    public void featureIDDialogCancel(FeatureIDDialog dlg) {
	dlg.setVisible(false);
	dlg.dispose();
    }

    @Override
    public void dispose() {
	setJobDialogListener(null);
	super.dispose();
    }
}
