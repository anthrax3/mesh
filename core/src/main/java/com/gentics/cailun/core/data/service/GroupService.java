package com.gentics.cailun.core.data.service;

import io.vertx.ext.apex.RoutingContext;

import org.springframework.data.domain.Page;

import com.gentics.cailun.core.data.model.auth.Group;
import com.gentics.cailun.core.data.model.auth.User;
import com.gentics.cailun.core.data.service.generic.GenericNodeService;
import com.gentics.cailun.core.rest.group.response.GroupResponse;
import com.gentics.cailun.paging.PagingInfo;

public interface GroupService extends GenericNodeService<Group> {

	public Group findByName(String name);

	public Group findByUUID(String uuid);

	public GroupResponse transformToRest(RoutingContext rc, Group group);

	public Page<Group> findAllVisible(User requestUser, PagingInfo pagingInfo);

}
