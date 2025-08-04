package cs321.search;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Driver class for BTree
 *
 * Usage: java SSHSearchDatabase --type=<tree-type> \
 *           --database=<sqlite-database-path> --top-frequency=<10/25/50>
 */
public final class SSHSearchDatabase {

    public static void main(String[] args) {
        /** getting arguements **/
        SSHSearchDatabaseArguments parsedArgs = new SSHSearchDatabaseArguments(args);
        String type = parsedArgs.getType();
        String databasePath = parsedArgs.gataDatabasePath();
        int topN = parsedArgs.getTopN();

        /** type and databasePath are required **/
        if (type == null || databasePath == null) {
            System.err.println("Usage: java SSHSearchDatabase --type=<tree-type> --database=<sqlite-db-path> --top-frequency=<10/25/50>");
            return;
        }

        /** CREATE DB CONNECTION **/
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + databasePath)) {
            if (type.equals("test")) {
                // CREATE THE DUMMY DATA
            } else {
                // use the real data

            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
     }

    private static void createTestDatabase(Connection conn) throws Exception {
        String[] testData = {
                "Accepted-111.222.107.90 25",
                "Accepted-112.96.173.55 3",
                "Accepted-112.96.33.40 3",
                "Accepted-113.116.236.34 6",
                "Accepted-113.118.187.34 2",
                "Accepted-113.99.127.215 2",
                "Accepted-119.137.60.156 1",
                "Accepted-119.137.62.123 9",
                "Accepted-119.137.62.142 1",
                "Accepted-119.137.63.195 14",
                "Accepted-123.255.103.142 5",
                "Accepted-123.255.103.215 5",
                "Accepted-137.189.204.138 1",
                "Accepted-137.189.204.155 1",
                "Accepted-137.189.204.220 1",
                "Accepted-137.189.204.236 1",
                "Accepted-137.189.204.246 1",
                "Accepted-137.189.204.253 3",
                "Accepted-137.189.205.44 2",
                "Accepted-137.189.206.152 1",
                "Accepted-137.189.206.243 1",
                "Accepted-137.189.207.18 1",
                "Accepted-137.189.207.28 1",
                "Accepted-137.189.240.159 1",
                "Accepted-137.189.241.19 2"
        };

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DROP TABLE IF EXISTS acceptedtime");
            stmt.executeUpdate("DROP TABLE IF EXISTS invalidTime");
            stmt.executeUpdate("DROP TABLE IF EXISTS failedIP");
            stmt.executeUpdate("DROP TABLE IF EXISTS reverseAddressIp");
            stmt.executeUpdate("DROP TABLE IF EXISTS reverseAddressTime");
            stmt.executeUpdate("DROP TABLE IF EXISTS userIp");

            stmt.executeUpdate("CREATE TABLE acceptedtime (key TEXT, frequency INTEGER)");
            stmt.executeUpdate("CREATE TABLE invalidTime (key TEXT, frequency INTEGER)");
            stmt.executeUpdate("CREATE TABLE failedIP (key TEXT, frequency INTEGER)");
            stmt.executeUpdate("CREATE TABLE reverseAddressIp (key TEXT, frequency INTEGER)");
            stmt.executeUpdate("CREATE TABLE reverseAddressTime (key TEXT, frequency INTEGER)");
            stmt.executeUpdate("CREATE TABLE userIp (key TEXT, frequency INTEGER)");

        }
        }






        private static void printUsageAndExit () {
            System.err.println("Usage: java SSHSearchDatabase " +
                    "--cache=<0|1> --degree=<n> --btree-file=<file> --query-file=<file> " +
                    "[--top-frequency=<N>] [--cache-size=<n>] [--debug=<0|1>]");
            System.exit(1);
        }

}
