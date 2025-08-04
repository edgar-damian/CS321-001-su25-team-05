package cs321.search;

/**
 * Prses the command line args.
 *
 */
public class SSHSearchDatabaseArguments {
    private String type;
    private String dbPath;
    private int topN = 10;

    /**
     * Constructor
     * @param args command line args
     */
    public SSHSearchDatabaseArguments(String[] args) {
        for (String arg : args) {
            if (arg.startsWith("--type=")) {
                this.type = arg.substring("--type=".length());
            } else if (arg.startsWith("--database=")) {
                this.dbPath = arg.substring("--database=".length());
            } else if (arg.startsWith("--top-frequency=")) {
                String val = arg.substring("--top-frequency=".length());
                try {
                    this.topN = Integer.parseInt(val);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                            "Invalid number for --top-frequency: " + val, e);
                }
            } else {
                // ignore or handle other flags
            }
        }
    }

    /** tree type */
    public String getType() {
        return type;
    }

    /** the path to the file */
    public String gataDatabasePath() {
        return dbPath;
    }

    /**  number of top frequency keys  */
    public int getTopN() {
        return topN;
    }
}
