package admin;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableModel;
import main.MainWindow;
import main.TDBException;
import main.Trigramme;

public class AdminListDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    protected MainWindow parent;
    private AdminListDialogListener listener = new AdminListDialogListener();

    protected JButton creerButton;
    protected JButton modifierButton;
    protected JButton supprimerButton;
    protected JButton fermerButton;

    private JPanel fond;
    protected JTable listeAdmin;
    private DefaultTableModel modele = new DefaultTableModel() {
	private static final long serialVersionUID = 1L;

	public boolean isCellEditable(int rowIndex, int columnInder) {
	    return false;
	}
    };
    private JScrollPane resultatsScrollPane;

    public class AdminListDialogListener implements ActionListener, KeyListener {

	AdminListDialogListener() {
	    super();
	}

	public void keyPressed(KeyEvent arg0) {
	    if (arg0.getKeyChar() == KeyEvent.VK_ESCAPE) {
		dispose();
	    }
	}

	public void keyReleased(KeyEvent arg0) {}

	public void keyTyped(KeyEvent arg0) {}

	public void actionPerformed(ActionEvent arg0) {
	    try {
		if (arg0.getSource().equals(creerButton)) {
		    AdminCreationDialog dialog = new AdminCreationDialog(parent);
		    dialog.executer();
		} else if (arg0.getSource().equals(modifierButton)) {
		    int ligneChoisie = listeAdmin.getSelectedRow();
		    if (ligneChoisie == -1) { throw new TDBException("Pas d'admin sélectionné"); }
		    int permissions = 0;
		    String perms = (String) listeAdmin.getValueAt(ligneChoisie, 3);
		    if (perms.equals("BôBarman")) {
			permissions = 3;
		    } else if (perms.equals("Ex-BôBarman")) {
			permissions = 2;
		    } else if (perms.equals("Ami du BôB")) {
			permissions = 1;
		    } else {
			permissions = 0;
		    }
		    AdminModificationDialog dialog =
			    new AdminModificationDialog(parent, (String) listeAdmin.getValueAt(
				    ligneChoisie, 0), permissions);
		    dialog.executer();
		} else if (arg0.getSource().equals(supprimerButton)) {
		    int ligneChoisie = listeAdmin.getSelectedRow();
		    if (ligneChoisie == -1) { throw new TDBException("Pas d'admin sélectionné"); }
		    Trigramme trigramme =
			    new Trigramme(parent, (String) listeAdmin.getValueAt(ligneChoisie, 0));
		    int confirmation =
			    JOptionPane.showConfirmDialog(parent,
				    "Etes-vous sur de vouloir supprimer " + trigramme.trigramme
					    + " (" + trigramme.name + " " + trigramme.first_name
					    + ")", "Confirmation", JOptionPane.OK_CANCEL_OPTION,
				    JOptionPane.QUESTION_MESSAGE, null);
		    if (confirmation == JOptionPane.OK_OPTION) {
			Statement stmt = parent.connexion.createStatement();
			stmt.executeUpdate("DELETE FROM admins WHERE id=" + trigramme.id);
		    }
		} else if (arg0.getSource() == fermerButton) {
		    dispose();
		}
		refresh();
	    } catch (Exception e) {
		parent.afficherErreur(e);
	    }
	}
    }

    public AdminListDialog(MainWindow parent) {
	super(parent, "Administrateurs", true);
	this.parent = parent;
    }

    public void executer() throws Exception {

	AuthentificationDialog authentification = new AuthentificationDialog(parent);
	authentification.executer();

	if (authentification.droits == AuthentificationDialog.BoBarman) {
	    this.addKeyListener(listener);

	    listeAdmin = new JTable();
	    listeAdmin.addKeyListener(listener);
	    listeAdmin.setModel(modele);
	    listeAdmin.repaint();

	    resultatsScrollPane = new JScrollPane(listeAdmin);
	    resultatsScrollPane
		    .setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
	    resultatsScrollPane.setPreferredSize(new Dimension(400, 340));

	    String[] header = { "Trigramme", "Nom", "Prénom", "Statut" };
	    modele.setColumnIdentifiers(header);

	    listeAdmin.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    listeAdmin.setModel(modele);
	    listeAdmin.getColumnModel().getColumn(0).setPreferredWidth(65);
	    listeAdmin.getColumnModel().getColumn(1).setPreferredWidth(80);
	    listeAdmin.getColumnModel().getColumn(2).setPreferredWidth(80);
	    listeAdmin.getColumnModel().getColumn(3).setPreferredWidth(70);
	    listeAdmin.repaint();

	    listeAdmin.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

	    creerButton = new JButton("Créer un admin");
	    creerButton.setPreferredSize(new Dimension(220, 20));
	    creerButton.addActionListener(listener);

	    modifierButton = new JButton("Modifier l'admin");
	    modifierButton.setPreferredSize(new Dimension(220, 20));
	    modifierButton.addActionListener(listener);

	    supprimerButton = new JButton("Supprimer l'admin");
	    supprimerButton.setPreferredSize(new Dimension(220, 20));
	    supprimerButton.addActionListener(listener);

	    fermerButton = new JButton("Fermer la fenêtre");
	    fermerButton.setPreferredSize(new Dimension(220, 20));
	    fermerButton.addActionListener(listener);

	    fond = new JPanel();
	    fond.setLayout(new FlowLayout(FlowLayout.CENTER));
	    fond.add(resultatsScrollPane);
	    fond.add(creerButton);
	    fond.add(modifierButton);
	    fond.add(supprimerButton);
	    fond.add(fermerButton);

	    fond.setPreferredSize(new Dimension(410, 450));
	    fond.setOpaque(true);

	    Statement stmt = parent.connexion.createStatement();
	    ResultSet rs =
		    stmt.executeQuery("SELECT admins.id,permissions,trigramme,name,first_name FROM admins INNER JOIN accounts ON admins.id=accounts.id ORDER BY permissions DESC,name ASC");

	    while (rs.next()) {
		String perms = Trigramme.adminCategoriesList[rs.getInt("permissions")];
		String[] item =
			{ rs.getString("trigramme"), rs.getString("name"),
				rs.getString("first_name"), perms };
		modele.addRow(item);
	    }
	    listeAdmin.setModel(modele);

	    this.setContentPane(fond);
	    this.pack();
	    this.setLocation((parent.getWidth() - this.getWidth()) / 2,
		    (parent.getHeight() - this.getHeight()) / 2);
	    this.setResizable(false);

	    this.setVisible(true);
	}
    }

    // On crée une petite fonction refresh pour après les modifs
    public void refresh() throws Exception {
	for (int i = modele.getRowCount() - 1; i >= 0; i--) {
	    modele.removeRow(i);
	}
	Statement stmt = parent.connexion.createStatement();
	ResultSet rs =
		stmt.executeQuery("SELECT admins.id,permissions,trigramme,name,first_name FROM admins INNER JOIN accounts ON admins.id=accounts.id ORDER BY permissions DESC,name ASC");

	while (rs.next()) {
	    String perms = Trigramme.adminCategoriesList[rs.getInt("permissions")];
	    String[] item =
		    { rs.getString("trigramme"), rs.getString("name"), rs.getString("first_name"),
			    perms };
	    modele.addRow(item);
	}
	listeAdmin.setModel(modele);
	listeAdmin.repaint();
	resultatsScrollPane.repaint();
	this.repaint();

    }
}