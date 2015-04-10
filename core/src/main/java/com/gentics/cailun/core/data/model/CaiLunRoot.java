package com.gentics.cailun.core.data.model;

import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

import com.gentics.cailun.core.data.model.auth.AuthRelationships;
import com.gentics.cailun.core.data.model.auth.Group;
import com.gentics.cailun.core.data.model.auth.GroupRoot;
import com.gentics.cailun.core.data.model.auth.Role;
import com.gentics.cailun.core.data.model.auth.RoleRoot;
import com.gentics.cailun.core.data.model.auth.User;
import com.gentics.cailun.core.data.model.auth.UserRoot;
import com.gentics.cailun.core.data.model.generic.AbstractPersistable;
import com.gentics.cailun.core.data.model.relationship.BasicRelationships;

@NodeEntity
public class CaiLunRoot extends AbstractPersistable {

	private static final long serialVersionUID = -901251232180415110L;

	@Fetch
	@RelatedTo(type = BasicRelationships.HAS_PROJECT, direction = Direction.OUTGOING, elementClass = ProjectRoot.class)
	private ProjectRoot projectRoot;

	@Fetch
	@RelatedTo(type = AuthRelationships.HAS_USER, direction = Direction.OUTGOING, elementClass = UserRoot.class)
	private UserRoot userRoot;

	@Fetch
	@RelatedTo(type = AuthRelationships.HAS_ROLE, direction = Direction.OUTGOING, elementClass = RoleRoot.class)
	private RoleRoot roleRoot;

	@Fetch
	@RelatedTo(type = BasicRelationships.HAS_LANGUAGE, direction = Direction.OUTGOING, elementClass = LanguageRoot.class)
	private LanguageRoot languageRoot;

	@Fetch
	@RelatedTo(type = BasicRelationships.HAS_SCHEMA, direction = Direction.OUTGOING, elementClass = ObjectSchemaRoot.class)
	private ObjectSchemaRoot objectSchemaRoot;

	@Fetch
	@RelatedTo(type = BasicRelationships.HAS_ROOT_GROUP, direction = Direction.INCOMING, elementClass = GroupRoot.class)
	private GroupRoot groupRoot;

	@Indexed(unique = true)
	private String unique = CaiLunRoot.class.getSimpleName();

	public Set<User> getUsers() {
		return userRoot.getUsers();
	}

	public void addUser(User user) {
		this.userRoot.getUsers().add(user);
	}

	public Set<Language> getLanguages() {
		return languageRoot.getLanguages();
	}

	public void addLanguage(Language language) {
		this.languageRoot.getLanguages().add(language);
	}

	public Set<Group> getGroups() {
		return groupRoot.getGroups();
	}

	public void addGroup(Group group) {
		this.groupRoot.getGroups().add(group);
	}

	public Set<Role> getRoles() {
		return roleRoot.getRoles();
	}

	public void addRole(Role role) {
		this.roleRoot.getRoles().add(role);
	}

	public Set<ObjectSchema> getSchemas() {
		return objectSchemaRoot.getSchemas();
	}

	public void addSchema(ObjectSchema schema) {
		this.objectSchemaRoot.getSchemas().add(schema);
	}

	public Set<Project> getProjects() {
		return projectRoot.getProjects();
	}

	public void addProject(Project project) {
		this.projectRoot.getProjects().add(project);
	}

	public void setProjectRoot(ProjectRoot projectRoot) {
		this.projectRoot = projectRoot;
	}

	public void setGroupRoot(GroupRoot groupRoot) {
		this.groupRoot = groupRoot;
	}

	public void setRoleRoot(RoleRoot roleRoot) {
		this.roleRoot = roleRoot;
	}

	public void setLanguageRoot(LanguageRoot languageRoot) {
		this.languageRoot = languageRoot;
	}

	public void setObjectSchemaRoot(ObjectSchemaRoot objectSchemaRoot) {
		this.objectSchemaRoot = objectSchemaRoot;
	}

	public void setUserRoot(UserRoot userRoot) {
		this.userRoot = userRoot;
	}
}
