package persistence;

import block.Block;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.lang.String.format;

public class PersistenceUtils {

    private static final String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS block (\n"
            + " id text PRIMARY KEY,\n"
            + " transaction_content text NOT NULL\n"
            + ");";
    private static final String INSERT_SQL = "INSERT INTO block(id, transaction_content) VALUES(?,?)";
    private static final String SELECT_ALL_SQL = "SELECT * FROM block";

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
            stmt.setString(1, UUID.randomUUID().toString());
            stmt.setString(2, block.getTransaction());
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<Block> queryBlocks(String databaseName) {
        try {
            var conn = getConnection(databaseName);
            var stmt = conn.createStatement();
            ResultSet rs    = stmt.executeQuery(SELECT_ALL_SQL);
            List<Block> result = new ArrayList<>();
            while (rs.next()) {
                result.add(new Block(rs.getString("transaction_content")));
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
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
