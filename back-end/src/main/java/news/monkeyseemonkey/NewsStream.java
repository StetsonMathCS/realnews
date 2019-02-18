package news.monkeyseemonkey;

import com.chimbori.crux.articles.Article;
import com.chimbori.crux.articles.ArticleExtractor;
import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.collect.Lists;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class NewsStream implements Runnable
{

    private SentimentDetector sentimentDetector;
    private Gson gson;
    private String apiKey;
    private ArrayList<String> searchTerms;
    private SimpleDateFormat dateFormat;
    
    private static final Logger log = LogManager.getLogger(NewsStream.class);

    public NewsStream(SentimentDetector sentimentDetector,
                      Gson gson,
                      Properties props)
    {
        this.sentimentDetector = sentimentDetector;
        this.gson = gson;
        apiKey = props.getProperty("news_api_key");
        searchTerms = Lists.newArrayList(
                props.getProperty("news_api_terms")
                        .split("\\s*,\\s*"));
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    }

    public void run()
    {
        try
        {
            while (true)
            {
                for (String searchTerm : searchTerms)
                {
                    Date todayDate = new Date();
                    String today = dateFormat.format(todayDate);
                    HttpRequest request = HttpRequest.get(
                            "https://newsapi.org/v2/everything",
                            true,
                            "apiKey", apiKey,
                            "q", searchTerm,
                            "from", today,
                            "sortBy", "popularity")
                            .accept("application/json");
                    if (request.code() == 200) {
                        String json = request.body();
                        Map<String, Object> respmap = gson.fromJson(json, Map.class);
                        ArrayList<Map<String, Object>> articles = (ArrayList<Map<String, Object>>) respmap.get("articles");
                        for (Map<String, Object> article : articles) {
                            String url = (String) article.get("url");
                            if(!sentimentDetector.alreadyProcessed(url)) {
                                // fetch the page and extract the main text
//                                logger.log(Level.INFO, "Fetching " + url);
                            	System.out.println("Fetching " + url);
//                            	logger = LoggerFactory.getLogger(NewsStream.class);
                                HttpRequest artRequest = HttpRequest.get(url)
                                        .userAgent("SmartCode");
                                if (artRequest.code() == 200) {
                                    String artHtml = artRequest.body();
                                    Article crux = ArticleExtractor.with(url, artHtml)
                                            .extractContent()
                                            .article();
                                    String body = crux.document.text();
                                    List images = crux.images;
                                    String publisher = crux.siteName;
                                    String title = crux.title;
                                    sentimentDetector.intoDB(url, publisher, body, title, images);
                                }
                            }
                        }
                    }
                }
                // sleep 1 day
                Thread.sleep(1000 * 60 * 60 * 24);
            }
        }
        catch(InterruptedException e)
        {
        	
        }
    }
}