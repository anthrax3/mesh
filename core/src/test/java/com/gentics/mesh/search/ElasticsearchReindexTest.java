package com.gentics.mesh.search;

import org.junit.Test;

import com.gentics.mesh.core.data.User;
import com.gentics.mesh.search.impl.ElasticSearchProvider;
import com.gentics.mesh.test.TestSize;
import com.gentics.mesh.test.context.AbstractMeshTest;
import com.gentics.mesh.test.context.MeshTestSetting;
import com.syncleus.ferma.tx.Tx;

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
		provider.clear().blockingAwait();

		tx(() -> {
			for (int i = 0; i < 400; i++) {
				boot().userRoot().create("user_" + i, user(), null);
			}
		});

		try (Tx tx = tx()) {
			recreateIndices();
		}

		// call(() -> client().invokeReindex());
		provider.invokeReindex().blockingAwait();
		System.out.println("--------------");
		provider.invokeReindex().blockingAwait();

		provider.deleteDocument(User.composeIndexName(), userUuid()).blockingAwait();
		provider.refreshIndex("_all").blockingAwait();
		System.out.println("--------------");
		provider.invokeReindex().blockingAwait();
		try (Tx tx = tx()) {
			user().setName("blar");
			tx.success();
		}
		System.out.println("--------------");
		provider.invokeReindex().blockingAwait();

		try (Tx tx = tx()) {
			user().getElement().remove();
			tx.success();
		}
		System.out.println("--------------");
		provider.invokeReindex().blockingAwait();

	}
}
