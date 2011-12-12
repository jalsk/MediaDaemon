package ws.ip4u.mediadaemon;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author jalsk
 */
public class Series
{
	private Log log = LogFactory.getLog(Series.class);
	private Integer seriesId;
	private String showName;
	private Map<Integer, Season> seasons;
	private String basePath;
	private boolean rename;
	private String path;

	public Series(String showName, String basePath)
	{
		this.showName = showName;
		this.basePath = basePath;
		seasons = Maps.newHashMap();
	}

	public String getShowName()
	{
		return showName;
	}

	public Season addSeason(int number)
	{
		log.info("Adding a new season to show " + showName + ", " + number);
		Season s;
		if(!seasons.containsKey(number))
		{
			s = new Season(number, seriesId);
			seasons.put(number, s);
		}
		else
		{
			s = seasons.get(number);
		}
		return s;
	}

	public Season getSeason(int number)
	{
		log.info("Getting season " + number);
		return seasons.get(number);
	}

	public List<Season> getSeasons()
	{
		log.info("Getting all seasons");
		return Lists.newArrayList(seasons.values());
	}

	public void setSeriesId(int seriesId)
	{
		this.seriesId = seriesId;
	}

	public Integer getSeriesId()
	{
		return seriesId;
	}

	public void setRename(boolean rename)
	{
		this.rename = rename;
	}

	public boolean getRename()
	{
		return rename;
	}

	public String getPath()
	{
		return basePath;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();

		sb.append("Base Path: " ).append(this.basePath).append("\n Name: ").append(this.showName).append("\nSeason:\n");

		if(seasons.size() > 0)
		{
			for(Season season : this.seasons.values())
			{
				sb.append(season.toString()).append("\n");
			}
		}
		else
		{
			sb.append("None");
		}

		return sb.toString();
	}
}
