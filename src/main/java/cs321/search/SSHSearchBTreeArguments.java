package cs321.search;

public final class SSHSearchBTreeArguments {
    private final boolean cacheEnabled; // --cache=<0|1>
    private final int degree; // --degree=<btree-degree>
    private final String btreeFile; // --btree-file=<file>
    private final String queryFile; // --query-file=<file>
    private final int topFrequency; // [--top-frequency=<10|25|50>] (default 25)
    private final Integer cacheSize; // [--cache-size=<n>] (optional)
    private final boolean debugEnabled; // [--debug=<0|1>] (default 0)

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
    } // may be null if not provided

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public static SSHSearchBTreeArguments parse(String[] args) {
        if (args == null || args.length == 0) {
            throw new IllegalArgumentException("No arguments provided.\n" + usage());
        }

        Boolean cache = null;
        Integer degree = null;
        String btree = null;
        String query = null;
        Integer top = null; // default to 25 if missing
        Integer cacheSize = null; // optional
        Boolean debug = null; // default to false if missing

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if (arg.startsWith("--cache=")) {
                cache = parseBool01(arg.substring("--cache=".length()).trim(), "--cache");
            } else if ("--cache".equals(arg) && i + 1 < args.length) {
                cache = parseBool01(args[++i].trim(), "--cache");
            } else if (arg.startsWith("--degree=")) {
                degree = parsePositiveInt(arg.substring("--degree=".length()).trim(), "--degree");
            } else if ("--degree".equals(arg) && i + 1 < args.length) {
                degree = parsePositiveInt(args[++i].trim(), "--degree");
            } else if (arg.startsWith("--btree-file=")) {
                btree = arg.substring("--btree-file=".length()).trim();
            } else if ("--btree-file".equals(arg) && i + 1 < args.length) {
                btree = args[++i].trim();
            } else if (arg.startsWith("--query-file=")) {
                query = arg.substring("--query-file=".length()).trim();
            } else if ("--query-file".equals(arg) && i + 1 < args.length) {
                query = args[++i].trim();
            } else if (arg.startsWith("--top-frequency=")) {
                top = parseTop(arg.substring("--top-frequency=".length()).trim());
            } else if ("--top-frequency".equals(arg) && i + 1 < args.length) {
                top = parseTop(args[++i].trim());
            } else if (arg.startsWith("--cache-size=")) {
                cacheSize = parsePositiveInt(arg.substring("--cache-size=".length()).trim(), "--cache-size");
            } else if ("--cache-size".equals(arg) && i + 1 < args.length) {
                cacheSize = parsePositiveInt(args[++i].trim(), "--cache-size");
            } else if (arg.startsWith("--debug=")) {
                debug = parseBool01(arg.substring("--debug=".length()).trim(), "--debug");
            } else if ("--debug".equals(arg) && i + 1 < args.length) {
                debug = parseBool01(args[++i].trim(), "--debug");
            } else if ("--help".equals(arg) || "-h".equals(arg)) {
                throw new IllegalArgumentException(usage());
            } else {
                throw new IllegalArgumentException("Unknown argument: " + arg + "\n" + usage());
            }
        }

        if (cache == null) {
            throw new IllegalArgumentException("Missing required --cache.\n" + usage());
        }
        if (degree == null) {
            throw new IllegalArgumentException("Missing required --degree.\n" + usage());
        }
        if (isBlank(btree)) {
            throw new IllegalArgumentException("Missing required --btree-file.\n" + usage());
        }
        if (isBlank(query)) {
            throw new IllegalArgumentException("Missing required --query-file.\n" + usage());
        }
        if (degree < 2) {
            throw new IllegalArgumentException("--degree must be >= 2\n" + usage());
        }
        if (top == null) {
            top = 25; // default
        }
        if (cache == Boolean.FALSE && cacheSize != null) {
            // Allow but warn via exception message? Keep permissive: ignore and allow.
        }

        return new SSHSearchBTreeArguments(
                cache,
                degree,
                btree,
                query,
                top,
                cacheSize,
                debug != null && debug);
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static Boolean parseBool01(String s, String flag) {
        if ("0".equals(s))
            return false;
        if ("1".equals(s))
            return true;
        throw new IllegalArgumentException("Invalid " + flag + ": " + s + " (allowed: 0 or 1)\n" + usage());
    }

    private static int parsePositiveInt(String s, String flag) {
        try {
            int v = Integer.parseInt(s);
            if (v <= 0) {
                throw new IllegalArgumentException("Invalid " + flag + ": " + s + " (must be > 0)\n" + usage());
            }
            return v;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid " + flag + ": " + s + " (must be an integer)\n" + usage());
        }
    }

    private static int parseTop(String s) {
        try {
            int v = Integer.parseInt(s);
            if (v != 10 && v != 25 && v != 50) {
                throw new IllegalArgumentException(
                        "Invalid --top-frequency: " + s + " (allowed: 10, 25, 50)\n" + usage());
            }
            return v;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid --top-frequency: " + s + " (must be an integer)\n" + usage());
        }
    }

    public static String usage() {
        return "Usage:\n" +
                "  java -jar build/libs/SSHSearchBTree.jar \\\n" +
                "       --cache=<0|1> \\\n" +
                "       --degree=<btree-degree> \\\n" +
                "       --btree-file=<btree-filename> \\\n" +
                "       --query-file=<query-filename> \\\n" +
                "       [--top-frequency=<10|25|50>] \\\n" +
                "       [--cache-size=<n>] \\\n" +
                "       [--debug=<0|1>]\n";
    }

    @Override
    public String toString() {
        return "SSHSearchBTreeArguments{" +
                "cacheEnabled=" + cacheEnabled +
                ", degree=" + degree +
                ", btreeFile='" + btreeFile + '\'' +
                ", queryFile='" + queryFile + '\'' +
                ", topFrequency=" + topFrequency +
                ", cacheSize=" + cacheSize +
                ", debugEnabled=" + debugEnabled +
                '}';
    }
}