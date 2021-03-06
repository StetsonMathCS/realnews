package news.monkeyseemonkey.crawler;

import com.chimbori.crux.articles.Article;
import com.chimbori.crux.articles.ArticleExtractor;
import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.collect.Lists;
import com.google.gson.Gson;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class Crawl implements Runnable
{

    private Insert inserter;
    private Gson gson;
    private String apiKey;
    private ArrayList<String> searchTerms;
    private SimpleDateFormat dateFormat;

    public Crawl(Insert inserter,
                      Gson gson,
                      Properties props)
    {
        this.inserter = inserter;
        this.gson = gson;
        apiKey = props.getProperty("news_api_key");
        searchTerms = Lists.newArrayList(
                props.getProperty("news_api_terms")
                        .split("\\s*,\\s*"));
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    }

    @SuppressWarnings("unchecked")
	public void run()
    {
        try
        {
            while(true)
            {
                for(String searchTerm : searchTerms)
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
                    if(request.code() == 200)
                    {
                        String json = request.body();
                        Map<String, Object> respmap = gson.fromJson(json, Map.class);
                        ArrayList<Map<String, Object>> articles = (ArrayList<Map<String, Object>>) respmap.get("articles");
                        for(Map<String, Object> article : articles)
                        {
                            String url = (String) article.get("url");
                            if(!inserter.alreadyProcessed(url))
                            {
                                HttpRequest artRequest = HttpRequest.get(url)
                                        .userAgent("MonkeySeeMonkeyNews");
                                if(artRequest.code() == 200)
                                {
                                    String artHtml = artRequest.body();
                                    Article crux = ArticleExtractor.with(url, artHtml)
                                            .extractContent()
                                            .article();
                                    String body = crux.document.text();
                                    Object publisher = ((Map<String, Object>)(article.get("source"))).get("name");
                                    Object title = article.get("title");
                                    String image = crux.imageUrl;
                                    Object date = article.get("publishedAt");
                                    try
                                    {
                                    	inserter.intoDB(url, publisher, date, body, title, image);
                                    }
                                    catch(SQLException e)
                                    {
                                    	System.out.println("Error");
                                    }
                                }
                            }
                        }
                    }
                }
                System.out.println("sleeping");
                // sleep 1 day
                Thread.sleep(1000 * 60 * 60 * 24);
                /*
                // sleep 1 minute
                Thread.sleep(1000 * 60);
                */
            }
        }
        catch(InterruptedException e)
        {
        	
        }
    }
}
