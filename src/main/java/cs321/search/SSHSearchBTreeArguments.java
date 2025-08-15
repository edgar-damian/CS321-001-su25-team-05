package cs321.search;

import cs321.common.ParseArgumentException;

public final class SSHSearchBTreeArguments {
    private final boolean cacheEnabled;
    private final int degree; // -
    private final String btreeFile;
    private final String queryFile;
    private final int topFrequency;
    private final Integer cacheSize;
    private final boolean debugEnabled;

    private SSHSearchBTreeArguments(boolean cacheEnabled,
            int degree,
            String btreeFile,
            String queryFile,
            int topFrequency,
            Integer cacheSize,
            boolean debugEnabled) {
        this.cacheEnabled = cacheEnabled;
        this.degree = degree;
        this.btreeFile = btreeFile;
        this.queryFile = queryFile;
        this.topFrequency = topFrequency;
        this.cacheSize = cacheSize;
        this.debugEnabled = debugEnabled;
    }

    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    public int getDegree() {
        return degree;
    }

    public String getBtreeFile() {
        return btreeFile;
    }

    public String getQueryFile() {
        return queryFile;
    }

    public int getTopFrequency() {
        return topFrequency;
    }

    public Integer getCacheSize() {
        return cacheSize;
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public static SSHSearchBTreeArguments parse(String[] args) throws ParseArgumentException{
        if (args == null || args.length == 0) {
            throw new IllegalArgumentException("No arguments provided.\n" + usage());
        }

        Boolean cache = null;
        Integer degree = null;
        String btree = null;
        String query = null;
        Integer topF = null;
        Integer cacheSize = null;
        Boolean debug = null;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if (arg.startsWith("--cache=")) {
                String userValue = arg.substring("--cache=".length());
                if (!"0".equals(userValue) && !"1".equals(userValue)) {
                    throw new ParseArgumentException("cache must be 0 or 1 " + userValue);
                }
                cache = ("0".equals(userValue) ? false : true);
            }  else if (arg.startsWith("--degree=")) {
                String userValue = arg.substring("--degree=".length());
                try{
                    int paredDegree = Integer.parseInt(userValue);
                    //right off the bat, if it is negative, throw
                    if (paredDegree < 0){
                        throw new NumberFormatException();
                    }
                    // these are not valid degrees, so use the default
                    if (paredDegree < 1){
                        degree = 128;
                    }
                    //valid degree, just copy over
                    else {
                        degree = paredDegree;
                    }
                } catch (NumberFormatException e){
                    throw new ParseArgumentException("degree must be a positive: " + userValue);
                }
            }  else if (arg.startsWith("--btree-file=")) {
                btree = arg.substring("--btree-file=".length()).trim();
            }  else if (arg.startsWith("--query-file=")) {
                query = arg.substring("--query-file=".length()).trim();
            }  else if (arg.startsWith("--top-frequency=")) {
                String userValue = arg.substring("--top-frequency=".length()).trim();
                try {
                    int parseUserValue = Integer.parseInt(userValue);
                    if (parseUserValue != 10 && parseUserValue != 25 && parseUserValue != 50) {
                        throw new IllegalArgumentException(
                                "Invalid top frequency" + usage());
                    }
                    topF = parseUserValue;
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid top frequency\n" + usage());
                }
            }  else if (arg.startsWith("--cache-size=")) {
                String userValue = arg.substring("--cache-size=".length());
                int size;
                try {
                    size = Integer.parseInt(userValue);
                } catch (NumberFormatException e) {
                    throw new ParseArgumentException("Invalid integer  " + userValue);
                }
                if (size <= 0) {
                    throw new ParseArgumentException("cache size must be greater than 0");
                }
                cacheSize = size;
                cache = true;
            }  else if (arg.startsWith("--debug=")) {
               String userValue = arg.substring("--debug=".length()).trim();
                if ("0".equals(userValue))
                    debug = false;
                if ("1".equals(userValue))
                    debug =  true;

            }  else {
                throw new IllegalArgumentException("Unknown argument: " + arg + "\n" + usage());
            }
        }

        if (cache == null) {
            throw new IllegalArgumentException("Missing cache.\n" + usage());
        }
        if (degree == null) {
            throw new IllegalArgumentException("Missing degree.\n" + usage());
        }
        if (btree == null) {
            throw new IllegalArgumentException("Missing btree file.\n" + usage());
        }
        if (query == null) {
            throw new IllegalArgumentException("Missing query file.\n" + usage());
        }
        if (degree < 1) {
            throw new IllegalArgumentException("degree must be >= 1\n" + usage());
        }
        if (topF == null) {
            topF = 25;
        }

        return new SSHSearchBTreeArguments(
                cache,
                degree,
                btree,
                query,
                topF,
                cacheSize,
                debug != null && debug);
    }






    public static String usage() {
        return "java -jar build/libs/SSHSearchBTree.jar --cache=<0/1> --degree=<btree-degree> \\\n" +
                "          --btree-file=<btree-filename> --query-file=<query-fileaname> \\\n" +
                "          [--top-frequency=<10/25/50>] [--cache-size=<n>]  [--debug=<0|1>]";
    }
}