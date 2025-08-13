package cs321.create;

import cs321.btree.BTree;
import cs321.btree.TreeObject;
import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;


/**
 * The driver class for building a BTree representation of an SSH Log file.
 *
 * @author Diego Dominguez
 */
public class SSHCreateBTree {
    /**
     * Main driver of program.
     * @param args
     */
    public static void main(String[] args) throws Exception 
	{

        SSHCreateBTreeArguments myArgs = SSHCreateBTreeArguments.parse(args);
        // Print the args to user if there debugger is on
        if (myArgs.getDebug() == 1){
            System.out.println("Arguements parsed successfully" + args.toString());
        }
        int userDegree = myArgs.getDegree();
        String fileName = "SSH_log.txt.ssh.btree." + myArgs.getTreeType()+"."+myArgs.getDegree();
        BTree btree = new BTree(userDegree, fileName);

        // create scanner for the file
        Scanner scanner = new Scanner(new File(myArgs.getSSHFileName()));
        // just setting the treeType in a variable here and calling inside the while instead of calling the getTreeType all the time
        String treeType = myArgs.getTreeType();
        String line; // variable to store the line each time we loop through file lines
        while (scanner.hasNextLine()) {
            line = scanner.nextLine();
            String log = SSHFileReader.extractSSHLogEntries(line, treeType); // call my SSHFileReader extracter method i made
            TreeObject treeObject = new TreeObject(log); // setting the log to a treeObject
            btree.insert(treeObject); // inserting into the btree
        }
        scanner.close();

        // Allow user to build a file and check the btree, we need to implement a dumpFile method in Btree at some point
        if (myArgs.getDebugLevel() == 1) {
            String dumpFileName = "dump-"+myArgs.getTreeType()+"."+myArgs.getDegree()+ ".txt";
            PrintWriter file = new PrintWriter(dumpFileName);
            btree.dumpFile(file); //edgar
            file.close();
            System.out.println("Debug dump file created here: " + dumpFileName);
            if (myArgs.getUseDatabase() == 1){
                // strips all chars and nums
                String tableName = myArgs.getTreeType().replaceAll("[^a-zA-Z0-9]", "");
//            btree.dumpToDatabase("SSHLogDB.db", tableName); // need to create this method in btree tomorrow!
            }
        }
        System.out.println("BTree successfully created.");
	}




	/** 
	 * Print usage message and exit.
	 * @param errorMessage the error message for proper usage
	 */
	private static void printUsageAndExit(String errorMessage)
    {
        System.err.println("Error: " + errorMessage);
        System.err.println(
                "Usage: java -jar build/libs/SSHCreateBTree.jar --cache=<0/1> --degree=<btree-degree> \\\n" +
                        "        --sshFile=<ssh-File> --type=<tree-type> [--cache-size=<n>] \\\n" +
                        "        --database=<yes/no> [--debug=<0|1>]");
        System.exit(1);

	}

}
