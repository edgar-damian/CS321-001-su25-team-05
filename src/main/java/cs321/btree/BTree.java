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

    private long rootAddress = METADATA_SIZE;
    private Node root;

    /**
     * Constructor
     */
    public BTree() {
        Node x = new Node();
        x.leaf = true;
        x.n = 0;
        try {
            diskWrite(x);
        } catch (IOException e) {
            e.printStackTrace();
        }
        root = x;
        nodeSize++;
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
            while (i >= 0 && obj < x.keys[i]) {
                x.keys[i + 1] = x.keys[i];
                i++;
            }
            x.keys[i + 1] = obj;
            x.n++;
            diskWrite(x);
        } else {
            while (i >= 0 && obj < x.keys[i]) {
                i--;
            }
            diskRead(x.c[i]);
            if (x.c[i].n == 2 t - 1){
                BTreeSplitChild(x, i);
                if (obj.getKey() > x.keys[i]) {
                    i++;
                }
            }
            BTreeInsertNonFull(x.c[i], obj);
        }
    }

        public Node BTreeSplitRoot() {
            Node s = new Node();
            s.leaf = false;
            s.n = 0;
            s.children[0]=root;
            root=s;
            BTreeSplitChild(s,0);
            return s;
        }

        public void BTreeSplitChild(Node x,int i)
        {
            Node y = x.children;
            Node z= new Node();
            z.leaf=y.leaf;
            z.n=t-1;
            for(int j=0; j<t; j++)
            {
                z.keys[j] = y.keys[j+t];
            }
            if(!y.leaf)
            {
                for(int j=0;j<t+1;j++)
                {
                    z.c[j]=y.c[j+t];
                }
            }
            y.n=t-1;
            for(int j=x.n;j>i;j--) {
                x.c[j]=x.c[j-1];
            }
            x.c[i]=z;
            for(int j=x.n; j>=i; j--) {
                x.keys[j]=x.keys[j-1];
            }
            x.keys[i-1]=y.keys[t-1];
            x.n++;
            diskWrite(y);
            diskWrite(z);
            diskWrite(x);
        }

        /**
         * @author Edgar
         *
         * This method will search through a BTree, it starts at Node x, if they key we look for is not
         * in the array of keys, it will recursively call itself in the corresponding children array and
         * start a new search there, and so on until either the key k is found or null is returned.
         */
        public Node search (Node x, int k){
            int i = 1;
            while (i <= x.n && k > x.keys[i]){
                i++;
            }
            if (i <= x.n && k == x.keys[i]){
                return (x, i);
            }
            else if (x.leaf){
                return null;
            }
            else {
                diskRead(x.children[i]);
                return search(x.children[i], k);
            }
            return null;
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

            ArrayList<Integer> keys = new ArrayList<>(); //array of ints INSIDE a node
            ArrayList<Long> children = new ArrayList<>(); //array of pointers to children

            //reading n
            int n = buffer.getInt();

            // reading leaf
            byte flag = buffer.get(); // read a byte
            boolean leaf = false;
            if (flag == 1)
                leaf = true;

            //reading keys
            for (int i = 0; i < (2 * t -1); i++) {
                long tempKey = buffer.getLong();
                if (i < n)
                    keys.add((int) tempKey);
            }

            //reading children
            for (int i = 0; i < (2 * t); i++) {
                long tempChild = buffer.getLong();
                if ((i < n + 1) && (!leaf))
                    children.add(tempChild);
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
                long tempKey = x.keys.get(i);
                if (i < tempN)
                    buffer.putLong(tempKey);
                else
                    buffer.put((byte)0);
            }

            //writing children
            for (int i = 0; i < (2 * t); i++) {
                long tempChild = x.children.get(i);
                if ((i < tempN + 1) && (!tempLeaf))
                    buffer.putLong(tempChild);
                else
                    buffer.put((byte)0);
            }

            buffer.flip();
            file.write(buffer);
        }
    }