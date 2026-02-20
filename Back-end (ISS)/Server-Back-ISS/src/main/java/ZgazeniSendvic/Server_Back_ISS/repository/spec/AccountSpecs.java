package ZgazeniSendvic.Server_Back_ISS.repository.spec;

import ZgazeniSendvic.Server_Back_ISS.model.*;
import org.springframework.data.jpa.domain.Specification;

public final class AccountSpecs {

    private AccountSpecs() {}

    public static Specification<Account> search(String q) {
        return (root, query, cb) -> {
            if (q == null || q.isBlank()) return cb.conjunction();
            String like = "%" + q.trim().toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("email")), like),
                    cb.like(cb.lower(root.get("name")), like),
                    cb.like(cb.lower(root.get("lastName")), like),
                    cb.like(cb.lower(root.get("phoneNumber")), like)
            );
        };
    }

    public static Specification<Account> confirmed(Boolean confirmed) {
        return (root, query, cb) -> {
            if (confirmed == null) return cb.conjunction();
            return cb.equal(root.get("confirmed"), confirmed);
        };
    }

    public static Specification<Account> type(String type) {
        return (root, query, cb) -> {
            if (type == null || type.isBlank()) return cb.conjunction();

            return switch (type.trim().toUpperCase()) {
                case "ADMIN"  -> cb.equal(root.type(), Admin.class);
                case "DRIVER" -> cb.equal(root.type(), Driver.class);
                case "USER"   -> cb.equal(root.type(), User.class);
                default       -> cb.disjunction(); // invalid type => no results
            };
        };
    }
}
