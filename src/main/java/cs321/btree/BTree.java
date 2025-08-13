package cs321.btree;

import cs321.btree.TreeObject;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BTree {

    private int METADATA_SIZE = Long.BYTES;
    private long nextDiskAddress = METADATA_SIZE;
    private FileChannel file;
    private ByteBuffer buffer;
    private int nodeLength; //the length of each node in bytes
    private int diskBlock = 4096;

    private int t;
    private int numNodes = 0; //number of nodes in the tree
    private int numObjects = 0; //number of objects inside each node
    private long rootAddress = METADATA_SIZE;
    private Node root;

    public BTree(String fileNameString) {

        this.t = (diskBlock - Integer.BYTES - 1 + (2 * Long.BYTES)) / (4 * Long.BYTES);
        this.nodeLength = calculateBytes();
        this.buffer = ByteBuffer.allocateDirect(calculateBytes());
        this.nextDiskAddress = METADATA_SIZE;



        File fileName = new File(fileNameString);
        boolean exists;
        if (fileName.exists()) {
            exists = true;
        } else {
            exists = false;
        }

        try {
            //open file, create if !exists
            if (!exists) {
                fileName.createNewFile();
            }
            RandomAccessFile dataFile = new RandomAccessFile(fileName, "rw");
            file = dataFile.getChannel();

            /**
             * This is for a brand new tree. Sets it up in the
             * disk and get nextDiskAddress ready
             */
            if (!exists) {
                Node x = new Node(true);
                x.leaf = true;
                x.n = 0;
                root = x;
                numNodes = 1;
                diskWrite(x);
                rootAddress = x.address;

                writeMetaData();
                //makes sure nextDiskAddress actually points to the next address
                nextDiskAddress = Math.max(nextDiskAddress, file.size());
            }
            /**
             * This is if a tree already exitst, basically
             * load the tree by getting in the right address to continue.
             */
            else {
                readMetaData();
                root = diskRead(rootAddress);
                nextDiskAddress = Math.max(file.size(), METADATA_SIZE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public BTree(int degree,String fileNameString) {
        this.t = degree;
        this.nodeLength = calculateBytes();
        this.buffer = ByteBuffer.allocateDirect(calculateBytes());
        this.nextDiskAddress = METADATA_SIZE;


        File fileName = new File(fileNameString);
        boolean exists;
        if (fileName.exists()) {
            exists = true;
        } else {
            exists = false;
        }

        try {
            //open file, create if !exists
            if (!exists) {
                fileName.createNewFile();
            }
            RandomAccessFile dataFile = new RandomAccessFile(fileName, "rw");
            file = dataFile.getChannel();

            /**
             * This is for a brand new tree. Sets it up in the
             * disk and get nextDiskAddress ready
             */
            if (!exists) {
                Node x = new Node(true);
                x.leaf = true;
                x.n = 0;
                root = x;
                numNodes = 1;
                diskWrite(x);
                rootAddress = x.address;

                writeMetaData();
                //makes sure nextDiskAddress actually points to the next address
                nextDiskAddress = Math.max(nextDiskAddress, file.size());
            }
            /**
             * This is if a tree already exitst, basically
             * load the tree by getting in the right address to continue.
             */
            else {
                readMetaData();
                root = diskRead(rootAddress);
                nextDiskAddress = Math.max(file.size(), METADATA_SIZE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void insert (TreeObject obj){

        //checking to see if this is a duplicate
        boolean duplicate = isDuplicate(obj);
        if (duplicate) {
            searchNode(root, obj.getKey());
            return; //done after that
        }

        Node r = root;
        if (r.n == ( (2*t) - 1)) {
            Node s = BTreeSplitRoot();
            BTreeInsertNonFull(s, obj);
            numObjects++;
        } else {
            BTreeInsertNonFull(r, obj);
            numObjects++;
        }
    }

    /**
     * This method calls a search to check if the object about to be inserted
     * is already on the tree. If it is NOT in the tree, the search will return
     * null, and the method will return false. Else, the object is a duplicate,
     * and the method returns true.
     *
     * @param obj
     * @return returns true if obj is a duplicate, false otherwise
     */
    public boolean isDuplicate (TreeObject obj){
        TreeObject check = search(obj.getKey());
        if (check == null) {
            return false;
        }
        return true;

    }

    public void BTreeInsertNonFull(Node x, TreeObject obj) {
        int i = x.n - 1;
        if (x.leaf) {
            while (i >= 0 && obj.compareTo(x.keys[i]) < 0) {
                x.keys[i + 1] = x.keys[i];
                //i++;
                i--;
            }
            x.keys[i + 1] = obj;
            x.n++;
            try {
                diskWrite(x);
            } catch (IOException e) {
                e.printStackTrace();
            }


        } else {
            while (i >= 0 && obj.compareTo(x.keys[i]) < 0) {
                i--;
            }
            i++;
            /*
            DISK-READ(x.c[i])
            if x.c[i].n == 2t - 1				// child is full
                BTREE-SPLIT-CHILD(x, i)
                if k > x.key[i]
                    i = i + 1
            BTREE-INSERT-NONFULL(x.c[i], k)

             */
            Node childNode = null; //temp holder
            try {
                //diskRead(x.children[i]);
                childNode = diskRead(x.children[i] == null ? 0L : x.children[i]);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (childNode.n == (2 * t - 1) && childNode != null) {
                BTreeSplitChild(x, i);
                if (obj.compareTo(x.keys[i]) > 0) {
                    i++;
                }
                try {
                    childNode = diskRead(x.children[i] == null ? 0L : x.children[i]);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (childNode != null) {
                BTreeInsertNonFull(childNode, obj);
            }
        }
        //numObjects++;
    }

    public Node BTreeSplitRoot() {
        Node s = new Node(true); //going to be the root/parent
        s.leaf = false;
        s.n = 0;

        s.children[0] = root.address;
        root = s;
        BTreeSplitChild(s,0);

        //update s
        try {
            diskWrite(s);
            rootAddress = s.address;
            writeMetaData();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //

        numNodes++;
        return s;
    }

    public void BTreeSplitChild(Node current, int i) {
        Node left = null;
        try{
            left =  diskRead(current.children[i]);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (left == null) {
            System.err.println("BTreeSplitChild: child is null");
            return;
        }

        Node right = new Node(true);
        right.leaf = left.leaf;
        right.n = t - 1;
        //for (int j = 0; j < t - 2; j++) {
        for (int j = 0; j < t - 1; j++) {
            right.keys[j] = left.keys[j+t];
        }
        if (!left.leaf) {
            for (int j = 0; j < t; j++) {
                right.children[j] = left.children[j + t];
            }
        }
        left.n = t - 1;

        for (int j = current.n; j >= i + 1; j--) {
            current.children[j + 1] = current.children[j];
        }
        current.children[i + 1] = right.address;
        for (int j = current.n - 1; j >= i; j--) {
            current.keys[j + 1] = current.keys[j];
        }
        current.keys[i] = left.keys[t - 1];
        current.n++;


        //clearing up node
        for (int j = t - 1; j < (2 * t - 1); j++) {
            left.keys[j] = null;
        }
        if (!left.leaf) {
            for (int j = t; j < (2 * t); j++) {
                left.children[j] = null;
            }
        }

        try {
            diskWrite(left);
            diskWrite(right);
            diskWrite(current);
        } catch (IOException e) {
            System.err.println("BTreeSplitChild: oh no bro");
            e.printStackTrace();
        }
        numNodes++;
    }

    long getSize() {
        return numObjects;
    }

    int getDegree() {
        return t;
    }

    long getNumberOfNodes(){
        return numNodes;
    }

    int getHeight() {
        //h ≤ log[base t]((n+1)/2)
        double d = (numNodes + 1) / 2;
        double base = t;

        //finding base t log
        double logBaseT = Math.log(d) / Math.log(base);
        return(int) Math.ceil(logBaseT);
    }

    public TreeObject search (String k){
        Node x = root;
        return searchRecur(x,k);
    }

    public TreeObject searchRecur (Node currentNode, String k){
        if (currentNode == null){
            System.err.println("Error in searchRecur: currentNode is null: " + currentNode);
            return null;
        }
        int i = 0;

        while (i < currentNode.n && k.compareTo(currentNode.keys[i].getKey()) > 0) {
            i++;
        }
        if (i < currentNode.n && k.equals(currentNode.keys[i].getKey())) {
            return currentNode.keys[i];
        } else if (currentNode.leaf){  //not in Tree
            return null;
        } else {
            /*
            Node newChild = new Node(false);
            try{
                newChild = diskRead(currentNode.children[i]);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return searchRecur(newChild, k);

             */
            try{
                Node child = diskRead(currentNode.children[i] == null ? 0L : currentNode.children[i]);
                return searchRecur(child, k);
            } catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }
    }

    /**
     * This method should be called when we want to increment a duplicate
     * and write it back into the disk.
     *
     * @param currentNode should always be root when called
     * @param k key that needs to be incremented
     */
    public void searchNode (Node currentNode, String k){
        if (currentNode == null){
            System.err.println("Error in searchRecur: currentNode is null: " + currentNode);
            return;
        }
        int i = 0;

        while (i < currentNode.n && k.compareTo(currentNode.keys[i].getKey()) > 0) {
            i++;
        }
        if (i < currentNode.n && k.equals(currentNode.keys[i].getKey())) {
            currentNode.keys[i].incCount();
            try{
                diskWrite(currentNode);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return;
        } else if (currentNode.leaf){
            //not in Tree
            System.out.println("Not in tree");
            return;
        } else {
            try{
                Node child = diskRead(currentNode.children[i] == null ? 0L : currentNode.children[i]);
                searchNode(child, k);
            } catch (Exception e){
                e.printStackTrace();
            }
            return;
        }
    }

    /**
     *
     * @returns number of bytes needed to represent a whole Node
     */
    private int calculateBytes(){
        return Integer.BYTES + 1 +((2*t-1) * TreeObject.BYTES) + ((2*t) * Long.BYTES);
    }

    public class Node {

        private int n; //num of keys in node
        private TreeObject[] keys;
        private Long[] children;
        private boolean leaf;
        private long address;
        public final int BYTES = Integer.BYTES + 1 +((2*t-1) * TreeObject.BYTES) + ((2*t) * Long.BYTES);

        public Node(){
            this.keys = new TreeObject[2 * BTree.this.t - 1];
            this.children = new Long[2 * BTree.this.t];
//            address = nextDiskAddress;
            this.address = nextDiskAddress;
            nextDiskAddress += nodeLength;
        }

        public Node(boolean makeNew){
            this.keys = new TreeObject[2 * BTree.this.t - 1];
            this.children = new Long[2 * BTree.this.t];
            if (makeNew) {
//                address = nextDiskAddress;
                this.address = nextDiskAddress;
                nextDiskAddress += nodeLength;
            } else {
                this.address = 0;
            }
        }

        /**
         * Constructor that builds a full node
         * This is used by diskRead, where all the data is already available
         *
         */
        public Node(int myN, TreeObject[] myKeys, Long[] myChildren, boolean isLeaf, long address, boolean onDisk) {
            this.keys = new TreeObject[2 * BTree.this.t - 1];
            this.children = new Long[2 * BTree.this.t];
            this.n = myN;
            this.leaf = isLeaf;

            if (onDisk) {
                this.address = address;
            } else {
                this.address = nextDiskAddress;
                nextDiskAddress += nodeLength;
            }

            if (address == 0L || myKeys == null || (!isLeaf && myChildren == null) ) {
                System.err.println("Something is null :/");
                System.err.println("address = " + address);
                System.err.println("myKeys = " + myKeys);
                System.err.println("myChildren = " + myChildren);
            }

            //copying keys
            if (myN <= (2*t -1)){ //making sure that myN is in bounds
                for (int i = 0; i < (2 * t -1); i++) {
                    this.keys[i] = myKeys[i];
                }
            }
//            else {
//                System.err.println("myN was not in bounds: " + myN + " / " + (2 * t -1));
//            }

            //copying children
            if (!leaf && myN <= (2*t) ){
                for (int i = 0; i <= myN; i++) {
                    this.children[i] = myChildren[i];
                }
            }
//            else {
//                System.err.println("myN was not in bounds: " + myN + " / " + (2 * t));
//            }
        }
    }

    /**
     * Read the metadata from the data file.
     * @throws IOException
     */
    public void readMetaData() throws IOException {
        file.position(0);

        ByteBuffer tmpbuffer = ByteBuffer.allocateDirect(METADATA_SIZE);

        tmpbuffer.clear();
        file.read(tmpbuffer);

        tmpbuffer.flip();
        rootAddress = tmpbuffer.getLong();
    }

    /**
     * Write the metadata to the data file.
     * @throws IOException
     */
    public void writeMetaData() throws IOException {
        file.position(0);

        ByteBuffer tmpbuffer = ByteBuffer.allocateDirect(METADATA_SIZE);

        tmpbuffer.clear();
        tmpbuffer.putLong(rootAddress);

        tmpbuffer.flip();
        file.write(tmpbuffer);
    }

    /**
     * Reads binary data from the disk at a given address, then fully builds a node
     * and returns it.
     *
     * @param diskAddress
     * @return
     * @throws IOException
     */
    public Node diskRead(long diskAddress) throws IOException {
        if (diskAddress == 0){
            System.out.println("FLAG: diskAddress is zero when calling diskAddress");
            return null;
        }

        file.position(diskAddress);
        buffer.clear();

        file.read(buffer);
        buffer.flip(); //flips the buffer so we can pull from the file

        TreeObject[] keys = new TreeObject[2*t - 1];
        Long[] children = new Long[2*t];

        //reading n
        int n = buffer.getInt();

        // reading leaf
        byte flag = buffer.get(); // read a byte
        boolean leaf = false;
        if (flag == 1)
            leaf = true;

        //reading keys
        for (int i = 0; i < (2 * t -1); i++) {
            //reading string
            StringBuilder builder = new StringBuilder();
            for (int j = 0; j < 32; j++){
                char tempChar = buffer.getChar();
                if (tempChar != 0) {
                    builder.append(tempChar);
                }
            }
            String tempKeyString = builder.toString();

            //reading the frequency
            long frequency = buffer.getLong();

            if (i < n){ //only builds valid keys for objects that are in bound
                keys[i] = new TreeObject(tempKeyString, frequency);
            } else {
                keys[i] = null;
            }
        }

        for (int i = 0; i < (t * 2); i++) {
            long tempChild = buffer.getLong();
            if (tempChild != 0L && i <= n){
                children[i] = tempChild;
            } else {
                children[i] = null;
            }
        }

        Node x = new Node(n, keys, children, leaf, diskAddress, true);
        return x;
    }

    /**
     * Writes a node into disk. This will fill any empty slots with null.
     *
     * @param x
     * @throws IOException
     */
    public void diskWrite(Node x) throws IOException {

        if (x.address == 0){
            x.address = nextDiskAddress;
            nextDiskAddress += nodeLength;
            System.out.println("FLAG: Node shifted address in diskWrite");
        }

        file.position(x.address); //file interception null
        buffer.clear();

        //writing n
        buffer.putInt(x.n); //made it all the way to here

        //writing leaf
        if (x.leaf)
            buffer.put((byte)1);
        else
            buffer.put((byte)0);

        //keys
        for (int i = 0; i < (2 * t -1); i++) {
            //once per object
            String tempString = "";
            if (x.keys[i] != null && i < x.n) {
                tempString = x.keys[i].getKey();
            }
            //writing string
            for (int j = 0; j < 32; j++){
                if (i < x.n && j < tempString.length()){
                    //makes sure we are in bounds of the array and that what we try to put != null
                    buffer.putChar(tempString.charAt(j));
                } else { //pads the rest of the space
                    //buffer.putChar(0);
                    buffer.putChar((char) 0);
                }
            }
            //writing the frequency
            long tempFrequency = 0L;
            if (x.keys[i] != null)
                tempFrequency = x.keys[i].getCount();
            buffer.putLong(tempFrequency);
        }

        //writing children
        for (int i = 0; i < (2 * t); i++) {
            long tempChild = 0L;
            if ((i <= x.n ) && x.children[i] != null)
                tempChild = x.children[i];
            buffer.putLong(tempChild);
        }

        buffer.flip();
        file.write(buffer);
    }

    public String[] getSortedKeyArray() throws IOException {
        ArrayList<String> sortedKeyArr = new ArrayList<>(); // empty arr for the keys
        recurGetSortedKeyArray(root, sortedKeyArr); // start with the root
        return sortedKeyArr.toArray(new String[0]); // return the sortedArr
    }

    private void recurGetSortedKeyArray(Node node, List<String> sortedKeyArr) throws IOException {
        if (node == null) return; // check that the node is not null

        if (node.leaf) {
            // if a leaf then already sorted
            for (int i = 0; i < node.n; i++) {
                if (node.keys[i] != null) {
                    sortedKeyArr.add(node.keys[i].getKey()); // add key to the array
                }
            }
            return; // stop the method
        }

        // loop through children of current node and get the address
        for (int i = 0; i < node.n; i++) {
            Long childAddr = node.children[i];
            if (childAddr != null) {
                Node child = diskRead(childAddr); // read the node
                if (child != null) {
                    recurGetSortedKeyArray(child, sortedKeyArr); // call method recrusively
                }
            }
            if (node.keys[i] != null) {
                sortedKeyArr.add(node.keys[i].getKey());
            }
        }

        // check the last child node
        Long lastAddr = node.children[node.n];
        if (lastAddr != null) {
            Node lastChild = diskRead(lastAddr); // read node
            if (lastChild != null) {
                recurGetSortedKeyArray(lastChild, sortedKeyArr);
            }
        }
    }

    /**
     * This function makes sure the file passed in exists, then calls recurPrintToDump
     * starting at root and writing to the file that was passed in.
     *
     * @param file that will store the output
     */
    public void dumpFile(PrintWriter file) {
//        if (!file.exists()) {
//            System.err.println("HELP!: file was not set up");
//        }

        try{
            //PrintWriter out = new PrintWriter(file);
            recurPrintToDump(root, file);
        } catch (IOException e){
            e.printStackTrace();
        }

    }

    private void recurPrintToDump(Node node, PrintWriter out) throws IOException {
        if (node == null) return; // check that the node is not null

        if (node.leaf) {
            // if a leaf then already sorted
            for (int i = 0; i < node.n; i++) {
                if (node.keys[i] != null) {
                    out.println(node.keys[i].getKey() + " " + node.keys[i].getCount());
                }
            }
            return; // stop the method
        }

        // loop through children of current node and get the address
        for (int i = 0; i < node.n; i++) {
            Long childAddr = node.children[i];
            if (childAddr != null) {
                Node child = diskRead(childAddr); // read the node
                if (child != null) {
                    recurPrintToDump(child, out); // call method recrusively
                }
            }
            if (node.keys[i] != null) {
                out.println(node.keys[i].getKey() + " " + node.keys[i].getCount());
            }
        }

        // check the last child node
        Long lastAddr = node.children[node.n];
        if (lastAddr != null) {
            Node lastChild = diskRead(lastAddr); // read node
            if (lastChild != null) {
                recurPrintToDump(lastChild, out);
            }
        }

    }


}