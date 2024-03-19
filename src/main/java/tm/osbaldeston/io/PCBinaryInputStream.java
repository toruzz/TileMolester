package tm.osbaldeston.io;

/** 
  * PCBinaryInputStream
  *
  * @author Richard J.Osbaldeston
  * @version 1.1 02/08/98
  * @copyright Richard J.Osbaldeston (http://www.osbald.co.uk)
  */

import java.io.*;
import java.net.URL;

public class PCBinaryInputStream {

    DataInputStream file;

    public PCBinaryInputStream(File f) throws IOException
    {
        file = new DataInputStream(new BufferedInputStream(new FileInputStream(f)));
    }

    public PCBinaryInputStream(URL url) throws IOException
    {
        file = new DataInputStream(new BufferedInputStream((url.openConnection()).getInputStream()));
    }

    public int readInt() throws IOException
    {
        int i=file.readInt();
        return ((i<<24)|((i&0x0000FF00)<<8)|((i&0x00FF0000)>>>8)|(i>>>24));
    }

    public short readShort() throws IOException
    {
        int i=file.readUnsignedShort();
        return (short)((i<<8)|(i>>>8));
    }

    public byte readByte() throws IOException
    {
        return (byte)file.readUnsignedByte();
    }

    public void readByteArray(byte b[]) throws IOException
    {
        file.readFully(b);
    }

    public void skip(long nbytes) throws IOException
    {
        file.skip(nbytes);
    }

    public void close() throws IOException
    {
        file.close();
        file=null;
    }

    public void finalize() {
        if (file != null) {
            try {
                close();
            } catch (IOException e) {}
        }
    }
}

