package ws.ip4u.mediadaemon;

import com.google.common.collect.Lists;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.javatuples.Pair;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import ws.ip4u.mediadaemon.Episode.EpisodeNotMatchedException;

/**
 *
 * @author jalsk
 */
public class ScannerTest
{
	private static List<String> EPISODE_NAMES = Lists.newArrayList();
	private static List<Pair<String, String>> MAPPINGS = Lists.newArrayList();

	@BeforeClass
	public static void setUp() throws FileNotFoundException, IOException
	{
//		InputStreamReader fr = null;
//		BufferedReader br = null;
//		try
//		{
//			fr = new InputStreamReader(ScannerTest.class.getResourceAsStream("testNames.txt"));
//			br = new BufferedReader(fr);
//
//			String line;
//			while ((line = br.readLine()) != null)
//			{
//				EPISODE_NAMES.add(line);
//			}
//		}
//		finally
//		{
//			IOUtils.closeQuietly(fr);
//			IOUtils.closeQuietly(br);
//		}
		
		MAPPINGS.add(Pair.with("30.Rock.S06E18.HDTV.x264-LOL.mp4", "30 Rock [6x18] .mp4"));
		MAPPINGS.add(Pair.with("Alphas.S01E07.Catch.and.Release.HDTV.XviD-FQM.[VTV].avi", "Alphas [1x07] .avi"));
		MAPPINGS.add(Pair.with("CSI.Miami.S07E22.HDTV.XviD-LOL.avi", "CSI Miami [7x22] .avi"));
		MAPPINGS.add(Pair.with("Game.of.Thrones.S02E06.720p.HDTV.x264-2HD.mkv", "Game of Thrones [2x06] .mkv"));
		MAPPINGS.add(Pair.with("How.I.Met.Your.Mother.S07E04.HDTV.XviD-LOL.[VTV].avi", "How I Met Your Mother [7x04] .avi"));
		MAPPINGS.add(Pair.with("Lie.to.Me.S02E04.HDTV.XviD-NoTV.[VTV].avi", "Lie to Me [2x04] .avi"));
		MAPPINGS.add(Pair.with("The.Big.Bang.Theory.S03E01.HDTV.XviD-NoTV.avi", "The Big Bang Theory [3x01] .avi"));
	}
	
	@Test
	public void testFileRecognitionAndRenaming() throws EpisodeNotMatchedException
	{
		for (Pair<String, String> pair : MAPPINGS)
		{
			Episode e = new Episode(pair.getValue0(), null, FileMover.FileOption.NOTHING);
			assertEquals(pair.getValue1(), e.getFormattedName());
		}
	}
}