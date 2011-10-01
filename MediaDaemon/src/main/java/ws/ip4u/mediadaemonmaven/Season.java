/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ws.ip4u.mediadaemonmaven;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jalsk
 */
public class Season
{
	private Integer seasonId;
	private int seasonNumber;
	private Map<Integer, Episode> episodes;
	private static final String SEASON = "Season ";

	public Season(int seasonNumber)
	{
		this.seasonNumber = seasonNumber;
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
		return episodes.containsKey(number);
	}

	public List<Episode> getEpisodes()
	{
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
			sb.append("None\n");
		return sb.toString();
	}
}
