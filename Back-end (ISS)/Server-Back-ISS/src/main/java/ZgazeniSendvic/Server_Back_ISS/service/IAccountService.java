package ZgazeniSendvic.Server_Back_ISS.service;

import ZgazeniSendvic.Server_Back_ISS.model.Account;

import java.util.Collection;

public interface IAccountService {
    public Collection<Account> getAll();

    public Account findAccount(Long studentId);
    public Account insert(Account account);
    public Account update(Account account);
    public Account delete(Long accountId);
    public void deleteAll();
}
