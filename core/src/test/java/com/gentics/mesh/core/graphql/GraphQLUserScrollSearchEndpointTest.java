package com.gentics.mesh.core.graphql;

import com.gentics.mesh.test.TestSize;
import com.gentics.mesh.test.context.MeshTestSetting;
import com.syncleus.ferma.tx.Tx;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.ArrayList;
import java.util.List;

@MeshTestSetting(useElasticsearch = true, testSize = TestSize.FULL, startServer = true)
@RunWith(Parameterized.class)
public class GraphQLUserScrollSearchEndpointTest extends AbstractGraphQLSearchEndpointTest {

	@Parameters(name = "query={0}")
	public static List<String> paramData() {
		List<String> testQueries = new ArrayList<>();
		testQueries.add("user-elasticsearch-scroll-query");
		return testQueries;
	}


	public GraphQLUserScrollSearchEndpointTest(String queryName) {
		super(queryName);
	}

	@Before
	public void createUsers() {
		String username = "testuser";
		try (Tx tx = tx()) {
			for (int i = 0; i < 100; i++) {
				createUser(username + i);
			}
		}
	}

	@Test
	public void testNodeQuery() throws Exception {
		super.testNodeQuery();
	}

}
