package ws.ip4u.mediadaemonmaven;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import ws.ip4u.mediadaemonmaven.ConfigurationValidator.ValidationException;
import ws.ip4u.mediadaemonmaven.FileMover.FileMoveException;
import ws.ip4u.mediadaemonmaven.MediaDaemon.ConfigOptions;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

/**
 *
 * @author jalsk
 */
public class MediaDaemon
{
	private Log log = LogFactory.getLog(MediaDaemon.class);
	private static final String DEFAULT_API_KEY = "8495C6D0B9081C3C";
	private static final String DEFAULT_SEARCH_PATH = "/Users/jalsk/Movies/mediaTest/";
	private static final String DEFAULT_TORRENT_PATH = "/Users/jalsk/torrents/torrents/";

	//<editor-fold defaultstate="collapsed" desc="ConfigOptions enum">
	protected enum ConfigOptions
	{
		CONFIG("config", "c", true, "path to the config file", false, "config"),
		SEARCH_PATH("searchPath", "sp", true, "base path for final media files", true, "search path"),
		HELP("help", "h", false, "display this message", false, "help"),
		TORRENT_PATH("torrentPath", "tp", true, "path to finished torrent directory", true, "torrent path"),
		API_KEY("apiKey", "a", true, "API key for thetvdb.com", true, "api key"),
		SHOW_FORMAT("showFormat", "sf", false, "show the format for the config file", false, "show format"),
		PRETEND("pretend", "p", false, "don't actually do anything, just tell what would happen", false, "pretend"),
		TEST_CONFIG("testConfig", "td", true, "test the config for the specified config file", false, "test config");
		private String name;
		private String shortName;
		private boolean requiredParam;
		private String description;
		private boolean inConfig;
		private String friendlyName;

		ConfigOptions(String name, String shortName, boolean requiredParam, String description, boolean inConfig, String friendlyName)
		{
			this.name = name;
			this.shortName = shortName;
			this.requiredParam = requiredParam;
			this.description = description;
			this.inConfig = inConfig;
			this.friendlyName = friendlyName;
		}

		public String getName()
		{
			return this.name;
		}

		public String getShortName()
		{
			return this.shortName;
		}

		public boolean getRequiredParam()
		{
			return this.requiredParam;
		}

		public String getDescription()
		{
			return this.description;
		}

		public boolean getInConfig()
		{
			return this.inConfig;
		}

		public String getFriendlyName()
		{
			return this.friendlyName;
		}
	};
	//</editor-fold>

	public static void main(String[] args)
	{
		String searchPath = null, torrentPath = null, apiKey = null;
		boolean pretend = false;
		BufferedReader br = null;
		try
		{
			//<editor-fold defaultstate="collapsed" desc="Parse command line options">
			Options options = new Options();
			for(ConfigOptions co : ConfigOptions.values())
			{
				options.addOption(co.shortName, co.name, co.requiredParam, co.description);
			}
			CommandLineParser parser = new GnuParser();
			CommandLine cmd = parser.parse(options, args);

			if(args.length == 0 || cmd.hasOption(ConfigOptions.HELP.shortName))
			{
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("java -jar MediaDaemon", options);
				return;
			}

			if(cmd.hasOption(ConfigOptions.TEST_CONFIG.shortName))
			{
				String fileToTest = cmd.getOptionValue(ConfigOptions.TEST_CONFIG.shortName);

				int lineNo = 1;
				try
				{
					ConfigurationValidator cv = new ConfigurationValidator();
					br = new BufferedReader(new FileReader(fileToTest));
					String line;
					while((line = br.readLine()) != null)
					{
						cv.validate(line);
						lineNo++;
					}
				}
				catch(ValidationException e)
				{
					System.err.println("Error on line " + lineNo + ":\n" + e.getMessage());
				}
				return;
			}

			if(cmd.hasOption(ConfigOptions.CONFIG.shortName))
			{
				// Config file format:
				// key="value"
				// read in the config file
				String configFile = cmd.getOptionValue(ConfigOptions.TEST_CONFIG.shortName);
				br = new BufferedReader(new FileReader(configFile));
				String line;
				while((line = br.readLine()) != null)
				{
					String[] valPair = parseString(line);
					if(valPair[0].equals(ConfigOptions.SEARCH_PATH.name))
					{
						searchPath = valPair[1];
					}
					else if(valPair[0].equals(ConfigOptions.SEARCH_PATH.name))
					{
						searchPath = valPair[1];
					}
					else if(valPair[0].equals(ConfigOptions.API_KEY.name))
					{
						apiKey = valPair[1];
					}
				}
			}
			if(cmd.hasOption(ConfigOptions.API_KEY.shortName))
			{
				apiKey = cmd.getOptionValue(ConfigOptions.API_KEY.shortName);
			}
			else
			{
				apiKey = DEFAULT_API_KEY;
			}

			if(cmd.hasOption(ConfigOptions.SEARCH_PATH.shortName))
			{
				searchPath = cmd.getOptionValue(ConfigOptions.SEARCH_PATH.shortName);
			}
			else
			{
				searchPath = DEFAULT_SEARCH_PATH;
			}

			if(cmd.hasOption(ConfigOptions.TORRENT_PATH.shortName))
			{
				torrentPath = cmd.getOptionValue(ConfigOptions.TORRENT_PATH.shortName);
			}
			else
			{
				torrentPath = DEFAULT_TORRENT_PATH;
			}

			if(cmd.hasOption(ConfigOptions.PRETEND.shortName))
			{
				pretend = true;
			}
			//</editor-fold>

			Scanner sc = new Scanner(searchPath, torrentPath, pretend);
			List<Series> shows = sc.getShows();

			TVDB api = new TVDB(apiKey);

			for(Series show : shows)
			{
				api.updateSeriesInformation(show);
			}

			FileMover fm = new FileMover(new File(searchPath));
			for(Series show : shows)
			{
				fm.fixSeriesNaming(show);
			}
		}
		//<editor-fold defaultstate="collapsed" desc="catch statements">
		catch(ParseException e)
		{
			System.err.println("Problem parsing the config file.\n" + e.getMessage());
		}
		catch(ParserConfigurationException e)
		{
			System.err.println("Problem parsing data from the episode information provider.\n" + e.getMessage());
		}
		catch(SAXException e)
		{
			System.err.println("Problem parsing data from the episode information provider.\n" + e.getMessage());
		}
		catch(IOException e)
		{
			System.err.println("Problem parsing data from the episode information provider.\n" + e.getMessage());
		}
		catch(FileMoveException e)
		{
			System.err.println("Problem moving the files into their appropriate locations.\n" + e.getMessage());
		}
		finally
		{
			IOUtils.closeQuietly(br);
		}
		//</editor-fold>
	}

