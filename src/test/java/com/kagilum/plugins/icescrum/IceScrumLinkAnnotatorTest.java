package com.kagilum.plugins.icescrum;

import hudson.MarkupText;
import junit.framework.Assert;
import org.junit.Test;

public class IceScrumLinkAnnotatorTest {
    private final static String ICESCRUM_URL = "http://tools.icescrum.org/p/TESTPROJ-";

    @Test
    public final void testAnnotateStringMarkupText() {
        assertAnnotatedTextEquals("An issue Closes T671-0 link",
                "An issue Closes <a href='" + ICESCRUM_URL
                        + "-T671' title='Show details - Task: 671 with remaining time: 0'>T671-0</a> link");

        assertAnnotatedTextEquals("An issue Close T671-3 link",
                "An issue Close <a href='" + ICESCRUM_URL
                        + "-T671' title='Show details - Task: 671 with remaining time: 3'>T671-3</a> link");

        assertAnnotatedTextEquals("An issue closes T671 link",
                "An issue closes <a href='" + ICESCRUM_URL
                        + "-T671' title='Show details - Task: 671'>T671</a> link");

        assertAnnotatedTextEquals("An issue close T671-0 T674-3 link",
                "An issue close <a href='" + ICESCRUM_URL
                        + "-T671' title='Show details - Task: 671 with remaining time: 0'>T671-0</a> " +
                "<a href='" + ICESCRUM_URL + "-T674' title='Show details - Task: 674 with remaining time: 3'>T674-3</a> link");
    }

    private void assertAnnotatedTextEquals(final String originalText,
                                           final String expectedAnnotatedText) {
        MarkupText markupText = new MarkupText(originalText);

        IceScrumLinkAnnotator annotator = new IceScrumLinkAnnotator();
        annotator.annotate(ICESCRUM_URL, markupText);

        Assert.assertEquals(expectedAnnotatedText, markupText.toString());
    }
}
