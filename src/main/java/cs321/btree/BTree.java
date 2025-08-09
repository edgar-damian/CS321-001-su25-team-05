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
    private int nodeLength; //the length of each node in bytes

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

    private int calculateBytes(){
        return Integer.BYTES + 1 +((2*t-1) * TreeObject.BYTES) + ((2*t) * Long.BYTES);
    }

    private class Node {

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

        public Node(int myN, TreeObject[] myKeys, Long[] myChildren, boolean isLeaf, long address, boolean onDisk) {
            this.keys = new TreeObject[2 * BTree.this.t - 1];
            this.children = new Long[2 * BTree.this.t];
            this.n = myN;
            this.leaf = isLeaf;

            //my attempt vs alternative
            /*
            this.address = address;
            if (onDisk) {
                address = nextDiskAddress;
                nextDiskAddress += nodeLength;
            }

             */

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
            } else {
                System.err.println("myN was not in bounds: " + myN + " / " + (2 * t -1));
            }

            //copying children
            if (!leaf && myN <= (2*t) ){
                for (int i = 0; i <= myN; i++) {
                    this.children[i] = myChildren[i];
                }
            } else {
                System.err.println("myN was not in bounds: " + myN + " / " + (2 * t));
            }

            /**
            if (myKeys != null) {
                for (int i = 0; i < Math.min(myN, 2 * t - 1); i++) {
                    this.keys[i] = myKeys[i];
                }
            }

            if (!leaf && myChildren != null) {
                for (int i = 0; i < Math.min(myN + 1, 2 * t); i++) {
                    this.children[i] = myChildren[i];
                }
            }
            */

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