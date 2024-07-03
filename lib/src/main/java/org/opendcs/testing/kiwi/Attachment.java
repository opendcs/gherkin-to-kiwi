package org.opendcs.testing.kiwi;

/**
 * Attachment as referenced in a feature file. E.G graphics, charts, etc
 */
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
