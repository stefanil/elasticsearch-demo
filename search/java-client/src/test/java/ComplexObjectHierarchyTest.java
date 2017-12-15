import java.io.IOException;
import java.util.concurrent.TimeUnit;

import model.Feature;
import model.Product;
import org.apache.http.HttpHost;
import org.elasticsearch.ElasticsearchException;
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
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.ScoreSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class ComplexObjectHierarchyTest {

  private static final String INDEX = "myindex";
  private static final String TYPE = "product";

  private RestHighLevelClient client;

  private SearchSourceBuilder sourceBuilder;
  private SearchRequest searchRequest;

  @Before
  public void setUp() throws Exception {
    client = new RestHighLevelClient(RestClient.builder(
        new HttpHost("localhost", 9200, "http")));

    indexProduct(new Product("Tisch", "Ein Tisch", asList(
        new Feature("Aufbauservice", asList("mit Aufbauservice", "ohne Aufbauservice")),
        new Feature("Art Stauraum", asList("Ablageboden", "Innenfach geschlossen",
            "Innenfach offen", "Schubladen", "Zeitungsfach")),
        new Feature("Erweiterungsfunktion", asList("Ansteckplatte", "Ansteckplatten",
            "Butterfly-Auszug", "Kulissenauszug", "Mittelauszug", "Mittelauszug mit Einlegeplatte",
            "Mittelauszug mit Klappeinlage", "Synchronauszug")))),
        "1");
    indexProduct(new Product("Stuhl", "Ein Stuhl", asList(
        new Feature("Gestellform", asList("V-Form", "Y-Form")),
        new Feature("Gewicht", emptyList()))), "2");
    indexProduct(new Product("Tisch", "Ein Tisch", asList(
        new Feature("Aufbauservice1", asList("mit Aufbauservice1", "ohne Aufbauservice1")))),
        "3");

    sourceBuilder = new SearchSourceBuilder()
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
    client.close();
  }

  @Test
  public void shouldFindInComplexHierarchy() throws Exception {
    final SearchResponse response = client.search(searchRequest.source(
        sourceBuilder.query(QueryBuilders.boolQuery()
            .must(QueryBuilders
                .queryStringQuery(  // case insensitive as lowercase analyzer is used by default
                    "(name: Tisch)^10 (description: Tisch)^20 (features: Aufbauservice)^5")
                .analyzeWildcard(true)
                .defaultField("*")))));
     assertThat(response.getHits().getTotalHits()).isEqualTo(2L);
     assertThat(response.getHits().getAt(0).getId()).isEqualTo("1");
     assertThat(response.getHits().getAt(0).getScore()).isEqualTo(10.890821f);
     assertThat(response.getHits().getAt(1).getId()).isEqualTo("3");
     assertThat(response.getHits().getAt(1).getScore()).isEqualTo(8.630463f);
  }

  private void indexProduct(final Product product, final String documentId)
      throws IOException {
    final IndexRequest request = new IndexRequest(INDEX, TYPE, documentId)
        .timeout(TimeValue.timeValueSeconds(10))
        .setRefreshPolicy(RefreshPolicy.IMMEDIATE)
        .opType(OpType.INDEX)
        .source("name", product.getName(),
            "description", product.getDescription(),
            "features", product.getFeatures());

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
