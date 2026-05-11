package org.example.utils;

import java.sql.Connection;
import java.sql.Statement;

public class CreateAvisTable {
    public static void main(String[] args) {
        try {
            Connection c = MyDataBase.getConnection();
            Statement s = c.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS avis (" +
                "id_avis INT AUTO_INCREMENT PRIMARY KEY, " +
                "id_therapeute INT NOT NULL, " +
                "id_utilisateur INT NOT NULL, " +
                "note INT CHECK (note BETWEEN 1 AND 5), " +
                "commentaire TEXT, " +
                "date_avis DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (id_therapeute) REFERENCES therapeute(id_therapeute) ON DELETE CASCADE, " +
                "FOREIGN KEY (id_utilisateur) REFERENCES utilisateur(id_utilisateur) ON DELETE CASCADE" +
                ")";
            s.execute(sql);
            System.out.println("Table 'avis' créée avec succès !");
            System.exit(0);
        } catch(Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
