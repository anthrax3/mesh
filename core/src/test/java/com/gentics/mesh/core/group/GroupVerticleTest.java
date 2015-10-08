package com.gentics.mesh.core.group;

import static com.gentics.mesh.core.data.relationship.GraphPermission.CREATE_PERM;
import static com.gentics.mesh.core.data.relationship.GraphPermission.DELETE_PERM;
import static com.gentics.mesh.core.data.relationship.GraphPermission.READ_PERM;
import static com.gentics.mesh.core.data.relationship.GraphPermission.UPDATE_PERM;
import static com.gentics.mesh.util.MeshAssert.assertElement;
import static com.gentics.mesh.util.MeshAssert.assertSuccess;
import static com.gentics.mesh.util.MeshAssert.latchFor;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.CONFLICT;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.stream.Collectors;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.gentics.mesh.api.common.PagingInfo;
import com.gentics.mesh.core.AbstractWebVerticle;
import com.gentics.mesh.core.data.Group;
import com.gentics.mesh.core.data.User;
import com.gentics.mesh.core.data.root.GroupRoot;
import com.gentics.mesh.core.rest.common.GenericMessageResponse;
import com.gentics.mesh.core.rest.error.HttpStatusCodeErrorException;
import com.gentics.mesh.core.rest.group.GroupCreateRequest;
import com.gentics.mesh.core.rest.group.GroupListResponse;
import com.gentics.mesh.core.rest.group.GroupResponse;
import com.gentics.mesh.core.rest.group.GroupUpdateRequest;
import com.gentics.mesh.core.verticle.group.GroupVerticle;
import com.gentics.mesh.handler.InternalActionContext;
import com.gentics.mesh.test.AbstractBasicCrudVerticleTest;
import com.gentics.mesh.util.MeshAssert;

