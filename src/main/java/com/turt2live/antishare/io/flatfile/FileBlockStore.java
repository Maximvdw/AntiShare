package com.turt2live.antishare.io.flatfile;

import com.turt2live.antishare.ASLocation;
import com.turt2live.antishare.BlockType;
import com.turt2live.antishare.io.generics.GenericBlockStore;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * Represents a file used for storing block information.
 * <br/><br/>
 * This uses a binary file to store the information contained within as a continuous
 * data format. The file contains a header before the continuous data. Internally
 * nothing is saved to disk until {@link #save()} is called, therefore everything
 * is stored within memory. It should be noted that {@link #load()} will NOT clear
 * the store, instead, {@link #clear()} should be used to clear the store of data
 * before loading. However, {@link #load()} will overwrite the data, if present, in
 * the store while loading. This functionality may be useful in the event of a needed
 * merge between the file system and the memory, where the file system gets the priority
 * on the data stored. The entire file is ordered with big endian data.
 * <br/><br/>
 * The block size is the number of permitted blocks per dimension. Therefore, the block
 * size taken to the power of 3 will represent the total number of blocks the file can
 * store. To calculate the theoretical size of the resulting file, take the block size
 * to the power 3, multiply the result by 13, and add 16 to the final result. This
 * calculation determines the total number of blocks, calculates the size for the blocks
 * (at 13 bytes per block) and adds the header size to the result (16 bytes). A block
 * size of 50 will result in 125,000 possible block locations which takes up (if filled)
 * a theoretical total of 1,625,000 bytes (about 1.55 MB). When adding the header, the
 * total theoretical file size becomes 1,625,016 (about 1.55 MB).
 * <br/><br/>
 * The header consists of 4 4 byte integers (16 bytes total) representing the "store
 * location". This location is an X/Y/Z location alongside the block size. The X/Y/Z
 * location is the position for the  store when using the block size as a reference.
 * The header is formatted as X, Y, Z, then the block size. To calculate the store
 * location using the block size, floor the result of dividing the desired component
 * (X, Y, or Z) by the block size.
 * <br/><br/>
 * After the header is continuous data representing block information. The block
 * information consists of a byte flag for the {@link BlockType} and 3 4 byte integers
 * for the location of the block (13 bytes total). The data is formatted as flag, block X,
 * block Y, block Z with no leading or trailing bits. The methods {@link #byteToType(byte)}
 * and {@link #typeToByte(com.turt2live.antishare.BlockType)} can be used to convert
 * between the byte flag and block type.
 * <br/><br/>
 * This type of data store is designed to be used with the {@link FileBlockManager}
 *
 * @author turt2live
 * @see FileBlockStore
 */
public class FileBlockStore extends GenericBlockStore {

    private File file;
    private ByteBuffer buffer = ByteBuffer.allocateDirect(13);
    private ByteBuffer headerBuffer = ByteBuffer.allocateDirect(16);
    private final int[] header;

    /**
     * Creates a new file block store using a specified header
     *
     * @param file   the file to load, must not be null
     * @param sx     the header X
     * @param sy     the header Y
     * @param sz     the header Z
     * @param blocks the number of permitted blocks per dimension
     */
    public FileBlockStore(File file, int sx, int sy, int sz, int blocks) {
        if (file == null) throw new IllegalArgumentException("file cannot be null");

        this.file = file;
        header = new int[]{sx, sy, sz, blocks};

        // Setup buffer order
        headerBuffer.order(ByteOrder.BIG_ENDIAN);
        buffer.order(ByteOrder.BIG_ENDIAN);
    }

    /**
     * Creates a new file block store using no header (all zeros)
     *
     * @param file the file to load, must not be null
     */
    public FileBlockStore(File file) {
        if (file == null) throw new IllegalArgumentException("file cannot be null");

        this.file = file;
        header = new int[4];
    }

    /**
     * Gets a cloned copy of the header information
     *
     * @return the header information
     */
    public int[] getHeader() {
        return header.clone();
    }

    @Override
    public void save() {
        FileOutputStream output = null;
        try {
            file.delete();
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();
            file.createNewFile();

            output = new FileOutputStream(file);
            FileChannel channel = output.getChannel();

            // Write header
            headerBuffer.clear();
            headerBuffer.putInt(header[0]);
            headerBuffer.putInt(header[1]);
            headerBuffer.putInt(header[2]);
            headerBuffer.putInt(header[3]);
            headerBuffer.flip();
            channel.write(headerBuffer);

            // Write blocks
            ConcurrentMap<ASLocation, BlockType> blocks = getLiveMap();
            for (Map.Entry<ASLocation, BlockType> entry : blocks.entrySet()) {
                // Don't save "unknown" or otherwise "free" blocks
                if (entry.getValue() != BlockType.UNKNOWN) {
                    byte typeByte = typeToByte(entry.getValue());
                    ASLocation location = entry.getKey();

                    // Write the buffer
                    buffer.clear();
                    buffer.put(typeByte);
                    buffer.putInt(location.X);
                    buffer.putInt(location.Y);
                    buffer.putInt(location.Z);
                    buffer.flip();
                    channel.write(buffer);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                }
            }
        }
    }

    @Override
    public void load() {
        FileInputStream input = null;
        try {
            if (!file.exists()) return;

            input = new FileInputStream(file);
            FileChannel channel = input.getChannel();

            // Read header
            int read = loadHeader(channel);
            if (read == headerBuffer.capacity()) {
                // Read blocks
                while ((read = channel.read(buffer)) > -1) {
                    if (read == buffer.capacity()) {
                        buffer.position(0);
                        BlockType type = byteToType(buffer.get());
                        int x = buffer.getInt();
                        int y = buffer.getInt();
                        int z = buffer.getInt();
                        setType(x, y, z, type);
                        buffer.clear();
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                }
            }
        }
    }

    @Override
    public BlockType getType(ASLocation location) {
        if (location == null) throw new IllegalArgumentException("location cannot be null");

        // Validate block size range
        int sx = (int) Math.floor(location.X / (double) header[3]);
        int sy = (int) Math.floor(location.Y / (double) header[3]);
        int sz = (int) Math.floor(location.Z / (double) header[3]);

        if (sx != header[0] || sy != header[1] || sz != header[2])
            throw new IllegalArgumentException("location is out of range");

        return super.getType(location);
    }

    @Override
    public void setType(ASLocation location, BlockType type) {
        if (location == null) throw new IllegalArgumentException("location cannot be null");

        // Validate block size range
        int sx = (int) Math.floor(location.X / (double) header[3]);
        int sy = (int) Math.floor(location.Y / (double) header[3]);
        int sz = (int) Math.floor(location.Z / (double) header[3]);

        if (sx != header[0] || sy != header[1] || sz != header[2])
            throw new IllegalArgumentException("location is out of range");

        super.setType(location, type);
    }

    private int loadHeader(FileChannel channel) throws IOException {
        // Read header
        int read = channel.read(headerBuffer);
        if (read == headerBuffer.capacity()) {
            headerBuffer.position(0);
            header[0] = headerBuffer.getInt();
            header[1] = headerBuffer.getInt();
            header[2] = headerBuffer.getInt();
            header[3] = headerBuffer.getInt();
            headerBuffer.clear();
        }
        return read;
    }

    /**
     * Loads the header without loading the entire file's block data
     */
    public void loadHeader() {
        FileInputStream input = null;
        try {
            if (!file.exists()) return;

            input = new FileInputStream(file);
            FileChannel channel = input.getChannel();

            // Read header
            loadHeader(channel);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * Converts a block type to a byte flag
     *
     * @param type the type to convert. Null is assumed to be {@link BlockType#UNKNOWN}
     * @return the byte flag representation of the block type
     */
    public static byte typeToByte(BlockType type) {
        if (type == null) type = BlockType.UNKNOWN;
        switch (type) {
            case CREATIVE:
                return 0x01;
            case SURVIVAL:
                return 0x02;
            case ADVENTURE:
                return 0x03;
            case SPECTATOR:
                return 0x04;
            case UNKNOWN:
                return 0x05;
            default:
                return 0x09;
        }
    }

    /**
     * Converts a byte flag to a block type
     *
     * @param type the byte flag to convert. Unknown values return {@link BlockType#UNKNOWN}
     * @return the block type, never null
     */
    public static BlockType byteToType(byte type) {
        switch (type) {
            case 0x01:
                return BlockType.CREATIVE;
            case 0x02:
                return BlockType.SURVIVAL;
            case 0x03:
                return BlockType.ADVENTURE;
            case 0x04:
                return BlockType.SPECTATOR;
            case 0x05:
                return BlockType.UNKNOWN;
            default:
                return null;
        }
    }
}
