import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteRequest.OpType;
import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest.RefreshPolicy;
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

/**
 * @see <a href=
 *      "https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high-search.html">Search
 *      API Doc</a>
 * @see <a href=
 *      "https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high-document-update.html">Update
 *      API Doc</a>
 */
public class SearchApiTest {

  private static final String INDEX = "myindex";
  private static final String TYPE = "product";

  private RestHighLevelClient client;
  private SearchRequest searchRequest;
  private SearchSourceBuilder sourceBuilder;

  @Before
  public void setUp() throws Exception {
    client = new RestHighLevelClient(RestClient.builder(
        new HttpHost("localhost", 9200, "http")));

    indexProduct("1", "Tisch und Stuhl und Fisch",
        "Ein Tisch und ein Stuhl und ein Fisch");
    indexProduct("2", "Tisch", "Ein Tisch");
    indexProduct("3", "Stuhl", "Ein Stuhl");
    indexProduct("4", "Fisch", "Ein Fisch");

    sourceBuilder = new SearchSourceBuilder()
        .query(QueryBuilders.termQuery("name", "tisch"))
        .sort(new ScoreSortBuilder().order(SortOrder.DESC))
        .timeout(new TimeValue(60, TimeUnit.SECONDS))
        .size(4);

    searchRequest = new SearchRequest()
        .source(sourceBuilder)
        .indices(INDEX)
        .types(TYPE);
  }

  @After
  public void tearDown() throws Exception {
    deleteProduct("1");
    deleteProduct("2");
    deleteProduct("3");
    deleteProduct("4");
    client.close();
  }

  @Test
  public void shouldFindSynchronously() throws Exception {
    final SearchResponse searchResponse = client.search(searchRequest);

    assertThat(searchResponse).isNotNull();
    assertThat(searchResponse.status()).isEqualTo(RestStatus.OK);
    assertThat(searchResponse.isTimedOut()).isFalse();
    assertThat(searchResponse.getHits().getMaxScore()).isEqualTo(0.6931472f);
    assertThat(searchResponse.getHits().getTotalHits()).isEqualTo(2L);
    final SearchHit firstHit = searchResponse.getHits().getAt(0);
    assertThat(firstHit.getIndex()).isEqualTo(INDEX);
    assertThat(firstHit.getType()).isEqualTo(TYPE);
    assertThat(firstHit.getScore()).isEqualTo(0.6931472f);
    assertThat(firstHit.getSourceAsString())
        .isEqualTo("{\"name\":\"Tisch\",\"description\":\"Ein Tisch\"}");
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
  public void shouldSortDesc() throws Exception {
    final SearchResponse searchResponse = client.search(searchRequest);

    assertThat(searchResponse.getHits().getAt(0).getScore())
        .isGreaterThan(searchResponse.getHits().getAt(1).getScore());
  }

  @Test
  public void shouldFindMatchQuery() throws Exception {
    // todo
  }

  @Test
  public void shouldFindWildcardQuery() throws Exception {
    // todo
  }

  @Test
  public void shouldFindTermQuery() throws Exception {
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

  private void indexProduct(final String documentId, final String name, final String description)
      throws IOException {
    final IndexRequest request = new IndexRequest(INDEX, TYPE, documentId)
        .timeout(TimeValue.timeValueSeconds(10))
        .setRefreshPolicy(RefreshPolicy.IMMEDIATE) // do not use in production
        .opType(OpType.INDEX) // replace existing document with same id
        .source("name", name, // field: [name, value]
            "description", description);

    try {
      final IndexResponse response = client.index(request);
      assertThat(response.getResult()).isIn(Result.CREATED, Result.UPDATED);
    } catch (final ElasticsearchException ex) {
      if (ex.status() == RestStatus.CONFLICT) {
        fail("Document already exists.");
      }
      fail("Failed to create index...", ex);
    }
  }

  private void deleteProduct(final String documentId) throws IOException {
    final DeleteRequest request = new DeleteRequest(INDEX, TYPE, documentId)
        .timeout(TimeValue.timeValueSeconds(10));
    final DeleteResponse response = client.delete(request);
    assertThat(response.getResult()).isIn(Result.DELETED, Result.NOT_FOUND);
  }
}
