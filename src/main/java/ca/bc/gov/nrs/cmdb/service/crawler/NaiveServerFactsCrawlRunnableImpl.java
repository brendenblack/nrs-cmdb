package ca.bc.gov.nrs.cmdb.service.crawler;

import ca.bc.gov.nrs.cmdb.model.vertices.ComputeNode;
import ca.bc.gov.nrs.cmdb.model.vertices.Filesystem;
import ca.bc.gov.nrs.cmdb.model.vertices.OperatingSystem;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.pastdev.jsch.DefaultSessionFactory;
import com.pastdev.jsch.command.CommandRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


/**
 * Retrieves a set of facts about the target host
 */
public class NaiveServerFactsCrawlRunnableImpl implements Runnable, OngoingCrawl
{
    private static final Logger log = LoggerFactory.getLogger(NaiveServerFactsCrawlRunnableImpl.class);


    private final String crawlId;

    private final SimpMessagingTemplate template;
//    private final String fqdn;
//    private final String username;
//    private final String password;
    private final DefaultSessionFactory sessionFactory;
    private final ComputeNode server;
    private ServerFactCrawlCallback callback;

    private boolean running = false;
    private Message status;

    @Autowired
    public NaiveServerFactsCrawlRunnableImpl(String crawlId,
                                             SimpMessagingTemplate template,
                                             ComputeNode server,
                                             String username,
                                             String password)
    {
        this.crawlId = crawlId;
        this.template = template;
        this.server = server;
        DefaultSessionFactory sessionFactory = new DefaultSessionFactory(username, server.getFqdn(), 22);
        sessionFactory.setPassword(password);
        sessionFactory.setConfig("StrictHostKeyChecking", "no"); // https://www.mail-archive.com/jsch-users@lists.sourceforge.net/msg00529.html
        this.sessionFactory = sessionFactory;
//
//        this.fqdn = fqdn;
//        this.username = username;
//        this.password = password;

        Message m = new Message();
        m.setStatus(Message.STATUS_INFO);
        m.setFqdn(server.getFqdn());
        m.setMessage("Waiting...");
        this.status = m;
    }

    @Override
    public void run()
    {
        info("Attempting to determine what family of OS the target host is...");

        String uname = "";
        try
        {
            uname = getUname(this.sessionFactory);
        }
        catch (JSchException | IOException e)
        {
            log.error("An error occurred while attempting to determine host {}'s uname value", sessionFactory.getHostname(), e);
            error("An error occurred while attempting to determine host's uname value: " + e.getMessage());
            return;
        }

        log.debug("Server returned a uname value of {}", uname);

        OperatingSystem os;
        try
        {
            switch (uname.toUpperCase())
            {
                case "LINUX":
                    info("Host appears a Linux server");
                    os = getOsFactsFromLinux(sessionFactory);
                    List<Filesystem> filesystems =  getFilesystemsFromLinux(sessionFactory);

                    break;
                case "SUNOS":
                    info("Host appears to be a Solaris server");
                    os = getOsFactsFromSolaris(sessionFactory);
                    break;
                default:
                    log.error("Unsupported uname was returned from {}: {}; this will likely require a code change to support.",
                              this.sessionFactory.getHostname(),
                              uname);
                    error("Unsupported uname value was returned from host: " + uname);
                    return;
            }
        }
        catch (JSchException | IOException e)
        {
            log.error("An error occurred while attempting to gather facts about {}", sessionFactory.getHostname(), e);
            error("An error occurred while attempting to gather facts about target host: " + e.getMessage());
            return;
        }

        this.server.setOperatingSystem(os);

        if (this.callback != null)
        {
            this.callback.doCallback(this.crawlId, this.server);
        }
    }

    @Override
    public String getId()
    {
        return this.crawlId;
    }

    @Override
    public String getFqdn()
    {
        return this.server.getFqdn();
    }

    @Override
    public Message getStatus()
    {
        return this.status;
    }

    public void setCallback(ServerFactCrawlCallback callback)
    {
        this.callback = callback;
    }

    public OperatingSystem getOsFactsFromLinux(DefaultSessionFactory sessionFactory) throws IOException, JSchException
    {
        String command = "cat /etc/*release";
        String result = doExecuteCommand(sessionFactory, command);

        String[] lines = result.split("\n");
        Properties properties = new Properties();
        for (String line : lines)
        {
            if (line.contains("="))
            {
                log.trace("Line: {}", line);
                properties.load(new StringReader(line));
            }
        }
        log.trace("Found {} properties", properties.size());

        OperatingSystem os = new OperatingSystem();
        os.setFamily("Linux");
        os.setVariantId(properties.getProperty("VARIANT_ID"));
        os.setVersion(properties.getProperty("VERSION_ID"));
        os.setVersionName(properties.getProperty("VERSION"));
        os.setName(properties.getProperty("NAME"));

        log.trace("Host: [family: {}] [variant: {}] [version: {}] [version name: {}] [name: {}]",
                  os.getFamily(),
                  os.getVariantId(),
                  os.getVersion(),
                  os.getVersionName(),
                  os.getName());

        return os;
    }

