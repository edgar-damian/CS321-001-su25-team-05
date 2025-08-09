package cs321.btree;

import cs321.btree.TreeObject; //no prog

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;


import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.Random;

public class BTree {

    private int METADATA_SIZE = Long.BYTES;
    private long nextDiskAddress = METADATA_SIZE;
    private FileChannel file;
    private ByteBuffer buffer;
    private int nodeLength; //

    private int t; //should be initialized by constructor?
    private int numNodes = 0; //number of nodes in the tree
    private int numObjects = 0; //number of objects inside each node
    private long rootAddress = METADATA_SIZE;
    private Node root;

    /**
     * Constructor
     */
    BTree(String fileNameString) {
        t=3;
        Node x = new Node(true);
        x.leaf = true;
        x.n = 0;

        root = x;
        numNodes++;

        nodeLength = calculateBytes();
        buffer = ByteBuffer.allocateDirect(calculateBytes());

        File fileName = new File(fileNameString);


        try {
            if (!fileName.exists()) {
                fileName.createNewFile(); // IOException
                RandomAccessFile dataFile = new RandomAccessFile(fileName, "rw");
                file = dataFile.getChannel();
                writeMetaData(); // IOException
            } else {
                RandomAccessFile dataFile = new RandomAccessFile(fileName, "rw");
                file = dataFile.getChannel();
                readMetaData(); // IOException
                root = diskRead(rootAddress); // IOException
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            diskWrite(x);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    BTree(int degree,String fileNameString) {
        t = degree;
        Node x = new Node(true);
        x.leaf = true;
        x.n = 0;
        root = x;
        numNodes++;
        nodeLength = calculateBytes();


//nodeSize = x.BYTES; // this line causes an error
        buffer = ByteBuffer.allocateDirect(calculateBytes());

        File fileName = new File(fileNameString);

        try {
            if (!fileName.exists()) {
                fileName.createNewFile(); // IOException
                RandomAccessFile dataFile = new RandomAccessFile(fileName, "rw");
                file = dataFile.getChannel();
                writeMetaData(); // IOException
            } else {
                RandomAccessFile dataFile = new RandomAccessFile(fileName, "rw");
                file = dataFile.getChannel();
                readMetaData(); // IOException
                root = diskRead(rootAddress); // IOException
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            diskWrite(x);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public void insert (TreeObject obj){
        Node r=root;
        if (r.n == ( (2*t) - 1)) {
            Node s = BTreeSplitRoot();
            BTreeInsertNonFull(s, obj);
        } else {
            BTreeInsertNonFull(r, obj);
        }
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
            try {
                diskRead(x.children[i]);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Node childNode = new Node(false);
            try {
                childNode = diskRead(x.children[i]);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (childNode.n == (2 * t - 1)) {
                BTreeSplitChild(x, i);
//                if (obj.compareTo(x.keys[i]) > 0) {
//                    i++;
//                }
                if (obj.compareTo(x.keys[i]) > 0) {
                    i++;
                }

                childNode = null;
                try {
                    childNode = diskRead(x.children[i]);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            BTreeInsertNonFull(childNode, obj);
        }
        numObjects++;
    }

    public Node BTreeSplitRoot() {
        Node s = new Node(true); //going to be the root/parent
        s.leaf = false;
        s.n = 0;

        s.children[0] = root.address;
        root = s;
        BTreeSplitChild(s,0); //error called on here
        numNodes++;
        return s;
    }

    public void BTreeSplitChild(Node x,int i) {
        //Node y = x.children;
        Node y = new Node(true);
        try{
            y = diskRead(x.children[i]);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Node z= new Node(true);
        z.leaf=y.leaf;
        z.n=t-1;
        for(int j=0; j <= t-2; j++)
        {
            z.keys[j] = y.keys[j+t];
        }
        if(!y.leaf)
        {
            for(int j=0;j<t-1;j++)
            {
                z.children[j]=y.children[j+t];
            }
        }
        y.n = t-1;

        //go back here if error
        //for(int j=x.n; j == i + 1; j--) {
        for (int j = x.n; j >= i + 1; j--){
            x.children[j+1]=x.children[j];
        }
        //

        x.children[1 + i] = z.address;
        for(int j = x.n - 1; j >= i; j--) {
            x.keys[j+1]=x.keys[j];
        }
        x.keys[i]=y.keys[t-1];
        x.n++;

        try {
            diskWrite(y);
            diskWrite(z);
            diskWrite(x);
        } catch (IOException e) {
            e.printStackTrace();
        }
        numNodes++;
    }

    long getSize()
    {
        return numObjects;
    }

    int getDegree()
    {
        return t;
    }

    long getNumberOfNodes()
    {
        return numNodes;
    }

    int getHeight()
    {
        return 0;
    }

    /**
     * @author Edgar
     *
     * This method will search through a BTree, it starts at Node x, if they key we look for is not
     * in the array of keys, it will recursively call itself in the corresponding children array and
     * start a new search there, and so on until either the key k is found or null is returned.
     */
    public TreeObject search (String k){
        Node x = root;
        int i = 0;

        while (i < x.n && k.compareTo(x.keys[i].getKey()) > 0){
            i++;
        }
        //if (i < x.n && k == x.keys[i].getKey()){
        if (i < x.n && k.equals(x.keys[i].getKey())) {
            return x.keys[i];
        }
        else if (x.leaf){
            //not in Tree
            return null;
        }
        else {
            Node newChild = new Node(false);
            try{
                newChild = diskRead(x.children[i]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return searchRecur(newChild, k);
        }
        //return null;
    }

    public TreeObject searchRecur (Node currentChild, String k){

        Node x = currentChild;
        int i = 0;
        //while (i <= x.n && k.compareTo(x.keys[i].getKey()) > 0){
        while (i < x.n && k.compareTo(x.keys[i].getKey()) > 0) {
            i++;
        }
        //if (i <= x.n && k == x.keys[i].getKey()){
        if (i < x.n && k.equals(x.keys[i].getKey())) {
            return x.keys[i];
        }
        else if (x.leaf){
            //not in Tree
            return null;
        }
        else {
            Node newChild = new Node(false);
            try{
                newChild = diskRead(x.children[i]);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return searchRecur(newChild, k);
        }
        //return null;
    }

    /**
     * This method calculates the number of bytes needed per each node
     * The calculation is as follows:
     *
     * n: Integer.BYTES
     * leaf: '1'
     * keys: ((2 * t -1) * Long.BYTES)
     * children: ((2 * t) * Long.BYTES)
     *
     * @return number of bytes needed per node
     * */
    private int calculateBytes(){
        //return Integer.BYTES + 1 +((2*t-1) * Long.BYTES) + ((2*t) * Long.BYTES);
        return Integer.BYTES + 1 +((2*t-1) * TreeObject.BYTES) + ((2*t) * Long.BYTES);
    }

    private class Node {

        private int n; //represents the number of keys inside the node

        //private String[] keys; //array of strings inside a node
        private TreeObject[] keys;// = new TreeObject[2*t - 1];
        private Long[] children;// = new Long[ 2*t ]; //array of pointers to children
        private boolean leaf;
        private long address;


        //public final int BYTES = Integer.BYTES + 1 +((2*t-1) * Long.BYTES) + ((2*t) * Long.BYTES);

        public final int BYTES = Integer.BYTES + 1 +((2*t-1) * TreeObject.BYTES) + ((2*t) * Long.BYTES);
        /**
         * Basic constructor for a Node. This grabs all the variables we will use
         *
         * @param myN
         * @param myKeys an array of TreeObjects
         * @param myChildren an array of children (Longs)
         * @param isLeaf
         * @param address current address, gets updates if onDisk is true
         * @param onDisk
         */
        public Node(int myN, TreeObject[] myKeys, Long[] myChildren, boolean isLeaf, long address, boolean onDisk) {

            this.keys = new TreeObject[2 * BTree.this.t - 1];
            this.children = new Long[2 * BTree.this.t];

            this.n = myN;
            this.leaf = isLeaf;
            this.address = address;

            //copying keys
            for (int i = 0; i < (2 * t -1); i++) {
                TreeObject tempKey = myKeys[i]; //grabs each key one by one
                if (i < n)
                    keys[i] = tempKey;
            }

            if (!leaf){
                //copying children
                for (int i = 0; i < (2 * t); i++) {

                    //only grabbing from the known array
                    if (i <= n){
                        Long tempChild = myChildren[i];
                        if (tempChild != null){
                            children[i] = tempChild;
                        } else {
                            children[i] = null;
                        }
                    }
                }
            }

            if (onDisk) {
                address = nextDiskAddress;
                nextDiskAddress += nodeLength;
            }
        }

        /**
         * Empty Node constructor, should ONLY be called when making a NEW node that
         * has not been in the disk.
         */
        public Node(){
            this.keys = new TreeObject[2 * BTree.this.t - 1];
            this.children = new Long[2 * BTree.this.t];
            //
            address = nextDiskAddress;
            nextDiskAddress += nodeLength;
            //
        }

        public Node(boolean onDisk){
            this.keys = new TreeObject[2 * BTree.this.t - 1];
            this.children = new Long[2 * BTree.this.t];
            //

            if (onDisk) {
                address = nextDiskAddress;
                nextDiskAddress += nodeLength;
            }
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
     * @author Edgar
     *
     * This function will read from the disk, take that information and
     * put it in a new node called x. This will be returned at the end.
     *
     * Order of reading:
     *      n
     *      leaf
     *      keys
     *      children
     *
     * @param diskAddress
     * @return
     * @throws IOException
     */
    public Node diskRead(long diskAddress) throws IOException {
        if (diskAddress == 0) return null;

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
        for (int i = 0; i < n; i++) {
            //once per object that is actually there (According to n)

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

            //making key objects and adding them
            keys[i] = new TreeObject(tempKeyString, frequency);
        }

        //reading children if necessary
        if (!leaf) {
            for (int i = 0; i <= n; i++) {
                long tempChild = buffer.getLong();
                children[i] = tempChild;
            }
        }


        Node x = new Node(n, keys, children, leaf, diskAddress, true);
        return x;
    }

    /**
     * @author Edgar
     *
     * This function will take in a node as a parameter and write it onto the disk.
     * If the keys or children arrays are not fully occupied, the function will
     * fill the remainder with 0.
     *
     * Order of writing:
     *      n
     *      leaf
     *      keys
     *      children
     *
     * @param x
     * @throws IOException
     */
    public void diskWrite(Node x) throws IOException {
        file.position(x.address); //file interception null
        buffer.clear();
        int tempN = x.n;
        boolean tempLeaf = x.leaf;

        //writing n
        buffer.putInt(x.n); //made it all the way to here

        //writing leaf
        if (x.leaf)
            buffer.put((byte)1);
        else
            buffer.put((byte)0);

        //keys -----------------------------------------------------------------------------------------------
        for (int i = 0; i < (2 * t -1); i++) {
            //once per object
            String tempString = "";
            if (x.keys[i] != null && i < tempN ) {
                tempString = x.keys[i].getKey();
            }

            //writing string
            for (int j = 0; j < 32; j++){
                if (i < tempN && j < tempString.length()){
                    //makes sure we are in bounds of the array and that what we try to put != null
                    buffer.putChar(tempString.charAt(j));
                } else { //pads the rest of the space
                    //buffer.putChar(0); //
                    buffer.putChar((char) 0);
                }
            }

            //writing the frequency
            long tempFrequency = 0L;
            if (x.keys[i] != null)
                tempFrequency = x.keys[i].getCount();

            buffer.putLong(tempFrequency);
        } //-------------------------------------------------------------------------------------------------------


        //children -----------------------------------------------------------------------------------------------
        if (!tempLeaf) {
            //writing children
            for (int i = 0; i < (2 * t); i++) {
                long tempChild = 0L;
                if (x.children[i] != null)
                    tempChild = x.children[i];

                if ((i < tempN + 1 ) && x.children[i] != null)
                    buffer.putLong(tempChild);
                else
                    buffer.putLong(0L);
            }
        }
        //-------------------------------------------------------------------------------------------------------
        buffer.flip();
        file.write(buffer);
    }
}