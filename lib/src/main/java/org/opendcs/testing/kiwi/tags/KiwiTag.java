package org.opendcs.testing.kiwi.tags;

public interface KiwiTag
{

    public static KiwiTag of(String tag)
    {
        if (tag.startsWith("@Kiwi.Plan"))
        {
            return new PlanTag(getArgs(tag));
        }
        else if (tag.startsWith("@Kiwi.Priority"))
        {
            return new PriorityTag(getArgs(tag));
        }
        return null;
    }

    public static String[] getArgs(String tag)
    {
        int idxOfOpenParen = tag.indexOf("(");
        int idxOfCloseParen = tag.indexOf(")");
        return tag.substring(idxOfOpenParen + 1, idxOfCloseParen).split(",");
    }
}
