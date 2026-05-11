package org.example.utils;

import java.sql.Connection;

public class TestConnexion {
    public static void main(String[] args) {
        Connection connection = MyDataBase.getConnection();
        if (connection != null) {
            System.out.println("Test de connexion réussi!");
        } else {
            System.out.println("Test de connexion échoué!");
        }
    }
}