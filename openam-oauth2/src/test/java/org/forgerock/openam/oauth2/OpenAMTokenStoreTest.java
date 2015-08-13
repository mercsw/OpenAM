/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2014-2015 ForgeRock AS.
 */

package org.forgerock.openam.oauth2;

import static org.assertj.core.api.Assertions.*;
import static org.forgerock.json.JsonValue.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import com.iplanet.sso.SSOTokenManager;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

import com.sun.identity.shared.debug.Debug;
import org.forgerock.json.JsonValue;
import org.forgerock.oauth2.core.AccessToken;
import org.forgerock.oauth2.core.OAuth2ProviderSettingsFactory;
import org.forgerock.oauth2.core.OAuth2Request;
import org.forgerock.oauth2.core.exceptions.InvalidGrantException;
import org.forgerock.oauth2.core.exceptions.NotFoundException;
import org.forgerock.oauth2.core.exceptions.ServerException;
import org.forgerock.oauth2.restlet.RestletOAuth2Request;
import org.forgerock.openam.cts.exceptions.CoreTokenException;
import org.forgerock.openam.oauth2.guice.OAuth2GuiceModule;
import org.forgerock.openam.utils.RealmNormaliser;
import org.forgerock.openidconnect.OpenIdConnectClientRegistrationStore;
import org.restlet.Request;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class OpenAMTokenStoreTest {

    private OpenAMTokenStore openAMtokenStore;

    private OAuthTokenStore tokenStore;
    private OAuth2ProviderSettingsFactory providerSettingsFactory;
    private OpenIdConnectClientRegistrationStore clientRegistrationStore;
    private RealmNormaliser realmNormaliser;
    private SSOTokenManager ssoTokenManager;
    private CookieExtractor cookieExtractor;
    private Request request;
    private OAuth2AuditLogger auditLogger;
    private Debug debug;

    @BeforeMethod
    public void setUp() {

        tokenStore = mock(OAuthTokenStore.class);
        providerSettingsFactory = mock(OAuth2ProviderSettingsFactory.class);
        clientRegistrationStore = mock(OpenIdConnectClientRegistrationStore.class);
        realmNormaliser = mock(RealmNormaliser.class);
        ssoTokenManager = mock(SSOTokenManager.class);
        request = mock(Request.class);
        cookieExtractor = mock(CookieExtractor.class);
        auditLogger = mock(OAuth2AuditLogger.class);
        debug = mock(Debug.class);

        openAMtokenStore = new OpenAMTokenStore(tokenStore, providerSettingsFactory, clientRegistrationStore,
                realmNormaliser, ssoTokenManager, cookieExtractor, auditLogger, debug);
    }

    @Test
    public void shouldReadAccessToken() throws Exception {
        //Given
        JsonValue token = json(object(
                field("tokenName", Collections.singleton("access_token")),
                field("realm", Collections.singleton("/testrealm"))));
        given(tokenStore.read("TOKEN_ID")).willReturn(token);
        ConcurrentHashMap<String, Object> attributes = new ConcurrentHashMap<String, Object>();
        attributes.put("realm", "/testrealm");
        given(request.getAttributes()).willReturn(attributes);
        
        given(realmNormaliser.normalise("/testrealm")).willReturn("/testrealm");

        OAuth2Request request = new RestletOAuth2Request(this.request);

        //When
        AccessToken accessToken = openAMtokenStore.readAccessToken(request, "TOKEN_ID");

        //Then
        assertThat(accessToken).isNotNull();
        assertThat(request.getToken(AccessToken.class)).isSameAs(accessToken);
    }

    @Test (expectedExceptions = InvalidGrantException.class)
    public void shouldNotReadOtherRealmsAccessToken() throws Exception {
        //Given
        JsonValue token = json(object(
                field("tokenName", Collections.singleton("access_token")),
                field("realm", Collections.singleton("/otherrealm"))));
        given(tokenStore.read("TOKEN_ID")).willReturn(token);
        ConcurrentHashMap<String, Object> attributes = new ConcurrentHashMap<String, Object>();
        given(request.getAttributes()).willReturn(attributes);
        attributes.put("realm", "/testrealm");

        OAuth2Request request = new RestletOAuth2Request(this.request);

        //When
        AccessToken accessToken = openAMtokenStore.readAccessToken(request, "TOKEN_ID");

        //Then
        // expect InvalidGrantException
    }

    @Test (expectedExceptions = InvalidGrantException.class)
    public void shouldReadAccessTokenWhenNull() throws Exception {
        //Given
        given(tokenStore.read("TOKEN_ID")).willReturn(null);
        OAuth2Request request = new RestletOAuth2Request(this.request);

        //When
        openAMtokenStore.readAccessToken(request, "TOKEN_ID");

        //Then
        //Expected InvalidGrantException
    }

    @Test (expectedExceptions = ServerException.class)
    public void shouldFailToReadAccessToken() throws Exception {

        //Given
        doThrow(CoreTokenException.class).when(tokenStore).read("TOKEN_ID");
        OAuth2Request request = new RestletOAuth2Request(this.request);

        //When
        openAMtokenStore.readAccessToken(request, "TOKEN_ID");

        //Then
        //Expected ServerException
    }

    @Test (expectedExceptions = NotFoundException.class)
    public void shouldFailWhenNoProvider() throws Exception {

        //Given
        OAuth2Request request = new RestletOAuth2Request(this.request);
        doThrow(NotFoundException.class).when(providerSettingsFactory).get(request);

        //When
        openAMtokenStore.createAccessToken(null, null, null, null, null, null, null, null, null, null, request);

        //Then
        //Expected NotFoundException
    }

    @Test
    public void realmAgnosticTokenStoreShouldIgnoreRealmMismatch() throws Exception {
        //Given
        OpenAMTokenStore realmAgnosticTokenStore = new OAuth2GuiceModule.RealmAgnosticTokenStore(tokenStore,
                providerSettingsFactory, clientRegistrationStore, realmNormaliser, ssoTokenManager, cookieExtractor,
                auditLogger, debug);
        JsonValue token = json(object(
                field("tokenName", Collections.singleton("access_token")),
                field("realm", Collections.singleton("/otherrealm"))));
        given(tokenStore.read("TOKEN_ID")).willReturn(token);
        ConcurrentHashMap<String, Object> attributes = new ConcurrentHashMap<String, Object>();
        given(request.getAttributes()).willReturn(attributes);
        attributes.put("realm", "/testrealm");

        OAuth2Request request = new RestletOAuth2Request(this.request);

        //When
        AccessToken accessToken = realmAgnosticTokenStore.readAccessToken(request, "TOKEN_ID");

        //Then
        assertThat(accessToken).isNotNull();
        assertThat(request.getToken(AccessToken.class)).isSameAs(accessToken);
    }
}
