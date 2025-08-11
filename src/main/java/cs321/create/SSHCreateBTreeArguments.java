
package cs321.create;
import cs321.common.ParseArgumentException;

/**
 * SSHCreateBTreeArguments
 *
 * @author Diego Dominguez
 *
 */
public class SSHCreateBTreeArguments
{
	/* TODO: Complete this class */

    private final boolean useCache;
    private final int degree;
    private final String SSHFileName;
    private final String treeType;
    private final int cacheSize;
    private final int debugLevel;
    private final int useDatabase;
    /**
     * Builds a new SSHCreateBTreeArguments with the specified
     * command line arguments and tests their validity.
     *
     * @param useCache boolean for using cache or not
     * @param degree degree for BTree
     * @param SSHFileName String of filename
     * @param treeType type of tree
     * @param cacheSize size of cache if using
     * @param debugLevel level of debugging
     */
    private SSHCreateBTreeArguments(boolean useCache, int degree, String SSHFileName, String treeType, int cacheSize,int useDatabase, int debugLevel)
    {
        this.useCache = useCache;
        this.degree = degree;
        this.SSHFileName = SSHFileName;
        this.treeType = treeType;
        this.cacheSize = cacheSize;
        this.debugLevel = debugLevel;
        this.useDatabase = useDatabase;
    }

    public static SSHCreateBTreeArguments parse(String[] args) throws ParseArgumentException {
        if (args == null || args.length == 0) {
            throw new ParseArgumentException("No arguments provided.");
        }

        boolean useCache = false;
        Integer degree = null;
        String SSHFileName = null;
        String treeType = "btree";
        int cacheSize = 0;
        int debugLevel = 0;
        int useDatabase = 0;

        for (String arg : args) {
            if (arg.startsWith("--degree=")) {
                String userValue = arg.substring("--degree=".length());
                try {
                    degree = Integer.parseInt(userValue);
                    if (degree <= 0) throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    throw new ParseArgumentException("degree must be a positive: " + userValue);
                }
            } else if (
                    arg.startsWith("--ssh-File=")
            ) {
                String key = arg.substring(0, arg.indexOf('=') + 1);
                SSHFileName = arg.substring(key.length()).trim();
                if (SSHFileName.isEmpty()) {
                    throw new ParseArgumentException(key + " is empty");
                }
            } else if (arg.startsWith("--tree-type=")) {
                treeType = arg.substring("--tree-type=".length()).trim();
                if (treeType.isEmpty()) {
                    throw new ParseArgumentException("tree type is empty");
                }
            } else if (arg.startsWith("--cache=")) {
                String userValue = arg.substring("--cache=".length());
                if (!"0".equals(userValue) && !"1".equals(userValue)) {
                    throw new ParseArgumentException("cache must be 0 or 1 " + userValue);
                }
                useCache = "1".equals(userValue);
            } else if (arg.startsWith("--cache-size=")) {
                String userValue = arg.substring("--cache-size=".length());
                int i;
                try {
                    i = Integer.parseInt(userValue);
                } catch (NumberFormatException e) {
                    throw new ParseArgumentException("Invalid integer  " + userValue);
                }
                if (i <= 0) {
                    throw new ParseArgumentException("cache size must be greater than 0");
                }
                cacheSize = i;
                useCache = true;
            }else if(arg.startsWith("--database=")) {
                String userValue = arg.substring("--database=".length());
                if (!"0".equals(userValue) && !"1".equals(userValue)) {
                    throw new ParseArgumentException("database must be 0 or 1 " + userValue);
                }
                useDatabase = Integer.parseInt(userValue);
            }
                else if (arg.startsWith("--debug=")) {
                String userValue = arg.substring("--debug=".length());
                if (!"0".equals(userValue) && !"1".equals(userValue)) {
                    throw new ParseArgumentException("debug must be 0 or 1: " + userValue);
                }
                debugLevel = Integer.parseInt(userValue);
            } else {
                throw new ParseArgumentException("Unknown argument: " + arg);
            }
        }

        if (degree == null) {
            throw new ParseArgumentException("Missing required degree");
        }
        if (SSHFileName == null) {
            throw new ParseArgumentException("Missing required file");
        }
        if (useCache && cacheSize <= 0) {
            throw new ParseArgumentException("cache size must be provided");
        }

        return new SSHCreateBTreeArguments(useCache, degree, SSHFileName, treeType, cacheSize,useDatabase, debugLevel);
    }


    public boolean useCache() { return useCache; }
    public int getDegree() { return degree; }
    public String getSSHFileName() { return SSHFileName; }
    public String getTreeType() { return treeType; }
    public int getCacheSize() { return cacheSize; }
    public int getDebugLevel() { return debugLevel; }
    public int getDebug() { return debugLevel; }
    public int getUseDatabase() { return useDatabase; }

    @Override
    public String toString()
    {
        return "SSHFileNameCreateBTreeArguments{" +
                "useCache=" + useCache +
                ", degree=" + degree +
                ", SSH_Log_File='" + SSHFileName + '\'' +
                ", TreeType=" + treeType +
                ", cacheSize=" + cacheSize +
                ", debugLevel=" + debugLevel +
                '}';
    }
}
