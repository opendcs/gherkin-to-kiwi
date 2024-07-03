package org.opendcs.testing.kiwi.tags;

public interface KiwiTag
{

    public static KiwiTag of(String tag)
    {
        if (tag.startsWith("@Kiwi.Plan"))
        {
            int idxOfOpenParen = tag.indexOf("(");
            int idxOfCloseParen = tag.indexOf(")");
            String args[] = tag.substring(idxOfOpenParen + 1, idxOfCloseParen).split(",");
            return new PlanTag(args);
        }
        return null;
    }
}
