package com.bits_perform.vertretungsplan;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * Created by Miguel on 14.12.2016.
 */

public class ParsePlan {
    public static String parsePlan(String html) {
        Document doc = Jsoup.parse(html);
        Elements es = doc.getAllElements().select("table").not(":contains(Vertretungen sind nicht freigegeben)");
        return es.outerHtml();
    }
}
