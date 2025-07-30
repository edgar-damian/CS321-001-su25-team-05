package cs321.create;

import cs321.btree.BTree;
import cs321.btree.BTreeException;
import cs321.btree.TreeObject;
import cs321.common.ParseArgumentException;



/**
 * The driver class for building a BTree representation of an SSH Log file.
 *
 * @author 
 */
public class SSHCreateBTree {
    /**
     * Main driver of program.
     * @param args
     */
    public static void main(String[] args) throws Exception 
	{
		System.out.println("Hello world from cs321.create.SSHCreateBTree.main");
        SSHCreateBTreeArguments myArgs = parseArguments(args);
        // other code    
	}


    /**
     * Process command line arguments.
     * @param args  The command line arguments passed to the main method.
     */
    public static SSHCreateBTreeArguments parseArguments(String[] args) throws ParseArgumentException
    {
        return null;
    }


	/** 
	 * Print usage message and exit.
	 * @param errorMessage the error message for proper usage
	 */
	private static void printUsageAndExit(String errorMessage)
    {
        System.exit(1);
	}

}
