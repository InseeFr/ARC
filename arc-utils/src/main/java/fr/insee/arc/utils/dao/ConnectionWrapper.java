package fr.insee.arc.utils.dao;

import java.io.Closeable;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import fr.insee.arc.utils.utils.LoggerDispatcher;

public class ConnectionWrapper implements Closeable {

    private static final Logger LOGGER = Logger.getLogger(ConnectionWrapper.class);

    private Connection connexion;
    private boolean isLocal = false;

    public ConnectionWrapper(boolean anIsLocal, Connection aConnexion) {
        this.isLocal = anIsLocal;
        this.connexion = aConnexion;
    }

    public boolean isLocal() {
        return this.isLocal;
    }

    public Connection getConnexion() {
        return this.connexion;
    }

    public void setConnexion(Connection connexion) {
        this.connexion = connexion;
    }

    public void close() {
        if (this.isLocal()) {
            try {
                if (this.connexion.isClosed()) {
                    return;
                }
                this.connexion.close();
                this.connexion = null;
            } catch (SQLException ex) {
            	LoggerDispatcher.error(ex,LOGGER);
            }
        }
    }
}
