package main;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    Connection connexion;

    private String getExecutionPath() {
	String absolutePath =
		getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
	absolutePath = absolutePath.substring(0, absolutePath.lastIndexOf("/"));
	absolutePath = absolutePath.replaceAll("%20", "\\ "); // Surely need to do this here
	if(absolutePath.substring(absolutePath.length()-3).equals("bin")){
	    absolutePath+="/.."; //Hack sordide pour l'éxécution dans Eclipse
	}
	return absolutePath;
    }

    public void connecter() throws SQLException, ClassNotFoundException, IOException {
	Class.forName("com.mysql.jdbc.Driver");
	String user = "root";
	String passwd = "";
	String host = "localhost";
	String db = "tdb";
	String absolutePath = this.getExecutionPath();
	InputStream ips = new FileInputStream(absolutePath + "/src//TDB.config");
	InputStreamReader ipsr = new InputStreamReader(ips);
	BufferedReader br = new BufferedReader(ipsr);
	String ligne;
	while ((ligne = br.readLine()) != null) {
	    if (ligne.charAt(0) != '#') {
		int pos = ligne.indexOf('=');
		String debut = ligne.substring(0, pos).trim();
		String fin = ligne.substring(pos + 1, ligne.length()).trim();
		if (debut.equals("dbUser") && fin.length() > 0) {
		    user = fin;
		} else if (debut.equals("dbPassword") && fin.length() > 0) {
		    passwd = fin;
		} else if (debut.equals("dbHost") && fin.length() > 0) {
		    host = fin;
		} else if (debut.equals("dbName") && fin.length() > 0) {
		    db = fin;
		}
	    }
	}
	br.close();
	this.connexion =
		DriverManager.getConnection("jdbc:mysql://" + host + "/" + db, user, passwd);
    }

    public boolean deconnecter() throws SQLException {
	connexion.close();
	return true;
    }

}
