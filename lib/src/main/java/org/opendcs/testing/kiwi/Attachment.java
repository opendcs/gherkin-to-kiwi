package org.opendcs.testing.kiwi;

public class Attachment
{
    public final String filename;
    public final byte[] data;

    public Attachment(String filename, byte[] data)
    {
        this.filename = filename;
        this.data = data;
    }
}
