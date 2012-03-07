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

import com.google.common.base.Preconditions;
/**
 *
 * @author jalsk
 */
public class SeriesFactory
{
	private Integer seriesId = null;
	private String showName = null;
	private String basePath = null;
	private Boolean rename = null;

	public SeriesFactory()
	{ }

	public SeriesFactory(SeriesFactory that)
	{
		this.basePath = that.basePath;
		this.showName = that.showName;
		this.seriesId = that.seriesId;
		this.rename = that.rename;
	}

	public SeriesFactory withSeriesId(int seriesId)
	{
		this.seriesId = seriesId;
		return this;
	}

	public SeriesFactory withShowName(String showName)
	{
		this.showName = showName;
		return this;
	}

	public SeriesFactory withBasePath(String basePath)
	{
		this.basePath = basePath;
		return this;
	}

	public SeriesFactory withRename(boolean rename)
	{
		this.rename = rename;
		return this;
	}

	public Series build()
	{
		Preconditions.checkNotNull(showName);
		Preconditions.checkNotNull(basePath);
		Preconditions.checkNotNull(seriesId);

		Series ret = new Series(showName, basePath);
		ret.setSeriesId(seriesId);
		if(rename != null)
			ret.setRename(rename);
		return ret;
	}

	public SeriesFactory but()
	{
		return new SeriesFactory(this);
	}
}
