import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Random;

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
        private ArrayList<Integer> keys; //array of ints inside a node
        private ArrayList<Long> children; //array of pointers to children
        private boolean leaf;
        private long address;

        //public static final int BYTES = Integer.BYTES + 1 +((2*t-1) * Long.BYTES) + ((2*t) * Long.BYTES);
        public static final int BYTES = calculateBytes();

        /**
         * Basic constructor for a Node. This grabs all the variables we will use
         *
         * @param myN
         * @param myKeys an array of keys
         * @param myChildren an array of children
         * @param isLeaf
         * @param address current address, gets updates if onDisk is true
         * @param onDisk
         */
        public Node(int myN, ArrayList<Integer> myKeys, ArrayList<Long> myChildren, boolean isLeaf, long address, boolean onDisk) {

            this.n = myN;
            this.leaf = isLeaf;
            this.address = address;

            //copying keys
            for (int i = 0; i < (2 * t -1); i++) {
                int tempKey = myKeys.get(i);
                if (i < n)
                    keys.add(tempKey);
            }

            //copying children
            for (int i = 0; i < (2 * t); i++) {
                long tempChild = myChildren.get(i);
                if ((i < n + 1) && (!leaf))
                    children.add(tempChild);
            }

            if (onDisk) {
                address = nextDiskAddress;
                nextDiskAddress += nodeSize;
            }
        }
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