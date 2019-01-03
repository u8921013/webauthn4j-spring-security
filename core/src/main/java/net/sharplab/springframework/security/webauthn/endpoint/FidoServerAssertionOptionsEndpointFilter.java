package net.sharplab.springframework.security.webauthn.endpoint;

import com.webauthn4j.registry.Registry;
import com.webauthn4j.response.client.challenge.DefaultChallenge;
import com.webauthn4j.util.Base64UrlUtil;
import org.springframework.security.authentication.AuthenticationServiceException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * FIDO Server Endpoint for assertion options processing
 * With this endpoint, non-authorized user can observe requested username existence and his/her credentialId list.
 */
public class FidoServerAssertionOptionsEndpointFilter extends ServerEndpointFilterBase {

    /**
     * Default name of path suffix which will validate this filter.
     */
    public static final String FILTER_URL = "/webauthn/assertion/options";

    //~ Instance fields
    // ================================================================================================

    private OptionsProvider optionsProvider;

    public FidoServerAssertionOptionsEndpointFilter(Registry registry, OptionsProvider optionsProvider){
        super(FILTER_URL, registry);
        this.optionsProvider = optionsProvider;
    }


    @Override
    protected ServerResponse processRequest(HttpServletRequest request) {
        if (!request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }
        ServerPublicKeyCredentialGetOptionsRequest serverRequest;
        try {
            serverRequest = registry.getJsonMapper().readValue(request.getInputStream(), ServerPublicKeyCredentialGetOptionsRequest.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        String username = serverRequest.getUsername();
        AssertionOptions options = optionsProvider.getAssertionOptions(request, username, new DefaultChallenge());
        return new ServerPublicKeyCredentialGetOptionsResponse(
                Base64UrlUtil.encodeToString(options.getChallenge().getValue()),
                options.getAuthenticationTimeout(),
                options.getRpId(),
                options.getCredentials(),
                serverRequest.getUserVerification(),
                options.getAuthenticationExtensions());
    }

}