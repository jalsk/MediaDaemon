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
public class Series
{
	private Integer seriesId;
	private String showName;
	private Map<Integer, Season> seasons;
	private String basePath;

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
		Season s;
		if(!seasons.containsKey(number))
		{
			s = new Season(number);
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
		return seasons.get(number);
	}

	public List<Season> getSeasons()
	{
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
			sb.append("None");

		return sb.toString();
	}
}