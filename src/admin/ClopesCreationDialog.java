package admin;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.Statement;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import main.MainWindow;

public class ClopesCreationDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    MainWindow parent;
    ClopesCreationDialogListener listener = new ClopesCreationDialogListener();

    JTextField champMarque;
    JTextField champPrix;
    JButton okButton;
    JButton cancelButton;

    boolean validation = false;

    public class ClopesCreationDialogListener implements KeyListener, ActionListener {

	public ClopesCreationDialogListener() {
	    super();
	}

	public void keyPressed(KeyEvent arg0) {
	    if (arg0.getKeyChar() == KeyEvent.VK_ENTER) {
		validation = true;
		dispose();
	    } else if (arg0.getKeyChar() == KeyEvent.VK_ESCAPE) {
		validation = false;
		dispose();
	    }
	}

	public void keyReleased(KeyEvent arg0) {}

	public void keyTyped(KeyEvent arg0) {}

	public void actionPerformed(ActionEvent e) {
	    if (e.getSource().equals(okButton)) {
		validation = true;
		dispose();
	    } else if (e.getSource().equals(cancelButton)) {
		validation = false;
		dispose();
	    }

	}

    }

    ClopesCreationDialog(MainWindow parent) {
	super(parent, "Création de clopes", true);
	this.parent = parent;
    }

    public void executer() throws Exception {

	JLabel labelMarque = new JLabel("Marque : ");
	labelMarque.setPreferredSize(new Dimension(120, 20));

	champMarque = new JTextField();
	champMarque.setPreferredSize(new Dimension(150, 20));
	champMarque.addKeyListener(listener);

	JLabel labelPrix = new JLabel("Prix : ");
	labelPrix.setPreferredSize(new Dimension(120, 20));

	champPrix = new JTextField();
	champPrix.setPreferredSize(new Dimension(150, 20));
	champPrix.addKeyListener(listener);

	okButton = new JButton("Valider");
	okButton.addActionListener(listener);
	okButton.setPreferredSize(new Dimension(140, 20));

	cancelButton = new JButton("Annuler");
	cancelButton.addActionListener(listener);
	cancelButton.setPreferredSize(new Dimension(140, 20));

	JPanel pane = new JPanel();
	pane.add(labelMarque);
	pane.add(champMarque);
	pane.add(labelPrix);
	pane.add(champPrix);
	pane.add(okButton);
	pane.add(cancelButton);
	pane.setPreferredSize(new Dimension(300, 90));

	this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

	Container contentPane = this.getContentPane();
	contentPane.add(pane);
	this.pack();
	this.setLocation((parent.getWidth() - this.getWidth()) / 2,
		(parent.getHeight() - this.getHeight()) / 2);
	this.setResizable(false);
	this.setVisible(true);

	if (validation) {
	    // La, c'est le bordel, ca remet les majuscule au début des mots, et
	    // ca vire les virgules pour pas niquer les fichiers .csv
	    String marque = champMarque.getText().toLowerCase();
	    marque.replace(",", ";");
	    boolean majusculeSuivant = true;
	    for (int i = 0; i < marque.length(); i++) {
		if (majusculeSuivant) {
		    if (i != 0) {
			marque =
				marque.substring(0, i) + (char) (marque.charAt(i) - 32)
					+ marque.substring(i + 1);
		    } else {
			marque = (char) (marque.charAt(i) - 32) + marque.substring(i + 1);
		    }
		}
		if (marque.charAt(i) == ' ' || marque.charAt(i) == '-') {
		    majusculeSuivant = true;
		} else {
		    majusculeSuivant = false;
		}
	    }
	    try {
		Statement stmt = parent.connexion.createStatement();
		stmt.executeUpdate("INSERT INTO clopes (marque,prix,quantite) VALUES ('" + marque
			+ "'," + (int) (Math.round(100 * Double.parseDouble(champPrix.getText())))
			+ ",0)");
	    } catch (Exception e) {
		parent.afficherErreur(e);
	    }
	}
    }
}
