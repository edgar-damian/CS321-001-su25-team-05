package cs321.search;
import java.sql.*;
import java.util.PriorityQueue;

import cs321.search.SSHSearchDatabaseArguments;
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
        String databasePath = parsedArgs.getaDatabasePath();
        int topN = parsedArgs.getTopN();

        /** type and databasePath are required **/
        if (type == null || databasePath == null) {
            System.err.println("Usage: java SSHSearchDatabase --type=<tree-type> --database=<sqlite-db-path> --top-frequency=<10/25/50>");
            return;
        }

        /** creating db connection **/
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + databasePath)) {
            if (type.equals("test")) {
                // CREATE THE mock data
                createTestDatabase(conn);
                System.out.println("Database created: " + databasePath);
            } else {
                String tableName=type;
                String sql="SELECT key, frequency FROM \""+tableName+"\"";
                PriorityQueue<KeyFreq> pq= new PriorityQueue<>();

                try(Statement stmt=conn.createStatement();
                    ResultSet rs=stmt.executeQuery(sql)){
                    while(rs.next()){
                        String key=rs.getString("key");
                        int frequency= rs.getInt("frequency");
                        pq.add(new KeyFreq(key,frequency));
                    }
                } catch (Exception e) {
                    System.err.println("Database Query Failed");
                    return;
                }
                int count=0;
                while(!pq.isEmpty() && count<topN)
                {
                    KeyFreq entry=pq.poll();
                    System.out.println(entry.key+" "+entry.frequency);
                    count++;
                }
                // use the real data
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private static class KeyFreq implements Comparable<KeyFreq>{
        String key;
        int frequency;
        KeyFreq(String key,int frequency){
            this.key=key;
            this.frequency=frequency;
        }
        @Override
        public int compareTo(KeyFreq other){
            int cmp=this.key.compareTo(other.key);
            if(cmp!=0){
                return cmp;
            }
            return Integer.compare(this.frequency, other.frequency);
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
            /** dropping tables if they are already existing **/
            stmt.executeUpdate("DROP TABLE IF EXISTS acceptedIP");
            stmt.executeUpdate("DROP TABLE IF EXISTS acceptedTimeStamp");
            stmt.executeUpdate("DROP TABLE IF EXISTS failedIP");
            stmt.executeUpdate("DROP TABLE IF EXISTS failedTimeStamp");
            stmt.executeUpdate("DROP TABLE IF EXISTS invalidIP");
            stmt.executeUpdate("DROP TABLE IF EXISTS invalidTimeStamp");
            stmt.executeUpdate("DROP TABLE IF EXISTS reverseAddressIP");
            stmt.executeUpdate("DROP TABLE IF EXISTS reverseAddressTimeStamp");
            stmt.executeUpdate("DROP TABLE IF EXISTS userIp");
            /** creating tables **/
            stmt.executeUpdate("CREATE TABLE acceptedIP (key TEXT, frequency INTEGER)");
            stmt.executeUpdate("CREATE TABLE acceptedTimeStamp (key TEXT, frequency INTEGER)");
            stmt.executeUpdate("CREATE TABLE failedIP (key TEXT, frequency INTEGER)");
            stmt.executeUpdate("CREATE TABLE failedTimeStamp (key TEXT, frequency INTEGER)");
            stmt.executeUpdate("CREATE TABLE invalidIP (key TEXT, frequency INTEGER)");
            stmt.executeUpdate("CREATE TABLE invalidTimeStamp (key TEXT, frequency INTEGER)");
            stmt.executeUpdate("CREATE TABLE reverseAddressIP (key TEXT, frequency INTEGER)");
            stmt.executeUpdate("CREATE TABLE reverseAddressTimeStamp (key TEXT, frequency INTEGER)");
            stmt.executeUpdate("CREATE TABLE userIp (key TEXT, frequency INTEGER)");

        }
        /** creating statements **/
        try (PreparedStatement insertAcceptedIP = conn.prepareStatement("INSERT INTO acceptedIP VALUES (?, ?)");
             PreparedStatement insertAcceptedTimeStamp = conn.prepareStatement("INSERT INTO acceptedTimeStamp VALUES (?, ?)");
             PreparedStatement insertFailedIP = conn.prepareStatement("INSERT INTO failedIP VALUES (?, ?)");
             PreparedStatement insertFailedTimeStamp = conn.prepareStatement("INSERT INTO failedTimeStamp VALUES (?, ?)");
             PreparedStatement insertInvalidIP= conn.prepareStatement("INSERT INTO invalidIP VALUES (?, ?)");
             PreparedStatement insertInvalidTimeStamp= conn.prepareStatement("INSERT INTO invalidTimeStamp VALUES (?, ?)");
             PreparedStatement insertReverseAddressIp = conn.prepareStatement("INSERT INTO reverseAddressIP VALUES (?, ?)");
             PreparedStatement insertReverseAddressTimeStamp = conn.prepareStatement("INSERT INTO reverseAddressTimeStamp VALUES (?, ?)");
             PreparedStatement insertUserIP = conn.prepareStatement("INSERT INTO userIp VALUES (?, ?)");
        ){
            for (String data : testData) {
                String[] subStrings = data.split(" ");
                String firstString = subStrings[0];
                int freq = Integer.parseInt(subStrings[1]);

                if (firstString.startsWith("Accepted-")) {
                    insertAcceptedIP.setString(1, firstString);
                    insertAcceptedIP.setInt(2, freq);
                    insertAcceptedIP.executeUpdate();
                    insertAcceptedTimeStamp.setString(1, firstString);
                    insertAcceptedTimeStamp.setInt(2, freq);
                    insertAcceptedTimeStamp.executeUpdate();
                } else if (firstString.startsWith("Failed-")) {
                    insertFailedIP.setString(1, firstString);
                    insertFailedIP.setInt(2, freq);
                    insertFailedIP.executeUpdate();
                    insertFailedTimeStamp.setString(1, firstString);
                    insertFailedTimeStamp.setInt(2, freq);
                    insertFailedTimeStamp.executeUpdate();
                } else if (firstString.startsWith("Invalid-")) {
                    insertInvalidIP.setString(1, firstString);
                    insertInvalidIP.setInt(2, freq);
                    insertInvalidIP.executeUpdate();
                    insertInvalidTimeStamp.setString(1, firstString);
                    insertInvalidTimeStamp.setInt(2, freq);
                    insertInvalidTimeStamp.executeUpdate();
                } else if (firstString.startsWith("Reverse-")) {
                    insertReverseAddressIp.setString(1, firstString);
                    insertReverseAddressIp.setInt(2, freq);
                    insertReverseAddressIp.executeUpdate();
                    insertReverseAddressTimeStamp.setString(1, firstString);
                    insertReverseAddressTimeStamp.setInt(2, freq);
                    insertReverseAddressTimeStamp.executeUpdate();
                } else {
                    insertUserIP.setString(1, firstString);
                    insertUserIP.setInt(2, freq);
                    insertUserIP.executeUpdate();
                }
            }
        }
    }

    private static void printUsageAndExit () {
        System.err.println("Usage: java SSHSearchDatabase " +
                "--cache=<0|1> --degree=<n> --btree-file=<file> --query-file=<file> " +
                "[--top-frequency=<N>] [--cache-size=<n>] [--debug=<0|1>]");
        System.exit(1);
    }

}