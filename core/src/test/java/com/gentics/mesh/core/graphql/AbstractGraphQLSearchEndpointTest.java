package com.gentics.mesh.core.graphql;

import com.gentics.mesh.core.rest.graphql.GraphQLResponse;
import com.gentics.mesh.test.context.AbstractMeshTest;
import com.syncleus.ferma.tx.Tx;
import io.vertx.core.json.JsonObject;

import static com.gentics.mesh.assertj.MeshAssertions.assertThat;
import static com.gentics.mesh.test.ClientHelper.call;
import static com.gentics.mesh.test.TestDataProvider.PROJECT_NAME;


public abstract class AbstractGraphQLSearchEndpointTest extends AbstractMeshTest {

	protected final String queryName;

	public AbstractGraphQLSearchEndpointTest(String queryName) {
		this.queryName = queryName;
	}


	public void testNodeQuery() throws Exception {
		try (Tx tx = tx()) {
			recreateIndices();
		}

		GraphQLResponse response = call(() -> client().graphqlQuery(PROJECT_NAME, getGraphQLQuery(queryName)));
		JsonObject json = new JsonObject(response.toJson());
		System.out.println(json.encodePrettily());
		assertThat(json).compliesToAssertions(queryName);
	}
}
