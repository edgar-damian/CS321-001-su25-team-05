package cs321.search;



/**
 * Driver class for BTree
 *
 * Usage: java SSHSearchDatabase --type=<tree-type> \
 *           --database=<sqlite-database-path> --top-frequency=<10/25/50>
 */
public final class SSHSearchDatabase {

    public static void main(String[] args) {
        SSHSearchDatabaseArguments parsedArgs = new SSHSearchDatabaseArguments(args);

        String type = parsedArgs.getType();
        String databasePath = parsedArgs.gataDatabasePath();
        int topN = parsedArgs.getTopN();

        // type and databasePath are required
        if (type == null || databasePath == null) {
            System.err.println("Usage: java SSHSearchDatabase --type=<tree-type> --database=<sqlite-db-path> --top-frequency=<10/25/50>");
            return;
        }


        // CREATE DB CONNECTION HERE?




     }


        private static void printUsageAndExit () {
            System.err.println("Usage: java SSHSearchDatabase " +
                    "--cache=<0|1> --degree=<n> --btree-file=<file> --query-file=<file> " +
                    "[--top-frequency=<N>] [--cache-size=<n>] [--debug=<0|1>]");
            System.exit(1);
        }

}
