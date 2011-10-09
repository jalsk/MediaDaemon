package ws.ip4u.mediadaemon;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ws.ip4u.mediadaemon.FileMover.FileOption;

/**
 *
 * @author jalsk
 */
public class Episode
{
//	private Log log = LogFactory.getLog(Episode.class);
	private Integer id;
	private Integer seasonId;
	private Integer seriesId;
	private String filename;
	private int episodeSeason;
	private int episodeNumber;
	private boolean needsRenamed;
	private String showName;
	private String episodeName;
	private String extension;
	private File parentDir;
	private static List<Pattern> renameEpisodeRegex = Arrays.asList(
			Pattern.compile("(.*)[Ss](\\d{2})[eE](\\d{2})(.*)\\.([\\w]{3})$"),
			Pattern.compile("(.*)\\.(\\d)(\\d{2})\\.(.*)\\.([\\w]{3})$"));
	private static List<Pattern> noRenameEpisodeRegex = Arrays.asList(
			Pattern.compile("(.*)\\[(\\d+)x(\\d{2})\\](.*)\\.([\\w]{3})$"),
			Pattern.compile("(.*)\\.(\\d+)x(\\d{2})(.*)\\.([\\w]{3})$"));
	private FileOption fileOption;

	// Data/Episode/id
	// Data/Episode/EpisodeNumber
	// Data/Episode/SeasonNumber
	// Data/Episode/EpisodeName
	public Episode(String filename, File parentDir, FileOption fileOption) throws EpisodeNotMatchedException
	{
		this.parentDir = parentDir;
		this.filename = filename;
		this.fileOption = fileOption;
		if(!(new File(parentDir, filename)).isFile())
		{
			throw new EpisodeNotMatchedException("Specified item is not a file.");
		}
		boolean matched = false;
		for(Pattern regex : renameEpisodeRegex)
		{
			Matcher m = regex.matcher(filename);
			if(m.matches())
			{
				this.showName = capitalizeString(m.group(1));
				this.needsRenamed = true;
				this.episodeSeason = Integer.parseInt(m.group(2));
				this.episodeNumber = Integer.parseInt(m.group(3));
				this.extension = m.group(5);
				matched = true;
				break;
			}
		}
		if(!matched)
		{
			for(Pattern regex : Episode.noRenameEpisodeRegex)
			{
				Matcher m = regex.matcher(filename);
				if(m.matches())
				{
					this.showName = capitalizeString(m.group(1));
					this.needsRenamed = false;
					this.episodeSeason = Integer.parseInt(m.group(2));
					this.episodeNumber = Integer.parseInt(m.group(3));
					this.episodeName = m.group(4).trim();
					this.extension = m.group(5);
					matched = true;
					break;
				}
			}
			if(!matched)
			{
				throw new EpisodeNotMatchedException("Episode cannot be matched!" + episodeName);
			}
		}
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public Integer getId()
	{
		return id;
	}

	public void setSeasonId(int seasonId)
	{
		this.seasonId = seasonId;
	}

	public Integer getSeasonId()
	{
		return seasonId;
	}

	public void setSeriesId(int seriesId)
	{
		this.seriesId = seriesId;
	}

	public Integer getSeriesId()
	{
		return seriesId;
	}

	/**
	 * @return the filename
	 */
	public String getFilename()
	{
		return filename;
	}

	/**
	 * @return the season
	 */
	public int getEpisodeSeason()
	{
		return episodeSeason;
	}

	/**
	 * @return the number
	 */
	public int getEpisodeNumber()
	{
		return episodeNumber;
	}

	/**
	 * @return the needsRenamed
	 */
	public boolean needsRenamed()
	{
		return needsRenamed;
	}

	/**
	 * @return the showName
	 */
	public String getShowName()
	{
		return showName;
	}

	public void setEpisodeName(String name)
	{
		this.episodeName = name;
	}

	/**
	 * @return the episodeName
	 */
	public String getEpisodeName()
	{
		return episodeName;
	}

	public String getFormattedName()
	{
		StringBuilder sb = new StringBuilder();

		sb.append(showName).append(' ').append('[').append(episodeSeason).append('x').append(String.format("%02d", episodeNumber)).append(']').append(' ').append(episodeName).append('.').append(getExtension());

		return sb.toString();
	}

	/**
	 * @return the extension
	 */
	public String getExtension()
	{
		return extension;
	}

	/**
	 * @return the parentDir
	 */
	public File getParentDir()
	{
		return parentDir;
	}

	public FileOption getFileOption()
	{
		return fileOption;
	}

	@Override
	public String toString()
	{
		return String.format("\nSeason: %d\nEpisode Number: %d\n Needs renamed: %b\nFilename: %s\nFormatted Filename: %s",
				this.episodeSeason,
				this.episodeNumber,
				this.needsRenamed,
				this.filename,
				getFormattedName());
	}

	private String capitalizeString(String tmp)
	{
		String temp = tmp.replaceAll("\\.", " ").trim().toLowerCase();
		String[] parts = temp.split(" ");
		if(parts.length > 1)
		{
			for(int i = 0; i < parts.length; i++)
			{
				if(Character.isLetter(parts[i].charAt(0)))
				{
					parts[i] = parts[i].substring(0, 1).toUpperCase() + parts[i].substring(1);
				}
			}
			StringBuilder sb = new StringBuilder();
			for(String str : parts)
			{
				sb.append(str).append(' ');
			}
			temp = sb.toString().trim();
		}
		return temp;
	}

	public static class EpisodeNotMatchedException extends Exception
	{
		public EpisodeNotMatchedException(String message)
		{
			super(message);
		}
	}
}
