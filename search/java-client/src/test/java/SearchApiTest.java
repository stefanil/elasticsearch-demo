import java.util.concurrent.TimeUnit;

import org.apache.http.HttpHost;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.ScoreSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class SearchApiTest {

  private RestHighLevelClient client;
  private SearchRequest searchRequest;

  @Before
  public void setUp() throws Exception {
    client = new RestHighLevelClient(
        RestClient.builder(
            new HttpHost("localhost", 9200, "http")));
    final SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
    sourceBuilder.query(QueryBuilders.wildcardQuery("name", "t?s*"));
    sourceBuilder.sort(new ScoreSortBuilder().order(SortOrder.DESC));
    sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
    sourceBuilder.size(5);
    searchRequest = new SearchRequest(new String[]{"arteria"}, sourceBuilder);
    searchRequest.types("product");
  }

  @After
  public void tearDown() throws Exception {
    client.close();
  }

  @Test
  public void shouldFindSynchronously() throws Exception {
    final SearchResponse searchResponse = client.search(searchRequest);

    assertThat(searchResponse).isNotNull();
    assertThat(searchResponse.status()).isEqualTo(RestStatus.OK);
    assertThat(searchResponse.isTimedOut()).isFalse();
    assertThat(searchResponse.getHits().getMaxScore()).isEqualTo(1.0f);
    assertThat(searchResponse.getHits().getTotalHits()).isEqualTo(7L);
    final SearchHit firstHit = searchResponse.getHits().getAt(0);
    assertThat(firstHit.getIndex()).isEqualTo("arteria");
    assertThat(firstHit.getType()).isEqualTo("product");
    assertThat(firstHit.getScore()).isEqualTo(1.0f);
    assertThat(firstHit.getSourceAsString())
        .isEqualTo("{\"name\":\"Tisch1\",\"description\":\"Ein Tisch 1\"}");
  }

  @Test
  public void shouldFindAsynchronously() throws Exception {
    client.searchAsync(searchRequest, new ActionListener<SearchResponse>() {
      @Override
      public void onResponse(final SearchResponse searchResponse) {
        assertThat(searchResponse).isNotNull();
      }

      @Override
      public void onFailure(final Exception ex) {
        fail("Search Failure", ex);
      }
    });
  }

  @Test
  public void shouldFindMatchQuery() throws Exception {
    // todo
  }

  @Test
  public void shouldFindWidlcardQuery() throws Exception {
    // todo
  }

  @Test
  public void shouldFindTermQuery() throws Exception {
    // todo
  }

  @Test
  public void shouldSortDesc() throws Exception {
    // todo
  }

  @Test
  public void shouldHighlightFragments() throws Exception {
    // todo
  }

  @Test
  public void shoulkdRetrieveAggregations() throws Exception {
    // todo
  }

  @Test
  public void shouldRetrieveSuggestions() throws Exception {
    // todo
  }

  @Test
  public void name() throws Exception {
  }
}
