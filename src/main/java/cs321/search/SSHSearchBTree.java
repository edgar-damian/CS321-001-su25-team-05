package cs321.search;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import cs321.Cache.Cache;
import cs321.btree.BTree;
import cs321.btree.TreeObject;

public final class SSHSearchBTree {

	public static void main(String[] args) {
		SSHSearchBTreeArguments a;
		a = SSHSearchBTreeArguments.parse(args);

		/* build optional cache */
		Cache cache = a.isCacheEnabled() ? new Cache(a.getCacheSize()) : null;

		/* open BTree (constructor matches provided implementation) */
		BTree btree;
		try {
			btree = new BTree(a.getDegree(), a.getBtreeFile());
		} catch (Exception e) {
			System.err.println("Failed to open BTree: " + e.getMessage());
			return;
		}

		List<Result> results = new ArrayList<>();

		/* read query file */
		try (BufferedReader br = new BufferedReader(new FileReader(a.getQueryFile()))) {
			String line;
			while ((line = br.readLine()) != null) {
				String key = line.trim();
				if (key.isEmpty())
					continue;
				if (key.length() > 32)
					key = key.substring(0, 32);

				long freq = -1;

				/* check cache first */
				if (cache != null && cache.contains(key)) {
					freq = Long.parseLong(cache.get(key)[0]);
				} else {
					TreeObject obj = btree.search(key);
					if (obj != null) {
						freq = obj.getCount();
						if (cache != null)
							cache.put(key, new String[] { String.valueOf(freq) });
					}
				}

				if (freq == -1) {
					System.out.println(key + " NOT FOUND");
				} else {
					results.add(new Result(key, freq));
				}
			}
		} catch (Exception e) {
			System.err.println("Error reading query file: " + e.getMessage());
			return;
		}

		/* print results (all or top-N) */
		if (a.getTopFrequency() > 0) {
			PriorityQueue<Result> pq = new PriorityQueue<>(a.getTopFrequency(), Result.ORDER);
			pq.addAll(results);
			int printed = 0;
			while (!pq.isEmpty() && printed < a.getTopFrequency()) {
				Result r = pq.poll();
				System.out.println(r.key + " " + r.freq);
				printed++;
			}
		} else {
			for (Result r : results) {
				System.out.println(r.key + " " + r.freq);
			}
		}

		if (a.isDebugEnabled()) {
			System.err.println("DEBUG: searched " + results.size() + " keys");
		}

		/* tidy */
		// try {
		// btree.close();
		// } catch (Exception ignored) {
		// }
	}

	private static void printUsageAndExit() {
		System.err.println("Usage: java SSHSearchBTree "
				+ "--cache=<0|1> --degree=<n> --btree-file=<file> --query-file=<file> "
				+ "[--top-frequency=<10/25/50>] [--cache-size=<n>] [--debug=<0|1>]");
		System.exit(1);
	}

	/* simple POJO for results */
	private static final class Result {
		final String key;
		final long freq;

		Result(String key, long freq) {
			this.key = key;
			this.freq = freq;
		}

		/* comparator: freq desc, then key asc */
		static final Comparator<Result> ORDER = Comparator.<Result>comparingLong(r -> -r.freq)
				.thenComparing(r -> r.key);
	}
}