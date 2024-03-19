package tm.osbaldeston.io;

/** 
  * PCBinaryOutputStream
  *
  * @author Richard J.Osbaldeston
  * @version 1.1 02/08/98
  * @copyright Richard J.Osbaldeston (http://www.osbald.co.uk)
  */

import java.io.*;
import java.net.URL;

public class PCBinaryOutputStream {

    DataOutputStream file;

    public PCBinaryOutputStream(File f) throws IOException
    {
        file = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f)));
    }

    public PCBinaryOutputStream(URL url) throws IOException
    {
        file = new DataOutputStream(new BufferedOutputStream((url.openConnection()).getOutputStream()));
    }

    public void writeInt(int i) throws IOException
    {
        int j=(i<<24)|((i&0x0000FF00)<<8)|((i&0x00FF0000)>>>8)|(i>>>24);
        file.writeInt(j);
    }

    public void writeShort(short i) throws IOException
    {
        int j=(i<<8)|(i>>>8);
        file.writeShort((short)j);
    }

    public void writeByte(byte i) throws IOException
    {
        file.writeByte(i);
    }

    public void writeByteArray(byte b[]) throws IOException
    {
        file.write(b);
    }

    public long getFilePointer() throws IOException
    {
        return file.size();
    }

    public long size() throws IOException
    {
        return file.size();
    }

    public void close() throws IOException
    {
        file.flush();
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