    public List<Filesystem> getFilesystemsFromLinux(DefaultSessionFactory sessionFactory) throws IOException, JSchException
    {
        String command = "df -T";
        String result = doExecuteCommand(sessionFactory, command);
        String[] lines = result.split("\n");
        List<Filesystem> filesystems = new ArrayList<>();
        for (String line : lines)
        {
            line = line.replaceAll("\\s+", "\\s");
            String[] cols = line.split("\\s");
            StringBuilder sb = new StringBuilder();
            for (String col : cols)
            {
                sb.append(col).append(" ");
            }
            log.debug(sb.toString());

            if (cols.length == 7)
            {
                Filesystem fs = new Filesystem();

                fs.setName(cols[0]);
                fs.setType(cols[1]);
                try
                {
                    long size = Long.parseLong(cols[2]);
                    fs.setSize(size);
                }
                catch (NumberFormatException e)
                {
                    fs.setSize(-1);
                }

                try
                {
                    long used = Long.parseLong(cols[3]);
                    fs.setSize(used);
                }
                catch (NumberFormatException e)
                {
                    fs.setSize(-1);
                }

                try
                {
                    long avail = Long.parseLong(cols[4]);
                    fs.setSize(avail);
                }
                catch (NumberFormatException e)
                {
                    fs.setSize(-1);
                }

                fs.setMountedOn(cols[6]);

                log.debug("Filesystem [name: {}] [type: {}] [size: {}] [used: {}] [avail: {}] [mounted: {}]",
                          fs.getName(),
                          fs.getType(),
                          fs.getSize(),
                          fs.getUsed(),
                          fs.getAvailable(),
                          fs.getMountedOn());

                filesystems.add(fs);
            }
        }

        return filesystems;
    }

    public OperatingSystem getOsFactsFromSolaris(DefaultSessionFactory sessionFactory)
    {
        OperatingSystem os = new OperatingSystem();

        return os;
    }


    public String getUname(DefaultSessionFactory sessionFactory) throws IOException, JSchException
    {
        String command = "uname -s";
        return doExecuteCommand(sessionFactory, command);
    }

    /**
     * Executes a routine command on the target host and returns the Stdout. The full, unmodified contents of Stdout
     * will also be logged at DEBUG. If the command returns Stderr, then the contents will be logged at WARNING.
     *
     * @param sessionFactory
     * @param command
     * @return
     * @throws IOException
     * @throws JSchException
     */
    public String doExecuteCommand(DefaultSessionFactory sessionFactory, String command) throws IOException, JSchException
    {
        CommandRunner cmd = new CommandRunner(sessionFactory);

        log.trace("Attempting to execute command '{}' on {}", command, sessionFactory.getHostname());
        CommandRunner.ExecuteResult result = cmd.execute(command);
        if (result.getStderr() != null && result.getStderr().length() > 0)
        {
            log.warn("Stderr: {}", result.getStderr().trim());
        }

        log.debug(result.getStdout());

        return result.getStdout().trim();
    }

    //region Status updates
    public void info(String message)
    {
        Message m = new Message();
        m.setFqdn(this.sessionFactory.getHostname());
        m.setStatus(Message.STATUS_INFO);
        m.setMessage(message);

        sendProgress(m);
    }

    public void warn(String message)
    {
        Message m = new Message();
        m.setFqdn(this.sessionFactory.getHostname());
        m.setStatus(Message.STATUS_WARNING);
        m.setMessage(message);

        sendProgress(m);
    }

    public void error(String message)
    {
        this.running = false;

        Message m = new Message();
        m.setFqdn(this.sessionFactory.getHostname());
        m.setStatus(Message.STATUS_ERROR);
        m.setMessage(message);

        sendProgress(m);
    }

    public void finish(String message)
    {
        this.running = false;

        Message m = new Message();
        m.setFqdn(this.sessionFactory.getHostname());
        m.setStatus(Message.STATUS_SUCCESS);
        m.setMessage(message);

        sendProgress(m);
    }



    public void sendProgress(Message message)
    {
        this.status = message;
        //  + "/" + this.crawlId
        template.convertAndSend(ServerCrawlManager.WEBSOCKET_ROOT, this.status);
    }
    //endregion

    public boolean isRunning()
    {
        return running;
    }


    /**
     * A messaging template to provide feedback to any WebSocket listeners
     */
    @JsonAutoDetect
    public static class Message
    {
        // map internally-meaningful status names to Bootstrap contextual classes
        // TODO: this logic should be the responsibility of the front end
        private static final String STATUS_INFO = "INFO";
        private static final String STATUS_WARNING = "WARN";
        private static final String STATUS_ERROR = "ERROR";
        private static final String STATUS_SUCCESS = "SUCCESS";

        private String fqdn;
        private String status;
        private String message;


        public String getFqdn()
        {
            return fqdn;
        }

        public void setFqdn(String fqdn)
        {
            this.fqdn = fqdn;
        }

        public void setStatus(String status)
        {
            this.status = status;
        }

        public String getStatus()
        {
            return this.status;
        }

        public String getMessage()
        {
            return message;
        }

        public void setMessage(String message)
        {
            this.message = message;
        }
    }
}
