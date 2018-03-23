package com.gentics.mesh.core.graphql;

import com.gentics.mesh.test.TestSize;
import com.gentics.mesh.test.context.MeshTestSetting;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.ArrayList;
import java.util.List;

@MeshTestSetting(useElasticsearch = true, testSize = TestSize.FULL, startServer = true)
@RunWith(Parameterized.class)
public class GraphQLSearchEndpointTest extends AbstractGraphQLSearchEndpointTest {

	@Parameters(name = "query={0}")
	public static List<String> paramData() {
		List<String> testQueries = new ArrayList<>();
		testQueries.add("user-elasticsearch-query");
		testQueries.add("group-elasticsearch-query");
		testQueries.add("role-elasticsearch-query");
		testQueries.add("node-elasticsearch-query");
		testQueries.add("tag-elasticsearch-query");
		testQueries.add("tagFamily-elasticsearch-query");
		return testQueries;
	}

	public GraphQLSearchEndpointTest(String queryName) {
		super(queryName);
	}

	@Test
	public void testNodeQuery() throws Exception {
		super.testNodeQuery();
	}
}
