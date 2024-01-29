package bootiful.elasticSearch.base;

import bootiful.elasticSearch.user.AuthUser;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author pari on 19/01/24
 */
public class BaseController {
    private final List<String> admins;

    public BaseController(List<String> admins) {
        this.admins = admins;
    }

    protected void enrichModelWithPrincipal(final Model model, final AuthUser authenticatedUser) {
        model.addAttribute("user", authenticatedUser);
        model.addAttribute("is_admin", isAdmin(authenticatedUser));
    }

    protected void ensureAdmin(AuthUser authenticatedUser) {
        if (authenticatedUser == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        // ensure only admins can go here
        final String username = authenticatedUser.getUsername();
        if (!this.admins.contains(username)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    protected boolean isAdmin(AuthUser authenticatedUser) {
        if (authenticatedUser == null) {
            return false;
        }
        final String username = authenticatedUser.getUsername();
        return this.admins.contains(username);
    }

    static List<String> loadAdmins() {
        final String[] admins = System.getenv("ADMINS").split(",");
        return Arrays.stream(admins).map(String::trim).collect(Collectors.toList());
    }

}
