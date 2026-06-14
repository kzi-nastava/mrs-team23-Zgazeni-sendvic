package ZgazeniSendvic.Server_Back_ISS.security;

import ZgazeniSendvic.Server_Back_ISS.model.Account;
import ZgazeniSendvic.Server_Back_ISS.model.Admin;
import ZgazeniSendvic.Server_Back_ISS.model.Driver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CustomUserDetails implements UserDetails {

    @Autowired
    Account account;

    public CustomUserDetails(Account account) {
        this.account = account;
    }

    public Long getId() {
        return account.getId();
    }

    public Account getAccount() {
        return account;
    }

    public boolean isBanned() {
        return Boolean.TRUE.equals(account.getIsBanned());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        String role;

        if (account instanceof Admin) {
            role = "ROLE_ADMIN";
        } else if (account instanceof Driver) {
            role = "ROLE_DRIVER";
        } else {
            role = "ROLE_USER";
        }

        return List.of(new SimpleGrantedAuthority(role));
    }


    @Override public String getUsername() { return account.getEmail(); }
    @Override public String getPassword() { return account.getPassword(); }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}

