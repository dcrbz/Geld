package bz.dcr.geld.data;

import bz.dcr.geld.api.Transaction;
import bz.dcr.geld.pin.RedeemablePin;
import bz.dcr.geld.pin.results.PinValidationResult;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class MySQL extends Database {

    private HikariDataSource dataSource;
    private HikariConfig cfg;
    private String tablePrefix;

    // Constructor
    public MySQL(HikariConfig cfg, String tablePrefix){
        this.cfg = cfg;
        this.tablePrefix = tablePrefix;
    }


    @Override
    public void init() {
        this.dataSource = new HikariDataSource(this.cfg);

        // Create tables
        PreparedStatement stmt = null;
        try (Connection con = this.dataSource.getConnection()){
            stmt = con.prepareStatement("CREATE TABLE IF NOT EXISTS gld_players (uuid BINARY(16) UNIQUE NOT NULL PRIMARY KEY, balance DECIMAL(32,2) NOT NULL DEFAULT 0.00, accept_transfers BOOL NOT NULL DEFAULT 1)");
            stmt.execute();
            stmt = con.prepareStatement("CREATE TABLE IF NOT EXISTS gld_transfers (id INT NOT NULL PRIMARY KEY AUTO_INCREMENT, sender BINARY(16) NOT NULL, target BINARY(16) NOT NULL, value DECIMAL(32,2) NOT NULL)");
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if(stmt != null)
                    stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void shutdown() {
        if(this.dataSource != null)
            this.dataSource.close();
    }


    @Override
    public void addPlayer(PlayerData data) {
        PreparedStatement stmt = null;
        try (Connection con = this.dataSource.getConnection()) {
            stmt = con.prepareStatement("INSERT IGNORE INTO gld_players (uuid,balance,accept_transfers) VALUES (UNHEX(?),?,?)");
            stmt.setString(1, data.getUniqueId().toString().replace("-", ""));
            stmt.setDouble(2, data.getBalance());
            stmt.setBoolean(3, data.doesAcceptTransfers());
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if(stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Optional<PlayerData> getPlayerData(UUID uuid) {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try (Connection con = this.dataSource.getConnection()) {
            stmt = con.prepareStatement("SELECT * FROM gld_players WHERE uuid=UNHEX(?) LIMIT 1");
            stmt.setString(1, uuid.toString().replace("-", ""));
            rs = stmt.executeQuery();

            if(!rs.next())
                return Optional.empty();

            // Get money
            final PlayerData data = new PlayerData(uuid);
            data.setBalance(rs.getDouble("balance"));
            data.setAcceptTransfer(rs.getBoolean("accept_transfers"));

            return Optional.of(data);
        } catch (SQLException e) {
            e.printStackTrace();
            return Optional.empty();
        } finally {
            try {
                if(stmt != null) stmt.close();
                if(rs != null) rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setPlayerData(PlayerData data) {
        PreparedStatement stmt = null;
        try (Connection con = this.dataSource.getConnection()) {
            stmt = con.prepareStatement("INSERT IGNORE INTO gld_players (uuid,balance,accept_transfers) VALUES (UNHEX(?),?,?) ON DUPLICATE KEY UPDATE balance=?,accept_transfers=?");
            stmt.setString(1, data.getUniqueId().toString().replace("-", ""));
            stmt.setDouble(2, data.getBalance());
            stmt.setBoolean(3, data.doesAcceptTransfers());
            stmt.setDouble(4, data.getBalance());
            stmt.setBoolean(5, data.doesAcceptTransfers());
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if(stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void addTransaction(Transaction transaction) {

    }

    @Override
    public List<Transaction> getTransactions(UUID uuid) {
        return new ArrayList<>();
    }

    @Override
    public void setAllPlayerData(Collection<PlayerData> data) {
        PreparedStatement stmt = null;
        try (Connection con = this.dataSource.getConnection()) {
            stmt = con.prepareStatement("INSERT IGNORE INTO gld_players (uuid,balance,accept_transfers) VALUES (UNHEX(?),?,?) ON DUPLICATE KEY UPDATE balance=?,accept_transfers=?");
            for(PlayerData playerData : data){
                stmt.setString(1, playerData.getUniqueId().toString().replace("-", ""));
                stmt.setDouble(2, playerData.getBalance());
                stmt.setBoolean(3, playerData.doesAcceptTransfers());
                stmt.setDouble(4, playerData.getBalance());
                stmt.setBoolean(5, playerData.doesAcceptTransfers());
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if(stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setAcceptTransfers(UUID player, boolean status) {
        PreparedStatement stmt = null;
        try (Connection con = this.dataSource.getConnection()) {
            stmt = con.prepareStatement("UPDATE gld_players SET accept_transfers=? WHERE uuid=UNHEX(?) LIMIT 1");
            stmt.setBoolean(1, status);
            stmt.setString(2, player.toString().replace("-", ""));
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if(stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean getAcceptTransfers(UUID player) {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try (Connection con = this.dataSource.getConnection()) {
            stmt = con.prepareStatement("SELECT accept_transfers FROM gld_players WHERE uuid=UNHEX(?) LIMIT 1");
            rs = stmt.executeQuery();

            return rs.next() && rs.getBoolean(1);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if(stmt != null) stmt.close();
                if(rs != null) rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    @Override
    public boolean hasPlayer(UUID uuid){
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try (Connection con = this.dataSource.getConnection()) {
            stmt = con.prepareStatement("SELECT balance FROM gld_players WHERE uuid=UNHEX(?) LIMIT 1");
            stmt.setString(1, uuid.toString().replace("-", ""));
            rs = stmt.executeQuery();

            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if(stmt != null) stmt.close();
                if(rs != null) rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public List<PlayerData> getTop(int amount) {
        return new ArrayList<>();
    }

    @Override
    public void addRedeemablePin(RedeemablePin pin) {

    }

    @Override
    public Optional<RedeemablePin> getRedeemablePin(String  pinCode) {
        return null;
    }

    @Override
    public PinValidationResult validatePin(String pinCode) {
        return null;
    }

}
