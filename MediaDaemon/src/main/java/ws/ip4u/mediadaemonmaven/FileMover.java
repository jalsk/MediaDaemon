package ws.ip4u.mediadaemonmaven;

import java.io.File;

/**
 *
 * @author jalsk
 */
public class FileMover
{
	private File tvShowPath;

	public FileMover(File tvShowPath)
	{
		this.tvShowPath = tvShowPath;
	}

	public void moveAndRenameEpisode(Episode episode, File seasonPath) throws FileMoveException
	{
		if(!seasonPath.exists())
		{
			if(!seasonPath.mkdirs())
				throw new FileMoveException("Error creating the directory: " + seasonPath);
		}

		File oldEpisode = new File(episode.getParentDir(), episode.getFilename());
		File newEpisode = new File(seasonPath, episode.getFormattedName());
		if(!oldEpisode.equals(newEpisode))
		{
			if(!fixPermissions(oldEpisode))
				throw new FileMoveException("Can't set the file as readable and writable.");
			System.err.println("Moving file " + oldEpisode + " to " + newEpisode);
			if(!oldEpisode.renameTo(newEpisode))
			{
				throw new FileMoveException("Error moving the requested file: " + episode.getParentDir() + " to: " + seasonPath);
			}
		}
	}

	public void fixSeasonNaming(Season season, File seriesPath) throws FileMoveException
	{
		for(Episode e : season.getEpisodes())
		{
			moveAndRenameEpisode(e, new File(seriesPath, season.getSeasonName()));
		}
	}

	public void fixSeriesNaming(Series series) throws FileMoveException
	{
		for(Season s : series.getSeasons())
		{
			fixSeasonNaming(s, new File(tvShowPath, series.getShowName()));
		}
	}

	private boolean fixPermissions(File f)
	{
		boolean success = f.canWrite() && f.canRead();
		if(!f.canWrite())
			success = f.setWritable(true);
		if(success && !f.canRead())
			success = f.setReadable(true);
		return success;
	}

	public class FileMoveException extends Exception
	{
		public FileMoveException(String message)
		{
			super(message);
		}
	}
}