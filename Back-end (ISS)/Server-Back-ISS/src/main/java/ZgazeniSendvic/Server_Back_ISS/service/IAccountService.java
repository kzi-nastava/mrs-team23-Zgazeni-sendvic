package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.model.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;

public interface IAccountService {
    public Collection<Account> getAll();

    public Page<Account> getAllPaged(String q, String type, Boolean confirmed, Pageable pageable);

    public Account findAccount(Long studentId);
    public Account insert(Account account);
    public Account update(Account account);
    public Account delete(Long accountId);
    public void deleteAll();
}