import io.vertx.core.Future;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class GroupVerticleTest extends AbstractBasicCrudVerticleTest {

	private static final Logger log = LoggerFactory.getLogger(GroupVerticleTest.class);

	@Autowired
	private GroupVerticle verticle;

	@Override
	public List<AbstractWebVerticle> getVertices() {
		List<AbstractWebVerticle> list = new ArrayList<>();
		list.add(verticle);
		return list;
	}

	// Create Tests
	@Test
	@Override
	public void testCreate() throws Exception {
		final String name = "test12345";
		GroupCreateRequest request = new GroupCreateRequest();
		request.setName(name);
		role().grantPermissions(meshRoot().getGroupRoot(), CREATE_PERM);

		Future<GroupResponse> future = getClient().createGroup(request);
		latchFor(future);
		assertSuccess(future);
		GroupResponse restGroup = future.result();
		test.assertGroup(request, restGroup);
		assertElement(boot.groupRoot(), restGroup.getUuid(), true);
	}

	@Test
	public void testBatchCreation() {
		for (int i = 0; i < 10; i++) {
			System.out.println(i);
			final String name = "test_" + i;
			GroupCreateRequest request = new GroupCreateRequest();
			request.setName(name);
			GroupRoot root = meshRoot().getGroupRoot();
			root.reload();
			role().grantPermissions(root, CREATE_PERM);

			Future<GroupResponse> future = getClient().createGroup(request);
			latchFor(future);
			assertSuccess(future);
			GroupResponse restGroup = future.result();
			test.assertGroup(request, restGroup);
		}
	}

	@Test
	public void testConflicingGroupCreation() throws Exception {
		final String name = "test12345";
		GroupCreateRequest request = new GroupCreateRequest();
		request.setName(name);
		role().grantPermissions(meshRoot().getGroupRoot(), CREATE_PERM);
		Future<GroupResponse> future = getClient().createGroup(request);
		latchFor(future);
		assertSuccess(future);
		GroupResponse restGroup = future.result();
		test.assertGroup(request, restGroup);

		assertElement(boot.groupRoot(), restGroup.getUuid(), true);
		future = getClient().createGroup(request);
		latchFor(future);
		expectException(future, CONFLICT, "group_conflicting_name", name);
	}

	@Test
	@Override
	public void testCreateReadDelete() throws Exception {
		// Create the group
		final String name = "test12345";
		GroupCreateRequest request = new GroupCreateRequest();
		request.setName(name);

		Future<GroupResponse> future = getClient().createGroup(request);
		latchFor(future);
		assertSuccess(future);
		GroupResponse restGroup = future.result();
		test.assertGroup(request, restGroup);

		boot.groupRoot().findByUuid(restGroup.getUuid(), rh -> {
			assertNotNull("Group should have been created.", rh.result());
		});

		Future<GroupResponse> readFuture = getClient().findGroupByUuid(restGroup.getUuid());
		latchFor(readFuture);
		assertSuccess(readFuture);

		// Now delete the group
		Future<GenericMessageResponse> deleteFuture = getClient().deleteGroup(restGroup.getUuid());
		latchFor(deleteFuture);
		assertSuccess(deleteFuture);
		expectMessageResponse("group_deleted", deleteFuture, restGroup.getUuid() + "/" + restGroup.getName());
	}

	@Test
	public void testCreateGroupWithMissingName() throws Exception {
		GroupCreateRequest request = new GroupCreateRequest();
		role().grantPermissions(group(), CREATE_PERM);

		Future<GroupResponse> future = getClient().createGroup(request);
		latchFor(future);
		expectException(future, BAD_REQUEST, "error_name_must_be_set");

	}

	@Test
	public void testCreateGroupWithNoPerm() throws Exception {
		final String name = "test12345";
		GroupCreateRequest request = new GroupCreateRequest();
		InternalActionContext ac = getMockedInternalActionContext("");
		request.setName(name);
		String rootUuid;
		GroupRoot root = meshRoot().getGroupRoot();
		rootUuid = root.getUuid();
		role().revokePermissions(root, CREATE_PERM);
		User user = user();
		assertFalse("The create permission to the groups root node should have been revoked.", user.hasPermission(ac, root, CREATE_PERM));

		Future<GroupResponse> future = getClient().createGroup(request);
		latchFor(future);
		expectException(future, FORBIDDEN, "error_missing_perm", rootUuid);

	}

	// Read Tests

	@Test
	@Override
	public void testReadMultiple() throws Exception {

		int totalGroups = 0;
		String extraGroupName = "no_perm_group";
		GroupRoot root = meshRoot().getGroupRoot();
		// Create and save some groups
		final int nGroups = 21;
		root.create(extraGroupName, user());
		for (int i = 0; i < nGroups; i++) {
			Group group = root.create("group_" + i, user());
			role().grantPermissions(group, READ_PERM);
		}

		totalGroups = nGroups + groups().size();
		// Test default paging parameters
		Future<GroupListResponse> future = getClient().findGroups();
		latchFor(future);
		assertSuccess(future);
		GroupListResponse restResponse = future.result();
		assertEquals(25, restResponse.getMetainfo().getPerPage());
		assertEquals(1, restResponse.getMetainfo().getCurrentPage());
		assertEquals(25, restResponse.getData().size());

		int perPage = 6;
		future = getClient().findGroups(new PagingInfo(3, perPage));
		latchFor(future);
		assertSuccess(future);
		restResponse = future.result();

		assertEquals(perPage, restResponse.getData().size());

		// created groups + test data group
		int totalPages = (int) Math.ceil(totalGroups / (double) perPage);
		assertEquals("The response did not contain the correct amount of items", perPage, restResponse.getData().size());
		assertEquals(3, restResponse.getMetainfo().getCurrentPage());
		assertEquals("We expect {" + totalGroups + "} groups and with a paging size of {" + perPage + "} exactly {" + totalPages + "} pages.",
				totalPages, restResponse.getMetainfo().getPageCount());
		assertEquals(perPage, restResponse.getMetainfo().getPerPage());
		assertEquals(totalGroups, restResponse.getMetainfo().getTotalCount());

		List<GroupResponse> allGroups = new ArrayList<>();
		for (int page = 1; page <= totalPages; page++) {
			Future<GroupListResponse> pageFuture = getClient().findGroups(new PagingInfo(page, perPage));
			latchFor(pageFuture);
			assertSuccess(pageFuture);
			restResponse = pageFuture.result();
			allGroups.addAll(restResponse.getData());
		}
		assertEquals("Somehow not all groups were loaded when loading all pages.", totalGroups, allGroups.size());

		// Verify that extra group is not part of the response
		List<GroupResponse> filteredUserList = allGroups.parallelStream().filter(restGroup -> restGroup.getName().equals(extraGroupName))
				.collect(Collectors.toList());
		assertTrue("Extra group should not be part of the list since no permissions were added.", filteredUserList.size() == 0);

		future = getClient().findGroups(new PagingInfo(-1, perPage));
		latchFor(future);
		expectException(future, BAD_REQUEST, "error_invalid_paging_parameters");

		future = getClient().findGroups(new PagingInfo(1, 0));
		latchFor(future);
		expectException(future, BAD_REQUEST, "error_invalid_paging_parameters");

		future = getClient().findGroups(new PagingInfo(1, -1));
		latchFor(future);
		expectException(future, BAD_REQUEST, "error_invalid_paging_parameters");

		future = getClient().findGroups(new PagingInfo(4242, 1));
		latchFor(future);
		assertSuccess(future);

		assertEquals(0, future.result().getData().size());
		assertEquals(4242, future.result().getMetainfo().getCurrentPage());
		assertEquals(25, future.result().getMetainfo().getPageCount());
		assertEquals(25, future.result().getMetainfo().getTotalCount());
		assertEquals(1, future.result().getMetainfo().getPerPage());

	}

	@Test
	@Override
	public void testReadByUUID() throws Exception {
		Group group = group();
		assertNotNull("The UUID of the group must not be null.", group.getUuid());

		Future<GroupResponse> future = getClient().findGroupByUuid(group.getUuid());
		latchFor(future);
		assertSuccess(future);
		test.assertGroup(group, future.result());
	}

	@Test
	@Override
	public void testReadByUUIDWithMissingPermission() throws Exception {
		Group group = group();
		role().revokePermissions(group, READ_PERM);
		assertNotNull("The UUID of the group must not be null.", group.getUuid());
		Future<GroupResponse> future = getClient().findGroupByUuid(group.getUuid());
		latchFor(future);
		expectException(future, FORBIDDEN, "error_missing_perm", group.getUuid());
	}

	@Test
	public void testReadGroupWithBogusUUID() throws Exception {
		final String bogusUuid = "sadgasdasdg";
		Future<GroupResponse> future = getClient().findGroupByUuid(bogusUuid);
		latchFor(future);
		expectException(future, NOT_FOUND, "object_not_found_for_uuid", bogusUuid);
	}

	// Update Tests

	@Test
	@Override
	public void testUpdate() throws HttpStatusCodeErrorException, Exception {
		Group group = group();
		final String name = "New Name";
		GroupUpdateRequest request = new GroupUpdateRequest();
		request.setName(name);

		Future<GroupResponse> future = getClient().updateGroup(group.getUuid(), request);
		latchFor(future);
		assertSuccess(future);
		GroupResponse restGroup = future.result();
		test.assertGroup(request, restGroup);

		CountDownLatch latch = new CountDownLatch(1);
		boot.groupRoot().findByUuid(restGroup.getUuid(), rh -> {
			Group reloadedGroup = rh.result();
			assertEquals("The group should have been updated", name, reloadedGroup.getName());
			latch.countDown();
		});
		MeshAssert.failingLatch(latch);
	}

	@Test
	@Override
	public void testUpdateByUUIDWithoutPerm() throws Exception {
		role().revokePermissions(group(), UPDATE_PERM);
		String uuid = group().getUuid();
		GroupUpdateRequest request = new GroupUpdateRequest();
		request.setName("new Name");

		Future<GroupResponse> future = getClient().updateGroup(uuid, request);
		latchFor(future);
		expectException(future, FORBIDDEN, "error_missing_perm", uuid);
	}

	@Test
	public void testUpdateGroupWithEmptyName() throws HttpStatusCodeErrorException, Exception {
		Group group = group();

		role().grantPermissions(group, UPDATE_PERM);
		final String name = "";
		GroupUpdateRequest request = new GroupUpdateRequest();
		request.setName(name);

		Future<GroupResponse> future = getClient().updateGroup(group.getUuid(), request);
		latchFor(future);
		expectException(future, BAD_REQUEST, "error_name_must_be_set");

		boot.groupRoot().findByUuid(group.getUuid(), rh -> {
			Group reloadedGroup = rh.result();
			assertEquals("The group should not have been updated", group.getName(), reloadedGroup.getName());
		});
	}

	@Test
	public void testUpdateGroupWithConflictingName() throws HttpStatusCodeErrorException, Exception {

		final String alreadyUsedName = "extraGroup";
		GroupRoot groupRoot = meshRoot().getGroupRoot();
		// Create a group which occupies the name
		assertNotNull(groupRoot.create(alreadyUsedName, user()));
		role().grantPermissions(group(), UPDATE_PERM);
		GroupUpdateRequest request = new GroupUpdateRequest();
		request.setName(alreadyUsedName);

		Future<GroupResponse> future = getClient().updateGroup(group().getUuid(), request);
		latchFor(future);
		expectException(future, CONFLICT, "group_conflicting_name");

		groupRoot.findByUuid(group().getUuid(), rh -> {
			Group reloadedGroup = rh.result();
			assertEquals("The group should not have been updated", group().getName(), reloadedGroup.getName());
		});
	}

	@Test
	@Override
	public void testUpdateWithBogusUuid() throws HttpStatusCodeErrorException, Exception {
		final String name = "New Name";
		GroupUpdateRequest request = new GroupUpdateRequest();
		request.setName(name);

		Future<GroupResponse> future = getClient().updateGroup("bogus", request);
		latchFor(future);
		expectException(future, NOT_FOUND, "object_not_found_for_uuid", "bogus");
	}

	// Delete Tests

	@Test
	@Override
	public void testDeleteByUUID() throws Exception {
		Group group = group();
		String name = group.getName();
		String uuid = group.getUuid();
		assertNotNull(uuid);
		Future<GenericMessageResponse> future = getClient().deleteGroup(uuid);
		latchFor(future);
		assertSuccess(future);
		expectMessageResponse("group_deleted", future, uuid + "/" + name);
		assertElement(boot.groupRoot(), uuid, false);

	}

	@Test
	@Override
	public void testDeleteByUUIDWithNoPermission() throws Exception {
		Group group = group();
		String uuid = group.getUuid();
		assertNotNull(uuid);
		// Don't allow delete
		role().revokePermissions(group, DELETE_PERM);

		Future<GenericMessageResponse> future = getClient().deleteGroup(uuid);
		latchFor(future);
		expectException(future, FORBIDDEN, "error_missing_perm", group.getUuid());
		assertElement(boot.groupRoot(), group.getUuid(), true);
	}

	@Test
	@Override
	@Ignore("not yet enabled")
	public void testUpdateMultithreaded() throws InterruptedException {
		GroupUpdateRequest request = new GroupUpdateRequest();
		request.setName("changed");

		int nJobs = 5;
		CyclicBarrier barrier = prepareBarrier(nJobs);
		Set<Future<?>> set = new HashSet<>();
		for (int i = 0; i < nJobs; i++) {
			set.add(getClient().updateGroup(group().getUuid(), request));
		}
		validateSet(set, barrier);

	}

	@Test
	@Override
	@Ignore("not yet enabled")
	public void testReadByUuidMultithreaded() throws InterruptedException {
		int nJobs = 10;
		String uuid = user().getUuid();
		CyclicBarrier barrier = prepareBarrier(nJobs);
		Set<Future<?>> set = new HashSet<>();
		for (int i = 0; i < nJobs; i++) {
			log.debug("Invoking findGroupByUuid REST call");
			set.add(getClient().findGroupByUuid(uuid));
		}
		validateSet(set, barrier);
	}

	@Test
	@Override
	@Ignore("not yet enabled")
	public void testDeleteByUUIDMultithreaded() throws InterruptedException {
		int nJobs = 3;
		String uuid = group().getUuid();
		CyclicBarrier barrier = prepareBarrier(nJobs);
		Set<Future<GenericMessageResponse>> set = new HashSet<>();
		for (int i = 0; i < nJobs; i++) {
			log.debug("Invoking deleteUser REST call");
			set.add(getClient().deleteGroup(uuid));
		}
		validateDeletion(set, barrier);
	}

	@Test
	@Override
	@Ignore("not yet enabled")
	public void testCreateMultithreaded() throws Exception {
		int nJobs = 5;
		CyclicBarrier barrier = prepareBarrier(nJobs);
		Set<Future<?>> set = new HashSet<>();
		for (int i = 0; i < nJobs; i++) {
			log.debug("Invoking createGroup REST call");
			GroupCreateRequest request = new GroupCreateRequest();
			request.setName("test12345_" + i);
			set.add(getClient().createGroup(request));
		}
		validateCreation(set, barrier);

	}

	@Test
	@Override
	public void testReadByUuidMultithreadedNonBlocking() throws InterruptedException {
		int nJobs = 200;
		Set<Future<GroupResponse>> set = new HashSet<>();
		for (int i = 0; i < nJobs; i++) {
			log.debug("Invoking findGroupByUuid REST call");
			set.add(getClient().findGroupByUuid(group().getUuid()));
		}
		for (Future<GroupResponse> future : set) {
			latchFor(future);
			assertSuccess(future);
		}
	}

}