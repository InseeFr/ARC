package fr.insee.arc.web.dao;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.arc.core.util.LoggerDispatcher;
import fr.insee.arc.utils.ressourceUtils.PropertiesHandler;

@Service
public class IgesaClient implements UserManagementDao {

	private static final Logger LOGGER = LogManager.getLogger(IgesaClient.class);
	
	@Autowired
	private PropertiesHandler properties;
	@Autowired
	private LoggerDispatcher loggerDispatcher;

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class IgesaApplication {
		private String cn;
		private List<IgesaGroup> groupes;
		public void setCn(String cn) {
			this.cn = cn;
		}
		public void setGroupes(List<IgesaGroup> groupes) {
			this.groupes = groupes;
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class IgesaGroup {
		private String cn;
		private String description;
		private List<IgesaPersonne> personnes;
		public void setCn(String cn) {
			this.cn = cn;
		}
		public void setDescription(String description) {
			this.description = description;
		}
		public void setPersonnes(List<IgesaPersonne> personnes) {
			this.personnes = personnes;
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class IgesaPersonne {
		private String cn;
		private String uid;
		public String getCn() {
			return cn;
		}
		public void setCn(String cn) {
			this.cn = cn;
		}
		public String getUid() {
			return uid;
		}
		public void setUid(String uid) {
			this.uid = uid;
		}
	}

	@Override
	public ArrayList<ArrayList<String>> getListProfils() {
		String response = get("/recherche/application/" + properties.getLdapApplicatioName());

		List<IgesaApplication> applicationsFound = mapTo(response, new TypeReference<List<IgesaApplication>>(){});
		ArrayList<ArrayList<String>> profiles = new ArrayList<>();
		if (applicationsFound != null) {
			for (IgesaApplication appli : applicationsFound) {
				if (appli.cn.equalsIgnoreCase(properties.getLdapApplicatioName())) {
					for (IgesaGroup group : applicationsFound.get(0).groupes) {
						ArrayList<String> profile = new ArrayList<>();
						profile.add(group.cn);
						profile.add(group.description);
						profiles.add(profile);
					}
				}
			}
		}
		return profiles;
	}

	@Override
	public ArrayList<ArrayList<String>> getListUsers(String profil) {
		String response = get("/recherche/application/" + properties.getLdapApplicatioName() + "/groupe/" + profil);

		List<IgesaGroup> groupsFound = mapTo(response, new TypeReference<List<IgesaGroup>>(){});
		ArrayList<ArrayList<String>> users = new ArrayList<>();
		if (groupsFound != null) {
			for (IgesaGroup group : groupsFound) {
				if (group.cn.equalsIgnoreCase(profil)) {
					for (IgesaPersonne userProfile : group.personnes) {
						ArrayList<String> user = new ArrayList<>();
						user.add(userProfile.uid);
						user.add(userProfile.cn);
						user.add(group.cn);
						user.add(group.description);
						users.add(user);
					}
				}

			}
		}
		return users;
	}

	@Override
	public boolean addProfile(String name, String description) {
		return post("/gestion/ajout/groupe/application/"
				+ properties.getLdapApplicatioName()
				+ "/groupe/" + name)
				.equals(HttpStatus.OK);
	}

	@Override
	public boolean deleteProfile(String name) {
		return post("/gestion/suppression/groupe/application/"
				+ properties.getLdapApplicatioName()
				+ "/groupe/" + name)
				.equals(HttpStatus.OK);
	}

	@Override
	public boolean addUser(String id, String group) {
		return post("/gestion/ajout/personne/application/"
				+ properties.getLdapApplicatioName()
				+ "/groupe/" + group
				+ "/utilisateur/" + id)
				.equals(HttpStatus.OK)
				&& isUserInGroup(id, group);
	}

	@Override
	public boolean removeUser(String id, String group) {
		return post("/gestion/suppression/personne/application/"
				+ properties.getLdapApplicatioName()
				+ "/groupe/" + group
				+ "/utilisateur/" + id)
				.equals(HttpStatus.OK)
				&& !isUserInGroup(id, group);
	}

	private <T> T mapTo(String value, TypeReference<T> typeRef) {
		try {
			return new ObjectMapper().readValue(value, typeRef);
		} catch (JsonProcessingException e) {
			loggerDispatcher.error("An error occured when mapping a result from Igesa", e, LOGGER);
		}
		return null;
	}
	
	private boolean isUserInGroup(String userId, String group) {
		String response = get("/recherche/application/" + properties.getLdapApplicatioName() + "/groupe/" + group);
		List<IgesaGroup> groupsFound = mapTo(response, new TypeReference<List<IgesaGroup>>(){});
		if (groupsFound == null) {
			return false;
		}
		return groupsFound.stream()
				.filter(g -> g.cn.equalsIgnoreCase(group))
				.anyMatch(g -> g.personnes.stream()
						.anyMatch(p -> p.uid.equalsIgnoreCase(userId)));
	}

	private String get(String uri){
		return WebClient.create(properties.getLdapDirectoryUri())
				.method(HttpMethod.GET)
				.uri(uri)
				.accept(MediaType.APPLICATION_JSON)
				.acceptCharset(StandardCharsets.UTF_8)
				.retrieve()
				.bodyToMono(String.class)
				.block();
	}

	private HttpStatus post(String uri) {
		return WebClient.create(properties.getLdapDirectoryUri())
				.method(HttpMethod.POST)
				.uri(uri)
				.headers(httpHeaders -> httpHeaders.setBasicAuth(
						properties.getLdapDirectoryIdent(),
						properties.getLdapDirectoryPassword()))
				.exchange()
				.block()
				.statusCode();
	}
}
