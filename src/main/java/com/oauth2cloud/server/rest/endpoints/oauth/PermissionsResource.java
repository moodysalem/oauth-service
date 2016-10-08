package com.oauth2cloud.server.rest.endpoints.oauth;

public class PermissionsResource {
//
//
//    private Response handlePermissionsAction(final Client client,
//                                             final boolean rememberMe,
//                                             final MultivaluedMap<String, String> formParams) {
//        final String loginToken = formParams.getFirst("login_token");
//
//        final LoginModel loginModel = new LoginModel();
//        loginModel.setClient(client);
//        loginModel.setURLs(req);
//        loginModel.setState(state);
//
//        // they just completed the second step of the login
//        if (!isBlank(loginToken)) {
//            final Token token = QueryUtil.getPermissionToken(em, loginToken, client);
//            if (token == null) {
//                loginModel.setLoginbadRequest(SOMETHING_WENT_WRONG_PLEASE_TRY_AGAIN);
//            } else {
//                if (token.getExpires().before(new Date())) {
//                    loginModel.setLoginbadRequest(YOUR_LOGIN_ATTEMPT_HAS_EXPIRED_PLEASE_TRY_AGAIN);
//                } else {
//                    // first get all the client scopes we will try to approve or check if are approved
//                    final Set<ClientScope> clientScopes = QueryUtil.getScopes(em, client, getScopes());
//                    // we'll populate this as we loop through the scopes
//                    final Set<AcceptedScope> tokenScopes = new HashSet<>();
//                    // get all the scope ids that were explicitly granted
//                    final Set<UUID> acceptedScopeIds = formParams.keySet().stream()
//                            .map((s) -> {
//                                try {
//                                    return (s != null && s.startsWith(SCOPE_FORM_TOGGLE_NAME) &&
//                                            CHECKBOX_CHECKED_VALUE.equalsIgnoreCase(formParams.getFirst(s))) ?
//                                            UUID.fromString(s.substring(SCOPE_FORM_TOGGLE_NAME.length())) :
//                                            null;
//                                } catch (Exception e) {
//                                    return null;
//                                }
//                            })
//                            .filter((id) -> id != null)
//                            .collect(Collectors.toSet());
//
//                    // if it's not ASK, or it's explicitly granted, we should create/find the AcceptedScope record
//                    // create/find the accepted scope for this client scope
//                    tokenScopes.addAll(
//                            clientScopes.stream()
//                                    .filter(cs -> !cs.getPriority().equals(ClientScope.Priority.ASK) || acceptedScopeIds.contains(cs.getScope().getId()))
//                                    .map(cs -> QueryUtil.acceptScope(em, token.getUser(), cs))
//                                    .collect(Collectors.toList())
//                    );
//
//                    final TokenType type = getTokenType(responseType);
//
//                    // now create the token we will be returning to the user
//                    return getRedirectResponse(redirectUri, state, fromPermissionToken(type, token, tokenScopes), rememberMe);
//                }
//            }
//        } else {
//            return badRequest(INVALID_REQUEST_PLEASE_CONTACT_AN_ADMINISTRATOR_IF_THIS_CONTINUES);
//        }
//
//        return Response.ok(new Viewable(AUTHORIZE_TEMPLATE, loginModel)).build();
//    }

//
//    private LoginCookie makeLoginCookie(final User user, final String secret, final Date expires, final boolean rememberMe) {
//        final LoginCookie loginCookie = new LoginCookie();
//        loginCookie.setUser(user);
//        loginCookie.setSecret(secret);
//        loginCookie.setExpires(expires);
//        loginCookie.setRememberMe(rememberMe);
//
//        try {
//            TXHelper.withinTransaction(em, () -> {
//                em.persist(loginCookie);
//                em.flush();
//            });
//        } catch (Exception e) {
//            LOG.log(Level.SEVERE, "Failed to create a login cookie", e);
//            return null;
//        }
//
//        return loginCookie;
//    }
//
//    /**
//     * Helper function to generate a token from a permission token
//     *
//     * @param type            type of token to generate
//     * @param permissionToken the token that triggered the generation
//     * @param scopes          the list of scopes for the token
//     * @return generated token
//     */
//    private Token fromPermissionToken(final TokenType type, final Token permissionToken, final Set<AcceptedScope> scopes) {
//        return QueryUtil.generateToken(
//                em, type, permissionToken.getClient(), permissionToken.getUser(),
//                Token.getExpires(permissionToken.getClient(), type),
//                permissionToken.getRedirectUri(), scopes, null, null
//        );
////    }
//
//
//
//
//
//
//    // successfully authenticated the user
//    final Set<ClientScope> toAsk = QueryUtil.getScopesToRequest(em, client, user, scopes);
//        if (!toAsk.isEmpty()) {
//        // we need to generate a temporary token for them to get to the next step with
//        final Token token = QueryUtil.generatePermissionToken(em, user, client, redirectUri);
//        final PermissionsModel permissionsModel = new PermissionsModel(token, toAsk, rememberMe);
//        permissionsModel.setClient(client);
//        permissionsModel.setURLs(req);
//        permissionsModel.setState(state);
//        permissionsModel.setRedirectUri(redirectUri);
//        return Response.ok(new Viewable(TEMPLATES_PERMISSIONS, permissionsModel)).build();
//    } else {
//        // accept all the always permissions
//        final Set<ClientScope> clientScopes = QueryUtil.getScopes(em, client, scopes);
//        final Set<AcceptedScope> acceptedScopes = clientScopes.stream()
//                .map(clientScope -> QueryUtil.acceptScope(em, user, clientScope))
//                .collect(Collectors.toSet());
//
//        final TokenType type = getTokenType(responseType);
//        // redirect with token since they've already asked for all the permissions
//        final Token token = QueryUtil.generateToken(em, type, client, user, Token.getExpires(client, type),
//                redirectUri, acceptedScopes, null, null);
//        return getRedirectResponse(redirectUri, state, token, rememberMe);
//    }


//    private static final String IPV4_ADDRESS_REGEX = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$",
//            HTTPS = "HTTPS",
//            OAUTH2_CLOUD_LOGIN_COOKIE = "OAuth2 Cloud Login Cookie";
//
//    private static final long ONE_MONTH = 1000L * 60L * 60L * 24L * 30L;
//
//    private NewCookie getNewCookie(final Token tkn, final Boolean rememberMe) {
//        final Date expires;
//
//        LoginCookie loginCookie = CookieUtil.getLoginCookie(em, req, tkn.getClient());
//        if (loginCookie != null) {
//            // we should re-use the same values
//            expires = loginCookie.getExpires();
//        } else {
//            expires = new Date(System.currentTimeMillis() + ONE_MONTH);
//            // we should issue a new cookie
//            loginCookie = makeLoginCookie(tkn.getUser(), randomAlphanumeric(64), expires, rememberMe);
//        }
//
//        final int maxAge = rememberMe ? (new Long(ONE_MONTH / 1000L)).intValue() : NewCookie.DEFAULT_MAX_AGE;
//        final Date expiry = rememberMe ? expires : null;
//
//        final boolean isHTTPS = HTTPS.equalsIgnoreCase(forwardedProto);
//
//        String cookieDomain = req.getUriInfo().getBaseUri().getHost();
//        if (cookieDomain != null) {
//            if (cookieDomain.matches(IPV4_ADDRESS_REGEX)) {
//                // don't put a domain on a cookie that is passed to an IP address
//                cookieDomain = null;
//            } else {
//                // the domain should be the last two pieces of the domain name
//                final List<String> pcs = Arrays.asList(cookieDomain.split("\\."));
//                cookieDomain = pcs.subList(Math.max(0, pcs.size() - 2), pcs.size())
//                        .stream()
//                        .collect(Collectors.joining("."));
//            }
//        }
//
//        return new NewCookie(CookieUtil.getCookieName(tkn.getClient()), loginCookie.getSecret(), "/", cookieDomain, NewCookie.DEFAULT_VERSION,
//                OAUTH2_CLOUD_LOGIN_COOKIE, maxAge, expiry, isHTTPS, true);
//    }
}
