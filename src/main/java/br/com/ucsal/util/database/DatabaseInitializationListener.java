package br.com.ucsal.util.database;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class DatabaseInitializationListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("\nIniciando o banco de dados HSQLDB...");
        DatabaseUtil.iniciarBanco();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("\nAplicação sendo finalizada.");
    }
}