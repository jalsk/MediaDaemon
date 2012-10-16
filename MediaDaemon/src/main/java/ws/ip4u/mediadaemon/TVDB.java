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
import com.google.common.collect.Maps;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author jalsk
 */
public class TVDB
{
	private Log log = LogFactory.getLog(TVDB.class);
	private String apikey;
	private Map<ResourceType, List<String>> mirrors;
	private WebResource service;
	private DocumentBuilder parser;
	private int serverTime;
	private DB db;

	enum ResourceType
	{
		XML_FILE(1),
		BANNER_FILE(2),
		ZIP_FILE(4);
		int typeMask;

		ResourceType(int typeMask)
		{
			this.typeMask = typeMask;
		}
	}

	// TODO: change this so that it uses the mirror and caches the data locally until it is expired.
	public TVDB(String apikey, DB db) throws ParserConfigurationException, SAXException, IOException
	{
		this.apikey = apikey;
		this.db = db;
		service = Client.create().resource("http://www.thetvdb.com/api");
		String mirrorsXml = service.path(this.apikey + "/mirrors.xml").accept(MediaType.TEXT_XML).get(String.class);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		parser = factory.newDocumentBuilder();
		Document doc = parser.parse(new ByteArrayInputStream(mirrorsXml.getBytes()));

		NodeList mirrorsList = doc.getElementsByTagName("Mirrors");
		mirrors = Maps.newEnumMap(ResourceType.class);

		for (int i = 0; i < mirrorsList.getLength(); i++)
		{
			Node mirrorNode = mirrorsList.item(i);
			NodeList attributes = mirrorNode.getChildNodes();
			String mirrorPath = null;
			int typeMask = 0;
			for (int j = 0; j < attributes.getLength(); j++)
			{
				Node attrib = attributes.item(j);
				switch (attrib.getLocalName())
				{
					case "mirrorpath":
						mirrorPath = attrib.getNodeValue();
						break;
					case "typemask":
						typeMask = Integer.parseInt(attrib.getNodeValue());
						break;
				}
			}
			if (mirrorPath != null)
			{
				for (ResourceType resourceType : ResourceType.values())
				{
					if ((resourceType.typeMask & typeMask) == 1)
					{
						if (!mirrors.containsKey(resourceType))
						{
							mirrors.put(resourceType, Lists.<String>newArrayList());
						}

						mirrors.get(resourceType).add(mirrorPath);
					}
				}
			}
		}
	}

	public void updateSeriesInformation(Series series)
	{
		try
		{
			if (series.getSeriesId() == null)
			{
				series.setSeriesId(lookupSeriesId(series.getShowName()));
			}
			File zip = service.path("/" + this.apikey + "/series/" + series.getSeriesId() + "/all/en.zip").get(File.class);
			ZipFile zipFile = new ZipFile(zip);
			ZipEntry entry;
			Document xmlDoc = null;
			Enumeration enumeration = zipFile.entries();

			while (enumeration.hasMoreElements())
			{
				entry = (ZipEntry) enumeration.nextElement();
				if (entry.getName().equalsIgnoreCase("en.xml"))
				{
					xmlDoc = parser.parse(zipFile.getInputStream(entry));
					break;
				}
			}
			if (xmlDoc != null)
			{
				NodeList episodes = xmlDoc.getElementsByTagName("Episode");
				for (int i = 0; i < episodes.getLength(); i++)
				{
					Node episode = episodes.item(i);
					Integer episodeNumber = getEpisodeNumber(episode);
					Integer seasonNumber = getSeasonNumber(episode);
					if (series.getSeason(seasonNumber) != null)
					{
						Season s = series.getSeason(seasonNumber);
						s.setSeasonId(getSeasonId(episode));
						if (s.getEpisode(episodeNumber) != null)
						{
							Episode e = s.getEpisode(episodeNumber);
							e.setId(getEpisodeId(episode));
							e.setSeasonId(getSeasonId(episode));
							e.setSeriesId(getSeriesId(episode));
							e.setEpisodeName(getEpisodeName(episode));
						}
						else
						{
							log.info(String.format("Unable to find episode number %d", episodeNumber));
						}
					}
					else
					{
						log.info(String.format("Unable to find season number %d", seasonNumber));
					}
				}
			}
		}
		catch (SeriesLookupException e)
		{
			log.fatal("Problem parsing data from the episode information provider.\n" + e.getMessage());
		}
		catch (ZipException e)
		{
			log.fatal("Problem handling the zip file.\n" + e.getMessage());
		}
		catch (IOException e)
		{
			log.fatal("There was a problem with I/O.\n" + e.getMessage());
		}
		catch (SAXException e)
		{
			log.fatal("There was a problem parsing the XML File.\n" + e.getMessage());
		}
	}

	private Integer getSeasonId(Node n)
	{
		return getAttributeInt(n, "seasonid");
	}

	private Integer getSeriesId(Node n)
	{
		return getAttributeInt(n, "seriesid");
	}

	private Integer getEpisodeId(Node n)
	{
		return getAttributeInt(n, "id");
	}

	private Integer getEpisodeNumber(Node n)
	{
		return getAttributeInt(n, "EpisodeNumber");
	}

	private Integer getSeasonNumber(Node n)
	{
		return getAttributeInt(n, "SeasonNumber");
	}

	private String getEpisodeName(Node n)
	{
		return getAttribute(n, "EpisodeName");
	}

	private Integer getAttributeInt(Node n, String attribute)
	{
		String ret = getAttribute(n, attribute);
		if (ret != null)
		{
			return Integer.parseInt(ret);
		}
		return null;
	}

	private String getAttribute(Node n, String attribute)
	{
		return getTextValue((Element) n, attribute);
	}

	private int lookupSeriesId(String seriesName) throws SeriesLookupException, SAXException, IOException
	{
		MultivaluedMap mvm = new MultivaluedMapImpl();
		mvm.add("seriesname", seriesName);
		String s = service.path("GetSeries.php").queryParams(mvm).get(String.class);
		Document doc = parser.parse(new ByteArrayInputStream(s.getBytes()));
		NodeList nl = doc.getElementsByTagName("Series");
		for (int i = 0; i < nl.getLength(); i++)
		{
			Node tn = nl.item(i);
			if (tn.getNodeType() == Node.ELEMENT_NODE)
			{
				return getSeriesId(tn);
			}
		}
		throw new SeriesLookupException("Unable to determine SeriesID");
	}

	private String getTextValue(Element e, String tagName)
	{
		String textVal = null;

		NodeList nl = e.getElementsByTagName(tagName);
		if (nl != null && nl.getLength() > 0)
		{
			Element el = (Element) nl.item(0);
			textVal = el.getFirstChild().getNodeValue();
		}

		return textVal;
	}

	private static class SeriesLookupException extends Exception
	{
		public SeriesLookupException(String message)
		{
			super(message);
		}
	}
}
