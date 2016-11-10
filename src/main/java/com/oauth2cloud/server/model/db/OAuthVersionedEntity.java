package com.oauth2cloud.server.model.db;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.voodoodyne.jackson.jsog.JSOGGenerator;

@JsonIdentityInfo(generator = JSOGGenerator.class)
public class OAuthVersionedEntity extends com.moodysalem.hibernate.model.VersionedEntity {
}