	public static String[] parseString(String input)
	{
		String[] ret = new String[2];

		ret[0] = input.substring(0, input.indexOf("="));

		ret[1] = input.substring(input.indexOf("=") + 1).substring(1); // cut off the leading quote
		ret[1] = ret[1].substring(0, ret[0].length() - 2); // cut off the trailing quote

		return ret;
	}
}

//<editor-fold defaultstate="collapsed" desc="Configuration Validator">
class ConfigurationValidator
{
	public void validate(String input) throws ValidationException
	{
		// if the line is empty, disregard it
		if(input.isEmpty())
			return;

		if(input.indexOf("=") < 0)
			throw new ValidationException("Configuration file must contain key-value pairs separated by '='");

		String key = input.substring(0, input.indexOf("="));
		String value = input.substring(input.indexOf("=") + 1);

		if(key.isEmpty())
			throw new ValidationException("Cannot have a blank key value");

		boolean foundValue = false;
		ConfigOptions option = null;
		for(ConfigOptions co : ConfigOptions.values())
		{
			if(key.equals(co.getName()) && co.getInConfig())
			{
				option = co;
				foundValue = true;
			}
		}

		if(!foundValue)
			throw new ValidationException("Key value: " + key + " is not known.\nValid values are:\n" + getValidKeyValues());

		if(!value.startsWith("\"") || !value.endsWith("\""))
			throw new ValidationException("Value associated with key " + key + " does not begin and end with quotation marks");

		value = value.substring(0, value.length() - 2);

		if(option == ConfigOptions.SEARCH_PATH || option == ConfigOptions.TORRENT_PATH)
		{
			File f = new File(value);
			if(!f.exists())
				throw new ValidationException("Specified " + option.getFriendlyName() + " is not a valid path name.\n"
											  + "Tested path: " + value);
			if(!f.canRead())
				throw new ValidationException("Specified " + option.getFriendlyName() + " cannot be read.\n"
											  + "Tested path: " + value);

			if(!f.isDirectory())
				throw new ValidationException("Specified " + option.getFriendlyName() + " is not a directory.\n"
											  + "Tested path: " + value);
		}
	}

	private String getValidKeyValues()
	{
		StringBuilder sb = new StringBuilder();

		for(ConfigOptions co : ConfigOptions.values())
		{
			if(co.getInConfig())
				sb.append(co.getName()).append(", ");
		}

		sb.deleteCharAt(sb.length() - 1); // get rid of the last two characters (the comma and space)
		sb.deleteCharAt(sb.length() - 1);

		return sb.toString();
	}

	public class ValidationException extends Exception
	{
		public ValidationException(String message)
		{
			super(message);
		}
	}
}
//</editor-fold>