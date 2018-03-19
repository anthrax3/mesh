package com.gentics.mesh.search;

import org.junit.Test;

import com.gentics.elasticsearch.client.okhttp.RequestBuilder;
import com.gentics.mesh.core.data.User;
import com.gentics.mesh.core.data.search.IndexHandler;
import com.gentics.mesh.dagger.MeshInternal;
import com.gentics.mesh.search.impl.ElasticSearchProvider;
import com.gentics.mesh.search.impl.SearchClient;
import com.gentics.mesh.search.index.user.UserIndexHandler;
import com.gentics.mesh.test.TestSize;
import com.gentics.mesh.test.context.AbstractMeshTest;
import com.gentics.mesh.test.context.MeshTestSetting;
import com.syncleus.ferma.tx.Tx;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Test differential sync of elasticsearch.
 */
@MeshTestSetting(useElasticsearch = true, testSize = TestSize.PROJECT_AND_NODE, startServer = true)
public class ElasticsearchReindexTest extends AbstractMeshTest {

	private ElasticSearchProvider getProvider() {
		return ((ElasticSearchProvider) searchProvider());
	}

	@Test
	public void testReindex() throws Exception {
		ElasticSearchProvider provider = getProvider();

		tx(() -> {
			for (int i = 0; i < 200; i++) {
				boot().userRoot().create("user_" + i, user(), null);
			}
		});

		try (Tx tx = tx()) {
			recreateIndices();
		}

		// JsonObject doc = provider.getDocument(User.composeIndexName(), userUuid()).blockingGet();
		// System.out.println(doc.encodePrettily());

		SearchClient client = provider.getClient();
		JsonObject query = new JsonObject();
		query.put("size", 4);
		query.put("_source", new JsonArray().add("uuid").add("version"));
		query.put("query", new JsonObject().put("match_all", new JsonObject()));
		query.put("sort", new JsonArray().add("_doc"));

		// System.out.println(query.encodePrettily());
		RequestBuilder<JsonObject> builder = client.searchScroll(query, "1m", User.composeIndexName());
		JsonObject result = builder.sync();
		String scrollId = result.getString("_scroll_id");
		System.out.println(result.encodePrettily());
		System.out.println(scrollId);
		JsonObject result2 = client.scroll(scrollId, "1m").sync();
		System.out.println("----------");
		System.out.println(result2.encodePrettily());
		IndexHandler<?> indexHandler = MeshInternal.get().indexHandlerRegistry().getForClass(User.class);
		UserIndexHandler userIndexHandler = (UserIndexHandler) indexHandler;
		try (Tx tx = tx()) {
			for (User user : boot().userRoot().findAllIt()) {
				System.out.println("---------");
				System.out.println(userIndexHandler.generateVersion(user));
				System.out.println(user.getElementVersion());
			}
		}

	}
}
