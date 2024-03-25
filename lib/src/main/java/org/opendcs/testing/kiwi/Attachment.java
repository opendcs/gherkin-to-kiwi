package org.opendcs.testing.kiwi;

public class Attachment
{
    private String filename;
    private byte[] data;

    public Attachment(String filename, byte[] data)
    {
        this.filename = filename;
        this.data = data;
    }

    public String getFilename()
    {
        return filename;
    }

    public byte[] getData()
    {
        return this.data;
    }
}
