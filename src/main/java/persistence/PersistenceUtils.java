package persistence;

import block.Block;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

public class PersistenceUtils {

    private static final String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS block (\n"
            + " hash text PRIMARY KEY,\n"
            + " transaction_content text NOT NULL\n"
            + ");";
    private static final String INSERT_SQL = "INSERT INTO block(hash, transaction_content) VALUES(?,?)";
    private static final String DELETE_FROM_TABLE_SQL = "DELETE FROM block;";
    private static final String SELECT_ALL_SQL = "SELECT * FROM block";
    private static final String HASH_COLUMN = "hash";
    private static final String TRANSACTION_CONTENT_COLUMN = "transaction_content";

    public static void createTableIfNotExists(String databaseName) {
        try {
            var conn = getConnection(databaseName);
            var stmt = conn.createStatement();
            stmt.execute(CREATE_TABLE_SQL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void insertBlock(String databaseName, Block block) {
        try {
            var conn = getConnection(databaseName);
            var stmt = conn.prepareStatement(INSERT_SQL);
            stmt.setString(1, block.getHash());
            stmt.setString(2, block.getTransaction());
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteAllBLocks(String databaseName) {
        try {
            var conn = getConnection(databaseName);
            var stmt = conn.createStatement();
            stmt.execute(DELETE_FROM_TABLE_SQL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<Block> queryBlocks(String databaseName) {
        try {
            var conn = getConnection(databaseName);
            var stmt = conn.createStatement();
            var rs    = stmt.executeQuery(SELECT_ALL_SQL);
            return getBlocksFromResultSet(rs);
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    private static List<Block> getBlocksFromResultSet(ResultSet rs) throws SQLException {
        List<Block> result = new ArrayList<>();
        while (rs.next()) {
            result.add(getBlockFromResultSet(rs));
        }
        return result;
    }

    private static Block getBlockFromResultSet(ResultSet rs) throws SQLException {
        return new Block(rs.getString(HASH_COLUMN), rs.getString(TRANSACTION_CONTENT_COLUMN));
    }

    private static Connection getConnection(String databaseName) throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        return DriverManager.getConnection(getConnectionUrl(databaseName));
    }

    private static String getConnectionUrl(String databaseName) {
        File dbFile = new File(".");
        return format("jdbc:sqlite:" + dbFile.getAbsolutePath() + "\\%s.db", databaseName);
    }
}
