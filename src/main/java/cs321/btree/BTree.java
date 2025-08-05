package cs321.btree;

import cs321.btree.TreeObject;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

public class BTree {

    private int METADATA_SIZE = Long.BYTES;
    private long nextDiskAddress = METADATA_SIZE;
    private FileChannel file;
    private ByteBuffer buffer;
    private int nodeSize;
    private static int t; //should be initialized by constructor?
    private int numNodes = 0; //number of nodes in the tree
    private long rootAddress = METADATA_SIZE;
    private Node root;

            /**
             * Constructor
             */
    BTree(String fileName) {
        Node x = new Node();
        x.leaf = true;
        x.n = 0;
        t=3;
        try {
            diskWrite(x);
        } catch (IOException e) {
            e.printStackTrace();
        }
        root = x;
        numNodes++;
    }

    BTree(int degree,String fileName)
    {
        Node x = new Node();
        x.leaf = true;
        x.n = 0;
        t=degree;
        try {
            diskWrite(x);
        } catch (IOException e) {
            e.printStackTrace();
        }
        root = x;
        numNodes++;
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
                i++;
            }
            x.keys[i + 1] = obj;
            x.n++;
            try{
                diskWrite(x);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        else {
            while (i >= 0 && obj.compareTo(x.keys[i])<0) {
                i--;
            }
            i++;
            try{
                diskRead(x.children[i]);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Node childNode = new Node();
            try {
                childNode = diskRead(x.children[i]);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (childNode.n == (2*t - 1)){
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
    }

        public Node BTreeSplitRoot() {
            Node s = new Node(); //going to be the root/parent
            s.leaf = false;
            s.n = 0;

            s.children[0] = root.address;
            root = s;
            BTreeSplitChild(s,0);
            numNodes++;
            return s;
        }

        public void BTreeSplitChild(Node x,int i)
        {
            //Node y = x.children;
            Node y = new Node();
            try{
                y = diskRead(x.children[i]);
            } catch (IOException e) {
                e.printStackTrace();
            }

            Node z= new Node();
            z.leaf=y.leaf;
            z.n=t-1;
            for(int j=0; j<t-2; j++)
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
            for(int j=x.n; j == i + 1; j--) {
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
            return nodeSize;
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
            while (i <= x.n && k.compareTo(x.keys[i].getKey()) > 0){
                i++;
            }
            if (i <= x.n && k == x.keys[i].getKey()){
                return x.keys[i];
            }
            else if (x.leaf){
                //not in Tree
                return null;
            }
            else {
                Node newChild = new Node();
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
            while (i <= x.n && k.compareTo(x.keys[i].getKey()) > 0){
                i++;
            }
            if (i <= x.n && k == x.keys[i].getKey()){
                return x.keys[i];
            }
            else if (x.leaf){
                //not in Tree
                return null;
            }
            else {
                Node newChild = new Node();
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
         */
        private static int calculateBytes(){
            return Integer.BYTES + 1 +((2*t-1) * Long.BYTES) + ((2*t) * Long.BYTES);
        }

        private class Node {

            private int n; //represents the number of keys inside the node

            //private String[] keys; //array of strings inside a node
            private TreeObject[] keys = new TreeObject[2*t - 1];


            private Long[] children = new Long[ 2*t ]; //array of pointers to children
            private boolean leaf;
            private long address;

            //public static final int BYTES = Integer.BYTES + 1 +((2*t-1) * Long.BYTES) + ((2*t) * Long.BYTES);
            public static final int BYTES = calculateBytes();

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

                this.n = myN;
                this.leaf = isLeaf;
                this.address = address;

                //copying keys
                for (int i = 0; i < (2 * t -1); i++) {
                    TreeObject tempKey = myKeys[i]; //grabs each key one by one
                    if (i < n)
                        keys[i] = tempKey;
                }

                //copying children
                for (int i = 0; i < (2 * t); i++) {
                    long tempChild = myChildren[i];
                    if ((i < n + 1) && (!leaf))
                        children[i] = tempChild;
                }

                if (onDisk) {
                    address = nextDiskAddress;
                    nextDiskAddress += nodeSize;
                }
            }

            /**
             * Empty Node constructor
             */
            public Node(){}
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
            Long[] children = new Long[2*t - 1];

            //reading n
            int n = buffer.getInt();

            // reading leaf
            byte flag = buffer.get(); // read a byte
            boolean leaf = false;
            if (flag == 1)
                leaf = true;




            /*
            String temp = buffer.getChar();
                if (i < n)
                    keys.add((int) tempKey);
             */
            //reading keys
            for (int i = 0; i < (2 * t -1); i++) {

            }

            //reading children
            for (int i = 0; i < (2 * t); i++) {
                long tempChild = buffer.getLong();
                if ((i < n + 1) && (!leaf))
                    children[i] = tempChild;
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
            file.position(x.address);
            buffer.clear();
            int tempN = x.n;
            boolean tempLeaf = x.leaf;

            //writing n
            buffer.putInt(x.n);

            //writing leaf
            if (x.leaf)
                buffer.put((byte)1);
            else
                buffer.put((byte)0);

            //writing keys
            for (int i = 0; i < (2 * t -1); i++) {
                if (i < tempN && x.keys[i] != null){
                    String tempString = x.keys[i].getKey();
                    buffer.put(tempString.getBytes());
                } else {
                  //so we are either out of bounds or there is nothing to write
                  buffer.put((byte)0);
                }
            }

            //writing children
            for (int i = 0; i < (2 * t); i++) {
                long tempChild = x.children[i];
                if ((i < tempN + 1) && (!tempLeaf))
                    buffer.putLong(tempChild);
                else
                    buffer.put((byte)0);
            }

            buffer.flip();
            file.write(buffer);
        }
    }