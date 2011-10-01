/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ws.ip4u.mediadaemonmaven;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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
	private String apikey;
	private String mirrorPath;
	private Client client;
	private WebResource service;
	private DocumentBuilder parser;

	public TVDB(String apikey) throws ParserConfigurationException, SAXException, IOException
	{
		this.apikey = apikey;
		client = Client.create();
		service = client.resource("http://www.thetvdb.com/api");
		String mirrors = service.path(this.apikey + "/mirrors.xml").accept(MediaType.TEXT_XML).get(String.class);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		parser = factory.newDocumentBuilder();
		Document doc = parser.parse(new ByteArrayInputStream(mirrors.getBytes()));

		Node mirrorPathNode = doc.getElementsByTagName("mirrorpath").item(0);
		mirrorPath = mirrorPathNode.getNodeValue();
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
				if (entry.getName().equalsIgnoreCase("en.xml"));
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
							System.err.println(String.format("Unable to find episode number %d", episodeNumber));
						}
					}
					else
					{
						System.err.println(String.format("Unable to find season number %d", seasonNumber));
					}
				}
			}
		}
		catch (Exception e)
		{
			System.err.println("Problem parsing data from the episode information provider.\n" + e.getMessage());
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

	private int lookupSeriesId(String seriesName) throws Exception
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
		throw new Exception("Unable to determine SeriesID");
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
}
