package org.apache.maven.cli;

import org.apache.maven.DefaultMaven;
import org.apache.maven.Maven;
import org.apache.maven.execution.DefaultMavenExecutionResult;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequestPopulator;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.execution.ProjectDependencyGraph;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.PlexusContainer;
import org.eclipse.aether.RepositorySystemSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import static org.apache.maven.cli.MavenCli.ExitException;
import static org.apache.maven.cli.MavenCli.CliRequest;

/**
 * This class is in the same package as {@link MavenCli} in order to access the
 * package private {@link CliRequest}
 */
public class SimplifiedCli
{
    private final Logger slf4jLogger = LoggerFactory.getLogger( getClass() );

    private final CliRequest cliRequest;

    public SimplifiedCli( String[] args )
    {
        cliRequest = new CliRequest( args, null );
    }

    public int run() throws Exception
    {
        final MavenCli mavenCli = new MavenCli( );

        PlexusContainer localContainer = null;
        try
        {
            invokeMaven( "initialize", mavenCli, cliRequest );
            invokeMaven( "cli", mavenCli, cliRequest );
            invokeMaven( "logging", mavenCli, cliRequest );
            invokeMaven( "version", mavenCli, cliRequest );
            invokeMaven( "properties", mavenCli, cliRequest );

            localContainer = (PlexusContainer) invokeMaven( "container", mavenCli, cliRequest );

            invokeMaven( "commands", mavenCli, cliRequest );
            invokeMaven( "settings", mavenCli, cliRequest );
            MavenExecutionRequest executionRequest =
                            (MavenExecutionRequest) invokeMaven( "populateRequest", mavenCli, cliRequest );
            invokeMaven( "encryption", mavenCli, cliRequest );
            invokeMaven( "repository", mavenCli, cliRequest );

            return executeModuleSearch ( localContainer, executionRequest);
        }
        catch (ExitException e)
        {
            return e.exitCode;
        }
        finally
        {
            if ( localContainer != null )
            {
                localContainer.dispose();
            }
        }
    }

    /**
     * Essentially similar to {@link org.apache.maven.cli.MavenCli#execute(CliRequest)} but customised for the module searching.
     * Rather than executing an actual lifecycle we'll do all the initial processing and then simply output the dependency graph.
     */
    private int executeModuleSearch( PlexusContainer container, MavenExecutionRequest executionRequest ) throws Exception
    {
        final MavenExecutionResult result = new DefaultMavenExecutionResult();
        final MavenExecutionRequestPopulator populator = container.lookup( MavenExecutionRequestPopulator.class );
        final Maven maven = container.lookup( Maven.class );

        populator.populateDefaults( executionRequest );

        final RepositorySystemSession repoSession = ((DefaultMaven)maven).newRepositorySession( executionRequest );
        final MavenSession session = new MavenSession( container, repoSession, executionRequest, result );
        @SuppressWarnings("unchecked")
        final List<MavenProject> projects = (List<MavenProject>) invokeMaven( "getProjectsForMavenReactor", maven, session );
        session.setAllProjects( projects );

        final Method projectDependencyGraphMethod = maven.getClass().getDeclaredMethod
                        ( "createProjectDependencyGraph", Collection.class, MavenExecutionRequest.class, MavenExecutionResult.class, boolean.class );
        projectDependencyGraphMethod.setAccessible( true );

        final ProjectDependencyGraph projectDependencyGraph =
                        (ProjectDependencyGraph) projectDependencyGraphMethod.invoke( maven, projects, executionRequest, result, true );

        slf4jLogger.debug( "ProjectDependencyGraph is " + projectDependencyGraph );

        projectDependencyGraph.getSortedProjects().forEach(
                        p -> slf4jLogger.info ("{} : {} : {} ", p.getGroupId() , p.getArtifactId(), p.getVersion())
        );

        return 1;
    }

    private Object invokeMaven( String operation, Object object, Object parameter) throws Exception
    {
        try
        {
            Method m = object.getClass().getDeclaredMethod( operation, parameter.getClass() );
            m.setAccessible( true );
            return m.invoke( object, parameter );
        }
        catch ( NoSuchMethodException e )
        {
            throw new Exception( "Unable to invoke method", e );
        }
        catch ( InvocationTargetException e )
        {
            if ( e.getTargetException() instanceof Exception )
            {
                throw (Exception)e.getTargetException();
            }
            else
            {
               throw new Exception( "Unable to invoke target", e );                
            }
        }
    }
}
