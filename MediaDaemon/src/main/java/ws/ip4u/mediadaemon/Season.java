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
public class Season
{
	private Log log = LogFactory.getLog(Season.class);
	private Integer seasonId;
	private int seasonNumber;
	private int seriesId;
	private Map<Integer, Episode> episodes;
	private static final String SEASON = "Season ";

	public Season(int seasonNumber, int seriesId)
	{
		this.seasonNumber = seasonNumber;
		this.seriesId = seriesId;
		episodes = Maps.newHashMap();
	}

	public void addEpisode(Episode e)
	{
		episodes.put(e.getEpisodeNumber(), e);
	}

	public Episode getEpisode(int number)
	{
		return episodes.get(number);
	}

	public boolean hasEpisode(int number)
	{
		log.info("Does this season have episode " + number + "?");
		return episodes.containsKey(number);
	}

	public List<Episode> getEpisodes()
	{
		log.info("Getting all the episodes");
		return Lists.newArrayList(episodes.values());
	}

	public void setSeasonId(int seasonId)
	{
		this.seasonId = seasonId;
	}

	public Integer getSeasonId()
	{
		return seasonId;
	}

	public String getSeasonName()
	{
		return SEASON + seasonNumber;
	}

	public int getSeasonNumber()
	{
		return seasonNumber;
	}

	public int getSeriesId()
	{
		return seriesId;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();

		sb.append("Season ").append(seasonNumber).append("\nEpisodes:\n");

		if(episodes.size() > 0)
		{
			for(Episode episode : episodes.values())
			{
				sb.append(episode.toString()).append("\n");
			}
		}
		else
		{
			sb.append("None\n");
		}
		return sb.toString();
	}
}
