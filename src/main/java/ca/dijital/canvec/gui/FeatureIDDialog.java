package ca.dijital.canvec.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.border.EmptyBorder;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import java.awt.GridBagConstraints;
import javax.swing.JList;

import ca.dijital.canvec.FeatureType;

import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class FeatureIDDialog extends JDialog {

    /**
     * 
     */
    private static final long serialVersionUID = 1862711451172911364L;
    private final JPanel contentPanel = new JPanel();

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
	try {
	    FeatureIDDialog dialog = new FeatureIDDialog();
	    dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	    dialog.setVisible(true);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    JList<FeatureType> lstFeatures;

    /**
     * Create the dialog.
     */
    public FeatureIDDialog() {
	setBounds(100, 100, 450, 300);
	getContentPane().setLayout(new BorderLayout());
	contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
	getContentPane().add(contentPanel, BorderLayout.CENTER);
	GridBagLayout gbl_contentPanel = new GridBagLayout();
	gbl_contentPanel.columnWidths = new int[] { 0, 0 };
	gbl_contentPanel.rowHeights = new int[] { 0, 0, 0 };
	gbl_contentPanel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
	gbl_contentPanel.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
	contentPanel.setLayout(gbl_contentPanel);
	{
	    JLabel lblSelectFeatures = new JLabel("Select Features");
	    GridBagConstraints gbc_lblSelectFeatures = new GridBagConstraints();
	    gbc_lblSelectFeatures.anchor = GridBagConstraints.WEST;
	    gbc_lblSelectFeatures.insets = new Insets(0, 0, 5, 0);
	    gbc_lblSelectFeatures.gridx = 0;
	    gbc_lblSelectFeatures.gridy = 0;
	    contentPanel.add(lblSelectFeatures, gbc_lblSelectFeatures);
	}
	{
	    JScrollPane scrFeatures = new JScrollPane();
	    lstFeatures = new JList<FeatureType>();
	    GridBagConstraints gbc_lstFeatures = new GridBagConstraints();
	    gbc_lstFeatures.fill = GridBagConstraints.BOTH;
	    gbc_lstFeatures.gridx = 0;
	    gbc_lstFeatures.gridy = 1;
	    scrFeatures.setViewportView(lstFeatures);
	    contentPanel.add(scrFeatures, gbc_lstFeatures);
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
	try {
	    List<FeatureType> types = FeatureType.loadFromFile(new File(
		    "conf/feature_types"));
	    DefaultListModel<FeatureType> model = new DefaultListModel<FeatureType>();
	    for (FeatureType type : types)
		model.addElement(type);
	    lstFeatures.setModel(model);
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    /**
     * Returns a {@link List} of selected feature IDs.
     * 
     * @return A {@link List} of selected feature IDs.
     */
    public List<String> getSelectedFeatureIds() {
	List<String> ret = new ArrayList<String>();
	ListModel<FeatureType> types = lstFeatures.getModel();
	int size = types.getSize();
	for (int i = 0; i < size; ++i) {
	    if (lstFeatures.isSelectedIndex(i))
		ret.add(types.getElementAt(i).getId());
	}
	return ret;
    }

    FeatureIDDialogListener listener;

    public void setFeatureIDDialogListener(FeatureIDDialogListener listener) {
	this.listener = listener;
    }

    public void doOk() {
	if (listener != null)
	    listener.featureIDDialogOk(this);
    }

    public void doCancel() {
	if (listener != null)
	    listener.featureIDDialogCancel(this);
    }

    /**
     * Provides a listener for objects that manage a job dialog.
     * 
     * @author rob
     */
    public static interface FeatureIDDialogListener {

	void featureIDDialogOk(FeatureIDDialog dlg);

	void featureIDDialogCancel(FeatureIDDialog dlg);

    }

    @Override
    public void dispose() {
	setFeatureIDDialogListener(null);
	super.dispose();
    }

}
