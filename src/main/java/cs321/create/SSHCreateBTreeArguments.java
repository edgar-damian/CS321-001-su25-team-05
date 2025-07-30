package cs321.create;

/**
 * SSHCreateBTreeArguments parses command line arguments for SSHCreateBTree.
 *
 *  @author
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
    public SSHCreateBTreeArguments(boolean useCache, int degree, String SSHFileName, String treeType, int cacheSize, int debugLevel)
    {
        this.useCache = useCache;
        this.degree = degree;
        this.SSHFileName = SSHFileName;
        this.treeType = treeType;
        this.cacheSize = cacheSize;
        this.debugLevel = debugLevel;
    }


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
