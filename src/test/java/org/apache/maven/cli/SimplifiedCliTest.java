package org.apache.maven.cli;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;

import static org.junit.Assert.*;

public class SimplifiedCliTest
{
    @Rule
    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog();

    @Test
    public void testSimplifiedCli() throws Exception
    {
        final SimplifiedCli cli = new SimplifiedCli(new String[]{});
        cli.run();

        assertEquals (systemOutRule.getLog().trim(), ( "org.goots.maven:module-reader:1.0-SNAPSHOT" ));
    }

    @Test
    public void testSimplifiedCliDebug() throws Exception
    {
        final SimplifiedCli cli = new SimplifiedCli( new String[] { "-X" } );
        cli.run();
        assertTrue (systemOutRule.getLog().trim().contains ( "DEBUG -" ));
    }
}