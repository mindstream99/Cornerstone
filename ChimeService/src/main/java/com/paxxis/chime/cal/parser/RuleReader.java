package com.paxxis.chime.cal.parser;

import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * This class reads the contents of a rule file
 * and produces an instance of a String that contains
 * those contents.  Nested includes are supported using
 * the #include file-name# directive.
 */
public class RuleReader
{
    List<String> _fileNames = new ArrayList<String>();

    public RuleReader()
    {
    }

    private String getContents(String str)
    {
        // for each instance of #include file-name# directive,
        // get the contents of that file.  then we replace the directive
        // with the contents.
        StringBuffer sbuf = new StringBuffer(str);
        int index = sbuf.indexOf("#include");
        while (index != -1)
        {
            int start = index + 8;
            int end = sbuf.indexOf(";", start);
            String fileName = sbuf.substring(start, end).trim();
            String text = getFileContents(fileName);
            sbuf.replace(index, end + 1, text);
            index = sbuf.indexOf("#include");
        }

        return sbuf.toString();
    }

    public String getFileContents(String fname)
    {
        // make sure we haven't seen this file name before
        String filename = fname.toUpperCase();
        if (_fileNames.contains(filename))
        {
            throw new RuntimeException("Including " + fname +
                    " in an #include directive causes an illegal cyclic reference.");
        }

        _fileNames.add(filename);

        try
        {
            Reader reader = new FileReader(filename);
            return getContents(reader);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private String getContents(Reader reader)
    {
        StringBuffer buf = new StringBuffer();

        try
        {
            int c = reader.read();
            while (c != -1)
            {
                buf.append((char)c);
                c = reader.read();
            }

            return getContents(buf.toString());
        }
        catch (java.io.IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
