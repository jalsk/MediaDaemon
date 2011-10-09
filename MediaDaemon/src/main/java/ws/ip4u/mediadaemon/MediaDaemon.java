package ws.ip4u.mediadaemon;

import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import ws.ip4u.mediadaemon.FileMover.FileMoveException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;
import ws.ip4u.mediadaemon.Config.ConfigException;
import ws.ip4u.mediadaemon.Config.ConfigTestingException;

/**
 * TODO: turn this thing into a service somehow
 *
 * @author jalsk
 */
public final class MediaDaemon
{
	private static Log log = LogFactory.getLog(MediaDaemon.class);

	private MediaDaemon()
	{
	}

	public static void main(String[] args)
	{
		try
		{
			Config config = new Config(args);

			Scanner sc = new Scanner(config.getShowPath(), config.getTorrentPath(), config.isPretend());
			List<Series> shows = sc.getShows();

			TVDB api = new TVDB(config.getApiKey());

			for(Series show : shows)
			{
				api.updateSeriesInformation(show);
			}

			FileMover fm = new FileMover(new File(config.getShowPath()));
			for(Series show : shows)
			{
				fm.fixSeriesNaming(show);
			}
		}
		//<editor-fold defaultstate="collapsed" desc="catch statements">
		catch(ConfigException e)
		{
			log.fatal("There was a problem with the configuration specified.\n" + e.getMessage(), e);
		}
		catch(ConfigTestingException e)
		{
			log.info("Finished testing the config file. Exiting.");
		}
		catch(ParserConfigurationException e)
		{
			log.error("Problem parsing data from the episode information provider.\n" + e.getMessage(), e);
		}
		catch(SAXException e)
		{
			log.error("Problem parsing data from the episode information provider.\n" + e.getMessage(), e);
		}
		catch(IOException e)
		{
			log.error("Problem parsing data from the episode information provider.\n" + e.getMessage(), e);
		}
		catch(FileMoveException e)
		{
			log.error("Problem moving the files into their appropriate locations.\n" + e.getMessage(), e);
		}
		//</editor-fold>
	}
}
