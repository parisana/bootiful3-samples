package bootiful.elasticSearch.user;

import org.springframework.security.core.GrantedAuthority;

import java.lang.reflect.Field;
import java.util.Collection;

/**
 * @author pari on 28/01/24
 */
public class AuthUser extends org.springframework.security.core.userdetails.User {

    public AuthUser(String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
    }

    public AuthUser(String username, String password, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
    }
    Object getAttribute(String any) {
        try {
            final Field declaredField = this.getClass().getDeclaredField(any);
            return declaredField.get(this);
        } catch (NoSuchFieldException e) {
            return null;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
