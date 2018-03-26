package org.apache.maven.cli;
/*
 * Copyright (C) 2018 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
