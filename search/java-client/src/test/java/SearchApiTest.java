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
 *      "https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high-document-index.html">Index
 *      API Doc</a>
 * @see <a href=
 *      "https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high-document-delete.html">Delete
 *      API Doc</a>
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
        "Ein Tisch und ein Stuhl und ein Fisch", 100);
    indexProduct("2", "Tisch", "Ein Tisch", 200);
    indexProduct("3", "Stuhl", "Ein Stuhl", 300);
    indexProduct("4", "Fisch", "Ein Fisch", 400);

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
  public void shouldFindWildcardQuery() throws Exception {
    executeWildcardQuery("name", "?isch", 3L);
    executeWildcardQuery("name", "st*", 2L);
  }

  @Test
  public void shouldFindMatchQuery() throws Exception {
    assertThat(client.search(searchRequest.source(
        sourceBuilder.query(QueryBuilders.matchQuery("name", "tisch")))).getHits().getTotalHits())
            .isEqualTo(2L);
    assertThat(client.search(searchRequest.source(
        sourceBuilder.query(QueryBuilders.matchQuery("name", "t?sch")))).getHits().getTotalHits())
            .isEqualTo(0L);
  }

  @Test
  public void shouldFindExactPhrase() throws Exception {
    final SearchResponse response = client.search(searchRequest.source(
        sourceBuilder.query(QueryBuilders.matchPhraseQuery("description", "ein stuhl"))));
    assertThat(response.getHits().getTotalHits()).isEqualTo(2L);
    assertThat(response.getHits().getAt(0).getId()).isEqualTo("1");
    assertThat(response.getHits().getAt(1).getId()).isEqualTo("3");
  }

  @Test
  public void shouldFindOrCombinedTerm() throws Exception {
    final SearchResponse response = client.search(searchRequest.source(
        sourceBuilder.query(QueryBuilders.termsQuery("name", "tisch", "stuhl"))));
    assertThat(response.getHits().getTotalHits()).isEqualTo(3L);
    assertThat(response.getHits().getAt(0).getId()).isEqualTo("2");
    assertThat(response.getHits().getAt(1).getId()).isEqualTo("1");
    assertThat(response.getHits().getAt(2).getId()).isEqualTo("3");
  }

  @Test
  public void shouldFindAndCombinedTerm() throws Exception {
    final SearchResponse response = client.search(searchRequest.source(
        sourceBuilder.query(QueryBuilders.boolQuery()
            .must(QueryBuilders.termQuery("name", "tisch"))
            .must(QueryBuilders.termQuery("name", "stuhl")))));
    assertThat(response.getHits().getTotalHits()).isEqualTo(1L);
    assertThat(response.getHits().getAt(0).getId()).isEqualTo("1");
  }

  @Test
  public void shouldFindXorCombinedTerm() throws Exception {
    final SearchResponse response = client.search(searchRequest.source(
        sourceBuilder.query(QueryBuilders.boolQuery()
            .should(QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery("name", "tisch"))
                .mustNot(QueryBuilders.termQuery("name", "stuhl")))
            .should(QueryBuilders.boolQuery()
                .mustNot(QueryBuilders.termQuery("name", "tisch"))
                .must(QueryBuilders.termQuery("name", "stuhl"))))));
    assertThat(response.getHits().getTotalHits()).isEqualTo(2L);
    assertThat(response.getHits().getAt(0).getId()).isEqualTo("2");
    assertThat(response.getHits().getAt(1).getId()).isEqualTo("3");
  }

  @Test
  public void shouldFindRanges() throws Exception {
    final SearchResponse response = client.search(searchRequest.source(
        sourceBuilder.query(QueryBuilders.rangeQuery("price").gte(200).lte(300))));
    assertThat(response.getHits().getTotalHits()).isEqualTo(2L);
    assertThat(response.getHits().getAt(0).getId()).isEqualTo("2");
    assertThat(response.getHits().getAt(1).getId()).isEqualTo("3");
  }

  @Test
  public void shouldExcludeTerm() throws Exception {
    final SearchResponse response = client.search(searchRequest.source(
        sourceBuilder.query(QueryBuilders.boolQuery()
            .must(QueryBuilders.termQuery("name", "tisch"))
            .mustNot(QueryBuilders.termQuery("name", "stuhl")))));
    assertThat(response.getHits().getTotalHits()).isEqualTo(1L);
    assertThat(response.getHits().getAt(0).getId()).isEqualTo("2");
  }

  private void indexProduct(final String documentId, final String name, final String description,
      final Object price)
      throws IOException {
    final IndexRequest request = new IndexRequest(INDEX, TYPE, documentId)
        .timeout(TimeValue.timeValueSeconds(10))
        .setRefreshPolicy(RefreshPolicy.IMMEDIATE) // do not use in production
        .opType(OpType.INDEX) // replace existing document with same id
        .source("name", name, // field: [name, value]
            "description", description,
            "price", price);

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

  private void executeWildcardQuery(final String fieldName, final String pattern,
      final long totalHits) throws IOException {
    final SearchResponse searchResponse = client.search(searchRequest.source(
        sourceBuilder.query(QueryBuilders.wildcardQuery(fieldName, pattern))));

    assertThat(searchResponse.getHits().getTotalHits()).isEqualTo(totalHits);
  }
}
