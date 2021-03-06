/**
 *  This file is part of MediaDaemon.
 *
 *  MediaDaemon is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  MediaDaemon is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with MediaDaemon.  If not, see <http://www.gnu.org/licenses/>.
 */
package ws.ip4u.mediadaemon;

import com.google.common.collect.Lists;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ws.ip4u.mediadaemon.Episode.EpisodeNotMatchedException;
import ws.ip4u.mediadaemon.FileMover.FileOption;

/**
 *
 * @author jalsk
 */
public class Scanner
{
	private Log log = LogFactory.getLog(Scanner.class);
	private String scanpath;
	private String torrentPath;
	private boolean pretend;
	private List<Series> shows;
	private static List<Pattern> seasonRegex = Arrays.asList(Pattern.compile("[Ss]eason (\\d+)"));

	public Scanner(String scanpath, String torrentPath, boolean pretend)
	{
		this.scanpath = scanpath;
		this.torrentPath = torrentPath;
		this.pretend = pretend;
	}

	public List<Series> getShows()
	{
		if(shows == null)
		{
			populateShows();
		}
		return this.shows;
	}

	private void populateShows()
	{
		shows = Lists.newArrayList();

		File topDir = new File(scanpath);
		String[] showList = topDir.list();
		if(showList != null)
		{
			for(String showString : showList)
			{
				Series show = new Series(showString, scanpath);
				shows.add(show);
				File showFile = new File(topDir, showString);
				String[] seasonList = showFile.list();
				if(seasonList != null)
				{
					for(String seasonString : seasonList)
					{
						for(Pattern rx : seasonRegex)
						{
							Matcher m = rx.matcher(seasonString);
							if(m.matches())
							{
								Season season = show.addSeason(Integer.parseInt(m.group(1)));
								File seasonFile = new File(showFile, seasonString);
								String[] episodeList = seasonFile.list();
								if(episodeList != null)
								{
									for(String episodeString : episodeList)
									{
										try
										{
											season.addEpisode(new Episode(episodeString, seasonFile, pretend ? FileOption.NOTHING : FileOption.MOVE));
										}
										catch(EpisodeNotMatchedException e)
										{
											log.warn(e.getMessage());
										}
									}
								}
								break;
							}
						}
					}
				}
			}
		}

		// Now that we're done parsing in the existing files, look for completed torrents
		File torrentDir = new File(torrentPath);
		String[] torrentList = torrentDir.list();
		if(torrentList != null)
		{
			for(String episode : torrentList)
			{
				try
				{
					Episode e = new Episode(episode, torrentDir, pretend ? FileOption.NOTHING : FileOption.COPY);

					Series series = null;
					for(Series s : shows)
					{
						if(s.getShowName().equals(e.getShowName()))
						{
							series = s;
							break;
						}
					}
					if(series == null)
					{
						series = new Series(e.getShowName(), scanpath);
						shows.add(series);
					}

					Season season = series.getSeason(e.getEpisodeSeason());
					if(season == null)
					{
						season = series.addSeason(e.getEpisodeSeason());
					}
					season.addEpisode(e);
				}
				catch(EpisodeNotMatchedException e)
				{
					log.warn(e.getMessage());
				}
			}
		}
	}
}
